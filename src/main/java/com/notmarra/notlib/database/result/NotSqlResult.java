package com.notmarra.notlib.database.result;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotTable;

public class NotSqlResult {
    final NotDatabase database;
    final NotTable table;
    
    public NotSqlResult(NotDatabase database, NotTable table) {
        this.database = database;
        this.table = table;
    }
}
