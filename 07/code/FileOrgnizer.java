import java.io.*;

/**
 * Created by Ron on 24/04/2016.
 */
public class FileOrgnizer {


    /* data members */
    int currentLine;
    PrintWriter pw;

    /* Ctor */
    public FileOrgnizer(String name) throws IOException {
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

    public void addInstruction(String instruction) {
        pw.println(instruction);
        currentLine++;
    }

    public void addLabel(String label) {
        pw.println(label);
    }

    public void addComment(String comment) {
        pw.println("// " + comment);
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
}
