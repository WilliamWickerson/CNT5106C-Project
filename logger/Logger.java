package logger;

import java.util.List;

public class Logger {

	private String fileName;
	
	public Logger(int id) {
		fileName = "log_peer_" + id + ".log";
	}
	
	private void write(String message) {
		//TODO: write message to file
	}
	
	public void connectedTo(int id) {
		//TODO: use write()
	}
	
	public void connectedFrom(int id) {
		//TODO: use write()
	}
	
	public void changedPreferredNeighbors(List<Integer> ids) {
		//TODO: use write()
	}
	
	public void changedOptimisticallyUnchokedNeighbor(int id) {
		//TODO: use write()
	}
	
	public void receivedUnchoked(int id) {
		//TODO: use write()
	}
	
	public void receivedChoking(int id) {
		//TODO: use write()
	}
	
	public void receivedHave(int id, int pieceNum) {
		//TODO: use write()
	}
	
	public void receivedInterested(int id) {
		//TODO: use write()
	}
	
	public void receivedNotInterested(int id) {
		//TODO: use write()
	}
	
	public void receivedPiece(int id, int pieceNum) {
		//TODO: use write()
	}
	
	public void completedDownload() {
		//TODO: use write()
	}
}
