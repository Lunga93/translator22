@echo off

rem Use the following for the JDK compiler
javac -d . -nowarn Assem\PVM.java Assem\Assem.java Assem\PVMAsm.java

rem Use the following for the JIKES compiler
rem jikes -d . -deprecation -nowarn -Xstdout Assem\PVM.java Assem\Assem.java Assem\PVMAsm.java
