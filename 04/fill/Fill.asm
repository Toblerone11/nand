// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

//setting pixel variable to the top and bottom of the screen
@SCREEN //top
D=M
@pixel // the variable being iterate over the screen pixels.
M=0
@8192 //bottom (SCREEN + 8192(=256 X 32) )
D=A
@bottom
M=D

// check if some keyboard button is being pressed.
(LOOP)
@KBD
D=M

// if some key is pressed -> fill screen with black and stay black.
@Fill
D;JGT

// else clean screen back to white and keep it white.
@Clear
0;JMP

(Fill)
// check if the bottom of the screen was already reached.
@pixel
D=M
@bottom
D=D-M // D = pixel - bottom
@LOOP
D;JGE // if D >= 0,  the pixel reached the bottom location

// pixel still in the screen frame, color in black and increment for next iteration
@pixel
D=M
@SCREEN
D=D+A
A=D
M=-1

// increment pixel position by 1.
@pixel
M=M+1

// return to keyboard check
@LOOP
0;JMP

(Clear)
// check if the top of the screen was already reached.
@pixel
D=M
@LOOP
D;JLT // if pixel == 0, top was reached.

// pixel still in the screen frame, color in white and decrement for next iteration.
@pixel
D=M
@SCREEN
D=D+A
A=D
M=0

// decrement pixel position by 1.
@pixel
M=M-1

// return to keyboard check
@LOOP
0;JMP
