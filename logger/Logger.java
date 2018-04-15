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
		//Get file and delete any previous log if it exists
		logFile = new File("log_peer_" + myId + ".log");
		logFile.delete();
	}
	
	private String getTime() {
		//Gets the current time and converts it to a nice format
		return new SimpleDateFormat("h:mm:ss a").format(new Date());
	}
	
	private void write(String message) {
		try {
			//Open a BufferedWriter and append the message with the time to the log
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
			output.write(getTime() + ": " + message + System.lineSeparator());
			output.close();
		} catch(Exception e) {
			System.out.println("Error: could not write to log file");
		}
	}
	
	public void connectedTo(int id) {
		write("Peer " + myId + " makes a connection to Peer " + id + ".");
	}
	
	public void connectedFrom(int id) {
		write("Peer " + myId + " is connected from Peer " + id + ".");
	}
	
	public void changedPreferredNeighbors(List<Integer> ids) {
		String idList = "";
		if (ids.size() > 0)
			idList = Integer.toString(ids.get(0));
		for (int i = 1; i < ids.size(); i++)
			idList = idList + "," + Integer.toString(ids.get(i));
		write("Peer " + myId + " has the preferred neighbors " + idList + ".");
	}
	
	public void changedOptimisticallyUnchokedNeighbor(int id) {
		write("Peer " + myId + " has the optimistically unchoked neighbor " + id + ".");
	}
	
	public void receivedUnchoked(int id) {
		write("Peer " + myId + " is unchoked by " + id + ".");
	}
	
	public void receivedChoking(int id) {
		write("Peer " + myId + " is choked by " + id + ".");
	}
	
	public void receivedHave(int id, int pieceNum) {
		write("Peer " + myId + " received the 'have' message from " + id + " for the piece " + pieceNum + ".");
	}
	
	public void receivedInterested(int id) {
		write("Peer " + myId + " received the 'interested' message from " + id + ".");
	}
	
	public void receivedNotInterested(int id) {
		write("Peer " + myId + " received the 'not interested' message from " + id + ".");
	}
	
	public void receivedPiece(int id, int pieceNum, int numPieces) {
		write("Peer " + myId + " has downloaded the piece " + pieceNum + " from " + id + ". Now the number of pieces it has is " + numPieces + ".");
	}
	
	public void completedDownload(int id) {
		write("Peer " + id + " has downloaded the complete file.");
	}
}
