import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import constants.TokenType;

/**
 * Created by Ron on 17/05/2016.
 */
public class CompilationEngine {

    /* data members */
    BufferedWriter out;
    JackTokenizer tokenizer;

    /* C-tor */
    public CompilationEngine(File jackFile, BufferedWriter out) throws FileNotFoundException {
        this.out = out;
        this.tokenizer = new JackTokenizer(jackFile);
    }

    public void compileClass() {

    }

    public void compileClassVarDec() {

    }

    public void compileSubroutine() {

    }

    public void compileParameterList() {

    }

    public void  comopileVarDec() {

    }

    public void compileStatements() {

    }

    public void compileDo() {

    }

    public void compileLet() {

    }

    public void compileWhile() {

    }

    public void compileReturn() {

    }

    public void compileIf() {

    }

    public void compileExpression() {

    }

    public void compileTerm() {

    }

    public void compileExpressionList() {

    }
}
