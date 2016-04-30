// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

//initialize R2 and D to 0 
@R2
M=0
(LOOP)
@R1
M=M-1
D=M

//check if the value of R1 is negative, in order to determine if the process has been finished.
@END
D;JLT

//if not increment the result one time by the first argument (given at R0)
@R2
D=M
@R0
D=D+M

// store the new value at the result register (R2).
@R2
M=D

// go to LOOP anyway.
@LOOP
0;JMP

@END

