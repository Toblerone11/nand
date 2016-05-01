import java.io.*;
import java.nio.file.FileSystems;

import static constants.CommandType.*;

public class Main {

    /* constants */
    private static final String VM_FILETYPE = ".vm", ASM_FILETYPE = ".asm", ASM_FILENAME = "vmTranslated";


    private static boolean isVmFile(String filePath) {
        if ((filePath.substring(filePath.length() - 3)).equals(VM_FILETYPE)) {
            return true;
        }
        return false;
    }


    public static void main(String[] args) throws Exception {
        File[] files;
        String outDir;
        File inputFile = new File(args[0]);
        if (inputFile.isDirectory()) {
            files = inputFile.listFiles();
            outDir = args[0];
        } else {
            files = new File[] {inputFile};
            outDir = inputFile.getParent();
        }

        // prepare file to write.
        String outputPath = outDir + FileSystems.getDefault().getSeparator() + ASM_FILENAME + ASM_FILETYPE;
        CodeWriter cw = new CodeWriter(outputPath);
        cw.writeInit();

        for (File vmFile : files) {
            if (!isVmFile(vmFile.getPath()))
                continue;

            // reading from vm file and translating
            String funcName = vmFile.getName().substring(0, vmFile.getName().length() - VM_FILETYPE.length());
            Parser parser = new Parser(vmFile);
            while (parser.hasMoreLines()) {
                parser.advance();
                cw.writeBlankLine();
                cw.writeComment(parser.getCurrentLine());

                switch(parser.getType()) {
                    case C_ARITHMETIC:
                        cw.writeArithmaticCommand(parser.getArg1());
                        break;
                    case C_PUSH:
                        cw.writePushPopCommand(C_PUSH, parser.getArg1(), parser.getArg2());
                        break;
                    case C_POP:
                        cw.writePushPopCommand(C_POP, parser.getArg1(), parser.getArg2());
                        break;
                    case C_LABEL:
                        cw.writeLabel(String.format("%s$%s", funcName, parser.getArg1()));
                        break;
                    case C_GOTO:
                        cw.writeGoto(parser.getArg1());
                        break;
                    case C_IF:
                        cw.writeIf(parser.getArg1());
                        break;
                    case C_FUNCTION:
                        cw.writeFunction(parser.getArg1(), Integer.parseInt(parser.getArg2()));
                        break;
                    case C_RETURN:
                        cw.writeReturn();
                        break;
                    case C_CALL:
                        cw.writeCall(parser.getArg1(), Integer.parseInt(parser.getArg2()));
                        break;
                }
            }
            cw.finish();
        }
    }
}
