package federationOptimization;

import federationOptimization.precomputation.RedundancyRelation;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Category(ObdfTest.class)
public class TestTeiid {

    public String checkRedundancy(Statement stmt, String relation1, String relation2) throws Exception{

        Set<String> ans1 = new HashSet<String>();
        Set<String> ans2 = new HashSet<String>();

        ResultSet rs1 = stmt.executeQuery(relation1);
        ResultSetMetaData rsmd1 = rs1.getMetaData();
        int column_count1 = rsmd1.getColumnCount();

        if(rs1.next()){
            while(rs1.next()){
                String res1 = "";
                for(int i=1; i<column_count1+1; i++){
                    res1 = res1+rs1.getString(i)+",";
                }
                ans1.add(res1);
            }
        } else {
            return RedundancyRelation.STRICT_CONTAINMENT.toString();
        }


        rs1.close();
        rs1 = null;

        ResultSet rs2 = stmt.executeQuery(relation2);
        ResultSetMetaData rsmd2 = rs2.getMetaData();
        int column_count2 = rsmd2.getColumnCount();

        if(rs2.next()){
            while(rs2.next()){
                String res2 = "";
                for(int i=1; i<column_count2+1; i++){
                    res2 = res2+rs2.getString(i)+",";
                }
                ans2.add(res2);
            }
        } else {
            return RedundancyRelation.STRICT_CONTAINMENT.toString();
        }

        rs2.close();
        rs2 = null;

        for(String str1: ans1){
            if(ans2.size() == 0){
                return RedundancyRelation.STRICT_CONTAINMENT.toString();
            } else if(!ans2.contains(str1)){
                return "";
            } else {
                ans2.remove(str1);
            }
        }

        if(ans2.size()==0){
            return RedundancyRelation.EQUIVALENCE.toString();
        } else{
            return RedundancyRelation.STRICT_CONTAINMENT.toString();
        }

    }

    public void testQuery(Statement stmt, String sql) throws Exception {
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
            System.out.println("query result not empty");
            break;
        }
    }


    @Test
    public void myTest(){
        try{
            Class.forName("org.teiid.jdbc.TeiidDriver");
            Connection conn = DriverManager.getConnection("jdbc:teiid:homogeneous@mm://obdalin.inf.unibz.it:40030", "obdf", "obdfPwd0");
            Statement stmt = conn.createStatement();

           long start = System.currentTimeMillis();

            String relation1 = "select nr, publisher from ss2.review";
            String relation2 = "select nr, publisher from ss1.reviewc";
            String sql = "("+relation2+") except ("+relation1+")";
            testQuery(stmt, sql);
            checkRedundancy(stmt, relation1, relation2);

//            sql = "insert into smatv.MatV_0 ( V1_nr, V1_label, V1_comment, V1_parent, V1_publisher, V1_publishdate, V2_product, V2_producttype) " +
//                    "SELECT * FROM (select nr, label, comment, parent, publisher, publishdate from ss3.producttype) AS V1, (select product, producttype from ss1.producttypeproduct1) AS V2 WHERE V1.parent=V2.producttype";
//            int b = stmt.executeUpdate(sql);
//            System.out.println("results of update: "+b);

//            String sql1 = "set schema smatv";
//            String sql2 = "CREATE FOREIGN TABLE MatV_0 ( V1_nr integer, V1_label string, V1_comment string, V1_parent string, V1_publisher integer, V1_publishdate date , V2_product integer, V2_producttype integer)";
//            int b1 = stmt.executeUpdate(sql1);
//            int b2 = stmt.executeUpdate(sql1);
//
//            System.out.println(b1+"--"+b2);

//              sql = "select * from smatv.MatV_0";
//              ResultSet rs = stmt.executeQuery(sql);
//              while(rs.next()){
//                  System.out.println("result set does not empty");
//                   break;
//              }

            long end = System.currentTimeMillis();
            System.out.println("time used: "+(end-start));

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
