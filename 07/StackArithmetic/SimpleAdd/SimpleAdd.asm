
// push constant 7
@7
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 8
@8
D=A
@0
A=M
M=D
@0
M=M+1

// add
@0
M=M-1
@0
A=M
D=M
@0
M=M-1
@0
A=M
D=D+M
@0
A=M
M=D
@0
M=M+1
(END)
@END
0;JMP
