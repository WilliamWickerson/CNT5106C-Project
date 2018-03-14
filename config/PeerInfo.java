package config;

public class PeerInfo {
	
	private int id;
	private String hostName;
	private int port;
	private boolean hasFile;
	
	protected PeerInfo(String line) {
		//TODO: parse line from PeerInfo.cfg
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
