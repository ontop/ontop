package federationOptimization;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.dbschema.impl.json.JsonLens;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.impl.SimpleOntopOWLEngine;
import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;
import it.unibz.inf.ontop.utils.impl.LegacyVariableGenerator;
import org.apache.commons.rdf.api.RDF;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class QueryRewriting {

    public static String owlFile;
    public static String obdaFile;
    public static String propertyFile;
    public static String query;
    public static String hintFile;
    public static String labFile;
    public static String sourceFile;
    public static String effLabel;

    public static IntermediateQueryFactory IQ_FACTORY;
    public static AtomFactory ATOM_FACTORY;
    public static TypeFactory TYPE_FACTORY;
    public static TermFactory TERM_FACTORY;
    public static FunctionSymbolFactory FUNCTION_SYMBOL_FACTORY;
    public static SubstitutionFactory SUBSTITUTION_FACTORY;
    public static QueryTransformerFactory TRANSFORMER_FACTORY;
    public static OptimizerFactory OPTIMIZER_FACTORY;
    public static CoreUtilsFactory CORE_UTILS_FACTORY;
    public static BooleanExpressionPushDownTransformer PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER;
    public static DBConstant TRUE, FALSE;
    public static Constant NULL;

    public static RDF RDF_FACTORY;
    public static CoreSingletons CORE_SINGLETONS;

    public static DBTermType JSON_TYPE;

    public static HashMap<String, String> sourceMap;
    public static HashMap<String, String> labMap;
    public static List<Set<String>> hints; // str_redundancy set, equ_redundancy, empty federated join set, materialized views
    /***comment from Zhenzhen
     * several codes (labeled) need to be changed according to the different ways of representing hints
     * it is easy to change
     * minor change was made in QuestQueryProcessor.java (comment added).
     * **/

    public QueryRewriting(){
        try{
            owlFile = "src/test/resources/federation-test/bsbm-ontology.owl";
            obdaFile = "src/test/resources/federation-test/bsbm-mappings-sc2.obda";
            propertyFile = "src/test/resources/federation-test/sc2.properties";
            hintFile = "src/test/resources/federation-test/hintFile.txt";
            labFile = "src/test/resources/federation-test/SourceLab.txt";
            sourceFile = "src/test/resources/federation-test/SourceFile.txt";
            effLabel = "src/test/resources/federation-test/effLabel.txt";

            query = "PREFIX rev: <http://purl.org/stuff/rev#>\n" +
                    "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                    "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
                    "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                    "\n" +
                    "SELECT ?p ?mbox_sha1sum ?country ?r ?product ?title\n" +
                    "WHERE {\n" +
                    "<http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromRatingSite/Review88> rev:reviewer ?p .\n" +
                    "?p foaf:name ?name .\n" +
                    "?p foaf:mbox_sha1sum ?mbox_sha1sum .\n" +
                    "?p bsbm:country ?country .\n" +
                    "?r rev:reviewer ?p .\n" +
                    "?r bsbm:reviewFor ?product .\n" +
                    "?r dc:title ?title .\n" +
                    "}";

            sourceMap = new HashMap<String, String>();
            labMap = new HashMap<String, String>();
            hints = new ArrayList<Set<String>>();
            Set<String> str_redundancy = new HashSet<String>();
            Set<String> equ_redundancy = new HashSet<String>();
            Set<String> emptyJoin = new HashSet<String>();
            Set<String> matViews = new HashSet<String>();
            hints.add(str_redundancy);
            hints.add(equ_redundancy);
            hints.add(emptyJoin);
            hints.add(matViews);

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
            String line = null;
            while((line=br.readLine()) != null ){
                String[] arr = line.split("-");
                sourceMap.put(arr[0], arr[1]);
            }
            br.close();

            BufferedReader br_new = new BufferedReader(new InputStreamReader(new FileInputStream(effLabel)));
            line = null;
            while((line=br_new.readLine()) != null ){
                String[] arr = line.split("-");
                labMap.put(arr[0], arr[1]);
            }
            br_new.close();

            BufferedReader br_hint = new BufferedReader(new InputStreamReader(new FileInputStream(hintFile)));
            line = null;
            while((line=br_hint.readLine()) != null){
                String[] arr = line.split(":");
                if(arr[0].startsWith("empty_federated_join")){
                    hints.get(2).add(arr[1]);
                } else if(arr[0].startsWith("strict_redundancy")){
                    hints.get(0).add(arr[1]);
                } else if(arr[0].startsWith("equivalent_redundancy")){
                    hints.get(1).add(arr[1]);
                } else {
                    hints.get(3).add(arr[1]);
                }
            }
            br_hint.close();

            Properties tmpProperties = new Properties();
            tmpProperties.put(VariableGenerator.class.getCanonicalName(), LegacyVariableGenerator.class.getCanonicalName());

            OntopOptimizationConfiguration defaultConfiguration = OntopOptimizationConfiguration.defaultBuilder()
                    .properties(tmpProperties)
                    .enableTestMode()
                    .build();

            Injector injector = defaultConfiguration.getInjector();
            IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
            ATOM_FACTORY = injector.getInstance(AtomFactory.class);
            TYPE_FACTORY = injector.getInstance(TypeFactory.class);
            TERM_FACTORY = injector.getInstance(TermFactory.class);
            FUNCTION_SYMBOL_FACTORY = injector.getInstance(FunctionSymbolFactory.class);
            SUBSTITUTION_FACTORY = injector.getInstance(SubstitutionFactory.class);
            CORE_UTILS_FACTORY = injector.getInstance(CoreUtilsFactory.class);
            PUSH_DOWN_BOOLEAN_EXPRESSION_TRANSFORMER = injector.getInstance(BooleanExpressionPushDownTransformer.class);
            TRANSFORMER_FACTORY = injector.getInstance(QueryTransformerFactory.class);
            OPTIMIZER_FACTORY = injector.getInstance(OptimizerFactory.class);
            CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
            JSON_TYPE = TYPE_FACTORY.getDBTypeFactory().getDBTermType("JSON");

            NULL = TERM_FACTORY.getNullConstant();
            TRUE = TERM_FACTORY.getDBBooleanConstant(true);
            FALSE = TERM_FACTORY.getDBBooleanConstant(false);
            RDF_FACTORY = injector.getInstance(RDF.class);
            //ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /************************************************************************************************
     * @Zhenzhen Gu
     * obtain the IQ tree translated by Ontop when taking query, ontology and mapping set as input
     * @param sparql
     * @param owlFile
     * @param obdaFile
     * @param propertyFile
     * @return
     * @throws OWLException
     */
    public IQTree getIQTree(String sparql, String owlFile, String obdaFile, String propertyFile) throws OWLException {
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

        iq = st.getExecutableQuery(sparql); /**minor revise of QuestQueryprocessor to obtain IQ tree rather than SQL query*/

        return iq.getTree();
    }
    /***************************************************************************************************/


    /****************************************************************************************************
     * @Zhenzhen Gu, compute the cost of IQ tree
     * @param iqTree
     * @return
     */
    public List<Integer> getCostOfIQTree(IQTree iqTree){
        List<Integer> cost_measure = new ArrayList<Integer>(); // cost(q) = (m,n)
        int costJ = 0;
        int ineffSource = 0;
        ArrayList<IQTree> subTrees = getAllSubTree(iqTree);
        for(IQTree t : subTrees){
            if(t.isLeaf() && (t instanceof ExtensionalDataNode)){
                Set<String> sources = getSources(t);
                for(String s: sources){
                    if(labMap.containsKey(s)){
                        if(labMap.get(s).equals(SourceLab.INEFFICIENT.toString())){
                            ineffSource = ineffSource + 1;
                        }
                    }
                }
            } else {
                QueryNode root = t.getRootNode();
                if((root instanceof LeftJoinNode)){
                    costJ = costJ + getCostOfLeftJoin(t);
                } else if((root instanceof InnerJoinNode)){
                    costJ = costJ + getCostOfInnerJoin(t);
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

    /****
     * compute the cost of an atomic join, @Zhenzhen Gu
     * local join with zero value, federated join with value greater than zero;
     * */
    public int getCostOfInnerJoin(IQTree joinTree){
        int cost = 0;
        /**local join with value 0, federated join with value greater than 0, considered situation
         * inner join 和 left outer join
         * (U Ai) JOIN (U Bj), A JOIN B*/
        Set<String> sources = getSources(joinTree);
        if(sources.size() == 1){
            /**this join is a local join*/
            return 0;
        }
        ImmutableList<IQTree> subTrees = joinTree.getChildren();
        /**sub trees: subT1, ..., subTn, join patterns: subT1 JOIN subTi, 1<i*/
        /**确定Join的形式，pairs of Joins, subTi JOIN subT(i+1)*/
        for(int i=0; i<subTrees.size()-1; i++){
            cost = cost + getCostOfAtomicInnerJoin(subTrees.get(i), subTrees.get(i+1));
        }
        return cost;
    }

    public int getCostOfLeftJoin(IQTree iqt){
        int cost = 0;
        ImmutableList<IQTree> childern = iqt.getChildren(); //只有两个child
        IQTree left = childern.get(0);
        IQTree right = childern.get(1);
        if((left.getRootNode() instanceof UnionNode)){
            cost = cost + left.getChildren().size();
        } else {
            cost = cost + 1;
        }
        if((right.getRootNode() instanceof UnionNode)){
            cost = cost + right.getChildren().size();
        } else {
            cost = cost + 1;
        }

        return cost;
    }

    public int getCostOfAtomicInnerJoin(IQTree iqt1, IQTree iqt2){
        int cost = 0;
        Set<String> source_1 = getSources(iqt1);
        Set<String> source_2 = getSources(iqt2);
        if((source_1.size() == 1) && (source_2.size() == 1)){
            source_1.removeAll(source_2);
            if(source_1.size() == 0){
                return 0;
            }
        }

        if((iqt1.getRootNode() instanceof UnionNode)){
            cost = cost + iqt1.getChildren().size();
        } else {
            cost = cost + 1;
        }
        if((iqt2.getRootNode() instanceof UnionNode)){
            cost = cost + iqt2.getChildren().size();
        } else {
            cost = cost + 1;
        }
        return cost;
    }

    public Set<String> getSources(IQTree iqTree){
        Set<String> sources = new HashSet<String>();
        List<IQTree> subTrees = getAllSubTree(iqTree);
        for(IQTree t : subTrees){
            if(t.isLeaf()){
                QueryNode dn = t.getRootNode();
                if(dn instanceof ExtensionalDataNode){
                    RelationPredicate RP = ((ExtensionalDataNode) dn).getRelationDefinition().getAtomPredicate();
                    String relationName = RP.toString();
                    if(sourceMap.containsKey(relationName)){
                        sources.add(sourceMap.get(relationName));
                    }
                }
            }
        }
        return sources;
    }
    /********************************************************************************************/


    /*************************************************************************************************
     * start the query rewriting part
     * @param iqt
     * @return
     */

    public IQTree rewriteIQTree(IQTree iqt){
        iqt = removeRedundancy(iqt);
        //remove redundancy needs self-join optimization

        iqt = rewriteInnerJoin(iqt);

        iqt = rewriteLeftJoin(iqt);
        //rewriting by materialized views;

        return iqt;
    }

    /***-remove redundancy in the unions-************************/
    public IQTree removeRedundancy(IQTree iqt){
        boolean change = true;
        while(change){
            change = false;
            List<IQTree> subTrees = getAllSubTree(iqt);
            outer: for(IQTree t: subTrees){
                QueryNode root = t.getRootNode();
                if((root instanceof UnionNode)){
                    ImmutableList<IQTree> childern = t.getChildren();
                    for(int i=0; i<childern.size()-1; i++){
                        for(int j=i+1; j<childern.size(); j++){
                            IQTree sub1 = childern.get(i);
                            IQTree sub2 = childern.get(j);
                            if(checkStrictRedundancy(sub1, sub2)){
                                //remove sub1
                                if(childern.size()>2){
                                    List<IQTree> child_new = new ArrayList<IQTree>();
                                    child_new.addAll(childern);
                                    child_new.remove(sub1);
                                    ImmutableList<IQTree> list = ImmutableList.copyOf(child_new);
                                    UnionNode root_new = (UnionNode) root;
                                    IQTree iqt_new = IQ_FACTORY.createNaryIQTree(root_new, list);
                                    iqt = iqt.replaceSubTree(t, iqt_new);
                                } else {
                                    iqt = iqt.replaceSubTree(t, sub2);
                                }
                                change = true;
                                continue outer;
                            } else if(checkStrictRedundancy(sub2, sub1)){
                                //remove sub2
                                if(childern.size()>2){
                                    List<IQTree> child_new = new ArrayList<IQTree>();
                                    child_new.addAll(childern);
                                    child_new.remove(sub2);
                                    ImmutableList<IQTree> list = ImmutableList.copyOf(child_new);
                                    UnionNode root_new = (UnionNode) root;
                                    IQTree iqt_new = IQ_FACTORY.createNaryIQTree(root_new, list);
                                    iqt = iqt.replaceSubTree(t, iqt_new);
                                } else {
                                    iqt = iqt.replaceSubTree(t, sub1);
                                }
                                change = true;
                                continue outer;
                            } else if(checkEquivalentRedundancy(sub1, sub2)){
                                //remove sub1 or sub2, based on removing which part can obtain query with less cost
                                IQTree iqt1 = null;  // for removing sub1
                                IQTree iqt2 = null;  // for removing sub2
                                IQTree sub1_new = null;
                                IQTree sub2_new = null;
                                if(childern.size()>2){
                                    List<IQTree> child_new1 = new ArrayList<IQTree>();
                                    child_new1.addAll(childern);
                                    child_new1.remove(sub1);
                                    ImmutableList<IQTree> list1 = ImmutableList.copyOf(child_new1);
                                    UnionNode root_new1 = (UnionNode) root;
                                    sub1_new = IQ_FACTORY.createNaryIQTree(root_new1, list1);
                                    List<IQTree> child_new2 = new ArrayList<IQTree>();
                                    child_new2.addAll(childern);
                                    child_new2.remove(sub2);
                                    ImmutableList<IQTree> list2 = ImmutableList.copyOf(child_new2);
                                    UnionNode root_new2 = (UnionNode) root;
                                    sub2_new = IQ_FACTORY.createNaryIQTree(root_new2, list2);
                                } else {
                                    sub1_new = sub1;
                                    sub2_new = sub2;
                                }
                                iqt1 = iqt.replaceSubTree(t, sub1_new);
                                iqt2 = iqt.replaceSubTree(t, sub2_new);
                                List<Integer> cost1 = getCostOfIQTree(iqt1);
                                List<Integer> cost2 = getCostOfIQTree(iqt2);
                                if((cost1.get(0) >= cost2.get(0)) && (cost1.get(1) >= cost2.get(1))){
                                    iqt = iqt.replaceSubTree(t, sub2_new);
                                } else {
                                    iqt = iqt.replaceSubTree(t, sub1_new);
                                }
                                change = true;
                                continue outer;
                            }
                        }
                    }
                }
            }
        }
        return iqt;
    }

    public boolean checkStrictRedundancy(IQTree union_ele_1, IQTree union_ele_2){
        //the code needs to be updated according to the different ways of representing hints
        boolean b = false;
        QueryNode root_1 = union_ele_1.getRootNode();
        QueryNode root_2 = union_ele_2.getRootNode();
        if((root_1 instanceof InnerJoinNode) || (root_1 instanceof LeftJoinNode)){
            return false;
        }
        if((root_2 instanceof InnerJoinNode) || (root_2 instanceof LeftJoinNode)){
            return false;
        }
        if(union_ele_1.isLeaf() && union_ele_2.isLeaf()){
            if((union_ele_1 instanceof ExtensionalDataNode) && (union_ele_2 instanceof ExtensionalDataNode)){
                //the following code needs to be changed according to the different ways of representing hints
                RelationPredicate predicate1 = ((ExtensionalDataNode) union_ele_1).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) union_ele_2).getRelationDefinition().getAtomPredicate();
                String rel_name_1 = predicate1.toString();
                String rel_name_2 = predicate2.toString();
                //improve the following compare conditions
                if(hints.get(0).contains(rel_name_1+"<>"+rel_name_2)|| hints.get(0).contains(rel_name_2+"<>"+rel_name_1)){
                    return true;
                }
            }
        } else if((root_1 instanceof FilterNode) && (root_2 instanceof FilterNode)){
               //improve the following compare conditions
            ImmutableList<IQTree> childs_childs_1 = union_ele_1.getChildren();
            ImmutableList<IQTree> childs_childs_2 = union_ele_2.getChildren();
            IQTree child_child_1 = childs_childs_1.get(0);
            IQTree child_child_2 = childs_childs_2.get(0);
            if((child_child_1 instanceof ExtensionalDataNode) && (child_child_2 instanceof ExtensionalDataNode)){
                RelationPredicate predicate1 = ((ExtensionalDataNode) child_child_1).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) child_child_2).getRelationDefinition().getAtomPredicate();
                String rel_name_1 = predicate1.toString();
                String rel_name_2 = predicate2.toString();
                //improve the following compare conditions
                if(hints.get(0).contains(rel_name_1+"<>"+rel_name_2)|| hints.get(0).contains(rel_name_2+"<>"+rel_name_1)){
                    return true;
                }
            }
        }
        return b;
    }

    public boolean checkEquivalentRedundancy(IQTree union_ele_1, IQTree union_ele_2){
        boolean b = false;
        QueryNode root_1 = union_ele_1.getRootNode();
        QueryNode root_2 = union_ele_2.getRootNode();
        if(root_1 instanceof LeftJoinNode){
            return false;
        }
        if(root_2 instanceof LeftJoinNode){
            return false;
        }
        if(union_ele_1.isLeaf() && union_ele_2.isLeaf()){
            if((union_ele_1 instanceof ExtensionalDataNode) && (union_ele_2 instanceof ExtensionalDataNode)){
                //the following code needs to be changed according to the different ways of representing hints
                RelationPredicate predicate1 = ((ExtensionalDataNode) union_ele_1).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) union_ele_2).getRelationDefinition().getAtomPredicate();
                String rel_name_1 = predicate1.toString();
                String rel_name_2 = predicate2.toString();                //improve the following compare conditions
                if(hints.get(1).contains(rel_name_1+"<>"+rel_name_2)|| hints.get(1).contains(rel_name_2+"<>"+rel_name_1)){
                    return true;
                }
            }
        } else if((root_1 instanceof FilterNode) && (root_2 instanceof FilterNode)){
            //improve the following compare conditions
            ImmutableList<IQTree> childs_childs_1 = union_ele_1.getChildren();
            ImmutableList<IQTree> childs_childs_2 = union_ele_2.getChildren();
            IQTree child_child_1 = childs_childs_1.get(0);
            IQTree child_child_2 = childs_childs_2.get(0);
            if((child_child_1 instanceof ExtensionalDataNode) && (child_child_2 instanceof ExtensionalDataNode)){
                RelationPredicate predicate1 = ((ExtensionalDataNode) child_child_1).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) child_child_2).getRelationDefinition().getAtomPredicate();
                String rel_name_1 = predicate1.toString();
                String rel_name_2 = predicate2.toString();
                //improve the following compare conditions
                if(hints.get(1).contains(rel_name_1+"<>"+rel_name_2)|| hints.get(1).contains(rel_name_2+"<>"+rel_name_1)){
                    return true;
                }
            }
        } else if((root_1 instanceof InnerJoinNode) && (root_2 instanceof InnerJoinNode)){

            if(union_ele_1.getChildren().size() == union_ele_2.getChildren().size()){
                List<ExtensionalDataNode> leafs_left = new ArrayList<ExtensionalDataNode>();
                List<FilterNode> filter_left = new ArrayList<FilterNode>();
                List<ExtensionalDataNode> leafs_right = new ArrayList<ExtensionalDataNode>();
                List<FilterNode> filter_right = new ArrayList<FilterNode>();

                for(IQTree t: union_ele_1.getChildren()){
                    if(t instanceof ExtensionalDataNode){
                        leafs_left.add((ExtensionalDataNode)t);
                        filter_left.add(null);
                    } else if(t.getRootNode() instanceof FilterNode){
                        leafs_left.add((ExtensionalDataNode)t.getChildren().get(0));
                        filter_left.add((FilterNode) t.getRootNode());
                    } else {
                        return false;
                    }
                }
                for(IQTree t: union_ele_2.getChildren()){
                    if(t instanceof ExtensionalDataNode){
                        leafs_right.add((ExtensionalDataNode)t);
                        filter_right.add(null);
                    } else if(t.getRootNode() instanceof FilterNode){
                        leafs_right.add((ExtensionalDataNode)t.getChildren().get(0));
                        filter_right.add((FilterNode) t.getRootNode());
                    } else {
                        return false;
                    }
                }

                int index_left = -1, index_right = -1;
                for(int i=0; i<leafs_left.size(); i++){
                    String relation_left = leafs_left.get(i).getRelationDefinition().getAtomPredicate().toString();
                    ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_left = leafs_left.get(i).getArgumentMap();

                    for(int j=0; j<leafs_right.size(); j++){
                        String relation_right = leafs_right.get(j).getRelationDefinition().getAtomPredicate().toString();
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_right = leafs_right.get(j).getArgumentMap();
                        if(hints.get(1).contains(relation_left+"<>"+relation_right) || hints.get(1).contains(relation_right+"<>"+relation_left)){
                            index_left = i;
                            index_right = j;
                        }
                    }
                }
                if(index_left != -1){
                    for(int i=0; i<leafs_left.size(); i++){
                        if(i != index_left){
                            if(leafs_right.contains( leafs_left.get(i))){
                                if(filter_right.contains(filter_right.get(i))){
                                    continue;
                                } else {
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                } else{
                    return false;
                }
            }
        }
        return b;
    }

    /***-rewrite the inner join node-******************************/
    public IQTree rewriteInnerJoin(IQTree iqt){
        boolean update = true;
        int count = 0;
        module: while(update){
            update = false;
            count = count + 1;
            List<IQTree> subTrees = getAllSubTree(iqt);
            for(IQTree subt: subTrees){
                QueryNode root = subt.getRootNode();
                if((root instanceof InnerJoinNode)){
                    ImmutableList<IQTree> childern = subt.getChildren();
                    for(int i=0; i<childern.size()-1; i++){
                        for(int j=i+1; j<childern.size(); j++){
                            IQTree sub1 = childern.get(i);
                            IQTree sub2 = childern.get(j);
                            //InnerJoinNode root_new = (InnerJoinNode) root;

                            IQTree sub_new = rewriteAtomicJoin((InnerJoinNode)root, sub1, sub2);

                            if(sub_new != null){
                                IQTree iqt_new = null;
                                if(childern.size()>2){
                                    List<IQTree> childern_new = new ArrayList<IQTree>();
                                    for(int k=0; k<childern.size(); k++){
                                        if((k!=i) && (k!=j)){
                                            childern_new.add(childern.get(k));
                                        } else if(k == i){
                                            childern_new.add(sub_new);
                                        } else if(k == j){
                                            continue;
                                        }
                                    }
                                    InnerJoinNode root_new_new = (InnerJoinNode) root;
                                    IQTree subt_new = IQ_FACTORY.createNaryIQTree(root_new_new, ImmutableList.copyOf(childern_new));
                                    iqt_new = iqt.replaceSubTree(subt, subt_new);
                                } else {
                                    iqt_new = iqt.replaceSubTree(subt, sub_new);
                                }
                                List<Integer> cost1 = getCostOfIQTree(iqt);
                                List<Integer> cost2 = getCostOfIQTree(iqt_new);

                                if((cost1.get(0) >= cost2.get(0))&&((cost1.get(1) >= cost2.get(1)))){
                                    iqt = iqt_new;
                                    update = true;
                                    continue module;
                                }
                            }
                        }
                    }
                }
                // }
            }
        }


        return iqt;
    }

    public IQTree rewriteAtomicJoin(InnerJoinNode root, IQTree left_part, IQTree right_part){
        //format (A1 UNION ... UNION An) JOIN (B1 UNION ... UNION Bm)

        IQTree iqt_new = null;

        boolean can_rewrite = false;

        InnerJoinNode ij_copy = root;
        QueryNode root_l = left_part.getRootNode();
        QueryNode root_r = right_part.getRootNode();
        ImmutableSet<Variable> var_l = left_part.getVariables();
        ImmutableSet<Variable> var_r = right_part.getVariables();
        Set<Variable> vars_all = new HashSet<Variable>();
        vars_all.addAll(var_l); vars_all.addAll(var_r);
        if(vars_all.size() == (var_l.size()+var_r.size())){
            return null;
        }

        if((root_l instanceof InnerJoinNode) || (root_l instanceof LeftJoinNode) || (root_r instanceof InnerJoinNode) || (root_r instanceof LeftJoinNode)){
            return iqt_new;
        }
        List<IQTree> childern_l = new ArrayList<IQTree>();
        List<IQTree> childern_r = new ArrayList<IQTree>();

        if((root_l instanceof UnionNode) || (left_part.isLeaf())){
            childern_l.addAll(left_part.getChildren());
        }
        if((root_r instanceof UnionNode) || (right_part.isLeaf())){
            childern_r.addAll(right_part.getChildren());
        }
        List<IQTree> SubTree_new = new ArrayList<IQTree>();

        for(int i=0; i<childern_l.size(); i++){
            for(int j=0; j<childern_r.size(); j++){
                IQTree child_l = childern_l.get(i);
                IQTree child_r = childern_r.get(j);

                // check whether (child_l JoinNode child_r) can be rewritten into empty join or by materialized view
                ExpRewriten rewrite = rewriteAtomicJoinWithoutUnionInLeftAndRight(root, child_l, child_r);

                if(rewrite.newRewritten != null){
                    SubTree_new.add(rewrite.newRewritten);
                }
                if(rewrite.canRewrite || rewrite.sjRewrite){
                    can_rewrite = true;
                }
            }
        }

        if(can_rewrite){  //some Ai JOIN Bj can rewritten into empty relation or materialized view or can apply sjr
            if(SubTree_new.size() == 1){
                iqt_new = SubTree_new.get(0);
            } else if(SubTree_new.size() > 1){
                ImmutableSet<Variable> variables_l = root_l.getLocalVariables();
                ImmutableSet<Variable> variables_r = root_r.getLocalVariables();
                Set<Variable> vars = new HashSet<Variable>();
                vars.addAll(variables_l); vars.addAll(variables_r);
                ImmutableSet<Variable> allVars = ImmutableSet.copyOf(vars);
                UnionNode root_new = IQ_FACTORY.createUnionNode(allVars);
                ImmutableList<IQTree> childern_new = ImmutableList.copyOf(SubTree_new);
                iqt_new = IQ_FACTORY.createNaryIQTree(root_new, childern_new);
            } else { //(join of unions ) rewritten into empty relation
                ImmutableSet<Variable> vars = root.getLocalVariables();
                EmptyNode en = IQ_FACTORY.createEmptyNode(vars);
                iqt_new = en;
            }
        }

        return iqt_new;
    }

    public ExpRewriten rewriteAtomicJoinWithoutUnionInLeftAndRight(InnerJoinNode root, IQTree left, IQTree right){
        //Rewriting by empty federated joins

        ExpRewriten ER = new ExpRewriten();
        //complete the checking conditions
        //Left A, A1 JOIN A2 JOIN ... An
        //Right B, B1 JOIN B2 JOIN ... JOIN Bm
        //A, B, Ai, Bi, without UNION

        QueryNode root_l = left.getRootNode();
        QueryNode root_r = right.getRootNode();

        JoinOfLeafs JOL_left = getLeafsOfJoin(left);
        JoinOfLeafs JOL_right = getLeafsOfJoin(right);
        //keep the order of the leafs
        if((JOL_left.dataNodes.size() == 0) || (JOL_right.dataNodes.size() == 0)){
            return ER;
        }


        for(int i=0; i<JOL_left.dataNodes.size(); i++){
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_left = JOL_left.dataNodes.get(i).getArgumentMap();
            for(int j=0; j<JOL_right.dataNodes.size(); j++){
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_right = JOL_right.dataNodes.get(j).getArgumentMap();
                for(int k: JOL_left.dataNodes.get(i).getArgumentMap().keySet()){
                    for(int l: JOL_right.dataNodes.get(j).getArgumentMap().keySet()){
                        if(JOL_left.dataNodes.get(i).getArgumentMap().get(k).equals(JOL_right.dataNodes.get(j).getArgumentMap().get(l))){
                            //change the check condition based on different ways of representing hints
                            if(hints.get(2).contains(JOL_left.relations.get(i).getName()+"<>"+JOL_right.relations.get(j).getName()+"<>"+k+"<>"+l) || hints.get(2).contains(JOL_right.relations.get(j).getName()+"<>"+JOL_left.relations.get(i).getName()+"<>"+l+"<>"+k)){
                                ER.canRewrite = true;
                                return ER;
                            }
                            // check rewriting by materialized views
                        }
                    }
                }

            }
        }

        ER = createJoinTree(root, JOL_left, JOL_right);

        //the input (left JOIN right) cannot be rewritten by empty federated join and materialized views
        //create a IQTree for (left JOIN right), by applying self-join optimization

        return ER;
    }

    //recreate join tree where sfr rule is applied
    public ExpRewriten createJoinTree(InnerJoinNode root, JoinOfLeafs left, JoinOfLeafs right){
        ExpRewriten ER = new ExpRewriten();

        Set<Variable> vars_l_r = new HashSet<>();
        List<IQTree> childern_new = new ArrayList<IQTree>();
        for(int i=0; i<left.dataNodes.size(); i++){
            IQTree t = left.dataNodes.get(i);
            FilterNode fn = left.filters.get(i);

            List<Integer> index_right = new ArrayList<Integer>();
            Map<Integer, VariableOrGroundTerm> arg_maps = new HashMap<Integer, VariableOrGroundTerm>();
            if(right.relations.contains(left.relations.get(i))){ // try sjr rewriting
                for(int j=0; j<right.relations.size(); j++){
                    if(right.relations.get(j).equals(left.relations.get(i))){
                        for(int index: getSinglePrimaryKeyIndexOfRelations(left.dataNodes.get(i))){
                            if(left.dataNodes.get(i).getArgumentMap().containsKey(index) && right.dataNodes.get(j).getArgumentMap().containsKey(index) &&
                                    left.dataNodes.get(i).getArgumentMap().get(index).equals(right.dataNodes.get(j).getArgumentMap().get(index))){
                                index_right.add(j);
                                arg_maps.putAll(right.dataNodes.get(j).getArgumentMap());
                                ER.sjRewrite = true;
                            }
                        }
                    }
                }
            }

            if(index_right.size() == 0){
                if(fn != null){
                    IQTree t_new = IQ_FACTORY.createUnaryIQTree(fn, t);
                    childern_new.add(t_new);
                } else {
                    childern_new.add(t);
                }
                vars_l_r.addAll(t.getVariables());
            } else {
                arg_maps.putAll(left.dataNodes.get(i).getArgumentMap());
                ExtensionalDataNode dataNode_new = IQ_FACTORY.createExtensionalDataNode(left.dataNodes.get(i).getRelationDefinition(), ImmutableMap.copyOf(arg_maps));
                //all the filter conditions are AND
                List<ImmutableExpression> conditions = new ArrayList<ImmutableExpression>();
                if(left.filters.get(i) != null){
                    BooleanFunctionSymbol bfs = left.filters.get(i).getFilterCondition().getFunctionSymbol();
                    if(bfs.toString().startsWith("AND")){ //change
                        for(ImmutableTerm it: left.filters.get(i).getFilterCondition().getTerms()){
                            conditions.add((ImmutableExpression) it);
                        }
                    } else {
                        conditions.add(left.filters.get(i).getFilterCondition());
                    }
                }
                for(int j: index_right){
                    if(right.filters.get(j) != null){
                        BooleanFunctionSymbol bfs = right.filters.get(j).getFilterCondition().getFunctionSymbol();
                        if(bfs.toString().startsWith("AND")){ //change
                            for(ImmutableTerm it: right.filters.get(j).getFilterCondition().getTerms()){
                                conditions.add((ImmutableExpression) it);
                            }
                        } else {
                            conditions.add(right.filters.get(j).getFilterCondition());
                        }
                    }
                    vars_l_r.addAll(right.dataNodes.get(j).getVariables());
                    right.dataNodes.remove(j);
                    right.filters.remove(j);
                }
                if(conditions.size() == 0){
                    childern_new.add(dataNode_new);
                } else {
                    ImmutableExpression exp = TERM_FACTORY.getConjunction(ImmutableList.copyOf(conditions));//null; //null;
                    FilterNode fn_new = IQ_FACTORY.createFilterNode(exp);
                    childern_new.add(IQ_FACTORY.createUnaryIQTree(fn_new, dataNode_new));
                }
            }
        }
        for(int i=0; i<right.dataNodes.size(); i++){
            IQTree t = right.dataNodes.get(i);
            FilterNode fn = right.filters.get(i);
            if(fn != null){
                IQTree t_new = IQ_FACTORY.createUnaryIQTree(fn, t);
                childern_new.add(t_new);
            } else {
                childern_new.add(t);
            }
            vars_l_r.addAll(t.getVariables());
        }

        InnerJoinNode root_new = root;
        ImmutableSet<Variable> vars = root.getLocalVariables();
        //可能childern_new只有一个孩子, self-join rule的应用;;
        if(childern_new.size() == 1){
            if(vars.size() == 0){
                //没有join condition
                ER.newRewritten = childern_new.get(0);
            } else {
                if(vars_l_r.containsAll(vars)){
                    FilterNode fn = IQ_FACTORY.createFilterNode(root.getOptionalFilterCondition().get());
                    ER.newRewritten = IQ_FACTORY.createUnaryIQTree(fn, childern_new.get(0));
                } else {
                    //split the join condition
                    ER.newRewritten = childern_new.get(0);
                }
            }
        } else {
            if(vars.size() == 0){
                ER.newRewritten = IQ_FACTORY.createNaryIQTree(root_new, ImmutableList.copyOf(childern_new));
            } else {
                if(vars_l_r.containsAll(vars)){
                    ER.newRewritten = IQ_FACTORY.createNaryIQTree(root_new, ImmutableList.copyOf(childern_new));
                } else {
                    //need to creat new Innor Join Node with files operating on the variables of the sub-trees;
                    //the following condition needs to be changed again
                    InnerJoinNode IJN = IQ_FACTORY.createInnerJoinNode();
                    ER.newRewritten = IQ_FACTORY.createNaryIQTree(IJN, ImmutableList.copyOf(childern_new));
                }
            }
        }

        return ER;
    }

    public JoinOfLeafs getLeafsOfJoin(IQTree t){
        JoinOfLeafs JOL = new JoinOfLeafs();

        if(t.isLeaf()){
            if(t instanceof ExtensionalDataNode){
                JOL.dataNodes.add((ExtensionalDataNode) t);
                JOL.filters.add(null);
                JOL.relations.add(((ExtensionalDataNode) t).getRelationDefinition().getAtomPredicate());
            }
        } else if (t.getRootNode() instanceof FilterNode){
            if(t.getChildren().get(0) instanceof ExtensionalDataNode){
                JOL.filters.add((FilterNode)t.getRootNode());
                JOL.dataNodes.add((ExtensionalDataNode)t.getChildren().get(0));
                JOL.relations.add(((ExtensionalDataNode)t.getChildren().get(0)).getRelationDefinition().getAtomPredicate());
            }
        } else if(t.getRootNode() instanceof InnerJoinNode){
            JOL.conditions = ((InnerJoinNode) t.getRootNode()).getOptionalFilterCondition();
            for(IQTree t_new: t.getChildren()){
                if(t_new.isLeaf()){
                    if(t_new instanceof ExtensionalDataNode){
                        JOL.dataNodes.add((ExtensionalDataNode)t_new);
                        JOL.filters.add(null);
                        JOL.relations.add(((ExtensionalDataNode) t_new).getRelationDefinition().getAtomPredicate());
                    }
                } else if(t_new.getRootNode() instanceof FilterNode){
                    if(t_new.getChildren().get(0) instanceof ExtensionalDataNode){
                        JOL.filters.add((FilterNode) t_new.getRootNode());
                        JOL.dataNodes.add((ExtensionalDataNode) t_new.getChildren().get(0));
                        JOL.relations.add(((ExtensionalDataNode) t_new.getChildren().get(0)).getRelationDefinition().getAtomPredicate());
                    }
                }  else if(t_new.getRootNode() instanceof InnerJoinNode){
                    //lift join condition
                    for(IQTree sub: t_new.getChildren()){
                        if(sub instanceof ExtensionalDataNode){
                            JOL.dataNodes.add((ExtensionalDataNode)sub);
                            JOL.filters.add(null);
                            JOL.relations.add(((ExtensionalDataNode)sub).getRelationDefinition().getAtomPredicate());
                        } else if(sub.getRootNode() instanceof FilterNode){
                            JOL.filters.add((FilterNode)sub.getRootNode());
                            JOL.dataNodes.add((ExtensionalDataNode) sub.getChildren().get(0));
                            JOL.relations.add(((ExtensionalDataNode) sub.getChildren().get(0)).getRelationDefinition().getAtomPredicate());
                        }
                    }
                }
                else {
                    return JOL;
                }
            }

        } else {
            return JOL;
        }
        return JOL;
    }

    public List<Integer> getSinglePrimaryKeyIndexOfRelations(ExtensionalDataNode dataNode){
        //unique primary key index
        List<Integer> index = new ArrayList<Integer>();

        ImmutableList<Attribute> attrs = dataNode.getRelationDefinition().getAttributes();
        for(UniqueConstraint uc: dataNode.getRelationDefinition().getUniqueConstraints()){
            if(uc.isPrimaryKey()){
                ImmutableList<Attribute> ats = uc.getAttributes();
                if(ats.size() == 1){
                    index.add(attrs.indexOf(ats.get(0)));
                }
            }
        }

        return index;
    }

    /***-rewrite the left outer join node-**********************/
    public IQTree rewriteLeftJoin(IQTree iqt){
        boolean update = true;
        module: while(update){
            update = false;
            List<IQTree> subTrees = getAllSubTree(iqt);

            for(IQTree t : subTrees){
                QueryNode qn = t.getRootNode();
                if(qn instanceof LeftJoinNode){
                    Set<String> sources = getSources(t);
                    if(sources.size() == 1){
                        continue;
                    }
                    ImmutableList<IQTree> childern =t.getChildren(); // only have two childern
                    ExpRewriten rewriten = rewriteAtomicLeftJoin((LeftJoinNode)qn, childern.get(0), childern.get(1));
                    if(rewriten.canRewrite){
                        IQTree t_new = iqt.replaceSubTree(t, rewriten.newRewritten);
                        List<Integer> cost_new = getCostOfIQTree(t_new);
                        List<Integer> cost_old = getCostOfIQTree(iqt);
                        if((cost_old.get(0)>= cost_new.get(0)) && (cost_old.get(1) >= cost_new.get(1))){
                            update = true;
                            iqt = t_new;
                            continue module;
                        }

                    } else {
                        continue;
                    }
                }
            }
        }
        return iqt;
    }

    public ExpRewriten rewriteAtomicLeftJoin(LeftJoinNode root, IQTree left, IQTree right){
        //format: (A1 UNION ... UNION Am) LOJ (B1 UNION ... UNION Bn), Ai, Bj leaf or join tree

        ExpRewriten ER = new ExpRewriten();
        if((left.getRootNode() instanceof LeftJoinNode) || (right.getRootNode() instanceof LeftJoinNode)){
            return ER;
        }

        List<JoinOfLeafs> left_part = new ArrayList<JoinOfLeafs>();
        List<JoinOfLeafs> right_part = new ArrayList<JoinOfLeafs>();

        if((left instanceof ExtensionalDataNode) || (left.getRootNode() instanceof FilterNode) || (left.getRootNode() instanceof InnerJoinNode) ){
            if(getLeafsOfJoin(left).dataNodes.size() > 0){
                left_part.add(getLeafsOfJoin(left));
            } else {
                return ER;
            }
        } else if(left.getRootNode() instanceof UnionNode){
            for(IQTree t: left.getChildren()){
                if((t instanceof ExtensionalDataNode) || (t.getRootNode() instanceof FilterNode) || (t.getRootNode() instanceof InnerJoinNode)){
                    if(getLeafsOfJoin(t).dataNodes.size()>0){
                        left_part.add(getLeafsOfJoin(t));
                    } else {
                        return ER;
                    }
                }
            }
        } else {
            return ER;
        }

        if((right instanceof ExtensionalDataNode) || (right.getRootNode() instanceof FilterNode) || (right.getRootNode() instanceof InnerJoinNode) ){
            if(getLeafsOfJoin(right).dataNodes.size() > 0){
                right_part.add(getLeafsOfJoin(right));
            } else {
                return ER;
            }
        } else if(right.getRootNode() instanceof UnionNode){
            for(IQTree t: right.getChildren()){
                if((t instanceof ExtensionalDataNode) || (t.getRootNode() instanceof FilterNode) || (t.getRootNode() instanceof InnerJoinNode)){
                    if(getLeafsOfJoin(t).dataNodes.size()>0){
                        right_part.add(getLeafsOfJoin(t));
                    } else {
                        return ER;
                    }
                }
            }
        } else {
            return ER;
        }

        Map<Integer, Integer> index = new HashMap<Integer, Integer>(); // Ai JOIN Bj no empty
        for(int i=0; i<left_part.size(); i++){
            for(int k=0; k<right_part.size(); k++){
                boolean label = false;

                for(int j=0; j<left_part.get(i).dataNodes.size(); j++){
                    for(int l=0; l<right_part.get(k).dataNodes.size(); l++){
                        RelationPredicate relation_left = left_part.get(i).relations.get(j);
                        String name_left = relation_left.toString();
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_left = left_part.get(i).dataNodes.get(j).getArgumentMap();

                        RelationPredicate relation_right = right_part.get(k).relations.get(l);
                        String name_right = relation_right.toString();
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_right = right_part.get(k).dataNodes.get(l).getArgumentMap();

                        for(int f: arg_left.keySet()){
                            for(int h: arg_right.keySet()){
                                if((arg_left.get(f) instanceof Variable)&&(arg_right.get(h) instanceof Variable)&&(arg_left.get(f).equals(arg_right.get(h)))){
                                    if(hints.get(2).contains(name_left+"<>"+name_right+"<>"+f+"<>"+h)||hints.get(2).contains(name_right+"<>"+name_left+"<>"+h+"<>"+f)){
                                        label = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if(!label){
                    if(left_part.size() == 1){
                        index.put(k,i);
                    } else{
                        if(!index.containsKey(i)){
                            index.put(i, k);
                        } else {
                            return ER;
                        }
                    }
                }
            }
        }

        //check the pairwise disjoint feature of the elements in the unions in the left hand side.
        if(left_part.size()>1 && right_part.size()>1){
            for(int i=0; i<left_part.size(); i++){
                for(int j=i+1; j<left_part.size(); j++){
                    boolean b = false;
                    for(int k=0; k<left_part.get(i).dataNodes.size(); k++){
                        RelationPredicate relation_1 = left_part.get(i).relations.get(k);
                        String name_1 = relation_1.toString();
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_1 = left_part.get(i).dataNodes.get(k).getArgumentMap();
                        for(int l=0; l<left_part.get(j).dataNodes.size(); l++){
                            RelationPredicate relation_2 = left_part.get(j).relations.get(l);
                            String name_2 = relation_2.toString();
                            ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_2 = left_part.get(j).dataNodes.get(l).getArgumentMap();
                            for(int h: arg_1.keySet()){
                                for(int f: arg_2.keySet()){
                                    if(arg_1.get(h).equals(arg_2.get(f))){
                                        if(hints.get(2).contains(name_1+"<>"+name_2+"<>"+h+"<>"+f)||hints.get(2).contains(name_2+"<>"+name_1+"<>"+f+"<>"+h)){
                                            b = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!b){
                        return ER;
                    }
                }
            }
        }

        if(index.size() == left_part.size()){
            List<IQTree> subtrees = new ArrayList<IQTree>();
            for(int i: index.keySet()){
                int j = index.get(i);
                if(right_part.get(j).dataNodes.size()==1){
                    boolean b = false; //self-left-join check
                    int ind_i = 0;
                    RelationPredicate relation_j = right_part.get(j).relations.get(0);
                    ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_j = right_part.get(j).dataNodes.get(0).getArgumentMap();

                    if(left_part.get(i).relations.contains(relation_j)){
                        int ind = left_part.get(i).relations.indexOf(relation_j);
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_i = left_part.get(i).dataNodes.get(ind).getArgumentMap();
                        List<Integer> pk_index_i = getSinglePrimaryKeyIndexOfRelations(left_part.get(i).dataNodes.get(ind));
                        for(int pk_ind: pk_index_i){
                            if(arg_j.containsKey(pk_ind) && arg_i.containsKey(pk_ind) && (arg_j.get(pk_ind).equals(arg_i.get(pk_ind)))){
                                b=true;
                                ind_i = ind;
                            }
                        }
                    }
                    if(b){
                        //first create a new data node
                        Map<Integer, VariableOrGroundTerm> args = new HashMap<Integer, VariableOrGroundTerm>();
                        args.putAll(arg_j);
                        args.putAll(left_part.get(i).dataNodes.get(ind_i).getArgumentMap());
                        ExtensionalDataNode data_new = IQ_FACTORY.createExtensionalDataNode(left_part.get(i).dataNodes.get(ind_i).getRelationDefinition(), ImmutableMap.copyOf(args));
                        List<ImmutableExpression> cond_new = new ArrayList<ImmutableExpression>();
                        FilterNode fn_new = null;
                        if(left_part.get(i).filters.get(ind_i) != null){
                            if(left_part.get(i).filters.get(ind_i).getFilterCondition().getFunctionSymbol().getName().startsWith("AND")){
                                for(ImmutableTerm it : left_part.get(i).filters.get(ind_i).getFilterCondition().getTerms()){
                                    cond_new.add((ImmutableExpression)it);
                                }
                            } else {
                                cond_new.add(left_part.get(i).filters.get(ind_i).getFilterCondition());
                            }
                        }
                        if(right_part.get(j).filters.get(0) != null){
                            if(right_part.get(j).filters.get(0).getFilterCondition().getFunctionSymbol().getName().startsWith("AND")){
                                for(ImmutableTerm it : right_part.get(j).filters.get(0).getFilterCondition().getTerms()){
                                    cond_new.add((ImmutableExpression)it);
                                }
                            } else {
                                cond_new.add(right_part.get(j).filters.get(0).getFilterCondition());
                            }
                        }
                        if(cond_new.size() == 1){
                            fn_new = IQ_FACTORY.createFilterNode(cond_new.get(0));
                        } else if(cond_new.size() > 1){
                            fn_new = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.copyOf(cond_new)));
                        }

                        if(left_part.get(i).dataNodes.size() == 1){
                             if(fn_new == null){
                                 subtrees.add(data_new);
                             } else {
                                 subtrees.add(IQ_FACTORY.createUnaryIQTree(fn_new, data_new));
                             }
                        } else {
                            InnerJoinNode join = IQ_FACTORY.createInnerJoinNode(left_part.get(i).conditions);
                            List<IQTree> sub_sub_t = new ArrayList<IQTree>();
                            for(int l=0; l<left_part.get(i).dataNodes.size(); l++){
                                if(l != ind_i){
                                    if(left_part.get(i).filters.get(l) == null){
                                        sub_sub_t.add(left_part.get(i).dataNodes.get(l));
                                    } else {
                                        sub_sub_t.add(IQ_FACTORY.createUnaryIQTree(left_part.get(i).filters.get(l), left_part.get(i).dataNodes.get(l)));
                                    }
                                } else {
                                    if(fn_new == null){
                                        sub_sub_t.add(data_new);
                                    } else {
                                        sub_sub_t.add(IQ_FACTORY.createUnaryIQTree(fn_new, data_new));
                                    }
                                }
                            }
                            subtrees.add(IQ_FACTORY.createNaryIQTree(join, ImmutableList.copyOf(sub_sub_t)));
                        }
                    } else {
                        LeftJoinNode root_subtree = IQ_FACTORY.createLeftJoinNode();
                        subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left.getChildren().get(i), right.getChildren().get(j)));
                    }
                } else {
                    LeftJoinNode root_subtree = IQ_FACTORY.createLeftJoinNode();
                    subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left.getChildren().get(i), right.getChildren().get(j)));
                }
            }

            ER.canRewrite = true;
            if(subtrees.size() == 1){
                ER.newRewritten = subtrees.get(0);
            } else {
                Set<Variable> vars = new HashSet<Variable>();
                vars.addAll(left.getVariables());
                vars.addAll(right.getVariables());
                UnionNode root_new = IQ_FACTORY.createUnionNode(ImmutableSet.copyOf(vars));
                ER.newRewritten = IQ_FACTORY.createNaryIQTree(root_new, ImmutableList.copyOf(subtrees));
            }
        }

        return ER;
    }

    /*******************************************************************************************************/

    /***********************************************************************************************
     * translate the rewritten IQ query into SQL query
     * @param iqt
     * @return
     */
    public String IQTree2SQL(IQTree iqt){
        //TODO
        //later, not hard
        String sql = "";

//        sql = Optional.of(iqt)
//                .filter(t -> t instanceof UnaryIQTree)
//                .map(t -> ((UnaryIQTree) t).getChild().getRootNode())
//                .filter(n -> n instanceof NativeNode)
//                .map(n -> ((NativeNode) n).getNativeQueryString())
//                .orElseThrow(() -> new RuntimeException("Cannot extract the SQL query from\n" + iqt));
//
        return sql;
    }

    public String queryRewrite(String sparql, String owlFile, String obdaFile, String propertyFile, String hintFile, String labFile) throws OWLException {
        String SQL = "";
        IQTree iqt = getIQTree(sparql, owlFile, obdaFile, propertyFile);
        iqt = rewriteIQTree(iqt);
        SQL = IQTree2SQL(iqt);
        return SQL;
    }

    @Test
    public void testPart(){
        try{
            QueryRewriting QR = new QueryRewriting();
            IQTree iqt = QR.getIQTree(query, owlFile, obdaFile, propertyFile);
            System.out.println("generated IQ tree:");
            System.out.println(iqt);
            List<Integer> cost = QR.getCostOfIQTree(iqt);

            System.out.println("computed cost: "+cost);
            IQTree iqt_new = QR.rewriteIQTree(iqt);
            System.out.println("new generated tree");
            System.out.println(iqt_new);
            System.out.println("computed cost: "+QR.getCostOfIQTree(iqt_new));

//            List<IQTree> subtrees = getAllSubTree(iqt);
//            for(IQTree t: subtrees){
//                if(t.getRootNode() instanceof FilterNode){
//                    FilterNode fn = (FilterNode) t.getRootNode();
//                    System.out.println(fn);
//                    System.out.println(fn.getFilterCondition());
//                    System.out.println(fn.getFilterCondition().getTerms());
//                    System.out.println();
//
//                }
//            }


        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class ExpRewriten{
    public boolean canRewrite;
    public boolean sjRewrite;
    public IQTree newRewritten;

    public ExpRewriten(){
        canRewrite = false;
        sjRewrite = false;
        newRewritten = null;
    }
}

class JoinOfLeafs{
    public List<ExtensionalDataNode> dataNodes;
    public List<FilterNode> filters;
    public List<RelationPredicate> relations;
    public Optional<ImmutableExpression> conditions;

    public JoinOfLeafs(){
        dataNodes = new ArrayList<ExtensionalDataNode>();
        filters = new ArrayList<FilterNode>();
        relations = new ArrayList<RelationPredicate>();
        conditions = null;
    }
}
