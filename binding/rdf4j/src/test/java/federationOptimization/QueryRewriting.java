package federationOptimization;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.NativeNode;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class QueryRewriting {

    public IQ getIQ(String sparql, String owlFile, String obdaFile, String propertyFile) throws OWLException { IQ iq = null;

        OntopOWLEngine res;
        OntopOWLConnection ct;
        OntopOWLStatement st;
        TupleOWLResultSet rs;
        Logger logger;

       String owlfile = QueryRewriting.class.getResource(owlFile).toString();
       String obdafile =  QueryRewriting.class.getResource(obdaFile).toString();
       String propertyfile  =  QueryRewriting.class.getResource(propertyFile).toString();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(obdafile)
                .ontologyFile(owlfile)
                .propertyFile(propertyfile)
                .enableTestMode()
                .build();
        res = new SimpleOntopOWLEngine(config);
        ct = res.getConnection();
        st = ct.createStatement();

        IQ executableQuery = st.getExecutableQuery(sparql);
        //String SQL = st.getRewritingRendering();

        return executableQuery;
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

    public List<Integer> cost(IQ iq){
        //costMeasure (m, n)
        List<Integer> measure = new ArrayList<Integer>();

        return measure;
    }

    public String queryRewrite(String sparql, String owlFile, String obdaFile, String propertyFile, String hintFile, String labFile) throws OWLException {
        String SQL = "";
        IQ iq = getIQ(sparql, owlFile, obdaFile, propertyFile);
        iq = rewriteIQ(iq, hintFile, labFile);
        SQL = IQ2SQL(iq);
        return SQL;
    }

    @Test
    public void testGetIQ(){
        try{
//            String owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
//            String obdaFile = "src/test/resources/federation-test/bsbm-mappings-hom-het.obda";
//            String propertyFile = "src/test/resources/federation-test/teiid.properties";
//            String hintFile = "src/test/resources/federation-test/hintFile.txt";
//            String labFile = "src/test/resources/federation-test/SourceLab.txt";
//
//            String query = "PREFIX bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>\n" +
//                    "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
//                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                    "SELECT ?product (count(?feature) as ?featureNum)\n" +
//                    "WHERE {\n" +
//                    " ?product bsbm:productId ?id .\n" +
//                    "     FILTER (?id < 1000 )\n" +
//                    " ?product bsbm:productFeature ?feature .\n" +
//                    " }\n" +
//                    "GROUP BY ?product";
//
//            QueryRewriting QR = new QueryRewriting();
//            IQ iq = QR.getIQ(query, owlFile, obdaFile, propertyFile);
//            System.out.println(iq);
//            System.out.println("===========================================");
//
//            System.out.println(iq.normalizeForOptimization());
            System.out.println("==abc==");

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
