import java.io.*;
import java.util.*;
/*
 * Author: Kelly Gross
 * 
 */
public class BTree {
	
	RandomAccessFile f;
	int order;
	int blockSize;
	long root;
	long free;
	//add instance variables as needed
	Stack<BTreeNode> path;
	long addr;
	long addressToRemember = 0;
	
	private class BTreeNode {
		private int count;
		private int keys[];
		private long children[];
		private long addr;
		
		//constructors and other methods
		
		private BTreeNode(int c, int k[], long child[], long address) {
			count = c;
			keys = k;
			children = child;
			addr = address;
		}
		
		private BTreeNode(long addr) throws IOException {
			//constructor for a node that exists and is stored in the file
			f.seek(addr);
			this.addr = addr;
			//System.out.println("ADDRESS: " + addr);
			count = f.readInt();
			keys = new int[order - 1];
			children = new long[order];
			for (int i = 0; i < order - 1; ++i) {
				keys[i] = f.readInt();
			}
			
			//don't skip over anything
			for (int i = 0; i < order; ++i) {
				children[i] = f.readLong();
			}		
		}
		
		private void writeNode(long addr) throws IOException {
			//writes the node to the file at location addr
			f.seek(addr);
			this.addr = addr;
			f.writeInt(count);
			for (int i = 0; i < order - 1; ++i) {
				f.writeInt(keys[i]);
			}
			for (int i = 0; i < order; ++i) {
				f.writeLong(children[i]);
			}
		}
	}
	
	public BTree(String filename, int bsize) throws IOException {
	//bsize is the block size. This value is used to calculate the order of the B+Tree
	//all B+Tree nodes will use bsize bytes
		
		File path = new File(filename);
		
		path.delete();
		
		f = new RandomAccessFile(path, "rw");
		root = 0;
		free = 0;
		blockSize = bsize;
		order = (int) Math.floor(blockSize / 12);
		
		f.writeLong(root);
		f.writeLong(free);
		f.writeInt(blockSize);
		
	}
	
	public BTree(String filename) throws IOException {
	//open an existing B+Tree
		File path = new File(filename);
		
		f = new RandomAccessFile(path, "rw");
		f.seek(0);
		root = f.readLong();
		free = f.readLong();
		blockSize = f.readInt();
		order = (int) Math.floor(blockSize / 12);
		
		
	}
	
	public boolean insert(int key, long addr) throws IOException {
	//if key isn't a duplicate add to the tree
	//addr (in DBTable) is the address of the row that contains the key
		//KEEP TRACK OF THE ADDRESS
		
		if (search(key) != 0) {
			return false;
		}
		BTreeNode node = null;
		int nextKey = 0;
		Long address = (long) 0;
		if (root == 0) {
			insertRoot(key, node, addr);
			return true;
		}
		else {
			node = path.pop();
		}
		
		boolean split = false;
		if (Math.abs(node.count) < order - 1) {
			//System.out.println("Insert Key, no split");
			//if the node has room for the key
			
			node = insertKey(node, key, addr);
			node.writeNode(node.addr);
			split = false;
		}
		else {
			//have to split
			nextKey = splitNode(node, key, addr);
			address = node.addr;
			split = true;
			while (!path.empty() && split) {
				if (node.count < 0) {
					address = node.children[order-1];
				}
				else if (addressToRemember == 0){
					address = node.children[node.count - 1];
				}
				else {
					address = addressToRemember;
				}
				node = path.pop();
				if (Math.abs(node.count) < order - 1) {
					if (node.count < 0) {
						node = insertKey(node, nextKey, addr);
					}
					else {
						node = insertKey(node, nextKey, address);
					}
					node.writeNode(node.addr);
					split = false;
				}
				else {
					if (node.count < 0) {
						nextKey = splitNode(node, nextKey, addr);
					}
					else {
						nextKey = splitNonLeaf(node, nextKey, address);
					}
					split = true;
				}
			}
		}
		if (split) {
			//make a new root
			int keys[] = new int[order - 1];
			long children[] = new long[order];
			root = getFree();
			BTreeNode newNode = new BTreeNode(1, keys, children, root);
			newNode.keys[0] = nextKey;
			//write the children
			newNode.children[0] = node.addr;
			if(isLeaf(node)) {
				newNode.children[1] = node.children[order - 1];
			}
			else {
				//when a nonleaf splits
				newNode.children[1] = addressToRemember;
			}
			addressToRemember = 0;
			newNode.writeNode(newNode.addr);
			
		}
		return true; 
	}
	
	private int splitNonLeaf(BTreeNode node, int key, long addr) throws IOException {
		int ret_val = 0;
		int tempKeys[] = new int[order];
		long tempChildren[] = new long[order + 1];
		int i = 0;
		while (i < node.count && key > node.keys[i]) {
			tempKeys[i] = node.keys[i];
			tempChildren[i] = node.children[i];
			++i;
		}
		tempChildren[i] = node.children[i];
		//move keys and addresses over 
		for (int j = i; j < Math.abs(node.count); ++j) {
			tempKeys[j+1] = node.keys[j];
		}
		for (int j = i + 1; j <= Math.abs(node.count); ++j) {
			tempChildren[j+1] = node.children[j];
		}
		tempKeys[i] = key;
		tempChildren[i + 1] = addr;
		
		/*for (int m = 0; m < tempChildren.length; ++m) {
			System.out.println(tempChildren[m]);
		}*/
		/*for (int m = 0; m < tempKeys.length; ++m) {
			System.out.println("KEYS: " + tempKeys[m]);
		}*/
		//reset all node keys and children to 0
		for (int j = 0; j < order - 1; ++j) {
			node.keys[j] = 0;
			node.children[j] = 0;
		}
		node.children[order - 1] = 0;
		
		i = 0;
		for (int j = i; j < (int) Math.ceil((double) order/2) - 1; ++j) {
			node.keys[j] = tempKeys[i];
			++i;
		}
		int k = 0;
		for (int j = k; j < (int) Math.ceil((double) order/2); ++j) {
			node.children[j] = tempChildren[k];
			++k;
		}
		
		node.count = (int) Math.ceil((double) order/2) - 1;
		
		ret_val = tempKeys[i];
		++i;
		
		int keys[] = new int[order - 1];
		long children[] = new long[order];
		int count = 0;
		if (order%2 == 0) {
			count = (int) Math.ceil((double) order/2);
		}
		else {
			count = (int) Math.ceil((double) order/2) - 1;;
		}
		//count = (int) Math.ceil((double) order/2) - 1;
		BTreeNode newNode = new BTreeNode(count, keys, children, getFree());
		
		for (int j = 0; j < count; ++j) {
			if (i >= order) {
				break;
			}
			newNode.keys[j] = tempKeys[i];
			++i;
		}
		for (int j = 0; j < count + 1; ++j) {
			if (k > order) {
				break;
			}
			newNode.children[j] = tempChildren[k];
			++k;
		}
		addressToRemember = newNode.addr;
		node.writeNode(node.addr);
		newNode.writeNode(newNode.addr);
		
		return ret_val;
	}
	
	private int splitNode(BTreeNode node, int key, long addr) throws IOException {
		//key is the smallest value in the new node
		int ret_val = 0;
		int tempKeys[] = new int[order];
		long tempChildren[] = new long[order + 1];
		int i = 0;
		while (i < Math.abs(node.count) && key > node.keys[i]) {
			tempKeys[i] = node.keys[i];
			tempChildren[i] = node.children[i];
			++i;
		}
		//move keys and addresses over 
		for (int j = i; j < Math.abs(node.count); ++j) {
			tempKeys[j + 1] = node.keys[j];
			tempChildren[j + 1] = node.children[j];
		}
		tempKeys[i] = key;
		tempChildren[i] = addr;
		
		/*for (int m = 0; m < tempKeys.length; ++m) {
			System.out.println("KEYSLoop: " + tempKeys[m]);
		}*/
		//reset all node keys and children to 0
		for (int j = 0; j < order - 1; ++j) {
			node.keys[j] = 0;
			node.children[j] = 0;
		}
		//insert the keys into the original node
		i = 0;
		for (int j = 0; j < (int) Math.ceil((double) order/2) - 1; ++j) {
			node.keys[j] = tempKeys[i];
			node.children[j] = tempChildren[i];
			++i;
		}

		node.count = -((int) Math.ceil((double) order/2) - 1);
		long link = node.children[order - 1];
		//set the return value to the first thing in the new node
		ret_val = tempKeys[i];
		
		
		//create the new node
		int keys[] = new int[order - 1];
		long children[] = new long[order];
		BTreeNode newNode;
		
		//insert keys into the new node
		if (order%2 == 1) {
			newNode = new BTreeNode(-(int) Math.ceil((double) order/2), keys, children, getFree());
			for (int j = 0; j < (int) Math.ceil((double) order/2); ++j) {
				//newNode stuff
				newNode.keys[j] = tempKeys[i];
				newNode.children[j] = tempChildren[i];
				++i;
			}
		}
		else {
			newNode = new BTreeNode(-((int) Math.ceil((double) order/2) + 1), keys, children, getFree());
			for (int j = 0; j <= (int) Math.ceil((double) order/2); ++j) {
				//newNode stuff
				newNode.keys[j] = tempKeys[i];
				newNode.children[j] = tempChildren[i];
				++i;
			}
		}
		//write the nodes to the filename
		if (isLeaf(node)) {
			node.children[order - 1] = newNode.addr;
			newNode.children[order - 1] = link;
		}
		node.writeNode(node.addr);
		newNode.writeNode(newNode.addr);
		
		return ret_val;
		
	}
	
	private boolean isLeaf(BTreeNode node) {
		if (node.count < 0) {
			return true;
		}
		return false;
	}
	
	private void insertRoot(int key, BTreeNode node, long addr) throws IOException {
		//method to insert root
		int keys[] = new int[order - 1];
		keys[0] = key;
		long children[] = new long[order];
		children[0] = addr;
		root = getFree();
		node = new BTreeNode(-1, keys, children, root);
		node.writeNode(root);
	}
	
	private BTreeNode insertKey(BTreeNode node, int key, long addr) throws IOException {
		//method to insert a key into a node
		int tempKeys[] = new int[order - 1];
		long tempChildren[] = new long[order];
		for (int k = 0; k < order - 1; ++k) {
			tempKeys[k] = node.keys[k];
		}
		for (int k = 0; k < order; ++k) {
			tempChildren[k] = node.children[k];
		}
		/*for (int m = 0 ; m < tempKeys.length; ++m) {
			//System.out.println("TEMP: " + tempKeys[m]);
		}*/
		int i = 0;
		while (i < Math.abs(node.count) && key > node.keys[i]) {
			++i;
		}
		
		//move keys and addresses over 
		if (isLeaf(node)) {
			for (int j = i; j < Math.abs(node.count); ++j) {
				node.keys[j+1] = tempKeys[j];
				node.children[j+1] = tempChildren[j];
			}
		}
		else {
			for (int j = i; j < Math.abs(node.count); ++j) {
				node.keys[j+1] = tempKeys[j];
			}
			for (int j = i; j <= Math.abs(node.count); ++j) {
				node.children[j+1] = tempChildren[j];
				//System.out.println(tempChildren[j]);
			}
		}
		node.keys[i] = key;
	
		if (isLeaf(node)) {
			node.children[i] = addr;
			node.count--;
		}
		else {
			node.children[i + 1] = addr;
			node.count++;
		}
		return node;
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
	public long remove(int key) throws IOException {
	//if key is in tree, remove it and return the address of the row
	//return 0 if not found
		long ret_addr = 0;
		boolean tooSmall = false;
		boolean borrowed = false;
		if (search(key) == 0) {
			return 0;
		}
		BTreeNode node = path.pop();
		ret_addr = deleteKey(node, key);
		//System.out.println("KEY: " + key);
		ret_addr = node.addr;
		if (Math.abs(node.count) < Math.ceil((double) order/2) - 1 && node.addr != root) {
			tooSmall = true;
		}
		else {
			tooSmall = false;
		}
		BTreeNode child = null;
		BTreeNode rightNode = null;
		BTreeNode leftNode = null;
		while (!path.empty() && tooSmall) {
			child = node; //node that is too small
			node = path.pop(); //parent of child
			long left = 0;
			long right = 0;
			int i = 0;
			while (i < node.count && node.children[i] != child.addr) {
				++i;
			}
			//node.children[i] == address where key is
			if (i != 0) {
				//left neighbor exists
				left = node.children[i-1];
			}
			right = child.children[order - 1];
			//System.out.println("RIGHT: " + right);
			if (isLeaf(child)) {
				leftNode = new BTreeNode(left);
				rightNode = new BTreeNode(right);
			}
			else {
				leftNode = new BTreeNode(left);
				rightNode = new BTreeNode(node.children[i + 1]);
			}
			if (Math.abs(leftNode.count) >  Math.ceil((double) order/2) - 1) {
				//can borrow
				borrowed = true;
				int keyToDelete = child.keys[0];
				int newKey = borrow(leftNode, child);
				swapLeft(node, newKey, keyToDelete);
				node.writeNode(node.addr);
				tooSmall = false;
			}
			else if(Math.abs(rightNode.count) >  Math.ceil((double) order/2) - 1) {
					//can borrow
					borrowed = true;
					int keyToDelete = rightNode.keys[0];
					int newKey = borrow(rightNode, child);
					swapRight(node, newKey, keyToDelete);
					node.writeNode(node.addr);
					tooSmall = false;
			}
			//System.out.println("Borrow: " + borrowed);
			if (node.addr == root && tooSmall && node.count == 1) {
				System.out.println("MADE IT");
				BTreeNode rootLeft = new BTreeNode(node.children[0]);
				BTreeNode rootRight = new BTreeNode(node.children[1]);
				insertKey(rootLeft, node.keys[0], rootRight.children[0]);
				while (Math.abs(rootRight.count) > 0) {
					System.out.println(rootRight.keys[0]);
					insertKey(rootLeft, rootRight.keys[0], rootRight.children[1]);
					deleteKey(rootRight, rootRight.keys[0]);
				}
				addFree(rootRight.addr);
				addFree(node.addr);
				rootLeft.writeNode(rootLeft.addr);
				tooSmall = true;
				break;
			}
			if (!borrowed) {
				//have to merge
				//always combine with left, unless the leftmost child
				if (node.children[0] == child.addr) {
					//merge with right
					mergeRight(node, child, rightNode);
					int keyToDelete = rightNode.keys[0];
					deleteKey(node, keyToDelete);
					if (Math.abs(node.count) < Math.ceil((double) order/2) - 1 & node.addr != root) {
						tooSmall = true;
					}
					else {
						tooSmall = false;
					}
					//ret_addr = rightNode.addr;
				}
				else {
					mergeLeft(node, child, leftNode);
					leftNode.children[order - 1] = rightNode.addr;
					leftNode.writeNode(leftNode.addr);
					int keyToDelete = child.keys[0];
					deleteKey(node, keyToDelete);
					if (Math.abs(node.count) < Math.ceil((double) order/2) - 1 && node.addr != root) {
						tooSmall = true;
					}
					else {
						tooSmall = false;
					}
					//ret_addr = leftNode.addr;
				}
			}
		}
		if(tooSmall) {
			root = node.children[0];
		}
		return ret_addr;
	}
	private void mergeLeft(BTreeNode node, BTreeNode child, BTreeNode left) throws IOException {
		//System.out.println("MERGE LEFT");
		int i = 0;
		while (Math.abs(child.count) > 0) {
			insertKey(left, child.keys[i], left.addr);
			deleteKey(child, child.keys[i]);
			++i;
		}
		node.children[i] = left.addr;
		addFree(child.addr);
		left.writeNode(left.addr);
	}
	private void mergeRight(BTreeNode node, BTreeNode child, BTreeNode right) throws IOException {
		//System.out.println("RIGHT NODE: " + right.addr);
		//System.out.println("MERGE RIGHT");
		int i = 0;
		while (Math.abs(child.count) > 0) {
			insertKey(right, child.keys[i], right.addr);
			deleteKey(child, child.keys[i]);
			++i;
		}
		node.children[i] = right.addr;
		addFree(child.addr);
		right.writeNode(right.addr);
	}
	
	
	private void swapLeft(BTreeNode node, int newKey, int oldKey) {
		//method to swap keys from a left borrow
		int i = 0;
		while (i < Math.abs(node.count) && newKey > node.keys[i]) {
			++i;
		}
		node.keys[i] = newKey;
	}
	private void swapRight(BTreeNode node, int newKey, int oldKey) {
		//method to swap keys from a right borrow
		int i = 0;
		while (i < Math.abs(node.count) && oldKey > node.keys[i]) {
			++i;
		}
		node.keys[i] = newKey;
	}
	private int borrow(BTreeNode neighbor, BTreeNode node) throws IOException {
		//method to borrow keys from a neighbor of a node
		int ret_key = 0;
		if (neighbor.keys[Math.abs(neighbor.count) - 1] < node.keys[Math.abs(node.count) - 1]) {
			//left side borrow
			int key = neighbor.keys[Math.abs(neighbor.count) - 1];
			long address = neighbor.children[Math.abs(neighbor.count) - 1];
			deleteKey(neighbor, key);
			insertKey(node, key, address);
			neighbor.writeNode(neighbor.addr);
			node.writeNode(node.addr);
			ret_key = key;
		}
		else {
			//right side borrow
			int key = neighbor.keys[0];
			long address = neighbor.children[0];
			deleteKey(neighbor, key);
			insertKey(node, key, address);
			neighbor.writeNode(neighbor.addr);
			node.writeNode(node.addr);
			ret_key = neighbor.keys[0];
		}
		
		return ret_key;
		
	}
	
	private long deleteKey(BTreeNode node, int key) throws IOException {
		//method to delete a single key from a node
		int i = 0;
		while (key > node.keys[i] && i < Math.abs(node.count)) {
			++i;
		}
		long ret_addr = node.children[i];
		if(isLeaf(node)) {
			while (i < Math.abs(node.count) - 1) {
				node.keys[i] = node.keys[i + 1];
				node.children[i] = node.children[i+1];
				++i;
			}
		}
		else {
			int k = i;
			while (i < node.count - 1) {
				node.keys[i] = node.keys[i + 1];
				++i;
			}
			while (k < node.count) {
				node.children[k] = node.children[k + 1];
				++k;
			}
		}
		int j = i;
		while (i < order - 1) {
			node.keys[i] = 0;
			++i;
		}
		if (isLeaf(node)) {
			while (j < order - 1) {
				node.children[j] = 0;
				++j;
			}
		}
		else {
			j = j + 1;
			while (j < order) {
				node.children[j] = 0;
				++j;
			}
		}
		if (isLeaf(node)) {
			node.count++;
		}
		else {
			node.count--;
		}
		node.writeNode(node.addr);
		return ret_addr;
	}
	
	public long search(int k) throws IOException {
	//equality search
	//if found return the address of the row with the key
	//else return 0
		if (root == 0) {
			return 0;
		}
		
		path = new Stack<>();
		BTreeNode r = new BTreeNode(root);
		path.push(r);
		while (r.count > 0) {
			int i = 0;
			while(i < r.count && k >= r.keys[i]) {
				++i;
			}
			addr = r.children[i];
			r = new BTreeNode(r.children[i]);
			path.push(r);
		}
		
		//at a leaf
		for (int j = 0; j < Math.abs(r.count); ++j) {
			if (k == r.keys[j]) {
				return r.children[j];
				
			}
		}
		//not in the tree
		return  0;
		
	}
	
	public LinkedList<Long> rangeSearch(int low, int high) throws IOException {
	//PRE: low<= high
	//low and high are inclusive
	//return a list of row addresses for all keys in the range
	//return empty list if no keys in the range
		LinkedList<Long> list = new LinkedList<>();
		BTreeNode r = new BTreeNode(root);
		int i = 0;
		while (r.count > 0) {
			i = 0;
			while(i < r.count && low >= r.keys[i]) {
				++i;
			}
			r = new BTreeNode(r.children[i]);
		}
		i = 0;
		while(i < Math.abs(r.count) && low > r.keys[i]) {
			++i;
		}
		while(high >= r.keys[i]) {
			list.push(r.children[i]);
			++i;
			if (i >= Math.abs(r.count)) {
				if (r.children[order - 1] > 0) {
					r = new BTreeNode(r.children[order - 1]);
					i = 0;
				}
				else {
					break;
				}
			}
		}
		
		return list;
	}
	
	public void print() throws IOException {
	//print the B+Tree
	//one node per line
		f.seek(0);
		System.out.println("ROOT: " + root);
		System.out.println("FREE: " + free);
		for (int i = 20; i < f.length(); i = i + blockSize) {
			BTreeNode r = new BTreeNode(i);
			System.out.print("ADDRESS: " + r.addr + " ");
			System.out.print("COUNT: " + r.count + " Keys: ");
			for (int j = 0; j < order - 1; ++j) {
				System.out.print(" " + r.keys[j]);
			}
			System.out.print(" " + "Children: ");
			for (int j = 0; j < order; ++j) {
				System.out.print(" " + r.children[j]);
			}
			System.out.println();
		}
		
	}
	
	public void close() throws IOException {
	//close the b+Tree. the tree should not be access after close is called
		f.seek(0);
		f.writeLong(root);
		f.writeLong(free);
		f.writeInt(blockSize);
		f.close();
	}
	
	public static void main(String[] args) {
		//test b+tree
		try {
			BTree tree = new BTree("b+tree", 60);
			
			//my test case for deleting
			tree.insert(24, 20);
			tree.insert(12, 84);
			tree.insert(45, 148);			
			tree.insert(10, 212);			
			tree.insert(16, 276);			
			tree.insert(42, 340);			
			tree.insert(13, 404);		
			tree.insert(36, 468);			
			tree.insert(70, 532);			
			tree.insert(55, 596);			
			tree.insert(38, 660);			
			tree.insert(40, 724);			
			tree.insert(39, 788);
			tree.insert(64, 852);
			tree.insert(57, 916);		
			tree.insert(56, 980);			
			tree.insert(11, 1044);			
			tree.insert(26, 1108);			
			tree.insert(32, 1172);	
			tree.insert(15, 1236);	
			tree.insert(34, 1300);
			
			
			tree.remove(10);
			tree.remove(13);
			tree.remove(12);
			tree.remove(11);
			tree.remove(15);
			tree.remove(16);
			tree.remove(24);
			
			tree.print();
			
			tree.close();

			
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}
