package com.ctrip.framework.drc.applier.activity.monitor.entity;


import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName ConflictTable
 * @Author haodongPan
 * @Date 2023/6/7 15:32
 * @Version: $
 */
public class ConflictTable {
    
    private String db;
    private String table;
    private int conflictRes;   // 0-commit,1-rollback


    public Map<String,String> generateTags() {
        Map<String,String> tags = Maps.newHashMap();
        tags.put("db",db);
        tags.put("table",table);
        return tags;
    }
    
    
    public ConflictTable() {
    }

    public ConflictTable(String db, String table, int type) {
        this.db = db;
        this.table = table;
        this.conflictRes = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConflictTable that = (ConflictTable) o;
        return Objects.equals(db, that.db) && Objects.equals(table, that.table) && conflictRes == that.getConflictRes();
    }

    @Override
    public int hashCode() {
        return Objects.hash(db, table, conflictRes);
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getConflictRes() {
        return conflictRes;
    }

    public void setConflictRes(int conflictRes) {
        this.conflictRes = conflictRes;
    }
}
