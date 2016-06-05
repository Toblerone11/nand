package vmproduce;

import java.io.IOException;

import static vmproduce.VmConstants.VM_DIV;
import static vmproduce.VmConstants.VM_MUL;

/**
 * Created by Ron on 01/06/2016.
 */
public class VMWriter {

    /* formats of commands */
    private static final String PUSH_FORMAT = "push %s %d", POP_FORMAT = "pop %s %d", LABEL_FORMAT = "label %s",
                                GOTO_FORMAT = "goto %s", IF_FORMAT = "if-goto %s", CALL_FORMAT = "call %s %d",
                                FUNCTION_FORMAT = "function %s %d", RETURN_FORMAT = "return";

    /* OS helper functions */
    private static final String MULTIPLY = "Math.multiply", DIVIDE = "Math.divide";

    /* data members */
    private VmFileOrganizer vmFile;

    public VMWriter(String outFile) throws IOException {
        vmFile = new VmFileOrganizer(outFile);
    }

    public void writePush(String segment, int index) {
        vmFile.addCommand(String.format(PUSH_FORMAT, segment, index));
    }

    public void writePop(String segment, int index) {
        vmFile.addCommand(String.format(POP_FORMAT, segment, index));
    }

    public void writeArithmetic(String command) {
        if (command.equals(VM_MUL)) {
            writeCall(MULTIPLY, 2);
        }
        else if (command.equals(VM_DIV)) {
            writeCall(DIVIDE, 2);
        }
        else {
            vmFile.addCommand(command);
        }
    }

    public void writeLabel(String label) {
        vmFile.addCommand(String.format(LABEL_FORMAT, label));
    }

    public void writeGoTo(String label) {
        vmFile.addCommand(String.format(GOTO_FORMAT, label));
    }

    public void writeIf(String label) {
        vmFile.addCommand(String.format(IF_FORMAT, label));
    }

    public void writeCall(String funcName, int nArgs) {
        vmFile.addCommand(String.format(CALL_FORMAT, funcName, nArgs));
    }

    public void writeFunction(String funcName, int nLocals) {
        vmFile.addCommand(String.format(FUNCTION_FORMAT, funcName, nLocals));
    }

    public void writeReturn() {
        vmFile.addCommand(RETURN_FORMAT);
    }

    public void close() {
        vmFile.close();
    }
}
