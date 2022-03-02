import library.*;

class gc {

    static IntSet setOfPrimes(int num) {

        IntSet crossed = new IntSet();
        IntSet set = new IntSet();
        int i = 2;
        while (i <= num) {
            if (!crossed.contains(i)) {
                set.incl(i);
                int j = i;
                do {
                    crossed.incl(j);
                    j += i;

                } while (j <= num && j > 0);
            }
            i++;
        }
        return set;
    }

    static public void main(String[] args) {
        int n, i;
        {
            IO.write("Supply largest number to be tested ");
            n = IO.readInt();
        }
        IntSet set = setOfPrimes(n);
        IO.writeLine(setOfPrimes(n));
        boolean conjecture = true;
        i = 4;
        while (conjecture && i <= n) {
            boolean found = false;
            int k = 2;
            while (k <= i / 2 && !found) {

                if (set.contains(k) && set.contains(i - k)) {
                    found = true;
                    IO.writeLine(" " + i + "\t" + k + "\t" + (i - k));
                } else
                    k++;
            }
            if (!found) {
                IO.writeLine("failed for the conjecture of " + i);
                // conjecture = false;
            }
            i = i + 1;
        }
        // IO.writeLine("Conjecture is " + conjecture);

    } // main

} // sievej