import java.io.*;
import java.util.*;

/*
 * Author: Kelly Gross
 * 
 * 
 */
public class BinarySearchTree {

	final int CREATE = 0;
	final int REUSE = 1;
	
	private RandomAccessFile f;
	long root;
	long free;
	
	private class Node {
		private long left;
		private int data;
		private int count;
		private long right;
		
		private Node(long l, int d, long r) {
			//constructor for new node
			data = d;
			count = 1;
			right = r;
			left = l;
		}
		
		private Node(long addr) throws IOException {
			//constructor for a node that exists and is stored in the file
			f.seek(addr);
			data = f.readInt();
			count = f.readInt();
			left = f.readLong();
			right = f.readLong();
		}
		
		private void writeNode(long addr) throws IOException {
			//writes the node to the file at location addr
			f.seek(addr);
			f.writeInt(data);
			f.writeInt(count);
			f.writeLong(left);
			f.writeLong(right);
		}
	}
	
	public BinarySearchTree(String fname, int mode) throws IOException {
		//if mode is CREATE a new empty file is created
		//if mode is CREATE and a file with file name fname exists 
		//the file with fname must be deleted before the new empty file is created
		//if mode is REUSE an existing file is used if it exists otherwise a new empty file is created
		File path = new File(fname);
		
		if (path.exists() && mode == CREATE) {
			path.delete();
		}
		
		f = new RandomAccessFile(path, "rw");
		
		if(mode == CREATE) {
			root = 0;
			free = 0;
			f.writeLong(root);
			f.writeLong(free);
		}
		else{ //mode == REUSE
			f.seek(0);
			root = f.readLong();
			free = f.readLong();
		}
		
	}
	
	public void insert(int d) throws IOException {
		//insert d into the tree
		//if d is in the tree increment the count field associated with d
		root = insert(d, root);
	}
	
	private long insert(int d, Long addr) throws IOException {
		//takes in and int and long
		//searches for a place to insert the node
		if ( addr == 0) {
			Node n = new Node(0, d, 0);
			long address = getFree(); 
			n.writeNode(address);
			return address;
		}
		Node r = new Node(addr);
		if (d < r.data) {
			r.left = insert(d, r.left);
		}
		else if (d > r.data) {
			r.right = insert(d, r.right);
		}
		else {
			r.count++;
		}
		r.writeNode(addr);
		return addr;
	}
	
	public int find(int d) throws IOException {
		//if d is in the tree return the value of count associated with d
		//otherwise return o
		return find(d, root);
	}
	
	private int find(int d, Long addr) throws IOException { 
		//private method to use recursion
		if (addr == 0) {
			return 0;
		}
		Node r = new Node(addr);
		if (d < r.data) {
			return find(d, r.left);
		}
		else if (d > r.data) {
			return find(d, r.right);
		}
		//System.out.print(r.count); //DEBUGGING
		return r.count;
	}
	public void removeOne(int d) throws IOException {
		//remove one copy of d from the tree
		//if the copy is the last copy remove d from the tree
		//if d is not in the tree the method has no effect
		root = removeOne(d, root);
	}
	private long removeOne(int d, long addr) throws IOException {
		//takes in an int and a long (for address)
		//operates as a recursive method
		if (addr == 0) {
			//not in file
			return 0;
		}
		Node r = new Node(addr);
		long newAddr = addr;
		if (d < r.data) {
			r.left = removeOne(d, r.left);
			r.writeNode(addr);
		}
		else if (d > r.data) {
			r.right = removeOne(d, r.right);
			r.writeNode(addr);
		}
		else { // d = r.data
			r.count--;
			r.writeNode(addr);
			if(r.count == 0) {
				if (r.left == 0) {
					newAddr = r.right;
					addFree(addr);
				}
				else if(r.right == 0) {
					newAddr = r.left;
					addFree(addr);
				}
				else {
					r.left = replace(r.left, addr);
					
					//to update r
					f.seek(addr); 
					r.data = f.readInt();
					r.count = f.readInt();
					r.writeNode(addr);
				}
			}
		}
		return newAddr;
	}
	
	private long replace(long addr, long replaceHere) throws IOException{
		Node r = new Node(addr);
		if (r.right != 0) {
			r.right = replace(r.right, replaceHere);
			addFree(r.right);
			r.writeNode(addr);
			return addr;
		}
		else {
			Node rep = new Node(replaceHere);
			r = new Node(addr);
			rep.data = r.data;
			rep.count = r.count;
			rep.writeNode(replaceHere);
			addFree(addr);
			return r.left;
			
		}
	}
	
	
	public void removeAll(int d) throws IOException {
		//remove d from the tree
		//if d is not in the tree the method has no effect
		root = removeAll(d, root);
	}
	
	private long removeAll(int d, long addr) throws IOException {
		if (addr == 0) {
			return 0;
		}
		Node r = new Node(addr);
		long newAddr = addr;
		if (d < r.data) {
			r.left = removeAll(d, r.left);
			r.writeNode(addr);
		}
		else if (d > r.data) {
			r.right = removeAll(d, r.right);
			r.writeNode(addr);
		}
		else { //found the node to delete
			r.count = 0;
			if (r.left == 0) {
				newAddr = r.right;
				addFree(addr);
			}
			else if(r.right == 0) {
				newAddr = r.left;
				addFree(addr);
			}
			else {
				r.left = replace(r.left, addr);
				
				f.seek(addr); 
				r.data = f.readInt();
				r.count = f.readInt();
				r.writeNode(addr);
			}
		}
		return newAddr;
	}
	
	public void close() throws IOException { 
		//close the random access file
		//before closing update the values of root and free if necessary
		f.seek(0);
		f.writeLong(root);
		f.writeLong(free);
		f.close();
	}
	
	private long getFree() throws IOException {
		//method to get the first thing in the free list
		if (free == 0) {
			return f.length();
		}
		else {
			long freeToReturn = free;
			f.seek(free);
			free = f.readLong();
			return freeToReturn;
		}
	}
	
	private void addFree(long addr) throws IOException {
		//method to add a node/address to the free list
		//write free
		f.seek(addr); 
		f.writeLong(free); 
		free = addr;
	}
	
	private void printFile() throws IOException {
		f.seek(0);
		f.writeLong(root);
		f.writeLong(free);
		f.seek(0);
		System.out.println("ROOT: " + f.readLong());
		System.out.println("FREE: " + f.readLong());
		for (int i = 16; i < f.length(); i = i+24) {
			System.out.print("ADDRESS: " + i);
			Node r = new Node(i);
			System.out.println(" DATA: " + r.data + " COUNT: " + r.count + " LEFT: " + r.left + " RIGHT: " + r.right);
		}
	}
	
	public static void main(String [] args) {
		//MAIN METHOD
		try {
			
		BinarySearchTree tree = new BinarySearchTree("binarytree.bin", 0);
		
		tree.insert(75);
		tree.insert(50);
		tree.insert(100);
		tree.insert(60);
		tree.insert(25);
		tree.insert(70);
		tree.insert(10);
		tree.insert(80);
		tree.insert(120);
		tree.insert(100);
	
		tree.removeOne(50);
		
		tree.removeAll(100);
		
		
		tree.removeOne(10);
		tree.removeOne(70);
		tree.removeOne(25);
		tree.removeOne(60);
		tree.removeOne(50);
		tree.removeOne(75);
		
		tree.removeOne(80);
		tree.removeOne(120);
		
		tree.removeAll(90);
		
	
		//root doesn't change if only two things left in list
		
		
		tree.printFile();
		
		System.out.println("FIND 100: " + tree.find(100));
		
		tree.close();
		
		BinarySearchTree tree2 = new BinarySearchTree("binarytree.bin", 1);
		
		System.out.println();
		System.out.println("Tree 2");
		
		tree2.insert(15);
		tree2.insert(20);
		tree2.insert(50);
		tree2.insert(100);
		tree2.insert(14);
		tree2.insert(5);
		
		
		
		tree2.printFile(); //everything gets copied over correctly
		
		tree2.close();
		
		
		} catch (IOException e) {
			System.out.println("IOException");
		}
		
		
	}
	
}
