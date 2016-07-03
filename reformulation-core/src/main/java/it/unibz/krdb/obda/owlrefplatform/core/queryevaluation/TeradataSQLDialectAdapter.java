package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import it.unibz.krdb.obda.parser.EncodeForURI;

import java.sql.Types;
import java.util.Map;

public class TeradataSQLDialectAdapter extends SQL99DialectAdapter {

    static {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (Map.Entry<String, String> e : EncodeForURI.TABLE.entrySet()) {
            sb1.append("OREPLACE(");
            sb2.append(", '").append(e.getValue()).append("', '").append(e.getKey()).append("')");
        }
        ENCODE_FOR_URI_START = sb1.toString();
        ENCODE_FOR_URI_END = sb2.toString();
    }


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

    @Override
    public String strStartsOperator(){
        return "SUBSTR(%1$s, 1, LENGTH(%2$s)) LIKE %2$s";
    }

    @Override
    public String strEndsOperator(){
        return "SUBSTR(%1$s,LENGTH(%1$s) - LENGTH(%2$s) + 1, LENGTH(%2$s)) LIKE %2$s";
    }


}
