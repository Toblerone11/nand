
// push constant 3030
@3030
D=A
@0
A=M
M=D
@0
M=M+1

// pop pointer 0
@0
M=M-1
@3
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

// push constant 3040
@3040
D=A
@0
A=M
M=D
@0
M=M+1

// pop pointer 1
@0
M=M-1
@3
D=A
@1
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

// push constant 32
@32
D=A
@0
A=M
M=D
@0
M=M+1

// pop this 2
@0
M=M-1
@3
D=M
@2
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

// push constant 46
@46
D=A
@0
A=M
M=D
@0
M=M+1

// pop that 6
@0
M=M-1
@4
D=M
@6
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

// push pointer 0
@3
D=A
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// push pointer 1
@3
D=A
@1
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

// push this 2
@3
D=M
@2
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

// push that 6
@4
D=M
@6
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
