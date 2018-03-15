package connection;

import logger.Logger;
import utility.ByteUtility;
import config.PeerInfo;
import java.net.Socket;
import java.util.Arrays;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;

public class PeerConnection {
	
	private static byte[] handshakeHeader = { 'P', '2', 'P', 'F', 'I', 'L', 'E', 'S', 'H' , 'A', 'R', 'I', 'N', 'G', 'P', 'R', 'O', 'J' };
	
	//Shared references
	private FileState fileState;
	private FileIO fileHandler;
	private Logger logger;
	//Own private data
	private Socket peerSocket;
	private Bitfield peerBitfield;
	private int peerId;
	private int dataReceived = 0;
	private int currentPiece = -1;
	@SuppressWarnings("unused") //Java's too stupid to realize this is being used...
	private boolean choked = false;
	private boolean choking = true;
	private boolean interested = false;
	
	public PeerConnection(ServerSocket serverSocket, FileState fileState, FileIO fileHandler, Logger logger, PeerInfo myInfo) {
		this.fileState = fileState;
		this.fileHandler = fileHandler;
		this.logger = logger;
		try {
			//Wait for connection from next peer
			peerSocket = serverSocket.accept();
		} catch(Exception e) {
			System.out.println("Error: could not form connection from peer");
			System.exit(1);
		}
		logger.connectedFrom(peerId);
		//Read for handshake response until it comes in
		byte[] peerHandshake = read(32);
		while (peerHandshake == null)
			peerHandshake = read(32);
		//Get peer Id from the handshake message
		this.peerId = ByteUtility.convertToInt(Arrays.copyOfRange(peerHandshake, 28, 32));
		//Check for correct header and padding
		if (!Arrays.equals(handshakeHeader, Arrays.copyOfRange(peerHandshake, 0, 18)) ||
			!Arrays.equals(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Arrays.copyOfRange(peerHandshake, 18, 28))) {
				System.out.println("Error: malformed handshake from peer " + this.peerId);
				System.exit(1);
		}
		//Send own handshake message
		sendHandshake(myInfo.getId());
		//If we own any pieces then send our bitfield as well
		if (fileState.getNumPiecesOwned() > 0)
			sendBitfield();
	}
	
	public PeerConnection(FileState fileState, FileIO fileHandler, Logger logger, PeerInfo myInfo, PeerInfo peerInfo) {
		this.fileState = fileState;
		this.fileHandler = fileHandler;
		this.logger = logger;
		this.peerId = peerInfo.getId();
		try {
			//Try to make connection to peer
			peerSocket = new Socket(peerInfo.getHostName(), peerInfo.getPort());
		} catch(Exception e) {
			System.out.println("Error: could not connect to peer " + peerInfo.getId());
			System.exit(1);
		}
		logger.connectedTo(peerId);
		//Send handshake message to connected peer
		sendHandshake(myInfo.getId());
		//Read for handshake response until it comes back
		byte[] peerHandshake = read(32);
		while (peerHandshake == null)
			peerHandshake = read(32);
		//Check for correct header, padding, and peer Id
		if (!Arrays.equals(handshakeHeader, Arrays.copyOfRange(peerHandshake, 0, 18)) ||
			!Arrays.equals(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, Arrays.copyOfRange(peerHandshake, 18, 28)) ||
			!(ByteUtility.convertToInt(Arrays.copyOfRange(peerHandshake, 28, 32)) == peerInfo.getId())) {
				System.out.println("Error: malformed handshake from peer " + peerInfo.getId());
				System.exit(1);
		}
		//Since we started the connection, we always send our bitfield
		sendBitfield();
	}
	
	private byte[] read(int numBytes) {
		InputStream input;
		try {
			//Get the input stream for the socket
			input = peerSocket.getInputStream();
			//If there are not enough bytes then return null
			if (input.available() < numBytes) {
				input.close();
				return null;
			}
			//Create a buffer and read in numBytes
			byte[] buffer = new byte[numBytes];
			input.read(buffer);
			//Close the stream and return the buffer
			input.close();
			return buffer;
		} catch(Exception e) {
			System.out.println("Error: could not read from socket with peer: " + peerId);
			return null;
		}
	}
	
	private void send(byte[] bytes) {
		OutputStream output;
		try {
			//Get an output stream and write bytes to it
			output = peerSocket.getOutputStream();
			output.write(bytes);
			//Close the output stream
			output.close();
		} catch(Exception e) {
			System.out.println("Error: could not write to socket with peer: " + peerId);
		}
	}
	
	private void sendHandshake(int myId) {
		byte[] zeroPadding = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] idBuffer = ByteUtility.convertInt(myId);
		send(ByteUtility.concatenate(handshakeHeader, zeroPadding, idBuffer));
	}
	
	public void sendChoke() {
		choking = true;
		byte[] length = ByteUtility.convertInt(1);
		byte[] type = { 0 };
		send(ByteUtility.concatenate(length, type));
	}
	
	public void sendUnchoke() {
		//We don't need to send another unchoke message if we already are
		if (!choking)
			return;
		choking = false;
		byte[] length = ByteUtility.convertInt(1);
		byte[] type = { 1 };
		send(ByteUtility.concatenate(length, type));
	}
	
	private void sendInterested() {
		interested = true;
		byte[] length = ByteUtility.convertInt(1);
		byte[] type = { 2 };
		send(ByteUtility.concatenate(length, type));
	}
	
	private void sendNotInterested() {
		interested = false;
		byte[] length = ByteUtility.convertInt(1);
		byte[] type = { 3 };
		send(ByteUtility.concatenate(length, type));
	}
	
	public void sendHave(int pieceNum) {
		byte[] length = ByteUtility.convertInt(5);
		byte[] type = { 4 };
		byte[] pieceNumber = ByteUtility.convertInt(pieceNum);
		send(ByteUtility.concatenate(length, type, pieceNumber));
	}
	
	private void sendBitfield() {
		byte[] byteArray = fileState.getByteArray();
		byte[] length = ByteUtility.convertInt(byteArray.length + 1);
		byte[] type = { 5 };
		send(ByteUtility.concatenate(length, type, byteArray));
	}
	
	private void sendRequest(int pieceNum) {
		byte[] length = ByteUtility.convertInt(5);
		byte[] type = { 6 };
		byte[] pieceNumber = ByteUtility.convertInt(pieceNum);
		send(ByteUtility.concatenate(length, type, pieceNumber));
	}
	
	private void sendPiece(int pieceNum) {
		byte[] piece = fileHandler.read(pieceNum);
		byte[] length = ByteUtility.convertInt(piece.length + 4 + 1);
		byte[] type = { 7 };
		byte[] pieceNumber = ByteUtility.convertInt(pieceNum);
		send(ByteUtility.concatenate(length, type, pieceNumber, piece));
	}
	
	public void handleConnection() {
		byte[] lengthBuffer;
		while ((lengthBuffer = read(4)) != null) {
			int length = ByteUtility.convertToInt(lengthBuffer);
			byte[] buffer = read(length);
			int type = buffer[0];
			switch(type) {
				case 0: //choke
					choked = true;
					fileState.ignoredPiece(currentPiece);
					currentPiece = -1;
					logger.receivedChoking(peerId);
					break;
				case 1: //unchoke
					choked = false;
					currentPiece = fileState.getRandomRequest(peerBitfield);
					sendRequest(currentPiece);
					logger.receivedUnchoked(peerId);
					break;
				case 2: //interested
					//TODO: Does this do anything?
					logger.receivedInterested(peerId);
					break;
				case 3: //not interested
					//TODO: Does this do anything?
					logger.receivedNotInterested(peerId);
					break;
				case 4: //have
					byte[] pieceNumBuffer = Arrays.copyOfRange(buffer, 1, buffer.length);
					int pieceNum = ByteUtility.convertToInt(pieceNumBuffer);
					peerBitfield.add(pieceNum);
					//If we find our peer has a new interesting piece then send interested
					if (!interested) {
						if (fileState.isInterested(peerBitfield)) {
							interested = true;
							sendInterested();
						}
					}
					logger.receivedHave(peerId, pieceNum);
					break;
				case 5: //bitfield
					byte[] bitfieldBuffer = Arrays.copyOfRange(buffer, 1, buffer.length);
					peerBitfield = new Bitfield(fileState.getNumPieces(), bitfieldBuffer);
					if (fileState.isInterested(peerBitfield)) {
						sendInterested();
					}
					else {
						sendNotInterested();
					}
					break;
				case 6: //request
					if (!choking) {
						pieceNumBuffer = Arrays.copyOfRange(buffer, 1, buffer.length);
						pieceNum = ByteUtility.convertToInt(pieceNumBuffer);
						sendPiece(pieceNum);
					}
					break;
				case 7: //piece
					pieceNumBuffer = Arrays.copyOfRange(buffer, 1, 5);
					byte[] pieceBuffer = Arrays.copyOfRange(buffer, 5, buffer.length);
					pieceNum = ByteUtility.convertToInt(pieceNumBuffer);
					dataReceived += pieceBuffer.length;
					fileHandler.write(pieceNum, pieceBuffer);
					fileState.receivedPiece(pieceNum);
					logger.receivedPiece(peerId, pieceNum);
					break;
			}
		}
	}
	
	public void checkStillInterested() {
		if (interested) {
			if (!fileState.isInterested(peerBitfield)) {
				interested = false;
				sendNotInterested();
			}
		}
	}
	
	public boolean getInterested() {
		return interested;
	}
	
	public int getDataReceived() {
		int temp = dataReceived;
		dataReceived = 0;
		return temp;
	}
	
	public boolean isFinished() {
		return (fileState.isComplete() && peerBitfield.isComplete());
	}
	
	public boolean close() {
		try {
			peerSocket.close();
		} catch (Exception e) {
			System.out.println("Error: could not close connection");
			return false;
		}
		return true;
	}
	
}
