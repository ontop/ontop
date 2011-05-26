package org.obda.reformulation.tests;

import inf.unibz.it.obda.api.controller.QueryControllerEntity;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerGroup;
import inf.unibz.it.obda.gui.swing.querycontroller.tree.QueryControllerQuery;
import inf.unibz.it.obda.io.PrefixManager;
import inf.unibz.it.obda.owlapi.OWLAPIController;
import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;
import inf.unibz.it.obda.queryanswering.DataQueryReasoner;
import inf.unibz.it.obda.queryanswering.QueryResultSet;
import inf.unibz.it.obda.queryanswering.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactoryImpl;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Tester {

    private Map<String, Vector<String>> queryheads = null;
    private Map<String, Set<String>> queryresults = null;
    private OWLOntologyManager manager = null;
    private OWLOntology ontology = null;
    private OWLAPIController apic = null;
    private DataQueryReasoner reasoner = null;
    private String owlloc = null;
    private String xmlLoc = null;
    private Map<String, String> queryMap = null;

    /**
     * A helper class that handles loading each test scenario and comparing the
     * expected results with the results given by the reasoner.
     * <p/>
     * Loading of an scenario can be done using either direct or complex
     * mappings.
     *
     * @param propfile A properties file that specifies the base location of the owl,
     *                 obda and xml files that contain the scenarios, mappings and
     *                 expected results.
     */
    public Tester(String propfile) {
        queryheads = new HashMap<String, Vector<String>>();
        queryresults = new HashMap<String, Set<String>>();
        try {
            loadProperties(propfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProperties(String propfile) throws Exception {

        FileInputStream stream = new FileInputStream(new File(propfile));
        Properties properties = new Properties();
        properties.load(stream);

        owlloc = properties.getProperty("location.of.owl.files");
        if (owlloc == null) {
            throw new Exception("Property location.of.owl.files not set!");
        }
        xmlLoc = properties.getProperty("location.of.result.files");
        if (xmlLoc == null) {
            throw new Exception("Property location.of.result.files not set!");
        }

        // Tester t = new Tester();
        // for(int i=minNr; i<=maxNr; i++){
        // String owlfile= owlLoc+filename+i+".owl";
        // String resultfile = xmlLoc+filename+i+".xml";
        // t.executeTestFor(owlfile, resultfile);
        // }

    }

    public void load(String onto, String unfold_type, String dbType) throws Exception {

        String owlfile = owlloc + onto + ".owl";
        String resultfile = xmlLoc + onto + ".xml";
        loadOntology(owlfile);
        loadResults(resultfile);

        ReformulationPlatformPreferences pref = new ReformulationPlatformPreferences();
        pref.setCurrentValueOf(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, "improved");
        if (unfold_type.equals("virtual"))
            pref.setCurrentValueOf(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, "true");
        else if (unfold_type.equals("material")) {
            pref.setCurrentValueOf(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, "false");
        } else {
            throw new Exception("The unfolding mechanism can only be either material or virtual");
            // Note: Due to resent changes the semantic mode has been removed. It is now a
            // submode of the material Abox mode and the tester has still to be adapted to that
            // changes
        }
        pref.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, dbType);
        pref.setCurrentValueOf(ReformulationPlatformPreferences.DATA_LOCATION, "inmemory");
        pref.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, unfold_type);

        OBDAOWLReformulationPlatformFactoryImpl fac = new OBDAOWLReformulationPlatformFactoryImpl();
        fac.setOBDAController(apic);
        fac.setPreferenceHolder(pref);

        reasoner = (DataQueryReasoner) fac.createReasoner(manager);

        queryMap = new HashMap<String, String>();
        Vector<QueryControllerEntity> vec = apic.getQueryController().getElements();
        Iterator<QueryControllerEntity> it = vec.iterator();
        while (it.hasNext()) {
            QueryControllerEntity e = it.next();
            if (e instanceof QueryControllerQuery) {
                QueryControllerQuery qcq = (QueryControllerQuery) e;
                queryMap.put(qcq.getID(), qcq.getQuery());
            } else {
                QueryControllerGroup group = (QueryControllerGroup) e;
                Vector<QueryControllerQuery> q = group.getQueries();
                Iterator<QueryControllerQuery> qit = q.iterator();
                while (qit.hasNext()) {
                    QueryControllerQuery qcq = qit.next();
                    String id = group.getID() + "_" + qcq.getID();
                    queryMap.put(id, qcq.getQuery());
                }
            }
        }
    }

    // TODO workaround for old syntax for calling tester.load
    public void load(String onto, String unfold_type) throws Exception {
        load(onto, unfold_type, "direct");
    }

    public Set<String> getQueryIds() {
        return queryresults.keySet();
    }

    public Set<String> executeQuery(String id) throws Exception {
        String query = queryMap.get(id);
        return execute(query, id);
    }

    public Set<String> getExpectedResult(String id) {
        return queryresults.get(id);
    }

    private Set<String> execute(String query, String id) throws Exception {

        String prefix = getPrefix();
        String fullquery = prefix + "\n" + query;
        Statement statement = reasoner.getStatement();
        QueryResultSet result = statement.executeQuery(fullquery);
        int col = result.getColumCount();
        HashSet<String> tuples = new HashSet<String>();
        while (result.nextRow()) {
            String tuple = "";
            for (int i = 1; i <= col; i++) {
                if (tuple.length() > 0) {
                    tuple = tuple + ",";
                }
                if (isBooleanQuery(id)) {
                    tuple = tuple + result.getAsString(i);
                } else {
                    URI uri = result.getAsURI(i);
                    tuple = tuple + uri.getFragment();
                }
            }
            tuples.add(tuple);
        }
        return tuples;
    }

    private void loadOntology(String owlfile) throws OWLOntologyCreationException, IOException {

        manager = OWLManager.createOWLOntologyManager();
        ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

        apic = new OWLAPIController();
        String obdafile = owlfile.substring(0, owlfile.length() - 3) + "obda";
        // apic.loadData(new File(owlfile).toURI());

        apic.getIOManager().loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getURI(), apic.getPrefixManager());
        fillPrefixManager();
    }

    private void loadResults(String resultfile) {

        queryheads = new HashMap<String, Vector<String>>();
        queryresults = new HashMap<String, Set<String>>();

        File results = new File(resultfile);

        if (!results.exists()) {
            System.err.println("result file not found.");
            return;
        }
        if (!results.canRead()) {
            System.err.print("WARNING: can't read the result file:" + results.toString());
            return;
        }

        Document doc = null;
        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(results);
            doc.getDocumentElement().normalize();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Element root = doc.getDocumentElement();
        if (root.getNodeName() != "results") {
            System.err.println("WARNING: result file must start with tag <RESULTS>");
            return;
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element node = (Element) children.item(i);
                if (node.getNodeName().equals("result")) {
                    String queryid = node.getAttribute("queryid");
                    Node head = node.getElementsByTagName("head").item(0);
                    Node tuples = node.getElementsByTagName("tuples").item(0);
                    queryheads.put(queryid, getVariables((Element) head));
                    queryresults.put(queryid, getResults((Element) tuples));
                }
            }
        }

    }

    private Vector<String> getVariables(Element node) {
        Vector<String> v = new Vector<String>();
        NodeList list = node.getElementsByTagName("variable");
        for (int i = 0; i < list.getLength(); i++) {
            Element n = (Element) list.item(i);
            String s = n.getTextContent().trim();
            if (s.length() > 0) {
                v.add(s);
            }
        }
        return v;
    }

    private Set<String> getResults(Element node) {
        HashSet<String> set = new HashSet<String>();
        NodeList list = node.getElementsByTagName("tuple");
        for (int i = 0; i < list.getLength(); i++) {
            Element n = (Element) list.item(i);
            NodeList constants = n.getElementsByTagName("constant");
            for (int j = 0; j < constants.getLength(); j++) {
                Element con = (Element) constants.item(j);
                String s = con.getTextContent().trim();
                if (s.length() > 0) {
                    set.add(s);
                }
            }
        }
        return set;
    }

    private String getPrefix() {
        String queryString = "";
        String defaultNamespace = ontology.getURI().toString();
        if (defaultNamespace.endsWith("#")) {
            queryString += "BASE <" + defaultNamespace.substring(0, defaultNamespace.length() - 1) + ">\n";
        } else {
            queryString += "BASE <" + defaultNamespace + ">\n";
        }
        queryString += "PREFIX :   <" + defaultNamespace + "#>\n";

        queryString += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
        queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
        queryString += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
        queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
        queryString += "PREFIX dllite: <http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#>\n";
        queryString += "PREFIX ucq: <http://www.obda.org/ucq/predicate/queryonly#>\n";

        return queryString;
    }

    private boolean isBooleanQuery(String queryid) {
        Vector<String> head = queryheads.get(queryid);
        if (head.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void fillPrefixManager() {
        PrefixManager man = apic.getPrefixManager();
        man.setDefaultNamespace(ontology.getURI().toString());
        man.addUri("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
        man.addUri("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        man.addUri("http://www.w3.org/2001/XMLSchema#", "xsd");
        man.addUri("http://www.w3.org/2002/07/owl#", "owl");
//        man.addUri(ontology.getURI().toString(), "xml:base");
        man.addUri("http://obda.inf.unibz.it/ontologies/tests/dllitef/test.owl#", "dllite");
    }
}
