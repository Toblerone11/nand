package tokenization.constants;

/**
 * Created by Ron on 17/05/2016.
 */
public class JackConstants {

    public static final int TERMINAL = 1, NON_TERMINAL = 2;

    /* symbols */
    public static final char OPEN_SCOPE = '{', CLOSE_SCOPE = '}', SEPARATOR = ',', END_INSTRUCTION = ';',
            OPEN_PARAM_LIST = '(', CLOSE_PARAM_LIST = ')', OPEN_ARRAY = '[', CLOSE_ARRAY = ']',
            EQ_ASSIGN = '=', OPEN_COND = '(', CLOSE_COND = ')', OPEN_PRE_EXP = '(', CLOSE_PRE_EXP = ')', CLASS_ACCESS = '.';

    /* unary operators */
    public static final char NEG_SIGN = '-', NOT_SIGN = '~';

    /* binary operators */
    public static final char PLUS = '+', MINUS = '-', MUL = '*', DIV = '/', AND = '&', OR = '|', GREATER_THAN = '>',
                             LOWER_THAN = '<', EQ = '=';

    /* keywords */
    public static final String CLASS_KW = "class", STATIC_KW = "static", FIELD_KW = "field",
            INT_KW = "int", CHAR_KW = "char", BOOL_KW = "boolean", VOID_KW = "void",
            CTOR_KW = "constructor", FUNC_KW = "function", METHOD_KW = "method",
            VAR_KW = "var", WHILE_KW = "while", IF_KW = "if", LET_KW = "let",
            ELSE_KW = "else", DO_KW = "do", RETURN_KW = "return";

    /* keyword JackConstants */
    public static final String TRUE = "true", FALSE = "false", NULL = "null", JACK_THIS = "this";
}
