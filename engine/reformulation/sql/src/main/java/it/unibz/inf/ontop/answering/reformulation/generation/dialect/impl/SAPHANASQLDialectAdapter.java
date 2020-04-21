package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

public class SAPHANASQLDialectAdapter extends SQL99DialectAdapter {

    /**
     * https://help.sap.com/viewer/4fe29514fd584807ac9f2a04f6754767/2.0.03/en-US/20fcf24075191014a89e9dc7b8408b26.html
     *
     * LIMIT
     * Limits the number of records returned and behaves like TOP.
     * <limit> ::=
     *  LIMIT <unsigned_integer> [ OFFSET <unsigned_integer> ]
     *
     * The following example returns the first 3 records after skipping 5 records.
     * LIMIT 3 [OFFSET 5]
     *
     */

    // sqlLimit and sqlTopNSQL are standard

    @Override
    public String sqlLimitOffset(long limit, long offset) {
        return String.format("LIMIT %d\nOFFSET %d", limit, offset);
    }

    @Override
    public String sqlOffset(long offset) {
        return sqlLimitOffset(Integer.MAX_VALUE, offset);
    }
}
