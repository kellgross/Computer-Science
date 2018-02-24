import java.io.*;
import java.util.*;


public class TopologicalSort {
	
	private class VertexNode {
		private String name;
		private VertexNode nextV;
		private EdgeNode edges;
		private int indegree;
		
		private VertexNode(String n, VertexNode v) {
			name = n;
			nextV = v;
			edges = null;
			indegree = 0;
		}
	}
	
	private class EdgeNode {
		private VertexNode vertex1;
		private VertexNode vertex2;
		private EdgeNode nextE;
		
		private EdgeNode(VertexNode v1, VertexNode v2, EdgeNode e) {
			vertex1 = v1;
			vertex2 = v2;
			nextE = e;
		}
	}
	
	private VertexNode vertices;
	private int numVertices;
	
	public TopologicalSort() {
		vertices = null;
		numVertices = 0;
	}
	
	public void addVertex(String s) {
	//PRE: the vertex list is sorted in ascending order using the name as a key
	//Post: a vertex with names s has been added to the vertex list and the vertex
	//list is sorted in ascending order using the name as a key
		if (vertices == null) {
			vertices = new VertexNode(s, null);
			return;
		}
		VertexNode v = vertices;
		while (v.nextV != null && (v.name.compareTo(s) > 0)) {
			v = v.nextV;
		}
		VertexNode node = new VertexNode(s, v.nextV);
		v.nextV = node;
		numVertices++;
	}
	
	public void addEdge(String n1, String n2) {
	//PRE: the vertices n1 and n2 have already been added
	//POST: the new edge (n1, n2) has been added to the n1 edge list
		VertexNode node1 = findNode(n1);
		VertexNode node2 = findNode(n2);
		if (node1.edges == null) {
			node1.edges = new EdgeNode(node1, node2, null);
		}
		else {
			node1.edges.nextE = new EdgeNode(node1, node2, node1.edges.nextE);
		}
		node2.indegree++;
	}
	
	private VertexNode findNode(String s) {
		//method to find a node 
		VertexNode v = vertices;
		while (v != null && !v.name.equals(s)) {
			v = v.nextV;
		}
		return v;
	}
	private VertexNode findIndegree(int n) {
		VertexNode v = vertices;
		while (v != null && v.indegree != 0) {
			v = v.nextV;
		}
		return v;
	}
	public String topoSort() {
	//if the graph contains a cycle return null
	//otherwise return a string containing the names of vertices separated by blanks
	//in topological order
		String sort = "";
		//String noGraph = "No topological ordering exists for the graph";
		VertexNode node = findIndegree(0);
		while (node != null) {
			node.indegree = -1;
			sort = sort + node.name + " ";
			EdgeNode edge = node.edges;
			while(edge != null) {
				edge.vertex2.indegree--;
				edge = edge.nextE;
			}
			node = findIndegree(0);
		}
		VertexNode v = vertices;
		while(v != null) {
			if (v.indegree > 0) {
				return null;
			}
			v = v.nextV;
		}
		return sort; 
	}
	
	
	public static void main(String args[]) throws IOException{
		
		TopologicalSort sort = new TopologicalSort();
		
		FileReader fin = new FileReader(args[0]);
		BufferedReader reader = new BufferedReader(fin);
		Scanner scan = new Scanner(reader);
		
		String array[] = scan.nextLine().split(" ");
		
		for (int i = 0; i < array.length; ++i) {
			sort.addVertex(array[i]);
		}
		
		String str = scan.nextLine();
		while(str != null) {
			String arr[] = str.split(" ");
			sort.addEdge(arr[0], arr[1]);
			if (scan.hasNext()) {
				str = scan.nextLine();
			}
			else {
				str = null;
			}
		}
		String topo = sort.topoSort();
		if (topo == null) {
			System.out.println("No topological ordering exists for the graph");
		}
		else {
			System.out.println(topo);
		}
		
	}
	
	
	
	

}
