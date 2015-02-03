package org.semanticweb.ontop.owlrefplatform.core.queryevaluation;



public class AdpSQLDialectAdapter extends SQL99DialectAdapter {

    /**
     * same as PostgreSQL
     *
     */
    @Override
    public String sqlSlice(long limit, long offset) {
        if (limit < 0 || limit == 0) {
            if (offset < 0) {
                // If both limit and offset is not specified.
                return "LIMIT 0";
            } else {
                // if the limit is not specified
                return String.format("LIMIT ALL\nOFFSET %d", offset);
            }
        } else {
            if (offset < 0) {
                // If the offset is not specified
                return String.format("LIMIT %d\nOFFSET 0", limit);
            } else {
                return String.format("LIMIT %d\nOFFSET %d", limit, offset);
            }
        }
    }

}
