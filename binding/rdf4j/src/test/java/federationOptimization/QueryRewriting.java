package federationOptimization;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.reformulation.generation.NativeQueryGenerator;
import it.unibz.inf.ontop.injection.*;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
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
            if(t.isLeaf()){
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

        iqt = rewriteInnerJoin(iqt);

        iqt = rewriteLeftJoin(iqt);

        return iqt;
    }

    /***-remove redundancy in the unions-************************/
    public IQTree  removeRedundancy(IQTree iqt){
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
                        IQTree sub1 = childern.get(i);
                        IQTree sub2 = childern.get(i+1);
                        InnerJoinNode root_new = (InnerJoinNode) root;

                        IQTree sub_new = rewriteAtomicJoin(root_new, sub1, sub2);
                        if(sub_new != null){
                            IQTree iqt_new = null;
                            if(childern.size()>2){
                                List<IQTree> childern_new = new ArrayList<IQTree>();
                                for(int j=0; j<childern.size(); j++){
                                    if((j!=i) && (j!=(i+1))){
                                        childern_new.add(childern.get(j));
                                    } else if(j == i){
                                        childern_new.add(sub_new);
                                    } else if(j == (i+1)){
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

                            if((cost1.get(0) != cost2.get(0))||(cost1.get(1) != cost2.get(1))){
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
                if(rewrite.canRewrite){
                    can_rewrite = true;
                }
            }
        }

        if(can_rewrite){  //some Ai JOIN Bj can rewritten into empty relation or materialized view
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
        } else {
            return iqt_new;
        }


        return iqt_new;
    }

    public ExpRewriten rewriteAtomicJoinWithoutUnionInLeftAndRight(InnerJoinNode root, IQTree left, IQTree right){
        ExpRewriten ER = new ExpRewriten();
        //complete the checking conditions
        //Left A, A1 JOIN A2 JOIN ... An
        //Right B, B1 JOIN B2 JOIN ... JOIN Bm
        //A, B, Ai, Bi, without UNION

//        System.out.println("start rewriting union elements");

        QueryNode root_l = left.getRootNode();
        QueryNode root_r = right.getRootNode();
        Map<ExtensionalDataNode, FilterNode> leaf_filter_left = new HashMap<ExtensionalDataNode, FilterNode>();
        Map<ExtensionalDataNode, FilterNode> leaf_filter_right = new HashMap<ExtensionalDataNode, FilterNode>();
        Optional<ImmutableExpression> on_join_left = null;
        Optional<ImmutableExpression> on_join_right = null;

        if(left.isLeaf()){
            if(left instanceof ExtensionalDataNode){
                leaf_filter_left.put((ExtensionalDataNode) left, null);
            }
        } else if (root_l instanceof FilterNode) {
            ImmutableList<IQTree> childern = left.getChildren();
            for(IQTree t: childern){
                if(t instanceof ExtensionalDataNode){
                    leaf_filter_left.put((ExtensionalDataNode)t, (FilterNode) root_l);
                }
            }
        } else if(root_l instanceof InnerJoinNode){
            on_join_left = ((InnerJoinNode) root_l).getOptionalFilterCondition();
            ImmutableList<IQTree> childern = left.getChildren();
            for(IQTree t: childern){
                Set<FilterNode> set = new HashSet<FilterNode>();
                if(t.isLeaf()){
                    if(t instanceof ExtensionalDataNode){
                        leaf_filter_left.put((ExtensionalDataNode)t, null);
                    }
                } else if(t.getRootNode() instanceof FilterNode){
                    for(IQTree sub_t: t.getChildren()){
                        if(sub_t instanceof ExtensionalDataNode){
                            leaf_filter_left.put((ExtensionalDataNode)sub_t, (FilterNode) t.getRootNode());
                        }
                    }
                } else {
                    return ER;
                }
            }

        } else {
            return ER;
        }

        if(right.isLeaf()){
            if(right instanceof ExtensionalDataNode){
                leaf_filter_right.put((ExtensionalDataNode)right, null);
            }
        } else if (root_r instanceof FilterNode) {
            ImmutableList<IQTree> childern = right.getChildren();
            for(IQTree t: childern){
                if(t instanceof ExtensionalDataNode){
                    leaf_filter_right.put((ExtensionalDataNode)t, (FilterNode) root_r);
                }
            }
        } else if(root_r instanceof InnerJoinNode){
            on_join_right = ((InnerJoinNode) root_r).getOptionalFilterCondition();
            ImmutableList<IQTree> childern = right.getChildren();
            for(IQTree t: childern){
                if(t.isLeaf()){
                    if(t instanceof ExtensionalDataNode){
                        leaf_filter_right.put((ExtensionalDataNode)t, null);
                    }
                } else if(t.getRootNode() instanceof FilterNode){
                    for(IQTree sub_t: t.getChildren()){
                        if(sub_t instanceof ExtensionalDataNode){
                            leaf_filter_right.put((ExtensionalDataNode)sub_t, (FilterNode) t.getRootNode());
                        }
                    }
                } else {
                    return ER;
                }
            }

        } else {
            return ER;
        }

        for(ExtensionalDataNode ele_left: leaf_filter_left.keySet()){
            for(ExtensionalDataNode ele_right: leaf_filter_right.keySet()){

                RelationPredicate predict_left = ele_left.getRelationDefinition().getAtomPredicate();
                RelationPredicate predict_right = ele_right.getRelationDefinition().getAtomPredicate();
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_left = ele_left.getArgumentMap();
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_right = ele_right.getArgumentMap();
                for(int i: arg_map_left.keySet()){
                    for(int j: arg_map_right.keySet()){
                        if(arg_map_left.get(i).equals(arg_map_right.get(j))){
                            String rel1 = predict_left.toString();
                            String rel2 = predict_right.toString();
                            //change the check condition based on different ways of representing hints

                            if(hints.get(2).contains(rel1+"<>"+rel2+"<>"+i+"<>"+j) || hints.get(2).contains(rel2+"<>"+rel1+"<>"+j+"<>"+i)){
                                ER.canRewrite = true;
                                return ER;
                            }
                            // check rewriting by materialized views
                        }
                    }
                }
                //the input (left JOIN right) cannot be rewritten by empty federated join and materialized views
                //create a IQTree for (left JOIN right)
                Set<Variable> vars_l_r = new HashSet<>();
                List<IQTree> childern_new = new ArrayList<IQTree>();
                for(IQTree t : leaf_filter_left.keySet()){
                    if(!(leaf_filter_left.get(t) == null)){
                        IQTree t_new = IQ_FACTORY.createUnaryIQTree(leaf_filter_left.get(t), t);
                        childern_new.add(t);
                    } else {
                        childern_new.add(t);
                    }
                    vars_l_r.addAll(t.getVariables());
                }
                for(IQTree t : leaf_filter_right.keySet()){
                    if(!(leaf_filter_left.get(t) == null)){
                        IQTree t_new = IQ_FACTORY.createUnaryIQTree(leaf_filter_right.get(t), t);
                        childern_new.add(t);
                    } else {
                        childern_new.add(t);
                    }
                    vars_l_r.addAll(t.getVariables());
                }
                InnerJoinNode root_new = root;
                ImmutableSet<Variable> vars = root.getLocalVariables();
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
        }

        return ER;
    }

    /***-rewrite the left outer join node-**********************/
    public IQTree rewriteLeftJoin(IQTree iqt){
        boolean update = true;
        while(update){
            update = false;
            //rewrite iqt;
        }
        return iqt;
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
            System.out.println("computed cost: "+QR.getCostOfInnerJoin(iqt_new));

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class ExpRewriten{
    public boolean canRewrite;
    public IQTree newRewritten;

    public ExpRewriten(){
        canRewrite = false;
        newRewritten = null;
    }
}
