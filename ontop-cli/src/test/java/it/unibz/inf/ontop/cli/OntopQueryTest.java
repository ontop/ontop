package it.unibz.inf.ontop.cli;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Ignore
public class OntopQueryTest {

    // a random port to avoid conflicts
    private static String H2_PORT = "19123";;

    @BeforeClass
    public static void setup() throws ClassNotFoundException, SQLException {
        Server.createTcpServer("-tcpPort", H2_PORT, "-tcpAllowOthers").start();
        Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:" + H2_PORT + "/./src/test/resources/h2/books", "sa", "test");
        System.out.println("Connection Established: " + conn.getMetaData().getDatabaseProductName() + "/" + conn.getCatalog());
    }

    @AfterClass
    public static void close() throws SQLException {
        Server.shutdownTcpServer("tcp://localhost:" + H2_PORT, "", true, true);
    }

    @Test
    public void testOntopQueryCMD (){
        String[] argv = {"query", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq"};
        Ontop.main(argv);
    }


    @Test
    public void testOntopQueryCMD_Out (){
        String[] argv = {"query", "-m", "src/test/resources/books/exampleBooks.obda",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq",
                "-o", "src/test/resources/books/q1-answer.csv"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopQueryR2RML (){
        String[] argv = {"query", "-m", "src/test/resources/books/exampleBooks.ttl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-t", "src/test/resources/books/exampleBooks.owl",
                "-q", "src/test/resources/books/q1.rq"};
        Ontop.main(argv);
    }

    @Test
    public void testOntopQueryR2RML_noOntology (){
        String[] argv = {"query",
                "-m", "src/test/resources/books/exampleBooks.ttl",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-q", "src/test/resources/books/q1.rq"};
        Ontop.main(argv);
    }

    @Ignore("too expensive")
    @Test
    public void testOntopQueryAnnotations_Ontology (){
        String[] argv = {"query",
                "-m", "../quest-test/src/test/resources/annotation/doid.obda",
                "-t", "../quest-test/src/test/resources/annotation/doid.owl",
                "-q", "../quest-test/src/test/resources/annotation/q1.q",
                "--enable-annotations"
        };
        Ontop.main(argv);
     }



}
