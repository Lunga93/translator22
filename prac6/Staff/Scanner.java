package Staff;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;

class Token {
	public int kind;    // token kind
	public int pos;     // token position in the source text (starting at 0)
	public int col;     // token column (starting at 0)
	public int line;    // token line (starting at 1)
	public String val;  // token value
	public Token next;  // AW 2003-03-07 Tokens are kept in linked list
}

class Buffer {
	public static final char EOF = (char)256;
	static byte[] buf;
	static int bufLen;
	static int pos;

	public static void Fill (FileInputStream s) {
		try {
			bufLen = s.available();
			buf = new byte[bufLen];
			s.read(buf, 0, bufLen);
			pos = 0;
		} catch (IOException e){
			System.out.println("--- error on filling the buffer ");
			System.exit(1);
		}
	}

	public static int Read () {
		if (pos < bufLen) return buf[pos++] & 0xff;  // mask out sign bits
		else return EOF;                             /* pdt */
	}

	public static int Peek () {
		if (pos < bufLen) return buf[pos] & 0xff;    // mask out sign bits
		else return EOF;                             /* pdt */
	}

	/* AW 2003-03-10 moved this from ParserGen.cs */
	public static String GetString (int beg, int end) {
		StringBuffer s = new StringBuffer(64);
		int oldPos = Buffer.getPos();
		Buffer.setPos(beg);
		while (beg < end) { s.append((char)Buffer.Read()); beg++; }
		Buffer.setPos(oldPos);
		return s.toString();
	}

	public static int getPos() {
		return pos;
	}

	public static void setPos (int value) {
		if (value < 0) pos = 0;
		else if (value >= bufLen) pos = bufLen;
		else pos = value;
	}

} // end Buffer

public class Scanner {
	static final char EOL = '\n';
	static final int  eofSym = 0;
	static final int charSetSize = 256;
	static final int maxT = 18;
	static final int noSym = 18;
	// terminals
	static final int EOF_SYM = 0;
	static final int name_Sym = 1;
	static final int initial_Sym = 2;
	static final int comma_Sym = 3;
	static final int point_Sym = 4;
	static final int Professor_Sym = 5;
	static final int Dr_Sym = 6;
	static final int Mr_Sym = 7;
	static final int Mrs_Sym = 8;
	static final int Ms_Sym = 9;
	static final int Miss_Sym = 10;
	static final int Prof_Sym = 11;
	static final int BSc_Sym = 12;
	static final int BCom_Sym = 13;
	static final int BSclparenHonsrparen_Sym = 14;
	static final int BComlparenHonsrparen_Sym = 15;
	static final int MSc_Sym = 16;
	static final int PhD_Sym = 17;
	static final int NOT_SYM = 18;
	// pragmas

	static short[] start = {
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  6,  0,  7,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  5, 20,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,
	  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
	  -1};


	static Token t;          // current token
	static char ch;          // current input character
	static int pos;          // column number of current character
	static int line;         // line number of current character
	static int lineStart;    // start position of current line
	static int oldEols;      // EOLs that appeared in a comment;
	static BitSet ignore;    // set of characters to be ignored by the scanner

	static Token tokens;     // the complete input token stream
	static Token pt;         // current peek token

	public static void Init (String fileName) {
		FileInputStream s = null;
		try {
			s = new FileInputStream(fileName);
			Init(s);
		} catch (IOException e) {
			System.out.println("--- Cannot open file " + fileName);
			System.exit(1);
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					System.out.println("--- Cannot close file " + fileName);
					System.exit(1);
				}
			}
		}
	}

	public static void Init (FileInputStream s) {
		Buffer.Fill(s);
		pos = -1; line = 1; lineStart = 0;
		oldEols = 0;
		NextCh();
		ignore = new BitSet(charSetSize+1);
		ignore.set(' '); // blanks are always white space
		ignore.set(0); ignore.set(1); ignore.set(2); ignore.set(3); 
		ignore.set(4); ignore.set(5); ignore.set(6); ignore.set(7); 
		ignore.set(8); ignore.set(9); ignore.set(10); ignore.set(11); 
		ignore.set(12); ignore.set(13); ignore.set(14); ignore.set(15); 
		ignore.set(16); ignore.set(17); ignore.set(18); ignore.set(19); 
		ignore.set(20); ignore.set(21); ignore.set(22); ignore.set(23); 
		ignore.set(24); ignore.set(25); ignore.set(26); ignore.set(27); 
		ignore.set(28); ignore.set(29); ignore.set(30); ignore.set(31); 
		
		//--- AW: fill token list
		tokens = new Token();  // first token is a dummy
		Token node = tokens;
		do {
			node.next = NextToken();
			node = node.next;
		} while (node.kind != eofSym);
		node.next = node;
		node.val = "EOF";
		t = pt = tokens;
	}

	static void NextCh() {
		if (oldEols > 0) { ch = EOL; oldEols--; }
		else {
			ch = (char)Buffer.Read(); pos++;
			// replace isolated '\r' by '\n' in order to make
			// eol handling uniform across Windows, Unix and Mac
			if (ch == '\r' && Buffer.Peek() != '\n') ch = EOL;
			if (ch == EOL) { line++; lineStart = pos + 1; }
		}

	}



	static void CheckLiteral() {
		String lit = t.val;
		if (lit.compareTo("Professor") == 0) t.kind = Professor_Sym;
		else if (lit.compareTo("Dr") == 0) t.kind = Dr_Sym;
		else if (lit.compareTo("Mr") == 0) t.kind = Mr_Sym;
		else if (lit.compareTo("Mrs") == 0) t.kind = Mrs_Sym;
		else if (lit.compareTo("Ms") == 0) t.kind = Ms_Sym;
		else if (lit.compareTo("Miss") == 0) t.kind = Miss_Sym;
		else if (lit.compareTo("Prof") == 0) t.kind = Prof_Sym;
		else if (lit.compareTo("BSc") == 0) t.kind = BSc_Sym;
		else if (lit.compareTo("BCom") == 0) t.kind = BCom_Sym;
		else if (lit.compareTo("MSc") == 0) t.kind = MSc_Sym;
		else if (lit.compareTo("PhD") == 0) t.kind = PhD_Sym;
	}

	/* AW Scan() renamed to NextToken() */
	static Token NextToken() {
		while (ignore.get(ch)) NextCh();

		t = new Token();
		t.pos = pos; t.col = pos - lineStart + 1; t.line = line;
		int state = start[ch];
		StringBuffer buf = new StringBuffer(16);
		buf.append(ch); NextCh();
		boolean done = false;
		while (!done) {
			switch (state) {
				case -1: { t.kind = eofSym; done = true; break; }  // NextCh already done /* pdt */
				case 0: { t.kind = noSym; done = true; break; }    // NextCh already done
				case 1:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 2:
					if ((ch >= 'A' && ch <= 'Z')) { buf.append(ch); NextCh(); state = 1; break;}
					else { t.kind = noSym; done = true; break; }
				case 3:
					if ((ch >= 'A' && ch <= 'Z')) { buf.append(ch); NextCh(); state = 1; break;}
					else { t.kind = noSym; done = true; break; }
				case 4:
					{ t.kind = initial_Sym; done = true; break; }
				case 5:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == '.') { buf.append(ch); NextCh(); state = 4; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 6:
					{ t.kind = comma_Sym; done = true; break; }
				case 7:
					{ t.kind = point_Sym; done = true; break; }
				case 8:
					if (ch == 'H') { buf.append(ch); NextCh(); state = 9; break;}
					else { t.kind = noSym; done = true; break; }
				case 9:
					if (ch == 'o') { buf.append(ch); NextCh(); state = 10; break;}
					else { t.kind = noSym; done = true; break; }
				case 10:
					if (ch == 'n') { buf.append(ch); NextCh(); state = 11; break;}
					else { t.kind = noSym; done = true; break; }
				case 11:
					if (ch == 's') { buf.append(ch); NextCh(); state = 12; break;}
					else { t.kind = noSym; done = true; break; }
				case 12:
					if (ch == ')') { buf.append(ch); NextCh(); state = 13; break;}
					else { t.kind = noSym; done = true; break; }
				case 13:
					{ t.kind = BSclparenHonsrparen_Sym; done = true; break; }
				case 14:
					if (ch == 'H') { buf.append(ch); NextCh(); state = 15; break;}
					else { t.kind = noSym; done = true; break; }
				case 15:
					if (ch == 'o') { buf.append(ch); NextCh(); state = 16; break;}
					else { t.kind = noSym; done = true; break; }
				case 16:
					if (ch == 'n') { buf.append(ch); NextCh(); state = 17; break;}
					else { t.kind = noSym; done = true; break; }
				case 17:
					if (ch == 's') { buf.append(ch); NextCh(); state = 18; break;}
					else { t.kind = noSym; done = true; break; }
				case 18:
					if (ch == ')') { buf.append(ch); NextCh(); state = 19; break;}
					else { t.kind = noSym; done = true; break; }
				case 19:
					{ t.kind = BComlparenHonsrparen_Sym; done = true; break; }
				case 20:
					if ((ch >= 'A' && ch <= 'B'
					  || ch >= 'D' && ch <= 'R'
					  || ch >= 'T' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == '.') { buf.append(ch); NextCh(); state = 4; break;}
					else if (ch == 'S') { buf.append(ch); NextCh(); state = 21; break;}
					else if (ch == 'C') { buf.append(ch); NextCh(); state = 22; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 21:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'b'
					  || ch >= 'd' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == 'c') { buf.append(ch); NextCh(); state = 23; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 22:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'n'
					  || ch >= 'p' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == 'o') { buf.append(ch); NextCh(); state = 24; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 23:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == '(') { buf.append(ch); NextCh(); state = 8; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 24:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'l'
					  || ch >= 'n' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == 'm') { buf.append(ch); NextCh(); state = 25; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }
				case 25:
					if ((ch >= 'A' && ch <= 'Z'
					  || ch >= 'a' && ch <= 'z')) { buf.append(ch); NextCh(); state = 1; break;}
					else if (ch == 39) { buf.append(ch); NextCh(); state = 2; break;}
					else if (ch == '-') { buf.append(ch); NextCh(); state = 3; break;}
					else if (ch == '(') { buf.append(ch); NextCh(); state = 14; break;}
					else { t.kind = name_Sym; t.val = buf.toString(); CheckLiteral(); return t; }

			}
		}
		t.val = buf.toString();
		return t;
	}

	/* AW 2003-03-07 get the next token, move on and synch peek token with current */
	public static Token Scan () {
		t = pt = t.next;
		return t;
	}

	/* AW 2003-03-07 get the next token, ignore pragmas */
	public static Token Peek () {
		do {                      // skip pragmas while peeking
			pt = pt.next;
		} while (pt.kind > maxT);
		return pt;
	}

	/* AW 2003-03-11 to make sure peek start at current scan position */
	public static void ResetPeek () { pt = t; }

} // end Scanner
