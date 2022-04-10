package Parva;

import java.util.*;
import library.*;

class Entry {
	public String name;
	public ArrayList<Integer> refs;


	public Entry(String name) {
	this.name = name;
	this.refs = new ArrayList<Integer>();
	}
} // Entry



class Table {
	static ArrayList<Entry> identifier = new ArrayList<Entry>();


	public static void clearTable() {
		// Reset the table
		identifier = new ArrayList<Entry>();
	}
	

	public static void addRef(String name, boolean declared, int lineRef) {
		// Enters name if not already there, adds another line reference
		int counter  = 0;
		while (counter < identifier.size() && !name.equals(identifier.get(counter).name)) counter++;
		if (counter >= identifier.size()) identifier.add(new Entry(name));
		identifier.get(counter).refs.add(new Integer(declared ? -lineRef : lineRef));

	}// addRef
	public static void printTable() {
		// Prints out all references in the table
		StringBuilder missing = new StringBuilder();
		for (Entry e : identifier) {
		  boolean isDeclared = false;         
		  Parser.output.write(e.name, -18);   
		  int last = 0;                       
		  for (int line : e.refs) {           
			isDeclared = isDeclared || line < 0;
			if (line != last) {               
			  Parser.output.write(line, 5);   
			  last = line;                    
			}
		  }
		  Parser.output.writeLine();
		  if (!isDeclared) missing.append(e.name + " "); // build up list of undeclared nonterminals
		}
		Parser.output.writeLine();
		if (missing.length() > 0) {                      // no need if there were none
		  Parser.output.writeLine("The following are terminals, or undefined non-terminals");
		  Parser.output.writeLine();
		  Parser.output.writeLine(missing.toString());
		}
	}
} // Table
