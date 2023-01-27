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


    @Test
    public void myTest(){
        try{
            Class.forName("org.teiid.jdbc.TeiidDriver");
            Connection conn = DriverManager.getConnection("jdbc:teiid:homogeneous@mm://localhost:11000", "obdf", "obdfPwd0");
            Statement stmt = conn.createStatement();

//            relation1: SELECT nr, propertytex4  from ss1.product1
//            relation2: SELECT nr, propertytex4  from ss5.product2
//            checking:
//            relation1: SELECT product, producttype  from ss1.producttypeproduct1
//            relation2: SELECT product, producttype  from ss5.producttypeproduct2
//            checking:
//            relation1: SELECT nr, propertytex3  from ss1.product1
//            relation2: SELECT nr, propertytex3  from ss5.product2


            String relation1 = "SELECT product, producttype  from ss1.producttypeproduct1";
            String relation2 = "SELECT product, producttype  from ss5.producttypeproduct2";
            String relation = checkRedundancy(stmt, relation1, relation2);
            System.out.println("a-"+relation+"?b");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
