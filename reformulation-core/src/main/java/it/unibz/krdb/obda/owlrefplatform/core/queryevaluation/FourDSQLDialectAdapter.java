package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

import it.unibz.krdb.obda.model.OBDAQueryModifiers;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL dialect adapter for 4D.
 */
public class FourDSQLDialectAdapter extends SQL99DialectAdapter {

    /**
     * TODO: check their value more carefully.
     */
    private static Map<Integer, String> SqlDatatypes;
    static {
        SqlDatatypes = new HashMap<>();
        SqlDatatypes.put(Types.INTEGER, "INT");
        SqlDatatypes.put(Types.BIGINT, "INT");
        SqlDatatypes.put(Types.DECIMAL, "DECIMAL");
        SqlDatatypes.put(Types.REAL, "REAL");
        SqlDatatypes.put(Types.FLOAT, "FLOAT");
        SqlDatatypes.put(Types.DOUBLE, "DOUBLE PRECISION");
        SqlDatatypes.put(Types.CHAR, "VARCHAR");
        SqlDatatypes.put(Types.VARCHAR, "VARCHAR");
        SqlDatatypes.put(Types.TIME, "TIMESTAMP");
        // Not found
        SqlDatatypes.put(Types.TIMESTAMP, "DATETIME");
        // Not found
        SqlDatatypes.put(Types.DATE, "DATE");
    }

    @Override
    public String strconcat(String[] strings) {
        if (strings.length == 0)
            throw new IllegalArgumentException("Cannot concatenate 0 strings");

        if (strings.length == 1)
            return strings[0];

        StringBuilder sql = new StringBuilder();
        String str = "CONCAT("+strings[0]+","+strings[1]+")";
        if (strings.length == 2)
            return str;
        sql.append(concat(strings,str,2));
        return sql.toString();
    }

    private String concat(String[] strings,String str, int i){
        str = "CONCAT("+str+","+strings[i]+")";
        if (i < strings.length-1) {
            return concat(strings,str,i+1);
        }
        return str;
    }

    @Override
    public String sqlCast(String value, int type) {
        String strType = SqlDatatypes.get(type);
        if (strType != null) {
            return "CAST(" + value + " AS " + strType + ")";
        }
        throw new RuntimeException("Unsupported SQL type");
    }

    /**
     * 4D does not accept expressions like " NULL AS myVar ".
     */
    //@Override
    public String sqlNull() {
        return "0";
    }

    /**
     * 4D does not handle properly expressions like " 1 AS myVarType " (assigns "null" instead of 1).
     */
   // @Override
    public String sqlTypeCode(int code)
    {
        return String.format("CAST(%d as INT)", code);
    }

    @Override
    public String sqlOrderBy(List<OBDAQueryModifiers.OrderCondition> conditions, String viewname) {
        String sql = "ORDER BY ";
        boolean needComma = false;
        for (OBDAQueryModifiers.OrderCondition c : conditions) {
            if (needComma) {
                sql += ", ";
            }
            sql += 1;//"\""+c.getVariable()+"\"";
            if (c.getDirection() == OBDAQueryModifiers.OrderCondition.ORDER_DESCENDING) {
                sql += " DESC";
            }
            needComma = true;
        }
        return sql;
    }

}
