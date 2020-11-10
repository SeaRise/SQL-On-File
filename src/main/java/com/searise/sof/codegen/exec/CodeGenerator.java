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
            "Executor generate(Executor child, List<ParamGenerator> paramGenerator) {\n" +
                    "    return new CodegenExec(child, paramGenerator);\n" +
                    "}\n" +
                    "public class CodegenExec implements Executor {\n" +
                    "    private final Executor child;\n" +
                    "    private final List<ParamGenerator> paramGenerators;\n" +
                    "\n" +
                    "    public CodegenExec(Executor child, List<ParamGenerator> paramGenerators) {\n" +
                    "        this.child = child;\n" +
                    "        this.paramGenerators = paramGenerators;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public RowIterator compute(int partition) {\n" +
                    "        RowIterator childRowIterator = child.compute(partition);\n" +
                    "        return new RowIterator() {\n" +
                    "            %s\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public void open() {\n" +
                    "                childRowIterator.open();\n" +
                    "            }\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public boolean hasNext() {\n" +
                    "                return childRowIterator.hasNext();\n" +
                    "            }\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public InternalRow next() {\n" +
                    "                InternalRow input = childRowIterator.next();\n" +
                    "                if (input == EMPTY_ROW) {\n" +
                    "                       return EMPTY_ROW;\n" +
                    "                }\n" +
                    "\n" +
                    "                %s\n" +
                    "\n" +
                    "                return %s;\n" +
                    "            }\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public void close() {\n" +
                    "                childRowIterator.close();\n" +
                    "            }\n" +
                    "        };\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public List<Executor> children() {\n" +
                    "        return ImmutableList.of(child);\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public Executor copyWithNewChildren(List<Executor> children) {\n" +
                    "        throw new UnsupportedOperationException();\n" +
                    "    }\n" +
                    "}\n";

    public static Executor gen(ExecCode execCode, Executor child) throws IOException, Scanner.ScanException, Parser.ParseException, CompileException {
        String importCode = genImportCode(Utils.combineDistinct(execCode.importClasses,
                Utils.toImmutableList(execCode.paramGenerators.stream().map(ParamGenerator::clazz))));

        String paramsCode = genParamsCode(execCode);
        String code = importCode + String.format(codeTemplate, paramsCode, execCode.code, execCode.output);

        GeneratedClass clazz = (GeneratedClass) ClassBodyEvaluator.createFastClassBodyEvaluator(
                new Scanner(null, new StringReader(code)),
                GeneratedClass.class,
                null
        );

        return clazz.generate(child, execCode.paramGenerators);
    }
    
    private static String genParamsCode(ExecCode execCode) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < execCode.paramGenerators.size(); i++) {
            ParamGenerator paramGenerator = execCode.paramGenerators.get(i);
            String type = paramGenerator.clazz().getSimpleName();
            String paramName = paramGenerator.name();
            // type paramName = (type) paramGenerators[i].gen();
            builder.append(type).append(" ").
                    append(paramName).append(" = (").
                    append(type).append(") paramGenerators[").
                    append(i).append("].gen();\n");
        }
        return builder.toString();
    }

    private static String genImportCode(List<Class> classes) {
        StringBuilder builder = new StringBuilder();
        String preImportCode =
                "import com.google.common.collect.ImmutableList;\n" +
                        "import com.searise.sof.core.row.InternalRow;\n" +
                        "import com.searise.sof.execution.Executor;\n" +
                        "import com.searise.sof.execution.RowIterator;\n" +
                        "import java.util.List;\n" +
                        "import static com.searise.sof.core.row.EmptyRow.EMPTY_ROW;\n";
        builder.append(preImportCode);
        for (Class aClass : classes) {
            builder.append("import ").append(aClass.getName()).append(";\n");
        }
        return builder.toString();
    }
}
