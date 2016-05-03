
// push constant 0
@0
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

// label LOOP_START
(BasicLoop$LOOP_START)

// push argument 0
@2
D=M
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

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

// push argument 0
@2
D=M
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// push constant 1
@1
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

// pop argument 0     
@0
M=M-1
@2
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

// push argument 0
@2
D=M
@0
A=A+D
D=M
@0
A=M
M=D
@0
M=M+1

// if-goto LOOP_START 
@0
M=M-1
A=M
D=M
@BasicLoop$LOOP_START
D;JNE

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
(END)
@END
0;JMP
