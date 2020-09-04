package edu.agh.tkk.translator;

import edu.agh.tkk.pythonantlr.Python3Lexer;
import edu.agh.tkk.pythonantlr.Python3Parser;
import edu.agh.tkk.pythonantlr.Python3Visitor;
import org.antlr.v4.runtime.tree.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TestVisitor extends AbstractParseTreeVisitor<String> implements Python3Visitor<String> {

    private Set<String> variables;
    private int tmpVarCounter;
    private String currentTmpVar;

    public TestVisitor() {
        variables = new HashSet<>();
        tmpVarCounter = 0;
        currentTmpVar = "";
    }

    @Override
    public String visitSingle_input(Python3Parser.Single_inputContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitFile_input(Python3Parser.File_inputContext ctx) {
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
    public String visitFuncdef(Python3Parser.FuncdefContext ctx) {
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
    public String visitStmt(Python3Parser.StmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitSmall_stmt(Python3Parser.Small_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitSimple_assign(Python3Parser.Simple_assignContext ctx) {
        String right = ctx.right_atom.getText();
        String var = ctx.left.getText();

        if (!variables.contains(var)) {
            variables.add(var);
            System.out.printf("%%%s = alloca i32, align 4\n", var);
        }

        if (ctx.right_atom.NUMBER() == null) {
            System.out.printf("%%%d = load i32, i32* %%%s, align 4\n", tmpVarCounter, right);
            System.out.printf("store i32 %%%d, i32* %%%s, align 4\n", tmpVarCounter, var);
            tmpVarCounter += 1;
        } else {
            System.out.printf("store i32 %s, i32* %%%s, align 4\n", right, var);
        }

        return null;
    }

    @Override
    public String visitComplex_assign(Python3Parser.Complex_assignContext ctx) {
        String var = ctx.left.getText();

        if (!variables.contains(var)) {
            variables.add(var);
            System.out.printf("%%%s = alloca i32, align 4\n", var);
        }

        System.out.print(visit(ctx.right_expr));
        if (!currentTmpVar.isEmpty()) {
            System.out.printf("store i32 %%%s, i32* %%%s, align 4\n", currentTmpVar, var);
            currentTmpVar = "";
        }
        return null;
    }

    @Override
    public String visitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        if (ctx.getChild(1) instanceof TerminalNode) {
            System.out.println(visit(ctx.getChild(0)) + " = " + visit(ctx.getChild(2)));
        }
        return null;
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
    public String visitReturn_stmt(Python3Parser.Return_stmtContext ctx) {
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
    public String visitCompound_stmt(Python3Parser.Compound_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAsync_stmt(Python3Parser.Async_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitIf_stmt(Python3Parser.If_stmtContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitWhile_stmt(Python3Parser.While_stmtContext ctx) {
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
    public String visitSuite(Python3Parser.SuiteContext ctx) {
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
    public String visitOr_test(Python3Parser.Or_testContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAnd_test(Python3Parser.And_testContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitNot_test(Python3Parser.Not_testContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitComparison(Python3Parser.ComparisonContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitComp_op(Python3Parser.Comp_opContext ctx) {
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
    public String visitArith_expr_multi_term(Python3Parser.Arith_expr_multi_termContext ctx) {
        String operation;
        if (ctx.op.getType() == Python3Parser.ADD) {
            operation = "add";
        } else {
            operation = "sub";
        }

        String result = "";
        if (ctx.left instanceof Python3Parser.Arith_expr_single_termContext) {
            String left = visit(ctx.left);
            String right = visit(ctx.right);
            if (left.contains("%") && right.contains("%")) {
                result += String.format("%%%d = load i32, i32* %s, align 4\n", tmpVarCounter, left);
                tmpVarCounter += 1;
                result += String.format("%%%d = load i32, i32* %s, align 4\n", tmpVarCounter, right);
                result += String.format("%%%s = %s nsw i32 %%%d, %%%d\n", operation, operation, tmpVarCounter-1, tmpVarCounter);
                tmpVarCounter += 1;
                currentTmpVar = operation;
            } else if (left.contains("%")) {
                result += String.format("%%%d = load i32, i32* %s, align 4\n", tmpVarCounter, left);
                result += String.format("%%%s = %s nsw i32 %%%d, %s\n", operation, operation, tmpVarCounter, right);
                tmpVarCounter += 1;
                currentTmpVar = operation;
            } else if (right.contains("%")) {
                result += String.format("%%%d = load i32, i32* %s, align 4\n", tmpVarCounter, right);
                result += String.format("%%%s = %s nsw i32 %%%d, %s\n", operation, operation, tmpVarCounter, left);
                tmpVarCounter += 1;
                currentTmpVar = operation;
            } else {
                result += String.format("%%%s = %s nsw i32 %s, %s\n", operation, operation, left, right);
                currentTmpVar = operation;
            }
        } else {
            result += visit(ctx.left);
            result += String.format("%%%d = load i32, i32* %s, align 4\n", tmpVarCounter, visit(ctx.right));
            result += String.format("%%%s = %s nsw i32 %%%s, %%%d\n", operation, operation, currentTmpVar, tmpVarCounter);
            tmpVarCounter += 1;
            currentTmpVar = operation;
        }
        return result;
    }

    @Override
    public String visitTerm_multi_factor(Python3Parser.Term_multi_factorContext ctx) {
        switch (ctx.op.getType()) {
            case Python3Lexer.STAR:  return "mul i32 " + visit(ctx.left) + ", " + visit(ctx.right);
            case Python3Lexer.DIV: return "div i32 " + visit(ctx.left) + ", " + visit(ctx.right);
        }
        return visitChildren(ctx);
    }

    @Override
    public String visitTerm_single_factor(Python3Parser.Term_single_factorContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitFactor(Python3Parser.FactorContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitPower(Python3Parser.PowerContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAtom_expr(Python3Parser.Atom_exprContext ctx) {
        return this.visitChildren(ctx);
    }

    @Override
    public String visitAtom(Python3Parser.AtomContext ctx) {
        if (Objects.nonNull(ctx.NAME())) {
            return  "%" + ctx.NAME().toString();
        } else {
            return ctx.NUMBER().toString();
        }
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
    public String visitArglist(Python3Parser.ArglistContext ctx) {
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
}
