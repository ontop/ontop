package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.TargetAtom;
import it.unibz.inf.ontop.datalog.impl.DatalogConversionTools;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.transformer.StaticRuleMappingSaturator;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.StaticAtomicExpressionImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.eclipse.rdf4j.query.algebra.Copy;

import java.util.*;

@Singleton
public class StaticRuleMappingSaturatorImpl implements StaticRuleMappingSaturator {

    private  final IntermediateQueryFactory IQFactory;
    private final TermFactory termFactory;
    private final DatalogConversionTools datalogConversionTools;
    private final IQConverter iqConverter;
    private final SpecificationFactory specificationFactory;

    @Inject
    private StaticRuleMappingSaturatorImpl(IntermediateQueryFactory IQFactory, TermFactory termFactory,
                                           DatalogConversionTools datalogConversionTools, IQConverter iqConverter, SpecificationFactory specificationFactory) {
        this.IQFactory = IQFactory;
        this.termFactory = termFactory;
        this.datalogConversionTools = datalogConversionTools;
        this.iqConverter = iqConverter;
        this.specificationFactory = specificationFactory;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, DatalogMTLProgram datalogMTLProgram) {
        Queue<DatalogMTLRule> queue = new LinkedList<>();
        queue.addAll(datalogMTLProgram.getRules());
        IQ iq;
        Map<AtomPredicate, IntermediateQuery> mappingMap = new HashMap<>();
        mapping.getPredicates().forEach(atomPredicate -> mappingMap.put(atomPredicate, mapping.getDefinition(atomPredicate).get()));

        while(!queue.isEmpty()){
            DatalogMTLRule rule = queue.poll();
            if ((rule.getBody() instanceof StaticExpression) ||
                    ((rule.getBody() instanceof FilterExpression) &&
                            (((FilterExpression) rule.getBody()).getExpression() instanceof StaticExpression))) {
                ImmutableList<StaticAtomicExpression> staticAtomicExpressionsList = getAtomicExpressions(rule);
                if (areAllMappingsExist(mapping, staticAtomicExpressionsList)) {
                    iq = saturateRule(rule, mapping, dbMetadata);
                    try {
                        IntermediateQuery intermediateQuery = iqConverter.convert(iq, dbMetadata, mapping.getExecutorRegistry());
                        mappingMap.put(iq.getProjectionAtom().getPredicate(), intermediateQuery);
                        System.out.println(iq.toString());
                    } catch (EmptyQueryException e) {
                        e.printStackTrace();
                    }

                } else {
                    if (!queue.isEmpty()){
                        //TODO:Override compareTo for rule.getHead()
                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
                            queue.add(rule);
                    }
                }
            }
        }
        return specificationFactory.createMapping(mapping.getMetadata(), ImmutableMap.copyOf(mappingMap), mapping.getExecutorRegistry());
    }

    private IQ saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata){

        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
        Stack<DatalogMTLExpression> teStack = new Stack<>();
        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
        Stack<IQTree> iqTreeStack = new Stack<>();

        if(!teStack.empty()) {
            ImmutableMap<Variable, Term> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping);
            AtomicExpression atomicExpression;

            if (rule.getHead() instanceof StaticAtomicExpression) {
                atomicExpression = new StaticAtomicExpressionImpl(rule.getHead().getPredicate(), varMap.values().asList());


                TargetAtom targetAtom = datalogConversionTools
                        .convertFromDatalogDataAtom(termFactory.getFunction(atomicExpression.getPredicate(), ((List<Term>) atomicExpression.getTerms())));
                DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
                ConstructionNode constructionNode = IQFactory.createConstructionNode(projectionAtom.getVariables(),
                        targetAtom.getSubstitution(), Optional.empty());

                IQTree newTree;
                while (!teStack.isEmpty()) {
                    DatalogMTLExpression currentExpression = teStack.pop();

                    if (currentExpression instanceof StaticExpression || currentExpression instanceof ComparisonExpression) {
                        //TODO: Coalesce Node is missing, implement it.
                        if (currentExpression instanceof AtomicExpression) {

                            IntermediateQuery intermediateQuery;
                            if (currentExpression instanceof ComparisonExpression) {
                                continue;
                            } else { //StaticAtomicExpression
                                intermediateQuery = mapping.getDefinition(((StaticAtomicExpression) currentExpression).getPredicate()).get();
                                newTree = ((UnaryIQTree) iqConverter.convert(intermediateQuery).getTree()).getChild();
                            }
                        } else if (currentExpression instanceof StaticJoinExpression) {
                            List<IQTree> iqtList = new ArrayList<>();
                            for (int i = 0; i < ((StaticJoinExpression) currentExpression).getArity(); i++) {
                                IQTree iqTree = iqTreeStack.pop();
                                if (!iqtList.contains(iqTree))
                                    iqtList.add(iqTree);
//                                boolean flag = false;
//                                for(IQTree tree : iqtList){
//                                    if(!isContainedInTheTree(iqTree, tree))
//                                        flag = false;
//                                    else {flag = true; break;}
//                                }
//                                if (flag == true) {
//                                    //clone it
//                                }else iqtList.add(iqTree);

                            }
                            InnerJoinNode innerJoinNode = IQFactory.createInnerJoinNode();
                            newTree = IQFactory.createNaryIQTree(innerJoinNode, ImmutableList.copyOf(iqtList));

                        } else { //FilterExpression)
                            FilterNode filterNode = IQFactory
                                    .createFilterNode(comparisonExpToFilterCondition(((FilterExpression) currentExpression).getComparisonExpression()));
                            IQTree iqTree = iqTreeStack.pop();
                            newTree = IQFactory.createUnaryIQTree(filterNode, iqTree);
                        }

                        if (newTree != null)
                            iqTreeStack.push(newTree);
                    } else {
                        iqTreeStack.empty();
                        break;
                    }
                }
                if (!iqTreeStack.isEmpty())
                    return IQFactory.createIQ(projectionAtom, IQFactory.createUnaryIQTree(constructionNode, iqTreeStack.pop()));
            }
        }else{
            //TODO:????
        }
        return null;
    }

    private boolean isContainedInTheTree(IQTree newTree, IQTree iqTree){

        boolean flag = false;
        if (!newTree.equals(iqTree)){
            for (IQTree subTree : iqTree.getChildren()){
                flag = flag || isContainedInTheTree(newTree, subTree);
            }
        }else return true;

        return false || flag;
    }

    private ImmutableList<StaticAtomicExpression> getAtomicExpressions(DatalogMTLRule rule) {

        if (TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                .allMatch(dMTLExp -> dMTLExp instanceof StaticExpression)) {
            return TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                    .filter(dMTLexp -> dMTLexp instanceof StaticAtomicExpression)
                    .map(dMTLexp -> (StaticAtomicExpression) dMTLexp)
                    .collect(ImmutableCollectors.toList());
        }
        return null;
    }


    private boolean areAllMappingsExist(Mapping mapping, ImmutableList<StaticAtomicExpression> atomicExpressionsList){

        if (atomicExpressionsList.stream().filter(ae-> !(ae instanceof ComparisonExpression))
                .allMatch(ae -> mapping.getDefinition(ae.getPredicate()).isPresent()))
            return true;

        return false;
    }

    private ImmutableMap<Variable, Term> retrieveMapForVariablesOccuringInTheHead(DatalogMTLRule rule, Mapping mapping){
        Map<Variable, Term> varMap = new HashMap<>();
        ImmutableList<StaticAtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
        for(Term term : rule.getHead().getImmutableTerms()){
            if(term instanceof Variable){
                for(AtomicExpression ae :atomicExpressionsList){
                    int varIdxInBody = 0;
                    for(Term t : ae.getImmutableTerms()){
                        if (t instanceof Variable) {
                            //TODO:Override compareTo for Variable
                            if(((Variable) t).equals(term)){
                                if(mapping.getPredicates().contains(ae.getPredicate())){
                                    int varIdxInSub = 0;
                                    Optional<IntermediateQuery> iq = mapping.getDefinition(ae.getPredicate());
                                    for(ImmutableTerm subTerm : ((ConstructionNode)iq.get().getRootNode()).getSubstitution().getImmutableMap().values()){
                                        if(varIdxInBody == varIdxInSub){
                                            if(varMap.containsKey((Variable) t)){
                                                if (!varMap.get(t).equals(subTerm)){
                                                    //TODO:throw exception
                                                }
                                            }
                                            else {
                                                varMap.put((Variable) t, (NonGroundFunctionalTerm) subTerm);
                                            }
                                        }
                                        varIdxInSub++;
                                    }
                                } else{
                                    //TODO:throw exception;
                                }
                            }
                        }
                        varIdxInBody++;
                    }
                }
            }
        }
        return ImmutableMap.copyOf(varMap);
    }

    private ImmutableExpression comparisonExpToFilterCondition(ComparisonExpression comparisonExpression){
        String operator = comparisonExpression.getPredicate().getName();
        ExpressionOperation expressionOperation = null;
        if(operator == ExpressionOperation.LT.getName())
            expressionOperation = ExpressionOperation.LT;
        else if(operator == ExpressionOperation.GT.getName())
            expressionOperation = ExpressionOperation.GT;
        else if(operator == ExpressionOperation.EQ.getName())
            expressionOperation = ExpressionOperation.EQ;
        else if(operator == ExpressionOperation.NEQ.getName())
            expressionOperation = ExpressionOperation.NEQ;

        return termFactory.getImmutableExpression(expressionOperation,comparisonExpression.getLeftOperand(), comparisonExpression.getRightOperand());
    }

}
