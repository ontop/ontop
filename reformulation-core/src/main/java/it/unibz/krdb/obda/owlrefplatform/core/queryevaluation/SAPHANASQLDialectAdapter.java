package it.unibz.krdb.obda.owlrefplatform.core.queryevaluation;

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

//    @Override
//    public String strStartsOperator(){
//        return "LEFT(%1$s, LENGTH(%2$s)) LIKE %2$s";
//    }
//
//    @Override
//    public String strContainsOperator(){
//        return "LOCATE(%2$s , %1$s) > 0";
//    }
//
//    @Override
//    public String strBefore(String str, String before) {
//        return String.format("LEFT(%s,SIGN(LOCATE(%s,%s)) * (LOCATE(%s,%s)-1))", str,   before, str, before, str);
//    }
//
//    @Override
//    public String strAfter(String str, String after) {
//        //rtrim is needed to remove the space in
//        return String.format("RTRIM(SUBSTR(%s,LOCATE(%s,%s)+LENGTH(%s), SIGN(LOCATE(%s,%s))*LENGTH(%s)))",
//                str, after, str , after, after, str, str);
//    }

}
