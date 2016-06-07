import java.io.*;

/**
 * Created by Ron on 24/04/2016.
 */
public class FileOrganizer {

    /* constants */
    private static final String TAB = "  ";

    /* data members */
    int currentLine;
    PrintWriter pw;
    int numOfScopes;

    /* Ctor */
    public FileOrganizer(String name) throws IOException {
        // create new file
        File f = new File(name);
        if (f.exists()) {
            if (!f.delete()) {
                throw new FileNotFoundException("cannot delete the file: " + f.getAbsolutePath());
            }
        }
        if (!f.createNewFile()) {
            throw new IOException("can't create the file: " + f.getAbsolutePath());
        }

        pw = new PrintWriter(new FileWriter(f));
        currentLine = -1;
        numOfScopes = 0;
    }

    public void beginScope(String scopeTitle) {
        pw.println(String.format("%s<" + scopeTitle + ">", repeatString(numOfScopes, TAB)));
        currentLine++;
        numOfScopes++;
    }

    public void endScope(String scopeTitle) {
        numOfScopes--;
        pw.println(String.format("%s</" + scopeTitle + ">", repeatString(numOfScopes, TAB)));
        currentLine++;
    }

    public void addTerminal(String terminalType, String terminalVal) {
        pw.println(String.format("%s<%s> %s </%s>", repeatString(numOfScopes, TAB), terminalType, terminalVal, terminalType));
    }

    public void addTerminal(String terminalType, char terminalVal) {
        pw.println(String.format("%s<%s> %c </%s>", repeatString(numOfScopes, TAB), terminalType, terminalVal, terminalType));
    }

    public void addTerminal(String terminalType, int integerVal) {
        pw.println(String.format("%s<%s> %d </%s>", repeatString(numOfScopes, TAB), terminalType, integerVal, terminalType));
    }


    public void addBlankLine() {
        pw.println();
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public void close() {
        pw.close();
    }

    private String repeatString(int times, String str) {
        return new String(new char[times]).replace("\0", str);
    }
}
