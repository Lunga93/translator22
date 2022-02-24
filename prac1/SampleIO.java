// Program to demonstrate Infile, OutFile and IntSet classes
// PD Terry, Rhodes University, 2014

import library.*;

class SampleIO {

  public static void main(String[] args) {
//                                          check that arguments have been supplied
    if (args.length != 2) {
      IO.writeLine("missing args");
      System.exit(1);
    }
//                                          attempt to open data file
    InFile data = new InFile(args[0]);
    if (data.openError()) {
      IO.writeLine("cannot open " + args[0]);
      System.exit(1);
    }
//                                          attempt to open results file
    OutFile results = new OutFile(args[1]);
    if (results.openError()) {
      IO.writeLine("cannot open " + args[1]);
      System.exit(1);
    }
//                                          various initializations
    int total = 0;
    IntSet mySet = new IntSet();
    IntSet smallSet = new IntSet(1, 2, 3, 4, 5);
    String smallSetStr = smallSet.toString();
//                                          read and process data file
    int item = data.readInt();
    while (!data.noMoreData()) {
      total = total + item;
      if (item > 0) mySet.incl(item);
      item = data.readInt();
    }
//                                          write various results to output file
    results.write("total = ");
    results.writeLine(total, 5);
    results.writeLine("unique positive numbers " + mySet.toString());
    results.writeLine("union with " + smallSetStr
                       + " = " + mySet.union(smallSet).toString());
    results.writeLine("intersection with " + smallSetStr
                       + " = " + mySet.intersection(smallSet).toString());
    results.close();
  } // main

} // SampleIO
