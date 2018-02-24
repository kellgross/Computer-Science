import java.io.*;
import java.util.*;

public class Assignments {

	private Assignments(String in) {
		SymbolTable table = new SymbolTable(10);
		
		readFile(in, table);
		
		Iterator iter = table.iterator();
		while(iter.hasNext()) {
			System.out.println(iter.next());
		}		
		
		
	}
	
	private SymbolTable readFile(String in, SymbolTable t) {
		//method to read in the file
		
		try {
			FileReader fin = new FileReader(in);
			BufferedReader reader = new BufferedReader(fin);
			
			String line = reader.readLine();
			while (line != null) {
				String array[] = line.split(" = ");
				t.insert(array[0]);
				ExpressionTree tree = new ExpressionTree(array[1]);
				t.setValue(array[0], tree.evaluate(t));
				line = reader.readLine();
			}
		}
		catch (IOException e) {
			System.out.println("Exception");
		}
		
		return t;
	}
	
	public static void main(String args[]) {
		new Assignments(args[0]);
	}
}
