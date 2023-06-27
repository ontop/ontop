package federationOptimization.queryRewriting;

import federationOptimization.ObdfTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.teiid.client.plan.PlanNode;
import org.teiid.jdbc.TeiidStatement;

import java.sql.*;

@Category(ObdfTest.class)
public class ConnectDBTest {

    @Test
    public void testConnectTeiid(){
        try{
            Class.forName("org.teiid.jdbc.TeiidDriver");
            Connection conn = DriverManager.getConnection("jdbc:teiid:homogeneous@mm://localhost:11000", "obdf", "obdfPwd0");
//            DatabaseMetaData metadata = conn.getMetaData();
//            ResultSet rs = metadata.getColumns(null, "%", "review", "%");
//            while(rs.next()){
//                System.out.println("没有结果");
//                System.out.println(rs.getString("COLUMN_NAME"));
//            }

            Statement stmt = conn.createStatement();
            String sql = "SELECT DISTINCT v5.\"label10m46\" AS \"label10m46\", v5.\"product0m2\" AS \"product0m2\"\n" +
                    "FROM (\n" +
                    "      SELECT v1.\"label\" AS \"label10m46\", v1.\"nr\" AS \"product0m2\"\n" +
                    "      FROM \"ss1\".\"product1\" v1\n" +
                    "      WHERE v1.\"label\" IS NOT NULL\n" +
                    "      UNION ALL\n" +
                    "      SELECT v3.\"label\" AS \"label10m46\", v3.\"nr\" AS \"product0m2\"\n" +
                    "      FROM \"ss5\".\"product2\" v3\n" +
                    "      WHERE v3.\"label\" IS NOT NULL\n" +
                    "     ) v5, (\n" +
                    "            SELECT v6.\"product\" AS \"product0m0\"\n" +
                    "            FROM \"ss1\".\"productfeatureproduct1\" v6\n" +
                    "            WHERE 1000 = v6.\"productfeature\"\n" +
                    "            UNION ALL\n" +
                    "            SELECT v8.\"product\" AS \"product0m0\"\n" +
                    "            FROM \"ss5\".\"productfeatureproduct2\" v8\n" +
                    "            WHERE 1000 = v8.\"productfeature\"\n" +
                    "          ) v10, (\n" +
                    "                  SELECT v11.\"product\" AS \"product0m1\"\n" +
                    "                  FROM \"ss1\".\"productfeatureproduct1\" v11\n" +
                    "                  WHERE 1000 = v11.\"productfeature\"\n" +
                    "                  UNION ALL\n" +
                    "                  SELECT v13.\"product\" AS \"product0m1\"\n" +
                    "                  FROM \"ss5\".\"productfeatureproduct2\" v13\n" +
                    "                  WHERE 1000 = v13.\"productfeature\"\n" +
                    "                 ) v15, (\n" +
                    "                         SELECT v16.\"nr\" AS \"product0m3\", v16.\"propertynum1\" AS \"propertynum1m41\"\n" +
                    "                         FROM \"ss1\".\"product1\" v16\n" +
                    "                         WHERE (v16.\"propertynum1\" IS NOT NULL AND (v16.\"propertynum1\" <= 1000))\n" +
                    "                         UNION ALL\n" +
                    "                         SELECT v18.\"nr\" AS \"product0m3\", v18.\"propertynum1\" AS \"propertynum1m41\"\n" +
                    "                         FROM \"ss5\".\"product2\" v18\n" +
                    "                         WHERE (v18.\"propertynum1\" IS NOT NULL AND (v18.\"propertynum1\" <= 1000))\n" +
                    "                        ) v20\n" +
                    "WHERE (v5.\"product0m2\" = v10.\"product0m0\" AND v5.\"product0m2\" = v15.\"product0m1\" AND v5.\"product0m2\" = v20.\"product0m3\")";
//            ResultSet rs = stmt.executeQuery(sql);
//            ResultSetMetaData rsmd = rs.getMetaData();
//            int attr_num = rsmd.getColumnCount();
//            System.out.println(attr_num);
//            for(int i=1; i<attr_num+1; i++){
//                System.out.println("属性名："+rsmd.getColumnName(i));
//                System.out.println("属性类型："+rsmd.getColumnTypeName(i));
//            }
//
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
