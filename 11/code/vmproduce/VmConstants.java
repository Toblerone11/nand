package vmproduce;

/**
 * Created by Ron on 01/06/2016.
 */
public class VmConstants {

    /* stack commands */
    public static final String PUSH = "push", POP = "pop";

    /* segments commands */
    public static final String ARG = "argument", LCL = "local", THIS = "this", THAT = "that", TEMP = "temp", CONSTANT = "constant", POINTER = "pointer", STATIC = "static", RAM = "r";

    /* arithmetic commands */
    public static final String VM_ADD = "add", VM_SUB = "sub", VM_NEG = "neg", VM_EQ = "eq", VM_GT = "gt", VM_LT = "lt",
            VM_AND = "and", VM_OR = "or", VM_NOT = "not", VM_MUL = "mul", VM_DIV = "div";

    /* symbol kind */
    public static final String FIELD_KIND = "field", STATIC_KIND = "static", VAR_KIND = "var";

    /* keywords in vm */
    public static final int VM_TRUE = 1, VM_FALSE = 0, VM_NULL = 0, BASE_INDEX = 0, PTR_THIS = 0, PTR_THAT = 1;

}
