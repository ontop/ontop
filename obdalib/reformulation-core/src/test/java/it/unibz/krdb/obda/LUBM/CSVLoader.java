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

    final String driver = "org.postgresql.Driver";
    final String url = "jdbc:postgresql://localhost/test:";
    final String username = "test";
    final String password = "test";
    Connection connection;

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
