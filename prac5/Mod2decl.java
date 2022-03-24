  // Do learn to insert your names and a brief description of
  // what the program is supposed to do!
  // Lunga Phakathi, Mduduzi Ndlovu, Jessy-Maurice Bossekota

  // This is a skeleton program for developing a parser for Modula-2 declarations
  // P.D. Terry, Rhodes University; Modified by KL Bradshaw 2022

  import java.util.*;
  import library.*;

  class Token {
    public int kind;
    public String val;

    public Token(int kind, String val) {
      this.kind = kind;
      this.val = val;
    }

  } // Token

  class Mod2decl {

    // +++++++++++++++++++++++++ File Handling and Error handlers ++++++++++++++++++++

    static InFile input;
    static OutFile output;

    static String newFileName(String oldFileName, String ext) {
    // Creates new file name by changing extension of oldFileName to ext
      int i = oldFileName.lastIndexOf('.');
      if (i < 0) return oldFileName + ext; else return oldFileName.substring(0, i) + ext;
    }

    static void reportError(String errorMessage) {
    // Displays errorMessage on standard output and on reflected output
      System.out.println(errorMessage);
      output.writeLine(errorMessage);
    }

    static void abort(String errorMessage) {
    // Abandons parsing after issuing error message
      reportError(errorMessage);
      output.close();
      System.exit(1);
    }

    // +++++++++++++++++++++++  token kinds enumeration +++++++++++++++++++++++++

    static final int
    noSym       =  0,
		EOFSym      =  1,
    numSym      =  2,
    identSym    =  3,
    typeSym     =  4,
    scolonSym   = 5,
    varSym      =  6,
    eqSym       =  7,
    colonSym    =  8,
    periodSym   =  9,
    lsqrbrSym   =  10,
    rsqrbrSym   =  11,
    rangeSym    =  12,
    lperanSym   =  13,
    rperanSym   =  14,
    comaSym     =  15,
    arraySym    =  16,
    ofSym       =  17,
    recordSym   =  18,
    endSym      =  19,
    setSym      =  20,
    pointerSym  =  21,
    toSym       =  22;
		

      // and others like this

    // +++++++++++++++++++++++++++++ Character Handler ++++++++++++++++++++++++++

    static final char EOF = '\0';
    static boolean atEndOfFile = false;

    // Declaring ch as a global variable is done for expediency - global variables
    // are not always a good thing

    static char ch;    // look ahead character for scanner

    static void getChar() {
    // Obtains next character ch from input, or CHR(0) if EOF reached
    // Reflect ch to output
      if (atEndOfFile) ch = EOF;
      else {
        ch = input.readChar();
        atEndOfFile = ch == EOF;
        if (!atEndOfFile) output.write(ch);
      }
    } // getChar

    // +++++++++++++++++++++++++++++++ Scanner ++++++++++++++++++++++++++++++++++

    // Declaring sym as a global variable is done for expediency - global variables
    // are not always a good thing

    static Token sym;

    static void getSym() {
    // Scans for next sym from input
      while (ch > EOF && ch <= ' ') getChar();
      StringBuilder symLex = new StringBuilder();
      int symKind = noSym;

      // over to you!

      sym = new Token(symKind, symLex.toString());

      if(Character.isLetter(ch)) {

      }
    } // getSym

  /*  ++++ Commented out for the moment

    // +++++++++++++++++++++++++++++++ Parser +++++++++++++++++++++++++++++++++++

    static void accept(int wantedSym, String errorMessage) {
    // Checks that lookahead token is wantedSym
      if (sym.kind == wantedSym) getSym(); else abort(errorMessage);
    } // accept


    static void accept(IntSet allowedSet, String errorMessage) {
    // Checks that lookahead token is in allowedSet
      if (allowedSet.contains(sym.kind)) getSym(); else abort(errorMessage);
    } // accept
  ++++++ */

    // +++++++++++++++++++++ Main driver function +++++++++++++++++++++++++++++++

    public static void main(String[] args) {
      // Open input and output files from command line arguments
      if (args.length == 0) {
        System.out.println("Usage: MOD2 FileName");
        System.exit(1);
      }
      input = new InFile(args[0]);
      output = new OutFile(newFileName(args[0], ".out"));

      getChar();                                  // Lookahead character

  //  To test the scanner we can use a loop like the following:

      do {
        getSym();                                 // Lookahead symbol
        OutFile.StdOut.write(sym.kind, 3);
        OutFile.StdOut.writeLine(" " + sym.val);
      } while (sym.kind != EOFSym);

  /*  After the scanner is debugged, comment out lines 127 to 131 and uncomment lines 135 to 138. 
      In other words, replace the code immediately above with this code:

      getSym();                                   // Lookahead symbol
      Mod2Decl();                                 // Start to parse from the goal symbol
      // if we get back here everything must have been satisfactory
      System.out.println("Parsed correctly");
  */
       output.close();
    } // main

  } // Mod2decl
