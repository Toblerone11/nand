import constants.CommandType;

import javax.print.DocFlavor;
import java.io.IOException;

import static constants.CommandType.*;
import static constants.Constants.*;

/**
 * Created by Ron on 18/04/2016.
 */
public class CodeWriter {

    /* constants */
    private static final int STACK_BEGIN = 256;
    private static final int PTR_TO_PTR = 0, PTR_TO_VAL = 1;
    private static final String SP_ADDR = "0", LCL_ADDR = "1", ARG_ADDR = "2", THIS_ADDR = "3", THAT_ADDR = "4", TEMP_ADDR = "5", TRISPC = "13", BASE_INDEX = "0", PTR_ADDR = "3", RAM_ADDR = "0";
    private static final String VM_MAIN_FUNC = "Sys.init";
    private static final String D_STORE_M = "D=M";
    private static final String D_STORE_A = "D=A";
    private static final String M_STORE_D = "M=D";
    private static final String GOTO = "0;JMP", IF_GOTO = "D;JNE";

    //RAM variables
    private static final String FRAME = "frame", ADDRESS_TO_POP = "popAddr", RET_VAL = "returnValue", RET_SP_ADDR = "spRetAddr";

    // arithmentic commands
    private static final String ADD = "add", SUB = "sub", NEG = "neg", EQ = "eq", GT = "gt", LT = "lt", AND = "and", OR = "or", NOT = "not";


    // templates
    private static final String LABEL_TEMPLATE = "(%s)";

    /* data members */
    private FileOrgnizer asmFile;
    private String currentVmFileName;
    private int conditionCounter;
    private int continueCounter;
    private int returnCounter;

    /* Ctors */
    public CodeWriter(String name) throws IOException {
        asmFile = new FileOrgnizer(name);
        conditionCounter = 0;
        continueCounter = 0;
        returnCounter = 0;
        currentVmFileName = "";
    }

    /* METHODS */

    private String generateConditionLabel() {
        return String.format("Condition_%d", ++conditionCounter);
    }

    private String generateContinueLabel() {
        return String.format("Continue_%d", ++continueCounter);
    }

    private String generateReturnLabel() {
        return String.format("Return_%d", ++returnCounter);
    }

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

    private void writeSetDFromConst(String constVal) {
        writeSetAReg(constVal);
        asmFile.addInstruction(D_STORE_A);
    }

    private void writeGetValFromAddress(String destAddr) {
        writeSetDFromAddr();
        writeSetAReg(destAddr);
        asmFile.addInstruction(M_STORE_D);
    }

    private void writeSetMemoryToIndexInSegment(String segment, String index) {
        //TODO encapsulate all the pointers type in this method
        String segAddrPointer = null;
        int pointerType = -1;
        if (segment.equals(ARG)) {
            segAddrPointer = ARG_ADDR;
            pointerType = PTR_TO_PTR;
        } else if (segment.equals(LCL)) {
            segAddrPointer = LCL_ADDR;
            pointerType = PTR_TO_PTR;
        } else if (segment.equals(THIS)) {
            segAddrPointer = THIS_ADDR;
            pointerType = PTR_TO_PTR;
        } else if (segment.equals(THAT)) {
            segAddrPointer = THAT_ADDR;
            pointerType = PTR_TO_PTR;
        } else if (segment.equals(TEMP)) {
            segAddrPointer = TEMP_ADDR;
            pointerType = PTR_TO_VAL;
        } else if (segment.equals(PTR)) {
            segAddrPointer = PTR_ADDR;
            pointerType = PTR_TO_VAL;
        } else if (segment.equals(RAM)) {
            segAddrPointer = RAM_ADDR;
            pointerType = PTR_TO_VAL;
        } else if (segment.equals(STAT)) {
            //TODO create table of all variables and get name from there
            segAddrPointer = String.format("%s.var_%s", currentVmFileName, index);
            pointerType = PTR_TO_VAL;
            index = "0";
        }

        writeSetAReg(segAddrPointer);
        switch (pointerType) {
            case (PTR_TO_PTR):
                asmFile.addInstruction(D_STORE_M);
                break;
            case (PTR_TO_VAL):
                asmFile.addInstruction(D_STORE_A);
        }

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
        if (type == C_POP) {
            writeUpdateBeforePop();
            //store address to pop
            writeSetMemoryToIndexInSegment(segment, index);
            asmFile.addInstruction(D_STORE_A);
            writeSetAReg(ADDRESS_TO_POP);
            asmFile.addInstruction(M_STORE_D);

            //take top element from stack
            writeSetARegToAdressInAddress(SP_ADDR);
            writeSetDFromAddr();

            //store in the relevant address
            writeSetARegToAdressInAddress(ADDRESS_TO_POP);
            asmFile.addInstruction(M_STORE_D);

        } else if (type == C_PUSH) {
            if (segment.equals(CONSTANT)) {
                writeSetDFromConst(index);
            } else {
                writeSetMemoryToIndexInSegment(segment, index);
                writeSetDFromAddr();
            }
            writeSetARegToAdressInAddress(SP_ADDR);
            asmFile.addInstruction(M_STORE_D);
            writeUpdateAfterPush();
        }
    }

    /**
     * writes assembly code to perform arithmetic commands.
     *
     * @param command possible commands: 'add', 'sub', 'neg', 'eq', 'gt', 'lt', 'and', 'or', 'not'
     * @return the result of the calculation
     */
    public void writeArithmaticCommand(String command) {
        // popping first argument from stack to register D
        writeUpdateBeforePop();
        writeSetARegToAdressInAddress(SP_ADDR);
        writeSetDFromAddr();

        if (command.equals(NEG)) {
            asmFile.addInstruction("D=-D");
        } else if (command.equals(NOT)) {
            asmFile.addInstruction(("D=!D"));
        } else {
            // store the popped value in the mentioned memory address
//            writeSetAReg(ARITHMETIC_ARG_1);
//            asmFile.addInstruction(M_STORE_D);

            // pointing to second argument in the top of the stack
            writeUpdateBeforePop();
            writeSetARegToAdressInAddress(SP_ADDR);
            if (command.equals(ADD)) {
                asmFile.addInstruction(("D=D+M"));
            } else if (command.equals(SUB)) {
                asmFile.addInstruction(("D=M-D"));
            } else if (command.equals(AND)) {
                asmFile.addInstruction(("D=D&M"));
            } else if (command.equals(OR)) {
                asmFile.addInstruction(("D=D|M"));
            } else {
                asmFile.addInstruction(("D=M-D"));
                String currentConditionLabel = generateConditionLabel();
                String currentContinueLabel = generateContinueLabel();

                //write jump if true value
                writeSetAReg(currentConditionLabel);
                if (command.equals(EQ)) {
                    asmFile.addInstruction("D;JEQ");
                } else if (command.equals(GT)) {
                    asmFile.addInstruction("D;JGT");
                } else if (command.equals(LT)) {
                    asmFile.addInstruction("D;JLT");
                } else {
                    asmFile.addInstruction(String.format("!!!!!!!! not arithmatic command: %s !!!!!!!!!!", command));
                }

                //write default (if not true)
                asmFile.addInstruction("D=0");

                // skip true code segment
                writeSetAReg(currentContinueLabel);
                asmFile.addInstruction("0;JMP");

                // write true code segment
                writeLabel(currentConditionLabel);
                asmFile.addInstruction("D=-1");

                //declare continue label address
                writeLabel(currentContinueLabel);

            } // condition command
        } // two args command

        //push result to the stack manually
        writeSetARegToAdressInAddress(SP_ADDR);
        asmFile.addInstruction("M=D");
        writeUpdateAfterPush();
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
        writeUpdateBeforePop();
        asmFile.addInstruction("A=M");
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
        // perform manual push
        String currentReturnLabel = generateReturnLabel();
        writeSetAReg(currentReturnLabel);
        asmFile.addInstruction(D_STORE_A);
        writeSetARegToAdressInAddress(SP_ADDR);
        asmFile.addInstruction(M_STORE_D);
        writeUpdateAfterPush();

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

        // declare label for return address
        writeLabel(currentReturnLabel);
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

        // store the current ARG base at RET_SP_ADDR variable
        writeSetAReg(ARG_ADDR);
        asmFile.addInstruction("D=M+1");
        writeSetAReg(RET_SP_ADDR);
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

        // set the top of the stack at arg[0]
        writeSetAReg(RET_SP_ADDR);
        writeGetValFromAddress(SP_ADDR);

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
//        writeSetDFromConst("2700");
//        writeSetAReg(ARG_ADDR);
//        asmFile.addInstruction(M_STORE_D);
//
//        writeSetDFromConst("2800");
//        writeSetAReg(LCL_ADDR);
//        asmFile.addInstruction(M_STORE_D);
//
//        writeSetDFromConst("3000");
//        writeSetAReg(THIS_ADDR);
//        asmFile.addInstruction(M_STORE_D);
//
//        writeSetDFromConst("4000");
//        writeSetAReg(THAT_ADDR);
//        asmFile.addInstruction(M_STORE_D);

//        writeSetDFromConst(String.format("%s", STACK_BEGIN));
//        writeSetAReg(SP_ADDR);
//        asmFile.addInstruction(M_STORE_D);

//         invoking the main.
//        writeCall(VM_MAIN_FUNC, 0);
//        writeGoto(VM_MAIN_FUNC);

//         go to infinite loop
//        writeGoto("END");
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

    public void setCurrentVmFileName(String currentVmFileName) {
        this.currentVmFileName = currentVmFileName;
    }
}
