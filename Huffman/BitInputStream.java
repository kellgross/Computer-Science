import java.io.*;

public abstract class BitInputStream {
	
	protected DataInputStream d;
	
	public BitInputStream(String filename) {
		try {
			d = new DataInputStream(new FileInputStream(filename));
		}
		catch (IOException e) {
			System.out.print("IOException");
		}
	}
	
	public abstract int readBit();
	
	public abstract void close();

}
