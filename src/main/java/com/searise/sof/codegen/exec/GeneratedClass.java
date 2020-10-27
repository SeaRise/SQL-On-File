package com.searise.sof.codegen.exec;

import com.searise.sof.execution.Executor;

import java.util.List;

public interface GeneratedClass {
    Executor generate(Executor child, List<Object> params);
}
