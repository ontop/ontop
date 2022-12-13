package it.unibz.inf.ontop.cli;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

public class OntopBootstrapTest {

    @ClassRule
    public static ExternalResource h2Connection = new H2ExternalResourceForBookExample();

    @Test
    public void testOntopHelp(){
        Ontop.main("help", "bootstrap");
    }


    @Test
    public void testOntopBootstrap (){
        Ontop.main("bootstrap",
                "-b", "http://www.example.org/",
                "-p", "src/test/resources/books/exampleBooks.properties",
                "-m", "src/test/resources/output/exampleBooks-bootstrapped.obda",
                "-t", "src/test/resources/output/exampleBooks-bootstrapped.owl");
    }

    @Test
    public void testOntopBootstrapDMSingleSchema(){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-d", "src/test/resources/single_schema/boot-conf.json",
                "-p", "src/test/resources/single_schema/single_schema.properties",
                "-m", "src/test/resources/single_schema/boot-single_schema.obda",
                "-t", "src/test/resources/single_schema/boot-single_schema.owl"
        };
        Ontop.main(argv);
    }

    @Test
    public void testOntopBootstrapMPSingleSchema(){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-a",
                "-d", "src/test/resources/single_schema/boot-conf.json",
                "-p", "src/test/resources/single_schema/single_schema.properties",
                "-m", "src/test/resources/single_schema/boot-single_schema.obda",
                "-t", "src/test/resources/single_schema/boot-single_schema.owl",
                "-w", "src/test/resources/single_schema/single_schema_workload.json"
        };
        Ontop.main(argv);
    }

    @Test
    public void testMPBootstrapWorkload(){
        String[] argv = {"bootstrap",
                "-b", "http://www.example.org/",
                "-a",
                "-p", "src/test/resources/flights/flight_2.properties",
                "-m", "src/test/resources/output/flight_2.obda",
                "-t", "src/test/resources/output/flight_2.owl",
                "-w", "src/test/resources/flights/flights_queries.json"
        };
        Ontop.main(argv);
    }

}
