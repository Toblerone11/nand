package vmproduce;

import java.io.*;

/**
 * Created by Ron on 24/04/2016.
 */
public class VmFileOrganizer {

    /* JackConstants */
    private static final String TAB = "  ";

    /* data members */
    int currentLine;
    PrintWriter pw;

    /* Ctor */
    public VmFileOrganizer(String name) throws IOException {
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
    }

    public void addCommand(String command) {
        pw.println(command);
        currentLine++;
    }

    public void addComment(String comment) {
        pw.println(String.format("// %s", comment));
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
