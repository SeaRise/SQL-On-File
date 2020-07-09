package com.searise.sof.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.expression.Attribute;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.Literal;
import com.searise.sof.expression.ScalarFunction;
import com.searise.sof.plan.Filter;
import com.searise.sof.plan.LogicalPlan;
import com.searise.sof.plan.Project;
import com.searise.sof.plan.Relation;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.searise.sof.type.DataType.*;

public class AstBuilder extends SqlBaseBaseVisitor<Object> {

    @SuppressWarnings("unchecked")
    public <V> V typedVisit(ParseTree ctx) {
        return (V) ctx.accept(this);
    }

    @Override
    public LogicalPlan visitSingleStatement(SqlBaseParser.SingleStatementContext ctx) {
        return typedVisit(ctx.statement());
    }

    @Override
    public LogicalPlan visitSelectStatement(SqlBaseParser.SelectStatementContext ctx) {
        LogicalPlan child = Preconditions.checkNotNull(typedVisit(ctx.fromCluse()));
        if (Objects.nonNull(ctx.whereCluse())) {
            Expression condition = Preconditions.checkNotNull(typedVisit(ctx.whereCluse().expression()));
            child = new Filter(child, ImmutableList.of(condition));
        }

        List<Expression> projectList = ctx.selectClause().expression().stream().
                map(expr -> Preconditions.<Expression>checkNotNull(typedVisit(expr))).collect(Collectors.toList());
        return new Project(child, ImmutableList.copyOf(projectList));
    }

    @Override
    public Expression visitExpression(SqlBaseParser.ExpressionContext ctx) {
        return typedVisit(ctx.booleanExpression());
    }

    @Override
    public Expression visitComparison(SqlBaseParser.ComparisonContext ctx) {
        Expression left = Preconditions.checkNotNull(typedVisit(ctx.left));
        Expression right = Preconditions.checkNotNull(typedVisit(ctx.right));
        String op = ctx.comparisonOperator().getText();
        return new ScalarFunction(op, ImmutableList.of(left, right));
    }

    @Override
    public Literal visitConstantValue(SqlBaseParser.ConstantValueContext ctx) {
        return typedVisit(ctx.constant());
    }

    @Override
    public Literal visitStringLiteral(SqlBaseParser.StringLiteralContext ctx) {
        String value = removeQuotation(ctx.STRING().getText());
        return new Literal(StringType, value);
    }

    private String removeQuotation(String originText) {
        return StringUtils.substring(originText, 1, originText.length() - 1);
    }

    @Override
    public Literal visitBooleanLiteral(SqlBaseParser.BooleanLiteralContext ctx) {
        boolean value = Objects.isNull(ctx.booleanValue().FALSE_());
        return new Literal(BooleanType, value);
    }

    @Override
    public Literal visitIntegerLiteral(SqlBaseParser.IntegerLiteralContext ctx) {
        int value = Integer.valueOf(ctx.INTEGER_LITERAL().getText());
        value = Objects.isNull(ctx.MINUS()) ? value : -value;
        return new Literal(IntegerType, value);
    }

    @Override
    public Literal visitFloatLiteral(SqlBaseParser.FloatLiteralContext ctx) {
        float value = Float.valueOf(ctx.FLOAT_LITERAL().getText());
        value = Objects.isNull(ctx.MINUS()) ? value : -value;
        return new Literal(FloatType, value);
    }

    @Override
    public Attribute visitColumnWithTable(SqlBaseParser.ColumnWithTableContext ctx) {
        return new Attribute(ctx.tableIdentifier().getText(), ctx.identifier().getText());
    }

    @Override
    public Attribute visitColumnWithoutTable(SqlBaseParser.ColumnWithoutTableContext ctx) {
        return new Attribute(ctx.identifier().getText());
    }

    @Override
    public Relation visitTableIdentifierDefault(SqlBaseParser.TableIdentifierDefaultContext ctx) {
        return new Relation(ctx.tableName.getText());
    }

    @Override
    public Relation visitTableAlias(SqlBaseParser.TableAliasContext ctx) {
        return new Relation(ctx.tableName.getText(), ctx.alias.getText());
    }
}
