import library.*;

class sampleIO {
  
  static public void main(String[] args) {
    int a, b;
    { IO.write("Supply a and b (0 stops) "); a = IO.readInt(); b = IO.readInt(); }
    while (a != 0) {
      int c = a - a / b * b;
      { IO.write(a); IO.write(b); IO.write(c); }
      { a = IO.readInt(); b = IO.readInt(); }
    }
  } // main
  
} // sampleIO