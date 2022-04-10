package Parva;

import library.*;


import java.io.*;

public class Parser {
	public static final int _EOF = 0;
	public static final int _identifier = 1;
	public static final int _number = 2;
	public static final int _stringLit = 3;
	public static final int _charLit = 4;
	// terminals
	public static final int EOF_SYM = 0;
	public static final int identifier_Sym = 1;
	public static final int number_Sym = 2;
	public static final int stringLit_Sym = 3;
	public static final int charLit_Sym = 4;
	public static final int void_Sym = 5;
	public static final int lparen_Sym = 6;
	public static final int rparen_Sym = 7;
	public static final int lbrace_Sym = 8;
	public static final int rbrace_Sym = 9;
	public static final int semicolon_Sym = 10;
	public static final int const_Sym = 11;
	public static final int comma_Sym = 12;
	public static final int equal_Sym = 13;
	public static final int true_Sym = 14;
	public static final int false_Sym = 15;
	public static final int null_Sym = 16;
	public static final int lbrack_Sym = 17;
	public static final int rbrack_Sym = 18;
	public static final int if_Sym = 19;
	public static final int elsif_Sym = 20;
	public static final int else_Sym = 21;
	public static final int break_Sym = 22;
	public static final int continue_Sym = 23;
	public static final int do_Sym = 24;
	public static final int while_Sym = 25;
	public static final int incl_Sym = 26;
	public static final int excl_Sym = 27;
	public static final int for_Sym = 28;
	public static final int in_Sym = 29;
	public static final int return_Sym = 30;
	public static final int halt_Sym = 31;
	public static final int read_Sym = 32;
	public static final int write_Sym = 33;
	public static final int plus_Sym = 34;
	public static final int minus_Sym = 35;
	public static final int new_Sym = 36;
	public static final int bang_Sym = 37;
	public static final int pointpoint_Sym = 38;
	public static final int lbrackrbrack_Sym = 39;
	public static final int int_Sym = 40;
	public static final int bool_Sym = 41;
	public static final int char_Sym = 42;
	public static final int set_Sym = 43;
	public static final int barbar_Sym = 44;
	public static final int star_Sym = 45;
	public static final int slash_Sym = 46;
	public static final int andand_Sym = 47;
	public static final int equalequal_Sym = 48;
	public static final int bangequal_Sym = 49;
	public static final int less_Sym = 50;
	public static final int lessequal_Sym = 51;
	public static final int greater_Sym = 52;
	public static final int greaterequal_Sym = 53;
	public static final int plusequal_Sym = 54;
	public static final int minusequal_Sym = 55;
	public static final int starequal_Sym = 56;
	public static final int slashequal_Sym = 57;
	public static final int percentequal_Sym = 58;
	public static final int andequal_Sym = 59;
	public static final int barequal_Sym = 60;
	public static final int NOT_SYM = 61;
	// pragmas

	public static final int maxT = 61;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public static Token token;    // last recognized token   /* pdt */
	public static Token la;       // lookahead token
	static int errDist = minErrDist;

	public static OutFile output;



	static void SynErr (int n) {
		if (errDist >= minErrDist) Errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public static void SemErr (String msg) {
		if (errDist >= minErrDist) Errors.Error(token.line, token.col, msg); /* pdt */
		errDist = 0;
	}

	public static void SemError (String msg) {
		if (errDist >= minErrDist) Errors.Error(token.line, token.col, msg); /* pdt */
		errDist = 0;
	}

	public static void Warning (String msg) { /* pdt */
		if (errDist >= minErrDist) Errors.Warn(token.line, token.col, msg);
		errDist = 2; //++ 2009/11/04
	}

	public static boolean Successful() { /* pdt */
		return Errors.count == 0;
	}

	public static String LexString() { /* pdt */
		return token.val;
	}

	public static String LookAheadString() { /* pdt */
		return la.val;
	}

	static void Get () {
		for (;;) {
			token = la; /* pdt */
			la = Scanner.Scan();
			if (la.kind <= maxT) { ++errDist; break; }

			la = token; /* pdt */
		}
	}

	static void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}

	static boolean StartOf (int s) {
		return set[s][la.kind];
	}

	static void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}

	static boolean WeakSeparator (int n, int syFol, int repFol) {
		boolean[] s = new boolean[maxT+1];
		if (la.kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			for (int i=0; i <= maxT; i++) {
				s[i] = set[syFol][i] || set[repFol][i] || set[0][i];
			}
			SynErr(n);
			while (!s[la.kind]) Get();
			return StartOf(syFol);
		}
	}

	static void Parva() {
		Table.clearTable();
		Expect(void_Sym);
		Expect(identifier_Sym);
		Table.addRef(token.val, true, token.line);
		Expect(lparen_Sym);
		Expect(rparen_Sym);
		Block();
		Table.printTable();
	}

	static void Block() {
		Expect(lbrace_Sym);
		while (StartOf(1)) {
			Statement();
		}
		Expect(rbrace_Sym);
	}

	static void Statement() {
		switch (la.kind) {
		case lbrace_Sym: {
			Block();
			break;
		}
		case identifier_Sym: {
			Assignments();
			break;
		}
		case const_Sym: {
			ConstDeclarations();
			break;
		}
		case int_Sym: case bool_Sym: case char_Sym: case set_Sym: {
			VarDeclarations();
			break;
		}
		case if_Sym: {
			IfStatement();
			break;
		}
		case while_Sym: {
			WhileStatement();
			break;
		}
		case return_Sym: {
			ReturnStatement();
			break;
		}
		case halt_Sym: {
			HaltStatement();
			break;
		}
		case read_Sym: {
			ReadStatement();
			break;
		}
		case write_Sym: {
			WriteStatement();
			break;
		}
		case for_Sym: {
			ForStatement();
			break;
		}
		case break_Sym: {
			BreakStatement();
			break;
		}
		case continue_Sym: {
			ContinueStatement();
			break;
		}
		case do_Sym: {
			DoWhileStatement();
			break;
		}
		case incl_Sym: {
			InclStatement();
			break;
		}
		case excl_Sym: {
			ExclStatement();
			break;
		}
		case semicolon_Sym: {
			Get();
			break;
		}
		default: SynErr(62); break;
		}
	}

	static void Assignments() {
		Designator();
		if (la.kind == equal_Sym) {
			Get();
		} else if (StartOf(2)) {
			CompoundAssignOp();
		} else SynErr(63);
		Expression();
		Expect(semicolon_Sym);
	}

	static void ConstDeclarations() {
		Expect(const_Sym);
		OneConst();
		while (la.kind == comma_Sym) {
			Get();
			OneConst();
		}
		Expect(semicolon_Sym);
	}

	static void VarDeclarations() {
		Type();
		OneVar();
		while (la.kind == comma_Sym) {
			Get();
			OneVar();
		}
		Expect(semicolon_Sym);
	}

	static void IfStatement() {
		Expect(if_Sym);
		Expect(lparen_Sym);
		Condition();
		Expect(rparen_Sym);
		Statement();
		while (la.kind == elsif_Sym) {
			Get();
			Expect(lparen_Sym);
			Condition();
			Expect(rparen_Sym);
			Statement();
		}
		if (la.kind == else_Sym) {
			Get();
			Statement();
		}
	}

	static void WhileStatement() {
		Expect(while_Sym);
		Expect(lparen_Sym);
		Condition();
		Expect(rparen_Sym);
		Statement();
	}

	static void ReturnStatement() {
		Expect(return_Sym);
		Expect(semicolon_Sym);
	}

	static void HaltStatement() {
		Expect(halt_Sym);
		Expect(semicolon_Sym);
	}

	static void ReadStatement() {
		Expect(read_Sym);
		Expect(lparen_Sym);
		ReadElement();
		while (la.kind == comma_Sym) {
			Get();
			ReadElement();
		}
		Expect(rparen_Sym);
		Expect(semicolon_Sym);
	}

	static void WriteStatement() {
		Expect(write_Sym);
		Expect(lparen_Sym);
		WriteElement();
		while (la.kind == comma_Sym) {
			Get();
			WriteElement();
		}
		Expect(rparen_Sym);
		Expect(semicolon_Sym);
	}

	static void ForStatement() {
		Expect(for_Sym);
		Expect(identifier_Sym);
		Table.addRef(token.val, false, token.line);
		Expect(in_Sym);
		Expect(lparen_Sym);
		ExpList();
		Expect(rparen_Sym);
		Statement();
	}

	static void BreakStatement() {
		Expect(break_Sym);
		Expect(semicolon_Sym);
	}

	static void ContinueStatement() {
		Expect(continue_Sym);
		Expect(semicolon_Sym);
	}

	static void DoWhileStatement() {
		Expect(do_Sym);
		Statement();
		Expect(while_Sym);
		Expect(lparen_Sym);
		Condition();
		Expect(rparen_Sym);
		Expect(semicolon_Sym);
	}

	static void InclStatement() {
		Expect(incl_Sym);
		Expect(lparen_Sym);
		Designator();
		Expect(comma_Sym);
		Expression();
		Expect(rparen_Sym);
		Expect(semicolon_Sym);
	}

	static void ExclStatement() {
		Expect(excl_Sym);
		Expect(lparen_Sym);
		Designator();
		Expect(comma_Sym);
		Expression();
		Expect(rparen_Sym);
		Expect(semicolon_Sym);
	}

	static void OneConst() {
		Expect(identifier_Sym);
		Table.addRef(token.val, false, token.line);
		Expect(equal_Sym);
		Constant();
	}

	static void Constant() {
		if (la.kind == number_Sym) {
			Get();
		} else if (la.kind == charLit_Sym) {
			Get();
		} else if (la.kind == true_Sym) {
			Get();
		} else if (la.kind == false_Sym) {
			Get();
		} else if (la.kind == null_Sym) {
			Get();
		} else SynErr(64);
	}

	static void Type() {
		BasicType();
		if (la.kind == lbrackrbrack_Sym) {
			Get();
		}
	}

	static void OneVar() {
		Expect(identifier_Sym);
		Table.addRef(token.val, true, token.line);
		if (la.kind == equal_Sym) {
			Get();
			Expression();
		}
	}

	static void Expression() {
		AddExp();
		if (StartOf(3)) {
			RelOp();
			AddExp();
		}
	}

	static void Designator() {
		Expect(identifier_Sym);
		Table.addRef(token.val, false, token.line);
		if (la.kind == lbrack_Sym) {
			Get();
			Expression();
			Expect(rbrack_Sym);
		}
	}

	static void CompoundAssignOp() {
		switch (la.kind) {
		case plusequal_Sym: {
			Get();
			break;
		}
		case minusequal_Sym: {
			Get();
			break;
		}
		case starequal_Sym: {
			Get();
			break;
		}
		case slashequal_Sym: {
			Get();
			break;
		}
		case percentequal_Sym: {
			Get();
			break;
		}
		case andequal_Sym: {
			Get();
			break;
		}
		case barequal_Sym: {
			Get();
			break;
		}
		default: SynErr(65); break;
		}
	}

	static void Condition() {
		Expression();
	}

	static void ExpList() {
		Range();
		while (la.kind == comma_Sym) {
			Get();
			Range();
		}
	}

	static void ReadElement() {
		if (la.kind == stringLit_Sym) {
			Get();
		} else if (la.kind == identifier_Sym) {
			Designator();
		} else SynErr(66);
	}

	static void WriteElement() {
		if (la.kind == stringLit_Sym) {
			Get();
		} else if (StartOf(4)) {
			Expression();
		} else SynErr(67);
	}

	static void AddExp() {
		if (la.kind == plus_Sym || la.kind == minus_Sym) {
			if (la.kind == plus_Sym) {
				Get();
			} else {
				Get();
			}
		}
		Term();
		while (la.kind == plus_Sym || la.kind == minus_Sym || la.kind == barbar_Sym) {
			AddOp();
			Term();
		}
	}

	static void RelOp() {
		switch (la.kind) {
		case equalequal_Sym: {
			Get();
			break;
		}
		case bangequal_Sym: {
			Get();
			break;
		}
		case less_Sym: {
			Get();
			break;
		}
		case lessequal_Sym: {
			Get();
			break;
		}
		case greater_Sym: {
			Get();
			break;
		}
		case greaterequal_Sym: {
			Get();
			break;
		}
		case in_Sym: {
			Get();
			break;
		}
		default: SynErr(68); break;
		}
	}

	static void Term() {
		Factor();
		while (la.kind == star_Sym || la.kind == slash_Sym || la.kind == andand_Sym) {
			MulOp();
			Factor();
		}
	}

	static void AddOp() {
		if (la.kind == plus_Sym) {
			Get();
		} else if (la.kind == minus_Sym) {
			Get();
		} else if (la.kind == barbar_Sym) {
			Get();
		} else SynErr(69);
	}

	static void Factor() {
		switch (la.kind) {
		case identifier_Sym: {
			Designator();
			break;
		}
		case number_Sym: case charLit_Sym: case true_Sym: case false_Sym: case null_Sym: {
			Constant();
			break;
		}
		case lbrace_Sym: {
			SetFactor();
			break;
		}
		case new_Sym: {
			Get();
			BasicType();
			Expect(lbrack_Sym);
			Expression();
			Expect(rbrack_Sym);
			break;
		}
		case bang_Sym: {
			Get();
			Factor();
			break;
		}
		case lparen_Sym: {
			Get();
			Expression();
			Expect(rparen_Sym);
			break;
		}
		default: SynErr(70); break;
		}
	}

	static void MulOp() {
		if (la.kind == star_Sym) {
			Get();
		} else if (la.kind == slash_Sym) {
			Get();
		} else if (la.kind == andand_Sym) {
			Get();
		} else SynErr(71);
	}

	static void SetFactor() {
		Expect(lbrace_Sym);
		if (StartOf(4)) {
			ExpList();
		}
		Expect(rbrace_Sym);
	}

	static void BasicType() {
		if (la.kind == int_Sym) {
			Get();
		} else if (la.kind == bool_Sym) {
			Get();
		} else if (la.kind == char_Sym) {
			Get();
		} else if (la.kind == set_Sym) {
			Get();
		} else SynErr(72);
	}

	static void Range() {
		Expression();
		if (la.kind == pointpoint_Sym) {
			Get();
			Expression();
		}
	}



	public static void Parse() {
		la = new Token();
		la.val = "";
		Get();
		Parva();
		Expect(EOF_SYM);

	}

	private static boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,x,x, x,x,x,x, T,x,T,T, x,x,x,x, x,x,x,T, x,x,T,T, T,T,T,T, T,x,T,T, T,T,x,x, x,x,x,x, T,T,T,T, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,T,T, T,x,x},
		{x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, T,T,T,T, T,T,x,x, x,x,x,x, x,x,x},
		{x,T,T,x, T,x,T,x, T,x,x,x, x,x,T,T, T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,T,T, T,T,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x}

	};

} // end Parser

/* pdt - considerable extension from here on */

class ErrorRec {
	public int line, col, num;
	public String str;
	public ErrorRec next;

	public ErrorRec(int l, int c, String s) {
		line = l; col = c; str = s; next = null;
	}

} // end ErrorRec

class Errors {

	public static int count = 0;                                     // number of errors detected
	public static int warns = 0;                                     // number of warnings detected
	public static String errMsgFormat = "file {0} : ({1}, {2}) {3}"; // 0=file 1=line, 2=column, 3=text
	static String fileName = "";
	static String listName = "";
	static boolean mergeErrors = false;
	static PrintWriter mergedList;

	static ErrorRec first = null, last;
	static boolean eof = false;

	static String getLine() {
		char ch, CR = '\r', LF = '\n';
		int l = 0;
		StringBuffer s = new StringBuffer();
		ch = (char) Buffer.Read();
		while (ch != Buffer.EOF && ch != CR && ch != LF) {
			s.append(ch); l++; ch = (char) Buffer.Read();
		}
		eof = (l == 0 && ch == Buffer.EOF);
		if (ch == CR) {  // check for MS-DOS
			ch = (char) Buffer.Read();
			if (ch != LF && ch != Buffer.EOF) Buffer.pos--;
		}
		return s.toString();
	}

	static private String Int(int n, int len) {
		String s = String.valueOf(n);
		int i = s.length(); if (len < i) len = i;
		int j = 0, d = len - s.length();
		char[] a = new char[len];
		for (i = 0; i < d; i++) a[i] = ' ';
		for (j = 0; i < len; i++) {a[i] = s.charAt(j); j++;}
		return new String(a, 0, len);
	}

	static void display(String s, ErrorRec e) {
		mergedList.print("**** ");
		for (int c = 1; c < e.col; c++)
			if (s.charAt(c-1) == '\t') mergedList.print("\t"); else mergedList.print(" ");
		mergedList.println("^ " + e.str);
	}

	public static void Init (String fn, String dir, boolean merge) {
		fileName = fn;
		listName = dir + "listing.txt";
		mergeErrors = merge;
		if (mergeErrors)
			try {
				mergedList = new PrintWriter(new BufferedWriter(new FileWriter(listName, false)));
			} catch (IOException e) {
				Errors.Exception("-- could not open " + listName);
			}
	}

	public static void Summarize () {
		if (mergeErrors) {
			mergedList.println();
			ErrorRec cur = first;
			Buffer.setPos(0);
			int lnr = 1;
			String s = getLine();
			while (!eof) {
				mergedList.println(Int(lnr, 4) + " " + s);
				while (cur != null && cur.line == lnr) {
					display(s, cur); cur = cur.next;
				}
				lnr++; s = getLine();
			}
			if (cur != null) {
				mergedList.println(Int(lnr, 4));
				while (cur != null) {
					display(s, cur); cur = cur.next;
				}
			}
			mergedList.println();
			mergedList.println(count + " errors detected");
			if (warns > 0) mergedList.println(warns + " warnings detected");
			mergedList.close();
		}
		switch (count) {
			case 0 : System.out.println("Parsed correctly"); break;
			case 1 : System.out.println("1 error detected"); break;
			default: System.out.println(count + " errors detected"); break;
		}
		if (warns > 0) System.out.println(warns + " warnings detected");
		if ((count > 0 || warns > 0) && mergeErrors) System.out.println("see " + listName);
	}

	public static void storeError (int line, int col, String s) {
		if (mergeErrors) {
			ErrorRec latest = new ErrorRec(line, col, s);
			if (first == null) first = latest; else last.next = latest;
			last = latest;
		} else printMsg(fileName, line, col, s);
	}

	public static void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "identifier expected"; break;
			case 2: s = "number expected"; break;
			case 3: s = "stringLit expected"; break;
			case 4: s = "charLit expected"; break;
			case 5: s = "\"void\" expected"; break;
			case 6: s = "\"(\" expected"; break;
			case 7: s = "\")\" expected"; break;
			case 8: s = "\"{\" expected"; break;
			case 9: s = "\"}\" expected"; break;
			case 10: s = "\";\" expected"; break;
			case 11: s = "\"const\" expected"; break;
			case 12: s = "\",\" expected"; break;
			case 13: s = "\"=\" expected"; break;
			case 14: s = "\"true\" expected"; break;
			case 15: s = "\"false\" expected"; break;
			case 16: s = "\"null\" expected"; break;
			case 17: s = "\"[\" expected"; break;
			case 18: s = "\"]\" expected"; break;
			case 19: s = "\"if\" expected"; break;
			case 20: s = "\"elsif\" expected"; break;
			case 21: s = "\"else\" expected"; break;
			case 22: s = "\"break\" expected"; break;
			case 23: s = "\"continue\" expected"; break;
			case 24: s = "\"do\" expected"; break;
			case 25: s = "\"while\" expected"; break;
			case 26: s = "\"incl\" expected"; break;
			case 27: s = "\"excl\" expected"; break;
			case 28: s = "\"for\" expected"; break;
			case 29: s = "\"in\" expected"; break;
			case 30: s = "\"return\" expected"; break;
			case 31: s = "\"halt\" expected"; break;
			case 32: s = "\"read\" expected"; break;
			case 33: s = "\"write\" expected"; break;
			case 34: s = "\"+\" expected"; break;
			case 35: s = "\"-\" expected"; break;
			case 36: s = "\"new\" expected"; break;
			case 37: s = "\"!\" expected"; break;
			case 38: s = "\"..\" expected"; break;
			case 39: s = "\"[]\" expected"; break;
			case 40: s = "\"int\" expected"; break;
			case 41: s = "\"bool\" expected"; break;
			case 42: s = "\"char\" expected"; break;
			case 43: s = "\"set\" expected"; break;
			case 44: s = "\"||\" expected"; break;
			case 45: s = "\"*\" expected"; break;
			case 46: s = "\"/\" expected"; break;
			case 47: s = "\"&&\" expected"; break;
			case 48: s = "\"==\" expected"; break;
			case 49: s = "\"!=\" expected"; break;
			case 50: s = "\"<\" expected"; break;
			case 51: s = "\"<=\" expected"; break;
			case 52: s = "\">\" expected"; break;
			case 53: s = "\">=\" expected"; break;
			case 54: s = "\"+=\" expected"; break;
			case 55: s = "\"-=\" expected"; break;
			case 56: s = "\"*=\" expected"; break;
			case 57: s = "\"/=\" expected"; break;
			case 58: s = "\"%=\" expected"; break;
			case 59: s = "\"&=\" expected"; break;
			case 60: s = "\"|=\" expected"; break;
			case 61: s = "??? expected"; break;
			case 62: s = "invalid Statement"; break;
			case 63: s = "invalid Assignments"; break;
			case 64: s = "invalid Constant"; break;
			case 65: s = "invalid CompoundAssignOp"; break;
			case 66: s = "invalid ReadElement"; break;
			case 67: s = "invalid WriteElement"; break;
			case 68: s = "invalid RelOp"; break;
			case 69: s = "invalid AddOp"; break;
			case 70: s = "invalid Factor"; break;
			case 71: s = "invalid MulOp"; break;
			case 72: s = "invalid BasicType"; break;
			default: s = "error " + n; break;
		}
		storeError(line, col, s);
		count++;
	}

	public static void SemErr (int line, int col, int n) {
		storeError(line, col, ("error " + n));
		count++;
	}

	public static void Error (int line, int col, String s) {
		storeError(line, col, s);
		count++;
	}

	public static void Error (String s) {
		if (mergeErrors) mergedList.println(s); else System.out.println(s);
		count++;
	}

	public static void Warn (int line, int col, String s) {
		storeError(line, col, s);
		warns++;
	}

	public static void Warn (String s) {
		if (mergeErrors) mergedList.println(s); else System.out.println(s);
		warns++;
	}

	public static void Exception (String s) {
		System.out.println(s);
		System.exit(1);
	}

	private static void printMsg(String fileName, int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.replace(pos, pos+3, fileName); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{2}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{3}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		System.out.println(b.toString());
	}

} // end Errors
