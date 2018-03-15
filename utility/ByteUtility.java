package utility;

public class ByteUtility {

	/**
	 * Returns a byte[] form of integer i
	 * @param i - the integer to convert
	 * @return the converted byte[] of length 4
	 */
	public static byte[] convertInt(int i) {
		return new byte[] {
			(byte)(i >> 24),
			(byte)(i >> 16),
			(byte)(i >> 8),
			(byte)(i),
		};
	}
	
	/**
	 * Converts a byte[] to an integer
	 * @param bytes - the byte[] to convert
	 * @return the converted integer
	 */
	public static int convertToInt(byte[] bytes) {
		int val = 0;
		for (int i = bytes.length - 1; i >= 0; i--) {
			val += bytes[i] << ((bytes.length - i - 1) * 8);
		}
		return val;
	}
	
	/**
	 * Concatenates a variadic number of byte[] to a single byte[]
	 * @param arrays - A variadic number of byte[]
	 * @return the combined single byte[]
	 */
	public static byte[] concatenate(byte[]... arrays) {
		int length = 0;
		for (byte[] arr : arrays)
			length += arr.length;
		byte[] out = new byte[length];
		int currentPosition = 0;
		for (byte[] arr : arrays) {
			System.arraycopy(arr, 0, out, currentPosition, arr.length);
			currentPosition += arr.length;
		}
		return out;
	}
	
}
