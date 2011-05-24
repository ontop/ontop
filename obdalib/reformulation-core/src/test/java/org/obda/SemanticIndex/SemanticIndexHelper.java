package org.obda.SemanticIndex;

import inf.unibz.it.obda.owlapi.OWLAPIController;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    public final static Logger log = LoggerFactory
            .getLogger(SemanticIndexHelper.class);

    public static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    public static OWLAPIController apic;
    public static final String owlloc = "src/test/resources/test/semanticIndex_ontologies/";
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
        OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
        apic = new OWLAPIController();

        Set<OWLOntology> onto_set = new HashSet<OWLOntology>(1);
        onto_set.add(ontology);
        return onto_set;
    }

    public DAG load_dag(String ontoname) throws OWLOntologyCreationException {
        return new DAG(load_onto(ontoname));
    }

    public List<List<DAGNode>> get_results(String resname) {
        String resfile = owlloc + resname + ".si";
        File results = new File(resfile);
        Document doc = null;


        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(results);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        doc.getDocumentElement().normalize();
        //Node root = doc.getElementsByTagName("dag");
        List<DAGNode> cls = get_dag_type(doc, "classes");
        List<DAGNode> obj_props = get_dag_type(doc, "objectproperties");
        List<DAGNode> data_props = get_dag_type(doc, "dataproperties");

        List<List<DAGNode>> rv = new ArrayList<List<DAGNode>>(3);
        rv.add(cls);
        rv.add(obj_props);
        rv.add(data_props);
        return rv;
    }

    /**
     * Extract particular type of DAG nodes from XML document
     *
     * @param doc  XML document containing encoded DAG nodes
     * @param type type of DAGNodes to extract
     * @return a list of DAGNodes
     */
    private List<DAGNode> get_dag_type(Document doc, String type) {
        List<DAGNode> rv = new LinkedList<DAGNode>();
        Node root = doc.getElementsByTagName(type).item(0);
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

                Element node = (Element) childNodes.item(i);
                String uri = node.getAttribute("uri");
                int idx = Integer.parseInt(node.getAttribute("index"));

                DAGNode _node = new DAGNode(uri);
                _node.setIndex(idx);
                _node.setRange(new SemanticIndexRange());

                String[] range = node.getAttribute("range").split(",");
                for (int j = 0; j < range.length; j++) {
                    String[] interval = range[j].split(":");
                    int start = Integer.parseInt(interval[0]);
                    int end = Integer.parseInt(interval[1]);
                    _node.getRange().addInterval(start, end);
                }
                rv.add(_node);
            }
        }
        return rv;
    }

    public List<String[]> get_abox(String resname) {
        String resfile = owlloc + resname + ".abox";
        List<String[]> rv = new LinkedList<String[]>();
        try {

            FileInputStream fstream = new FileInputStream(resfile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] tokens = strLine.split(" ");
                rv.add(tokens);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rv;
    }

}
