package com.notmarra.notlib.database.result;

import java.util.ArrayList;
import java.util.List;

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.structure.NotRecord;
import com.notmarra.notlib.database.structure.NotTable;

public class NotSqlSelectResult extends NotSqlResult {
    private List<NotRecord> records = new ArrayList<>();

    public NotSqlSelectResult(NotDatabase database, NotTable table) {
        super(database, table);
    }

    public NotSqlSelectResult addRecord(NotRecord record) {
        records.add(record);
        return this;
    }

    public NotSqlSelectResult addRecords(List<NotRecord> records) {
        this.records.addAll(records);
        return this;
    }

    public List<NotRecord> getRecords() {
        return records;
    }

    public static NotSqlSelectResult of(NotDatabase database, NotTable table, List<NotRecord> records) {
        return new NotSqlSelectResult(database, table).addRecords(records);
    }
}
