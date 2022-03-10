package Calc;


import java.io.*;

public class Parser {
	public static final int _EOF = 0;
	public static final int _decNumber = 1;
	public static final int _hexNumber = 2;
	// terminals
	public static final int EOF_SYM = 0;
	public static final int decNumber_Sym = 1;
	public static final int hexNumber_Sym = 2;
	public static final int equal_Sym = 3;
	public static final int lparen_Sym = 4;
	public static final int rparen_Sym = 5;
	public static final int plus_Sym = 6;
	public static final int minus_Sym = 7;
	public static final int uparrow_Sym = 8;
	public static final int sqrt_Sym = 9;
	public static final int star_Sym = 10;
	public static final int slash_Sym = 11;
	public static final int percent_Sym = 12;
	public static final int NOT_SYM = 13;
	// pragmas

	public static final int maxT = 13;

	static final boolean T = true;
	static final boolean x = false;
	static final int minErrDist = 2;

	public static Token token;    // last recognized token   /* pdt */
	public static Token la;       // lookahead token
	static int errDist = minErrDist;

	

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

	static void Calc() {
		while (StartOf(1)) {
			Expression();
			Expect(equal_Sym);
		}
		Expect(EOF_SYM);
	}

	static void Expression() {
		if (la.kind == lparen_Sym) {
			Get();
			Expression();
			Expect(rparen_Sym);
			while (la.kind == plus_Sym || la.kind == minus_Sym || la.kind == uparrow_Sym) {
				if (la.kind == plus_Sym) {
					Get();
					if (la.kind == decNumber_Sym || la.kind == hexNumber_Sym) {
						Term();
					} else if (StartOf(1)) {
						Expression();
					} else SynErr(14);
				} else if (la.kind == minus_Sym) {
					Get();
					if (la.kind == decNumber_Sym || la.kind == hexNumber_Sym) {
						Term();
					} else if (StartOf(1)) {
						Expression();
					} else SynErr(15);
				} else {
					Get();
					Expression();
				}
			}
		} else if (la.kind == decNumber_Sym || la.kind == hexNumber_Sym) {
			Term();
			while (la.kind == plus_Sym || la.kind == minus_Sym || la.kind == uparrow_Sym) {
				if (la.kind == plus_Sym) {
					Get();
					if (la.kind == decNumber_Sym || la.kind == hexNumber_Sym) {
						Term();
					} else if (StartOf(1)) {
						Expression();
					} else SynErr(16);
				} else if (la.kind == minus_Sym) {
					Get();
					if (la.kind == decNumber_Sym || la.kind == hexNumber_Sym) {
						Term();
					} else if (StartOf(1)) {
						Expression();
					} else SynErr(17);
				} else {
					Get();
					Expression();
				}
			}
		} else if (la.kind == sqrt_Sym) {
			Get();
			Expression();
		} else SynErr(18);
	}

	static void Term() {
		Factor();
		while (la.kind == star_Sym || la.kind == slash_Sym || la.kind == percent_Sym) {
			if (la.kind == star_Sym) {
				Get();
				if (la.kind == decNumber_Sym || la.kind == hexNumber_Sym) {
					Factor();
				} else if (StartOf(1)) {
					Expression();
				} else SynErr(19);
			} else if (la.kind == slash_Sym) {
				Get();
				Factor();
			} else {
				Get();
				Factor();
			}
		}
	}

	static void Factor() {
		if (la.kind == decNumber_Sym) {
			Get();
		} else if (la.kind == hexNumber_Sym) {
			Get();
		} else SynErr(20);
	}



	public static void Parse() {
		la = new Token();
		la.val = "";
		Get();
		Calc();
		Expect(EOF_SYM);

	}

	private static boolean[][] set = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x},
		{x,T,T,x, T,x,x,x, x,T,x,x, x,x,x}

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
			case 1: s = "decNumber expected"; break;
			case 2: s = "hexNumber expected"; break;
			case 3: s = "\"=\" expected"; break;
			case 4: s = "\"(\" expected"; break;
			case 5: s = "\")\" expected"; break;
			case 6: s = "\"+\" expected"; break;
			case 7: s = "\"-\" expected"; break;
			case 8: s = "\"^\" expected"; break;
			case 9: s = "\"sqrt\" expected"; break;
			case 10: s = "\"*\" expected"; break;
			case 11: s = "\"/\" expected"; break;
			case 12: s = "\"%\" expected"; break;
			case 13: s = "??? expected"; break;
			case 14: s = "invalid Expression"; break;
			case 15: s = "invalid Expression"; break;
			case 16: s = "invalid Expression"; break;
			case 17: s = "invalid Expression"; break;
			case 18: s = "invalid Expression"; break;
			case 19: s = "invalid Term"; break;
			case 20: s = "invalid Factor"; break;
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
