import tokenization.JackTokenizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

/**
 * Jack Analyzer main.
 */
public class Main {



    /* JackConstants */
    private static final String JACK_FILETYPE = ".jack", XML_FILETYPE = ".xml";
    private static final int JACK_TYPE_LEN = 5;


    private static boolean isJackFile(String filePath) {
        return ((filePath.substring(filePath.length() - JACK_TYPE_LEN)).equals(JACK_FILETYPE));
    }

    public static void main(String[] args) throws IOException, SyntaxException {
        // currently parsed only for linux.
        String sysSep = FileSystems.getDefault().getSeparator(); // system separator.
        File[] files;
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
        String parDir;
        File inputFile = new File(inFile);
        if (inputFile.isDirectory()) {
            if (!inFile.substring(inFile.length() - 1).equals(sysSep)) {
                inFile += sysSep;
            }
            files = inputFile.listFiles();
            parDir = inFile;
        } else {
            files = new File[] {inputFile};
            parDir = inputFile.getParent();
        }

        parDir = parDir + sysSep;
        assert (files != null);
        for (File jackFile : files) {
            if (!isJackFile(jackFile.getPath()))
                continue;

            // prepare file for writing
            String sourceFile = jackFile.getName();
            String xmlFile = parDir + sourceFile.substring(0, (sourceFile.length() - JACK_FILETYPE.length())) + XML_FILETYPE;

            JackTokenizer tokenizer = new JackTokenizer(jackFile);
            CompilationEngine compiler = new CompilationEngine(tokenizer, xmlFile);
            if (tokenizer.hasMoreTokens()) {
                tokenizer.advance();
                try {
                    compiler.compileClass();
                } catch (SyntaxException e) {
                    throw e;
                } catch (NullPointerException e) {
                    System.out.println(tokenizer.currentLineNum);
                    throw e;
                } catch (Exception e) {
                    throw e;
                } finally {
                    compiler.finish();
                }
            }
        }
    }
}
