import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  this object is meant to parse on .asm file designated for the hack assembly language.
 *  responsible for recognizing the type of each line and to give details about the current
 */
public class Parser {

    /* constants */
    private static final String COMMENT = "(?://.*|\\s*)";
    private static final String C_TYPE = "C_COMMAND", A_TYPE = "A_COMMAND", L_TYPE = "L_COMMAND";
    private static final String COMPUTE_INSTRUCTION = "((?:[DMA])?(?:(?:[\\+\\-&!\\\\|])?[01DMA])(?:<<|>>)?)";
    private static final String JUMP_INSTRUCTION = "J(?:(?:G|L)(?:T|E)|NE|MP|EQ)";
    private static final String DST_INSTRUCTION = "[DMA]+";
    private static final Pattern AInstruction = Pattern.compile("@([A-Za-z:_\\.\\$][\\w:_\\.\\$]+|[\\d]+)");
    private static final Pattern LInstruction = Pattern.compile("\\(([A-Za-z:_\\.\\$][\\w:_\\.\\$]+)\\)");
    private static final Pattern CInstruction = Pattern.compile(String.format(
											    "(?:(?<dst>%s)=)?(?<cmp>%s)(?:;(?<jmp>%s))?",
                                                DST_INSTRUCTION, COMPUTE_INSTRUCTION, JUMP_INSTRUCTION));

    private static final Pattern comment = Pattern.compile(COMMENT);
    private static final Pattern toIgnore = Pattern.compile(String.format("(?:[\\s]+|%s)", COMMENT));

    /* data members */
    private BufferedReader br;
    private String currentLine;
    private String type;
    private String dest;
    private String jump;
    private String comp;
    private String symbol;
    private int lineNumber;

    /* C-tors */
	
	// the basic Ctor.
    public Parser(Reader asmFile) {
        br = new BufferedReader(asmFile);
        this.lineNumber = 0;
    }

	// Ctor for path in String format.
    public Parser(String asmFile) throws FileNotFoundException {
        this(new FileReader(new File(asmFile)));
    }

    /* methods */
	/**
	 *  checks if there is more lines parse
	 *  while skipping comments and empty lines.
	 *  this method ends with the currentLine field being set on the next line to parse
	 *  (if there is one)
	 *  return true if there is another line to parse, false otherwise.
	 */
    public boolean hasMoreLines() throws IOException {
        currentLine = br.readLine();
        if (currentLine == null)
            return false;

        while (comment.matcher(currentLine).matches()) {
            currentLine = br.readLine();
            if (currentLine == null)
                return false;
        }
        return true;
    }

    /**
     * this function reads the next instruction in assembly file, parse it, and allowing to check later each
     * component separately.
     */
    public void advance() throws Exception {
        this.lineNumber++;
        currentLine = toIgnore.matcher(currentLine).replaceAll("");
        Matcher cInstMatch = CInstruction.matcher(this.currentLine);
        if (cInstMatch.matches()) {
            this.type = C_TYPE;
            this.dest = cInstMatch.group("dst");
            this.jump = cInstMatch.group("jmp");
            this.comp = cInstMatch.group("cmp");
        }
        else {
            this.dest = null;
            this.jump = null;
            this.comp = null;
            Matcher aInstMatch = AInstruction.matcher(this.currentLine);
            if (aInstMatch.matches()) {
                this.type = A_TYPE;
                this.symbol = aInstMatch.group(1);
            }
            else {
                Matcher lInstMatch = LInstruction.matcher(this.currentLine);
                if (lInstMatch.matches()) {
                    this.type = L_TYPE;
                    this.symbol = lInstMatch.group(1);
                }
                else
                    throw new Exception(String.format("Not a valid instruction in line %s", this.lineNumber));
            }
        }
    }

	/**
	 *  getter for the type of the current command.
	 */
    public String getType() {
        return type;
    }

	/**
	 *  getter for the current parsed destination.
	 */
    public String getDest() {
        return dest;
    }

	/**
	 *  getter for the current parsed jump condition.
	 */
    public String getJump() {
        return jump;
    }

	/**
	 *  getter for the current parsed compute instruction
	 */
    public String getComp() {
        return comp;
    }

	/**
	 *  getter for the last parsed symbol.
	 */
    public String getSymbol() {
        return symbol;
    }
}
