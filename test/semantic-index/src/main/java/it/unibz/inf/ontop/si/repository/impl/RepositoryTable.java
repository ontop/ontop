package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

public class RepositoryTable {
    private final String tableName;
    private final String createCommand;
    private final String insertCommand;
    private final String selectCommand;

    RepositoryTable(String tableName, ImmutableMap<String, String> columnDefinitions, String selectColumns) {
        this.tableName = tableName;
        this.createCommand = "CREATE TABLE " + tableName +
                " ( " + Joiner.on(", ").withKeyValueSeparator(" ").join(columnDefinitions) + " )";
        this.insertCommand = "INSERT INTO " + tableName +
                " (" + Joiner.on(", ").join(columnDefinitions.keySet()) + ") VALUES ";
        this.selectCommand = "SELECT " + selectColumns + " FROM " + tableName;
    }

    String getINSERT(String values) {
        return insertCommand + "(" + values + ")";
    }

    String getSELECT(String filter) {
        return selectCommand +  " WHERE " + filter;
    }

    String getCREATE() { return createCommand; }

    String getDELETE() { return "DELETE FROM " + tableName; }

    String getEXISTS() { return String.format("SELECT 1 FROM %s WHERE 1=0", tableName); }
}
