
// push constant 111
@111
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 333
@333
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 888
@888
D=A
@0
A=M
M=D
@0
M=M+1

// pop static 8
@0
M=M-1
@StaticTest.var_8
D=A
@0
A=A+D
D=A
@popAddr
M=D
@0
A=M
D=M
@popAddr
A=M
M=D

// pop static 3
@0
M=M-1
@StaticTest.var_3
D=A
@0
A=A+D
D=A
@popAddr
M=D
@0
A=M
D=M
@popAddr
A=M
M=D

// pop static 1
@0
M=M-1
@StaticTest.var_1
D=A
@0
A=A+D
D=A
@popAddr
M=D
@0
A=M
D=M
@popAddr
A=M
M=D

// push static 3
@StaticTest.var_3
D=A
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// push static 1
@StaticTest.var_1
D=A
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// sub
@0
M=M-1
@0
A=M
D=M
@0
M=M-1
@0
A=M
D=M-D
@0
A=M
M=D
@0
M=M+1

// push static 8
@StaticTest.var_8
D=A
@0
A=A+D
D=M
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
