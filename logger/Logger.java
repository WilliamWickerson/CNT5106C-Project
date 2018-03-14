package logger;

import java.util.List;

public class Logger {

	private String fileName;
	private int myId;
	
	public Logger(int id) {
		myId = id;
		fileName = "log_peer_" + myId + ".log";
	}
	
	private void write(String message) {
		//TODO: write message to file
	}
	
	public void connectedTo(int id) {
		write("Peer " + myId + " makes a connection to Peer " + id + ".");
	}
	
	public void connectedFrom(int id) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void changedPreferredNeighbors(List<Integer> ids) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void changedOptimisticallyUnchokedNeighbor(int id) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void receivedUnchoked(int id) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void receivedChoking(int id) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void receivedHave(int id, int pieceNum) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void receivedInterested(int id) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void receivedNotInterested(int id) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void receivedPiece(int id, int pieceNum) {
		//TODO: use write() it takes care of clock and newline
	}
	
	public void completedDownload() {
		//TODO: use write() it takes care of clock and newline
	}
}
