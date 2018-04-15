package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Duration;

public class CommonConfig {
	
	private int numPreferredNeighbors;
	private Duration unchokingInterval;
	private Duration optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	
	public CommonConfig() {
		File configFile = new File("Common.cfg");
		if (!configFile.exists()) {
			System.out.println("Error: Common.cfg does not exist");
			System.exit(1);
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			//Read and parse first line
			String line = reader.readLine();
			String[] parts = line.split(" ");
			if (!parts[0].equals("NumberOfPreferredNeighbors"))
				malformed("NumberOfPreferredNeighbors");
			this.numPreferredNeighbors = Integer.parseInt(parts[1]);
			//Read and parse second line
			line = reader.readLine();
			parts = line.split(" ");
			if (!parts[0].equals("UnchokingInterval"))
				malformed("UnchokingInterval");
			this.unchokingInterval = Duration.ofSeconds(Integer.parseInt(parts[1]));
			//Read and parse third line
			line = reader.readLine();
			parts = line.split(" ");
			if (!parts[0].equals("OptimisticUnchokingInterval"))
				malformed("OptimisticUnchokingInterval");
			this.optimisticUnchokingInterval = Duration.ofSeconds(Integer.parseInt(parts[1]));
			//Read and parse fourth line
			line = reader.readLine();
			parts = line.split(" ");
			if (!parts[0].equals("FileName"))
				malformed("FileName");
			this.fileName = parts[1];
			//Read and parse fifth line
			line = reader.readLine();
			parts = line.split(" ");
			if (!parts[0].equals("FileSize"))
				malformed("FileSize");
			this.fileSize = Integer.parseInt(parts[1]);
			//Read and parse sixth line
			line = reader.readLine();
			parts = line.split(" ");
			if (!parts[0].equals("PieceSize"))
				malformed("PieceSize");
			this.pieceSize = Integer.parseInt(parts[1]);
			reader.close();
		} catch(Exception e) {
			malformed("READERROR");
		}
	}
		
	private void malformed(String location) {
		System.out.println("Error: Common.cfg is malformed at " + location);
		System.exit(1);
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
	
	private int getLastPieceSize() {
		return fileSize - (getNumPieces() - 1) * pieceSize;
	}
	
	public int getPieceSize(int piece) {
		if (piece == (getNumPieces() - 1))
			return getLastPieceSize();
		else
			return getPieceSize();
	}
}
