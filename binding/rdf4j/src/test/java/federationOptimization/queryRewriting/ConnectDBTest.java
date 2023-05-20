package federationOptimization.queryRewriting;

import org.junit.Test;
import org.teiid.client.plan.PlanNode;
import org.teiid.jdbc.TeiidStatement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectDBTest {

    @Test
    public void testConnectTeiid(){
        try{
            Class.forName("org.teiid.jdbc.TeiidDriver");
            Connection conn = DriverManager.getConnection("jdbc:teiid:homogeneous@mm://localhost:11000", "obdf", "obdfPwd0");
            Statement stmt = conn.createStatement();
            String sql = "SELECT DISTINCT v9.\"label10m46\" AS \"label10m46\", v9.\"product0m2\" AS \"product0m2\"\n" +
                    "FROM ((SELECT v1.\"label\" AS \"label10m46\", v1.\"nr\" AS \"product0m2\"\n" +
                    "FROM \"ss1\".\"product1\" v1, \"ss1\".\"productfeatureproduct1\" v2, \"ss1\".\"productfeatureproduct1\" v3\n" +
                    "WHERE ((v1.\"propertynum1\" < 1000) AND v1.\"label\" IS NOT NULL AND v1.\"propertynum1\" IS NOT NULL AND v1.\"nr\" = v2.\"product\" AND v1.\"nr\" = v3.\"product\" AND 89 = v2.\"productfeature\" AND 91 = v3.\"productfeature\")\n" +
                    ")UNION ALL \n" +
                    "(SELECT v5.\"label\" AS \"label10m46\", v5.\"nr\" AS \"product0m2\"\n" +
                    "FROM \"ss5\".\"product2\" v5, \"ss5\".\"productfeatureproduct2\" v6, \"ss5\".\"productfeatureproduct2\" v7\n" +
                    "WHERE ((v5.\"propertynum1\" < 1000) AND v5.\"label\" IS NOT NULL AND v5.\"propertynum1\" IS NOT NULL AND v5.\"nr\" = v6.\"product\" AND v5.\"nr\" = v7.\"product\" AND 89 = v6.\"productfeature\" AND 91 = v7.\"productfeature\")\n" +
                    ")) v9";
//            ResultSet rs = stmt.executeQuery(sql);
//            if(rs.next()){
//                System.out.println("has answers");
//            }

            stmt.execute("set showplan on");
			ResultSet rs = stmt.executeQuery(sql);
			TeiidStatement tstatement = stmt.unwrap(TeiidStatement.class);
			PlanNode queryPlan = tstatement.getPlanDescription();
			System.out.println(queryPlan);


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testConnectDremio(){
        try{

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testConnectPostgreSQL(){
        try{
            String driver = "org.postgresql.Driver";
            String url = "jdbc:postgresql://localhost:10008/sc2";
            String user = "obdf";
            String password = "obdfPwd0";

            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement st = conn.createStatement();

            String sql1 = "CREATE TABLE \"of_prod1\"(\n" +
                    "\"1_0\" int4,\n" +
                    "\"1_1\" int4,\n" +
                    "\"1_2\" int4,\n" +
                    "\"1_3\" double precision,\n" +
                    "\"1_4\" date,\n" +
                    "\"1_5\" date,\n" +
                    "\"1_6\" int4,\n" +
                    "\"1_7\" varchar,\n" +
                    "\"1_8\" int4,\n" +
                    "\"1_9\" date,\n" +
                    "\"2_0\" int4 not null,\n" +
                    "\"2_1\" varchar,\n" +
                    "\"2_2\" varchar,\n" +
                    "\"2_3\" int4,\n" +
                    "\"2_4\" int4,\n" +
                    "\"2_5\" int4,\n" +
                    "\"2_6\" int4,\n" +
                    "\"2_7\" int4,\n" +
                    "\"2_8\" int4,\n" +
                    "\"2_9\" int4,\n" +
                    "\"2_10\" varchar,\n" +
                    "\"2_11\" varchar,\n" +
                    "\"2_12\" varchar,\n" +
                    "\"2_13\" varchar,\n" +
                    "\"2_14\" varchar,\n" +
                    "\"2_15\" varchar,\n" +
                    "\"2_16\" int4,\n" +
                    "\"2_17\" date\n" +
                    ")";
            String sql2 = "CREATE TABLE \"of_prod2\"(\n" +
                    "\"1_0\" int4 not null,\n" +
                    "\"1_1\" int4,\n" +
                    "\"1_2\" int4,\n" +
                    "\"1_3\" double precision,\n" +
                    "\"1_4\" date,\n" +
                    "\"1_5\" date,\n" +
                    "\"1_6\" int4,\n" +
                    "\"1_7\" varchar,\n" +
                    "\"1_8\" int4,\n" +
                    "\"1_9\" date,\n" +
                    "\"2_0\" int4 not null,\n" +
                    "\"2_1\" varchar,\n" +
                    "\"2_2\" varchar,\n" +
                    "\"2_3\" int4,\n" +
                    "\"2_4\" int4,\n" +
                    "\"2_5\" int4,\n" +
                    "\"2_6\" int4,\n" +
                    "\"2_7\" int4,\n" +
                    "\"2_8\" int4,\n" +
                    "\"2_9\" int4,\n" +
                    "\"2_10\" varchar,\n" +
                    "\"2_11\" varchar,\n" +
                    "\"2_12\" varchar,\n" +
                    "\"2_13\" varchar,\n" +
                    "\"2_14\" varchar,\n" +
                    "\"2_15\" varchar,\n" +
                    "\"2_16\" int4,\n" +
                    "\"2_17\" date\n" +
                    ")";
            String sql3 = "CREATE TABLE \"pf_pfp1\"(\n" +
                    "\"1_0\" int4,\n" +
                    "\"1_1\" varchar,\n" +
                    "\"1_2\" varchar,\n" +
                    "\"1_3\" int4,\n" +
                    "\"1_4\" date,\n" +
                    "\"2_0\" int4,\n" +
                    "\"2_1\" int4\n" +
                    ")";
            String sql4 = "CREATE TABLE \"pf_pfp2\"(\n" +
                    "\"1_0\" int4,\n" +
                    "\"1_1\" varchar,\n" +
                    "\"1_2\" varchar,\n" +
                    "\"1_3\" int4,\n" +
                    "\"1_4\" date,\n" +
                    "\"2_0\" int4,\n" +
                    "\"2_1\" int4\n" +
                    ")";
            String sql5 = "CREATE TABLE \"pro1_prod\"(\n" +
                    "\"1_0\" int4 not null,\n" +
                    "\"1_1\" varchar,\n" +
                    "\"1_2\" varchar,\n" +
                    "\"1_3\" int4,\n" +
                    "\"1_4\" int4,\n" +
                    "\"1_5\" int4,\n" +
                    "\"1_6\" int4,\n" +
                    "\"1_7\" int4,\n" +
                    "\"1_8\" int4,\n" +
                    "\"1_9\" int4,\n" +
                    "\"1_10\" varchar,\n" +
                    "\"1_11\" varchar,\n" +
                    "\"1_12\" varchar,\n" +
                    "\"1_13\" varchar,\n" +
                    "\"1_14\" varchar,\n" +
                    "\"1_15\" varchar,\n" +
                    "\"1_16\" int4,\n" +
                    "\"1_17\" date,\n" +
                    "\"2_0\" int4,\n" +
                    "\"2_1\" varchar,\n" +
                    "\"2_2\" varchar,\n" +
                    "\"2_3\" varchar,\n" +
                    "\"2_4\" varchar,\n" +
                    "\"2_5\" int4,\n" +
                    "\"2_6\" date\n" +
                    ")";
            String sql6 = "CREATE TABLE \"pro2_prod\"(\n" +
                    "\"1_0\" int4 not null,\n" +
                    "\"1_1\" varchar,\n" +
                    "\"1_2\" varchar,\n" +
                    "\"1_3\" int4,\n" +
                    "\"1_4\" int4,\n" +
                    "\"1_5\" int4,\n" +
                    "\"1_6\" int4,\n" +
                    "\"1_7\" int4,\n" +
                    "\"1_8\" int4,\n" +
                    "\"1_9\" int4,\n" +
                    "\"1_10\" varchar,\n" +
                    "\"1_11\" varchar,\n" +
                    "\"1_12\" varchar,\n" +
                    "\"1_13\" varchar,\n" +
                    "\"1_14\" varchar,\n" +
                    "\"1_15\" varchar,\n" +
                    "\"1_16\" int4,\n" +
                    "\"1_17\" date,\n" +
                    "\"2_0\" int4,\n" +
                    "\"2_1\" varchar,\n" +
                    "\"2_2\" varchar,\n" +
                    "\"2_3\" varchar,\n" +
                    "\"2_4\" varchar,\n" +
                    "\"2_5\" int4,\n" +
                    "\"2_6\" date\n" +
                    ")";

            boolean b = st.execute(sql1);
            st.execute(sql2);
            st.execute(sql3);
            st.execute(sql4);
            st.execute(sql5);
            st.execute(sql6);
            System.out.println(b);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
