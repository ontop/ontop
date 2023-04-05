package federationOptimization;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.util.*;
import java.util.logging.Logger;

public class QueryRewriting {

    public static String owlFile;
    public static String obdaFile;
    public static String propertyFile;
    public static String query;
    public static String hintFile;
    public static String labFile;

    public static HashMap<String, String> sourceMap;
    public static HashMap<String, String> labMap;

    public QueryRewriting(){
        owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
        obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
        propertyFile = "src/test/resources/federation-test/sc2.properties";
        hintFile = "src/test/resources/federation-test/hintFile.txt";
        labFile = "src/test/resources/federation-test/SourceLab.txt";

        query = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" +
                "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT DISTINCT ?product ?label\n" +
                "WHERE {\n" +
                " ?product rdfs:label ?label .\n" +
                " ?product a bsbm:Product .\n" +
                " ?product bsbm:productFeature bsbm-inst:ProductFeature89 .\n" +
                " ?product bsbm:productFeature bsbm-inst:ProductFeature91 .\n" +
                " ?product bsbm:productPropertyNumeric1 ?value1 .\n" +
                " FILTER (?value1 < 1000)\n" +
                "}";

        sourceMap = new HashMap<String, String>();
        labMap = new HashMap<String, String>();
    }

    /***@Zhenzhen Gu
     * obtain the IQ tree translated by Ontop when taking query, ontology and mapping set as input
     * @param sparql
     * @param owlFile
     * @param obdaFile
     * @param propertyFile
     * @return
     * @throws OWLException
     */
    public IQ getIQ(String sparql, String owlFile, String obdaFile, String propertyFile) throws OWLException {
        IQ iq = null;
        OntopOWLEngine res;
        OntopOWLConnection ct;
        OntopOWLStatement st;
        TupleOWLResultSet rs;
        Logger logger;

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdaFile)
                .ontologyFile(owlFile)
                .propertyFile(propertyFile)
                .enableTestMode()
                .build();
        res = new SimpleOntopOWLEngine(config);
        ct = res.getConnection();
        st = ct.createStatement();

        iq = st.getExecutableQuery(sparql); /**通过修改QuestQueryprocessor，来获取转换后的iq tree，转换为SQL之前的查询，让Ontop返回IQ tree 而不是SQL query*/

        return iq;
    }

    /***
     * @Zhenzhen Gu, compute the cost of IQ tree
     * @param iqTree
     * @return
     */
    public List<Integer> costOfIQTree(IQTree iqTree){
        List<Integer> cost_measure = new ArrayList<Integer>(); // cost(q) = (m,n)
        int costJ = 0;
        int ineffSource = 0;
        ImmutableList<IQTree> childern = iqTree.getChildren();
        for(IQTree t : childern){
            if(t.isLeaf()){
                Set<String> sources = getSources(t);
                for(String s: sources){
                    if(labMap.containsKey(s)){
                        if(labMap.get(s).equals("inefficient")){
                            ineffSource = ineffSource + 1;
                        }
                    }
                }
            } else {
                QueryNode root = t.getRootNode();
                if((root instanceof InnerJoinNode) || (root instanceof LeftJoinNode)){
                    costJ = costJ + getCostOfJoin(t);
                }
            }
        }

        cost_measure.add(costJ);
        cost_measure.add(ineffSource);
        return cost_measure;
    }

    public ArrayList<IQTree> getAllSubTree(IQTree iqTree){
        ArrayList<IQTree> subTrees = new ArrayList<IQTree>();
        ArrayList<IQTree> newAdded = new ArrayList<IQTree>();

        newAdded.add(iqTree);
        int label = 0;
        while(label == 0){
            if(newAdded.size() == 0){
                label = 1;
            } else {
                subTrees.addAll(newAdded);
                ArrayList<IQTree> list = new ArrayList<IQTree>();
                for(IQTree t : newAdded){
                    if(!t.isLeaf()){
                        list.addAll(t.getChildren());
                    }
                }
                newAdded.clear();
                newAdded.addAll(list);
            }
        }
        return subTrees;
    }

    public Set<String> getSources(IQTree iqTree){
        Set<String> sources = new HashSet<String>();
        List<IQTree> subTrees = getAllSubTree(iqTree);
        for(IQTree t : subTrees){
            if(t.isLeaf()){
                QueryNode dn = t.getRootNode();
                if(dn instanceof ExtensionalDataNode){
                    String dn_s = dn.toString();
                    String relationName = dn_s.substring(11, dn_s.indexOf("("));  //这一句还需要修改
                    sources.add(relationName);
                }

            }
        }
        return sources;
    }

    public int getCostOfJoin(IQTree joinTree){
        int cost = 0; // local join has cost 0, non-local join has cost greater than 0;
        Set<String> sources = getSources(joinTree);
        if(sources.size() == 1){
            return 0;
        }
        QueryNode root = joinTree.getRootNode();
        ImmutableList<IQTree> subTrees = joinTree.getChildren();

        return cost;
    }

    public IQ rewriteIQ(IQ iq, String hintFile, String labFile){
        IntermediateQueryFactory iqFactory = null;

        IQ iq_new = null;

        IQ normalizedQuery = iq.normalizeForOptimization();
        IQTree tree = normalizedQuery.getTree();

        IQTree initialTree = iq.getTree();

        //translate IQ tree into IQ query.
        iqFactory.createIQ(iq.getProjectionAtom(), initialTree)
                .normalizeForOptimization();

        return iq_new;
    }

    public String IQ2SQL(IQ iq){
        String sql = "";

        sql = Optional.of(iq.getTree())
                .filter(t -> t instanceof UnaryIQTree)
                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
                .filter(n -> n instanceof NativeNode)
                .map(n -> ((NativeNode) n).getNativeQueryString())
                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + iq));

        return sql;
    }

    public String queryRewrite(String sparql, String owlFile, String obdaFile, String propertyFile, String hintFile, String labFile) throws OWLException {
        String SQL = "";
        IQ iq = getIQ(sparql, owlFile, obdaFile, propertyFile);
        iq = rewriteIQ(iq, hintFile, labFile);
        SQL = IQ2SQL(iq);
        return SQL;
    }

    @Test
    public void testPart(){
        try{

            QueryRewriting QR = new QueryRewriting();
            IQ iq = QR.getIQ(query, owlFile, obdaFile, propertyFile);
            QR.getAllSubTree(iq.getTree());

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
