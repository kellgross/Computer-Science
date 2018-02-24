import java.io.*;
import java.util.*;

import javax.print.attribute.standard.RequestingUserName;

public class ExpressionTree {
	
	private class Node {
		private Node left;
		private String data;
		private Node right;
		
		private Node(Node l, String d, Node r) {
			left = l;
			data = d;
			right = r;
		}
	}
	
	private Node root;
	
	public ExpressionTree(String exp) {
		//PRE: exp is a legal infix expression
		//Build an expression tree from the expression exp
		
		String[] stringArray = exp.split(" ");
		Stack<Node> operators = new Stack<>();
		Stack<ExpressionTree> operands = new Stack<>();
		
		for (int i = 0; i < stringArray.length; ++i) {
			//System.out.println(stringArray[i]); //DEBUGGING
			if (!isOperator(stringArray[i])) {
				//push the operand
				operands.push(new ExpressionTree(null, stringArray[i], null));
			}
			else {
				//it is an operator
				if (operators.empty()) {
					operators.push(new Node(null, stringArray[i], null));
				}
				else if (push(operators.peek().data, stringArray[i]) && !stringArray[i].equals(")")) {
					//push the operator onto the stack
					operators.push(new Node(null, stringArray[i], null));
				}
				else {
					//pop off the stack until you can push it onto the stack
					pop(operands, operators, stringArray[i]);
					while (!operators.empty()) {
						if (!push(operators.peek().data, stringArray[i])) {
							pop(operands, operators, stringArray[i]);
						}
						else {
							break;
						}
					}
					if (!stringArray[i].equals(")")) {
						//if it isn't a closing parenthesis push onto stack
						operators.push(new Node(null, stringArray[i], null));
					}
				}
			}
		}
		while (!operators.empty()) {
			pop(operands, operators, operators.peek().data);
		}
		root = operands.pop().root;
	}
	
	private ExpressionTree(Node left, String s, Node right) {
		root = new Node(left, s, right);
	}
	
	private void pop(Stack<ExpressionTree> operands, Stack<Node> operators, String s) {
		if (s.equals(")")) {
			while (!operators.peek().data.equals("(")) {
				//while not equal to opening parenthesis
				Node right = operands.pop().root;
				Node left = operands.pop().root;
				operands.push(new ExpressionTree(left, operators.pop().data, right));
			}
			//pop off the opening parenthesis "("
			operators.pop();
		}
		else if(operators.peek().data.equals("!")) {
			Node right = operands.pop().root;
			operands.push(new ExpressionTree(null, operators.pop().data, right));
		}
		else {
			Node right = operands.pop().root;
			Node left = operands.pop().root;
			operands.push(new ExpressionTree(left, operators.pop().data, right));
		}
	}
	
	private boolean push(String peek, String push) {
		//peek is the string from the top of the stack
		//push is the string you're trying to push onto the stack
		
		int peekPrec = precedence(peek);
		int pushPrec = precedence(push);
		
		if (peekPrec == 0 || pushPrec == 0) {
			//an opening parenthesis
			return true;
		}
		else if (peekPrec < pushPrec || peek.equals("(")) {
			return true;
		}
		else if((pushPrec == 3 && peekPrec == 3) || (pushPrec == 4 && peekPrec == 4)) {
			return true;
		}
		else {
			return false;
		}
	}
	 
	private int precedence(String s) {
		//set a precedence for each operator
		char c = s.charAt(0);
		if (c == '(') {
			return 0;
		}
		else if (c == '+' || c == '-') {
			return 1;
		}
		else if (c == '*' || c == '/' || c == '%') {
			return 2;
		}
		else if (c == '^') {
			return 3;
		}
		else if(c == '!') {
			return 4;
		}
		else{//equal to ) or (
			return 5;
		}
	}
	
	
	private boolean isOperator(String c) {
		//method to check if a string is an operator
		if (c.equals("+") || c.equals("-") || c.equals("/") || c.equals("*") || c.equals("^")|| c.equals("!") || c.equals("%") || c.equals("(") || c.equals(")"))  {
			return true;
		}
		return false;
	}
	
	public int evaluate(SymbolTable t) {
		//return the int value of the expression tree
		//t is used to lookup values of variables
		return evaluate(t, root);
	}
	
	private int evaluate(SymbolTable t, Node r) {
		//return the int value of the expression tree with root r
		//t is used to lookup values of variables
		if (r == null) {
			return 0;
		}	
		if (!isOperator(r.data)) {
			if (isNumber(r.data)) {
				//is operand
				int num = Integer.parseInt(r.data);
				//System.out.println(num);
				return num;
			}
			else { //is a variable
				if (t.getData(r.data) == null) {
					return 0;
				}
				else {
					int var = (int) t.getData(r.data);
					return var;
				}
			}
		}
		else { //is operator
			//System.out.println(r.data);
			//System.out.println(r.right.data);
			if (r.data.equals("!")) {
				return -1 * evaluate(t, r.right);
			}
			return doOperation(r.data, evaluate(t, r.left), evaluate(t, r.right));
			
		}
		
	}
	private int doOperation(String op, int num1, int num2) {
		//method to complete mathematical operation
		char c = op.charAt(0);
		if (c == '+') {
			//System.out.println(num1);
			//System.out.println(num2);
			return num1 + num2;
		}
		else if (c == '-') {
			return num1 - num2;
		}
		else if (c == '*') {
			return num1 * num2;
		}
		else if (c == '%') {
			return num1 % num2;
		}
		else if (c == '/') {
			return num1 / num2;
		}
		else if (c == '^') {
			return (int) Math.pow(num1, num2);
		}
		else { //c == !
			return num2 * -1;
		}
	}
	
	private boolean isNumber(String s) {
		//method to check if a string is a number
		try {
			int i = Integer.parseInt(s);
		}
		catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public String toPostfix() {
		//return the postfix representation of the expression tree
		return toPostfix(root);
	}
	
	private String toPostfix(Node r) {
		//return the postfix representation of the tree with root r
		if (r == null) {
			return "";
		}
		return toPostfix(r.left) + toPostfix(r.right) + r.data;
	}
	
	public String toInfix() {
		//return the full parenthesized infix representation of the expression tree
		return toInfix(root);
	}
	
	private String toInfix(Node r) {
		//return the fully parenthesized representation of the tree with root r
		if (r == null) {
			return "";
		}
		return "(" + toInfix(r.left) + r.data + toInfix(r.right) + ")";
	}
	
	public static void main(String args[]) throws IOException {
		//used to test expression tree
		/*ExpressionTree tree = new ExpressionTree("! 2 ^ 3");
		
		System.out.println(tree.toInfix());
		
		SymbolTable table = new SymbolTable(10);
		
		System.out.println(tree.evaluate(table));*/
		
	}
}
