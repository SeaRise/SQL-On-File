package com.searise.sof.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.common.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.Literal;
import com.searise.sof.expression.ScalarFunction;
import com.searise.sof.expression.UnresolvedAttribute;
import com.searise.sof.plan.logic.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.searise.sof.type.DataType.*;

public class AstBuilder extends SqlBaseBaseVisitor<Object> {

    @SuppressWarnings("unchecked")
    public <V> V typedVisit(ParseTree ctx) {
        return (V) Preconditions.checkNotNull(ctx.accept(this));
    }

    @Override
    public LogicalPlan visitSingleStatement(SqlBaseParser.SingleStatementContext ctx) {
        return typedVisit(ctx.statement());
    }

    @Override
    public LogicalPlan visitSelectStatement(SqlBaseParser.SelectStatementContext ctx) {
        LogicalPlan child = typedVisit(ctx.fromCluse());
        if (Objects.nonNull(ctx.whereCluse())) {
            Expression condition = typedVisit(ctx.whereCluse().expression());
            child = new Filter(ImmutableList.of(condition), child);
        }

        List<Expression> projectList = Utils.toImmutableList(
                ctx.selectClause().expression().stream().map(this::typedVisit));
        return new Project(projectList, child);
    }

    @Override
    public LogicalPlan visitFromCluse(SqlBaseParser.FromCluseContext ctx) {
        return typedVisit(ctx.unresolvedRelation());
    }

    @Override
    public LogicalPlan visitUnresolvedRelation(SqlBaseParser.UnresolvedRelationContext ctx) {
        LogicalPlan relation = typedVisit(ctx.relationPrimary());
        if (Objects.nonNull(ctx.joinRelation()) && !ctx.joinRelation().isEmpty()) {
            for (SqlBaseParser.JoinRelationContext joinRelationContext : ctx.joinRelation()) {
                LogicalPlan right = typedVisit(joinRelationContext.right);
                List<Expression> conditions =
                        Objects.isNull(joinRelationContext.joinCriteria()) ?
                                ImmutableList.of() :
                                ImmutableList.of(typedVisit(joinRelationContext.joinCriteria().booleanExpression()));
                relation = new InnerJoin(relation, right, conditions);
            }
        }
        return relation;
    }

    @Override
    public LogicalPlan visitRelationPrimary(SqlBaseParser.RelationPrimaryContext ctx) {
        if (Objects.nonNull(ctx.selectStatement())) {
            String subqueryName = ctx.identifier().getText();
            Preconditions.checkArgument(StringUtils.isNotBlank(subqueryName));
            return new SubqueryAlias(subqueryName, typedVisit(ctx.selectStatement()));
        }

        Preconditions.checkArgument(Objects.nonNull(ctx.tableIdentifier()) && ctx.tableIdentifier().size() > 0);
        LogicalPlan plan = typedVisit(ctx.tableIdentifier(0));
        for (int i = 1; i < ctx.tableIdentifier().size(); i++) {
            plan = new InnerJoin(plan, typedVisit(ctx.tableIdentifier(i)));
        }
        return plan;
    }

    @Override
    public Expression visitExpression(SqlBaseParser.ExpressionContext ctx) {
        return typedVisit(ctx.booleanExpression());
    }

    @Override
    public Expression visitComparison(SqlBaseParser.ComparisonContext ctx) {
        Expression left = typedVisit(ctx.left);
        Expression right = typedVisit(ctx.right);
        String op = ctx.comparisonOperator().getText();
        return new ScalarFunction(op, ImmutableList.of(left, right));
    }

    @Override
    public Expression visitParenthesizedExpression(SqlBaseParser.ParenthesizedExpressionContext ctx) {
        return typedVisit(ctx.expression());
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
    public UnresolvedAttribute visitColumnWithTable(SqlBaseParser.ColumnWithTableContext ctx) {
        return new UnresolvedAttribute(ctx.tableIdentifier().getText(), ctx.identifier().getText());
    }

    @Override
    public UnresolvedAttribute visitColumnWithoutTable(SqlBaseParser.ColumnWithoutTableContext ctx) {
        return new UnresolvedAttribute(ctx.identifier().getText());
    }

    @Override
    public UnresolvedRelation visitTableIdentifierDefault(SqlBaseParser.TableIdentifierDefaultContext ctx) {
        return new UnresolvedRelation(ctx.tableName.getText());
    }

    @Override
    public UnresolvedRelation visitTableAlias(SqlBaseParser.TableAliasContext ctx) {
        return new UnresolvedRelation(ctx.tableName.getText(), Optional.of(ctx.alias.getText()));
    }

    @Override
    public Expression visitLogicalBinary(SqlBaseParser.LogicalBinaryContext ctx) {
        Expression left = typedVisit(ctx.left);
        Expression right = typedVisit(ctx.right);
        String op = ctx.opt.getText();
        return new ScalarFunction(op, ImmutableList.of(left, right));
    }

    @Override
    public Expression visitLogicalNot(SqlBaseParser.LogicalNotContext ctx) {
        return new ScalarFunction("not", ImmutableList.of(typedVisit(ctx.booleanExpression())));
    }
}
