package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.ConceptDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DescriptionFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.BasicDescriptionFactory;
import org.semanticweb.owl.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

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

    private static final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private static final DescriptionFactory descFactory = new BasicDescriptionFactory();

    public static void ABOX2DB(Set<OWLOntology> ontologies, DAG dag, DAG pureIsa, Connection conn) throws SQLException {
        PreparedStatement cls_stm = conn.prepareStatement(class_insert);
        PreparedStatement role_stm = conn.prepareStatement(role_insert);
//        DAG pureIsa  = DAGConstructor.filterPureISA(dag);

        for (OWLOntology onto : ontologies) {
            onto.getIndividualAxioms();

            for (OWLIndividualAxiom ax : onto.getIndividualAxioms()) {
                if (ax instanceof OWLDataPropertyAssertionAxiom) {
                    OWLDataPropertyAssertionAxiom triple = (OWLDataPropertyAssertionAxiom) ax;
                    String prop = triple.getProperty().asOWLDataProperty().getURI().toString();
                    String uri = triple.getSubject().asOWLIndividual().getURI().toString();
                    String lit = triple.getObject().getLiteral();

                    Predicate propPred = predicateFactory.getPredicate(URI.create(prop), 2);
                    RoleDescription propDesc = descFactory.getRoleDescription(propPred);
                    DAGNode node = pureIsa.getRoleNode(propDesc);
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


                    Predicate propPred = predicateFactory.getPredicate(URI.create(prop), 2);
                    RoleDescription propDesc = descFactory.getRoleDescription(propPred);
                    DAGNode node = pureIsa.getRoleNode(propDesc);
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

                        Predicate clsPred = predicateFactory.getPredicate(URI.create(cls), 1);
                        ConceptDescription clsDesc = descFactory.getAtomicConceptDescription(clsPred);
                        DAGNode node = pureIsa.getClassNode(clsDesc);
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
