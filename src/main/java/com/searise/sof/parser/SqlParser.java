package com.searise.sof.parser;

import com.searise.sof.plan.logic.LogicalPlan;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;

public class SqlParser {
    public LogicalPlan parsePlan(String sql) {
        SqlBaseLexer lexer = new SqlBaseLexer(new UpperCaseCharStream(CharStreams.fromString(sql)));
        BaseErrorListener baseErrorListener = new BaseErrorListener();
        lexer.addErrorListener(baseErrorListener);

        SqlBaseParser parser = new SqlBaseParser(new CommonTokenStream(lexer));
        parser.addErrorListener(baseErrorListener);
        SqlBaseParser.SingleStatementContext singleStatementContext = parser.singleStatement();
        AstBuilder visitor = new AstBuilder();
        return visitor.typedVisit(singleStatementContext);
    }

    private class UpperCaseCharStream implements CharStream {
        private final CodePointCharStream wrapped;
        UpperCaseCharStream(CodePointCharStream wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void consume() {
            wrapped.consume();
        }

        @Override
        public String getSourceName() {
            return wrapped.getSourceName();
        }

        @Override
        public int mark() {
            return wrapped.mark();
        }

        @Override
        public void release(int i) {
            wrapped.release(i);
        }

        @Override
        public int index() {
            return wrapped.index();
        }

        @Override
        public void seek(int i) {
            wrapped.seek(i);
        }

        @Override
        public int size() {
            return wrapped.size();
        }

        @Override
        public String getText(Interval interval) {
            return (size() > 0 && (interval.b - interval.a >= 0)) ? wrapped.getText(interval) : "";
        }

        @Override
        public int LA(int i) {
            int la = wrapped.LA(i);
            return (la == 0 || la == IntStream.EOF) ? la : Character.toUpperCase(la);
        }
    }
}
