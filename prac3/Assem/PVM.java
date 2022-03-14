// Definition of simple stack machine and simple emulator for Assem level 1
// Uses auxiliary methods push, pop and next
// - This version with bounds checks, but no timing
// P.D. Terry, Rhodes University, 2009; Modified KL Bradshaw, 2022 
// Solution for Practical 2 -- several changes to add new opcodes

package Assem;

import java.util.*;
import library.*;

class Processor {
  public int sp; // Stack pointer
  public int hp; // Heap pointer
  public int gp; // Global frame pointer
  public int fp; // Local frame pointer
  public int mp; // Mark stack pointer
  public int ir; // Instruction register
  public int pc; // Program counter
} // end Processor

class PVM {

  // Machine opcodes

  public static final int nop = 1,
      dsp = 2,
      ldc = 3,
      lda = 4,
      ldv = 5,
      sto = 6,
      ldxa = 7,
      inpi = 8,
      prni = 9,
      inpb = 10,
      prnb = 11,
      prns = 12,
      prnl = 13,
      neg = 14,
      add = 15,
      sub = 16,
      mul = 17,
      div = 18,
      rem = 19,
      not = 20,
      and = 21,
      or = 22,
      ceq = 23,
      cne = 24,
      clt = 25,
      cle = 26,
      cgt = 27,
      cge = 28,
      brn = 29,
      bze = 30,
      anew = 31,
      halt = 32,
      stk = 33,
      heap = 34,
      ldl = 35,
      stl = 36,
      lda_0 = 37,
      lda_1 = 38,
      lda_2 = 39,
      lda_3 = 40,
      ldl_0 = 41,
      ldl_1 = 42,
      ldl_2 = 43,
      ldl_3 = 44,
      stl_0 = 45,
      stl_1 = 46,
      stl_2 = 47,
      stl_3 = 48,
      ldc_0 = 49,
      ldc_1 = 50,
      ldc_2 = 51,
      ldc_3 = 52,
      inpc = 53,
      prnc = 54,
      stoc = 55,
      stlc = 56,
      low = 57,
      cap = 58,
      islet = 59,
      inc = 60,
      dec = 61,

      nul = 99; // leave gap for future

  public static String[] mnemonics = new String[PVM.nul + 1];

  // Memory

  public static final int memSize = 5120; // Limit on memory
  public static final int headerSize = 4;
  public static int[] mem; // Simulated memory
  static int stackBase, heapBase; // Limits on cpu.sp

  // Program status

  static final int running = 0,
      finished = 1,
      badMem = 2,
      badData = 3,
      noData = 4,
      divZero = 5,
      badOp = 6,
      badInd = 7,
      badVal = 8,
      badAdr = 9,
      badAll = 10,
      nullRef = 11;

  static int ps;

  // The processor

  static Processor cpu = new Processor();

  // Utilities

  static String padding = "                                                               ";
  static final int maxInt = Integer.MAX_VALUE;
  static final int maxChar = 255;

  static void stackDump(OutFile results, int pcNow) {
    // Dump local variable and stack area - useful for debugging
    int onLine = 0;
    results.write("\nStack dump at " + pcNow);
    results.write(" FP:");
    results.write(cpu.fp, 4);
    results.write(" SP:");
    results.writeLine(cpu.sp, 4);
    for (int i = stackBase - 1; i >= cpu.sp; i--) {
      results.write(i, 7);
      results.write(mem[i], 5);
      onLine++;
      if (onLine % 8 == 0)
        results.writeLine();
    }
    results.writeLine();
  } // PVM.stackDump

  static void heapDump(OutFile results, int pcNow) {
    // Dump heap area - useful for debugging
    if (heapBase == cpu.hp)
      results.writeLine("Empty Heap");
    else {
      int onLine = 0;
      results.write("\nHeap dump at " + pcNow);
      results.write(" HP:");
      results.write(cpu.hp, 4);
      results.write(" HB:");
      results.writeLine(heapBase, 4);
      for (int i = heapBase; i < cpu.hp; i++) {
        results.write(i, 7);
        results.write(mem[i], 5);
        onLine++;
        if (onLine % 8 == 0)
          results.writeLine();
      }
      results.writeLine();
    }
  } // PVM.heapDump

  static void trace(OutFile results, int pcNow, boolean traceStack, boolean traceHeap) {
    // Simple trace facility for run time debugging
    if (traceStack)
      stackDump(results, pcNow);
    if (traceHeap)
      heapDump(results, pcNow);

    results.write(" PC:");
    results.write(pcNow, 5);
    results.write(" FP:");
    results.write(cpu.fp, 5);
    results.write(" SP:");
    results.write(cpu.sp, 5);
    results.write(" HP:");
    results.write(cpu.hp, 5);
    results.write(" TOS:");
    if (cpu.sp < memSize)
      results.write(mem[cpu.sp], 5);
    else
      results.write(" ????");
    results.write("  " + mnemonics[cpu.ir], -8);
    switch (cpu.ir) {
      case PVM.brn:
      case PVM.bze:
      case PVM.dsp:
      case PVM.lda:
      case PVM.ldc:
      case PVM.prns:
        results.write(mem[cpu.pc], 7);
        break;
      default:
        break;
    }
    results.writeLine();
  }

  static void postMortem(OutFile results, int pcNow) {
    // Reports run time error and position
    results.writeLine();
    switch (ps) {
      case badMem:
        results.write("Memory violation");
        break;
      case badData:
        results.write("Invalid data");
        break;
      case noData:
        results.write("No more data");
        break;
      case divZero:
        results.write("Division by zero");
        break;
      case badOp:
        results.write("Illegal opcode");
        break;
      case badInd:
        results.write("Subscript out of range");
        break;
      case badVal:
        results.write("Value out of range");
        break;
      case badAdr:
        results.write("Bad address");
        break;
      case badAll:
        results.write("Heap allocation error");
        break;
      case nullRef:
        results.write("Null reference");
        break;
      default:
        results.write("Interpreter error!");
        break;
    }
    results.writeLine(" at " + pcNow);
  }

  // The interpreters and utility methods

  static int next() {
    // Fetches next word of program and bumps program counter
    return mem[cpu.pc++];
  }

  static void push(int value) {
    // Bumps stack pointer and pushes value onto stack
    mem[--cpu.sp] = value;
    if (cpu.sp < cpu.hp)
      ps = badMem;
  }

  static int pop() {
    // Pops and returns top value on stack and bumps stack pointer
    if (cpu.sp == cpu.fp)
      ps = badMem;
    return mem[cpu.sp++];
  }

  static boolean inBounds(int p) {
    // Check that memory pointer p does not go out of bounds. This should not
    // happen with correct code, but it is just as well to check
    if (p < heapBase || p > memSize)
      ps = badMem;
    return (ps == running);
  }

  public static void emulator(int initPC, int codeLen, int initSP,
      InFile data, OutFile results, boolean tracing, boolean traceStack, boolean traceHeap) {
    // Emulates action of the codeLen instructions stored in mem[0 .. codeLen-1],
    // with
    // program counter initialized to initPC, stack pointer initialized to initSP.
    // data and results are used for I/O. Tracing at the code level may be requested

    int pcNow; // current program counter
    int loop; // internal loops
    int tos, sos; // value popped from stack
    int adr; // effective address for memory accesses
    stackBase = initSP;
    heapBase = codeLen; // initialize boundaries
    cpu.hp = heapBase; // initialize registers
    cpu.sp = stackBase;
    cpu.gp = stackBase;
    cpu.mp = stackBase;
    cpu.fp = stackBase;
    cpu.pc = initPC; // initialize program counter
    ps = running; // prepare to execute
    int ops = 0;

    do {
      ops++;
      pcNow = cpu.pc; // retain for tracing/postmortem
      if (cpu.pc < 0 || cpu.pc >= codeLen) {
        ps = badAdr;
        break;
      }
      cpu.ir = next(); // fetch
      if (tracing)
        trace(results, pcNow, traceStack, traceHeap);
      switch (cpu.ir) { // execute
        case PVM.nop: // no operation
          break;
        case PVM.dsp: // decrement stack pointer (allocate space for variables)
          int localSpace = next();
          cpu.sp -= localSpace;
          if (inBounds(cpu.sp)) // initialize
            for (loop = 0; loop < localSpace; loop++)
              mem[cpu.sp + loop] = 0;
          break;
        case PVM.ldc: // push constant value
          push(next());
          break;
        case PVM.ldc_0:
          push(0);
          break;
        case PVM.ldc_1:
          push(1);
          break;
        case PVM.lda: // push local address
          adr = cpu.fp - 1 - next();
          if (inBounds(adr))
            push(adr);
          break;
        case PVM.lda_0:
          adr = cpu.fp - 1;
          if (inBounds(adr))
            push(adr);
          break;
        case PVM.lda_1:
          adr = cpu.fp - 2;
          if (inBounds(adr))
            push(adr);
          break;
        case PVM.ldv: // dereference
          push(mem[pop()]);
          break;
        case PVM.ldl_0:
          adr = cpu.fp - 1;
          if (inBounds(adr))
            push(adr);
          push(mem[pop()]);
          break;
        case PVM.ldl_1:
          adr = cpu.fp - 2;
          if (inBounds(adr))
            push(adr);
          push(mem[pop()]);
          break;
        case PVM.sto: // store
          tos = pop();
          adr = pop();
          if (inBounds(adr))
            mem[adr] = tos;
          break;
        case PVM.ldxa: // heap array indexing
          adr = pop();
          int heapPtr = pop();
          if (heapPtr == 0)
            ps = nullRef;
          else if (heapPtr < heapBase || heapPtr >= cpu.hp)
            ps = badMem;
          else if (adr < 0 || adr >= mem[heapPtr])
            ps = badInd;
          else
            push(heapPtr + adr + 1);
          break;
        case PVM.inpi: // integer input
          adr = pop();
          if (inBounds(adr)) {
            mem[adr] = data.readInt();
            if (data.error())
              ps = badData;
          }
          break;
        case PVM.prni: // integer output
          if (tracing)
            results.write(padding);
          results.write(pop(), 0);
          if (tracing)
            results.writeLine();
          break;
        case PVM.inpb: // boolean input
          adr = pop();
          if (inBounds(adr)) {
            mem[adr] = data.readBoolean() ? 1 : 0;
            if (data.error())
              ps = badData;
          }
          break;
        case PVM.prnb: // boolean output
          if (tracing)
            results.write(padding);
          if (pop() != 0)
            results.write(" true  ");
          else
            results.write(" false ");
          if (tracing)
            results.writeLine();
          break;
        // ++
        case PVM.inpc: // character input
          adr = pop();
          if (inBounds(adr)) {
            mem[adr] = data.readChar();
            if (data.error())
              ps = badData;
          }
          break;
        case PVM.prnc: // character output
          if (tracing)
            results.write(padding);
          results.write((char) (Math.abs(pop()) % (maxChar + 1)), 1);
          if (tracing)
            results.writeLine();
          break;
        // ++
        case PVM.prns: // string output
          if (tracing)
            results.write(padding);
          loop = next();
          while (ps == running && mem[loop] != 0) {
            results.write((char) mem[loop]);
            loop--;
            if (loop < stackBase)
              ps = badMem;
          }
          if (tracing)
            results.writeLine();
          break;
        case PVM.prnl: // newline
          results.writeLine();
          break;
        case PVM.neg: // integer negation
          push(-pop());
          break;
        case PVM.add: // integer addition
          tos = pop();
          push(pop() + tos);
          break;
        case PVM.sub: // integer subtraction
          tos = pop();
          push(pop() - tos);
          break;
        // ++
        case PVM.mul: // integer multiplication
          tos = pop();
          sos = pop();
          if (tos != 0 && Math.abs(sos) > maxInt / Math.abs(tos))
            ps = badVal;
          else
            push(sos * tos);
          break;
        /*
         * alternatively use an intermediate long value
         * 
         * case PVM.mul: // integer multiplication
         * tos = pop();
         * sos = pop();
         * long temp = (long) sos * (long) tos;
         * if (Math.abs(temp) > maxInt) ps = badVal;
         * else push(sos * tos);
         * break;
         * ++++
         */

        // ++
        case PVM.div: // integer division (quotient)
          tos = pop();
          if (tos == 0)
            ps = divZero;
          else
            push(pop() / tos);
          break;
        case PVM.rem: // integer division (remainder)
          tos = pop();
          if (tos == 0)
            ps = divZero;
          else
            push(pop() % tos);
          break;
        // ++
        case PVM.not: // logical negation
          push(pop() == 0 ? 1 : 0);
          break;
        case PVM.and: // logical and
          tos = pop();
          push(pop() & tos);
          break;
        case PVM.or: // logical or
          tos = pop();
          push(pop() | tos);
          break;
        case PVM.ceq: // logical equality
          tos = pop();
          push(pop() == tos ? 1 : 0);
          break;
        case PVM.cne: // logical inequality
          tos = pop();
          push(pop() != tos ? 1 : 0);
          break;
        case PVM.clt: // logical less
          tos = pop();
          push(pop() < tos ? 1 : 0);
          break;
        case PVM.cle: // logical less or equal
          tos = pop();
          push(pop() <= tos ? 1 : 0);
          break;
        case PVM.cgt: // logical greater
          tos = pop();
          push(pop() > tos ? 1 : 0);
          break;
        case PVM.cge: // logical greater or equal
          tos = pop();
          push(pop() >= tos ? 1 : 0);
          break;
        case PVM.brn: // unconditional branch
          cpu.pc = next();
          if (cpu.pc < 0 || cpu.pc >= codeLen)
            ps = badAdr;
          break;
        case PVM.bze: // pop top of stack, branch if false
          int target = next();
          if (pop() == 0) {
            cpu.pc = target;
            if (cpu.pc < 0 || cpu.pc >= codeLen)
              ps = badAdr;
          }
          break;
        case PVM.anew: // heap array allocation
          int size = pop();
          if (size <= 0 || size + 1 > cpu.sp - cpu.hp - 2)
            ps = badAll;
          else {
            mem[cpu.hp] = size;
            push(cpu.hp);
            cpu.hp += size + 1;
          }
          break;
        case PVM.inc: // ++
          adr = pop();
          if (inBounds(adr))
            mem[adr]++;
          break;
        case PVM.dec: // --
          adr = pop();
          if (inBounds(adr))
            mem[adr]--;
          break;
        case PVM.halt: // halt
          ps = finished;
          break;
        case PVM.stk: // stack dump (debugging)
          stackDump(results, pcNow);
          break;
        case PVM.heap: // heap dump (debugging)
          heapDump(results, pcNow);
          break;

        // ++
        case PVM.stoc: // character checked store
          tos = pop();
          adr = pop();
          if (inBounds(adr))
            if (tos >= 0 && tos <= maxChar)
              mem[adr] = tos;
            else
              ps = badVal;
          break;
        // ++

        // ++
        case PVM.cap: // upper
          push(Character.toUpperCase((char) pop()));
          break;
        // ++

        default: // unrecognized opcode
          ps = badOp;
          break;
      }
    } while (ps == running);

    System.out.println("\n\n" + ops + " operations. ");
    if (ps != finished)
      postMortem(results, pcNow);
  }

  public static void quickInterpret(int codeLen, int initSP) {
    // Interprets the codeLen instructions stored in mem, with stack pointer
    // initialized to initSP. Use StdIn and StdOut without asking
    System.out.println("\nHit <Enter> to start");
    InFile.StdIn.readLine();
    boolean tracing = false;
    InFile data = new InFile("");
    OutFile results = new OutFile("");
    emulator(0, codeLen, initSP, data, results, false, false, false);
  } // PVM.QuickInterpret

  public static void interpret(int codeLen, int initSP) {
    // Interactively opens data and results files. Then interprets the codeLen
    // instructions stored in mem, with stack pointer initialized to initSP
    System.out.print("\nTrace execution (y/N/q)? ");
    char reply = (InFile.StdIn.readLine() + " ").toUpperCase().charAt(0);
    boolean traceStack = false, traceHeap = false;
    if (reply != 'Q') {
      boolean tracing = reply == 'Y';
      if (tracing) {
        System.out.print("\nTrace Stack (y/N)? ");
        traceStack = (InFile.StdIn.readLine() + " ").toUpperCase().charAt(0) == 'Y';
        System.out.print("\nTrace Heap (y/N)? ");
        traceHeap = (InFile.StdIn.readLine() + " ").toUpperCase().charAt(0) == 'Y';
      }

      System.out.print("\nData file [STDIN] ? ");
      InFile data = new InFile(InFile.StdIn.readLine());
      System.out.print("\nResults file [STDOUT] ? ");
      OutFile results = new OutFile(InFile.StdIn.readLine());
      emulator(0, codeLen, initSP, data, results, tracing, traceStack, traceHeap);
      results.close();
      data.close();
    }
  } // PVM.interpret

  public static void listCode(String fileName, int codeLen) {
    // Lists the codeLen instructions stored in mem on a named output file
    int i, j, n;

    if (fileName == null)
      return;
    OutFile codeFile = new OutFile(fileName);

    /*
     * ------------- The following may be useful for debugging the interpreter
     * i = 0;
     * while (i < codeLen) {
     * codeFile.write(mem[i], 5);
     * if ((i + 1) % 15 == 0) codeFile.writeLine();
     * i++;
     * }
     * codeFile.writeLine();
     * 
     * -------------
     */

    i = 0;
    codeFile.writeLine("ASSEM\nBEGIN");
    while (i < codeLen) {
      int o = mem[i] % (PVM.nul + 1); // force in range
      codeFile.write("  {");
      codeFile.write(i, 5);
      codeFile.write(" } ");
      codeFile.write(mnemonics[o], -8);
      switch (o) {
        case PVM.brn:
        case PVM.bze:
        case PVM.dsp:
        case PVM.lda:
        case PVM.ldc:
          i = (i + 1) % memSize;
          codeFile.write(mem[i]);
          break;

        case PVM.prns:
          i = (i + 1) % memSize;
          j = mem[i];
          codeFile.write(" \"");
          while (mem[j] != 0) {
            switch (mem[j]) {
              case '\\':
                codeFile.write("\\\\");
                break;
              case '\"':
                codeFile.write("\\\"");
                break;
              case '\'':
                codeFile.write("\\\'");
                break;
              case '\b':
                codeFile.write("\\b");
                break;
              case '\t':
                codeFile.write("\\t");
                break;
              case '\n':
                codeFile.write("\\n");
                break;
              case '\f':
                codeFile.write("\\f");
                break;
              case '\r':
                codeFile.write("\\r");
                break;
              default:
                codeFile.write((char) mem[j]);
                break;
            }
            j--;
          }
          codeFile.write("\"");
          break;
      }
      i = (i + 1) % memSize;
      codeFile.writeLine();
    }
    codeFile.writeLine("END.");
    codeFile.close();
  } // PVM.listCode

  public static int opCode(String str) {
    // Maps str to opcode, or to PVM.nul if no match can be found
    int op = 0;
    while (op != PVM.nul && !(str.toUpperCase().equals(mnemonics[op])))
      op++;
    return op;
  } // PVM.opCode

  public static void init() {
    // Initializes stack machine
    mem = new int[memSize + 1]; // virtual machine memory
    for (int i = 0; i <= memSize; i++)
      mem[i] = 0;
    // Initialize mnemonic table this way for ease of modification in exercises
    for (int i = 0; i <= PVM.nul; i++)
      mnemonics[i] = "";
    mnemonics[PVM.add] = "ADD";
    mnemonics[PVM.and] = "AND";
    mnemonics[PVM.anew] = "ANEW";
    mnemonics[PVM.brn] = "BRN";
    mnemonics[PVM.bze] = "BZE";
    mnemonics[PVM.cap] = "CAP";
    mnemonics[PVM.ceq] = "CEQ";
    mnemonics[PVM.cge] = "CGE";
    mnemonics[PVM.cgt] = "CGT";
    mnemonics[PVM.cle] = "CLE";
    mnemonics[PVM.clt] = "CLT";
    mnemonics[PVM.cne] = "CNE";
    mnemonics[PVM.dec] = "DEC";
    mnemonics[PVM.div] = "DIV";
    mnemonics[PVM.dsp] = "DSP";
    mnemonics[PVM.halt] = "HALT";
    mnemonics[PVM.heap] = "HEAP";
    mnemonics[PVM.inc] = "INC";
    mnemonics[PVM.inpb] = "INPB";
    mnemonics[PVM.inpc] = "INPC";
    mnemonics[PVM.inpi] = "INPI";
    mnemonics[PVM.islet] = "ISLET";
    mnemonics[PVM.lda] = "LDA";
    mnemonics[PVM.lda_0] = "LDA_0";
    mnemonics[PVM.lda_1] = "LDA_1";
    mnemonics[PVM.lda_2] = "LDA_2";
    mnemonics[PVM.lda_3] = "LDA_3";
    mnemonics[PVM.ldc] = "LDC";
    mnemonics[PVM.ldc_0] = "LDC_0";
    mnemonics[PVM.ldc_1] = "LDC_1";
    mnemonics[PVM.ldc_2] = "LDC_2";
    mnemonics[PVM.ldc_3] = "LDC_3";
    mnemonics[PVM.ldl] = "LDL";
    mnemonics[PVM.ldl_0] = "LDL_0";
    mnemonics[PVM.ldl_1] = "LDL_1";
    mnemonics[PVM.ldl_2] = "LDL_2";
    mnemonics[PVM.ldl_3] = "LDL_3";
    mnemonics[PVM.ldv] = "LDV";
    mnemonics[PVM.ldxa] = "LDXA";
    mnemonics[PVM.low] = "LOW";
    mnemonics[PVM.mul] = "MUL";
    mnemonics[PVM.neg] = "NEG";
    mnemonics[PVM.nop] = "NOP";
    mnemonics[PVM.not] = "NOT";
    mnemonics[PVM.nul] = "NUL";
    mnemonics[PVM.or] = "OR";
    mnemonics[PVM.prnb] = "PRNB";
    mnemonics[PVM.prnc] = "PRNC";
    mnemonics[PVM.prni] = "PRNI";
    mnemonics[PVM.prnl] = "PRNL";
    mnemonics[PVM.prns] = "PRNS";
    mnemonics[PVM.rem] = "REM";
    mnemonics[PVM.stk] = "STK";
    mnemonics[PVM.stl] = "STL";
    mnemonics[PVM.stl_0] = "STL_0";
    mnemonics[PVM.stl_1] = "STL_1";
    mnemonics[PVM.stl_2] = "STL_2";
    mnemonics[PVM.stl_3] = "STL_3";
    mnemonics[PVM.stlc] = "STLC";
    mnemonics[PVM.sto] = "STO";
    mnemonics[PVM.stoc] = "STOC";
    mnemonics[PVM.sub] = "SUB";
  } // PVM.init

} // end PVM
