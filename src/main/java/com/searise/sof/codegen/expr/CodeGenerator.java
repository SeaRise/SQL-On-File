package com.searise.sof.codegen.expr;

import com.searise.sof.expression.Expression;
import com.searise.sof.expression.Literal;
import com.searise.sof.expression.attribute.BoundReference;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class CodeGenerator {
    private static final String codeTemplate =
            "public Expression generate(DataType dataType, Expression[] params) {\n" +
                    "    return new CodegenExpression(dataType, params);\n" +
                    "}\n" +
                    "public class CodegenExpression implements Expression {\n" +
                    "    public final DataType dataType;\n" +
                    "    public final Expression[] params;\n" +
                    "    public CodegenExpression(DataType dataType, Expression[] params) {\n" +
                    "        this.dataType = dataType;\n" +
                    "        this.params = params;\n" +
                    "    }\n" +
                    "    public boolean resolved() {\n" +
                    "        return true;\n" +
                    "    }\n" +
                    "    public DataType dataType() {\n" +
                    "        return dataType;\n" +
                    "    }\n" +
                    "    public Object eval(InternalRow input) {\n" +
                    "        return %s;\n" +
                    "    }\n" +
                    "}";

    public static Expression gen(ExprCode exprCode) throws IOException, Scanner.ScanException, Parser.ParseException, CompileException {
        String importCode = genImportCode(exprCode.importClasses);

        String codeBody = exprCode.code;
        for (int i = 0; i < exprCode.paramNames.size(); i++) {
            codeBody = StringUtils.replace(codeBody, exprCode.paramNames.get(i), String.format("params[%d]", i));
        }

        String code = importCode + String.format(codeTemplate, codeBody);

        GeneratedClass clazz = (GeneratedClass) ClassBodyEvaluator.createFastClassBodyEvaluator(
                new Scanner(null, new StringReader(code)),
                GeneratedClass.class,
                null
        );

        Expression[] expressions = new Expression[exprCode.params.size()];
        return clazz.generate(exprCode.dataType, exprCode.params.toArray(expressions));
    }

    private static String genImportCode(List<Class> classes) {
        StringBuilder builder = new StringBuilder();
        String preImportCode =
                "import com.searise.sof.core.row.InternalRow;\n" +
                        "import com.searise.sof.expression.Expression;\n" +
                        "import com.searise.sof.type.*;\n";
        builder.append(preImportCode);
        for (Class aClass : classes) {
            builder.append("import ").append(aClass.getName()).append(";\n");
        }
        return builder.toString();
    }

    public static Expression tryCodegen(Expression expression) {
        if (expression.getClass() == BoundReference.class || expression.getClass() == Literal.class) {
            return expression;
        }
        try {
            return CodeGenerator.gen(expression.genCode(new CodegenContext()));
        } catch (Exception e) {
            return expression;
        }
    }
}
