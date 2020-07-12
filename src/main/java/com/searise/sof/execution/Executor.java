package com.searise.sof.execution;

import com.searise.sof.core.row.InternalRow;

public interface Executor {
     void open();
     boolean hasNext();
     InternalRow next();
     void close();
}
