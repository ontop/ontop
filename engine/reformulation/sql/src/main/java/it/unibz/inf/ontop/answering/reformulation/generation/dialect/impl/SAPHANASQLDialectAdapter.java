package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class SAPHANASQLDialectAdapter extends SQL99DialectAdapter {

    @Override
    public String sqlLimitOffset(long limit, long offset) {
        return String.format("LIMIT %d\nOFFSET %d", limit, offset);
    }

    @Override
    public String sqlLimit(long limit) {
        return String.format("LIMIT %d\n", limit);
    }

    @Override
    public String sqlOffset(long offset) {
        return String.format("LIMIT 2147483647\nOFFSET %d", offset);
    }


    // standard
    // public String getTopNSQL(String sqlString, int top) {
    //    String slice = String.format("LIMIT %d", top);
    //    return String.format("%s %s", sqlString, slice);
    //}
}
