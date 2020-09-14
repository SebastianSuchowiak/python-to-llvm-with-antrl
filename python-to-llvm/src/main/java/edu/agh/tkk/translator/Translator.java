package edu.agh.tkk.translator;

import edu.agh.tkk.pythonantlr.Python3Lexer;
import edu.agh.tkk.pythonantlr.Python3Parser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;


public class Translator {

    private CharStream input;
    private Python3Lexer lexer;
    private CommonTokenStream commonTokenStream;
    private Python3Parser parser;
    private ParseTree tree;
    private TestVisitor llvmTranslatorVisitor;

    private void setup(String pythonCode) {
        input = CharStreams.fromString(pythonCode);
        lexer = new Python3Lexer(input);
        commonTokenStream = new CommonTokenStream(lexer);
        parser = new Python3Parser(commonTokenStream);
        tree = parser.file_input();
        llvmTranslatorVisitor = new TestVisitor();
    }

    public String translateFromString(String pythonCode) {
        setup(pythonCode);
        return llvmTranslatorVisitor.visit(tree);
    }
}
