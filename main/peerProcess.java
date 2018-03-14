package main;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import config.CommonConfig;
import config.PeerInfo;
import config.PeerInfoConfig;
import connection.Bitfield;
import connection.FileIO;
import connection.FileState;
import connection.PeerConnection;
import logger.Logger;

public class peerProcess {
	public static void main(String[] args) {
		//Get id from command line input
		int id = Integer.parseInt(args[0]);
		//Read config files
		CommonConfig commonConfig = new CommonConfig();
		PeerInfoConfig peerConfig = new PeerInfoConfig();
		//Set up the two File IO
		FileIO fileHandler = new FileIO(id, commonConfig);
		Logger logger = new Logger(id);
		//Find self in peer info, if not there then exit
		PeerInfo myInfo = peerConfig.getPeer(id);
		if (myInfo == null) {
			System.out.println("Error: id not found in PeerInfo.cfg");
			System.exit(1);
		}
		//Create a FileState to keep track of progress
		FileState fileState = new FileState(new Bitfield(commonConfig.getNumPieces(), myInfo.getHasFile()));
		//Open server socket for all peer connections after in the list
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(myInfo.getPort());
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
		try {
			serverSocket.close();
		} catch (Exception e) {
			System.out.println("Error: could not close server socket");
		}
		
		/* Basic idea
		while (notFinished) {
			for(PeerConnection connection : connections) {
				connection.handleConnection();
			}
			//This is assumed based on what 'have' should do
			if (fileState.hasNewPieces()) {
				for (int piece : fileState.getNewPieces()) {
					for (PeerConnection connection : connections) {
						connection.sendHave(piece);
					}
				}
			}
			//TODO: figure out how to write this
			if (currentTime - lastTime > commonConfig.getUnchokingInterval()) {
				redertermine preferred neighbors
				choke/unchoke accordingly
				logger.changedPreferredNeighbors(preferred neighbors)
			}
			if (currentTime - lastTime > commonConfig.getOptimisticUnchokingInterval()) {
				redertimine optimistic choking
				logger.changedOptimisticallyUnchokedNeighbor(new optimistic unchoked)
			}
			if (fileState.isComplete() != loggedComplete) {
				loggedComplete = true
				logger.completedDownload();
			}
		}
		 */
		
		//Clean up afterwards
		for (PeerConnection connection : connections) {
			connection.close();
		}
	}
}
