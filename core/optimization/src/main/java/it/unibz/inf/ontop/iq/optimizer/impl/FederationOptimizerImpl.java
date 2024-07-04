package it.unibz.inf.ontop.iq.optimizer.impl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.DatabaseTableDefinition;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FederationOptimizer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.substitution.Substitution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
public class FederationOptimizerImpl implements FederationOptimizer {

    private boolean enabled;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FederationOptimizerImpl.class);

    private final ThreadLocal<Boolean> tryingEmptyJoinRule = new ThreadLocal<>();
    private final ThreadLocal<Boolean> tryingSelfJoinRule = new ThreadLocal<>();

    @Inject
    public FederationOptimizerImpl(IntermediateQueryFactory iqFactory,
                                   AtomFactory atomFactory,
                                   TermFactory termFactory,
                                   CoreSingletons coreSingletons) {
        this(iqFactory,
                atomFactory,
                termFactory,
                coreSingletons,
                Boolean.parseBoolean(System.getenv().getOrDefault("ONTOP_OBDF_OPTIMIZATION_ENABLED", "false")),
                System.getenv().get("ONTOP_OBDF_SOURCE_FILE"),
                System.getenv().get("ONTOP_OBDF_EFF_LABEL_FILE"),
                System.getenv().get("ONTOP_OBDF_HINT_FILE"));
    }

    public FederationOptimizerImpl(IntermediateQueryFactory iqFactory,
                                   AtomFactory atomFactory,
                                   TermFactory termFactory,
                                   CoreSingletons coreSingletons,
                                   boolean enabled,
                                   String sourceFile,
                                   String effLabelFile,
                                   String hintFile) {
        this.enabled = enabled;
        if (enabled) {
            try {

                IQ_FACTORY = iqFactory;
                ATOM_FACTORY = atomFactory;
                TERM_FACTORY = termFactory;
                CORE_SINGLETONS = coreSingletons;
                dbTypeFactory = CORE_SINGLETONS.getTypeFactory().getDBTypeFactory();
                idFactory = new SQLStandardQuotedIDFactory();

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
                while ((line = br.readLine()) != null) {
                    String[] arr = line.split("-");
                    sourceMap.put(arr[0], arr[1]);
                }
                br.close();

                BufferedReader br_new = new BufferedReader(new InputStreamReader(new FileInputStream(effLabelFile)));
                while ((line = br_new.readLine()) != null) {
                    String[] arr = line.split("-");
                    labMap.put(arr[0], arr[1]);
                }
                br_new.close();

                BufferedReader br_hint = new BufferedReader(new InputStreamReader(new FileInputStream(hintFile)));
                while ((line = br_hint.readLine()) != null) {
                    String[] arr = line.split(":");
                    if (arr[0].startsWith("empty_federated_join")) {
                        hints.get(2).add(arr[1]);
                    } else if (arr[0].startsWith("strict_redundancy")) {
                        hints.get(0).add(arr[1]);
                    } else if (arr[0].startsWith("equivalent_redundancy")) {
                        hints.get(1).add(arr[1]);
                    } else {
                        String[] h_matv = arr[1].split("<-");
                        hint_matv.put(h_matv[1], h_matv[0]);
                    }
                }
                br_hint.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IQ optimize(IQ query) {
        if (!enabled) {
            return query;
        }

        // Call optimizeAndNormalize until the query stops changing
        // Quick fix should be replaced by proper implementation in rewriteIQTree
        IQ previousIQ;
        IQ currentIQ = query;
        do {
            previousIQ = currentIQ;
            currentIQ = optimizeAndNormalize(currentIQ);
        } while (!currentIQ.equals(previousIQ));
        return currentIQ;
    }

    public IQ optimizeAndNormalize(IQ query) {
        LOGGER.debug("My optimized query input:\n{}\n", query);
        IQTree iqTree = query.getTree();
        IQTree iqTreeOptimized = rewriteIQTree(iqTree);
        try {
            DistinctVariableOnlyDataAtom project_original = query.getProjectionAtom();
            IQ iqOptimized = IQTreeToIQ(project_original, iqTreeOptimized);
            iqOptimized = iqOptimized.normalizeForOptimization();
            LOGGER.debug("My optimized query output:\n{}\n", iqOptimized);
            return iqOptimized;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public IQTree rewriteIQTree(IQTree iqt){
        //check the possibility of applying the hints in advance
        LOGGER.debug("the IQ tree before hint-based optimization:\n{}\n", iqt);

        if(hints.get(0).size()>0){
            iqt = removeStrictRedundancy(iqt);
        }
        if(hints.get(1).size()>0){
            iqt = removeEquivalentRedundancy(iqt);
        }
        LOGGER.debug("the IQ tree after removing redundancy:\n{}\n", iqt);

        if(hints.get(2).size()>0){
            iqt = rewriteInnerJoin(iqt);
            iqt = rewriteLeftJoin(iqt);
        }

        if(hint_matv.size()>0){
            iqt = rewriteInnerJoinBasedOnMatV(iqt);
        }
        LOGGER.debug("the IQ tree after hint-based optimization:\n{}\n", iqt);

        return iqt;
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
                    //the formats of the names for different federation systems may be different
                    //the ways of obtaining the source information from the names may be different
                    String name = relationName.substring(relationName.lastIndexOf(".")+1,relationName.length());
                    if(name.startsWith("\"")){
                        name = name.substring(1, name.length()-1);
                    }
                    if(sourceMap.containsKey(name)){
                        sources.add(sourceMap.get(name));
                    } else if(sourceMap.containsKey("\""+name+"\"")){
                        sources.add(sourceMap.get("\""+name+"\""));
                    }

//                    String source = "";
//                    if(relationName.startsWith("\"")){
//                        source = relationName.substring(1, relationName.indexOf("."));
//                    } else {
//                        source = relationName.substring(0, relationName.indexOf("."));
//                    }
//                    sources.add(source);
//                    if(sourceMap.containsKey(relationName)){
//                        sources.add(sourceMap.get(relationName));
//                    }
                }
            }
        }
        return sources;
    }

    //增加情形来处理更多的redundancy的情形;;
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
                //改进了
                b = checkStrictRedundancyDataNode((ExtensionalDataNode) ele_1, (ExtensionalDataNode) ele_2);
            }
        } else if((ele_1.getRootNode() instanceof FilterNode) && (ele_2.getRootNode() instanceof FilterNode)){
            //improve the following compare conditions

            if((ele_1.getChildren().get(0) instanceof ExtensionalDataNode) && (ele_2.getChildren().get(0) instanceof ExtensionalDataNode)){
                if(ele_1.getRootNode().equals(ele_2.getRootNode())){
                    b = checkStrictRedundancyDataNode((ExtensionalDataNode) ele_1.getChildren().get(0),(ExtensionalDataNode) ele_2.getChildren().get(0));
                }
            }
        }
        return b;
    }

    public boolean checkStrictRedundancyDataNode(ExtensionalDataNode ele_1, ExtensionalDataNode ele_2){
        boolean res = false;
        RelationPredicate predicate1 = ele_1.getRelationDefinition().getAtomPredicate();
        RelationPredicate predicate2 = ele_2.getRelationDefinition().getAtomPredicate();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> args1 = ele_1.getArgumentMap();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> args2 = ele_2.getArgumentMap();
        //String normalName1 = getNormalFormOfRelation(predicate1.getName());
        //String normalName2 = getNormalFormOfRelation(predicate2.getName());
        String name1 = predicate1.getName();
        String name2 = predicate2.getName();
        if(hints.get(0).contains(name1+"<>"+name2) || hints.get(0).contains(name2+"<>"+name1)){
            for(int i: args1.keySet()){
                for(int j: args2.keySet()){
                    if(args2.get(j).equals(args1.get(i))){
                        if(i != j){
                            return false;
                        }
                    }
                }
            }
            return true;
        } else {
            List<Integer> attr_index_1 = new ArrayList<Integer>();
            attr_index_1.addAll(args1.keySet());
            Collections.sort(attr_index_1);
            String cand1 = name1+"(";
            String cand2 = name2+"(";
            for(int i: attr_index_1){
                cand1 = cand1 + i +",";
                for(int j: args2.keySet()){
                    if(args2.get(j).equals(args1.get(i))){
                        cand2 = cand2 + j +",";
                        break;
                    }
                }
            }
            cand1 = cand1.substring(0, cand1.length()-1)+")";
            cand2 = cand2.substring(0, cand2.length()-1)+")";
            if(hints.get(0).contains(cand1+"<>"+cand2)){
                return true;
            } else {
                List<Integer> attr_index_2 = new ArrayList<Integer>();
                attr_index_2.addAll(args2.keySet());
                Collections.sort(attr_index_2);
                cand1 = name1+"(";
                cand2 = name2+"(";
                for(int i: attr_index_2){
                    cand2 = cand2 + i +",";
                    for(int j: args1.keySet()){
                        if(args1.get(j).equals(args2.get(i))){
                            cand1 = cand1 + j +",";
                            break;
                        }
                    }
                }
                cand1 = cand1.substring(0, cand1.length()-1)+")";
                cand2 = cand2.substring(0, cand2.length()-1)+")";
                if(hints.get(0).contains(cand2+"<>"+cand1)){
                    return true;
                }
            }

        }
        return res;
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
                            boolean b = checkEquivalentRedundancy(childern.get(i), childern.get(j));

                            if(b){
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
                                onEquivalentRedundancyRuleApplied();
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

        Set<String> sources = getSources(ele_1);
        sources.addAll(getSources(ele_2));
        if(sources.size() == 1){
            return false;
        }

        if(ele_1.getRootNode() instanceof LeftJoinNode){
            return false;
        }
        if(ele_2.getRootNode() instanceof LeftJoinNode){
            return false;
        }

        if(((ele_1 instanceof ExtensionalDataNode ) && (ele_2 instanceof ExtensionalDataNode)) || ((ele_1.getRootNode() instanceof FilterNode) && (ele_2.getRootNode() instanceof FilterNode)) || ((ele_1.getRootNode() instanceof ConstructionNode) && (ele_2.getRootNode() instanceof ConstructionNode))){

            return checkEquivalentRedundancyDataElement(ele_1, ele_2);
        }  else if((ele_1.getRootNode() instanceof InnerJoinNode) && (ele_2.getRootNode() instanceof InnerJoinNode)){
            if((ele_1.getChildren().size() == ele_2.getChildren().size()) && (ele_1.getRootNode().equals(ele_2.getRootNode()))){
                //只包含一个不同的子树
                List<IQTree> subs1 = ele_1.getChildren();
                List<IQTree> subs2 = ele_2.getChildren();

                IQTree t1_check = null;
                IQTree t2_check = null;

                for(IQTree t: subs1){
                    if((!subs2.contains(t))){
                        if(t1_check == null){
                            t1_check = t;
                        } else {
                            return false;
                        }
                    }
                }

                for(IQTree t: subs2){
                    if((!subs1.contains(t))){
                        if(t2_check == null){
                            t2_check = t;
                        } else {
                            return false;
                        }
                    }
                }

                return checkEquivalentRedundancyDataElement(t1_check, t2_check);
            } else {
                return false;
            }
        }
        return b;
    }

    public boolean checkEquivalentRedundancyDataNode(ExtensionalDataNode ele_1, ExtensionalDataNode ele_2){
        boolean res = false;

        RelationPredicate predicate1 = ele_1.getRelationDefinition().getAtomPredicate();
        RelationPredicate predicate2 = ele_2.getRelationDefinition().getAtomPredicate();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> args1 = ele_1.getArgumentMap();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> args2 = ele_2.getArgumentMap();
//        String normalName1 = getNormalFormOfRelation(predicate1.getName());
//        String normalName2 = getNormalFormOfRelation(predicate2.getName());
        String name1 = predicate1.getName();
        String name2 = predicate2.getName();

        if(hints.get(1).contains(name1+"<>"+name2) || hints.get(1).contains(name2+"<>"+name1)){
            for(int i: args1.keySet()){
                for(int j: args2.keySet()){
                    if(args2.get(j).equals(args1.get(i))){
                        if(i != j){
                            return false;
                        }
                    }
                }
            }
            return true;
        } else {
            List<Integer> attr_index_1 = new ArrayList<Integer>();
            attr_index_1.addAll(args1.keySet());
            Collections.sort(attr_index_1);
            String cand1 = name1+"(";
            String cand2 = name2+"(";
            for(int i: attr_index_1){
                cand1 = cand1 + i +",";
                for(int j: args2.keySet()){
                    if(args2.get(j).equals(args1.get(i))){
                        cand2 = cand2 + j +",";
                        break;
                    }
                }
            }
            cand1 = cand1.substring(0, cand1.length()-1)+")";
            cand2 = cand2.substring(0, cand2.length()-1)+")";

            if(hints.get(1).contains(cand1+"<>"+cand2)){
                return true;
            } else {
                List<Integer> attr_index_2 = new ArrayList<Integer>();
                attr_index_2.addAll(args2.keySet());
                Collections.sort(attr_index_2);
                cand1 = name1+"(";
                cand2 = name2+"(";
                for(int i: attr_index_2){
                    cand2 = cand2 + i +",";
                    for(int j: args1.keySet()){
                        if(args1.get(j).equals(args2.get(i))){
                            cand1 = cand1 + j +",";
                            break;
                        }
                    }
                }
                cand1 = cand1.substring(0, cand1.length()-1)+")";
                cand2 = cand2.substring(0, cand2.length()-1)+")";

                if(hints.get(1).contains(cand2+"<>"+cand1)){
                    return true;
                }
            }

        }
        return res;
    }

    public boolean checkEquivalentRedundancyDataNodeMayWithFilter(IQTree ele_1, IQTree ele_2){
        boolean res = false;
        if((ele_1 instanceof ExtensionalDataNode) && (ele_2 instanceof ExtensionalDataNode)){
            return checkEquivalentRedundancyDataNode((ExtensionalDataNode) ele_1, (ExtensionalDataNode) ele_2);
        } else if ((ele_1.getRootNode() instanceof FilterNode) && (ele_2.getRootNode() instanceof FilterNode)){
            if(ele_1.getRootNode().equals(ele_2.getRootNode())){
                if((ele_1.getChildren().get(0) instanceof ExtensionalDataNode) && (ele_2.getChildren().get(0) instanceof ExtensionalDataNode)){
                    return checkEquivalentRedundancyDataNode((ExtensionalDataNode) ele_1.getChildren().get(0), (ExtensionalDataNode) ele_2.getChildren().get(0));
                }
            }
        }
        return res;
    }

    public boolean checkEquivalentRedundancyDataElement(IQTree ele_1, IQTree ele_2){
        //DataNode 可能包含Filter, Construction
        //
        boolean res = false;

        if((ele_1.getRootNode() instanceof ConstructionNode) && (ele_2.getRootNode() instanceof ConstructionNode)){
            if(ele_1.getChildren().get(0) instanceof ExtensionalDataNode){
                if(!(ele_2.getRootNode() instanceof ExtensionalDataNode)){
                    return false;
                }
            } else if (ele_1.getChildren().get(0).getRootNode() instanceof FilterNode){
                if(!(ele_1.getChildren().get(0).getChildren().get(0) instanceof ExtensionalDataNode)){
                    return false;
                }
                if(!(ele_2.getChildren().get(0).getRootNode() instanceof FilterNode)){
                    return false;
                }
                if(!(ele_2.getChildren().get(0).getChildren().get(0) instanceof ExtensionalDataNode)){
                    return false;
                }
            } else {
                return false;
            }

            ConstructionNode cn1 = (ConstructionNode) ele_1.getRootNode();
            ConstructionNode cn2 = (ConstructionNode) ele_2.getRootNode();

            Set<Variable> cn1_vars_1 = cn1.getVariables();
            Set<Variable> cn2_vars_2 = cn2.getVariables();

            Substitution<ImmutableTerm> subs1 = cn1.getSubstitution();
            Substitution<ImmutableTerm> subs2 = cn2.getSubstitution();

            Map<Variable, Variable> replace_1 = new HashMap<Variable, Variable>();
            Map<Variable, Variable> replace_2 = new HashMap<Variable, Variable>();

            if(!subs1.isEmpty()){
                for(Variable v: subs1.getDomain()){
                    if(subs1.get(v).getVariableStream().findFirst().isPresent()){
                        Variable v_original = subs1.get(v).getVariableStream().findFirst().get();
                        replace_1.put(v_original, v);
                    }
                }
            }

            if(!subs2.isEmpty()){
                for(Variable v: subs2.getDomain()){
                    if(subs2.get(v).getVariableStream().findFirst().isPresent()){
                        Variable v_original = subs2.get(v).getVariableStream().findFirst().get();
                        replace_2.put(v_original, v);
                    }
                }
            }

            ExtensionalDataNode dn1 = null;
            ExtensionalDataNode dn2 = null;

            if(ele_1.getChildren().get(0) instanceof ExtensionalDataNode){
                dn1 = (ExtensionalDataNode) ele_1.getChildren().get(0);
            } else if(ele_1.getChildren().get(0).getRootNode() instanceof FilterNode){
                if(ele_1.getChildren().get(0).getChildren().get(0) instanceof ExtensionalDataNode){
                    dn1 = (ExtensionalDataNode) ele_1.getChildren().get(0).getChildren().get(0);
                } else {
                    return false;
                }
            } else {
                return false;
            }

            if(ele_2.getChildren().get(0) instanceof ExtensionalDataNode){
                dn2 = (ExtensionalDataNode) ele_2.getChildren().get(0);
            } else if(ele_2.getChildren().get(0).getRootNode() instanceof FilterNode){
                if(ele_2.getChildren().get(0).getChildren().get(0) instanceof ExtensionalDataNode){
                    dn2 = (ExtensionalDataNode) ele_2.getChildren().get(0).getChildren().get(0);
                } else {
                    return false;
                }
            } else {
                return false;
            }

            //构建额外的变量映射
            int ct = 0;

            for(Integer ind: dn1.getArgumentMap().keySet()){
                VariableOrGroundTerm v1 = dn1.getArgumentMap().get(ind);
                if(!v1.isGround()){
                    Variable v1p = TERM_FACTORY.getVariable(v1.toString());
                    if(cn1_vars_1.contains(v1p) || replace_1.containsKey(v1p)){
                        continue;
                    } else {
                        if(dn2.getArgumentMap().keySet().contains(ind)){
                            VariableOrGroundTerm v2 = dn2.getArgumentMap().get(ind);
                            if(!v2.isGround()){
                                Variable v2p = TERM_FACTORY.getVariable(v2.toString());
                                Variable v_new = TERM_FACTORY.getVariable("Var"+ct);
                                replace_1.put(v1p, v_new);
                                replace_2.put(v2p, v_new);
                                ct = ct + 1;
                            }
                        }
                    }
                }
            }

            IQTree t1_new = replaceVariable(replace_1, ele_1.getChildren().get(0));
            IQTree t2_new = replaceVariable(replace_2, ele_2.getChildren().get(0));

            return checkEquivalentRedundancyDataNodeMayWithFilter(t1_new, t2_new);

        } else {
            return checkEquivalentRedundancyDataNodeMayWithFilter(ele_1, ele_2);
        }
//        return res;
    }

    public IQTree replaceVariable(Map<Variable, Variable> substitution, IQTree t){
        IQTree t_new = null;

        if(substitution.size() == 0){
            return t;
        }

        ExtensionalDataNode dn_old = null;
        ExtensionalDataNode dn_new = null;

        if(t instanceof ExtensionalDataNode){
            dn_old = (ExtensionalDataNode) dn_old;
        } else if(t.getRootNode() instanceof FilterNode) {
            dn_old = (ExtensionalDataNode) t.getChildren().get(0);
        } else {
            return null;
        }

        HashMap<Integer, VariableOrGroundTerm> maps_new = new HashMap<Integer, VariableOrGroundTerm>();
        for(Integer ind: dn_old.getArgumentMap().keySet()){
            if(substitution.containsKey((dn_old.getArgumentMap().get(ind)))){
                maps_new.put(ind, substitution.get((dn_old.getArgumentMap().get(ind))));
            } else {
                maps_new.put(ind, dn_old.getArgumentMap().get(ind));
            }
        }
        dn_new = IQ_FACTORY.createExtensionalDataNode(dn_old.getRelationDefinition(), ImmutableMap.copyOf(maps_new));

        FilterNode fn_old = null;
        Set<Variable> fn_vars_old = new HashSet<Variable>();
        FilterNode fn_new = null;
        if(t.getRootNode() instanceof FilterNode){
            fn_old = (FilterNode) t.getRootNode();
        }
        if(fn_old != null){
            ImmutableExpression condition = fn_old.getFilterCondition();
            fn_vars_old.addAll(fn_old.getLocalVariables());
        }

        if(fn_new == null){
            return dn_new;
        }

        return t_new;
    }



    public IQTree rewriteInnerJoin(IQTree iqt){
        tryingEmptyJoinRule.set(Boolean.FALSE);
        tryingSelfJoinRule.set(Boolean.FALSE);
        boolean update = true;
        int count = 0;

        module: while(update){
            update = false;
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
                                    if (tryingEmptyJoinRule.get()) {
                                        onEmptyJoinRuleApplied();
                                    }
                                    if (tryingSelfJoinRule.get()) {
                                        onSelfJoinRuleApplied();
                                    }
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
        //rewrite atomic join by EFJ or sj

        IQTree iqt_new = null;

        boolean can_rewrite = false;

        QueryNode root_l = left_part.getRootNode();
        QueryNode root_r = right_part.getRootNode();


        //////update+improved part//////
        if((root_l instanceof LeftJoinNode) || (root_r instanceof LeftJoinNode)){
            return iqt_new;
        }
        List<IQTree> childern_l = new ArrayList<IQTree>();
        List<IQTree> childern_r = new ArrayList<IQTree>();

        if(root_l instanceof UnionNode){
            childern_l.addAll(left_part.getChildren());
        } else if((root_l instanceof FilterNode) || (root_l instanceof InnerJoinNode) || (root_l instanceof ConstructionNode) || (left_part instanceof ExtensionalDataNode) ){
            childern_l.add(left_part);
        }
        if(root_r instanceof UnionNode){
            childern_r.addAll(right_part.getChildren());
        } else if((root_r instanceof FilterNode) || (root_r instanceof InnerJoinNode) || (root_r instanceof ConstructionNode) || (right_part instanceof ExtensionalDataNode)){
            childern_r.add(right_part);
        }
        List<IQTree> SubTree_new = new ArrayList<IQTree>();

        for(int i=0; i<childern_l.size(); i++){
            for(int j=0; j<childern_r.size(); j++){
                IQTree child_l = childern_l.get(i);
                IQTree child_r = childern_r.get(j);

                // check whether (child_l JoinNode child_r) can be rewritten into empty join, 查看是否能被EFJ优化
                ExpRewriten rewrite = rewriteAtomicJoinWithoutUnionInLeftAndRight(root, child_l, child_r);

                if(rewrite.newRewritten != null){
                    SubTree_new.add(rewrite.newRewritten);
                }
                if(rewrite.canRewrite || rewrite.sjRewrite){
                    can_rewrite = true;
                }
            }
        }

        if(can_rewrite){  //some Ai JOIN Bj can rewritten into empty relation or can apply sjr
            if(SubTree_new.size() == 1){
                iqt_new = SubTree_new.get(0);
            } else if(SubTree_new.size() > 1){

                //remove semantic redundancy and rolling back
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

    public ExpRewriten rewriteAtomicJoinWithoutUnionInLeftAndRight(InnerJoinNode root, IQTree left, IQTree right){
        //Rewriting by empty federated joins

        ExpRewriten ER = new ExpRewriten();
        //complete the checking conditions
        //Left part: A 或者 A1 JOIN A2 JOIN ... An
        //Right part: B 或者 B1 JOIN B2 JOIN ... JOIN Bm
        //A, B, Ai, Bi, without UNION

        JoinOfElements JOL_left = getElementsOfJoin(left);
        JoinOfElements JOL_right = getElementsOfJoin(right);
        //keep the order of the leafs
        //目前只处理基于base relation的hints
        if((JOL_left.dataElement.size() == 0) || (JOL_right.dataElement.size() == 0)){
            return ER;
        }

        Set<String> sources = getSources(left);
        sources.addAll(getSources(right));
        if(sources.size() > 1){
            for(int i=0; i<JOL_left.dataElement.size(); i++){
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_left = JOL_left.dataElement.get(i).dn.getArgumentMap();
                for(int j=0; j<JOL_right.dataElement.size(); j++){
                    ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_right = JOL_right.dataElement.get(j).dn.getArgumentMap();
                    for(int k: JOL_left.dataElement.get(i).dn.getArgumentMap().keySet()){
                        for(int l: JOL_right.dataElement.get(j).dn.getArgumentMap().keySet()){
                            if(JOL_left.dataElement.get(i).dn.getArgumentMap().get(k).equals(JOL_right.dataElement.get(j).dn.getArgumentMap().get(l))){
                                //change the check condition based on different ways of representing hints

//                            String normalName_left = getNormalFormOfRelation(JOL_left.relations.get(i).getName());
//                            String normalName_right = getNormalFormOfRelation(JOL_right.relations.get(j).getName());
                                String name_left = JOL_left.dataElement.get(i).relation.getName();
                                String name_right = JOL_right.dataElement.get(j).relation.getName();

                                if(hints.get(2).contains(name_left+"<>"+name_right+"<>"+k+"<>"+l) || hints.get(2).contains(name_right+"<>"+name_left+"<>"+l+"<>"+k)){
                                    ER.canRewrite = true;
                                    tryingEmptyJoinRule.set(Boolean.TRUE);
                                    return ER;
                                }
                                // check rewriting by materialized views
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

    //to handle the different ways of identifying relations in VDBs for different federation systems
    public String getNormalFormOfRelation(String relationName){
        String normalForm = "";
        normalForm = relationName.substring(relationName.lastIndexOf(".")+1, relationName.length());
        if(!normalForm.startsWith("\"")){
            normalForm = "\""+normalForm+"\"";
        }
        if(sourceMap.containsKey(normalForm)){
            normalForm = sourceMap.get(normalForm)+"."+normalForm;
        } else {
            LOGGER.debug("missing source information of relations in VDB");
        }

        return normalForm;
    }

    //recreate join tree for (JoinNode, left, right) where sfr rule is applied
    public ExpRewriten createJoinTree(InnerJoinNode root, JoinOfElements left, JoinOfElements right){
        ExpRewriten ER = new ExpRewriten();
        //添加construction nodes;

        Set<Variable> vars_l_r = new HashSet<>();
        List<IQTree> childern_new = new ArrayList<IQTree>();
        List<Integer> right_index = new ArrayList<Integer>(); //记录右边被SJ消除的部分

        for(int i=0; i<left.dataElement.size(); i++){
            IQTree t = left.dataElement.get(i).dn;

            List<Integer> right_index_new = new ArrayList<Integer>();  //for sj checking  //被溶解的点不含有投影

            for(int j=0; j<right.dataElement.size(); j++){
                if(right_index.contains(j)){
                    continue;
                }
                if(right.dataElement.get(j).relation.equals(left.dataElement.get(i).relation)){
                    for(int index: getSinglePrimaryKeyIndexOfRelations(left.dataElement.get(i).dn)){
                        if(left.dataElement.get(i).dn.getArgumentMap().containsKey(index) && right.dataElement.get(j).dn.getArgumentMap().containsKey(index) &&
                                left.dataElement.get(i).dn.getArgumentMap().get(index).equals(right.dataElement.get(j).dn.getArgumentMap().get(index))){
                            //if(right.constructions.get(j) == null){   //new added conditions
                            right_index_new.add(j);
                            ER.sjRewrite = true;
                            tryingSelfJoinRule.set(Boolean.TRUE);
                            //}
                        }
                    }
                }
            }

            if(right_index_new.size() == 0){  //不存在SJ优化
                childern_new.add(createDataTree(left.dataElement.get(i)));
                vars_l_r.addAll(t.getVariables());
            } else {   //存在SJ优化
                vars_l_r.addAll(getVariablesOfDataElement(left.dataElement.get(i)));
                List<DataElement> des = new ArrayList<DataElement>();
                for(int j: right_index_new){
                    des.add(right.dataElement.get(j));
                    vars_l_r.addAll(getVariablesOfDataElement(right.dataElement.get(j)));
                }
                childern_new.add(mergeDataTreesSJ(left.dataElement.get(i),des));

                right_index.addAll(right_index_new);
            }
        }

        for(int i=0; i<right.dataElement.size(); i++){
            if(right_index.contains(i)){
                continue;
            }
            childern_new.add(createDataTree(right.dataElement.get(i)));
            vars_l_r.addAll(getVariablesOfDataElement(right.dataElement.get(i)));
        }

        InnerJoinNode root_new = root;
        ImmutableSet<Variable> vars = root.getLocalVariables();
        //可能childern_new只有一个孩子, SJ的应用;;
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

    //这一块代码的优化
    public JoinOfElements getElementsOfJoin(IQTree t){
        JoinOfElements JOL = new JoinOfElements();

        if((t.isLeaf()) || (t.getRootNode() instanceof FilterNode) || (t.getRootNode() instanceof ConstructionNode)){
            addElement(JOL, t);
        } else if(t.getRootNode() instanceof InnerJoinNode){
            JOL.conditions = ((InnerJoinNode) t.getRootNode()).getOptionalFilterCondition();
            for(IQTree t_new: t.getChildren()){
                if((t_new instanceof ExtensionalDataNode) || (t_new.getRootNode() instanceof FilterNode) || (t_new.getRootNode() instanceof ConstructionNode)){
                    addElement(JOL, t_new);
                } else if(t_new.getRootNode() instanceof InnerJoinNode){
                    //lift join condition
                    for(IQTree sub: t_new.getChildren()){
                        if((sub instanceof ExtensionalDataNode) || (sub.getRootNode() instanceof FilterNode) || (sub.getRootNode() instanceof ConstructionNode)){
                            addElement(JOL, sub);
                        } else {
                            JOL.otherSubTrees.add(sub);
                        }
                    }
                } else {
                    JOL.otherSubTrees.add(t_new);
                }
            }

        } else {
            JOL.otherSubTrees.add(t);
        }
        return JOL;
    }

    public JoinOfElements addElement(JoinOfElements JOL, IQTree t){
        if(t.isLeaf()){
            if(t instanceof ExtensionalDataNode){
                DataElement de = new DataElement((ExtensionalDataNode) t, null, null, ((ExtensionalDataNode)t).getRelationDefinition().getAtomPredicate());
                JOL.dataElement.add(de);
            } else {
                JOL.otherSubTrees.add(t);
            }
        } else if (t.getRootNode() instanceof FilterNode){
            if(t.getChildren().get(0) instanceof ExtensionalDataNode){
                DataElement de = new DataElement((ExtensionalDataNode)t.getChildren().get(0),(FilterNode)t.getRootNode(),null, ((ExtensionalDataNode)t.getChildren().get(0)).getRelationDefinition().getAtomPredicate());
                JOL.dataElement.add(de);
            } else {
                JOL.otherSubTrees.add(t);
            }
        } else if (t.getRootNode() instanceof ConstructionNode){
            if(t.getChildren().get(0) instanceof ExtensionalDataNode){
                DataElement de = new DataElement((ExtensionalDataNode)t.getChildren().get(0),null,(ConstructionNode)t.getRootNode(), ((ExtensionalDataNode)t.getChildren().get(0)).getRelationDefinition().getAtomPredicate());
                JOL.dataElement.add(de);
            } else if(t.getChildren().get(0).getRootNode() instanceof FilterNode){
                if(t.getChildren().get(0).getChildren().get(0) instanceof ExtensionalDataNode){
                    DataElement de = new DataElement((ExtensionalDataNode)t.getChildren().get(0).getChildren().get(0),(FilterNode)t.getChildren().get(0).getRootNode(),(ConstructionNode)t.getRootNode(), ((ExtensionalDataNode)t.getChildren().get(0).getChildren().get(0)).getRelationDefinition().getAtomPredicate());
                    JOL.dataElement.add(de);
                } else {
                    JOL.otherSubTrees.add(t);
                }

            } else {
                JOL.otherSubTrees.add(t);
            }
        }
        return JOL;
    }

    public IQTree createDataTree(DataElement de){
        IQTree t = null;
        if((de.fn == null) && (de.cn == null)){
            t = de.dn;
        } else if(de.fn == null){
            t = IQ_FACTORY.createUnaryIQTree(de.cn, de.dn);
        } else if(de.cn == null){
            t = IQ_FACTORY.createUnaryIQTree(de.fn, de.dn);
        } else {
            t = IQ_FACTORY.createUnaryIQTree(de.cn, IQ_FACTORY.createUnaryIQTree(de.fn, de.dn));
        }
        return t;
    }

    public Set<Variable> getVariablesOfDataElement(DataElement de){
        if(de.cn != null){
            return de.cn.getVariables();
        } else {
            return de.dn.getVariables();
        }
    }

    public IQTree mergeDataTreesSJ(DataElement de_left, List<DataElement> des_right){
        IQTree t = null;

        HashMap<Integer, VariableOrGroundTerm> arg_maps = new HashMap<Integer, VariableOrGroundTerm>();
        arg_maps.putAll(de_left.dn.getArgumentMap());
        for(DataElement de: des_right){
            arg_maps.putAll(de.dn.getArgumentMap());
        }
        ExtensionalDataNode dataNode_new = IQ_FACTORY.createExtensionalDataNode(de_left.dn.getRelationDefinition(), ImmutableMap.copyOf(arg_maps));


        //创建filter nodes 并 构建filter node的conditions
        List<ImmutableExpression> conditions = new ArrayList<ImmutableExpression>();
        FilterNode fn_new = null;
        if(de_left.fn != null){
            BooleanFunctionSymbol bfs = de_left.fn.getFilterCondition().getFunctionSymbol();
            if(bfs.toString().startsWith("AND")){ //change
                for(ImmutableTerm it: de_left.fn.getFilterCondition().getTerms()){
                    conditions.add((ImmutableExpression) it);
                }
            } else {
                conditions.add(de_left.fn.getFilterCondition());
            }
        }
        for(int j=0; j<des_right.size(); j++){
            if(des_right.get(j).fn != null){
                BooleanFunctionSymbol bfs = des_right.get(j).fn.getFilterCondition().getFunctionSymbol();
                if(bfs.toString().startsWith("AND")){ //change
                    for(ImmutableTerm it: des_right.get(j).fn.getFilterCondition().getTerms()){
                        conditions.add((ImmutableExpression) it);
                    }
                } else {
                    conditions.add(des_right.get(j).fn.getFilterCondition());
                }
            }
        }
        if(conditions.size() > 0){
            ImmutableExpression exp = TERM_FACTORY.getConjunction(ImmutableList.copyOf(conditions));//null; //null;
            fn_new = IQ_FACTORY.createFilterNode(exp);
        }

        //创建 construction node 并且判断是否有construction node :
        boolean cn_b = false;
        Set<Variable> vars_cn = new HashSet<Variable>();
        List<Substitution<ImmutableTerm>> substitution_cn = new ArrayList<Substitution<ImmutableTerm>>();
        ConstructionNode cn_new = null;
        if(de_left.cn != null){
            cn_b = true;
            vars_cn.addAll(de_left.cn.getVariables());
            if(!de_left.cn.getSubstitution().isEmpty()){
                substitution_cn.add(de_left.cn.getSubstitution());
            }
        } else {
            vars_cn.addAll(de_left.dn.getVariables());
        }
        for(int j=0; j<des_right.size(); j++){
            if(des_right.get(j).cn != null){
                cn_b = true;
                vars_cn.addAll(des_right.get(j).cn.getVariables());
                if(!des_right.get(j).cn.getSubstitution().isEmpty()){
                    substitution_cn.add(des_right.get(j).cn.getSubstitution());
                }
            } else {
                vars_cn.addAll(des_right.get(j).dn.getVariables());
            }
        }
        if(cn_b){
            if(substitution_cn.size() == 0){
                cn_new = IQ_FACTORY.createConstructionNode(ImmutableSet.copyOf(vars_cn));
            } else {
                if(substitution_cn.size() == 1){
                    cn_new = IQ_FACTORY.createConstructionNode(ImmutableSet.copyOf(vars_cn), substitution_cn.get(0));
                } else {
                    Substitution<ImmutableTerm> subs_merge = substitution_cn.get(0);
                    for(int i=1; i<substitution_cn.size(); i++){
                        subs_merge = subs_merge.compose(substitution_cn.get(i));
                    }
                    cn_new = IQ_FACTORY.createConstructionNode(ImmutableSet.copyOf(vars_cn), subs_merge);
                }
            }
        }

        t = createDataTree(new DataElement(dataNode_new, fn_new, cn_new, null));

        return t;
    }


    public List<IQTree> removeSemanticRedundancyAndRollingBack(List<IQTree> trees){

        List<IQTree> results = trees;

        //check and remove semantic redundancy

        List<Integer> index = new ArrayList<Integer>();

        for(int i=0; i<trees.size(); i++){
            if(trees.get(i).getChildren().size() == 1){
                continue;
            }

            List<IQTree> subs = new ArrayList<IQTree>();
            subs.addAll(trees.get(i).getChildren());
            int ind = -1;
            for(int j=0; j<subs.size(); j++){
                if(subs.get(j).getVariables().size() == 1){
                    //可能非一元，extensionalNode中含有常量绑定
                    if(subs.get(j) instanceof ExtensionalDataNode){
                        if(((ExtensionalDataNode) subs.get(j)).getArgumentMap().size() == 1){
                            ind = j;
                        }
                    } else if (subs.get(j).getRootNode() instanceof FilterNode){
                        if(subs.get(j).getChildren().get(0) instanceof ExtensionalDataNode){
                            if(((ExtensionalDataNode)subs.get(j).getChildren().get(0)).getArgumentMap().size() == 1){
                                ind = j;
                            }
                        }
                    } else if(subs.get(j).getRootNode() instanceof ConstructionNode){
                        //新添加的
                        ind = j;
                    }
                }
            }
            if(ind == -1){
                continue;
            }
            //complex situation: inner join node:
            Optional<ImmutableExpression> join_cond_ti = ((InnerJoinNode) trees.get(i).getRootNode()).getOptionalFilterCondition();
            ImmutableExpression cond_join_ti = null;
            ImmutableExpression cond_cn_ind = null;
            ImmutableExpression cond_filter_ind = null;
            if(!join_cond_ti.isEmpty()){
                cond_join_ti = join_cond_ti.get();
            }
            if(subs.get(ind).getRootNode() instanceof ConstructionNode){
                Variable v_cn = null;
                for(Variable v: ((ConstructionNode) subs.get(ind).getRootNode()).getVariables()){
                    v_cn = v;
                }
                cond_cn_ind = TERM_FACTORY.getDBIsNotNull(v_cn);
                //create IS_NOT_NULL conditions on v_cn;

            }
            if(subs.get(ind).getRootNode() instanceof FilterNode){
                cond_filter_ind = ((FilterNode) subs.get(ind).getRootNode()).getFilterCondition();
            }

            subs.remove(ind);

            List<ImmutableExpression> cond_extra = new ArrayList<ImmutableExpression>();
            if(cond_filter_ind != null){
                if(cond_filter_ind.getFunctionSymbol().getName().startsWith("AND")){
                    for(ImmutableTerm term: cond_filter_ind.getTerms()){
                        cond_extra.add((ImmutableExpression) term);
                    }
                } else {
                    cond_extra.add(cond_filter_ind);
                }
            }
            if(cond_cn_ind != null){
                cond_extra.add(cond_cn_ind);
            }

            if(subs.size() == 1){
                if(subs.get(0).getRootNode() instanceof FilterNode){
                    List<ImmutableExpression> cond_fn = new ArrayList<ImmutableExpression>();
                    FilterNode fn = (FilterNode) subs.get(0).getRootNode();
                    if(fn.getFilterCondition().getFunctionSymbol().getName().startsWith("AND")){
                        for(ImmutableTerm term: fn.getFilterCondition().getTerms()){
                            cond_fn.add((ImmutableExpression) term);
                        }
                    } else {
                        cond_fn.add(fn.getFilterCondition());
                    }
                    IQTree subt = subs.get(0).getChildren().get(0);
                    if(cond_join_ti == null){
                        if(trees.contains(subs.get(0))){
                            index.add(i);
                        } else {
                            if(cond_extra.size() > 0){
                                cond_fn.addAll(cond_extra);
                                IQTree t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.copyOf(cond_fn))), subt);
                                if(trees.contains(t_new)){
                                    index.add(i);
                                }
                            }
                        }
                    } else {
                        IQTree t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(cond_join_ti, fn.getFilterCondition())), subt);
                        if(trees.contains(t_new)){
                            index.add(i);
                        } else {
                            if(cond_extra.size() > 0){
                                cond_fn.addAll(cond_extra);
                                t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(cond_join_ti, TERM_FACTORY.getConjunction(ImmutableList.copyOf(cond_fn)))), subt);
                                if(trees.contains(t_new)){
                                    index.add(i);
                                }
                            }
                        }
                    }
                } else {
                    if(cond_join_ti == null){
                        if(trees.contains(subs.get(0))){
                            index.add(i);
                        } else {
                            if(cond_extra.size() > 0){
                                IQTree t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.copyOf(cond_extra))),subs.get(0));
                                if(trees.contains(t_new)){
                                    index.add(i);
                                }
                            }
                        }
                    } else {
                        IQTree t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(cond_join_ti),subs.get(0));
                        if(trees.contains(t_new)){
                            index.add(i);
                        } else {
                            if(cond_extra.size() > 0){
                                ImmutableExpression exp = TERM_FACTORY.getConjunction(ImmutableList.copyOf(cond_extra));
                                t_new = IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(cond_join_ti,exp)),subs.get(0));
                                if(trees.contains(t_new)){
                                    index.add(i);
                                }
                            }
                        }
                    }
                }
            } else {
                IQTree t_new = IQ_FACTORY.createNaryIQTree( (InnerJoinNode)trees.get(i).getRootNode(), ImmutableList.copyOf(subs));
                if(trees.contains(t_new)){
                    index.add(i);
                }
            }
        }

        if(index.size() > 0){
            List<IQTree> trees_new = new ArrayList<IQTree>();
            for(int i=0; i<trees.size(); i++){
                if(!index.contains(i)){
                    trees_new.add(trees.get(i));
                }
            }
            trees.clear();
            trees.addAll(trees_new);
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

    public List<Integer> getSinglePrimaryKeyIndexOfRelations(ExtensionalDataNode dataNode){
        //unique primary key index
        List<Integer> index = new ArrayList<Integer>();

        ImmutableList<Attribute> attrs = dataNode.getRelationDefinition().getAttributes();
        for(UniqueConstraint uc: dataNode.getRelationDefinition().getUniqueConstraints()){
            //if(uc.isPrimaryKey()){
            ImmutableList<Attribute> ats = uc.getAttributes();
            //DO NOT ADD DUPLICATES otherwise you get multiple same filterconditions
            if(ats.size() == 1 && !index.contains(attrs.indexOf(ats.get(0)))){
                index.add(attrs.indexOf(ats.get(0)));
            }
            //}
        }

        return index;
    }

    public IQTree rewriteLeftJoin(IQTree iqt){
        tryingEmptyJoinRule.set(Boolean.FALSE);
        tryingSelfJoinRule.set(Boolean.FALSE);
        boolean update = true;
        module: while(update){
            update = false;
            List<IQTree> subTrees = getAllSubTree(iqt);

            for(IQTree t : subTrees){
                QueryNode qn = t.getRootNode();
                if(qn instanceof LeftJoinNode){
//                    Set<String> sources = getSources(t);
//                    if(sources.size() == 1){
//                        continue;
//                    }
                    ImmutableList<IQTree> childern =t.getChildren(); // only have two childern

                    ExpRewriten rewriten = rewriteAtomicLeftJoin((LeftJoinNode)qn, childern.get(0), childern.get(1));

                    if(rewriten.canRewrite){

                        IQTree t_new = iqt.replaceSubTree(t, rewriten.newRewritten);
                        List<Integer> cost_new = getCostOfIQTree(t_new);
                        List<Integer> cost_old = getCostOfIQTree(iqt);
                        if((cost_old.get(0)>= cost_new.get(0)) && (cost_old.get(1) >= cost_new.get(1))){
                            update = true;
                            iqt = t_new;
                            if (tryingEmptyJoinRule.get()) {
                                onEmptyJoinRuleApplied();
                            }
                            if (tryingSelfJoinRule.get()) {
                                onSelfJoinRuleApplied();
                            }
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

        List<JoinOfElements> left_part = new ArrayList<JoinOfElements>();
        List<JoinOfElements> right_part = new ArrayList<JoinOfElements>();

        if((left instanceof ExtensionalDataNode) || (left.getRootNode() instanceof FilterNode) || (left.getRootNode() instanceof InnerJoinNode) ){
            if(getElementsOfJoin(left).dataElement.size() > 0){
                left_part.add(getElementsOfJoin(left));
            } else {
                return ER;
            }
        } else if(left.getRootNode() instanceof UnionNode){
            for(IQTree t: left.getChildren()){
                if((t instanceof ExtensionalDataNode) || (t.getRootNode() instanceof FilterNode) || (t.getRootNode() instanceof InnerJoinNode)){
                    if(getElementsOfJoin(t).dataElement.size()>0){
                        left_part.add(getElementsOfJoin(t));
                    } else {
                        return ER;
                    }
                }
            }
        } else {
            return ER;
        }

        if((right instanceof ExtensionalDataNode) || (right.getRootNode() instanceof FilterNode) || (right.getRootNode() instanceof InnerJoinNode) ){
            if(getElementsOfJoin(right).dataElement.size() > 0){
                right_part.add(getElementsOfJoin(right));
            } else {
                return ER;
            }
        } else if(right.getRootNode() instanceof UnionNode){
            for(IQTree t: right.getChildren()){
                if((t instanceof ExtensionalDataNode) || (t.getRootNode() instanceof FilterNode) || (t.getRootNode() instanceof InnerJoinNode)){
                    if(getElementsOfJoin(t).dataElement.size()>0){
                        right_part.add(getElementsOfJoin(t));
                    } else {
                        return ER;
                    }
                }
            }
        } else {
            return ER;
        }

        if((left_part.size()>1) && (right_part.size()==1)){
            return ER;
        }

        Map<Integer, Integer> index = new HashMap<Integer, Integer>(); // Ai JOIN Bj not empty

        for(int i=0; i<left_part.size(); i++){
            for(int k=0; k<right_part.size(); k++){
                boolean label = false;

                for(int j=0; j<left_part.get(i).dataElement.size(); j++){
                    for(int l=0; l<right_part.get(k).dataElement.size(); l++){
                        RelationPredicate relation_left = left_part.get(i).dataElement.get(j).relation;
                        String name_left = relation_left.toString();
                        //                      String normalName_left = getNormalFormOfRelation(name_left);
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_left = left_part.get(i).dataElement.get(j).dn.getArgumentMap();

                        RelationPredicate relation_right = right_part.get(k).dataElement.get(l).relation;
                        String name_right = relation_right.toString();
                        //                      String normalName_right = getNormalFormOfRelation(name_right);
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_right = right_part.get(k).dataElement.get(l).dn.getArgumentMap();

                        for(int f: arg_left.keySet()){
                            for(int h: arg_right.keySet()){
                                if((arg_left.get(f) instanceof Variable)&&(arg_right.get(h) instanceof Variable)&&(arg_left.get(f).equals(arg_right.get(h)))){
                                    if(hints.get(2).contains(name_left+"<>"+name_right+"<>"+f+"<>"+h)||hints.get(2).contains(name_right+"<>"+name_left+"<>"+h+"<>"+f)){
                                        label = true;
                                        tryingEmptyJoinRule.set(Boolean.TRUE);
                                    }
                                }
                                ///new added part----------------------------------------------------------
                                else if((arg_left.get(f) instanceof GroundTerm)&&(arg_right.get(h) instanceof GroundTerm)&&(arg_left.get(f).equals(arg_right.get(h)))){ // new added condition for A(a) JOIN B(a) empty when A(x) JOIN B(x) empty
                                    if(hints.get(2).contains(name_left+"<>"+name_right+"<>"+f+"<>"+h)||hints.get(2).contains(name_right+"<>"+name_left+"<>"+h+"<>"+f)){
                                        label = true;
                                        tryingEmptyJoinRule.set(Boolean.TRUE);
                                    }
                                }
                                ///new added part----------------------------------------------------------
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

        //当LJ的左节点是一个UNION节点，检查左侧元素的两两不相交的性质
        if(left_part.size()>1){
            for(int i=0; i<left_part.size(); i++){
                for(int j=i+1; j<left_part.size(); j++){
                    boolean b = false;
                    for(int k=0; k<left_part.get(i).dataElement.size(); k++){
                        RelationPredicate relation_1 = left_part.get(i).dataElement.get(k).relation;
                        String name_1 = relation_1.toString();
//                        String normalName_1 = getNormalFormOfRelation(name_1);
                        ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_1 = left_part.get(i).dataElement.get(k).dn.getArgumentMap();
                        for(int l=0; l<left_part.get(j).dataElement.size(); l++){
                            RelationPredicate relation_2 = left_part.get(j).dataElement.get(l).relation;
                            String name_2 = relation_2.toString();
//                            String normalName_2 = getNormalFormOfRelation(name_2);
                            ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_2 = left_part.get(j).dataElement.get(l).dn.getArgumentMap();
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

        if((left_part.size() == 1)){
            if(right_part.size() > 1){
                if((index.size() < right_part.size())){
                    ER.canRewrite = true;
                    // A LeftJoin (B1 U...U Bm)
                    ImmutableSet<Variable> var_right = right.getVariables();
                    IQTree left_new = left;
                    List<IQTree> sub_right_new = new ArrayList<IQTree>();
                    for(int i: index.keySet()){
                        sub_right_new.add(right.getChildren().get(i));
                    }
                    IQTree right_new = IQ_FACTORY.createNaryIQTree((UnionNode)right.getRootNode(), ImmutableList.copyOf(sub_right_new));
                    ER.newRewritten = IQ_FACTORY.createBinaryNonCommutativeIQTree(root, left_new, right_new);
                }
                return ER;
            } else {
                if(right_part.get(0).dataElement.size() > 1){
                    //this situation cannot apply self-left-join rewriting
                    return ER;
                } // 可以SLJ重写的内容，涵盖在了下面的实现中
            }

        }

        if(index.size() == left_part.size()){   //rewrite into union of left joins
            List<IQTree> subtrees = new ArrayList<IQTree>();
            for(int i: index.keySet()){
                int j = index.get(i);     //generate Ai LJ Bj
                if(right_part.get(j).dataElement.size()==1){
                    boolean b = false; //self-left-join check
                    int ind_i = 0;
                    RelationPredicate relation_j = right_part.get(j).dataElement.get(0).relation;
                    ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_j = right_part.get(j).dataElement.get(0).dn.getArgumentMap();

                    for(int k=0; k<left_part.get(i).dataElement.size(); k++){
                        if(left_part.get(i).dataElement.get(k).relation.equals(relation_j)){
                            int ind = k;
                            ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_i = left_part.get(i).dataElement.get(ind).dn.getArgumentMap();
                            List<Integer> pk_index_i = getSinglePrimaryKeyIndexOfRelations(left_part.get(i).dataElement.get(ind).dn);
                            for(int pk_ind: pk_index_i){
                                if(arg_j.containsKey(pk_ind) && arg_i.containsKey(pk_ind) && (arg_j.get(pk_ind).equals(arg_i.get(pk_ind)))){
                                    tryingSelfJoinRule.set(Boolean.TRUE);
                                    b=true;
                                    ind_i = ind;
                                }
                            }
                        }
                    }
                    if(b){
                        List<DataElement> de_right = new ArrayList<DataElement>();

                        FilterNode fn_right = right_part.get(j).dataElement.get(0).fn;
                        if(fn_right != null) { //new added condition
                            if(fn_right.getFilterCondition().getFunctionSymbol().getName().startsWith("IS_NOT_NULL")){
                                fn_right = null;
                            } else if (fn_right.getFilterCondition().getFunctionSymbol().getName().startsWith("AND")){
                                ImmutableExpression ie = fn_right.getFilterCondition();
                                List<ImmutableTerm> terms = new ArrayList<ImmutableTerm>();
                                terms.addAll(ie.getTerms());
                                List<ImmutableExpression> terms_new = new ArrayList<ImmutableExpression>();
                                boolean have_null = false;
                                for(ImmutableTerm it: terms){
                                    if(! it.toString().startsWith("IS_NOT_NULL")){
                                        terms_new.add((ImmutableExpression)it);
                                    } else {
                                        have_null = true;
                                    }
                                }

                                //添加判断terms_new是否是一个空集合，是否具有一个元素，多个元素
                                if(have_null){
                                    if(terms_new.size() > 1){
                                        fn_right = IQ_FACTORY.createFilterNode(TERM_FACTORY.getConjunction(ImmutableList.copyOf(terms_new)));
                                    } else if(terms_new.size() == 1){
                                        fn_right = IQ_FACTORY.createFilterNode(terms_new.get(0));
                                    } else if(terms_new.size() == 0){
                                        fn_right = null;
                                    }
                                }
                            }
                        }
                        right_part.get(j).dataElement.get(0).fn = fn_right;  // 新添加的

                        de_right.add(right_part.get(j).dataElement.get(0));
                        IQTree node_SJ = mergeDataTreesSJ(left_part.get(i).dataElement.get(ind_i), de_right);

                        if(left_part.get(i).dataElement.size() == 1){
                            subtrees.add(node_SJ);
                        } else {
                            InnerJoinNode join = IQ_FACTORY.createInnerJoinNode(left_part.get(i).conditions);
                            List<IQTree> sub_sub_t = new ArrayList<IQTree>();
                            for(int l=0; l<left_part.get(i).dataElement.size(); l++){
                                if(l != ind_i){
                                    sub_sub_t.add(createDataTree(left_part.get(i).dataElement.get(l)));
                                } else {
                                    sub_sub_t.add(node_SJ);
                                }
                            }

                            sub_sub_t.addAll(left_part.get(i).otherSubTrees);  //新添加的;

                            subtrees.add(IQ_FACTORY.createNaryIQTree(join, ImmutableList.copyOf(sub_sub_t)));
                        }
                    } else {
                        if((left_part.size() == 1) && (right_part.size() == 1)){
                            //此种情况下，不存在SLJ优化，就可以返回了;
                            return ER;
                        }
                        LeftJoinNode root_subtree = root; //make a copy of root

                        IQTree left_part_new = left;
                        if(left.getChildren().size()>0){
                            left_part_new = left.getChildren().get(i);
                        }
                        IQTree right_part_new = right;
                        if(right.getChildren().size()>0){
                            right_part_new = right.getChildren().get(j);
                        }

                        //subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left.getChildren().get(i), right.getChildren().get(j)));
                        subtrees.add(IQ_FACTORY.createBinaryNonCommutativeIQTree(root_subtree, left_part_new, right_part_new));
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
                                    onMatViewRuleApplied();
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

        JoinOfElements JOL_left = getElementsOfJoin(left);
        JoinOfElements JOL_right = getElementsOfJoin(right);
        //keep the order of the leafs
        if((JOL_left.dataElement.size() == 0) || (JOL_right.dataElement.size() == 0)){
            return ER;
        }

        Set<String> sources = getSources(left);
        sources.addAll(getSources(right));
        if(sources.size() > 1){
            for(int i=0; i<JOL_left.dataElement.size(); i++){
                ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_left = JOL_left.dataElement.get(i).dn.getArgumentMap();
                for(int j=0; j<JOL_right.dataElement.size(); j++){
                    ImmutableMap<Integer, ? extends VariableOrGroundTerm> arg_map_right = JOL_right.dataElement.get(j).dn.getArgumentMap();
                    for(int k: JOL_left.dataElement.get(i).dn.getArgumentMap().keySet()){
                        for(int l: JOL_right.dataElement.get(j).dn.getArgumentMap().keySet()){
                            if(JOL_left.dataElement.get(i).dn.getArgumentMap().get(k).equals(JOL_right.dataElement.get(j).dn.getArgumentMap().get(l))){
                                //change the check condition based on different ways of representing hints
                                //check conditions for rewriting based on materialized views;
                                String relation1 = JOL_left.dataElement.get(i).relation.getName();
                                //                    String normalName_relation1 = getNormalFormOfRelation(relation1);
                                int ind1 = k;
                                String relation2 = JOL_right.dataElement.get(j).relation.getName();
                                //                    String normalName_relation2 = getNormalFormOfRelation(relation2);
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
                                    if(JOL_left.dataElement.get(i).fn != null){
                                        ImmutableExpression exp = JOL_left.dataElement.get(i).fn.getFilterCondition();
                                        if(exp.getFunctionSymbol().getName().startsWith("AND")){
                                            for(ImmutableTerm it: exp.getTerms()){
                                                conds.add((ImmutableExpression) it);
                                            }
                                        } else {
                                            conds.add(exp);
                                        }
                                    }
                                    if(JOL_right.dataElement.get(j).fn != null){
                                        ImmutableExpression exp = JOL_right.dataElement.get(j).fn.getFilterCondition();
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

                                    ConstructionNode cn_ij = null;
                                    boolean cn_node = false;
                                    Set<Variable> cn_vars = new HashSet<Variable>();
                                    List<Substitution<ImmutableTerm>> cn_subs = new ArrayList<Substitution<ImmutableTerm>>();
                                    if(JOL_left.dataElement.get(i).cn != null){
                                        cn_node = true;
                                        cn_vars.addAll(JOL_left.dataElement.get(i).cn.getVariables());
                                        cn_subs.add(JOL_left.dataElement.get(i).cn.getSubstitution());
                                    } else {
                                        cn_vars.addAll(getVariablesOfDataElement(JOL_left.dataElement.get(i)));
                                    }
                                    if(JOL_right.dataElement.get(j).cn != null){
                                        cn_node = true;
                                        cn_vars.addAll(JOL_right.dataElement.get(j).cn.getVariables());
                                        cn_subs.add(JOL_right.dataElement.get(j).cn.getSubstitution());
                                    } else {
                                        cn_vars.addAll(getVariablesOfDataElement(JOL_right.dataElement.get(j)));
                                    }
                                    if(cn_node){
                                        if(cn_subs.size() == 0){
                                            cn_ij = IQ_FACTORY.createConstructionNode(ImmutableSet.copyOf(cn_vars));
                                        } else if(cn_subs.size() == 1){
                                            cn_ij = IQ_FACTORY.createConstructionNode(ImmutableSet.copyOf(cn_vars), cn_subs.get(0));
                                        } else {
                                            cn_ij = IQ_FACTORY.createConstructionNode(ImmutableSet.copyOf(cn_vars), cn_subs.get(0).compose(cn_subs.get(1)));
                                        }
                                    }

                                    ExtensionalDataNode edn_ij = IQ_FACTORY.createExtensionalDataNode(NRD, ImmutableMap.copyOf(args_new));
                                    DataElement de = new DataElement(edn_ij, fn_ij, cn_ij, null);

                                    JOL_left.dataElement.set(i,de);

                                    JOL_right.dataElement.remove(j);

                                    ER = createJoinTree(root, JOL_left, JOL_right);
                                    ER.canRewrite = true;
                                    return ER;
                                }
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

    public IQ IQTreeToIQ (DistinctVariableOnlyDataAtom project_original, IQTree iqt)throws Exception {
        //AtomPredicate ANS1 = ATOM_FACTORY.getRDFAnswerPredicate(iqt.getVariables().size());
        //DistinctVariableOnlyDataAtom projection = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1, ImmutableList.copyOf(iqt.getVariables()));
        return IQ_FACTORY.createIQ(project_original, iqt);
    }

    protected void onEquivalentRedundancyRuleApplied() {
        // can override in sub-classes
    }

    protected void onEmptyJoinRuleApplied() {
        // can override in sub-classes
    }

    protected void onSelfJoinRuleApplied() {
        // can override in sub-classes
    }

    protected void onMatViewRuleApplied() {
        // can override in sub-classes
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

class JoinOfElements{
    //represent the nodes of join trees, DataNode, FilterNode+DataNode, ConstructionNode+DataNode, ConstructionNode+FilterNode+DataNode, Other SubTrees
    public List<DataElement> dataElement;
    public Optional<ImmutableExpression> conditions;
    public List<IQTree> otherSubTrees;

    public JoinOfElements(){
        dataElement = new ArrayList<DataElement>();
        conditions = null;
        otherSubTrees = new ArrayList<IQTree>();
    }
}

class DataElement{
    public ExtensionalDataNode dn;
    public FilterNode fn;
    public ConstructionNode cn;
    public RelationPredicate relation;

    public DataElement(){
        dn = null;
        fn = null;
        cn = null;
        relation = null;
    }
    public DataElement(ExtensionalDataNode dn, FilterNode fn, ConstructionNode cn, RelationPredicate relation){
        this.dn = dn;
        this.fn = fn;
        this.cn = cn;
        this.relation = relation;
    }
}
enum SourceLab {
    DYNAMIC, STATIC, EFFICIENT, INEFFICIENT;
}