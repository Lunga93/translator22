import library.*;

class fiboj {
  
  static public int fib(int m) {
    if (m == 0)
      return 0;
    if (m == 1)
      return 1;
    return fib(m - 1) + fib(m - 2);
  } // fib
  
  static public void main(String[] args) {
    int limit;
    { IO.write("Supply upper limit "); limit = IO.readInt(); }
    int i = 0;
    while (i < limit) {
      { IO.write(i); IO.write("\t"); IO.write(fib(i)); IO.write("\n"); }
      i = i + 1;
    }
  } // main
  
} // fiboj