
// push constant 200
@200
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 400
@400
D=A
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

// push constant -1000
@1000
D=A
D=-D
@0
A=M
M=D
@0
M=M+1

// lt
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
@Condition_1
D;JLT
D=0
@Continue_1
0;JMP
(Condition_1)
D=-1
(Continue_1)
@0
A=M
M=D
@0
M=M+1
(END)
@END
0;JMP
