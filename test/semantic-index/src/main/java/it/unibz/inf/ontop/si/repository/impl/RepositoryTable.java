package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RepositoryTable {
    private final String tableName, createCommand, insertCommand, selectCommand;

    RepositoryTable(String tableName, ImmutableMap<String, String> columnDefinitions, String selectList) {
        this.tableName = tableName;
        this.createCommand = "CREATE TABLE " + tableName +
                columnDefinitions.entrySet().stream()
                        .map(e -> e.getKey() + " " + e.getValue())
                        .collect(Collectors.joining(", ", " (", ")"));
        this.insertCommand = "INSERT INTO " + tableName +
                columnDefinitions.keySet().stream()
                        .collect(Collectors.joining(", ", " (", ") VALUES "));
        this.selectCommand = "SELECT " +  selectList + " FROM " + tableName + " WHERE ";
    }

    public static String getSelectListOf(String ... selectColumns) {
        return IntStream.range(0, selectColumns.length)
                .mapToObj(i -> selectColumns[i] + " as " + MappingProvider.MAPPING_VARIBLES.get(i))
                .collect(Collectors.joining(", "));
    }

    public static String getNotNull(String type) {
        return type + " NOT NULL";
    }

    public static String getStringLiteral(String v) {
        return "'" + v + "'";
    }

    public static String getBooleanLiteral(boolean b) {
        return b ? "TRUE" : "FALSE";
    }


    public static String getEq(String v1, String v2) {
        return v1 + " = " + v2;
    }

    String getINSERT(String values) {
        return insertCommand + "(" + values + ")";
    }

    String getSELECT(ImmutableList<String> filter) {
        return selectCommand + String.join(" AND ", filter);
    }

    String getCREATE() { return createCommand; }

    String getEXISTS() { return String.format("SELECT 1 FROM %s WHERE 1=0", tableName); }
}
