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
		if (hasFile) {
			if (!file.exists()) {
				System.out.println("Error: file does not exist");
				System.exit(0);
			}
		}
		else {
			File dir = new File("peer_" + id);
			if (!dir.exists())
				dir.mkdir();
			if (file.exists())
				file.delete();
			file = new File(filePath + ".temp");
		}
		this.config = config;
	}
	
	public byte[] read(int piece) {
		try {
			RandomAccessFile input = new RandomAccessFile(file, "r");
			input.seek(piece * config.getPieceSize());
			byte[] pieceBuffer = new byte[config.getPieceSize(piece)];
			input.read(pieceBuffer);
			input.close();
			return pieceBuffer;
		} catch (Exception e) {
			System.out.println("Error: could not read piece " + piece);
			System.out.println(e.getClass());
			return null;
		}
	}
	
	public void write(int piece, byte[] data) {
		try {
			RandomAccessFile output = new RandomAccessFile(file, "rw");
			output.seek(piece * config.getPieceSize());
			output.write(data);
			output.close();
		} catch (Exception e) {
			System.out.println("Error: could not write piece " + piece);
			System.out.println(e.getClass());
		}
	}
	
	public void finalize() {
		file.renameTo(new File(filePath));
	}

}
