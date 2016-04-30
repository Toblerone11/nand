import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

/**
 *  this is the manager of all the translation process.
 *  this class knows all the other classes and transferring data
 *  between them in order to perform the full translation.
 */
public class Assembler {

    private static final String ASM_FILETYPE = ".asm", HACK_FILETYPE = ".hack";

    private static final String A_COMMAND = "A_COMMAND";
    private static final String L_COMMAND = "L_COMMAND";
    private static final String C_COMMAND = "C_COMMAND";
    private static final String PREFIX_BINARY = "1";

    // Execute the first step which go through the code and
    // assign the right memory address for every symbol which perform L command.
    private static void execFirstStep(SymbolTable st, String path) throws Exception {
        int address = 0;
        Parser p = new Parser(path);
        while (p.hasMoreLines()) {
            p.advance();
            String commandType = p.getType();
            if (commandType == L_COMMAND) {
                String symbol = p.getSymbol();
                st.addEntry(symbol, address); // check this
                // st.addEntry(symbol, address);
            } else {
                address++;
            }

        }
    }

    // Execute the second step by going through the code and checking only the A commands and C commands
    // The method will write the binary results of the code into a file
    private static void execSecondStep(SymbolTable st, String inPath, String outPath) throws Exception {
        int address = 16;
        Parser p = new Parser(inPath);
//        CodeTranslator ct = new CodeTranslator();
        String binary = "";
        FileWriter fw = new FileWriter(outPath);
        while (p.hasMoreLines()) {
            p.advance();
            String commandType = p.getType();
            if (commandType.equals(A_COMMAND)) {
                String symbol = p.getSymbol();
                if (!isNumeric(symbol)) {
                    if (st.contains(symbol)) {
                        symbol = CodeTranslator.translateDecToBin(st.getAddress(symbol));
                    } else {
                        st.addEntry(symbol, address);
                        symbol = CodeTranslator.translateDecToBin(st.getAddress(symbol));
                        address++;
                    }
                } else
                    symbol = CodeTranslator.translateDecToBin(symbol);
                fw.write(binaryRepresention(symbol) + '\n');
            } else if (commandType.equals(C_COMMAND)) {
                String dest = CodeTranslator.translateDest(p.getDest());
                String comp = CodeTranslator.translateComp(p.getComp());
                String jump = CodeTranslator.translateJump(p.getJump());
                binary = PREFIX_BINARY + comp + dest + jump;
                fw.write(binaryRepresention(binary) + '\n');

            }
        }
        fw.close();
    }

    // Take string with binary number and separate it by 4 bits each group
    private static String binaryRepresention(String binary) {
        return binary;
    }

    // Check if the string is numeric or not
    private static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

	// check if some file is a .asm file
    private static boolean isAsmFile(String filePath) {
        if ((filePath.substring(filePath.length() - 4)).equals(ASM_FILETYPE)) {
            return true;
        }
        return false;

    }
    //main. able to get file or folder and translate all the relevant files in it.
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

        for (File file : files) {
            if (!isAsmFile(file.getPath()))
                continue;
            String outputPath = outDir + FileSystems.getDefault().getSeparator()
								+ file.getName().substring(0, file.getName().length() - 4)
								+ HACK_FILETYPE;
            SymbolTable st = new SymbolTable();
            execFirstStep(st, file.getPath());
            execSecondStep(st, file.getPath(), outputPath);
        }

    }

}
