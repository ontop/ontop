package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import junit.framework.TestCase;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ColocatedSensorTest extends TestCase {
    protected OntopOWLReasoner reasoner;
    protected OntopOWLConnection conn;

    public void testMapping(){

        String url = "jdbc:postgresql://obdalin.inf.unibz.it:5433/siemens_exp";
        String username = "postgres";
        String password = "postgres";
        String query =      "PREFIX : <http://siemens.com/ns#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "select ?tb ?br   where {" +
                "?tb rdf:type :Turbine." +
                "?br rdf:type :Burner." +
                "?br :isPartOf ?tb." +
//                "?br :isMonitoredBy ?ts." +
//                "?ts rdf:type :TemperatureSensor." +
//                "?pt rdf:type :PowerTurbine." +
//                "?pt :isMonitoredBy ?rs." +
//                "?rs rdf:type :RotationSpeedSensor." +
//                "?pt :isPartOf ?tb." +
                "}";;

        OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .enableFullMetadataExtraction(false)
                .ontologyFile("src/test/resources/siemens.owl")
                .nativeOntopMappingFile("src/test/resources/siemens2.obda")
                .jdbcUrl(url)
                .jdbcUser(username)
                .jdbcPassword(password)
                .build();

        try {
            reasoner = factory.createReasoner(config);
            // Now we are ready for querying
            conn = reasoner.getConnection();
            OWLStatement st = conn.createStatement();
            st.executeSelectQuery(query);

        } catch (OWLException e) {
            e.printStackTrace();
        }


    }
}
