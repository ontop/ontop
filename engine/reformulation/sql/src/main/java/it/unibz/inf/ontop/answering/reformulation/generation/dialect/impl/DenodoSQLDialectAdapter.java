package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class DenodoSQLDialectAdapter extends SQL99DialectAdapter {

    @Override
    public String sqlSlice(long limit, long offset) {
        if(limit == 0){
            return "WHERE 1 = 0";
        }

        if (limit < 0)  {
            if (offset < 0 ) {
                return "";
            } else {

                return String.format("OFFSET %d ROWS", offset);
            }
        } else {
            if (offset < 0) {
                // If the offset is not specified
                return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
            } else {
                return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }
        }
    }
    @Override
    public String getTopNSQL(String sqlString, int top) {
        String slice = String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", top);
        return String.format("%s %s", sqlString, slice);
    }

}
