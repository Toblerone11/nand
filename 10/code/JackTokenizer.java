import java.io.*;
import java.util.Set;

import constants.TokenType;

/**
 * Created by Ron on 17/05/2016.
 */
public class JackTokenizer {

    /* data members */
    private BufferedReader jackReader;
    private int currentLineNum;
    private String currentToken;
    private TokenType tokenType;
    private String keyWord;
    private char symbol;
    private String identifier;
    private int intVal;
    private String stringVal;
    private Set keywordSet;

    public JackTokenizer(File jackFile) throws FileNotFoundException {
        this.jackReader = new BufferedReader(new FileReader(jackFile));
        this.currentLineNum = 0;
        this.currentToken = null;
        tokenType = null;
        this.keyWord = null;
        this.symbol = 0;
        this.identifier = null;
        this.intVal = 0;
        this.stringVal = null;
    }

    public boolean hasMoreTokens() {
        return false;
    }

    public void advance() {

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
