package connection;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Bitfield {
	
	private static Random rand = new Random();
	
	private boolean[] bitArray;
	
	public Bitfield(int size, boolean complete) {
		bitArray = new boolean[size];
		for (int i = 0; i < size; i++) {
			bitArray[i] = complete;
		}
	}
	
	public Bitfield(int size, byte[] bitfield) {
		bitArray = new boolean[size];
		for (int i = 0; i < size; i++) {
			int bit = 128 >> (i % 8);
			bitArray[i] = (bitfield[i / 8] & bit) != 0;
		}
	}
	
	public void add(int position) {
		bitArray[position] = true;
	}
	
	public boolean has(int position) {
		return bitArray[position];
	}
	
	public int getRandomPositionFrom(Bitfield other, List<Integer> list) {
		//Look through our bitArray for things this has but not the others
		ArrayList<Integer> options = new ArrayList<Integer>();
		for (int i = 0; i < this.getSize(); i++) {
			if (other.has(i) && !this.has(i) && !list.contains(i))
				options.add(i);
		}
		//If there are some options then return a random one
		if (options.size() > 0)
			return options.get(rand.nextInt(options.size()));
		//Otherwise return -1 signifying none was found
		else
			return -1;
	}
	
	public byte[] getByteArray() {
		//Get the size and size of the array
		int size = this.getSize();
		int arrSize = (size % 8 == 0) ? size / 8 : size / 8 + 1;
		byte[] arr = new byte[arrSize];
		//Zero out the byte array so that we can add to an empty list
		for (int i = 0; i < arrSize; i++) {
			arr[i] = 0;
		}
		//Add values to the byte array with logical or
		for (int i = 0; i < size; i++) {
			if (this.has(i))
				arr[i / 8] |= 128 >> (i % 8);
		}
		return arr;
	}
	
	public boolean isInterested(Bitfield other) {
		for (int i = 0; i < this.getSize(); i++) {
			if (other.has(i) && !this.has(i))
				return true;
		}
		return false;
	}
	
	public boolean isComplete() {
		for (int i = 0; i < this.getSize(); i++) {
			if (!this.has(i))
				return false;
		}
		return true;
	}
	
	public int getSize() {
		return bitArray.length;
	}

}
