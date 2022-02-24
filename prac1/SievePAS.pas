PROGRAM Sieve (Input, Output);
(* Sieve of Eratosthenes for finding primes 2 <= N <= Max
   P.D. Terry, Rhodes University, 2017 *)

  CONST
    Max = 32000                                (* largest number allowed *);
  TYPE
    SIEVES = ARRAY [2 .. Max] OF BOOLEAN;
  VAR
    I, N, K, Primes, It, Iterations : INTEGER  (* counters *);
    Uncrossed : SIEVES                         (* the sieve *);
    Display : BOOLEAN;
  BEGIN
    Write('How many iterations '); Read(Input, Iterations);
    Display := Iterations = 1;
    Write('Supply largest number to be tested '); Read(Input, N);
    IF N > Max THEN BEGIN
      WriteLn(Output, 'N too large, sorry'); HALT
    END;
    WriteLn(Output, 'Prime numbers between 2 and ', N);
    WriteLn(Output, '------------------------------------');
    FOR It := 1 To Iterations DO BEGIN
      Primes := 0 (* no primes yet found *);
      FOR I := 2 TO N DO                       (* clear sieve *)
        Uncrossed[I] := TRUE;
      FOR I := 2 TO N DO                       (* the passes over the sieve *)
        IF Uncrossed[I] THEN BEGIN
          IF Display AND (Primes MOD 8 = 0) THEN WriteLn;    (* ensure line not too long *)
          Primes := Primes + 1;
          IF Display THEN Write(Output, I:6);
          K := I;                              (* now cross out multiples of I *)
          REPEAT
            Uncrossed[K] := FALSE; K := K + I
          UNTIL K > N
        END;
      IF Display THEN WriteLn
    END;
    Write(Primes, ' primes')
  END.
