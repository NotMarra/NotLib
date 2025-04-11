package com.notmarra.notlib.database.result;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotRecord;
import com.notmarra.notlib.database.structure.NotTable;

public class NotSqlSelectOneResult extends NotSqlResult {
    private NotRecord record = null;

    public NotSqlSelectOneResult(NotDatabase database, NotTable table) {
        super(database, table);
    }

    public NotSqlSelectOneResult setRecord(NotRecord record) {
        this.record = record;
        return this;
    }

    public NotRecord getRecord() {
        return record;
    }

    public static NotSqlSelectOneResult of(NotDatabase database, NotTable table, NotRecord record) {
        return new NotSqlSelectOneResult(database, table).setRecord(record);
    }
}
