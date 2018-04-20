

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import config.CommonConfig;
import config.PeerInfo;
import config.PeerInfoConfig;
import connection.Bitfield;
import connection.FileIO;
import connection.FileState;
import connection.PeerConnection;
import logger.Logger;

public class peerProcess {
	
	private static Random rand = new Random();
	
	public static void main(String[] args) {
		//Get id from command line input
		int id = Integer.parseInt(args[0]);
		//Read config files
		CommonConfig commonConfig = new CommonConfig();
		PeerInfoConfig peerConfig = new PeerInfoConfig();
		//Find self in peer info, if not there then exit
		PeerInfo myInfo = peerConfig.getPeer(id);
		if (myInfo == null) {
			System.out.println("Error: id not found in PeerInfo.cfg");
			System.exit(1);
		}
		//Set up the two File IO
		FileIO fileHandler = new FileIO(id, commonConfig, myInfo.getHasFile());
		Logger logger = new Logger(id);
		//Create a FileState to keep track of progress
		FileState fileState = new FileState(new Bitfield(commonConfig.getNumPieces(), myInfo.getHasFile()));
		//Open server socket for all peer connections after in the list
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(myInfo.getPort());
			//Set the server socket timeout so that serverSocket.accept() does not wait forever on error
			serverSocket.setSoTimeout(10000);
		} catch (Exception e) {
			System.out.println("Error: could not bind to port " + myInfo.getPort());
			System.exit(1);
		}
		//Create list of PeerConnection to add to
		ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>();
		boolean foundMyself = false;
		//Connect to all peers before in the list, and wait as a server for all peers after
		for (PeerInfo info : peerConfig.getPeers()) {
			if (!foundMyself) {
				if (info == myInfo) {
					foundMyself = true;
					continue;
				}
				connections.add(new PeerConnection(fileState, fileHandler, logger, myInfo, info));
			}
			else {
				connections.add(new PeerConnection(serverSocket, fileState, fileHandler, logger, myInfo));
			}
		}
		//Try to close the server socket since we've made all connections
		try {
			serverSocket.close();
		} catch (Exception e) {
			System.out.println("Error: could not close server socket");
		}
		
		//Keep track of the last unchoke and optimistic unchoke
		long lastUnchoke = System.currentTimeMillis();
		long lastOptimisticUnchoke = System.currentTimeMillis();
		//We don't need to log file complete multiple times
		HashMap<Integer, Boolean> loggedComplete = new HashMap<Integer, Boolean>();
		//We also don't need to log file complete if it started with the file
		for (PeerInfo info : peerConfig.getPeers())
			loggedComplete.put(new Integer(info.getId()), new Boolean(info.getHasFile()));
		//Keep track of the preferredNeighbors and optimistically unchoked neighbor
		List<PeerConnection> preferredNeighbors = new ArrayList<PeerConnection>();
		PeerConnection optimisticNeighbor = null;
		//Set up initial preferred neighbors and optimistic neighbor so we don't wait for first interval
		preferredNeighbors = handleUnchoking(connections, preferredNeighbors, optimisticNeighbor, commonConfig.getNumPreferredNeighbors(), fileState.isComplete(), logger);
		optimisticNeighbor = handleOptimisticUnchoking(connections, preferredNeighbors, optimisticNeighbor, logger);
		
		while (!finished(connections) || !fileState.isComplete()) {
			//Let all connections handle all incoming messages
			for(PeerConnection connection : connections) {
				connection.handleConnection();
			}
			//This is assumed based on what 'have' should do
			if (fileState.hasNewPieces()) {
				//Send have messages for each newly received piece
				for (int piece : fileState.getNewPieces()) {
					for (PeerConnection connection : connections) {
						connection.sendHave(piece);
					}
				}
				//Checks whether we still need pieces from this peer
				for (PeerConnection connection : connections) {
					connection.checkStillInterested();
				}
			}
			//If the unchoking interval has passed then handle it
			if (System.currentTimeMillis() - lastUnchoke > commonConfig.getUnchokingInterval().toMillis()) {
				preferredNeighbors = handleUnchoking(connections, preferredNeighbors, optimisticNeighbor, commonConfig.getNumPreferredNeighbors(), fileState.isComplete(), logger);
				lastUnchoke = System.currentTimeMillis();
			}
			//If the optimistic unchoking interval has passed then handle it
			if (System.currentTimeMillis() - lastOptimisticUnchoke > commonConfig.getOptimisticUnchokingInterval().toMillis()) {
				optimisticNeighbor = handleOptimisticUnchoking(connections, preferredNeighbors, optimisticNeighbor, logger);
				lastOptimisticUnchoke = System.currentTimeMillis();
			}
			//If this process has completed the download then log it
			if (!loggedComplete.get(id) && fileState.isComplete()) {
				loggedComplete.put(new Integer(id), new Boolean(true));
				logger.completedDownload(id);
				//Finalize the file writing since we're done
				fileHandler.finalize();
			}
			//If any of the peers have completed the download then log it
			for (PeerConnection connection : connections) {
				int peerId = connection.getPeerId();
				if (!loggedComplete.get(peerId) && connection.peerIsFinished()) {
					loggedComplete.put(new Integer(peerId), new Boolean(true));
					logger.completedDownload(peerId);
				}
			}
		}
		
		//Clean up afterwards
		for (PeerConnection connection : connections) {
			connection.close();
		}
	}
	
	private static boolean finished(List<PeerConnection> connections) {
		for (PeerConnection connection : connections) {
			if (!connection.peerIsFinished())
				return false;
		}
		return true;
	}
	
	private static List<PeerConnection> handleUnchoking(List<PeerConnection> connections,
			List<PeerConnection> previousPreferredNeighbors, PeerConnection optimisticNeighbor,
			int size, boolean complete, Logger logger) {
		//Get all candidates to be unchoked
		List<PeerConnection> candidates = new ArrayList<PeerConnection>();
		for (PeerConnection connection : connections) {
			if (connection.getPeerInterested())
				candidates.add(connection);
		}
		//Weigh the candidates by dataReceived if not complete or just a random number otherwise
		List<Integer> weights = new ArrayList<Integer>();
		for (PeerConnection connection : candidates) {
			if (complete) {
				weights.add(rand.nextInt(1000000));
			}
			else {
				weights.add(connection.getDataReceived());
			}
		}
		//Sort the candidates by descending weights and choose the k largest
		candidates.sort((a, b) -> Integer.compare(weights.get(candidates.indexOf(b)), weights.get(candidates.indexOf(a))));
		List<PeerConnection> preferredNeighbors = candidates.subList(0, Math.min(size, candidates.size()));
		//Unchoke all of the newly selected neighbors and keep track of ids
		List<Integer> preferredNeighborIds = new ArrayList<Integer>();
		for (PeerConnection connection : preferredNeighbors) {
			connection.sendUnchoke();
			preferredNeighborIds.add(connection.getPeerId());
		}
		//Choke all of the previously preferred neighbors
		for (PeerConnection connection : previousPreferredNeighbors) {
			if (!preferredNeighbors.contains(connection) && (connection != optimisticNeighbor))
				connection.sendChoke();
		}
		//Log the new preferred neighbors and return the list
		logger.changedPreferredNeighbors(preferredNeighborIds);
		return preferredNeighbors;
	}
	
	private static PeerConnection handleOptimisticUnchoking(List<PeerConnection> connections,
			List<PeerConnection> preferredNeighbors, PeerConnection previousOptimisticNeighbor, Logger logger) {
		//Get all candidates to be unchoked
		List<PeerConnection> candidates = new ArrayList<PeerConnection>();
		for (PeerConnection connection : connections) {
			if (connection.getPeerInterested() && connection.getChoking())
				candidates.add(connection);
		}
		//If there are no candidates then just return null, no one to unchoke
		if (candidates.size() == 0)
			return null;
		//Otherwise get a random candidate and unchoke him
		PeerConnection optimisticNeighbor = candidates.get(rand.nextInt(candidates.size()));
		optimisticNeighbor.sendUnchoke();
		//Choke the previous neighbor if it's not currently a preferred neighbor
		if ((previousOptimisticNeighbor != null) && !preferredNeighbors.contains(previousOptimisticNeighbor))
			previousOptimisticNeighbor.sendChoke();
		//Log the new optimistic neighbor and return it
		logger.changedOptimisticallyUnchokedNeighbor(optimisticNeighbor.getPeerId());
		return optimisticNeighbor;
	}
	
}
