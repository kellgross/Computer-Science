import java.io.*;

public class HuffmanInputStream extends BitInputStream{
	
	private String tree;
	private int totalChars;
	private int bit;
	private int count;
	private int[] array;
	int i;
	
	public HuffmanInputStream(String filename) {
		super(filename);
		try {
			tree = d.readUTF();
			totalChars = d.readInt();
			array = new int[8];
			bit = d.readUnsignedByte();
			for (int j = 7; j >= 0; --j) {
				array[j] = bit%2;
				bit = bit/2;
			}
			count = 0;
			
		}
		catch (IOException e) {
			System.out.print("IOEXception");
		}
	}
	
	public int readBit() {
		if (count == 8) {
			count = 0;
			try {
				bit = d.readUnsignedByte();
				for (int j = 7; j >=0 ; --j) {
					array[j] = bit%2;
					bit = bit/2;
				}
			}
			catch(IOException e) {
				System.out.println("Input Stream IOException");
			}
		}
		int retVal = array[count];
		count++;
		return retVal;
	}
	
	public String getTree() {
		return tree;
	}
	
	public int totalChars() {
		return totalChars;
	}
	
	public void close() {
		try {
			d.close();
		} 
		catch (IOException e) {
			System.out.print("IOException");
		}
	}
}
