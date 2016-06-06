import tokenization.JackTokenizer;
import tokenization.SyntaxException;
import vmproduce.SymbolTable;
import vmproduce.VMWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import static tokenization.constants.JackConstants.*;
import static tokenization.constants.TokenType.*;
import static vmproduce.VmConstants.*;

/**
 * Created by Ron on 17/05/2016.
 */
public class CompilationEngine {
    /* Jack Functions */
    private static final String ALLOC_FUNC = "Memory.alloc", NEW_STR_FUNC = "String.new",
                                STR_APPEND_FUNC = "String.appendChar";

    /* scope titles (for Exception messages */
    private static final String CLASS_TITLE = "class", CLASS_VAR_DEC = "class member declaration", VAR_DEC = "var declaration",
                                SUBROUTINE_DEC = "subroutine definition", PARAMLIST_TITLE = "parameter list",
                                SUBROUTINE_BODY = "subroutine body", STATEMENTS_TITLE = "statements", IF_TITLE = "if",
                                DO_TITLE = "do", LET_TITLE = "let", WHILE_TITLE = "while", RETURN_TITLE = "return",
                                EXPR_LIST = "expression list", EXPR_TITLE = "expression", TERM_TITLE = "term";


    /* exception messages */
    private static final String TOKEN_TYPE_MSG = "Wrong token type", TOKEN_MSG = "Wrong token";

    /* statements types first char */
    private static final char DO_C = 'd', WHILE_C = 'w', IF_C = 'i', LET_C = 'l', RETURN_C = 'r';

    /* patterns */
    private static final Pattern STATEMENT_PATT = Pattern.compile(String.format("(%s|%s|%s|%s|%s)",
                    WHILE_KW, IF_KW, LET_KW, DO_KW, RETURN_KW));

    private static final int NO_RETURN_VALUE = 0;

    /* data members */
    private JackTokenizer tokenizer;
    private VMWriter vmWriter;
    private Stack<String> stateStack;
    private SymbolTable symbolTable;

    // current state members
    private String className, functionName;
    private String scope;
    /* labels indices */
    private int ifTrueLabelIndex, elseLabelIndex, continueLabelIndex, whileLabelIndex;

    /* constant String variables indices */
    private int constStringIndex;


    /* C-tor */
    public CompilationEngine(JackTokenizer tokenizer, String outFileName) throws IOException {
        this.vmWriter = new VMWriter(outFileName);
        this.stateStack = new Stack<>();
        this.tokenizer = tokenizer;
        this.className = null;
        this.functionName = null;
        this.scope = null;
        this.symbolTable = new SymbolTable();
        this.ifTrueLabelIndex = 0;
        this.elseLabelIndex = 0;
        this.continueLabelIndex = 0;
        this.whileLabelIndex = 0;
        this.constStringIndex = 0;
    }

    private void setNextToken() throws IOException, SyntaxException {
        if (tokenizer.hasMoreTokens())
            tokenizer.advance();
    }

    private boolean isPrimitive(String keyword) {
        return (keyword.equals(VOID_KW) || keyword.equals(INT_KW)
                || keyword.equals(BOOL_KW) || keyword.equals(CHAR_KW));
    }

    private String generateIfTrueLabel() {
        elseLabelIndex = ifTrueLabelIndex;
        continueLabelIndex = ifTrueLabelIndex;
        whileLabelIndex = ifTrueLabelIndex;
        return String.format("IF_TRUE_%s", ifTrueLabelIndex++);
    }

    private String generateElseLabel() {
        return String.format("ELSE_%s", elseLabelIndex++);
    }

    private String generateContinueLabel() {
        return String.format("CONTINUE_%s", continueLabelIndex++);
    }

    private String generateWhileLabel() {
        return String.format("WHILE_%s", whileLabelIndex++);
    }

    private String generateStringVar() {
        return String.format("stringVar_%s", constStringIndex++);
    }

    public void finish() {
        vmWriter.close();
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

        // set the current name of the class
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        this.className = tokenizer.getIdentifier();

        // verify the existence of the open scope symbol
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);

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
                compileEndScope();
                vmWriter.close();
                return;
            }
            throwSyntaxException(TOKEN_MSG);
        }
        throwSyntaxException(TOKEN_TYPE_MSG);
    }

    public void compileClassVarDec() throws IOException, SyntaxException {
        // assuming that the static or field keywords already been encountered and checked
        compileBeginScope(CLASS_VAR_DEC);
        //determine the kind of the variable
        String kind = tokenizer.getKeyWord(); // should return 'static' or 'field'.

        // determine variable details: [type, ...names]
        String[] vars = compileBasicVarDec();
        String type = vars[0];
        for (int i = 1; i < vars.length; i++) {
            symbolTable.define(vars[i], type, kind);
        }
        compileEndScope();
    }

    public void compileSubroutine() throws IOException, SyntaxException {
        compileBeginScope(SUBROUTINE_DEC);
        symbolTable.startSubroutine();

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
                break;
            case IDENTIFIER:
                returnType = tokenizer.getIdentifier();
                break;
            default:
                throwSyntaxException(TOKEN_TYPE_MSG);
        }

        // getting the name of the subroutine, and write label.
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        String subroutineName = String.format("%s.%s", className, tokenizer.getIdentifier());
        symbolTable.defineSubroutine(subroutineName, subroutineType, returnType);
        this.functionName = subroutineName;

        // get parameter list.
        setNextToken(); // getting the open parenthesis for the param list.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(OPEN_PARAM_LIST == tokenizer.getSymbol())) throwSyntaxException(TOKEN_MSG);
        setNextToken();
        compileParameterList(); // adding all argument variables to symbol table.

        // close parameter list. assuming the current token is already checked to be symbol.
        if (!(CLOSE_PARAM_LIST == tokenizer.getSymbol())) throwSyntaxException(TOKEN_MSG);

        setNextToken();
        compileSubroutineBody();

        compileEndScope();

    }

    private void compileSubroutineBody() throws IOException, SyntaxException {
        compileBeginScope(SUBROUTINE_BODY);

        // check for open scope symbol
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);


        setNextToken();
        if (!(tokenizer.getTokenType() == KEYWORD)) throwSyntaxException(TOKEN_TYPE_MSG);

        // check for local variables declarations.
        while (tokenizer.getKeyWord().equals(VAR_KW)) {
            compileVarDec();
        }

        // start writing subroutine.
        int nVars = symbolTable.getVarCount(VAR_KIND);
        vmWriter.writeFunction(functionName, nVars);

        // special operations for specific functions.
        if (symbolTable.isMethod(functionName)) {
            // insert 'this' symbol
            symbolTable.define(JACK_THIS, this.className, ARG); // should get index 0;
            vmWriter.writePush(ARG, 0);
            vmWriter.writePop(POINTER, PTR_THIS);
        }
        else if (symbolTable.isCtor(functionName)) {
            // executing memory allocation for the instance being created.
            int nFields = symbolTable.getVarCount(FIELD_KIND);
            vmWriter.writePush(CONSTANT, nFields);
            vmWriter.writeCall(ALLOC_FUNC, 1);
            vmWriter.writePop(POINTER, PTR_THIS);
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

        // return the allocated  address of this
//        if (symbolTable.isCtor(functionName)) {
//            vmWriter.write
//        }

            // closing the scope, assuming the current token is the closing scope.
            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
            if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);
            compileEndScope();
            setNextToken();
    }

    public void compileParameterList() throws IOException, SyntaxException {
        // assuming the current token is the type of the first parameter of closed parenthesis.
        compileBeginScope(PARAMLIST_TITLE);

        boolean checkParams = (!(tokenizer.getTokenType() == SYMBOL) || !(tokenizer.getSymbol() == CLOSE_PARAM_LIST));
        while (checkParams) {
            // check param type
            String type = null;
            if (tokenizer.getTokenType() == KEYWORD) {
                type = tokenizer.getKeyWord();
                if (!isPrimitive(type)) {
                    throwSyntaxException(TOKEN_MSG);
                }
            } else if (tokenizer.getTokenType() == IDENTIFIER) {
                type = tokenizer.getIdentifier();
            } else {
                throwSyntaxException(TOKEN_TYPE_MSG);
            }

            // check param name.
            setNextToken();
            if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
            String name = tokenizer.getIdentifier();

            //adding to symbol table
            symbolTable.define(name, type, ARG);

            // check for other param.
            setNextToken();
            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG); // should be ',' or ')'
            if (tokenizer.getSymbol() != SEPARATOR) {
                break;
            } else {
                setNextToken();
            }
        }
        compileEndScope();
    }

    public void compileVarDec() throws IOException, SyntaxException {
        // assuming the current token is 'var'.
        compileBeginScope(VAR_DEC);

        // determine variable details: [type, ...names]
        String[] vars = compileBasicVarDec();
        String type = vars[0];
        for (int i = 1; i < vars.length ; i++) {
            symbolTable.define(vars[i], type, VAR_KIND);
        }
        compileEndScope();
    }

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

    public void compileDo() throws IOException, SyntaxException {
        // assuming the current token is 'do'.
        compileBeginScope(DO_TITLE);

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
            vmWriter.writePop(TEMP, BASE_INDEX); // dumping the return value.

            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
            if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
            compileEndScope();
            setNextToken();
            return;
        }
        throwSyntaxException(TOKEN_MSG);
    }

    public void compileLet() throws IOException, SyntaxException {
        // assuming the current token is 'let'.
        compileBeginScope(LET_TITLE);

        // get var name
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        String varName = tokenizer.getIdentifier();
        String varKind = symbolTable.getKindOf(varName);
        String varSegment = convertKindToSegment(varKind);
        int varIndex = symbolTable.getIndexOf(varName);

        // check array option
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (tokenizer.getSymbol() == OPEN_ARRAY) {

            // evaluate exact address.
            vmWriter.writePush(varSegment, varIndex); // push base address
            setNextToken();
            compileExpression();                    // compute the index
            vmWriter.writeArithmetic(VM_ADD);       // add the two values to get the real address.

            // store the exact address and change the segment and index respectively.
            vmWriter.writePop(POINTER, PTR_THAT);
            varSegment = THAT;
            varIndex = BASE_INDEX;

            // check closing brackets
            if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
            if (!(tokenizer.getSymbol() == CLOSE_ARRAY)) throwSyntaxException(TOKEN_MSG);
            setNextToken();
        }

        // check assignment operator.
        if (!(tokenizer.getSymbol() == EQ_ASSIGN)) throwSyntaxException(TOKEN_MSG);

        // get assigned expression
        setNextToken();
        compileExpression();

        // assign value to variable
        vmWriter.writePop(varSegment, varIndex);

        // check end instruction symbol (';')
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
        compileEndScope();
        setNextToken();
    }

    public void compileWhile() throws IOException, SyntaxException {
        // assuming the current token is 'while'.
        compileBeginScope(WHILE_TITLE);
        String ifTrueLabel = generateIfTrueLabel();
        String continueLabel = generateContinueLabel();
        String whileLabel = generateWhileLabel();

        // check open parenthesis for consition
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_COND)) throwSyntaxException(TOKEN_MSG);


        // set start point of the loop
        vmWriter.writeLabel(whileLabel);

        // check inner condition
        setNextToken();
        compileExpression();

        // check truth value
        vmWriter.writeIf(ifTrueLabel);
        vmWriter.writeGoTo(continueLabel);
        vmWriter.writeLabel(ifTrueLabel);

        // check close parenthesis
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_COND)) throwSyntaxException(TOKEN_MSG);

        // check open curly brackets for body
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);

        setNextToken();
        compileStatements();
        vmWriter.writeGoTo(whileLabel);

        // check closing scope, assuming the current token is '}'.
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);
        compileEndScope();

        vmWriter.writeLabel(continueLabel);
        setNextToken();
    }

    public void compileReturn() throws SyntaxException, IOException {
        compileBeginScope(RETURN_TITLE);

        // check if not empty return
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL) || !(tokenizer.getSymbol() == END_INSTRUCTION)) {
            compileExpression();
        } else {
            vmWriter.writePush(CONSTANT, NO_RETURN_VALUE);
        }

        // verify that the current token is ';'.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
        vmWriter.writeReturn();
        compileEndScope();
        setNextToken();
    }

    public void compileIf() throws SyntaxException, IOException {
        // assuming the current token is 'while'.
        compileBeginScope(IF_TITLE);

        // check open parenthesis
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_COND)) throwSyntaxException(TOKEN_MSG);

        // check condition
        setNextToken();
        compileExpression();

        // check close parenthesis
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_COND)) throwSyntaxException(TOKEN_MSG);

        String ifTrueLabel = generateIfTrueLabel();
        String elseLabel = generateElseLabel();
        String continueLabel = generateContinueLabel();
        // handle goto

        vmWriter.writeIf(ifTrueLabel);
        vmWriter.writeGoTo(elseLabel);
        // check open scope
        setNextToken();
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);

        vmWriter.writeLabel(ifTrueLabel);
        setNextToken();
        compileStatements();
        vmWriter.writeGoTo(continueLabel);

        // check closing scope, assuming the current token is '}'.
        if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);

        //checking else statement
        vmWriter.writeLabel(elseLabel);
        setNextToken();
        if (tokenizer.getTokenType() == KEYWORD) {
            if (tokenizer.getKeyWord().equals(ELSE_KW)) {

                // check open scope
                setNextToken();
                if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
                if (!(tokenizer.getSymbol() == OPEN_SCOPE)) throwSyntaxException(TOKEN_MSG);

                setNextToken();
                compileStatements();

                // check closing scope, assuming the current token is '}'.
                if (!(tokenizer.getSymbol() == CLOSE_SCOPE)) throwSyntaxException(TOKEN_MSG);

                setNextToken();
            }
        }
        vmWriter.writeLabel(continueLabel);
        compileEndScope();
    }

    private boolean preceed(char firstOp) {
        return ((firstOp == MUL) || (firstOp != DIV));
    }

    private String translateBinaryOperator(char jackOperator) {
        switch (jackOperator) {
            case PLUS:
                return VM_ADD;
            case MINUS:
                return VM_SUB;
            case EQ:
                return VM_EQ;
            case GREATER_THAN:
                return VM_GT;
            case LOWER_THAN:
                return VM_LT;
            case AND:
                return VM_AND;
            case OR:
                return VM_OR;
            case NOT_SIGN:
                return VM_NOT;
            case MUL:
                return VM_MUL;
            case DIV:
                return VM_DIV;
            default:
                return null;
        }
    }

    private String translateUnaryOperator(char jackOperator) {
        switch (jackOperator) {
            case NOT_SIGN:
                return VM_NOT;
            case NEG_SIGN:
                return VM_NEG;
            default:
                return null;
        }
    }

    public void compileExpression() throws IOException, SyntaxException {
        // must end with next token being set.
        compileBeginScope(EXPR_TITLE);
        Stack<Character> operationsStack = new Stack<>();

        compileTerm(); // next symbol is being set.

        // handle operators and math preceding rules.
        while (tokenizer.getTokenType() == SYMBOL) {
            char newOperator = tokenizer.getSymbol();
            if ((newOperator == GREATER_THAN) || (newOperator == LOWER_THAN) || (newOperator == EQ)
                    || (newOperator == AND) || (newOperator == OR)) {

                // write push for all arithmetic commands.
                while (!operationsStack.isEmpty()) {
                    char operator = operationsStack.pop();
                    vmWriter.writeArithmetic(translateBinaryOperator(operator));
                }
            } else if ((newOperator == PLUS) || (newOperator == MINUS) || (newOperator == MUL) || (newOperator == DIV)) {
                if (!operationsStack.empty()) {
                    if (preceed(operationsStack.firstElement())) {
                        char precedingOp = operationsStack.pop();
                        vmWriter.writeArithmetic(translateBinaryOperator(precedingOp));
                    }
                }
            } else {
                break;
            }
            operationsStack.push(newOperator);
            setNextToken();
            compileTerm(); // next symbol is being set.
        }

        while (!operationsStack.isEmpty()) {
            char operator = operationsStack.pop();
            vmWriter.writeArithmetic(translateBinaryOperator(operator));
        }
        compileEndScope();
    }


    public void compileTerm() throws IOException, SyntaxException {
        // assuming the first relevant token was set had been set already.
        compileBeginScope(TERM_TITLE);

        boolean isNextTokenSet = false;
        switch (tokenizer.getTokenType()) {
            case INT_CONST:
                vmWriter.writePush(CONSTANT, tokenizer.getIntVal());
                break;

            case STRING_CONST:
                //TODO create new string object, then append char after char
                String strToken = tokenizer.getStringVal();
                vmWriter.writePush(CONSTANT, strToken.length()); // pushing the size of the 'String' to allocate as arg.
                vmWriter.writeCall(NEW_STR_FUNC, 1);        // create new String object. should push the base address of the object.

                /* the address of the new String object is located at the top of the global stack.
                 * we use the address as this argument to the method 'appendChar'. the method returns the base address
                 * of the String object, and we can use it again to append more chars or to return it.
                 */
                for (int i = 0; i < strToken.length(); i++) {
                    int charToPush = strToken.charAt(i);
                    vmWriter.writePush(CONSTANT, charToPush);
                    vmWriter.writeCall(STR_APPEND_FUNC, 2);
                }
                break;

            case KEYWORD:
                String keyword = tokenizer.getKeyWord();
                switch (keyword) {
                    case (TRUE):
                        vmWriter.writePush(CONSTANT, VM_TRUE);
                        vmWriter.writeArithmetic(VM_NEG);
                        break;
                    case (FALSE):
                        vmWriter.writePush(CONSTANT, VM_FALSE);
                        break;
                    case (NULL):
                        vmWriter.writePush(CONSTANT, VM_NULL);
                        break;
                    case (JACK_THIS):
                        // assuming this is a method and the current instance's pointer was inserted to 'this' segment.
                        vmWriter.writePush(POINTER, PTR_THIS);
                        break;
                    default:
                        // invalid token if got here.
                        throwSyntaxException(TOKEN_MSG);
                }
                break;

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
                        isArray = true;
                    }
                }
                // identifier is simple variable name or an array.
                String kind = symbolTable.getKindOf(identName);
                int index = symbolTable.getIndexOf(identName);
                String segment = convertKindToSegment(kind);
                vmWriter.writePush(segment, index);

                if (isArray) {
                    // check the expression inside the brackets, denoting the index
                    setNextToken();
                    compileExpression();

                    // base index and index had been pushed
                    vmWriter.writeArithmetic(VM_ADD);   // add them together
                    vmWriter.writePop(POINTER, PTR_THAT);   // store the pointer to the memory location
                    vmWriter.writePush(THAT, BASE_INDEX);   // push the value

                    // check for closing brackets.
                    if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
                    if (!(tokenizer.getSymbol() == CLOSE_ARRAY)) throwSyntaxException(TOKEN_MSG);
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
                    setNextToken();
                    compileExpression();
                    if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
                    if (!(tokenizer.getSymbol() == CLOSE_PRE_EXP)) throwSyntaxException(TOKEN_MSG);
                } else {
                    // check if next is unary operation on term
                    if ((symbol != NEG_SIGN) && (symbol != NOT_SIGN)) throwSyntaxException(TOKEN_MSG);
                    String unaryOp = translateUnaryOperator(symbol);

                    setNextToken();
                    compileTerm();
                    vmWriter.writeArithmetic(unaryOp);
                    isNextTokenSet = true;
                }
                break;
        }
        if (!isNextTokenSet)
            setNextToken();

        compileEndScope();
    }

    public int compileExpressionList() throws IOException, SyntaxException {
        compileBeginScope(EXPR_LIST);

        int numOfExpressions = 0;

        if (tokenizer.getTokenType() == SYMBOL) {
            if ((tokenizer.getSymbol() == CLOSE_PARAM_LIST)) {
                compileEndScope();
                return numOfExpressions;
            }
        }
        compileExpression();
        numOfExpressions++;
        while (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == SEPARATOR) {
                setNextToken();
                compileExpression();
                numOfExpressions++;
                continue;
            }
            break;
        }
        compileEndScope();
        return numOfExpressions;
    }

    private String convertKindToSegment(String kind) throws SyntaxException {
        String segment = null;
        switch (kind) {
            case STATIC_KIND:
                segment = STATIC;
                break;
            case FIELD_KIND:
                segment = THIS;
                break;
            case ARG:
                segment = ARG;
                break;
            case VAR_KIND:
                segment = LCL;
                break;
            default:
                throwSyntaxException(TOKEN_MSG);
        }
        return segment;
    }

    /**
     * helper function for readability
     * @param name
     */
    private void compileSubroutineCall(String name, char nextSymbol) throws IOException, SyntaxException {
        // assuming the current token is '(' of the param list or '.' to access into class field.

        String subroutineName;
        boolean notDirect = false;
        boolean isMethod = false;

        if (nextSymbol == CLASS_ACCESS) {
            notDirect = true;
            setNextToken();
            if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);

            String relatedClass = symbolTable.getTypeOf(name);
            // determining class name
            if ((relatedClass == null) || (relatedClass.equals(this.className))) {  // the name is not variable of the subroutine neither class member.
                // error free code - therefore the name is class
                relatedClass = name;
            }
            else {
                isMethod = true;
//            } else {
//                // error - not name of class or instance of class.
//                throwSyntaxException(TOKEN_MSG);
            }
            subroutineName = String.format("%s.%s", relatedClass, tokenizer.getIdentifier());
            setNextToken();
        }
        else {
            subroutineName = String.format("%s.%s", this.className, name);
            isMethod = true;
        }

        // verifying the parenthesis for the parameter list.
        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == OPEN_PARAM_LIST)) throwSyntaxException(TOKEN_MSG);

        // compiling the arguments list - they should be pushed into the stack.
        int thisArg = 0;
        if (isMethod) {
            // push this argument first.
            if (notDirect) {
                String instanceKind = symbolTable.getKindOf(name);
                String segment = convertKindToSegment(instanceKind);
                int instanceIndex = symbolTable.getIndexOf(name);
                vmWriter.writePush(segment, instanceIndex);
            } else {
                vmWriter.writePush(POINTER, PTR_THIS);
            }
            thisArg = 1;
        }
        setNextToken();
        int numOfArgs = compileExpressionList();

        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == CLOSE_PARAM_LIST)) throwSyntaxException(TOKEN_MSG);

        // executing
        vmWriter.writeCall(subroutineName, numOfArgs + thisArg);
        setNextToken();
//        compileEndScope();
    }

    private void compileBeginScope(String scopeTitle) {
        stateStack.push(scopeTitle);
        this.scope = scopeTitle;
    }

    /**
     * used to end the current scope.
     */
    private void compileEndScope() {;
        this.scope = stateStack.firstElement();
        stateStack.pop();
    }

    private void throwSyntaxException(String message) throws SyntaxException {
        throw new SyntaxException(this.scope, tokenizer.currentLineNum, message, tokenizer.getCurrentToken());
    }

    private String[] compileBasicVarDec() throws IOException, SyntaxException {
        // check type
        setNextToken();
        String type = null;
        if (tokenizer.getTokenType() == KEYWORD) {
            type = tokenizer.getKeyWord();
            if (!(type.equals(INT_KW) || type.equals(CHAR_KW) || type.equals(BOOL_KW))) {
                throwSyntaxException(TOKEN_MSG);
            }
            //tokenizer.throwException(CLASS_VAR_DEC);
        } else if (tokenizer.getTokenType() == IDENTIFIER) {
            type = tokenizer.getIdentifier();
        } else {
            throwSyntaxException(TOKEN_TYPE_MSG);
        }

        // check var names
        ArrayList<String> varNames = new ArrayList<>();
        setNextToken();
        if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
        varNames.add(tokenizer.getIdentifier());

        // check if there are more var names, else end scope
        setNextToken();
        while (tokenizer.getTokenType() == SYMBOL) {
            if (tokenizer.getSymbol() == SEPARATOR) {
                setNextToken(); // check var name
                if (!(tokenizer.getTokenType() == IDENTIFIER)) throwSyntaxException(TOKEN_TYPE_MSG);
                varNames.add(tokenizer.getIdentifier());
                setNextToken();
            }
            else {
                break;
            }
        }

        if (!(tokenizer.getTokenType() == SYMBOL)) throwSyntaxException(TOKEN_TYPE_MSG);
        if (!(tokenizer.getSymbol() == END_INSTRUCTION)) throwSyntaxException(TOKEN_MSG);
        String[] result = new String[1 + varNames.size()];
        result[0] = type;
        for (int i = 0; i < varNames.size(); i++) {
            result[i+1] = varNames.get(i);
        }
        setNextToken();
        return result;
    }

}


