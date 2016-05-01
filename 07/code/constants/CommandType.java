package constants;

/**
 * Created by Ron on 18/04/2016.
 */
public enum CommandType {
    C_ARITHMETIC,
    C_PUSH, C_POP,
    C_LABEL,
    C_GOTO, C_IF,
    C_FUNCTION,
    C_RETURN,
    C_CALL;

    public int value() {
        return this.ordinal();
    }
}
