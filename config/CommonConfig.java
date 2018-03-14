package config;

import java.time.Duration;

public class CommonConfig {
	
	private int numPreferredNeighbors;
	private Duration unchokingInterval;
	private Duration optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	
	public CommonConfig() {
		//TODO: read in Common.cfg and set variables
	}
	
	public int getNumPreferredNeighbors() {
		return numPreferredNeighbors;
	}
	
	public Duration getUnchokingInterval() {
		return unchokingInterval;
	}
	
	public Duration getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public int getFileSize() {
		return fileSize;
	}
	
	public int getPieceSize() {
		return pieceSize;
	}
	
	public int getNumPieces() {
		int numPieces = fileSize / pieceSize;
		if (fileSize % pieceSize != 0)
			numPieces += 1;
		return numPieces;
	}
	
	public int getLastPieceSize() {
		return fileSize - (getNumPieces() - 1) * pieceSize;
	}
}
