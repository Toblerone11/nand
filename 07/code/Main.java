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
        String outDir = args[0];
		if (!outDir.substring(outDir.length() - 1).equals(FileSystems.getDefault().getSeparator())) {
                outDir += FileSystems.getDefault().getSeparator();
            }
        //System.out.println(outDir);
        File inputFile = new File(outDir);
        String asmFile;
        if (inputFile.isDirectory()) {
            files = inputFile.listFiles();
            asmFile = inputFile.getName() + ASM_FILETYPE;;
        } else {
            files = new File[] {inputFile};
            outDir = inputFile.getParent();
            asmFile = inputFile.getName();
            asmFile = asmFile.substring(0, (asmFile.length() - VM_FILETYPE.length())) + ASM_FILETYPE;
			
			//System.out.println(outDir);
        }

        // prepare file to write.
        String outputPath = outDir + FileSystems.getDefault().getSeparator() + asmFile;
        CodeWriter cw = new CodeWriter(outputPath);
        cw.writeInit();

        for (File vmFile : files) {
            if (!isVmFile(vmFile.getPath()))
                continue;

			//System.out.println(vmFile.getPath());
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
