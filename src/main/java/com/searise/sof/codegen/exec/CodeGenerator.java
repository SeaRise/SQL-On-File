package com.searise.sof.codegen.exec;

import com.searise.sof.core.Utils;
import com.searise.sof.execution.Executor;
import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class CodeGenerator {
    private static final String codeTemplate =
            "Executor generate(Executor child, List<Object> params) {\n" +
                    "    return new CodegenExec(child, params);\n" +
                    "}\n" +
                    "public class CodegenExec implements Executor {\n" +
                    "    private final Executor child;\n" +
                    "    private final List<Object> params;;\n" +
                    "\n" +
                    "    %s\n" +
                    "\n" +
                    "    public CodegenExec(Executor child, List<Object> params) {\n" +
                    "        this.child = child;\n" +
                    "        this.params = params;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public void open() {\n" +
                    "        child.open();\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public boolean hasNext() {\n" +
                    "        return child.hasNext();\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public InternalRow next() {\n" +
                    "        InternalRow input = child.next();\n" +
                    "        if (input == EMPTY_ROW) {\n" +
                    "            return EMPTY_ROW;\n" +
                    "        }\n" +
                    "\n" +
                    "        %s\n" +
                    "\n" +
                    "        return %s;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public void close() {\n" +
                    "        child.close();\n" +
                    "    }\n" +
                    "}\n";

    public static Executor gen(ExecCode execCode, Executor child) throws IOException, Scanner.ScanException, Parser.ParseException, CompileException {
        String importCode = genImportCode(Utils.combineDistinct(execCode.importClasses, execCode.paramClasses));

        String paramsCode = genParamsCode(execCode);
        String code = importCode + String.format(codeTemplate, paramsCode, execCode.code, execCode.output);

        GeneratedClass clazz = (GeneratedClass) ClassBodyEvaluator.createFastClassBodyEvaluator(
                new Scanner(null, new StringReader(code)),
                GeneratedClass.class,
                null
        );

        return clazz.generate(child, execCode.params);
    }

    private static String genParamsCode(ExecCode execCode) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < execCode.paramNames.size(); i++) {
            String type = execCode.paramClasses.get(i).getSimpleName();
            String paramName = execCode.paramNames.get(i);
            // type paramName = (type) params[i];
            builder.append(type).append(" ").
                    append(paramName).append(" = (").
                    append(type).append(") params[").
                    append(i).append("];\n");
        }
        return builder.toString();
    }

    private static String genImportCode(List<Class> classes) {
        StringBuilder builder = new StringBuilder();
        String preImportCode =
                "import com.searise.sof.core.row.InternalRow;\n" +
                        "import com.searise.sof.execution.Executor;" +
                        "import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;";
        builder.append(preImportCode);
        for (Class aClass : classes) {
            builder.append("import ").append(aClass.getName()).append(";\n");
        }
        return builder.toString();
    }
}
