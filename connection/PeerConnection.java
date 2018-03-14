package connection;

import logger.Logger;
import utility.ByteUtility;
import config.PeerInfo;
import java.net.Socket;
import java.util.Arrays;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class PeerConnection {
	
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
	private boolean choked = false;
	private boolean choking = false;
	private boolean interested = false;
	//Reader and writer for peerSocket
	private InputStream peerSocketReader;
	private OutputStream peerSocketSender;
	
	public PeerConnection(ServerSocket serverSocket, FileState fileState, FileIO fileHandler, Logger logger, PeerInfo myInfo) {
		this.fileState = fileState;
		this.fileHandler = fileHandler;
		this.logger = logger;
		//TODO: wait for connection as server
		logger.connectedFrom(peerId);
		try {
			peerSocketReader = peerSocket.getInputStream();
			peerSocketSender = peerSocket.getOutputStream();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public PeerConnection(FileState fileState, FileIO fileHandler, Logger logger, PeerInfo myInfo, PeerInfo peerInfo) {
		this.fileState = fileState;
		this.fileHandler = fileHandler;
		this.logger = logger;
		this.peerId = peerInfo.getId();
		//TODO: create connection as client
		logger.connectedTo(peerId);
		try {
			peerSocketReader = peerSocket.getInputStream();
			peerSocketSender = peerSocket.getOutputStream();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	private byte[] read(int numBytes) {
		//TODO: figure this out
	}
	
	private void send(byte[] bytes) {
		//TODO: figure this out
	}
	
	public void sendChoke() {
		if (choking)
			return;
		choking = true;
		byte[] length = ByteUtility.convertInt(1);
		byte[] type = { 0 };
		send(ByteUtility.concatenate(length, type));
	}
	
	public void sendUnchoke() {
		if (!choking)
			return;
		choking = false;
		//TODO: send unchoke message over socket, use send()
	}
	
	private void sendInterested() {
		if (interested)
			return;
		interested = true;
		//TODO: send interested message over socket, use send()
	}
	
	private void sendNotInterested() {
		if (!interested)
			return;
		interested = false;
		//TODO: send not interested message over socket, use send()
	}
	
	public void sendHave(int pieceNum) {
		//TODO: send have message over socket, use send()
	}
	
	private void sendBitfield() {
		byte[] byteArray = fileState.getByteArray();
		//TODO: send bitfield message with byteArray, use send()
	}
	
	private void sendRequest(int pieceNum) {
		//TODO: send request message with pieceNum, use send()
	}
	
	private void sendPiece(int pieceNum) {
		byte[] piece = fileHandler.read(pieceNum);
		byte[] length = ByteUtility.convertInt(piece.length + 4 + 1);
		byte[] pieceNumber = ByteUtility.convertInt(pieceNum);
		byte[] type = { 7 };
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
					/* This is what I assume have does
					peerBitfield.add(pieceNum);
					if (!interested) {
						if (fileState.isInterested(peerBitfield)) {
							sendInterested();
						}
					}
					*/
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
					fileHandler.write(pieceNum, pieceBuffer);
					fileState.receivedPiece(pieceNum);
					logger.receivedPiece(peerId, pieceNum);
					break;
			}
		}
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
			peerSocketReader.close();
			peerSocketSender.close();
			peerSocket.close();
		} catch (Exception e) {
			System.out.println("Error: could not close connection");
			return false;
		}
		return true;
	}
	
}
