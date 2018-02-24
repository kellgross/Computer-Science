import java.util.*;

public class SymbolTable {
	
	int size;
	
	private class Node {
	//a node used to build linked lists
	//do not use the java LinedList class
		private String key;
		private Object data;
		private Node next;
		
		private Node(String k, Object d, Node x) {
			key = k;
			data = d;
			next = x;
		}
	}
	
	private Node table[];
	
	public SymbolTable(int s) {
	//s is the size of the table. You do not have to handle resizing the table
		size = s;
		table = new Node[size];
	}
	
	private int hash(String k) {
	//return the hash function value for k
		int h = 0;
		for(int i = 0; i < k.length(); ++i) {
			h = h*31 + k.charAt(i);
			if (h < 0) {
				h = h * (-1);
			}
		}
		return h % table.length;
	}
	
	public boolean insert(String k) {
	//if k is not in the table create a new entry(with a null data value) and return true
	//else if k is in the table return false
		if(find(k)) {
			return false;
		}
		Node n = new Node(k, null, table[hash(k)]);
		table[hash(k)] = n;
		//System.out.println(hash(k) + " = " + k);
		return true;
	}
	
	public boolean find(String k)	{	
	//return true if k is in the table otherwise return false
		int index = hash(k);
		Node curr = table[index];
		while (curr != null) {
			if ((curr.key).equals(k)) {
				//System.out.println("found");
				return true;
			}
			curr = curr.next;
		}
		return false;
	}
	
	public Node findAux(String k) {
		Node retVal = null;
		//go through array
		for (int i = 0; i < size; ++i) {
			//for each index in the array look at all the nodes in the list
			Node curr = table[i];
			while (curr != null) {
				if ((curr.key).equals(k)) {
					retVal = curr;
				}
				curr = curr.next;
			}
		}
		return retVal;
		
	}
	
	public Object getData(String k) {	
		//if k is in the table return the data (which could be null) associated with k
		//if k is not in the table return null	
		Object return_val = null;
		if (!find(k)) {
			return null;
		}
		else {
			return_val = findAux(k).data;
		}
		return return_val;
	}	
		
	public void setValue(String k,	Object	d)	{	
	//PRE: k is in the table
	//make d the data value associated with k
		Node n = findAux(k);
		n.data = d;
	}
	
	public class STIterator implements Iterator<String> {
	//An iterator that iterates through the keys in the table
		private Node pos;
		private int index;
		public STIterator() {
			index = 0;
			pos = table[0];
			++index;
		}
		
		public boolean hasNext() {
			if (index < table.length || pos != null) {
				return true;
			}
			else {
				return false;
			}
		}
		
		public String next() {
		//PRE: hasNext()
		//The format of the string should be key:data 
		//where the key is a key in the symbol table and
		//data is the string representation of the data associated with the key
			if (pos == null) {
				pos = findNext(pos);
			}
			if (pos == null) {
				//end of the list
				return "";
			}
			String s = pos.key + ":" + pos.data;
			pos = pos.next;
			//System.out.println(s);
			return s;
		}
		
		public void remove() {
		//optional method not implemented
		}
		
		private Node findNext(Node r) {
			while (r == null) {
				if (index < table.length) {
					r = table[index];
					index++;
				}
			}
			return r;
		}
	}
	
	public boolean remove(String k) {
	//if k is in the table, return the entry for k and return true
	//if k is not in the table, return false
		if (!find(k)) {
			//System.out.println("false");
			return false;
		}
		//else do stuff
		int index = hash(k);
		Node curr = table[index];
		if ((curr.key).equals(k)){
			table[index] = curr.next;
			//System.out.println("Removed...");
		}
		else {
			while (curr.next != null) {
				if ((curr.next.key).equals(k)) {
					curr = curr.next.next; 
					//System.out.println("Removed: " + k);
				}
				curr = curr.next;
			}
		}
		return true;
	}
	
	public Iterator<String> iterator() {
	//return a STIterator object
		return new STIterator();
	}
	
	public static void main(String args[]) {
		
		/*SymbolTable table = new SymbolTable(3);
		table.insert("kelly");
		table.insert("jingle");
		table.insert("joe");
		table.insert("bob");
		table.remove("bob");
		
		
		Iterator iter = table.iterator();
		
		while(iter.hasNext()) {
			iter.next();
		}*/

		
	}

}
