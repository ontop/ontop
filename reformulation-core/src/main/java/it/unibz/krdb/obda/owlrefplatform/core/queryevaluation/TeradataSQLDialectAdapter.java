package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import java.sql.Types;

public class TeradataSQLDialectAdapter extends SQL99DialectAdapter {

    @Override
    public String sqlCast(String value, int type) {
        String strType = null;
        if (type == Types.VARCHAR) {
            strType = "VARCHAR(4000)";
        } else {
            throw new RuntimeException("Unsupported SQL type");
        }
        return "CAST(" + value + " AS " + strType + ")";
    }

    @Override
    public String sqlSlice(long limit, long offset) {
        if ((limit < 0) && (offset < 0)) {
            return "";
        }
        else if ((limit >= 0) && (offset >= 0)) {
            return String.format("LIMIT %d, %d", offset, limit);
        }
        else if (offset < 0) {
            return String.format("SAMPLE %d", limit);
        }
        // Else -> (limit < 0)
        else {
            return String.format("OFFSET %d", offset);
        }
    }

}
