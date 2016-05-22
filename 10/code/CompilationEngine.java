import java.io.*;
import java.util.Stack;
import java.util.regex.Pattern;

import static constants.constants.*;
import static constants.TokenType.*;

/**
 * Created by Ron on 17/05/2016.
 */
public class CompilationEngine {

    /* constants */
        /* scope titles */
    private static final String CLASS_TITLE = "class", STATEMENTS_TITLE = "statements", SUBROUT_DEC_TITLE = "subroutineDec",
            PARAMLIST_TITLE = "parameterList", SUBROUT_BODY = "subroutineBody", CLASS_VAR_DEC = "classVarDec",
            VAR_DEC = "varDec", DO_TITLE = "doStatement", WHILE_TITLE = "whileStatement", LET_TITLE = "letStatement",
            IF_TITLE = "ifStatement", RETURN_TITLE = "returnStatement", EXPR_TITLE = "expression", TERM_TITLE = "term",
            SUBROUT_CALL = "subroutineCall", INT_CONST_TITLE = "integerConstant", STR_CONST_TITLE = "stringConstant",
            EXPR_LIST_TITLE = "expressionList";
    /* terminal titles */
    private static final String KEYWORD_TITLE = "keyword", IDENT_TITLE = "IDENT_PATT", SYMBOL_TITLE = "symbol";

    /* statements types first char */
    private static final char DO_C = 'd', WHILE_C = 'w', IF_C = 'i', LET_C = 'l', RETURN_C = 'r';

    /* patterns */
    private static final Pattern IDENT_PATT = Pattern.compile("[a-zA-Z_][\\w_]*"),
            STATEMENT_PATT = Pattern.compile(String.format("(%s|%s|%s|%s|%s)",
                    WHILE_KW, IF_KW, LET_KW, DO_KW, RETURN_KW));


    /* data members */
    JackTokenizer tokenizer;
    FileOrganizer xmlFile;
    Stack<String> stateStack;
    String currentToken;

    /* C-tor */
    public CompilationEngine(JackTokenizer tokenizer, String outFileName) throws IOException {
        this.xmlFile = new FileOrganizer(outFileName);
        this.stateStack = new Stack<>();
        this.tokenizer = tokenizer;
        this.currentToken = null;
    }

    private void setNextToken() throws IOException {
        if (tokenizer.hasMoreTokens())
            tokenizer.advance();
    }

    public void compileClass() throws IOException, SyntaxException {
        stateStack.push(CLASS_TITLE);
        xmlFile.beginScope(CLASS_TITLE);

        if (!(tokenizer.getTokenType() == KEYWORD)) tokenizer.throwException(CLASS_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, CLASS_KW);

        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(CLASS_TITLE);
        xmlFile.addTerminal(IDENT_TITLE, currentToken);

        setNextToken();
        if (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == OPEN_SCOPE) {
                xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);
                return;
            }
        }
        tokenizer.throwException(CLASS_TITLE);

        setNextToken();
        while (tokenizer.getTokenType() == KEYWORD) {
            String type = tokenizer.getKeyWord();
            if (type.equals(STATIC_KW) || type.equals(FIELD_KW)) {
                compileClassVarDec();

            } else if (type.equals(CTOR_KW) || type.equals(METHOD_KW) || type.equals(FUNC_KW)) {
                compileSubroutine();

            } else {
                tokenizer.throwException(CLASS_TITLE);
            }

            setNextToken();
        }

        // next token already been set (just before the while had been broke.
        if (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == OPEN_SCOPE) {
                xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);
                return;
            }
        }
        tokenizer.throwException(CLASS_TITLE);
    }

    public void compileClassVarDec() throws IOException, SyntaxException {
        // assuming that the static or field keyword already been encountered and checked
        stateStack.push(CLASS_VAR_DEC);
        xmlFile.beginScope(CLASS_VAR_DEC);
        xmlFile.addTerminal(KEYWORD_TITLE, tokenizer.getKeyWord());

        compileBasicVarDec();
        compileEndScope();
    }

    public void compileSubroutine() throws IOException, SyntaxException {
        stateStack.push(SUBROUT_DEC_TITLE);
        xmlFile.beginScope(SUBROUT_DEC_TITLE);

        // assuming the current token is subroutine keyword.
        xmlFile.addTerminal(KEYWORD_TITLE, tokenizer.getKeyWord());

        // getting the return type of the subroutine.
        setNextToken();
        if (tokenizer.getTokenType() == KEYWORD) {
            if (!(tokenizer.getKeyWord() == VOID_KW)) tokenizer.throwException(SUBROUT_DEC_TITLE);
            xmlFile.addTerminal(KEYWORD_TITLE, VOID_KW);
        } else if (tokenizer.getTokenType() == IDENTIFIER) {
            xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
        } else {
            tokenizer.throwException(SUBROUT_DEC_TITLE);
        }

        // getting the name of the subroutine.
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(SUBROUT_DEC_TITLE);
        xmlFile.addTerminal(IDENT_TITLE, currentToken);

        // get parameter list.
        setNextToken(); // getting the open parenthesis for the param list.
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(SUBROUT_DEC_TITLE);
        if (!(OPEN_PARAM_LIST == tokenizer.getSymbol())) tokenizer.throwException(SUBROUT_DEC_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_PARAM_LIST);

        compileParameterList();

        // close parameter list. assuming the current token is closing parenthesis.
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_PARAM_LIST);

        compileSubroutineBody();

        compileEndScope();

    }

    private void compileSubroutineBody() throws IOException, SyntaxException {
        stateStack.push(SUBROUT_BODY);
        xmlFile.beginScope(SUBROUT_BODY);

        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(SUBROUT_BODY);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) tokenizer.throwException(SUBROUT_BODY);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);


        setNextToken();
        while (tokenizer.getTokenType() == KEYWORD) {
            // variables declarations
            if (tokenizer.getKeyWord().equals(VAR_KW)) {
                compileVarDec();
            }
            // statements
            else if (STATEMENT_PATT.matcher(tokenizer.getKeyWord()).matches()) {
                compileStatements();
            }
            // problem!
            else {
                tokenizer.throwException(SUBROUT_BODY);
            }
            setNextToken();
        }

        // closing the scope, assuming the current token is the closing scope.
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(SUBROUT_BODY);
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) tokenizer.throwException(SUBROUT_BODY);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);
        compileEndScope();
    }

    public void compileParameterList() throws IOException, SyntaxException {
        // assuming the current token is '('.
        // must end with the current token as ')'.

        // 4580910100126580 - exp: 01/19
        // recive - infinity - hebrew

        stateStack.push(PARAMLIST_TITLE);
        xmlFile.beginScope(PARAMLIST_TITLE);

        boolean firstIter = true;
        while (tokenizer.getTokenType() == SYMBOL) {

            if (!firstIter) {
                if (tokenizer.getSymbol() == CLOSE_PARAM_LIST) {
                    compileEndScope();
                    return;
                }
                if (!(tokenizer.getSymbol() == SEPARATOR)) tokenizer.throwException(PARAMLIST_TITLE);
                xmlFile.addTerminal(SYMBOL_TITLE, SEPARATOR);
            } else {
                firstIter = false;
            }

            // get parameter name.
            setNextToken();
            if (tokenizer.getTokenType() == IDENTIFIER) {
                xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
            }
            setNextToken();
        }
        tokenizer.throwException(PARAMLIST_TITLE);
    }

    public void compileVarDec() throws IOException, SyntaxException {
        // assuming the current token is 'var'.
        stateStack.push(VAR_DEC);
        xmlFile.beginScope(VAR_DEC);
        xmlFile.addTerminal(KEYWORD_TITLE, tokenizer.getKeyWord());

        compileBasicVarDec();
        compileEndScope();
    }

    public void compileStatements() throws SyntaxException, IOException {
        // assuming the cuurent token is statement keyword
        stateStack.push(STATEMENTS_TITLE);
        xmlFile.beginScope(STATEMENTS_TITLE);

        while (tokenizer.getTokenType() == KEYWORD) {
            char statementType = tokenizer.getKeyWord().charAt(0);
            switch (statementType) {
                case IF_C:
                    compileIf();
                    break;
                case WHILE_C:
                    compileWhile();
                    break;
                case DO_C:
                    compileDo();
                    break;
                case LET_C:
                    compileLet();
                    break;
                case RETURN_C:
                    compileReturn();
                    break;
                default:
                    tokenizer.throwException(STATEMENTS_TITLE);
            }
            // next token already been set.
        }

        compileEndScope();
    }

    public void compileDo() throws IOException, SyntaxException {
        // assuming the current token is 'do'.
        stateStack.push(DO_TITLE);
        xmlFile.beginScope(DO_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, DO_KW);

        compileSubroutineCall();

        compileEndScope();
    }

    public void compileLet() throws IOException, SyntaxException {
        // assuming the current token is 'let'.
        stateStack.push(LET_TITLE);
        xmlFile.beginScope(LET_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, LET_KW);

        // get var name
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(LET_TITLE);
        xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());

        // check array option
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(LET_TITLE);
        if (tokenizer.getSymbol() == OPEN_ARRAY) {
            xmlFile.addTerminal(SYMBOL_TITLE, OPEN_ARRAY);
            compileExpression();
            //TODO check if this is already the current token.
            xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_ARRAY);

            setNextToken();
            if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(LET_TITLE);
        }

        // check assignment operator.
        if (!(tokenizer.getSymbol() == EQ_ASSIGN)) tokenizer.throwException(LET_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, EQ_ASSIGN);

        // get assigned expression
        compileExpression();

        // end statement with ;
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(LET_TITLE);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) tokenizer.throwException(LET_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
        compileEndScope();

        setNextToken();
    }

    public void compileWhile() throws IOException, SyntaxException {
        // assuming the current token is 'while'.
        stateStack.push(WHILE_TITLE);
        xmlFile.beginScope(WHILE_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, WHILE_KW);

        // check open parenthesis
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(WHILE_TITLE);
        if (!(tokenizer.getSymbol() == OPEN_COND)) tokenizer.throwException(WHILE_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

        // check inner condition
        compileExpression();

        // check close parenthesis
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(WHILE_TITLE);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) tokenizer.throwException(WHILE_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

        compileStatements();

        // check closing scope, assuming the current token is '}'.
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) tokenizer.throwException(WHILE_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);
        compileEndScope();

        setNextToken();

    }

    public void compileReturn() throws SyntaxException, IOException {
        stateStack.push(RETURN_TITLE);
        xmlFile.beginScope(RETURN_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, RETURN_KW);

        compileExpression();

        // assumig the current token is ';'.
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(RETURN_TITLE);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) tokenizer.throwException(RETURN_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
        setNextToken();
        compileEndScope();
    }

    public void compileIf() throws SyntaxException, IOException {
        // assuming the current token is 'while'.
        stateStack.push(IF_TITLE);
        xmlFile.beginScope(IF_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, IF_KW);

        // check open parenthesis
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(IF_TITLE);
        if (!(tokenizer.getSymbol() == OPEN_COND)) tokenizer.throwException(IF_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

        // check inner condition
        compileExpression();

        // check close parenthesis
        setNextToken();
        // TODO check the ending of expression statement
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(IF_TITLE);
        if (!(tokenizer.getSymbol() == CLOSE_COND)) tokenizer.throwException(IF_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_COND);

        // check open scope
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(IF_TITLE);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) tokenizer.throwException(IF_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

        compileStatements();

        // check closing scope, assuming the current token is '}'.
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) tokenizer.throwException(IF_TITLE);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);

        //checking else statement
        setNextToken();
        if (tokenizer.getTokenType() == KEYWORD) {
            if (tokenizer.getKeyWord() == ELSE_KW) {
                xmlFile.addTerminal(KEYWORD_TITLE, ELSE_KW);

                // check open scope
                setNextToken();
                if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(IF_TITLE);
                if (!(tokenizer.getSymbol() == OPEN_SCOPE)) tokenizer.throwException(IF_TITLE);
                xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

                compileStatements();

                // check closing scope, assuming the current token is '}'.
                if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) tokenizer.throwException(IF_TITLE);
                xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);

                setNextToken();
            }
        }
        compileEndScope();

    }

    public void compileExpression() throws IOException, SyntaxException {
        // must end with next token being set.
        stateStack.push(EXPR_TITLE);
        xmlFile.beginScope(EXPR_TITLE);

        compileTerm(); // next symbol is being set.

        while (tokenizer.getTokenType() == SYMBOL) {
            char operator = tokenizer.getSymbol();
            if ((operator == PLUS) || (operator == MINUS) || (operator == MUL) || (operator == DIV) || (operator == GREATER_THAN)
                    || (operator == LOWER_THAN) || (operator == AND) || (operator == OR) || (operator == EQ)) {
                xmlFile.addTerminal(SYMBOL_TITLE, operator);
                compileTerm(); // next symbol is being set.
                continue;
            }
            break;
        }
        compileEndScope();
    }

    public void compileTerm() throws IOException, SyntaxException {
        // assuming the relevant symbol is not being set already.
        stateStack.push(TERM_TITLE);
        xmlFile.beginScope(TERM_TITLE);

        boolean isNextTokenSet = false;
        setNextToken();
        switch (tokenizer.getTokenType()) {
            case INT_CONST:
                xmlFile.addTerminal(INT_CONST_TITLE, tokenizer.getIntVal());
                break;

            case STRING_CONST:
                xmlFile.addTerminal(STR_CONST_TITLE, tokenizer.getStringVal());
                break;

            case KEYWORD:
                String keyword = tokenizer.getKeyWord();
                if (keyword.equals(TRUE) || keyword.equals(FALSE) || keyword.equals(NULL) || keyword.equals(THIS)) {
                    xmlFile.addTerminal(KEYWORD_TITLE, keyword);
                    break;
                }
                tokenizer.throwException(TERM_TITLE);

            case IDENTIFIER:
                String name = tokenizer.getIdentifier();
                setNextToken();
                if (tokenizer.getTokenType() == SYMBOL) {
                    char symbol = tokenizer.getSymbol();
                    if (symbol == OPEN_ARRAY) { // the identifier is array name
                        xmlFile.addTerminal(IDENT_TITLE, name);
                        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_ARRAY);
                        compileExpression();
                        if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(TERM_TITLE);
                        if (!(tokenizer.getSymbol() == CLOSE_ARRAY)) tokenizer.throwException(TERM_TITLE);
                        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_ARRAY);
                        break;
                    } else if ((symbol == OPEN_PARAM_LIST) || (symbol == CLASS_ACCESS)) { // the identifier is function name.
                        compileSubroutineCall(name, symbol);
                        break;
                    }
                }
                xmlFile.addTerminal(IDENT_TITLE, name); // identifier is simple variable name.
                isNextTokenSet = true;
                break;

            case SYMBOL:
                char symbol = tokenizer.getSymbol();
                if (symbol == OPEN_PRE_EXP) {
                    xmlFile.addTerminal(SYMBOL_TITLE, OPEN_PRE_EXP);
                    compileExpression();
                    if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(TERM_TITLE);
                    if (!(tokenizer.getSymbol() == CLOSE_PRE_EXP)) tokenizer.throwException(TERM_TITLE);
                    xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_PRE_EXP);
                } else if ((symbol == NEG_SIGN) || (symbol == DISPOSE)) {
                    xmlFile.addTerminal(SYMBOL_TITLE, symbol);
                    compileTerm();
                    isNextTokenSet = true;
                } else {
                    tokenizer.throwException(TERM_TITLE);
                }
                break;
        }
        if (!isNextTokenSet)
            setNextToken();

        compileEndScope();
    }

    public void compileExpressionList() throws IOException, SyntaxException {
        stateStack.push(EXPR_LIST_TITLE);
        xmlFile.beginScope(EXPR_LIST_TITLE);

        compileExpression();

        while (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == SEPARATOR) {
                xmlFile.addTerminal(SYMBOL_TITLE, SEPARATOR);
                compileExpression();
                continue;
            }
            break;
        }
        compileEndScope();
    }

    /**
     * helper function for readability
     * @param subroutineName
     */
    private void compileSubroutineCall(String subroutineName, char nextSymbol) throws IOException, SyntaxException {
        // assuming the current token is '(' of the param list or '.' to access into class field.
        stateStack.push(SUBROUT_CALL);
        xmlFile.beginScope(SUBROUT_CALL);

        xmlFile.addTerminal(IDENT_TITLE, subroutineName);

        while (nextSymbol == CLASS_ACCESS) {
            xmlFile.addTerminal(SYMBOL_TITLE, nextSymbol);
            setNextToken();
            if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(SUBROUT_CALL);
            xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
            setNextToken();
            if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(SUBROUT_CALL);
            nextSymbol = tokenizer.getSymbol();
        }
        if (nextSymbol == OPEN_PARAM_LIST) {
            xmlFile.addTerminal(SYMBOL_TITLE, nextSymbol);
            compileExpressionList();
            if (!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(SUBROUT_CALL);
            if (!(tokenizer.getSymbol() == CLOSE_PARAM_LIST)) tokenizer.throwException(SUBROUT_CALL);
            xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_PARAM_LIST);
            compileEndScope();
            return;
        }
        tokenizer.throwException(SUBROUT_CALL);
    }

    /**
     * helper function for readability
     */
    private void compileSubroutineCall() throws IOException, SyntaxException {
        // TODO delete method and implement this code segment in the compileDo method.
        // assuming the current token is before the name of the subroutine

        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(SUBROUT_CALL);
        String name = tokenizer.getIdentifier();
        setNextToken();
        if(!(tokenizer.getTokenType() == SYMBOL)) tokenizer.throwException(SUBROUT_CALL);
        char symbol = tokenizer.getSymbol();
        if ((symbol == OPEN_PARAM_LIST) || (symbol == CLASS_ACCESS)) { // the identifier is function name.
            compileSubroutineCall(name, symbol);
            return;
        }
        tokenizer.throwException(SUBROUT_CALL);

    }

    /**
     * used to end the current scope.
     */
    private void compileEndScope() {
        xmlFile.endScope(stateStack.pop());
    }

    private void compileBasicVarDec() throws IOException, SyntaxException {
        // check type
        setNextToken();
        if (tokenizer.getTokenType() == KEYWORD) {
            String type = tokenizer.getKeyWord();
            if (type.equals(INT_KW) || type.equals(CHAR_KW) || type.equals(BOOL_KW)) {
                xmlFile.addTerminal(KEYWORD_TITLE, type);
            }
            //TODO check if identifiers can be keywords.
            //tokenizer.throwException(CLASS_VAR_DEC);
        } else if (tokenizer.getTokenType() == IDENTIFIER) {
            xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
        } else {
            tokenizer.throwException(CLASS_VAR_DEC);
        }

        // check var name
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(CLASS_VAR_DEC);
        xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());

        // check if there are more var names, else end scope
        setNextToken();
        while (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == SEPARATOR) {
                xmlFile.addTerminal(SYMBOL_TITLE, SEPARATOR);
                setNextToken(); // check var name
                if (!(tokenizer.getTokenType() == IDENTIFIER)) tokenizer.throwException(CLASS_VAR_DEC);
                xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());

            } else if (tokenizer.getSymbol() == END_INSTRUCTION) {
                xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
                break;

            } else {
                tokenizer.throwException(CLASS_VAR_DEC);
            }
            setNextToken();
        }

    }

}
