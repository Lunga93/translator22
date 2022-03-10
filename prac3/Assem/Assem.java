// PVMAsm/Interpreter for simple stack machine
// P.D. Terry, Rhodes University, 2009 - modified by KL Bradshaw, 2022
// This version for Practical 2 only -- note that it supports "asm xx immediate"
// No changes needed

package Assem;

import java.io.*;

  class Assem {

    static String newFileName(String s, String ext) {
    // Returns a new string for a file name with the extension in s replaced by ext
      int i = s.lastIndexOf('.');
      if (i < 0) return s + ext; else return s.substring(0, i) + ext;
    }

    public static void main(String[] args)
	{
      if (args.length == 0) {  // check on correct parameter usage
        System.out.println("Usage: ASSEM source ");
      }
      else {
        String sourceName = args[0];
		boolean immediate = args.length > 1;
        String codeName = newFileName(sourceName, ".cod");
        PVM.init();

        boolean assembledOK = PVMAsm.assemble(sourceName);
        int codeLength = PVMAsm.codeLength();
        int initSP = PVMAsm.stackBase();
        PVM.listCode(codeName, codeLength);
        if (!assembledOK || codeLength == 0)
          System.out.println("Unable to interpret code");
        else {
				System.out.println("Interpreting code ...");
				if (immediate) PVM.quickInterpret(codeLength, initSP);
					else PVM.interpret(codeLength, initSP);
			}
      }
    }  // main

  } // end Assem
