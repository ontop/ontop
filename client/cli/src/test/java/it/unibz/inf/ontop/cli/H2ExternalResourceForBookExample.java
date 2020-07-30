package it.unibz.inf.ontop.cli;

import org.h2.tools.Server;
import org.junit.rules.ExternalResource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class H2ExternalResourceForBookExample extends ExternalResource {

    // a random port to avoid conflicts
    private static String H2_PORT = "19123";
    private String h2ConnectionName;

    @Override
    protected void before() throws Throwable {
        Server.createTcpServer("-tcpPort", H2_PORT, "-tcpAllowOthers").start();
        Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + H2_PORT + "/./src/test/resources/h2/books;ACCESS_MODE_DATA=r", "sa", "test");
        h2ConnectionName = conn.getMetaData().getDatabaseProductName() + "/" + conn.getCatalog();
        System.out.println("H2ExternalResourceForBookExample: Connection Established: " + h2ConnectionName);
    }

    @Override
    protected void after() {
        try {
            Server.shutdownTcpServer("tcp://localhost:" + H2_PORT, "", true, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("H2ExternalResourceForBookExample: Connection Closed: " + h2ConnectionName);
    }

}
