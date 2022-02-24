import library.*;

class voter {
  
  static public void main(String[] args) {
    final int votingAge = 18;
    final boolean overTheHill = true;
    int age, eligible = 0, total = 0;
    boolean allEligible = true;
    int[] voters = new int[100];
    { IO.write("Supply ages "); age = IO.readInt(); }
    while (age > 0) {
      boolean canVote = age > votingAge;
      allEligible = allEligible && canVote;
      if (canVote) {
        voters[eligible] = age;
        eligible = eligible + 1;
        total = total + voters[eligible - 1];
      }
      { age = IO.readInt(); }
    }
    { IO.write(eligible); IO.write(" voters.  Average age is "); IO.write(total / eligible); IO.write("\n"); }
    if (allEligible)
      { IO.write("Everyone was above voting age"); }
  } // main
  
} // voter