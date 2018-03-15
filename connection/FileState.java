package connection;

import java.util.List;
import java.util.ArrayList;

public class FileState {
	
	private Bitfield bitfield;
	private ArrayList<Integer> activePieces;
	private ArrayList<Integer> newPieces;
	
	public FileState(Bitfield bitfield) {
		this.bitfield = bitfield;
		activePieces = new ArrayList<Integer>();
		newPieces = new ArrayList<Integer>();
	}
	
	public void receivedPiece(int piece) {
		bitfield.add(piece);
		activePieces.remove(new Integer(piece));
		newPieces.add(piece);
	}
	
	public void ignoredPiece(int piece) {
		if (piece != -1)
			activePieces.remove(new Integer(piece));
	}
	
	public int getRandomRequest(Bitfield other) {
		int piece = bitfield.getRandomPositionFrom(other, activePieces);
		if (piece != -1)
			activePieces.add(piece);
		return piece;
	}
	
	public boolean hasNewPieces() {
		return (newPieces.size() > 0);
	}
	
	public List<Integer> getNewPieces() {
		List<Integer> temp = newPieces;
		newPieces = new ArrayList<Integer>();
		return temp;
	}
	
	public int getNumPieces() {
		return bitfield.getSize();
	}
	
	public int getNumPiecesOwned() {
		return bitfield.getNumTrue();
	}
	
	public byte[] getByteArray() {
		return bitfield.getByteArray();
	}
	
	public boolean isInterested(Bitfield other) {
		return bitfield.isInterested(other);
	}
	
	public boolean isComplete() {
		return bitfield.isComplete();
	}

}
