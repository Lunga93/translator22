PROGRAM Fibo;
(* Print a table of Fibonacci numbers using (slow) recursive definition
   P.D. Terry,  Rhodes University, 2017 *)

  FUNCTION Fib (m : LONGINT) : LONGINT;
  (* Compute m-th term in Fibonacci series 0,1,1,2 ... *)
    BEGIN
      IF m = 0 THEN Fib := 0
      ELSE IF m = 1 THEN Fib := 1
      ELSE Fib := Fib(m-1) + Fib(m-2);
    END;

  VAR
    i, limit : LONGINT;

  BEGIN
    Write('Supply upper limit ');
    Read(limit);
    i := 0;
    WHILE (i < limit) DO BEGIN
      WriteLn(i:5 , Fib(i) : 15);
      i := i + 1;
    END
  END.
