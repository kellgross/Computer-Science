import java.util.*;
public class PriorityQueue {
	
	private class Item{
		private int priority;
		private Object data;
		
		private Item(int p, Object d) {
			priority = p;
			data = d;
			
		}
	}
	
	private Item queue[];
	private int order;
	private int size;
	
	public PriorityQueue(int ord, int s) {
		queue = new Item[s];
		order = ord;
		size = 0;
	}
	
	public int getPriority() { 
		//Pre-condition !empty()
		//REturn the highest priority value in queue
		return queue[0].priority; 
	}
	
	public Object getData() {
		//pre-condition !empty()
		//Return the data associated with the highest priority
		return queue[0].data;
	}
	
	public int getSize() { 
		//return the number of items in the queue
		return size;
	}
	
	public boolean full() {
		return size == queue.length; 
	}
	
	public boolean empty() { 
		return size == 0; 
	}
	
	public void insert(int p, Object d) {
		//pre-condition !full()
		//insert a new item into the queue with priority p and associated data d
		size++;
		int child = size - 1;
		int parent = ((child - 1)/order);
		if (child == 0) {
			queue[child] = new Item(p,d);
			return;
		}
		while (queue[parent].priority > p) {
			queue[child] = queue[parent];
			child = parent;
			parent = ((child - 1)/order);
			if (child == 0) {
				queue[child] = new Item(p,d);
				return;
			}
		}
		queue[child] = new Item(p,d);
	}
	

	public void remove() { 
		//Pre-condition !empty()
		//remove item with the highest priority in the queue
		Item temp = queue[size-1];
		size--;
		int child = findSmallestChild(0);
		while (child < size) {
				if (temp.priority < queue[child].priority) {
					break;
				}
				else {
					queue[(child-1)/order] = queue[child];
					child = findSmallestChild(child); 
				}
			}
		queue[(child-1)/order] = temp;
		}
	
	private int findSmallestChild(int parent) {
		//Used to find the smallest child of a parent
		int child = order*parent + 1;
		for (int d = 1; d <= order; ++d) {
			if (order*parent + d < size) { //make sure to not walk off array
				if (queue[order*parent + d].priority < queue[child].priority) {
					child = order*parent + d;
				}
			}
			//do nothing
		}
		return child; 
	}
	
	private void printQueue() { //DEBUGGING
		//Used to print out the whole queue
		//For debugging purposes
		for (int i = 0; i < size; ++i) {
			System.out.println("NUMBER: " + queue[i].priority); //DEBUGGING
			System.out.println("INDEX:  " + i); //DEBUGGING
		}
		System.out.println();
	}
	
	public static void main(String args[]) {
		//Test Case 1
		PriorityQueue queue = new PriorityQueue(3, 12);
		
		queue.insert(10, null);
		queue.insert(20, null);
		queue.insert(30, null);
		queue.insert(40, null);
		queue.insert(50, null);
		queue.insert(60, null);
		queue.insert(70, null);
		queue.insert(80, null);
		queue.insert(90, null);
		queue.insert(100, null);
		queue.insert(110, null);
		queue.insert(120, null);
		
		System.out.println("Priority:" + queue.getPriority());
		System.out.println();
		queue.printQueue();
		
		while (!queue.empty()) {
			System.out.println("Priority: " + queue.getPriority());
			queue.remove();
			queue.printQueue();
		}
		
		
		//Test Case 2
		PriorityQueue queue2 = new PriorityQueue(2, 10);
		
		System.out.println("Test case 2");
		queue2.insert(100, null);
		queue2.insert(90, null);
		queue2.insert(80, null);
		queue2.insert(70, null);
		queue2.insert(60, null);
		queue2.insert(50, null);
		queue2.insert(40, null);
		queue2.insert(30, null);
		queue2.insert(20, null);
		queue2.insert(10, null);
		
		System.out.println("Priority for Queue2: " + queue2.getPriority());
		System.out.println();
		
		queue2.printQueue();
		System.out.println();

		while (!queue2.empty()) {
			System.out.println("Priority: " + queue2.getPriority());
			queue2.remove();
			queue2.printQueue();
		}
		
		
		//Test case 3
		PriorityQueue queue3 = new PriorityQueue(4, 10);
		
		queue3.insert(46, null);
		queue3.insert(32, null);
		queue3.insert(78, null);
		queue3.insert(96, null);
		queue3.insert(5, null);
		queue3.insert(24, null);
		queue3.insert(53, null);
		queue3.insert(10, null);
		queue3.insert(17, null);
		queue3.insert(83, null);
		
		System.out.println("Priority for Queue3: " + queue3.getPriority());
		System.out.println();
		
		queue3.printQueue();
		System.out.println();

		while (!queue3.empty()) {
			System.out.println("Priority: " + queue3.getPriority());
			queue3.remove();
			queue3.printQueue();
		}
		
		
		
		
		
		
		
		
		
		
		
	}

}
