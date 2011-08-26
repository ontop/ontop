package it.unibz.krdb.obda.SemanticIndex;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAG;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGConstructor;
import it.unibz.krdb.obda.owlrefplatform.core.dag.DAGNode;
import it.unibz.krdb.obda.owlrefplatform.core.dag.SemanticIndexRange;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OntologyFactory;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.h2.jdbcx.JdbcDataSource;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Helper class to load ontologies and comapre computed values to expected results
 *
 * @author Sergejs Pugac
 */
public class SemanticIndexHelper {
    public final static Logger log = LoggerFactory
            .getLogger(SemanticIndexHelper.class);

    public static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    //    public static OBDAModel apic;
    public static final String owlloc = "src/test/resources/test/semanticIndex_ontologies/";
    public Connection conn;

    private final OBDADataFactory predicateFactory = OBDADataFactoryImpl.getInstance();
    private final OntologyFactory descFactory = new OntologyFactoryImpl();

    private final static String owl_exists = "::__exists__::";
    private final static String owl_inverse_exists = "::__inverse__exists__::";
    private final static String owl_inverse = "::__inverse__::";

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

    public Ontology load_onto(String ontoname) throws Exception {
        String owlfile = owlloc + ontoname + ".owl";
        OWLOntology owlOntology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());
        OWLAPI2Translator translator = new OWLAPI2Translator();

        Ontology ontology;
        return translator.translate(owlOntology);

    }

    public DAG load_dag(String ontoname) throws Exception {

        return DAGConstructor.getISADAG(load_onto(ontoname));
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
        List<DAGNode> cls = get_dag_type(doc, "classes");
        List<DAGNode> roles = get_dag_type(doc, "rolles");

        List<List<DAGNode>> rv = new ArrayList<List<DAGNode>>(2);
        rv.add(cls);
        rv.add(roles);
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

                int arity = 1;
                boolean inverse = false;
                boolean exists = false;
                Predicate p;
                Description description;

                if (uri.startsWith(owl_exists)) {
                    uri = uri.substring(owl_exists.length());
                    arity = 2;
                    exists = true;

                } else if (uri.startsWith(owl_inverse_exists)) {
                    uri = uri.substring(owl_inverse_exists.length());
                    arity = 2;
                    inverse = true;
                    exists = true;
                } else if (uri.startsWith(owl_inverse)) {
                    uri = uri.substring(owl_inverse.length());
                    inverse = true;
                }

                p = predicateFactory.getPredicate(URI.create(uri), arity);

                if (type.equals("classes")) {
                    if (exists)
                        description = descFactory.getPropertySomeRestriction(p, inverse);
                    else
                        description = descFactory.createClass(p);
                } else {
                    description = descFactory.createProperty(p, inverse);
                }


                DAGNode _node = new DAGNode(description);

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
