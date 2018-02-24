import java.io.*;
import java.util.*;
/*
 * Author: Kelly Gross
 * 
 */
public class DBTable {
	
	RandomAccessFile rows; //the file that stores the rows in the table
	long free; //head of the free list space for rows
	int numOtherFields;
	int otherFieldLengths[];
	//add other instance variables as needed
	
	BTree tree;
	
	int blockSize;
	
	private class Row {
		private int keyField;
		private char otherFields[][];
		
		//constructor and other Row methods
		
		private Row(int k, char fields[][]) {
			keyField = k;
			otherFields = fields;
		}
		
		private Row(long addr) throws IOException {
			rows.seek(addr);
			keyField = rows.readInt();
			otherFields = new char[numOtherFields][];
			for (int i = 0; i < numOtherFields; ++i) {
				otherFields[i] = new char[otherFieldLengths[i]];
				for (int j = 0; j < otherFieldLengths[i]; ++j) {
					otherFields[i][j] = rows.readChar();
				}
			}
		}
		
		private void writeRow(long addr) throws IOException {
			rows.seek(addr);
			rows.writeInt(keyField);
			for (int i = 0; i < numOtherFields; ++i) {
				for (int j = 0; j < otherFieldLengths[i]; ++j) {
					rows.writeChar(otherFields[i][j]);
				}
			}
		}
	}
	
	public DBTable(String filename, int fL[], int bsize) throws IOException {
	//use to create a new DBTable
		File path = new File(filename);
		path.delete();
		File bTreePath = new File(filename + "tree");
		bTreePath.delete();
		otherFieldLengths = new int[fL.length];
		numOtherFields = fL.length;
		rows = new RandomAccessFile(path, "rw");
		rows.seek(0);
		rows.writeInt(numOtherFields);
		for (int i = 0; i < numOtherFields; ++i) {
			otherFieldLengths[i] = fL[i];
			rows.writeInt(otherFieldLengths[i]);
		}
		free = 0;
		rows.writeLong(free);
		
		//make a new empty tree
		tree = new BTree(filename + "tree", bsize);
		
		blockSize = bsize;
	}
	
	public DBTable(String filename) throws IOException {
	//use this constructor to open an existing DBTable
		rows = new RandomAccessFile(filename, "rw");
		rows.seek(0);
		numOtherFields = rows.readInt();
		otherFieldLengths = new int[numOtherFields];
		for (int i = 0; i < numOtherFields; ++i) {
			otherFieldLengths[i] = rows.readInt();
		}
		free = rows.readLong();
		
		//reuse BTree
		tree = new BTree(filename + "tree");
	}
	
	public boolean insert(int key, char fields[][]) throws IOException {
	//PRE: the length of each row's fields matches the expected length
		if (tree.search(key) != 0) {
			//already exists
			return false;
		}
		long addr = getFree();
		tree.insert(key, addr);
		//seek to address
		
		Row r = new Row(key, fields);
		r.writeRow(addr);
		//write the stuff out
		
		return true; 
	}
	
	private long getFree() throws IOException {
		//method to get the first thing in the free list
		if (free == 0) {
			return rows.length();
		}
		else {
			long freeToReturn = free;
			rows.seek(free);
			free = rows.readLong();
			return freeToReturn;
		}
	}

	public boolean remove(int key) throws IOException {
	//return false if the key is not in the table
		if (tree.search(key) == 0) {
			return false;
		}
		long addr = tree.remove(key);
		addFree(addr);
		return true;
	}
	
	private void addFree(long addr) throws IOException {
		//method to add a node/address to the free list
		//write free
		rows.seek(addr); 
		rows.writeLong(free); 
		free = addr;
	}
	
	public LinkedList<String> search(int key) throws IOException {
	//return the data of the key's row in linked list form if the key is in the table
	//else return false
		LinkedList<String> list = new LinkedList<>();
		if (tree.search(key) == 0) {
			return null;
		}
		String stringKey = Integer.toString(key);
		long addr = tree.search(key);
		Row r = new Row(addr);
		list.add(stringKey);
		for (int i = 0; i < numOtherFields; ++i) {
			String str = "";
			for (int j = 0; j < otherFieldLengths[i]; ++j) {
				//System.out.println(r.otherFields[i][j]);
				if (r.otherFields[i][j] == '\0') {
					//do nothing
					break;
				}
				else {
					str = str + r.otherFields[i][j];
				}
			}
			list.add(str);
		}
		return list;
	}
	
	public LinkedList<LinkedList<String>> rangeSearch(int low, int high) throws IOException {
	//PRE: low <= high
	//low and high are inclusive
		LinkedList<Long> list = tree.rangeSearch(low, high);
		LinkedList<LinkedList<String>> ret_list = new LinkedList<>();
		long addr = 0;
		while (!list.isEmpty()) {
			LinkedList<String> strings = new LinkedList<>();
			addr = list.pop();
			Row r = new Row(addr);
			String stringKey = Integer.toString(r.keyField);
			strings.add(stringKey);
			for (int i = 0; i < numOtherFields; ++i) {
				String str = "";
				for (int j = 0; j < otherFieldLengths[i]; ++j) {
					//System.out.println(r.otherFields[i][j]);
					if (r.otherFields[i][j] == '\0') {
						//do nothing
						break;
					}
					else {
						str = str + r.otherFields[i][j];
					}
				}
				strings.add(str);
			}
			ret_list.add(strings);
		}
		return ret_list;
	}
	
	public void print() throws IOException {
	//print the rows to standard output in ascending order(based on keys)
	//one row per line
		
		tree.print();
		
		
	}
	
	public void close() throws IOException {
	//close the DBTAble. The table should not be used after it is closed
		rows.seek(0);
		rows.readInt();
		for (int i = 0; i < numOtherFields; ++i) {
			rows.readInt();
		}
		rows.writeLong(free);
		rows.close();
		tree.close();
		
	}
	public static void main(String[] args) throws IOException {
		
	}
	

}
