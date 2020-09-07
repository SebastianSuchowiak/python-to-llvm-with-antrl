package edu.agh.tkk.translator;

import edu.agh.tkk.pythonantlr.Python3Lexer;
import edu.agh.tkk.pythonantlr.Python3Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Main {

    private static final String TEST_FILE_PATH = "python-to-llvm/src/main/resources/test.py";

    public static void main(String[] args) throws IOException {

        CharStream input = CharStreams.fromFileName(TEST_FILE_PATH);
        Python3Lexer lexer = new Python3Lexer(input);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(commonTokenStream);

        ParseTree tree = parser.file_input();

        TestVisitor llvmTranslatorVisitor = new TestVisitor();
        System.out.println(llvmTranslatorVisitor.visit(tree));
    }
}
