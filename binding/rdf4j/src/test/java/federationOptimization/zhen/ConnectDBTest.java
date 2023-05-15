package federationOptimization.zhen;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConnectDBTest {

    public static void main(String[] args){
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
