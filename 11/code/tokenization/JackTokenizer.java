package tokenization;

import tokenization.constants.TokenType;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Ron on 17/05/2016.
 */
public class JackTokenizer {

    //TODO order of recognition: comment, string, keyword, symbol, int,

    private static final String KEYWORD_STRING = "(class|method|function|constructor|int|"
            + "boolean|char|void|var|static|field|let|do|if|else|while|return|true|false|null|this)(.)?(.+)?";
    private static final String SYMBOL_STRING = "(\\{|}|\\(|\\)|\\[|]|\\.|,|;|\\+|\\-|\\*|/|&|\\||<|>|=|~)(.+)?";
    private static final String INT_CONST_STRING = "(\\d++)(([\\D])(.+)?)?";
    private static final String IDENTIFIER_STRING = "([a-zA-Z_][\\w_]*+)(.)?(.+)?";
    private static final String STR_CONST_STRING = "(\\\".+?\\\")";
    private static final String COMMENT = "(?://.*|/\\*{1,2}[\\s\\S]*\\*/)";
    private static final String DOC_BLOCK_OPEN = "/**", COMMENT_BLOCK_OPEN = "/*", COMMENT_BLOCK_CLOSE = "*/";


    private static final Pattern KEYWORD_PATTERN = Pattern.compile(KEYWORD_STRING);
    private static final Pattern SYMBOL_PATTERN = Pattern.compile(SYMBOL_STRING);
    private static final Pattern INT_CONST_PATTERN = Pattern.compile(INT_CONST_STRING);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER_STRING);
    private static final Pattern STR_PATTERN = Pattern.compile(STR_CONST_STRING);
    private static final Pattern COMMENT_PATT = Pattern.compile(COMMENT);
    private static final Pattern TO_IGNORE = Pattern.compile(String.format("(?:\\s*%s\\s*|^\\s*|\\s*$)", COMMENT));
    private static final Pattern COMMENT_BLOCK_BEGIN = Pattern.compile("^\\s*/\\*{1,2}.*"), COMMENT_BLOCK_END = Pattern.compile(".*?\\*/");

    /* data members */
    private BufferedReader jackReader;
    private int currentListNum;
    private String currentToken;
    private TokenType tokenType;
    private String keyWord;
    private Character symbol;
    private String identifier;
    private Integer intVal;
    private String stringVal;
    private List<String> carryLine;
    public int currentLineNum;
    private boolean commentMode;



    public JackTokenizer(File jackFile) throws IOException {
        this.jackReader = new BufferedReader(new FileReader(jackFile));
        this.currentListNum = 0;
        this.tokenType = null;
        this.keyWord = null;
        this.symbol = null;
        this.identifier = null;
        this.intVal = null;
        this.stringVal = null;
        this.carryLine = new ArrayList<String>();
        this.currentLineNum = 0;
        this.commentMode = false;
    }

    public boolean hasMoreTokens() throws IOException {
        if (this.carryLine.isEmpty() || this.carryLine.size() - 1 == this.currentListNum){
            String line = jackReader.readLine();
            currentLineNum++;
            if (line == null) {
                return false;
            }

            if (commentMode) {
                int endScopeIndex = line.indexOf(COMMENT_BLOCK_CLOSE);
                while (endScopeIndex == -1) {
                    line = jackReader.readLine();
                    currentLineNum++;
                    //TODO check for null and if encountered throw exception of missing block comment close symbol.
                    endScopeIndex = line.indexOf(COMMENT_BLOCK_CLOSE);
                }
                commentMode = false;
                line = line.substring(endScopeIndex + COMMENT_BLOCK_CLOSE.length());
            }


            while (TO_IGNORE.matcher(line).matches()) {
                line = jackReader.readLine();
                currentLineNum++;
                if (line == null)
                    return false;
            }

            if (COMMENT_BLOCK_BEGIN.matcher(line).matches()) {
                int endScopeIndex = line.indexOf(COMMENT_BLOCK_CLOSE);
                while (endScopeIndex == -1) {
                    line = jackReader.readLine();
                    currentLineNum++;
                    //TODO check for null and if encountered throw exception of missing block comment close symbol.
                    endScopeIndex = line.indexOf(COMMENT_BLOCK_CLOSE);
                }
                line = line.substring(endScopeIndex + COMMENT_BLOCK_CLOSE.length());
                if (line.equals("")) {
                    line = jackReader.readLine();
                    currentLineNum++;
                }
            }


            line = TO_IGNORE.matcher(line).replaceAll(""); // deleting comments from line
            String[] temp = line.split("\\s");
            carryLine.clear();
            Collections.addAll(carryLine, temp);
            this.currentListNum = 0;
        }
        else
        {
            this.currentListNum++;
        }

        return true;

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

    public void advance() throws SyntaxException, IOException {
        this.tokenType = null;
        this.keyWord = null;
        this.symbol = null;
        this.identifier = null;
        this.intVal = null;
        this.stringVal = null;
        this.currentToken = this.carryLine.get(this.currentListNum);
        Matcher tokenMatch = KEYWORD_PATTERN.matcher(currentToken);
        if (tokenMatch.matches()) {
            tokenType = TokenType.KEYWORD;
            keyWord = tokenMatch.group(1);
            if (tokenMatch.group(2) != null) {
                carryLine.add(currentListNum + 1, tokenMatch.group(2));
                if (tokenMatch.group(3) != null) {
                    carryLine.add(currentListNum + 2, tokenMatch.group(3));
                }
            }
        } else {
            if (currentToken.equals(COMMENT_BLOCK_OPEN) || currentToken.equals(DOC_BLOCK_OPEN)) {
                carryLine.remove(currentListNum);
                commentMode = true;
                if (hasMoreTokens()) {
                    advance();
                }
            } else {
                tokenMatch = SYMBOL_PATTERN.matcher(currentToken);
                if (tokenMatch.matches()) {
                    tokenType = TokenType.SYMBOL;
                    symbol = tokenMatch.group(1).charAt(0);
                    if (tokenMatch.group(2) != null) {
                        carryLine.add(currentListNum + 1, tokenMatch.group(2));
                    }
                } else {
                    tokenMatch = IDENTIFIER_PATTERN.matcher(currentToken);
                    if (tokenMatch.matches()) {
                        tokenType = TokenType.IDENTIFIER;
                        identifier = tokenMatch.group(1);
                        if (tokenMatch.group(2) != null) {
                            carryLine.add(currentListNum + 1, tokenMatch.group(2));
                            if (tokenMatch.group(3) != null) {
                                carryLine.add(currentListNum + 2, tokenMatch.group(3));
                            }
                        }
                    } else {
                        tokenMatch = STR_PATTERN.matcher(currentToken);
                        if (tokenMatch.matches()) {
                            tokenType = TokenType.STRING_CONST;
                            stringVal = tokenMatch.group(0);
                        } else {
                            tokenMatch = INT_CONST_PATTERN.matcher(currentToken);
                            if (tokenMatch.matches()) {
                                int temp = Integer.parseInt(tokenMatch.group(1));
                                if (temp >= 0 && temp <= 32767) {
                                    tokenType = TokenType.INT_CONST;
                                    intVal = temp;
                                }
                                if (tokenMatch.group(3) != null) {
                                    carryLine.add(currentListNum + 1, tokenMatch.group(3));
                                    if (tokenMatch.group(4) != null) {
                                        carryLine.add(currentListNum + 2, tokenMatch.group(4));
                                    }
                                }
                            } else {
                                throw new SyntaxException("General Scope", currentLineNum, "didn't recognize:", getCurrentToken());
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

//    public void throwException(String scopeName) throws SyntaxException {
//        throw new SyntaxException(scopeName, currentLineNum, currentToken);
//    }

}