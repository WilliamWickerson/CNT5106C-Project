package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Logger {

	private File logFile;
	private int myId;
	
	public Logger(int id) {
		myId = id;
		logFile = new File("log_peer_" + myId + ".log");
		logFile.delete();
	}
	
	private String getTime() {
		return new SimpleDateFormat("h:mm:ss a").format(new Date());
	}
	
	private void write(String message) {
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
			output.write(getTime() + ": " + message + System.lineSeparator());
			output.close();
		} catch(Exception e) {
			System.out.println("Error: could not write to log file");
			System.out.println(e.getClass().getName());
		}
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
