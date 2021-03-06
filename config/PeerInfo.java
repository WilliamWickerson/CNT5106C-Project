package config;

import java.util.StringTokenizer;

public class PeerInfo {
	
	private int id;
	private String hostName;
	private int port;
	private boolean hasFile;
	
	protected PeerInfo(String line) {
		//Open up a stream tokenizer and make sure there are 4 tokens
		StringTokenizer peerInfoFromLine = new StringTokenizer(line);
		if(peerInfoFromLine.countTokens() != 4) {
			System.out.println("Error: file format is not as specified");
			System.exit(1);
		}
		//Read in the four tokens from the line
		this.id = Integer.valueOf(peerInfoFromLine.nextToken());
		this.hostName = peerInfoFromLine.nextToken();
		this.port = Integer.valueOf(peerInfoFromLine.nextToken());
		this.hasFile = Integer.valueOf(peerInfoFromLine.nextToken()) == 1 ? true : false;
	}
	
	public int getId() {
		return id;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean getHasFile() {
		return hasFile;
	}
}
