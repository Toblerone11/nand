
// push constant 17
@17
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 17
@17
D=A
@0
A=M
M=D
@0
M=M+1

// eq
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
D;JEQ
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

// push constant 17
@17
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 16
@16
D=A
@0
A=M
M=D
@0
M=M+1

// eq
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
@Condition_2
D;JEQ
D=0
@Continue_2
0;JMP
(Condition_2)
D=-1
(Continue_2)
@0
A=M
M=D
@0
M=M+1

// push constant 16
@16
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 17
@17
D=A
@0
A=M
M=D
@0
M=M+1

// eq
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
@Condition_3
D;JEQ
D=0
@Continue_3
0;JMP
(Condition_3)
D=-1
(Continue_3)
@0
A=M
M=D
@0
M=M+1

// push constant 892
@892
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 891
@891
D=A
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
@Condition_4
D;JLT
D=0
@Continue_4
0;JMP
(Condition_4)
D=-1
(Continue_4)
@0
A=M
M=D
@0
M=M+1

// push constant 891
@891
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 892
@892
D=A
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
@Condition_5
D;JLT
D=0
@Continue_5
0;JMP
(Condition_5)
D=-1
(Continue_5)
@0
A=M
M=D
@0
M=M+1

// push constant 891
@891
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 891
@891
D=A
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
@Condition_6
D;JLT
D=0
@Continue_6
0;JMP
(Condition_6)
D=-1
(Continue_6)
@0
A=M
M=D
@0
M=M+1

// push constant 32767
@32767
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 32766
@32766
D=A
@0
A=M
M=D
@0
M=M+1

// gt
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
@Condition_7
D;JGT
D=0
@Continue_7
0;JMP
(Condition_7)
D=-1
(Continue_7)
@0
A=M
M=D
@0
M=M+1

// push constant 32766
@32766
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 32767
@32767
D=A
@0
A=M
M=D
@0
M=M+1

// gt
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
@Condition_8
D;JGT
D=0
@Continue_8
0;JMP
(Condition_8)
D=-1
(Continue_8)
@0
A=M
M=D
@0
M=M+1

// push constant 32766
@32766
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 32766
@32766
D=A
@0
A=M
M=D
@0
M=M+1

// gt
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
@Condition_9
D;JGT
D=0
@Continue_9
0;JMP
(Condition_9)
D=-1
(Continue_9)
@0
A=M
M=D
@0
M=M+1

// push constant 57
@57
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 31
@31
D=A
@0
A=M
M=D
@0
M=M+1

// push constant 53
@53
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

// push constant 112
@112
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

// neg
@0
M=M-1
@0
A=M
D=M
D=-D
@0
A=M
M=D
@0
M=M+1

// and
@0
M=M-1
@0
A=M
D=M
@0
M=M-1
@0
A=M
D=D&M
@0
A=M
M=D
@0
M=M+1

// push constant 82
@82
D=A
@0
A=M
M=D
@0
M=M+1

// or
@0
M=M-1
@0
A=M
D=M
@0
M=M-1
@0
A=M
D=D|M
@0
A=M
M=D
@0
M=M+1

// not
@0
M=M-1
@0
A=M
D=M
D=!D
@0
A=M
M=D
@0
M=M+1
(END)
@END
0;JMP
