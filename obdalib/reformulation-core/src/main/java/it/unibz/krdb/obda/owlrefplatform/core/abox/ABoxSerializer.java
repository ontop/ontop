package it.unibz.krdb.obda.owlrefplatform.core.abox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store ABox assertions in the DB
 *
 * @author Sergejs Pugac
 */
public class ABoxSerializer {
    private final static Logger log = LoggerFactory
            .getLogger(ABoxSerializer.class);

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

    public static final String class_insert = "INSERT INTO " + class_table
            + " (URI, IDX) VALUES (?, ?)";

    public static final String role_insert = "INSERT INTO " + role_table
            + " (URI1, URI2, IDX) VALUES (?, ?, ?)";


    public static void recreate_tables(Connection conn) throws SQLException {
        log.debug("Recreating ABox tables");
        conn.createStatement().execute(class_table_drop);
        conn.createStatement().execute(role_table_drop);

        conn.createStatement().execute(class_table_create);
        conn.createStatement().execute(role_table_create);

    }

    public static void ABOX2DB(Set<OWLOntology> ontologies, DAG dag, Connection conn) throws SQLException {
        PreparedStatement cls_stm = conn.prepareStatement(class_insert);
        PreparedStatement role_stm = conn.prepareStatement(role_insert);

        for (OWLOntology onto : ontologies) {
            onto.getIndividualAxioms();

            for (OWLIndividualAxiom ax : onto.getIndividualAxioms()) {
                if (ax instanceof OWLDataPropertyAssertionAxiom) {
                    OWLDataPropertyAssertionAxiom triple = (OWLDataPropertyAssertionAxiom) ax;
                    String prop = triple.getProperty().asOWLDataProperty().getURI().toString();
                    String uri = triple.getSubject().asOWLIndividual().getURI().toString();
                    String lit = triple.getObject().getLiteral();

                    DAGNode node = dag.getRoleNode(prop);
                    int idx = node.getIndex();

                    role_stm.setString(1, uri);
                    role_stm.setString(2, lit);
                    role_stm.setInt(3, idx);
                    role_stm.addBatch();

                } else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
                    OWLObjectPropertyAssertionAxiom triple = (OWLObjectPropertyAssertionAxiom) ax;
                    String prop = triple.getProperty().asOWLObjectProperty().getURI().toString();
                    String uri1 = triple.getSubject().asOWLIndividual().getURI().toString();
                    String uri2 = triple.getObject().asOWLIndividual().getURI().toString();


                    DAGNode node = dag.getRoleNode(prop);
                    int idx = node.getIndex();

                    role_stm.setString(1, uri1);
                    role_stm.setString(2, uri2);
                    role_stm.setInt(3, idx);
                    role_stm.addBatch();

                } else if (ax instanceof OWLClassAssertionAxiom) {
                    OWLClassAssertionAxiom triple = (OWLClassAssertionAxiom) ax;
                    String cls = triple.getDescription().asOWLClass().getURI().toString();
                    // XXX: strange behaviour - owlapi generates an extra assertion of the form ClassAssertion(Thing, i)
                    if (!cls.equals(DAG.thingStr)) {
                        String uri = triple.getIndividual().getURI().toString();

                        DAGNode node = dag.getClassNode(cls);
                        int idx = node.getIndex();

                        cls_stm.setString(1, uri);
                        cls_stm.setInt(2, idx);
                        cls_stm.addBatch();
                    }
                }
            }
        }
        cls_stm.executeBatch();
        role_stm.executeBatch();
    }
}
