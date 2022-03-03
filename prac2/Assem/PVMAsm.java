// Very simple assembler for simple stack machine
// P.D. Terry, Rhodes University, 2009
// Modified by KL Bradshaw for Prac 2, 2021

package Assem;

import java.io.*;
import library.*;

  class PVMAsm {

    static InFile src;
    static char ch;
    static boolean okay;
    static int codeLen, stkBase;

    public static int codeLength() {
      return codeLen;
    }

    public static int stackBase() {
      return stkBase;
    }

    static void skipLabel() {
      do {
        ch = src.readChar();
        if (ch == ';') src.readLn(); // ignore comments
      } while (!(src.eof() || Character.isLetter(ch)));
    }

    static void error(String message, int codeLen) {
      System.out.println(message + " at " + codeLen); okay = false;
    }

    static String readMnemonic() {
      StringBuffer sb = new StringBuffer();
      while (ch > ' ') {
        sb.append(Character.toUpperCase(ch));
        ch = src.readChar();
      }
      String s = sb.toString();
      // check for directives
      if (s.equals("ASSEM") || s.equals("BEGIN") || s.equals("END.") ) return null;
      return s;
    }

    public static boolean assemble(String sourceName) {
    // Assembles source code from an input file sourceName and loads codeLen
    // words of code directly into memory PVM.mem[0 .. codeLen-1],
    // storing strings in the string pool at the top of memory in
    // PVM.mem[stkBase .. memSize-1].
    //
    // Returns true if assembly succeeded
    //
    // Instruction format :
    //    Instruction  = [ Label ] Opcode [ AddressField ] [ Comment ]
    //    Label        = [ "{" ] Integer [ "}" ]
    //    Opcode       = Mnemonic
    //    AddressField = Integer | "String"
    //    Comment      = String
    //
    // A string AddressField may only be used with a PRNS opcode
    // Instructions are supplied one to a line; terminated at end of input file

      String mnemonic;   // mnemonic for matching
      char quote;        // string delimiter

      src = new InFile(sourceName);
      if (src.openError()) {
        System.out.println("Could not open input file\n");
        System.exit(1);
      }
      System.out.println("Assembling code ... \n");
      codeLen = 0;
      stkBase = PVM.memSize - 1;
      okay = true;
      do {
        skipLabel();                                 // ignore labels at start of line
        if (!src.eof()) {                            // we must have a line
          mnemonic = readMnemonic();                 // unpack mnemonic
          if (mnemonic == null) continue;            // ignore directives
          int op = PVM.opCode(mnemonic);             // look it up
          PVM.mem[codeLen] = op;                     // store in memory
          switch (op) {
            case PVM.prns:                           // requires a string address field
              do quote = src.readChar();             // search for opening quote character
              while (quote != '"' && quote != '\'' && quote != '\n');
              codeLen = (codeLen + 1) % PVM.memSize;
              PVM.mem[codeLen] = stkBase - 1;        // pointer to start of string
              if (quote == '\n')
                error("Missing string address", codeLen);
              else {                                 // stack string in literal pool
                ch = src.readChar();
                while (ch != quote && ch != '\n') {
                  if (ch == '\\') {
                    ch = src.readChar();
                    if (ch == '\n')                  // closing quote missing
                      error("Malformed string", codeLen);
                    switch(ch) {
                      case 't' : ch = '\t'; break;
                      case 'n' : ch = '\n'; break;
                      case '\"' : ch = '\"'; break;
                      case '\'' : ch = '\''; break;
                      default: break;
                    }
                  }
                  stkBase--;
                  PVM.mem[stkBase] = ch;
                  ch = src.readChar();
                }
                if (ch == '\n')                      // closing quote missing
                  error("Malformed string", codeLen);
              }
              stkBase--;
              PVM.mem[stkBase] = 0;                  // terminate string with nul
              break;
            case PVM.brn:                            // all require numeric address field
            case PVM.bze:
            case PVM.dsp:
            case PVM.lda:
            case PVM.ldc:
              codeLen = (codeLen + 1) % PVM.memSize;
              if (ch == '\n')                        // no field could be found
                error("Missing address", codeLen);
              else {                                 // unpack it and store
                PVM.mem[codeLen] = src.readInt();
                if (src.error()) error("Bad address", codeLen);
              }
              break;
            case PVM.nul:                            // unrecognized mnemonic
              System.out.print(mnemonic);
              error(" - Invalid opcode", codeLen);
              break;
            default:                                 // no address needed
              break;
          }
          if (!src.eol()) src.readLn();              // skip comments
          codeLen = (codeLen + 1) % PVM.memSize;
        }
      } while (!src.eof());
      for (int i = codeLen; i < stkBase; i++)        // fill with invalid OpCodes
        PVM.mem[i] = PVM.nul;
      return okay;
    }

  } // end PVMAsm
