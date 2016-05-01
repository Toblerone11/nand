import constants.CommandType;

import java.io.IOException;

import static constants.CommandType.*;
import static constants.Constants.*;

/**
 * Created by Ron on 18/04/2016.
 */
public class CodeWriter {

    /* constants */
    private static final int STACK_BEGIN = 256;
    private static final String SP_ADDR = "0", LCL_ADDR = "1", ARG_ADDR = "2", THIS_ADDR = "3", THAT_ADDR = "4", TEMP_ADDR = "5", TRISPC = "13", BASE_INDEX = "0";
    private static final String VM_MAIN_FUNC = "Sys.init";
    private static final String D_STORE_M = "D=M";
    private static final String D_STORE_A = "D=A";
    private static final String M_STORE_D = "M=D";
    private static final String GOTO = "0;JMP", IF_GOTO = "D;JMP";

    //RAM variables
    private static final String FRAME = "frame", ADDRESS_TO_POP = "popAddr", RET_VAL = "returnValue";


    // templates
    private static final String LABEL_TEMPLATE = "(%s)";

    /* data members */
    private FileOrgnizer asmFile; //TODO set name from system arguments.

    /* Ctors */
    public CodeWriter(String name) throws IOException {
        asmFile = new FileOrgnizer(name);
    }

    /* METHODS */

    private void writeSetAReg(String address) {
        asmFile.addInstruction(String.format("@%s", address));
    }


    private void writeSetARegToAdressInAddress(String firstAddr) {
        asmFile.addInstruction(String.format("@%s", firstAddr));
        asmFile.addInstruction("A=M");
    }


    private void writeSetDFromAddr() {
        asmFile.addInstruction(D_STORE_M);
    }

    private void writeSetDFromAddr(String sourceAddr) {
        writeSetAReg(sourceAddr);
        writeSetDFromAddr();
    }
    
    private void writeSetDFromConst(String constVal) {
        writeSetAReg(constVal);
        asmFile.addInstruction(D_STORE_A);
    }

    private void writeGetValFromAddress(String destAddr) {
        writeSetDFromAddr();
        writeSetAReg(destAddr);
        asmFile.addInstruction(M_STORE_D);
    }


    private void writeGetValFromConst(String destAddr, String constVal) {
        writeSetDFromConst(constVal);
        writeSetAReg(destAddr);
        asmFile.addInstruction(M_STORE_D);
    }

    private void writeSetMemoryToIndexInSegment(String segAddrPointer, String index) {
        asmFile.addInstruction(String.format("@%s", segAddrPointer));
        asmFile.addInstruction(D_STORE_M);
        asmFile.addInstruction(String.format("@%s", index));
        asmFile.addInstruction("A=A+D");
    }

    private void writeUpdateAfterPush() {
        writeSetAReg(SP_ADDR);
        asmFile.addInstruction("M=M+1");
    }

    private void writeUpdateBeforePop() {
        writeSetAReg(SP_ADDR);
        asmFile.addInstruction("M=M-1");
    }

    /**
     * writing assembly code to push or pop to the global stack
     *
     * @param type    push or pop command
     * @param segment the memory segment from which to push or where to pop.
     * @param index   the specific index in the memory segment
     * @return
     */
    public void writePushPopCommand(CommandType type, String segment, String index) {
        //TODO add more types of memory segments
        String segAddrPointer = null;
        if (segment.equals(ARG)) {
            segAddrPointer = ARG_ADDR;
        } else if (segment.equals(LCL)) {
            segAddrPointer = LCL_ADDR;
        } else if (segment.equals(THIS)) {
            segAddrPointer = THIS_ADDR;
        } else if (segment.equals(THAT)) {
            segAddrPointer = THAT_ADDR;
        } else if (segment.equals(TEMP)) {
            segAddrPointer = TEMP_ADDR;
        }

        if (type == C_POP) {
            writeUpdateBeforePop();
            if (segment.equals(RAM)) {
                writeSetARegToAdressInAddress(SP_ADDR);
                writeSetDFromAddr();
                writeSetAReg(index);
                asmFile.addInstruction(M_STORE_D);
            } else {
                //store address to pop
                writeSetMemoryToIndexInSegment(segAddrPointer, index);
                asmFile.addInstruction(D_STORE_A);
                writeSetAReg(ADDRESS_TO_POP);
                asmFile.addInstruction(M_STORE_D);

                //take top element from stack
                writeSetARegToAdressInAddress(SP_ADDR);
                writeSetDFromAddr();

                //store in the relevant address
                writeSetARegToAdressInAddress(ADDRESS_TO_POP);
                asmFile.addInstruction(M_STORE_D);
            }
        } else if (type == C_PUSH) {
            if (segment.equals(CONSTANT)) {
                writeSetDFromConst(index);
            } else {
                if (segment.equals(RAM)) {
                    writeSetAReg(index);
                } else {
                    writeSetMemoryToIndexInSegment(segAddrPointer, index);
                }
                writeSetDFromAddr();
            }
            writeSetARegToAdressInAddress(SP_ADDR);
            asmFile.addInstruction(M_STORE_D);
            writeUpdateAfterPush();
        }
    }

    public String writeArithmaticCommand(String command) {
        return null;
    }

    /**
     * writes assembly code that declares new label named as the given 'labelName' argument.
     *
     * @param labelName the name to give to the new declared label
     * @return assembly code that introduce new label.
     */
    public void writeLabel(String labelName) {
        asmFile.addLabel(String.format(LABEL_TEMPLATE, labelName));
    }

    /**
     * writes assemblt that handles simple goto - just jumping to some ROM address.
     *
     * @param label the label to refer when writin the 'goto' code segment.
     * @return assembly code segment which implement goto some label in any case.
     */
    public void writeGoto(String label) {
        writeSetAReg(label);
        asmFile.addInstruction(GOTO); // one line instruction
    }

    /**
     * the same as goto but under some condition.
     *
     * @param label the label to jump to in case the condition is true.
     * @return assembly code segment which implement goto some label if some condition occurred.
     */
    public void writeIf(String label) {
        // set A reg to sp - 1, the value to check, and dtore the value in D.
        writeSetAReg(SP_ADDR);
        writeUpdateBeforePop();
        asmFile.addInstruction(D_STORE_M);

        // prepare to jump and check the value
        writeSetAReg(label);
        asmFile.addInstruction(IF_GOTO);
    }

    /**
     * writes assembly that handles call to some function. the handling includes preparing the global stack to the call.
     *
     * @param funcName the name of the called function.
     * @param numArgs  the number of arguments this function need.
     * @return assembly code segment implementing the call to some function
     */
    public void writeCall(String funcName, int numArgs) {
        /*
        * thr preparations: all arguments has already been pushed.
        * push the return address - the address of the next instruction after the current call.
        * push the addresses of LCL, ARG, THIS, THAT of the current working function.
        * assign the 'arg' and 'lcl' data members to the addresses of the current LCL and ARG bases in the stack.
        */
        int linesToSkip = (7 * 5) + 13 + 1; // (num of lines in PUSH operation * num of operations) + goto + next line than that.
        int returnAddress = asmFile.getCurrentLine() + linesToSkip;
        writePushPopCommand(C_PUSH, CONSTANT, String.format("%s", returnAddress));
        writePushPopCommand(C_PUSH, RAM, LCL_ADDR);
        writePushPopCommand(C_PUSH, RAM, ARG_ADDR);
        writePushPopCommand(C_PUSH, RAM, THIS_ADDR);
        writePushPopCommand(C_PUSH, RAM, THAT_ADDR);

        // update the current arg address
        writeSetDFromConst(String.format("%s", numArgs + 5));
        writeSetAReg(SP_ADDR);
        asmFile.addInstruction("A=M-D");
        asmFile.addInstruction(D_STORE_A);
        writeSetAReg(ARG_ADDR);
        asmFile.addInstruction(M_STORE_D);

        // update the current lcl address
        writeSetAReg(SP_ADDR);
        asmFile.addInstruction(D_STORE_M);
        writeSetAReg(LCL_ADDR);
        asmFile.addInstruction(M_STORE_D);

        // goto the function code segment
        writeGoto(funcName);
    }

    /**
     * writes assembly which handles return instruction. restore the caller state with the return value.     *
     *
     * @return assembly code segment implementing return from some function
     */
    public void writeReturn() {
        /*
         * store the current LCL value.
         * store the return address previously stored.
         * set the return value to the address of the
         * set the
         * then assigning back the previously stored LCL, ARG, THIS and THAT addresses to be the current addresses.
         */
        // store the current LCL base at FRAME variable
        writeSetAReg(LCL_ADDR);
        asmFile.addInstruction(D_STORE_M);
        writeSetAReg(FRAME);
        asmFile.addInstruction(M_STORE_D);

        // store the return address at RET_VAL variable
        writeSetDFromConst(String.format("%s", 5));
        writeSetAReg(LCL_ADDR);
        asmFile.addInstruction("A=M-D"); // set D to store the address of the relevant place in stack
        asmFile.addInstruction(D_STORE_M);
        writeSetAReg(RET_VAL);
        asmFile.addInstruction(M_STORE_D);

        // store the return value assuming it is on the top of the stack, into the arg[0] location on the stack.
        writePushPopCommand(C_POP, ARG, BASE_INDEX);

        // set the top of the stack at lcl[0]
        writeSetAReg(FRAME);
        writeGetValFromAddress(SP_ADDR);

        // restore the state of the caller
        writePushPopCommand(C_POP, RAM, THAT_ADDR);
        writePushPopCommand(C_POP, RAM, THIS_ADDR);
        writePushPopCommand(C_POP, RAM, ARG_ADDR);
        writePushPopCommand(C_POP, RAM, LCL_ADDR);

        // put the top of the stack at
        //TODO change the address of SP

        // jump to the return address
        writeSetARegToAdressInAddress(RET_VAL);
        asmFile.addInstruction(GOTO);
    }

    /**
     * writes assembly that implement the function procedure. it is defining the function and assign to it the given function's name.
     *
     * @param funcName  the name of the function to declare
     * @param numLocals the number of local variables to push to the stack.
     * @return assembly code segment implementing initialization of function definition.
     */
    public void writeFunction(String funcName, int numLocals) {
        // assigning label for that function
        writeLabel(funcName);

        // push all local variables and set them to 0.
        for (int i = 0; i < numLocals; i++) {
            writePushPopCommand(C_PUSH, CONSTANT, "0");
        }
    }

    /**
     * writes code segment in assembly for initializing the program run. setting initial addresses and invoke the main function.
     *
     * @return program initializer code segment in assembly.
     */
    public void writeInit() {
        // setting the sp.
        writeSetDFromConst("2700");
        writeSetAReg(ARG_ADDR);
        asmFile.addInstruction(M_STORE_D);

        writeSetDFromConst("2800");
        writeSetAReg(LCL_ADDR);
        asmFile.addInstruction(M_STORE_D);

        writeSetDFromConst("2900");
        writeSetAReg(THIS_ADDR);
        asmFile.addInstruction(M_STORE_D);

        writeSetDFromConst("3000");
        writeSetAReg(THAT_ADDR);
        asmFile.addInstruction(M_STORE_D);

        writeSetDFromConst("3100");
        writeSetAReg(TEMP_ADDR);
        asmFile.addInstruction(M_STORE_D);

        writeSetDFromConst(String.format("%s", STACK_BEGIN));
        writeSetAReg(SP_ADDR);
        asmFile.addInstruction(M_STORE_D);

        // invoking the main.
        writeGoto(VM_MAIN_FUNC);

        // go to infinite loop
        writeGoto("END");
    }

    public void finish() {
        // finish with infinite loop
        writeLabel("END");
        writeGoto("END");
        asmFile.close();
    }

    public void writeComment(String comment) {
        asmFile.addComment(comment);
    }

    public void writeBlankLine() {
        asmFile.addBlankLine();
    }

}
