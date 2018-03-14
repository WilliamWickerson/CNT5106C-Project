package connection;

import config.CommonConfig;

public class FileIO {
	
	private String filePath;
	private CommonConfig config;
	
	public FileIO(int id, CommonConfig config) {
		this.filePath = "peer_" + id + "/" + config.getFileName();
		this.config = config;
	}
	
	public byte[] read(int piece) {
		//TODO: read in the piece
	}
	
	public void write(int piece, byte[] data) {
		//TODO: write the piece to file
	}
	
	public void finalize() {
		//TODO: finalize file when it's complete
		//only really necessary if we create a new file for each piece or keep a temp name
	}

}
