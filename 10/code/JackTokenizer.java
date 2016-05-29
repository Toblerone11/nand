import java.io.*;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import constants.TokenType;

/**
 * Created by Ron on 17/05/2016.
 */
public class JackTokenizer {

    private static final String END_OF_FILE = "</tokens>";
    private static final Pattern KEYWORD_PATT = Pattern.compile("<(keyword)> ([\\w_]+) </\\1>");
    private static final Pattern SYMBOL_PATT = Pattern.compile("<(symbol)> (.) </\\1>");
    private static final Pattern IDENTIFIER_PATT = Pattern.compile("<(identifier)> ([\\w_]+) </\\1>");
    private static final Pattern INTEGER_PATT = Pattern.compile("<(integerConstant)> (\\d+) </\\1>");
    private static final Pattern STRING_PATT = Pattern.compile("<(stringConstant)> (.+?) </\\1>");

    /* data members */
    private BufferedReader jackReader;
    private int currentLineNum;
    private String currentToken;
    private TokenType tokenType;
    private String keyWord;
    private Character symbol;
    private String identifier;
    private Integer intVal;
    private String stringVal;

    public JackTokenizer(File jackFile) throws IOException {
        this.jackReader = new BufferedReader(new FileReader(jackFile));
        this.currentLineNum = 0;
        this.currentToken = jackReader.readLine(); // first line is irrelevant: "<tokens>".
        tokenType = null;
        this.keyWord = null;
        this.symbol = null;
        this.identifier = null;
        this.intVal = null;
        this.stringVal = null;
    }

    public boolean hasMoreTokens() throws IOException {
        currentToken = jackReader.readLine();
        if (currentToken.equals(END_OF_FILE)) {
            return false;
        }
        return true;

    }

    public void advance() {
        this.tokenType = null;
        this.keyWord = null;
        this.symbol = null;
        this.identifier = null;
        this.intVal = null;
        this.stringVal = null;

        Matcher tokenMatch = KEYWORD_PATT.matcher(currentToken);
        if (tokenMatch.matches()) {
            tokenType = TokenType.KEYWORD;
            keyWord = tokenMatch.group(2);
        } else {
            tokenMatch = SYMBOL_PATT.matcher(currentToken);
            if (tokenMatch.matches()) {
                tokenType = TokenType.SYMBOL;
                symbol = tokenMatch.group(2).charAt(0);
            } else {
                tokenMatch = IDENTIFIER_PATT.matcher(currentToken);
                if (tokenMatch.matches()) {
                    tokenType = TokenType.IDENTIFIER;
                    identifier = tokenMatch.group(2);
                } else {
                    tokenMatch = INTEGER_PATT.matcher(currentToken);
                    if (tokenMatch.matches()) {
                        tokenType = TokenType.INT_CONST;
                        intVal = Integer.parseInt(tokenMatch.group(2));
                    } else {
                        tokenMatch = STRING_PATT.matcher(currentToken);
                        if (tokenMatch.matches()) {
                            tokenType = TokenType.STRING_CONST;
                            stringVal = tokenMatch.group(2);
                        }
                    }
                }
            }
        }
        currentLineNum += 1;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getIntVal() {
        return intVal;
    }

    public String getStringVal() {
        return stringVal;
    }

    void throwException(String scopeName) throws SyntaxException {
        throw new SyntaxException(scopeName, currentLineNum, currentToken);
    }
}
