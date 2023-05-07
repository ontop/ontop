package federationOptimization.queryRewriting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import federationOptimization.precomputation.SourceLab;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.impl.NativeNodeImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.query.KGQueryFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class QueryRewriting {

    public OntopSQLOWLAPIConfiguration configuration;

    public ArrayList<Set<String>> hints;
    public HashMap<String, String> hint_matv;
    public static HashMap<String, String> labMap;
    public static HashMap<String, String> sourceMap;

    public static IntermediateQueryFactory IQ_FACTORY;
    public static AtomFactory ATOM_FACTORY;
    public static TermFactory TERM_FACTORY;
    public static CoreSingletons CORE_SINGLETONS;

    public static QuotedIDFactory idFactory;
    public static DBTypeFactory dbTypeFactory;
    public QueryRewriting(String owlPath, String obdaPath, String PROPERTY, String hintFile, String labFile, String sourceFile, String effLabel) {
        try {
            this.configuration = OntopSQLOWLAPIConfiguration
                    .defaultBuilder()
                    .ontologyFile(owlPath)
                    .nativeOntopMappingFile(obdaPath)
                    .propertyFile(PROPERTY)
                    .enableTestMode()
                    .build();

            hints = new ArrayList<Set<String>>();
            hint_matv = new HashMap<String, String>();
            labMap = new HashMap<String, String>();
            sourceMap = new HashMap<String, String>();

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
            while((line=br_new.readLine()) != null ){
                String[] arr = line.split("-");
                labMap.put(arr[0], arr[1]);
            }
            br_new.close();

            BufferedReader br_hint = new BufferedReader(new InputStreamReader(new FileInputStream(hintFile)));
            while((line=br_hint.readLine()) != null){
                String[] arr = line.split(":");
                if(arr[0].startsWith("empty_federated_join")){
                    hints.get(2).add(arr[1]);
                } else if(arr[0].startsWith("strict_redundancy")){
                    hints.get(0).add(arr[1]);
                } else if(arr[0].startsWith("equivalent_redundancy")){
                    hints.get(1).add(arr[1]);
                } else {
                    String[] h_matv = arr[1].split("<-");
                    hint_matv.put(h_matv[1], h_matv[0]);
                }
            }
            br_hint.close();

            Injector injector = configuration.getInjector();
            IQ_FACTORY = injector.getInstance(IntermediateQueryFactory.class);
            ATOM_FACTORY = injector.getInstance(AtomFactory.class);
            TERM_FACTORY = injector.getInstance(TermFactory.class);
            CORE_SINGLETONS = injector.getInstance(CoreSingletons.class);
            dbTypeFactory = CORE_SINGLETONS.getTypeFactory().getDBTypeFactory();
            idFactory = new SQLStandardQuotedIDFactory();

        } catch (Exception e){
            e.printStackTrace();
        }
        return;
    }

//    public QueryRewritingMarco(String owlPath, String obdaPath, String PROPERTY, String hintFile, String labFile, String sourceFile, String effLabel, String query) {
//        this(owlPath, obdaPath, PROPERTY, hintFile, labFile, sourceFile, effLabel);
//        this.inputQuery = query;
//    }

    public String getOptimizedSQL(String sparqlQuery) throws Exception {
        KGQueryFactory kgQueryFactory = configuration.getKGQueryFactory();
        KGQuery<?> query = kgQueryFactory.createSPARQLQuery(sparqlQuery);
        QueryReformulator reformulator = configuration.loadQueryReformulator();
        QueryLogger queryLogger = reformulator.getQueryLoggerFactory().create(ImmutableMultimap.of());
        IQ iq = reformulator.reformulateIntoNativeQuery(query, queryLogger);
        IQTree iqt = iq.getTree();

        IQTree iqtopt = rewriteIQTree(iqt);
        IQ iqopt = IQTreeToIQ(iqtopt);
        IQ executableQuery = reformulator.generateExecutableQuery(iqopt);
        return ((NativeNodeImpl)executableQuery.getTree().getChildren().get(0)).getNativeQueryString();
    }

    public IQTree rewriteIQTree(IQTree iqt){
        //check the possibility of applying the hints in advance
        if(hints.get(0).size()>0){
            iqt = removeStrictRedundancy(iqt);
        }
        if(hints.get(1).size()>0){
            iqt = removeEquivalentRedundancy(iqt);
        }
        if(hints.get(2).size()>0){
            iqt = rewriteInnerJoin(iqt);
            iqt = rewriteLeftJoin(iqt);
        }
        System.out.println("IQ tree obtained by removing redundancy and applying EFJs");
        System.out.println(iqt);

        System.out.println("strating rewriting based on MatVs");
        if(hint_matv.size()>0){
            iqt = rewriteInnerJoinBasedOnMatV(iqt);
        }
        //first applying empty federated join optimization, and then applying materialized view based optimization

        return iqt;
    }

    public IQTree removeStrictRedundancy(IQTree iqt){
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
                            }
                        }
                    }
                }
            }
        }
        return iqt;
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

    public boolean checkStrictRedundancy(IQTree ele_1, IQTree ele_2){
        //the code needs to be updated according to the different ways of representing hints
        boolean b = false;

        if((ele_1.getRootNode() instanceof InnerJoinNode) || (ele_1.getRootNode() instanceof LeftJoinNode)){
            return false;
        }
        if((ele_2.getRootNode() instanceof InnerJoinNode) || (ele_2.getRootNode() instanceof LeftJoinNode)){
            return false;
        }
        if(ele_1.isLeaf() && ele_2.isLeaf()){
            if((ele_1 instanceof ExtensionalDataNode) && (ele_2 instanceof ExtensionalDataNode)){
                //the following code needs to be changed according to the different ways of representing hints
                RelationPredicate predicate1 = ((ExtensionalDataNode) ele_1).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) ele_2).getRelationDefinition().getAtomPredicate();
                String rel_name_1 = predicate1.toString();
                String rel_name_2 = predicate2.toString();
                //improve the following compare conditions
                if(hints.get(0).contains(rel_name_1+"<>"+rel_name_2)|| hints.get(0).contains(rel_name_2+"<>"+rel_name_1)){
                    return true;
                }
            }
        } else if((ele_1 instanceof FilterNode) && (ele_2 instanceof FilterNode)){
            //improve the following compare conditions

            if((ele_1.getChildren().get(0) instanceof ExtensionalDataNode) && (ele_2.getChildren().get(0) instanceof ExtensionalDataNode)){
                RelationPredicate predicate1 = ((ExtensionalDataNode) ele_1.getChildren().get(0)).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) ele_2.getChildren().get(0)).getRelationDefinition().getAtomPredicate();
                //improve the following compare conditions
                if(hints.get(0).contains(predicate1.getName()+"<>"+predicate2.getName())|| hints.get(0).contains(predicate2.getName()+"<>"+predicate1.getName())){
                    return true;
                }
            }
        }
        return b;
    }

    public IQTree removeEquivalentRedundancy(IQTree iqt){
        boolean change = true;
        while(change){
            change = false;
            outer: for(IQTree t: getAllSubTree(iqt)){
                if((t.getRootNode() instanceof UnionNode)){
                    ImmutableList<IQTree> childern = t.getChildren();
                    for(int i=0; i<childern.size()-1; i++){
                        for(int j=i+1; j<childern.size(); j++){
                            if(checkEquivalentRedundancy(childern.get(i), childern.get(j))){
                                //remove sub1 or sub2, based on removing which part can obtain query with less cost
                                IQTree iqt1 = null;  // for removing sub1
                                IQTree iqt2 = null;  // for removing sub2
                                IQTree sub1 = null;
                                IQTree sub2 = null;
                                if(childern.size()>2){
                                    List<IQTree> child_new1 = new ArrayList<IQTree>();
                                    child_new1.addAll(childern);
                                    child_new1.remove(childern.get(i));
                                    ImmutableList<IQTree> list1 = ImmutableList.copyOf(child_new1);
                                    UnionNode root_new1 = (UnionNode) t.getRootNode();
                                    sub1 = IQ_FACTORY.createNaryIQTree(root_new1, list1);
                                    List<IQTree> child_new2 = new ArrayList<IQTree>();
                                    child_new2.addAll(childern);
                                    child_new2.remove(childern.get(j));
                                    ImmutableList<IQTree> list2 = ImmutableList.copyOf(child_new2);
                                    UnionNode root_new2 = (UnionNode) t.getRootNode();
                                    sub2 = IQ_FACTORY.createNaryIQTree(root_new2, list2);
                                } else {
                                    sub1 = childern.get(i);
                                    sub2 = childern.get(j);
                                }
                                iqt1 = iqt.replaceSubTree(t, sub1);
                                iqt2 = iqt.replaceSubTree(t, sub2);
                                List<Integer> cost1 = getCostOfIQTree(iqt1);
                                List<Integer> cost2 = getCostOfIQTree(iqt2);
                                if((cost1.get(0) >= cost2.get(0)) && (cost1.get(1) >= cost2.get(1))){
                                    iqt = iqt.replaceSubTree(t, sub2);
                                } else {
                                    iqt = iqt.replaceSubTree(t, sub1);
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

    public boolean checkEquivalentRedundancy(IQTree ele_1, IQTree ele_2){
        boolean b = false;
        if(ele_1.getRootNode() instanceof LeftJoinNode){
            return false;
        }
        if(ele_2.getRootNode() instanceof LeftJoinNode){
            return false;
        }
        if(ele_1.isLeaf() && ele_2.isLeaf()){
            if((ele_1 instanceof ExtensionalDataNode) && (ele_2 instanceof ExtensionalDataNode)){
                //the following code needs to be changed according to the different ways of representing hints
                RelationPredicate predicate1 = ((ExtensionalDataNode) ele_1).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) ele_2).getRelationDefinition().getAtomPredicate();
                //improve the following compare conditions
                if(hints.get(1).contains(predicate1.getName()+"<>"+predicate2.getName())|| hints.get(1).contains(predicate2.getName()+"<>"+predicate1.getName())){
                    return true;
                }
            }
        } else if((ele_1.getRootNode() instanceof FilterNode) && (ele_2.getRootNode() instanceof FilterNode)){
            //improve the following compare conditions
            if((ele_1.getChildren().get(0) instanceof ExtensionalDataNode) && (ele_2.getChildren().get(0) instanceof ExtensionalDataNode)){
                RelationPredicate predicate1 = ((ExtensionalDataNode) ele_1.getChildren().get(0)).getRelationDefinition().getAtomPredicate();
                RelationPredicate predicate2 = ((ExtensionalDataNode) ele_2.getChildren().get(0)).getRelationDefinition().getAtomPredicate();
                //improve the following compare conditions
                if(hints.get(1).contains(predicate1.getName()+"<>"+predicate2.getName())|| hints.get(1).contains(predicate2.getName()+"<>"+predicate1.getName())){
                    return true;
                }
            }
        } else if((ele_1.getRootNode() instanceof InnerJoinNode) && (ele_2.getRootNode() instanceof InnerJoinNode)){

            if(ele_1.getChildren().size() == ele_2.getChildren().size()){
                List<ExtensionalDataNode> leafs_left = new ArrayList<ExtensionalDataNode>();
                List<FilterNode> filter_left = new ArrayList<FilterNode>();
                List<ExtensionalDataNode> leafs_right = new ArrayList<ExtensionalDataNode>();
                List<FilterNode> filter_right = new ArrayList<FilterNode>();

                for(IQTree t: ele_1.getChildren()){
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
                for(IQTree t: ele_2.getChildren()){
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
            }
        }


        return iqt;
    }

    public IQTree rewriteAtomicJoin(InnerJoinNode root, IQTree left_part, IQTree right_part){

        IQTree iqt_new = null;

        boolean can_rewrite = false;

        InnerJoinNode ij_copy = root;
        QueryNode root_l = left_part.getRootNode();
        QueryNode root_r = right_part.getRootNode();
        ImmutableSet<Variable> var_l = left_part.getVariables();
        ImmutableSet<Variable> var_r = right_part.getVariables();

        boolean shareVar = false;

        for(Variable b: var_r){
            if(var_l.contains(b)){
                shareVar = true;
            }
        }
        if(!shareVar){
            return null;
        }

        if((root_l instanceof InnerJoinNode) || (root_l instanceof LeftJoinNode) || (root_r instanceof InnerJoinNode) || (root_r instanceof LeftJoinNode)){
            return iqt_new;
        }
        List<IQTree> childern_l = new ArrayList<IQTree>();
        List<IQTree> childern_r = new ArrayList<IQTree>();

        if(root_l instanceof UnionNode){
            childern_l.addAll(left_part.getChildren());
        } else if((root_l instanceof FilterNode) || (left_part instanceof ExtensionalDataNode)){
            childern_l.add(left_part);
        }
        if(root_r instanceof UnionNode){
            childern_r.addAll(right_part.getChildren());
        } else if((root_r instanceof FilterNode) || (right_part instanceof ExtensionalDataNode)){
            childern_r.add(right_part);
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

                //add removing of semantic redundancy, add rolling back
                List<IQTree> SubTree_new_copy = new ArrayList<IQTree>();
                SubTree_new_copy.addAll(SubTree_new);
                SubTree_new = removeSemanticRedundancyAndRollingBack(SubTree_new);
                if(SubTree_new.size() == 1){
                    SubTree_new.clear();
                    SubTree_new.addAll(SubTree_new_copy);
                }

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

    public List<IQTree> removeSemanticRedundancyAndRollingBack(List<IQTree> trees){
        List<IQTree> results = trees;

        //check and remove semantic redundancy
        List<Integer> index = new ArrayList<Integer>();
        for(int i=0; i<trees.size(); i++){
            if(trees.get(i).getChildren().size() > 1){
                List<IQTree> subs = new ArrayList<IQTree>();
                subs.addAll(trees.get(i).getChildren());
                int ind = -1;
                for(int j=0; j<subs.size(); j++){
                    if(subs.get(j).getVariables().size() == 1){
                        ind = j;
                    }
                }
                if(ind == -1){
                    continue;
                }
                if(subs.get(ind).isLeaf()){
                    subs.remove(ind);
                    if(subs.size() == 1){
                        if(!((InnerJoinNode)trees.get(i).getRootNode()).getOptionalFilterCondition().isEmpty()){
                            ImmutableExpression cond = ((InnerJoinNode)trees.get(i).getRootNode()).getOptionalFilterCondition().get();
                            IQTree t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(cond), subs.get(0));
                            if(trees.contains(t_new)){
                                index.add(i);
                            }
                        } else {
                            if(trees.contains(subs.get(0))){
                                index.add(i);
                            }
                        }
                    } else {
                        IQTree t_new = IQ_FACTORY.createNaryIQTree((InnerJoinNode)trees.get(i).getRootNode(), ImmutableList.copyOf(subs));
                        if(trees.contains(t_new)){
                            index.add(i);
                        }
                    }
                } else if(subs.get(ind).getRootNode() instanceof FilterNode){
                    ImmutableExpression cond = ((FilterNode)subs.get(ind).getRootNode()).getFilterCondition();
                    Optional<ImmutableExpression> opt_cond = ((InnerJoinNode)trees.get(i).getRootNode()).getOptionalFilterCondition();
                    subs.remove(ind);
                    if(subs.size() == 1){
                        if(subs.get(0).isLeaf()){
                            IQTree t1 = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(opt_cond.get()), subs.get(0));
                            IQTree t2 = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.of(opt_cond.get(), cond))), subs.get(0));
                            if(((t1 != null) && trees.contains(t1))||((t2 != null) && trees.contains(t2))){
                                index.add(i);
                            }
                        } else if(subs.get(0).getRootNode() instanceof FilterNode){
                            IQTree t1 = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(opt_cond.get(), ((FilterNode)subs.get(0).getRootNode()).getFilterCondition())), subs.get(0).getChildren().get(0));
                            IQTree t2 = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(opt_cond.get(), ((FilterNode)subs.get(0).getRootNode()).getFilterCondition(), cond)), subs.get(0).getChildren().get(0));
                            if(((t1 != null) && trees.contains(t1))||((t2 != null) && trees.contains(t2))){
                                index.add(i);
                            }
                        }
                    } else {
                        InnerJoinNode ijn1 = IQ_FACTORY.createInnerJoinNode();
                        InnerJoinNode ijn2 = IQ_FACTORY.createInnerJoinNode();
                        IQTree t1 = IQ_FACTORY.createNaryIQTree(ijn1, ImmutableList.copyOf(subs));
                        IQTree t2 = IQ_FACTORY.createNaryIQTree(ijn2, ImmutableList.copyOf(subs));
                        if(((t1 != null) && trees.contains(t1))||((t2 != null) && trees.contains(t2))){
                            index.add(i);
                        }
                    }
                }
            }
        }

        if(trees.size()<2){
            return trees;
        }

        //rolling back;
        boolean b = true;
        module: while(b){
            b = false;
            for(int i=0; i<trees.size()-1; i++){
                if(trees.get(i).getChildren().size() < 2){
                    continue;
                }
                List<IQTree> subs_i = trees.get(i).getChildren();
                for(int j=i+1; j<trees.size(); j++){
                    if(trees.get(j).getChildren().size() < 2){
                        continue;
                    }
                    List<IQTree> subs_j = trees.get(j).getChildren();
                    List<IQTree> share_sub = new ArrayList<IQTree>();
                    for(IQTree t: subs_i){
                        if(subs_j.contains(t)){
                            share_sub.add(t);
                        }
                    }

                    if(share_sub.size() == 0){
                        continue;
                    }
                    Set<Variable> vars_union = new HashSet<Variable>();
                    List<IQTree> t_union_i = new ArrayList<IQTree>();
                    List<IQTree> t_union_j = new ArrayList<IQTree>();

                    for(IQTree t: subs_i){
                        if(!share_sub.contains(t)){
                            t_union_i.add(t);
                            vars_union.addAll(t.getVariables());
                        }
                    }
                    for(IQTree t: subs_j){
                        if(!share_sub.contains(t)){
                            t_union_j.add(t);
                            vars_union.addAll(t.getVariables());
                        }
                    }

                    IQTree t1, t2;
                    if(t_union_i.size() == 1){
                        t1 = t_union_i.get(0);
                    } else {
                        t1 = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.copyOf(t_union_i));
                    }
                    if(t_union_j.size() == 1){
                        t2 = t_union_j.get(0);
                    } else {
                        t2 = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.copyOf(t_union_j));
                    }

                    UnionNode union = IQ_FACTORY.createUnionNode(ImmutableSet.copyOf(vars_union));

                    IQTree union_tree = IQ_FACTORY.createNaryIQTree(union, ImmutableList.of(t1, t2));
                    IQTree join_tree = null;
                    if(share_sub.size() == 1){
                        join_tree = share_sub.get(0);
                    } else {
                        join_tree = IQ_FACTORY.createNaryIQTree(IQ_FACTORY.createInnerJoinNode(), ImmutableList.copyOf(share_sub));
                    }
                    IQTree new_t = IQ_FACTORY.createNaryIQTree((InnerJoinNode)trees.get(i).getRootNode(), ImmutableList.of(union_tree, join_tree));
                    trees.remove(i);
                    trees.remove(j-1);
                    trees.add(new_t);
                    b = true;
                    continue module;
                }
            }
        }

        return trees;
    }

    public ExpRewriten rewriteAtomicJoinWithoutUnionInLeftAndRight(InnerJoinNode root, IQTree left, IQTree right){
        //Rewriting by empty federated joins

        ExpRewriten ER = new ExpRewriten();
        //complete the checking conditions
        //Left A, A1 JOIN A2 JOIN ... An
        //Right B, B1 JOIN B2 JOIN ... JOIN Bm
        //A, B, Ai, Bi, without UNION

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

    public ExpRewriten createJoinTree(InnerJoinNode root, JoinOfLeafs left, JoinOfLeafs right){
        ExpRewriten ER = new ExpRewriten();

        Set<Variable> vars_l_r = new HashSet<>();
        List<IQTree> childern_new = new ArrayList<IQTree>();
        for(int i=0; i<left.dataNodes.size(); i++){
            IQTree t = left.dataNodes.get(i);
            FilterNode fn = left.filters.get(i);

            List<Integer> index_right = new ArrayList<Integer>();  //for sj checking
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
                    vars_l_r.addAll(left.dataNodes.get(i).getVariables());
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
                    List<ImmutableExpression> exps = new ArrayList<ImmutableExpression>();
                    exps.add(root.getOptionalFilterCondition().get());
                    if(childern_new.get(0).getRootNode() instanceof FilterNode){
                        exps.add(((FilterNode)childern_new.get(0).getRootNode()).getFilterCondition());
                        FilterNode fn = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.copyOf(exps)));
                        ER.newRewritten = IQ_FACTORY.createUnaryIQTree(fn, childern_new.get(0).getChildren().get(0));
                    } else {
                        FilterNode fn = IQ_FACTORY.createFilterNode(exps.get(0));
                        ER.newRewritten = IQ_FACTORY.createUnaryIQTree(fn, childern_new.get(0));
                    }

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
        if(left_part.size()>1){
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
                        LeftJoinNode root_subtree = root; //make a copy of root
                        subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left.getChildren().get(i), right.getChildren().get(j)));
                    }
                } else {
                    LeftJoinNode root_subtree = root; //make a copy of root
                    if(right_part.size()==1){
                        subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left.getChildren().get(i), right));
                    } else {
                        subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left.getChildren().get(i), right.getChildren().get(j)));
                    }

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

    public IQTree rewriteInnerJoinBasedOnMatV(IQTree iqt){

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

                            IQTree sub_new = rewriteAtomicJoinBasedOnMatV((InnerJoinNode)root, sub1, sub2);

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
            }
        }


        return iqt;
    }

    public IQTree rewriteAtomicJoinBasedOnMatV(InnerJoinNode root, IQTree left_part, IQTree right_part){

        IQTree iqt_new = null;

        boolean can_rewrite = false;

        InnerJoinNode ij_copy = root;
        QueryNode root_l = left_part.getRootNode();
        QueryNode root_r = right_part.getRootNode();
        ImmutableSet<Variable> var_l = left_part.getVariables();
        ImmutableSet<Variable> var_r = right_part.getVariables();

        boolean shareVar = false;

        for(Variable b: var_r){
            if(var_l.contains(b)){
                shareVar = true;
            }
        }
        if(!shareVar){
            return null;
        }

        if((root_l instanceof InnerJoinNode) || (root_l instanceof LeftJoinNode) || (root_r instanceof InnerJoinNode) || (root_r instanceof LeftJoinNode)){
            return iqt_new;
        }
        List<IQTree> childern_l = new ArrayList<IQTree>();
        List<IQTree> childern_r = new ArrayList<IQTree>();

        if(root_l instanceof UnionNode){
            childern_l.addAll(left_part.getChildren());
        } else if((root_l instanceof FilterNode) || (left_part instanceof ExtensionalDataNode)){
            childern_l.add(left_part);
        }
        if(root_r instanceof UnionNode){
            childern_r.addAll(right_part.getChildren());
        } else if((root_r instanceof FilterNode) || (right_part instanceof ExtensionalDataNode)){
            childern_r.add(right_part);
        }
        List<IQTree> SubTree_new = new ArrayList<IQTree>();

        for(int i=0; i<childern_l.size(); i++){
            for(int j=0; j<childern_r.size(); j++){
                IQTree child_l = childern_l.get(i);
                IQTree child_r = childern_r.get(j);

                // check whether (child_l JoinNode child_r) can be rewritten into empty join or by materialized view
                ExpRewriten rewrite = rewriteAtomicJoinWithoutUnionInLeftAndRightBasedOnMatV(root, child_l, child_r);

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

                //add removing of semantic redundancy, add rolling back
                List<IQTree> SubTree_new_copy = new ArrayList<IQTree>();
                SubTree_new_copy.addAll(SubTree_new);
                SubTree_new = removeSemanticRedundancyAndRollingBack(SubTree_new);
                if(SubTree_new.size() == 1){
                    SubTree_new.clear();
                    SubTree_new.addAll(SubTree_new_copy);
                }

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

    public ExpRewriten rewriteAtomicJoinWithoutUnionInLeftAndRightBasedOnMatV(InnerJoinNode root, IQTree left, IQTree right){

        ExpRewriten ER = new ExpRewriten();
        //complete the checking conditions
        //Left A, A1 JOIN A2 JOIN ... An
        //Right B, B1 JOIN B2 JOIN ... JOIN Bm
        //A, B, Ai, Bi, without UNION

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
                            //check conditions for rewriting based on materialized views;
                            String relation1 = JOL_left.relations.get(i).getName();
                            int ind1 = k;
                            String relation2 = JOL_right.relations.get(j).getName();
                            int ind2 = l;
                            if(hint_matv.containsKey(relation1+"<>"+relation2+"<>"+k+"<>"+l)||hint_matv.containsKey(relation2+"<>"+relation1+"<>"+l+"<>"+k)){
                                //create a new data node for left_i and right_j;
                                FilterNode fn_ij = null;
                                RelationPredicate matv = null; //name of the relations for MatV
                                NamedRelationDefinition NRD = null;
                                boolean b = true;
                                if(hint_matv.containsKey(relation1+"<>"+relation2+"<>"+k+"<>"+l)){
                                    NRD = createDatabaseRelationForMatV(hint_matv.get(relation1+"<>"+relation2+"<>"+k+"<>"+l));
                                } else if(hint_matv.containsKey(relation2+"<>"+relation1+"<>"+l+"<>"+k)){
                                    NRD = createDatabaseRelationForMatV(hint_matv.get(relation2+"<>"+relation1+"<>"+l+"<>"+k));
                                    b = false;
                                }

                                ImmutableList<Attribute> attrs_matv = NRD.getAttributes();
                                Map<Integer, VariableOrGroundTerm> args_new = new HashMap<Integer, VariableOrGroundTerm>();
                                //make sure the relations occurring in the left_part and right_part of the join
                                if(b){
                                    for(int k1: arg_map_left.keySet()){
                                        for(int index=0; index<attrs_matv.size(); index++){
                                            if(attrs_matv.get(index).getID().toString().equals("\"1_"+k1+"\"")){
                                                args_new.put(index, arg_map_left.get(k1));
                                            }
                                        }
                                    }
                                    for(int l1: arg_map_right.keySet()){
                                        if(l1 == l){
                                            continue;
                                        }
                                        for(int index=0; index<attrs_matv.size(); index++){
                                            if(attrs_matv.get(index).getID().toString().equals("\"2_"+l1+"\"")){
                                                args_new.put(index, arg_map_right.get(l1));
                                            }
                                        }
                                    }
                                } else {
                                    for(int k1: arg_map_left.keySet()){
                                        if(k1 == k){
                                            continue;
                                        }
                                        for(int index=0; index<attrs_matv.size(); index++){
                                            if(attrs_matv.get(index).getID().toString().equals("\"2_"+k1+"\"")){
                                                args_new.put(index, arg_map_left.get(k1));
                                            }
                                        }
                                    }
                                    for(int l1: arg_map_right.keySet()){
                                        for(int index=0; index<attrs_matv.size(); index++){
                                            if(attrs_matv.get(index).getID().toString().equals("\"1_"+l1+"\"")){
                                                args_new.put(index, arg_map_right.get(l1));
                                            }
                                        }
                                    }
                                }

                                List<ImmutableExpression> conds = new ArrayList<ImmutableExpression>();
                                if(JOL_left.filters.get(i) != null){
                                    ImmutableExpression exp = JOL_left.filters.get(i).getFilterCondition();
                                    if(exp.getFunctionSymbol().getName().startsWith("AND")){
                                        for(ImmutableTerm it: exp.getTerms()){
                                            conds.add((ImmutableExpression) it);
                                        }
                                    } else {
                                        conds.add(exp);
                                    }
                                }
                                if(JOL_right.filters.get(j) != null){
                                    ImmutableExpression exp = JOL_right.filters.get(j).getFilterCondition();
                                    if(exp.getFunctionSymbol().getName().startsWith("AND")){
                                        for(ImmutableTerm it: exp.getTerms()){
                                            conds.add((ImmutableExpression) it);
                                        }
                                    } else {
                                        conds.add(exp);
                                    }
                                }

                                if(conds.size() == 1){
                                    fn_ij = IQ_FACTORY.createFilterNode(conds.get(0));
                                } else if(conds.size() >1){
                                    fn_ij = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.copyOf(conds)));
                                }
                                ExtensionalDataNode edn_ij = IQ_FACTORY.createExtensionalDataNode(NRD, ImmutableMap.copyOf(args_new));

                                JOL_left.dataNodes.set(i,edn_ij);
                                JOL_left.filters.set(i, fn_ij);

                                JOL_right.dataNodes.remove(j);
                                JOL_right.filters.remove(j);
                                JOL_right.relations.remove(j);

                                ER = createJoinTree(root, JOL_left, JOL_right);
                                ER.canRewrite = true;
                                return ER;
                            }
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

    public NamedRelationDefinition createDatabaseRelationForMatV(String definition){
        String relation_name = definition.substring(0, definition.indexOf("("));
        String[] attributes_list = definition.substring(definition.indexOf("(")+1, definition.indexOf(")")).split(",");

        RelationID id = idFactory.createRelationID(relation_name);
        //DBTermType stringDBType = dbTypeFactory.getDBStringType();
        RelationDefinition.AttributeListBuilder builder = DatabaseTableDefinition.attributeListBuilder();
        for (int i=0; i<attributes_list.length; i++) {
            String[] attr_name_type = attributes_list[i].split(" ");
            DBTermType d_type = dbTypeFactory.getDBTermType(attr_name_type[1]);
            builder.addAttribute(idFactory.createAttributeID(attr_name_type[0]), d_type, true);
        }
        NamedRelationDefinition nrd = new DatabaseTableDefinition(ImmutableList.of(id), builder);
        return nrd;
    }

    public IQ IQTreeToIQ (IQTree iqt)throws Exception {
        AtomPredicate ANS1 = ATOM_FACTORY.getRDFAnswerPredicate(iqt.getVariables().size());
        DistinctVariableOnlyDataAtom projection = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1, ImmutableList.copyOf(iqt.getVariables()));
        return IQ_FACTORY.createIQ(projection, iqt);
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
