package com.notmarra.notlib.database.result;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotTable;

public class NotSqlResult {
    private final NotDatabase db;
    private final NotTable table;
    
    public NotSqlResult(NotDatabase db, NotTable table) {
        this.db = db;
        this.table = table;
    }

    public NotDatabase db() { return db; }
    public NotTable table() { return table; }
}
