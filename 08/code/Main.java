import java.io.*;
import java.nio.file.FileSystems;

import static constants.CommandType.*;

public class Main {

    /* constants */
    private static final String VM_FILETYPE = ".vm", ASM_FILETYPE = ".asm";


    private static boolean isVmFile(String filePath) {
        return ((filePath.substring(filePath.length() - 3)).equals(VM_FILETYPE));
    }


    public static void main(String[] args) throws Exception {
        File[] files;
        String sysSep = FileSystems.getDefault().getSeparator(); // system separator.
        String parDir;
        String asmFile;

        String inFile = args[0];
        if (inFile.length() == 1) {
            if (inFile.equals(".")) {
                inFile += sysSep;
            } else {
                inFile = "." + sysSep + inFile;
            }
        }
        else if (inFile.length() == 2) {
            if (inFile.equals("..")) {
                inFile += sysSep;
            } else if (!inFile.equals("." + sysSep)) {
                inFile = "." + sysSep + inFile;
            }
        }
        else if ((inFile.charAt(0) != sysSep.charAt(0)) && !(inFile.substring(0,2).equals("." + sysSep)) && !(inFile.substring(0,3).equals(".." + sysSep))) {
            inFile = "." + sysSep + inFile;
        }

        File inputFile = new File(inFile);
        if (inputFile.isDirectory()) {
            files = inputFile.listFiles();
            parDir = args[0];
            if (!parDir.substring(parDir.length() - 1).equals(FileSystems.getDefault().getSeparator())) {
                parDir += FileSystems.getDefault().getSeparator();
            }
            asmFile = inputFile.getName() + ASM_FILETYPE;
        } else {
            files = new File[] {inputFile};
            parDir = inputFile.getParent();
            asmFile = inputFile.getName();
            asmFile = asmFile.substring(0, (asmFile.length() - VM_FILETYPE.length())) + ASM_FILETYPE;
        }



        // prepare file to write.
        String outputPath = parDir + sysSep + asmFile;
        CodeWriter cw = new CodeWriter(outputPath);
        cw.writeInit();

        assert (files != null);
        for (File vmFile : files) {
            if (!isVmFile(vmFile.getPath()))
                continue;

            String funcName = vmFile.getName().substring(0, vmFile.getName().length() - VM_FILETYPE.length());
            cw.setCurrentVmFileName(funcName);

            // reading from vm file and translating
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
                        cw.writeGoto(String.format("%s$%s", funcName, parser.getArg1()));
                        break;
                    case C_IF:
                        cw.writeIf(String.format("%s$%s", funcName, parser.getArg1()));
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
        }
        cw.finish();
    }
}
