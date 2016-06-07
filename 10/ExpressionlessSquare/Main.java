import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

/**
 * Jack Analyzer main.
 */
public class Main {



    /* constants */
    private static final String JACK_FILETYPE = ".jack", VM_FILETYPE = ".vm";
    private static final int JACK_TYPE_LEN = 5;


    private static boolean isJackFile(String filePath) {
        return ((filePath.substring(filePath.length() - JACK_TYPE_LEN)).equals(JACK_FILETYPE));
    }

    public static void main(String[] args) throws IOException, SyntaxException {
        File[] files;
        String inFile = args[0];
        String parDir;
        System.out.println(inFile);
        File inputFile = new File(inFile);
        if (inputFile.isDirectory()) {
            if (!inFile.substring(inFile.length() - 1).equals(FileSystems.getDefault().getSeparator())) {
                inFile += FileSystems.getDefault().getSeparator();
            }
            files = inputFile.listFiles();
            parDir = inFile;
        } else {
            files = new File[] {inputFile};
            parDir = inputFile.getParent();
            //System.out.println(outDir);
        }

        parDir = parDir + FileSystems.getDefault().getSeparator();
        assert files != null;
        for (File jackFile : files) {
            if (!isJackFile(jackFile.getPath()))
                continue;

            // prepare file for writing
            String sourceFile = jackFile.getName();
            String vmFile = parDir + sourceFile.substring(0, (sourceFile.length() - JACK_FILETYPE.length())) + VM_FILETYPE;
            System.out.println(vmFile);

            JackTokenizer tokenizer = new JackTokenizer(jackFile);
            CompilationEngine compiler = new CompilationEngine(tokenizer, vmFile);
            if (tokenizer.hasMoreTokens()) {
                tokenizer.advance();
                try {
                    compiler.compileClass();
                } catch (SyntaxException e) {
                    compiler.finish();
                    throw e;
                }
            }
        }
    }
}
