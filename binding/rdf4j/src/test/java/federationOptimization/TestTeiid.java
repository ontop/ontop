package federationOptimization;

import org.junit.Test;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

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
            Connection conn = DriverManager.getConnection("jdbc:teiid:homogeneous@mm://localhost:11000", "obdf", "obdfPwd0");
            Statement stmt = conn.createStatement();

           long start = System.currentTimeMillis();

            String relation1 = "select nr, publisher from ss2.review";
            String relation2 = "select nr, publisher from ss1.reviewc";
            String sql = "("+relation2+") except ("+relation1+")";
            //testQuery(stmt, sql);
            checkRedundancy(stmt, relation1, relation2);

            long end = System.currentTimeMillis();
            System.out.println("time used: "+(end-start));

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
