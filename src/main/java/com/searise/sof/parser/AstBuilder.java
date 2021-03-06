package com.searise.sof.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.searise.sof.catalog.CatalogTable;
import com.searise.sof.catalog.StructField;
import com.searise.sof.core.SofContext;
import com.searise.sof.core.Utils;
import com.searise.sof.expression.Expression;
import com.searise.sof.expression.Literal;
import com.searise.sof.expression.ScalarFunction;
import com.searise.sof.expression.attribute.Alias;
import com.searise.sof.expression.attribute.UnresolvedAttribute;
import com.searise.sof.plan.logic.*;
import com.searise.sof.plan.runnable.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.searise.sof.type.DataType.*;

public class AstBuilder extends SqlBaseBaseVisitor<Object> {
    private static final String DEFAULT_SEPARATOR = ",";
    private final SofContext context;

    public AstBuilder(SofContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    <V> V typedVisit(ParseTree ctx) {
        return (V) Preconditions.checkNotNull(ctx.accept(this));
    }

    @Override
    public LogicalPlan visitSingleStatement(SqlBaseParser.SingleStatementContext ctx) {
        return typedVisit(ctx.statement());
    }

    @Override
    public LogicalPlan visitCreateTableAsSelect(SqlBaseParser.CreateTableAsSelectContext ctx) {
        String table = ctx.tablenName.getText();
        Pair<String, String> fileMeta = visitFileMetaClause(ctx.fileMetaClause());
        String filePath = fileMeta.getLeft();
        checkFilePath(filePath);
        String separator = fileMeta.getRight();
        LogicalPlan query = typedVisit(ctx.selectStatement());
        return new CreateTableAsSelect(context, table, filePath, separator, query);
    }

    @Override
    public LogicalPlan visitInsertOverwrite(SqlBaseParser.InsertOverwriteContext ctx) {
        LogicalPlan query = typedVisit(ctx.selectStatement());
        return new InsertOverwrite(context, ctx.tablenName.getText(), query);
    }

    @Override
    public LogicalPlan visitSetConf(SqlBaseParser.SetConfContext ctx) {
        String key = ctx.key.getText();
        String value = ctx.value.getText();
        return new SetCommand(key, value, context);
    }

    @Override
    public LogicalPlan visitShowTable(SqlBaseParser.ShowTableContext ctx) {
        return new ShowTable(context);
    }

    @Override
    public LogicalPlan visitCreateStatement(SqlBaseParser.CreateStatementContext ctx) {
        String tableName = ctx.tablenName.getText();

        Preconditions.checkArgument(ctx.dataType().size() == ctx.identifier().size());
        List<SqlBaseParser.DataTypeContext> dataTypes = ctx.dataType();
        List<SqlBaseParser.IdentifierContext> identifiers = ctx.identifier();
        ImmutableList.Builder<StructField> structTypeBuilder = ImmutableList.builder();
        for (int i = 0; i < dataTypes.size(); i++) {
            StructField structField = new StructField(identifiers.get(i).getText(), getType(dataTypes.get(i).getText()));
            structTypeBuilder.add(structField);
        }

        Pair<String, String> fileMeta = visitFileMetaClause(ctx.fileMetaClause());
        String filePath = fileMeta.getLeft();
        String separator = fileMeta.getRight();
        checkFilePath(filePath);
        CatalogTable catalogTable = new CatalogTable(tableName, structTypeBuilder.build(), filePath, separator);
        return new CreateTable(catalogTable, context);
    }

    private void checkFilePath(String filePath) {
        File file = new File(filePath);
        Utils.checkArgument(!file.exists() || file.isDirectory(), "filePath must be directory");
    }

    @Override
    public Pair<String, String> visitFileMetaClause(SqlBaseParser.FileMetaClauseContext fileMeta) {
        String filePath = removeQuotation(fileMeta.filePathClause().path.getText());
        String separator = Objects.isNull(fileMeta.separatorClause()) ?
                DEFAULT_SEPARATOR : removeQuotation(fileMeta.separatorClause().separator.getText());
        return Pair.of(filePath, separator);
    }

    @Override
    public LogicalPlan visitSelectStatement(SqlBaseParser.SelectStatementContext ctx) {
        LogicalPlan child = typedVisit(ctx.fromCluse());
        if (Objects.nonNull(ctx.whereCluse())) {
            Expression condition = typedVisit(ctx.whereCluse().expression());
            child = new Filter(ImmutableList.of(condition), child, context);
        }

        List<Expression> projectList = Utils.toImmutableList(
                ctx.selectClause().expression().stream().map(this::typedVisit));
        return new Project(projectList, child, context);
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
                relation = new InnerJoin(relation, right, conditions, context);
            }
        }
        return relation;
    }

    @Override
    public LogicalPlan visitRelationPrimary(SqlBaseParser.RelationPrimaryContext ctx) {
        if (Objects.nonNull(ctx.selectStatement())) {
            String subqueryName = ctx.identifier().getText();
            Preconditions.checkArgument(StringUtils.isNotBlank(subqueryName));
            return new SubqueryAlias(subqueryName, typedVisit(ctx.selectStatement()), context);
        }

        Preconditions.checkArgument(Objects.nonNull(ctx.tableIdentifier()) && ctx.tableIdentifier().size() > 0);
        LogicalPlan plan = typedVisit(ctx.tableIdentifier(0));
        for (int i = 1; i < ctx.tableIdentifier().size(); i++) {
            plan = new InnerJoin(plan, typedVisit(ctx.tableIdentifier(i)), context);
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
    public Literal visitDoubleLiteral(SqlBaseParser.DoubleLiteralContext ctx) {
        double value = Double.valueOf(ctx.DOUBLE_LITERAL().getText());
        value = Objects.isNull(ctx.MINUS()) ? value : -value;
        return new Literal(DoubleType, value);
    }

    @Override
    public Expression visitAlias(SqlBaseParser.AliasContext ctx) {
        String name = ctx.identifier().getText();
        Expression child = typedVisit(ctx.primaryExpression());
        return new Alias(new UnresolvedAttribute(name), child);
    }

    @Override
    public UnresolvedAttribute visitColumnWithTable(SqlBaseParser.ColumnWithTableContext ctx) {
        return new UnresolvedAttribute(Optional.of(ctx.tableIdentifier().getText()), ctx.identifier().getText());
    }

    @Override
    public UnresolvedAttribute visitColumnWithoutTable(SqlBaseParser.ColumnWithoutTableContext ctx) {
        return new UnresolvedAttribute(ctx.identifier().getText());
    }

    @Override
    public UnresolvedRelation visitTableIdentifierDefault(SqlBaseParser.TableIdentifierDefaultContext ctx) {
        return new UnresolvedRelation(ctx.tableName.getText(), context);
    }

    @Override
    public UnresolvedRelation visitTableAlias(SqlBaseParser.TableAliasContext ctx) {
        return new UnresolvedRelation(ctx.tableName.getText(), Optional.of(ctx.alias.getText()), context);
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
        return new ScalarFunction(ctx.NOT().getText(), ImmutableList.of(typedVisit(ctx.booleanExpression())));
    }

    @Override
    public Expression visitArithmeticUnary(SqlBaseParser.ArithmeticUnaryContext ctx) {
        Expression child = typedVisit(ctx.valueExpression());
        return new ScalarFunction(ctx.MINUS().getText(), ImmutableList.of(child));
    }

    @Override
    public Expression visitArithmeticBinary(SqlBaseParser.ArithmeticBinaryContext ctx) {
        Expression left = typedVisit(ctx.left);
        Expression right = typedVisit(ctx.right);
        String op = ctx.opt.getText();
        return new ScalarFunction(op, ImmutableList.of(left, right));
    }
}
