package tokenization;

import tokenization.constants.TokenType;



import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tokenization.constants.TokenType;


/**
 * Created by Ron on 17/05/2016.
 */
public class JackTokenizer {

    private static final String KEYWORD_STRING = "(class|method|function|constructor|int|"
            + "boolean|char|void|var|static|field|let|do|if|else|while|return|true|false|null|this)";
    private static final String SYMBOL_STRING = "(\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|\\;|\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\=|\\-)";
    private static final String INT_CONST_STRING = "([0-32767])";
    private static final String IDENTIFIER_STRING = "([a-zA-z_][\\w_]*)";
    private static final String STR_CONST_STRING = "(\\\".*?\\\")";
    private static final String COMMENT = "(?://.*|/\\*\\*?[\\s\\S]*\\*/)";
    private static final String SINGLE_LINE_COMMENT = "//.*\n";
    private static final String DOC_BLOCK_OPEN = "/**", COMMENT_BLOCK_OPEN = "/*", COMMENT_BLOCK_CLOSE = "*/";
    private static final String STR_COMMENT = "/\\*\\*.*?\\*/|\\*.*?\\*/|//.*+\n";

    private static final Pattern KEYWORD_PATTERN = Pattern.compile(KEYWORD_STRING);
    private static final Pattern SYMBOL_PATTERN = Pattern.compile(SYMBOL_STRING);
    private static final Pattern INT_CONST_PATTERN = Pattern.compile(INT_CONST_STRING);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER_STRING);
    private static final Pattern STR_PATTERN = Pattern.compile(STR_CONST_STRING);
    private static final String STR_ALL = (STR_COMMENT + '|' + STR_CONST_STRING + '|' + SYMBOL_STRING + '|' + KEYWORD_STRING + '|' + INT_CONST_STRING + '|' + IDENTIFIER_STRING );
    private static final Pattern ALL_PATT = Pattern.compile(STR_ALL);
    private static final Pattern SINGLE_LINE_COMMENT_PATT = Pattern.compile(SINGLE_LINE_COMMENT);

    /* data members */
    private BufferedReader jackReader;
    private String currentToken;
    private TokenType tokenType;
    private String keyWord;
    private Character symbol;
    private String identifier;
    private Integer intVal;
    private String stringVal;
    public int currentLineNum;
    private StringBuffer data;
    private Matcher m;


    public JackTokenizer(File jackFile) throws IOException {
        this.jackReader = new BufferedReader(new FileReader(jackFile));
        this.data = new StringBuffer();
        String line = null;

        while((line = jackReader.readLine())!=null){
            this.data.append(line).append("\n");
        }
        this.m = ALL_PATT.matcher(data);
        this.tokenType = null;
        this.keyWord = null;
        this.symbol = null;
        this.identifier = null;
        this.intVal = null;
        this.stringVal = null;
        this.currentLineNum = 0;
    }

    public boolean hasMoreTokens() throws IOException {

        if (m.find()) {
            this.currentToken = m.group();
        }
        else
        {
            return false;
        }
        return true;
    }

    public void advance() throws IOException {
        this.tokenType = null;
        this.keyWord = null;
        this.symbol = null;
        this.identifier = null;
        this.intVal = null;
        this.stringVal = null;
        Matcher tokenMatch = KEYWORD_PATTERN.matcher(currentToken);
        if (tokenMatch.matches()) {
            tokenType = TokenType.KEYWORD;
            keyWord = tokenMatch.group(1);
        } else {
            if (currentToken.equals(COMMENT_BLOCK_OPEN) || currentToken.equals(DOC_BLOCK_OPEN)) {
                if (hasMoreTokens()) {
                    advance();
                }
            } else {
                tokenMatch = SINGLE_LINE_COMMENT_PATT.matcher(currentToken);
                if (tokenMatch.matches()) {
                    if (hasMoreTokens()) {
                        advance();
                    }
                } else {
                    tokenMatch = SYMBOL_PATTERN.matcher(currentToken);
                    if (tokenMatch.matches()) {
                        tokenType = TokenType.SYMBOL;
                        symbol = tokenMatch.group(1).charAt(0);
                    } else {
                        tokenMatch = IDENTIFIER_PATTERN.matcher(currentToken);
                        if (tokenMatch.matches()) {
                            tokenType = TokenType.IDENTIFIER;
                            identifier = tokenMatch.group(1);
                        } else {
                            tokenMatch = STR_PATTERN.matcher(currentToken);
                            if (tokenMatch.matches()) {
                                tokenType = TokenType.STRING_CONST;
                                stringVal = tokenMatch.group(0);
                            } else {
                                tokenMatch = INT_CONST_PATTERN.matcher(currentToken);
                                if (tokenMatch.matches()) {
                                    int temp = Integer.parseInt(currentToken);
                                    if (temp >= 0 && temp <= 32767) {
                                        tokenType = TokenType.INT_CONST;
                                        intVal = temp;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getKeyWord() {
        if (tokenType == TokenType.KEYWORD)
            return keyWord;
        return null;
    }

    public char getSymbol() {
        if (tokenType == TokenType.SYMBOL)
            return symbol;
        return '0';
    }

    public String getIdentifier() {
        if (tokenType == TokenType.IDENTIFIER)
            return identifier;
        return null;
    }

    public int getIntVal() {
        if (tokenType == TokenType.INT_CONST)
            return intVal;
        return 0;
    }

    public String getStringVal() {
        if (tokenType == TokenType.STRING_CONST)
            return stringVal;
        return null;
    }

    public char[] takeWhile(Reader reader, char[] target) throws IOException {
        ArrayList<Character> collectedChars = new ArrayList<>();
        char[] charBuffer = new char[target.length];
        reader.read(charBuffer);
        while (charBuffer != target) {
            for (char c : charBuffer)
            collectedChars.add(c);
            reader.read(charBuffer);
        }
        char[] result = new char[collectedChars.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = collectedChars.get(i);
        }
        return result;
    }

    /**
     * for testing
     */
    public String getCurrentToken() {
        switch (tokenType) {
            case INT_CONST:
                return String.format("%d", intVal);
            case STRING_CONST:
                return stringVal;
            case KEYWORD:
                return keyWord;
            case SYMBOL:
                return String.format("%c", symbol);
            case IDENTIFIER:
                return identifier;
            default:
                return "";
        }
    }
}
