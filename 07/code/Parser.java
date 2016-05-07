import constants.CommandType;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static constants.CommandType.*;
import static constants.Constants.*;

/**
 * Created by Ron on 18/04/2016.
 */
public class Parser {

    /* constants */
    //line type recognition
    private static final String COMMENT = "(?://.*)", LABEL = "[A-Za-z:_\\.\\$][\\w:_\\.\\$]+";
    private static final Pattern comment = Pattern.compile(COMMENT);
    private static final Pattern toIgnore = Pattern.compile(String.format("(?:|\\s+|%s)", COMMENT));


    public static final Pattern stackCmd = Pattern.compile("(?<stkcmd>pop|push)\\s+(?<seg>[a-z]++)\\s+(?<index>(-)?\\d++)\\s*");
    public static final Pattern arithmatickCmd = Pattern.compile("(?<op>add|sub|neg|eq|gt|lt|and|or|not)\\s*");
    public static final Pattern labelCmd = Pattern.compile("label\\s+(?<label>" + LABEL + ")\\s*");
    public static final Pattern gotoCmd = Pattern.compile("goto\\s+(?<label>" + LABEL + ")\\s*");
    public static final Pattern ifCmd = Pattern.compile("if-goto\\s+(?<label>" + LABEL + ")\\s*");
    public static final Pattern callCmd = Pattern.compile("call\\s+(?<func>" + LABEL + ")\\s+(?<args>\\d+)\\s*");
    public static final Pattern functionCmd = Pattern.compile("function\\s+(?<func>" + LABEL + ")\\s+(?<locals>\\d+)\\s*");
    public static final Pattern returnCmd = Pattern.compile("return");

    /* data members */
    private BufferedReader br;
    private String currentLine;
    private String nextLine;
    private int lineNum;
    private String arg1;
    private String arg2;
    private CommandType type;

    /* Ctors */

    /**
     * C-tor
     * @param file the file to parse witch vm extension
     * @throws FileNotFoundException
     */
    public Parser(File file) throws FileNotFoundException {
        br = new BufferedReader(new FileReader(file));
        currentLine = "";
        nextLine = "";
        arg1 = null;
        arg2 = null;
        type = null;
        lineNum = 0;
    }
    /* METHODS */

    /**
     *
     * @return
     * @throws IOException
     */
    public boolean hasMoreLines() throws IOException {
        nextLine = br.readLine();
        if (nextLine == null)
            return false;

        while (toIgnore.matcher(nextLine).matches()) {
            nextLine = br.readLine();
            if (nextLine == null)
                return false;
        }
        return true;
    }

    /**
     * this function reads the next instruction in assembly file, parse it, and allowing to check later each
     * component separately.
     */
    public void advance() throws Exception {
        currentLine = nextLine.trim();
        this.lineNum++;
        currentLine = comment.matcher(currentLine).replaceAll("");
        Matcher stkCmdMatch = stackCmd.matcher(this.currentLine);
        if (stkCmdMatch.matches()) {
            if (stkCmdMatch.group("stkcmd").equals(PUSH)) {
                this.type = CommandType.C_PUSH;
            } else if (stkCmdMatch.group("stkcmd").equals(POP)) {
                this.type = C_POP;
            }
            this.arg1 = stkCmdMatch.group("seg");
            this.arg2 = stkCmdMatch.group("index");
            return;
        }
        Matcher arithmenticMatch = arithmatickCmd.matcher(this.currentLine);
        if (arithmenticMatch.matches()) {
            this.arg1 = arithmenticMatch.group("op");
            this.arg2 = null;
            this.type = C_ARITHMETIC;
            return;
        }
        Matcher labelMatch = labelCmd.matcher(this.currentLine);
        if (labelMatch.matches()) {
            this.arg1 = labelMatch.group("label");
            this.arg2 = null;
            this.type = C_LABEL;
            return;
        }
        Matcher gotoMatch = gotoCmd.matcher(this.currentLine);
        if (gotoMatch.matches()) {
            this.arg1 = gotoMatch.group("label");
            this.arg2 = null;
            this.type = C_GOTO;
            return;
        }
        Matcher ifMatch = ifCmd.matcher(this.currentLine);
        if (ifMatch.matches()) {
            this.arg1 = ifMatch.group("label");
            this.arg2 = null;
            this.type = C_IF;
            return;
        }
        Matcher callMatch = callCmd.matcher(this.currentLine);
        if (callMatch.matches()) {
            this.arg1 = callMatch.group("func");
            this.arg2 = callMatch.group("args");
            this.type = C_CALL;
            return;
        }
        Matcher funcMatch = functionCmd.matcher(this.currentLine);
        if (funcMatch.matches()) {
            this.arg1 = funcMatch.group("func");
            this.arg2 = funcMatch.group("locals");
            this.type = C_FUNCTION;
            return;
        }
        Matcher returnMatch = returnCmd.matcher(this.currentLine);
        if (returnMatch.matches()) {
            this.arg1 = null;
            this.arg2 = null;
            this.type = C_RETURN;
            return;
        }

        throw new Exception("invalid command: " + currentLine);
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public CommandType getType() {
        return type;
    }

    public String getCurrentLine() {
        return this.currentLine;
    }
}
