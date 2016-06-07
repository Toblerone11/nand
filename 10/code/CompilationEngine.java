import tokenization.JackTokenizer;
import vmproduce.xmlFileOrganizer;

import java.io.*;
import java.util.Stack;
import java.util.regex.Pattern;

import static tokenization.constants.JackConstants.*;
import static tokenization.constants.TokenType.*;

/**
 * a top- down CFG parser which compiles a jack source code into intermediate VM code.
 * this compiler engine uses the tokenizer in order to separate the content and understand the semantics. this way it
 * responsible only to preserve the recursive context free rules.
 */
public class CompilationEngine {

    /* scope titles (for Exception messages */
    private static final String CLASS_TITLE = "class", STATEMENTS_TITLE = "statements", SUBROUT_DEC_TITLE = "subroutineDec",
            PARAMLIST_TITLE = "parameterList", SUBROUT_BODY = "subroutineBody", CLASS_VAR_DEC = "classVarDec",
            VAR_DEC = "varDec", DO_TITLE = "doStatement", WHILE_TITLE = "whileStatement", LET_TITLE = "letStatement",
            IF_TITLE = "ifStatement", RETURN_TITLE = "returnStatement", EXPR_TITLE = "expression", TERM_TITLE = "term",
            EXPR_LIST_TITLE = "expressionList";

    /* xml titles */
    private static final String IDENT_TITLE = "identifier", KEYWORD_TITLE = "keyword", INT_TITLE = "integerConstant",
                                STR_TITLE = "stringConstant", SYMBOL_TITLE = "symbol";


    /* exception messages */
    private static final String TOKEN_TYPE_MSG = "Wrong token type", TOKEN_MSG = "Wrong token";

    /* statements types first char */
    private static final char DO_C = 'd', WHILE_C = 'w', IF_C = 'i', LET_C = 'l', RETURN_C = 'r';

    /* patterns */
    private static final Pattern STATEMENT_PATT = Pattern.compile(String.format("(%s|%s|%s|%s|%s)",
                    WHILE_KW, IF_KW, LET_KW, DO_KW, RETURN_KW));


    /* data members */
    private JackTokenizer tokenizer;
    private xmlFileOrganizer xmlFile;
    private Stack<String> stateStack;

    // current state members
    private String className;
    private String scope;



    /* C-tor */
    public CompilationEngine(JackTokenizer tokenizer, String outFileName) throws IOException {
        this.stateStack = new Stack<>();
        this.tokenizer = tokenizer;
        this.className = null;
        this.scope = null;
        xmlFile = new xmlFileOrganizer(outFileName);
    }

    private void setNextToken() throws IOException, SyntaxException {
        if (tokenizer.hasMoreTokens())
            tokenizer.advance();
    }

    /**
     * use to flush the already generated code into the file in the case of excpetion or at the end of processing.
     */
    public void finish() {
        xmlFile.close();
    }

    /**
     * checks if the given keyword is a primitive type keyword
     * @param keyword type name
     * @return true if primitive type, false otherwise
     */
    private boolean isPrimitive(String keyword) {
        return (keyword.equals(VOID_KW) || keyword.equals(INT_KW)
                || keyword.equals(BOOL_KW) || keyword.equals(CHAR_KW));
    }

    /**
     * Sets the current state of the program to be under the scope of the class that is now being analysed.
     * the parameters of the state are used by the VmWriter.
     * sets the name of the class
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileClass() throws IOException, SyntaxException {
        compileBeginScope(CLASS_TITLE);
        // checks that the class scope is being parsed now.
        if (!(tokenizer.getTokenType() == KEYWORD)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getKeyWord().equals(CLASS_KW))) throwSyntaxException(TOKEN_TYPE_MSG);
        xmlFile.addTerminal(KEYWORD_TITLE, CLASS_KW);

        // set the current name of the class
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        this.className = tokenizer.getIdentifier();
        xmlFile.addTerminal(IDENT_TITLE, this.className);

        // verify the existence of the open scope symbol
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);

        // compile class body
        setNextToken();
        while (tokenizer.getTokenType() == KEYWORD) {
            String type = tokenizer.getKeyWord();
            if (type.equals(STATIC_KW) || type.equals(FIELD_KW)) {
                compileClassVarDec();

            } else if (type.equals(CTOR_KW) || type.equals(METHOD_KW) || type.equals(FUNC_KW)) {
                compileSubroutine();

            } else {
                throwSyntaxException(TOKEN_TYPE_MSG);
            }
        }

        // next token already been set (just before the while had been broke.
        if (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == CLOSE_SCOPE) {
                xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);
                compileEndScope();
                xmlFile.close();
                return;
            }
            throwSyntaxException(TOKEN_MSG);
        }
        throwSyntaxException(TOKEN_TYPE_MSG);
    }

    /**
     * verify syntax and compiles the fields and static variables of the current class.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileClassVarDec() throws IOException, SyntaxException {
        // assuming that the static or field keywords already been encountered and checked
        compileBeginScope(CLASS_VAR_DEC);
        xmlFile.addTerminal(KEYWORD_TITLE, tokenizer.getKeyWord());

        compileBasicVarDec();
        compileEndScope();
    }

    /**
     * verify and compile a subroutine in the current class.
     * the subroutine can be constructor, method or static function.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileSubroutine() throws IOException, SyntaxException {
        // assuming the current token is subroutine keyword.
        compileBeginScope(SUBROUT_DEC_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, tokenizer.getKeyWord());

        // assuming the current token is subroutine keyword.
        String subroutineType = tokenizer.getKeyWord();

        // getting the return type of the subroutine.
        setNextToken();
        String returnType = null;
        switch (tokenizer.getTokenType()) {
            case KEYWORD:
                returnType = tokenizer.getKeyWord();
                if (!isPrimitive(returnType)) {
                    throwSyntaxException(TOKEN_MSG);
                }
                xmlFile.addTerminal(KEYWORD_TITLE, returnType);
                break;
            case IDENTIFIER:
                returnType = tokenizer.getIdentifier();
                xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
                break;
            default:
                throwSyntaxException(TOKEN_TYPE_MSG);
        }

        // getting the name of the subroutine, and write label.
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());

        // get parameter list.
        setNextToken(); // getting the open parenthesis for the param list.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(OPEN_PARAM_LIST == tokenizer.getSymbol())) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_PARAM_LIST);

        setNextToken();
        compileParameterList(); // adding all argument variables to symbol table.

        // close parameter list. assuming the current token is already checked to be symbol.
        if (!(CLOSE_PARAM_LIST == tokenizer.getSymbol())) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_PARAM_LIST);

        setNextToken();
        compileSubroutineBody();
        compileEndScope();

    }

    /**
     * verify and compile the body of the current subroutine including local variables and statements
     * @throws IOException
     * @throws SyntaxException
     */
    private void compileSubroutineBody() throws IOException, SyntaxException {
        compileBeginScope(SUBROUT_BODY);

        // check for open scope symbol
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);


        setNextToken();
        if (!(tokenizer.getTokenType() == KEYWORD)) throwSyntaxException(TOKEN_TYPE_MSG);

        // check for local variables declarations.
        while (tokenizer.getKeyWord().equals(VAR_KW)) {
            compileVarDec();
        }

        // check for statements
        if (tokenizer.getTokenType() == KEYWORD) {
            while (STATEMENT_PATT.matcher(tokenizer.getKeyWord()).matches()) {
                compileStatements();
                if (tokenizer.getTokenType() != KEYWORD) {
                    break;
                }
            }
        }

        // closing the scope, assuming the current token is the closing scope.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);
        compileEndScope();
        setNextToken();
    }

    /**
     * verify and compile the parameter list of the current subroutine. including types of primitives or class objects
     * and the name.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileParameterList() throws IOException, SyntaxException {
        // assuming the current token is the type of the first parameter of closed parenthesis.
        compileBeginScope(PARAMLIST_TITLE);

        boolean checkParams = (!(tokenizer.getTokenType() == SYMBOL) || !(tokenizer.getSymbol() == CLOSE_PARAM_LIST));
        while (checkParams) {
            // check param type
            String type;
            if (tokenizer.getTokenType() == KEYWORD) {
                type = tokenizer.getKeyWord();
                if (!isPrimitive(type)) {
                    throwSyntaxException(TOKEN_MSG);
                }
                xmlFile.addTerminal(KEYWORD_TITLE, type);
            }
            else if (tokenizer.getTokenType() == IDENTIFIER) {
                type = tokenizer.getIdentifier();
                xmlFile.addTerminal(IDENT_TITLE, type);
            }
            else {
                throwSyntaxException(TOKEN_TYPE_MSG);
            }

            // check param name.
            setNextToken();
            if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
            String name = tokenizer.getIdentifier();
            xmlFile.addTerminal(IDENT_TITLE, name);

            // check for other param.
            setNextToken();
            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG); // should be ',' or ')'
            if (tokenizer.getSymbol() != SEPARATOR) {
                break;
            } else {
                xmlFile.addTerminal(SYMBOL_TITLE, SEPARATOR);
                setNextToken();
            }
        }
        compileEndScope();
    }

    /**
     * verify and compile the local variables being defined inside the current subroutine.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileVarDec() throws IOException, SyntaxException {
        // assuming the current token is 'var'.
        compileBeginScope(VAR_DEC);
        xmlFile.addTerminal(KEYWORD_TITLE, VAR_KW);

        // determine variable details: [type, ...names]
        compileBasicVarDec();
        compileEndScope();
    }

    /**
     * verify syntax and compile 5 kinds of statements: 'while', 'if', 'do', 'let' and 'return'. each has its own
     * properties and this function decides which statements to generate, depends on the tokens.
     * @throws SyntaxException
     * @throws IOException
     */
    public void compileStatements() throws SyntaxException, IOException {
        // assuming the current token is statement keyword (while, if, let, etc...)
        compileBeginScope(STATEMENTS_TITLE);

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
                    throwSyntaxException(TOKEN_MSG);
            }
            // next token already been set.
        }

        compileEndScope();
    }

    /**
     * verify structure and compile do statement.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileDo() throws IOException, SyntaxException {
        // assuming the current token is 'do'.
        compileBeginScope(DO_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, DO_KW);

        // determine identifier name.
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        String name = tokenizer.getIdentifier();

        // check the next symbol - if access or param list
        setNextToken();
        if(!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        char symbol = tokenizer.getSymbol();
        if ((symbol == OPEN_PARAM_LIST) || (symbol == CLASS_ACCESS)) { // the identifier is function name.
            compileSubroutineCall(name, symbol);

            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
            if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
            xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
            compileEndScope();
            setNextToken();
            return;
        }
        throwSyntaxException(TOKEN_MSG);
    }

    /**
     * verify structure and compile let statement.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileLet() throws IOException, SyntaxException {
        // assuming the current token is 'let'.
        compileBeginScope(LET_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, LET_KW);

        // get var name
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());

        // check array option
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (tokenizer.getSymbol() == OPEN_ARRAY) {
            xmlFile.addTerminal(SYMBOL_TITLE, OPEN_ARRAY);
            setNextToken();
            compileExpression();                    // compute the index

            // check closing brackets
            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
            if (!(tokenizer.getSymbol() == CLOSE_ARRAY)) throwSyntaxException(TOKEN_MSG);
            xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_ARRAY);
            setNextToken();
        }

        // check assignment operator.
        if (!(tokenizer.getSymbol() == EQ_ASSIGN)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, EQ_ASSIGN);

        // get assigned expression
        setNextToken();
        compileExpression();

        // check end instruction symbol (';')
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
        compileEndScope();
        setNextToken();
    }

    /**
     * verify syntax and compile while loop statement.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileWhile() throws IOException, SyntaxException {
        // assuming the current token is 'while'.
        compileBeginScope(WHILE_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, WHILE_KW);

        // check open parenthesis for consition
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_COND)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

        // check inner condition
        setNextToken();
        compileExpression();

        // check close parenthesis
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_COND)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_COND);

        // check open curly brackets for body
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);

        setNextToken();
        compileStatements();

        // check closing scope, assuming the current token is '}'.
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);
        compileEndScope();
        setNextToken();
    }

    /**
     * verify and compile return statement. this method does not verify the return value with the return type
     * of the method.
     * @throws SyntaxException
     * @throws IOException
     */
    public void compileReturn() throws SyntaxException, IOException {
        compileBeginScope(RETURN_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, RETURN_KW);

        // check if not empty return
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL) || !(tokenizer.getSymbol() == END_INSTRUCTION)) {
            compileExpression();
        }

        // verify that the current token is ';'.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
        compileEndScope();
        setNextToken();
    }

    /**
     * verify and compile if-else statement, where the else is optional.
     * @throws SyntaxException
     * @throws IOException
     */
    public void compileIf() throws SyntaxException, IOException {
        // assuming the current token is 'while'.
        compileBeginScope(IF_TITLE);
        xmlFile.addTerminal(KEYWORD_TITLE, IF_KW);

        // check open parenthesis
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_COND)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_COND);

        // check condition
        setNextToken();
        compileExpression();

        // check close parenthesis
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_COND)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_COND);

        // check open scope
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);

        setNextToken();
        compileStatements();

        // check closing scope, assuming the current token is '}'.
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);

        //checking else statement
        setNextToken();
        if (tokenizer.getTokenType() == KEYWORD) {
            if (tokenizer.getKeyWord().equals(ELSE_KW)) {
                xmlFile.addTerminal(KEYWORD_TITLE, ELSE_KW);

                // check open scope
                setNextToken();
                if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
                if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);
                xmlFile.addTerminal(SYMBOL_TITLE, OPEN_SCOPE);

                setNextToken();
                compileStatements();

                // check closing scope, assuming the current token is '}'.
                if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);
                xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_SCOPE);

                setNextToken();
            }
        }
        compileEndScope();
    }

    /**
     * verify and compile expression in the oode. the expression is evaluated according to the standard math preceding rules.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileExpression() throws IOException, SyntaxException {
        // must end with next token being set.
        compileBeginScope(EXPR_TITLE);
        compileTerm(); // next symbol is being set.

        // handle operators and math preceding rules.
        while (tokenizer.getTokenType() == SYMBOL) {
            char newOperator = tokenizer.getSymbol();
            if ((newOperator == GREATER_THAN) || (newOperator == LOWER_THAN) || (newOperator == EQ)
                    || (newOperator == AND) || (newOperator == OR) || (newOperator == PLUS) || (newOperator == MINUS)
                    || (newOperator == MUL) || (newOperator == DIV)) {

                xmlFile.addTerminal(SYMBOL_TITLE, newOperator);
            } else {
                break;
            }
            setNextToken();
            compileTerm(); // next symbol is being set.
        }
        compileEndScope();
    }

    /**
     * verify the syntax of a term and compile it. a term can be any data type and value.
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileTerm() throws IOException, SyntaxException {
        // assuming the first relevant token was set had been set already.
        compileBeginScope(TERM_TITLE);

        boolean isNextTokenSet = false;
        switch (tokenizer.getTokenType()) {
            case INT_CONST:
                xmlFile.addTerminal(INT_TITLE, tokenizer.getIntVal());
                break;

            case STRING_CONST:
                String str = tokenizer.getStringVal();
                if (str.length() == 0) {
                    str = "\\n";
                }
                xmlFile.addTerminal(STR_TITLE, str);
                break;

            case KEYWORD:
                String keyword = tokenizer.getKeyWord();
                if (keyword.equals(TRUE) || keyword.equals(FALSE) || keyword.equals(NULL) || keyword.equals(THIS)) {
                    xmlFile.addTerminal(KEYWORD_TITLE, keyword);
                    break;
                }
                throwSyntaxException(TOKEN_MSG);

            case IDENTIFIER:
                boolean isArray = false;
                String identName = tokenizer.getIdentifier();
                setNextToken();
                if (tokenizer.getTokenType() == SYMBOL) {
                    char symbol = tokenizer.getSymbol();
                    if ((symbol == OPEN_PARAM_LIST) || (symbol == CLASS_ACCESS)) { // the identifier is function name.
                        compileSubroutineCall(identName, symbol);
                        isNextTokenSet = true;
                        break;
                    }
                    else if (symbol == OPEN_ARRAY) { // the identifier is array name
                        xmlFile.addTerminal(IDENT_TITLE, identName);
                        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_ARRAY);
                        isArray = true;
                    }
                    else {
                        // identifier is simple variable name or an array.
                        xmlFile.addTerminal(IDENT_TITLE, identName);
                    }
                }

                if (isArray) {
                    // check the expression inside the brackets, denoting the index
                    setNextToken();
                    compileExpression();

                    // check for closing brackets.
                    if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
                    if (!(tokenizer.getSymbol() == CLOSE_ARRAY)) throwSyntaxException(TOKEN_MSG);
                    xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_ARRAY);
                    setNextToken();
                    isNextTokenSet = true;
                } else {
                    isNextTokenSet = true;
                }
                break;

            case SYMBOL:
                char symbol = tokenizer.getSymbol();
                // check if next is expression is parentheses
                if (symbol == OPEN_PRE_EXP) {
                    xmlFile.addTerminal(SYMBOL_TITLE, OPEN_PRE_EXP);
                    setNextToken();
                    compileExpression();
                    if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
                    if (!(tokenizer.getSymbol() == CLOSE_PRE_EXP)) throwSyntaxException(TOKEN_MSG);
                    xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_PRE_EXP);
                } else {
                    // check if next is unary operation on term
                    if ((symbol != NEG_SIGN) && (symbol != NOT_SIGN)) throwSyntaxException(TOKEN_MSG);
                    xmlFile.addTerminal(SYMBOL_TITLE, symbol);
                    setNextToken();
                    compileTerm();
                    isNextTokenSet = true;
                }
                break;
        }
        if (!isNextTokenSet)
            setNextToken();

        compileEndScope();
    }

    /**
     * verify and compile expression list, where the expressions are enclosed in parenthesis separated by comas (',').
     * @throws IOException
     * @throws SyntaxException
     */
    public void compileExpressionList() throws IOException, SyntaxException {
        compileBeginScope(EXPR_LIST_TITLE);

        if (tokenizer.getTokenType() == SYMBOL) {
            if ((tokenizer.getSymbol() == CLOSE_PARAM_LIST)) {
                compileEndScope();
                return;
            }
        }
        compileExpression();
        while (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == SEPARATOR) {
                xmlFile.addTerminal(SYMBOL_TITLE, SEPARATOR);
                setNextToken();
                compileExpression();
                continue;
            }
            break;
        }
        compileEndScope();
    }

    /**
     * helper function for readability. this function check the syntax of calling a subrouting and determining which
     * subroutine had=s been called.
     * @param name the first name encountered. can be class name, variable name or subroutine name.
     */
    private void compileSubroutineCall(String name, char nextSymbol) throws IOException, SyntaxException {
        // assuming the current token is '(' of the param list or '.' to access into class field.

        xmlFile.addTerminal(IDENT_TITLE, name);

        if (nextSymbol == CLASS_ACCESS) {
            xmlFile.addTerminal(SYMBOL_TITLE, nextSymbol);
            setNextToken();
            if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
            xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
            setNextToken();
            }

        // verifying the parenthesis for the parameter list.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_PARAM_LIST)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, OPEN_PARAM_LIST);

        // compiling the given expressions to be the arguments of the subroutine in the current call.
        setNextToken();
        compileExpressionList();

        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_PARAM_LIST)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, CLOSE_PARAM_LIST);

        setNextToken();
    }

    /**
     * general function which activated anywhere a new scope begins.
     * this function changes few settings in order to adapt the compiler to nested scope.
     * @param scopeTitle the name of the scope which begins
     */
    private void compileBeginScope(String scopeTitle) {
        stateStack.push(scopeTitle);
        xmlFile.beginScope(scopeTitle);
        this.scope = scopeTitle;
    }

    /**
     * used to end the current scope, and restore the settings of the previous one.
     */
    private void compileEndScope() {
        String endingScope = stateStack.lastElement();
        xmlFile.endScope(endingScope);
        stateStack.pop();
        if (stateStack.empty()) {
            return;
        }
        this.scope = stateStack.firstElement();
    }

    /**
     * throws exception. this exception gets only the message itself and build the other details by itself.
     * helps to throw syntax exceptions in the code and keeps it clean and readable.
     * @param message the message
     * @throws SyntaxException
     */
    private void throwSyntaxException(String message) throws SyntaxException {
        throw new SyntaxException(this.scope, tokenizer.currentLineNum, message, tokenizer.getCurrentToken());
    }

    /**
     * verify syntax and compile declared variables in any scope. this function only return an array of the names of
     * the declared variable where the first index stores the type
     * @throws IOException
     * @throws SyntaxException
     */
    private void compileBasicVarDec() throws IOException, SyntaxException {
        // check type
        setNextToken();
        String type = null;
        if (tokenizer.getTokenType() == KEYWORD) {
            type = tokenizer.getKeyWord();
            if (!(type.equals(INT_KW) || type.equals(CHAR_KW) || type.equals(BOOL_KW))) {
                throwSyntaxException(TOKEN_MSG);
            }
            xmlFile.addTerminal(KEYWORD_TITLE, type);
        }
        else if (tokenizer.getTokenType() == IDENTIFIER) {
            xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
        } else {
            throwSyntaxException(TOKEN_TYPE_MSG);
        }

        // check var names
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());

        // check if there are more var names, else end scope
        setNextToken();
        while (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == SEPARATOR) {
                xmlFile.addTerminal(SYMBOL_TITLE, SEPARATOR);
                setNextToken(); // check var name
                if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
                xmlFile.addTerminal(IDENT_TITLE, tokenizer.getIdentifier());
                setNextToken();
            }
            else {
                break;
            }
        }

        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
        xmlFile.addTerminal(SYMBOL_TITLE, END_INSTRUCTION);
        setNextToken();
    }

}


