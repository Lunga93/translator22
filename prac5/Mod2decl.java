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
    scolonSym   =  5,
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
      if (Character.isLetter(ch)) {
        do {
          symLex.append(ch); getChar();
        }while (Character.isLetterOrDigit(ch));
        

        if (symLex.toString().equals("ARRAY")) {
          symKind = arraySym;
        }
        else if (symLex.toString().equals("POINTER")) {
          symKind = pointerSym;
        }
        else if (symLex.toString().equals("TO")) {
          symKind = toSym;
        }
        else if (symLex.toString().equals("RECORD")) {
          symKind = recordSym;
        }
        else if (symLex.toString().equals("OF")) {
          symKind = ofSym;
        }
        else if (symLex.toString().equals("TYPE")) {
          symKind = typeSym;
        }
        else if (symLex.toString().equals("VAR")) {
          symKind = varSym;
        }
        else if (symLex.toString().equals("SET")) {
          symKind = setSym;
        }
        else if (symLex.toString().equals("END")) {
          symKind = endSym;
        }

        else {
          symKind = identSym;
        }
      }
      else if (Character.isDigit(ch)) {
          do{
            symLex.append(ch); getChar();
          } while (Character.isDigit(ch));
          symKind = numSym;
      }
      else {
        symLex.append(ch);
        switch(ch) {
          case EOF:
            symLex = new StringBuilder("EOF");
            symKind = EOFSym; break;
          case ';':
            symKind = scolonSym; getChar(); break;
          case '.':
            symKind = periodSym; getChar();
            if(ch == '.') {
              symLex.append(ch); symKind = rangeSym; getChar();
            }
            break;
          case '=':
            symKind = eqSym; getChar(); break;
          case ',':
            symKind = comaSym; getChar(); break;
          case '[':
            symKind = lsqrbrSym; getChar(); break;
          case ']':
            symKind = rsqrbrSym; getChar(); break;
          case '(':
            symKind = lperanSym; getChar(); break;
          case ')':
            symKind = rperanSym; getChar(); break;
          case ':':
            symKind = colonSym; getChar(); break;
          default:
            symKind = noSym; getChar(); break;
        }

      }
      sym = new Token(symKind, symLex.toString());
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
