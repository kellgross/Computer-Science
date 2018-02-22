import java.util.*;

public class HuffmanTree {
	
	//post order:
	//left
	//right
	//parent
	
	private class Node {
		private Node left;
		private char data;
		private Node right;
		
		private Node(Node L, char d, Node R) {
			left = L;
			data = d;
			right = R;
		}
	}
	private Node root;
	private Node current;
	
	public HuffmanTree() {
		root = null;
		current = null;
	}
	
	public HuffmanTree(char d) {
		//make a one node tree
		root = new Node(null, d, null);
	}
	
	public HuffmanTree(String t, char nonLeaf) {
		//assumes t represents a post order representation of the tree
		//where a node is either a leaf or has two children
		//nonLeaf is the char value of the data in the non-leaf nodes
		Stack<HuffmanTree> stack = new Stack();
		for (int i = 0; i < t.length(); ++i) {
			if (t.charAt(i) != nonLeaf) {
				//push the one node tree onto the stack
				stack.push(new HuffmanTree(t.charAt(i)));
			}
			else {
				//pop two trees and merge them
				HuffmanTree tree1 = stack.pop();
				HuffmanTree tree2 = stack.pop();
				HuffmanTree newtree = new HuffmanTree(tree2, tree1, nonLeaf);
				stack.push(newtree);
			}
		}
		root = stack.pop().root;
	}
	
	public HuffmanTree(HuffmanTree b1, HuffmanTree b2, char d) { 
		//merges b1 and b2
		root = new Node(b1.root, d, b2.root);	
	}
	
	public void moveRoot() {
		current = root;
	}
	
	public void moveLeft() {
		current = current.left;
	}
	
	public void moveRight() {
		current = current.right; 
	}
	
	public boolean atLeaf() {
		if (current.left == null && current.right == null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public char current() {
		return current.data;
	}
    
	public class PathIterator implements Iterator<String> {
		private LinkedList<String> list;
		public PathIterator() {
			list = new LinkedList<>();
			encoding(root, "");
		}
		public boolean hasNext() {
			if (list.size() != 0) {
				return true;
			}
			return false;
		}
		public String next() {
			return list.poll();
			
		}
		public void remove() {
			//not implemented
		}
		
		private void encoding(Node r, String s) {
			if (r.right == null && r.left == null) {
				list.add(r.data + s);
				return;
			}
			else {
				encoding(r.left, s + "0");
				encoding(r.right, s + "1");
			}
			return;
			
		}
	}
	
	public Iterator<String> iterator() {
		//return a PathIterator object
		return new PathIterator();
	}
	
	public String toString() {
		//return a post order representation of the tree
		return toString(root);
	}
	
	private String toString(Node r) {
		if (r == null) {
			return "";
		}
		return toString(r.left) + toString(r.right) + r.data;
	}

}
