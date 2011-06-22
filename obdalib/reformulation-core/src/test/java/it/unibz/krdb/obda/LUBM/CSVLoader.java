package it.unibz.krdb.obda.LUBM;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CSVLoader {
    private final Logger log = LoggerFactory.getLogger(CSVLoader.class);


    public static final String class_table = "class";
    public static final String role_table = "role";

    public static final String class_table_create =
            "CREATE TABLE " + class_table + " ( "
                    + "URI VARCHAR(100),"
                    + "IDX INTEGER" + ");";

    public static final String role_table_create =
            "CREATE TABLE " + role_table + " ( "
                    + "URI1 VARCHAR(100), "
                    + "URI2 VARCHAR(100), "
                    + "IDX INTEGER" + ");";

    public static final String class_table_drop = "DROP TABLE IF EXISTS " + class_table;

    public static final String role_table_drop = "DROP TABLE IF EXISTS " + role_table;

    public String class_load_sql;

    public String role_load_sql;

    public static final String driver = "org.postgresql.Driver";
    public static final String url = "jdbc:postgresql://localhost/test:";
    public static final String username = "obda";
    public static final String password = "obda09";
    public Connection connection;

    private String dataDir;

    public CSVLoader(String dataDir) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        connection = DriverManager.getConnection(url, username, password);
        this.dataDir = dataDir;

        class_load_sql = String.format("\\COPY %s FROM '%s' WITH DELIMITER '\t' ",
                class_table, new File(dataDir + "classes.csv").getAbsolutePath());

        role_load_sql = String.format("\\COPY %s FROM '%s' WITH DELIMITER '\t' ",
                role_table, new File(dataDir + "rolles.csv").getAbsolutePath());
    }

    public void recreateDB() throws SQLException {
        Statement st = connection.createStatement();

        st.execute(class_table_drop);
        st.execute(role_table_drop);
        st.execute(class_table_create);
        st.execute(role_table_create);
        st.close();
    }

    public void loadData() throws IOException, InterruptedException {
        final long startTime = System.nanoTime();
        final long endTime;

        List<String> commands = new ArrayList<String>(2);
        commands.add("/usr/bin/psql");
        commands.add("-d");
        commands.add("test");
        commands.add("-U");
        commands.add("test");
        commands.add("-c");
        commands.add(class_load_sql);
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        builder.directory(new File("/"));

        Process clsProcess = builder.start();
        LogStreamReader lsr = new LogStreamReader(clsProcess.getInputStream());
        Thread clsThread = new Thread(lsr, "ClassLogStreamReader");
        clsThread.start();

        clsProcess.waitFor();
        commands.remove(class_load_sql);
        commands.add(role_load_sql);

        builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        builder.directory(new File("/"));
        Process roleProcess = builder.start();
        LogStreamReader roleReader = new LogStreamReader(roleProcess.getInputStream());
        Thread roleThread = new Thread(roleReader, "RoleLogStreamReader");
        roleThread.start();


        endTime = System.nanoTime();
        final long duration = endTime - startTime;

        log.info("Loading ABox took: {}", duration * 1.0e-9);
    }

    public void makeIndexes() throws SQLException {
        String idxCls1 = "CREATE INDEX cls_idx on class(idx) ;";
        String idxCls2 = "CREATE INDEX cls_idx_uri on class(idx,uri); ";

        String idxRole1 = "CREATE INDEX role_idx on role(idx);";
        String idxRole2 = "CREATE INDEX role_idx_uri1_uri2 on role(idx, uri1, uri2);";
        String idxRole3 = "CREATE INDEX role_idx_uri2_uri1 on role(idx, uri2, uri1);";

        Statement st = connection.createStatement();
        long startTime = System.nanoTime();
        st.execute(idxCls1);
        st.execute(idxCls2);

        st.execute(idxRole1);
        st.execute(idxRole2);
        st.execute(idxRole3);
        st.close();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        log.info("Building Indexes took: {}", duration);
    }


    public class LogStreamReader implements Runnable {

        private BufferedReader reader;

        public LogStreamReader(InputStream is) {
            this.reader = new BufferedReader(new InputStreamReader(is));
        }

        public void run() {
            try {
                String line = reader.readLine();
                while (line != null) {
                    System.out.println(line);
                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
