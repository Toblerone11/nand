
// push constant 10
@10
D=A
@0
A=M
M=D
@0
M=M+1

// pop local 0
@0
M=M-1
@1
D=M
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

// push constant 21
@21
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 22
@22
D=A
@0
A=M
M=D
@0
M=M+1

// pop argument 2
@0
M=M-1
@2
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

// pop argument 1
@0
M=M-1
@2
D=M
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

// push constant 36
@36
D=A
@0
A=M
M=D
@0
M=M+1

// pop this 6
@0
M=M-1
@3
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

// push constant 42
@42
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 45
@45
D=A
@0
A=M
M=D
@0
M=M+1

// pop that 5
@0
M=M-1
@4
D=M
@5
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

// pop that 2
@0
M=M-1
@4
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

// push constant 510
@510
D=A
@0
A=M
M=D
@0
M=M+1

// pop temp 6
@0
M=M-1
@5
D=A
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

// push local 0
@1
D=M
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// push that 5
@4
D=M
@5
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

// push argument 1
@2
D=M
@1
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

// push this 6
@3
D=M
@6
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// push this 6
@3
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

// push temp 6
@5
D=A
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
