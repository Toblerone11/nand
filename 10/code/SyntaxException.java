/**
 * Created by Ron on 21/05/2016.
 */
public class SyntaxException extends Exception {

    public SyntaxException(String scopeName, int lineNum, String theProblem) {
        super(String.format("Syntex Error in %s scope at line %d:\n\t%s", scopeName, lineNum, theProblem));
    }
}
