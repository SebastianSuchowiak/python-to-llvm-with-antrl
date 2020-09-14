package edu.agh.tkk.translator;

import edu.agh.tkk.pythonantlr.Python3Lexer;
import edu.agh.tkk.pythonantlr.Python3Parser;
import edu.agh.tkk.pythonantlr.Python3Visitor;
import org.antlr.v4.runtime.tree.*;

import java.util.*;
import java.util.stream.Collectors;

public class TestVisitor extends AbstractParseTreeVisitor<String> implements Python3Visitor<String> {

    private Set<String> variables;
    private int tmpVarCounter;
    private Stack<String> atomStack;

    public TestVisitor() {
        variables = new HashSet<>();
        tmpVarCounter = 0;
        atomStack = new Stack<>();
    }

    @Override
    public String visitAssign_stmt(Python3Parser.Assign_stmtContext ctx) {
        String result = "";

        String left = "%" + ctx.left.getText();
        if (!variables.contains(left)) {
            variables.add(left);
            result += String.format("%s = alloca i32, align 4\n", left);
        }

        result += visit(ctx.right);
        String right = atomStack.pop();
        result += String.format("store i32 %s, i32* %s, align 4\n", right, left);

        return result;
    }

    @Override
    public String visitArith_expr_multi_term(Python3Parser.Arith_expr_multi_termContext ctx) {
        String operation;
        switch (ctx.op.getType()) {
            case Python3Lexer.ADD:  operation = "add"; break;
            case Python3Lexer.MINUS: operation = "sub"; break;
            default: throw new IllegalArgumentException("Unsupported operation code for arith_expr_multi_term: " + ctx.op.getType());
        }

        return createLeftRightOperation(ctx.left, ctx.right, operation);
    }

    @Override
    public String visitTerm_multi_factor(Python3Parser.Term_multi_factorContext ctx) {
        String operation;
        switch (ctx.op.getType()) {
            case Python3Lexer.STAR:  operation = "mul"; break;
            case Python3Lexer.DIV: operation = "div"; break;
            default: throw new IllegalArgumentException("Unsupported operation code for term_multi_factor: " + ctx.op.getType());
        }

        return createLeftRightOperation(ctx.left, ctx.right, operation);
    }

    private String createLeftRightOperation(ParseTree left, ParseTree right, String operation) {
        String result = "";

        result += visit(left);
        String leftVar = atomStack.pop();

        result += visit(right);
        String rightVar = atomStack.pop();

        String opTmpVarName = "%" + operation + tmpVarCounter++;
        result += String.format("%s = %s nsw i32 %s, %s\n", opTmpVarName, operation, leftVar, rightVar);
        atomStack.push(opTmpVarName);

        return result;
    }

    @Override
    public String visitAtom(Python3Parser.AtomContext ctx) {
        if (Objects.nonNull(ctx.NAME())) {
            String varName = "%" + ctx.NAME().getText();
            String tmpVarName = getNextTmpVarName();
            atomStack.push(tmpVarName);
            return  String.format("%s = load i32, i32* %s, align 4\n", tmpVarName, varName);
        } else {
            String number = ctx.NUMBER().getText();
            atomStack.push(number);
            return "";
        }
    }

    @Override
    public String visitIf_stmt(Python3Parser.If_stmtContext ctx) {
        StringBuilder result = new StringBuilder();

        List<Python3Parser.TestContext> ifTests = ctx.test();
        List<Python3Parser.SuiteContext> ifSuits = ctx.suite();
        Python3Parser.SuiteContext elseSuit = null;
        if (ifSuits.size() != ifTests.size()) {
             elseSuit = ifSuits.remove(ifSuits.size() - 1);
        }

        String endLabel = "if.end" + tmpVarCounter++;
        for (int i = 0; i < ifSuits.size(); i++){
            String successLabel = "if.then" + tmpVarCounter++;
            String failLabel = "if.else" + tmpVarCounter++;

            result.append(createOrCond(successLabel, failLabel, ifTests.get(i).or_test(0)));
            result.append(formatLabel(successLabel));
            result.append(visit(ifSuits.get(i)));
            result.append(String.format("br label %%%s\n", endLabel));
            result.append(formatLabel(failLabel));
        }

        if (Objects.nonNull(elseSuit)) {
            result.append(visit(elseSuit));
        }
        result.append(String.format("br label %%%s\n", endLabel));
        result.append(formatLabel(endLabel));

        return result.toString();
    }

    @Override
    public String visitWhile_stmt(Python3Parser.While_stmtContext ctx) {
        StringBuilder result = new StringBuilder();

        Python3Parser.Or_testContext cond = ctx.test().or_test(0);
        Python3Parser.SuiteContext body = ctx.suite(0);

        String endLabel = "while.end" + tmpVarCounter++;
        String condLabel = "while.cond" + tmpVarCounter++;
        String bodyLabel = "while.body" + tmpVarCounter++;

        result.append(String.format("br label %%%s\n", condLabel));
        result.append(formatLabel(condLabel));
        result.append(createOrCond(bodyLabel, endLabel, cond));
        result.append(formatLabel(bodyLabel));
        result.append(visit(body));
        result.append(String.format("br label %%%s\n", condLabel));
        result.append(formatLabel(endLabel));

        return result.toString();
    }

    @Override
    public String visitSuite(Python3Parser.SuiteContext ctx) {
        return getChildrenText(ctx.children);
    }

    @Override
    public String visitOr_test(Python3Parser.Or_testContext ctx) {
        return visitChildren(ctx);
    }

    private String createOrCond(String successLabel, String failLabel, Python3Parser.Or_testContext ctx) {
        StringBuilder result = new StringBuilder();
        List<Python3Parser.And_testContext> andTests = ctx.and_test();
        int lastEndIdx = andTests.size()-1;

        for (int i = 0; i < lastEndIdx; i++) {
            String orFailLabel = "if.or.fail" + tmpVarCounter++;
            result.append(createAndCond(successLabel, orFailLabel, andTests.get(i)));
            result.append(formatLabel(orFailLabel));
        }

        Python3Parser.And_testContext lastAnd = andTests.get(lastEndIdx);
        result.append(createAndCond(successLabel, failLabel, lastAnd));

        return result.toString();
    }

    private String createAndCond(String successLabel, String failLabel, Python3Parser.And_testContext ctx) {
        StringBuilder result = new StringBuilder();
        List<Python3Parser.Not_testContext> notTests = ctx.not_test();
        int lastEndIdx = notTests.size()-1;

        for (int i = 0; i < lastEndIdx; i++) {
            String notSuccessLabel = "if.and.success" + tmpVarCounter++;
            result.append(createNotCond(notSuccessLabel, failLabel, notTests.get(i)));
            result.append(formatLabel(notSuccessLabel));
        }

        Python3Parser.Not_testContext lastAnd = notTests.get(lastEndIdx);
        result.append(createNotCond(successLabel, failLabel, lastAnd));

        return result.toString();
    }

    private String createNotCond(String successLabel, String failLabel, Python3Parser.Not_testContext ctx) {
        if (Objects.nonNull(ctx.not_test())) {
            throw new IllegalArgumentException("\"not\" operator is not supported");
        }
        return createComparisonCond(successLabel, failLabel, ctx.comparison());
    }

    private String createComparisonCond(String successLabel, String failLabel, Python3Parser.ComparisonContext ctx) {
        //TODO: Signed unsigned support
        StringBuilder result = new StringBuilder();

        String operation = visit(ctx.comp_op(0));

        result.append(visit(ctx.expr(0)));
        String left = atomStack.pop();

        result.append(visit(ctx.expr(1)));
        String right = atomStack.pop();

        String cmpVar = "%cmp" + tmpVarCounter++;
        result.append(String.format("%s = icmp %s i32 %s, %s\n", cmpVar, operation, left, right));
        result.append(String.format("br i1 %s, label %%%s, label %%%s\n", cmpVar, successLabel, failLabel));

        return result.toString();
    }

    @Override
    public String visitFuncdef(Python3Parser.FuncdefContext ctx) {
        StringBuilder result = new StringBuilder();

        List<String> parameters;
        if (Objects.nonNull(ctx.parameters().typedargslist())) {
            parameters = createParametersList(ctx.parameters());
        } else {
            parameters = Collections.emptyList();
        }

        String args = parameters.stream().map(c -> "i32 %" + c + ".arg").collect(Collectors.joining(", "));
        result.append(String.format("define dso_local i32 @%s(%s) #0 {\nentry:\n", ctx.NAME(), args));

        result.append(parameters.stream().map(this::createArgInit).collect(Collectors.joining()));
        result.append(visit(ctx.suite()));
        result.append("}\n\n");

        return result.toString();
    }

    private String createArgInit(String arg) {
        return "%" + arg + " = alloca i32, align 4\n" +
                "store i32 %" + arg + ".arg, i32* %" + arg + ", align 4\n";
    }

    private List<String> createParametersList(Python3Parser.ParametersContext ctx) {
        return ctx.typedargslist().tfpdef()
                .stream()
                .map(c -> c.NAME().getText())
                .collect(Collectors.toList());
    }

    @Override
    public String visitArglist(Python3Parser.ArglistContext ctx) {
        return this.visitChildren(ctx);
    }


    private String formatLabel(String label) {
        return "\n" + label + ":\n";
    }

    @Override
    public String visitComparison(Python3Parser.ComparisonContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public String visitAnd_test(Python3Parser.And_testContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public String visitNot_test(Python3Parser.Not_testContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public String visitComp_op(Python3Parser.Comp_opContext ctx) {
        int operationType = ((TerminalNode) ctx.children.get(0)).getSymbol().getType();
        switch (operationType) {
            case Python3Lexer.EQUALS: return "eq";
            case Python3Lexer.LESS_THAN: return "slt";
            case Python3Lexer.GREATER_THAN: return "sgt";
            case Python3Lexer.GT_EQ: return "sge";
            case Python3Lexer.LT_EQ: return "sle";
            case Python3Lexer.NOT_EQ_2: return "ne";
            default: throw new IllegalArgumentException("Invalid operation type for comp_op: " + operationType);
        }
    }

    @Override
    public String visitStmt(Python3Parser.StmtContext ctx) {
        return getChildrenText(ctx.children);
    }

    @Override
    public String visitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        return getChildrenText(ctx.children);
    }

    @Override
    public String visitSmall_stmt(Python3Parser.Small_stmtContext ctx) {
        return getChildrenText(ctx.children);
    }

    @Override
    public String visitFile_input(Python3Parser.File_inputContext ctx) {
        return getChildrenText(ctx.children);
    }

    @Override
    public String visitCompound_stmt(Python3Parser.Compound_stmtContext ctx) {
        return getChildrenText(ctx.children);
    }

    @Override
    public String visitReturn_stmt(Python3Parser.Return_stmtContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append(visit(ctx.testlist()));
        String varToReturn = atomStack.pop();
        result.append(String.format("ret i32 %s\n", varToReturn));
        return result.toString();
    }

    private String getChildrenText(List<ParseTree> children) {
        StringBuilder result = new StringBuilder();
        for (ParseTree stmt: children) {
            String childText = visit(stmt);
            if (Objects.nonNull(childText)) {
                result.append(childText);
            }
        }
        atomStack = new Stack<String>();
        return result.toString();
    }

    @Override
    public String visitFactor(Python3Parser.FactorContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append(visitChildren(ctx));

        if (Objects.nonNull(ctx.op) && ctx.op.getType() == Python3Lexer.MINUS) {
            String atom = atomStack.pop();
            String factorizedAtom;
            if (atom.contains("%")) {
                factorizedAtom = "%sub" + tmpVarCounter++;
                result.append(String.format("%s = sub nsw i32 0, %s\n", factorizedAtom, atom));
            } else {
                if (atom.contains("-")) {
                    factorizedAtom = atom.substring(1);
                } else {
                    factorizedAtom = "-" + atom;
                }
            }
            atomStack.push(factorizedAtom);
        }

        return result.toString();
    }


    @Override
    public String visitAtom_expr(Python3Parser.Atom_exprContext ctx) {
        if (ctx.trailer().size() != 0) {
            String callVariable = "%call" + tmpVarCounter;
            String result = createArgs(ctx.trailer().get(0));
            String args = atomStack.pop();
            result += String.format("%s = call i32 @test(%s)\n", callVariable, args);
            atomStack.push(callVariable);
            return result;
        } else {
            return this.visitChildren(ctx);
        }
    }

    public String createArgs(Python3Parser.TrailerContext ctx) {
        if (Objects.isNull(ctx.arglist())) {
            atomStack.push("");
            return "";
        }
        StringBuilder result = new StringBuilder();
        List<String> argStrings = new ArrayList<>();
        for (Python3Parser.ArgumentContext arg: ctx.arglist().argument()) {
            result.append(visit(arg));
            argStrings.add(atomStack.pop());
        }
        String args = argStrings.stream().map(c -> "i32 " + c).collect(Collectors.joining(", "));
        atomStack.push(args);
        return result.toString();
    }

    @Override
    public String visitSingle_input(Python3Parser.Single_inputContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitEval_input(Python3Parser.Eval_inputContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDecorator(Python3Parser.DecoratorContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDecorators(Python3Parser.DecoratorsContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDecorated(Python3Parser.DecoratedContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAsync_funcdef(Python3Parser.Async_funcdefContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitParameters(Python3Parser.ParametersContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTypedargslist(Python3Parser.TypedargslistContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTfpdef(Python3Parser.TfpdefContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitVarargslist(Python3Parser.VarargslistContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitVfpdef(Python3Parser.VfpdefContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        return visitChildren(ctx);
    }

    @Override
    public String visitTerm_single_factor(Python3Parser.Term_single_factorContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitPower(Python3Parser.PowerContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAnnassign(Python3Parser.AnnassignContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTestlist_star_expr(Python3Parser.Testlist_star_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAugassign(Python3Parser.AugassignContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDel_stmt(Python3Parser.Del_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitPass_stmt(Python3Parser.Pass_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitFlow_stmt(Python3Parser.Flow_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitBreak_stmt(Python3Parser.Break_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitContinue_stmt(Python3Parser.Continue_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitYield_stmt(Python3Parser.Yield_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitRaise_stmt(Python3Parser.Raise_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitImport_stmt(Python3Parser.Import_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitImport_name(Python3Parser.Import_nameContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitImport_from(Python3Parser.Import_fromContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitImport_as_name(Python3Parser.Import_as_nameContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDotted_as_name(Python3Parser.Dotted_as_nameContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitImport_as_names(Python3Parser.Import_as_namesContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDotted_as_names(Python3Parser.Dotted_as_namesContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDotted_name(Python3Parser.Dotted_nameContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitGlobal_stmt(Python3Parser.Global_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitNonlocal_stmt(Python3Parser.Nonlocal_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAssert_stmt(Python3Parser.Assert_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAsync_stmt(Python3Parser.Async_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitFor_stmt(Python3Parser.For_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTry_stmt(Python3Parser.Try_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitWith_stmt(Python3Parser.With_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitWith_item(Python3Parser.With_itemContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitExcept_clause(Python3Parser.Except_clauseContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTest(Python3Parser.TestContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTest_nocond(Python3Parser.Test_nocondContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitLambdef(Python3Parser.LambdefContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitLambdef_nocond(Python3Parser.Lambdef_nocondContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitStar_expr(Python3Parser.Star_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitExpr(Python3Parser.ExprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitXor_expr(Python3Parser.Xor_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAnd_expr(Python3Parser.And_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitShift_expr(Python3Parser.Shift_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitArith_expr_single_term(Python3Parser.Arith_expr_single_termContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTestlist_comp(Python3Parser.Testlist_compContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTrailer(Python3Parser.TrailerContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitSubscriptlist(Python3Parser.SubscriptlistContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitSubscript(Python3Parser.SubscriptContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitSliceop(Python3Parser.SliceopContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitExprlist(Python3Parser.ExprlistContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitTestlist(Python3Parser.TestlistContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitDictorsetmaker(Python3Parser.DictorsetmakerContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitClassdef(Python3Parser.ClassdefContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitArgument(Python3Parser.ArgumentContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitComp_iter(Python3Parser.Comp_iterContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitComp_for(Python3Parser.Comp_forContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitComp_if(Python3Parser.Comp_ifContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitEncoding_decl(Python3Parser.Encoding_declContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitYield_expr(Python3Parser.Yield_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitYield_arg(Python3Parser.Yield_argContext ctx) {
        return this.visitChildren(ctx);
    }

    private String getNextTmpVarName() {
        return "%" + tmpVarCounter++;
    }
}
