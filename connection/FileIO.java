package connection;

import java.io.File;
import java.io.RandomAccessFile;

import config.CommonConfig;

public class FileIO {
	
	private String filePath;
	private File file;
	private CommonConfig config;
	
	public FileIO(int id, CommonConfig config, boolean hasFile) {
		this.filePath = "peer_" + id + "/" + config.getFileName();
		this.file = new File(filePath);
		//If we're supposed to have the file, check we do
		if (hasFile) {
			//If we do not have it, exit
			if (!file.exists()) {
				System.out.println("Error: file does not exist");
				System.exit(0);
			}
		}
		//If we're not supposed to have the file, make sure dir exists and delete file if it exists
		else {
			File dir = new File("peer_" + id);
			dir.mkdir();
			file.delete();
			//Delete and start writing to .temp file to keep intention clean
			file = new File(filePath + ".temp");
			file.delete();
		}
		this.config = config;
	}
	
	public byte[] read(int piece) {
		try {
			//Open RandomAccessFile to read at desired position
			RandomAccessFile input = new RandomAccessFile(file, "r");
			//Seek to desired position
			input.seek(piece * config.getPieceSize());
			//Create a buffer of piece size and read data
			byte[] pieceBuffer = new byte[config.getPieceSize(piece)];
			input.read(pieceBuffer);
			input.close();
			return pieceBuffer;
		} catch (Exception e) {
			System.out.println("Error: could not read piece " + piece);
			return null;
		}
	}
	
	public void write(int piece, byte[] data) {
		try {
			//Open RandomAccessFile to write at desired position
			RandomAccessFile output = new RandomAccessFile(file, "rw");
			//Seek to desired position and write data
			output.seek(piece * config.getPieceSize());
			output.write(data);
			output.close();
		} catch (Exception e) {
			System.out.println("Error: could not write piece " + piece);
		}
	}
	
	public void finalize() {
		//Since we've been writing to <filePath>.temp rename to <filePath>
		file.renameTo(new File(filePath));
	}

}
