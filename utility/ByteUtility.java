package utility;

public class ByteUtility {

	public static byte[] convertInt(int i) {
		return new byte[] {
			(byte)(i >> 24),
			(byte)(i >> 16),
			(byte)(i >> 8),
			(byte)(i),
		};
	}
	
	public static int convertToInt(byte[] bytes) {
		int val = 0;
		for (int i = bytes.length - 1; i >= 0; i--) {
			val += bytes[i] << ((bytes.length - i - 1) * 8);
		}
		return val;
	}
	
}
