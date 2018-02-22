import java.io.*;

public class HuffmanOutputStream extends BitOutputStream {
	
	int b;
	int count;
	
	public HuffmanOutputStream(String filename, String tree, int totalChars) {
		super(filename);
		try {
			d.writeUTF(tree);
			d.writeInt(totalChars);
			b = 0;
			count = 0;
		}
		catch(IOException e) {
			System.out.print("IOException");
		}
	}
	
	public void writeBit(int bit) {//bit math
		b = b*2 + bit;
		++count;
		if (count == 8) {
			//write byte
			try {
				d.write(b);
			}
			catch(IOException e) {
				System.out.print("IOException");
			}
			b = 0;
			count = 0;
		}
	}
	
	public void close() {
		if (count < 8) {
			int value = count;
			for (int i = value; i < 8; ++i) {
				writeBit(0);
			}
		}
		try {
			d.close();
		} 
		catch (IOException e) {
			System.out.print("IOException");
		}
	}
}
