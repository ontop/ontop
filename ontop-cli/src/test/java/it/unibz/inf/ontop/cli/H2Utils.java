package it.unibz.inf.ontop.cli;

import org.h2.tools.Server;

import java.sql.SQLException;

/**
 * Created by xiao on 28/02/2017.
 */
public class H2Utils {
    private static void startDB() throws SQLException {
        Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();

    }

    private static void stopDB() throws SQLException {
        Server.shutdownTcpServer("tcp://localhost:9092", "", true, true);
    }

}
