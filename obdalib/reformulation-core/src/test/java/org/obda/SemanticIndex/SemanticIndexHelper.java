package org.obda.SemanticIndex;

import org.h2.jdbcx.JdbcDataSource;
import org.obda.owlrefplatform.core.abox.DAG;
import org.obda.owlrefplatform.core.abox.DAGNode;
import org.obda.owlrefplatform.core.abox.SemanticIndexRange;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Helper class to load ontologies and comapre computed values to expected results
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexHelper {
    private final static Logger log = LoggerFactory
            .getLogger(SemanticIndexHelper.class);

    public OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    ;
    public OWLOntology ontology = null;
    public String owlloc = "src/test/resources/test/semanticIndex_ontologies/";
    public Connection conn;

    public SemanticIndexHelper() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:db1");
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            log.error("Error creating test database");
            e.printStackTrace();
        }
    }

    public Set<OWLOntology> load_onto(String ontoname) throws OWLOntologyCreationException {
        String owlfile = owlloc + ontoname + ".owl";
        OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(
                owlfile)).toURI());

        Set<OWLOntology> onto_set = new HashSet<OWLOntology>(1);
        onto_set.add(ontology);
        return onto_set;
    }

    public DAG load_dag(String ontoname) throws OWLOntologyCreationException {
        return new DAG(load_onto(ontoname));
    }

    public List<DAGNode> get_results(String resname) {
        String resfile = owlloc + resname + ".si";
        //Map<String, DAGNode> rv = new HashMap<String, DAGNode>();
        List<DAGNode> rv = new LinkedList<DAGNode>();
        try {

            FileInputStream fstream = new FileInputStream(resfile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] tokens = strLine.split(" ");
                String uri = tokens[0];
                String[] ranges = tokens[1].split(":");
                DAGNode node = new DAGNode(uri);
                node.setRange(new SemanticIndexRange());
                for (int i = 0; i < ranges.length; ++i) {
                    String[] range = ranges[i].split(",");

                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);
                    node.getRange().addInterval(start, end);
                }
                rv.add(node);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rv;
    }

    public Map<String, Integer> get_abox(String resname) {
        String resfile = owlloc + resname + ".abox";
        Map<String, Integer> rv = new HashMap<String, Integer>();
        try {

            FileInputStream fstream = new FileInputStream(resfile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] tokens = strLine.split(" ");
                String uri = tokens[0];
                int index = Integer.parseInt(tokens[1]);
                rv.put(uri, index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rv;
    }

}
