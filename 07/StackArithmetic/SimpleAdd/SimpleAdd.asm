@256
D=A
@0
M=D
@Return_1
D=A
@0
A=M
M=D
@0
M=M+1
@0
D=A
@1
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1
@0
D=A
@2
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1
@0
D=A
@3
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1
@0
D=A
@4
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1
@5
D=A
@0
A=M-D
D=A
@2
M=D
@0
D=M
@1
M=D
@Sys.init
0;JMP
(Return_1)
@END
0;JMP

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
