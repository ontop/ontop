package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class SAPHANASQLDialectAdapter extends SQL99DialectAdapter {

    @Override
    public String sqlSlice(long limit, long offset) {
        if (limit < 0 || limit == 0) {
            if (offset < 0) {
                // If both limit and offset is not specified.
                return "";
            } else {
                // The max number of rows is specified by the development team.
                return String.format("LIMIT 2147483647\nOFFSET %d", offset);
            }
        } else {
            if (offset < 0) {
                // If the offset is not specified
                return String.format("LIMIT %d\n", limit);
            } else {
                return String.format("LIMIT %d\nOFFSET %d", limit, offset);
            }
        }
    }
}
