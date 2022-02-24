import library.*;

class sievej {
  
  static public void main(String[] args) {
    final int Max = 49000;
    boolean[] Uncrossed = new boolean[Max];
    int i, n, k, it, iterations, primes = 0;
    { IO.write("How many iterations? "); iterations = IO.readInt(); }
    { IO.write("Supply largest number to be tested "); n = IO.readInt(); }
    if (n > Max) {
      { IO.write("n too large, sorry"); }
      return;
    }
    it = 1;
    while (it <= iterations) {
      primes = 0;
      i = 2;
      while (i <= n) {
        Uncrossed[i - 2] = true;
        i = i + 1;
      }
      i = 2;
      while (i <= n) {
        if (Uncrossed[i - 2]) {
          primes = primes + 1;
          k = i;
          Uncrossed[k - 2] = false;
          k = k + i;
          while (k <= n) {
            Uncrossed[k - 2] = false;
            k = k + i;
          }
        }
        i = i + 1;
      }
      it = it + 1;
    }
    { IO.write(primes); IO.write(" primes"); }
  } // main
  
} // sievej