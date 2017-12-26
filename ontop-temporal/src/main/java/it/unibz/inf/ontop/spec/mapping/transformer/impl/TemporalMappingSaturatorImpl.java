package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.TargetAtom;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.QuadrupleDefinition;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.TemporalCoalesceNode;
import it.unibz.inf.ontop.temporal.iq.node.TemporalJoinNode;
import it.unibz.inf.ontop.temporal.mapping.IntervalQueryParser;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.StaticAtomicExpressionImpl;
import it.unibz.inf.ontop.temporal.model.impl.TemporalAtomicExpressionImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;
import org.apache.jena.base.Sys;

import java.util.*;

@Singleton
public class TemporalMappingSaturatorImpl implements TemporalMappingSaturator{

    private  final TemporalIntermediateQueryFactory TIQFactory;
    private final TermFactory termFactory;
    private final DatalogMTLConversionTools datalogMTLConversionTools;
    private final IQConverter iqConverter;

    @Inject
    private TemporalMappingSaturatorImpl(TemporalIntermediateQueryFactory TIQFactory, TermFactory termFactory,
                                         DatalogMTLConversionTools datalogMTLConversionTools, IQConverter iqConverter) {
        this.TIQFactory = TIQFactory;
        this.termFactory = termFactory;
        this.datalogMTLConversionTools = datalogMTLConversionTools;
        this.iqConverter = iqConverter;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {
        return null;
    }

    public TemporalMapping saturate(Mapping mapping, DBMetadata dbMetadata,
                            TemporalMapping temporalMapping, DBMetadata temporalDBMetadata,
                            DatalogMTLProgram datalogMTLProgram){

        //List<TargetQueryParser> parsers = OntopNativeTemporalMappingParser.createParsers(datalogMTLProgram.getPrefixes());

        Queue<DatalogMTLRule> queue = new LinkedList<>();

        queue.addAll(datalogMTLProgram.getRules());

        IQ iq;
        while(!queue.isEmpty()){
            DatalogMTLRule rule = queue.poll();
            if (!(rule.getBody() instanceof StaticJoinExpression) ||
                    ((rule.getBody() instanceof FilterExpression) &&
                            !(((FilterExpression) rule.getBody()).getExpression() instanceof StaticJoinExpression))) {
                ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
                if (areAllMappingsExist(mapping, temporalMapping, atomicExpressionsList)) {
                    iq = saturateRule(rule, mapping, dbMetadata, temporalMapping, temporalDBMetadata);
                    System.out.println(iq.toString());
                } else {
                    if (!queue.isEmpty()){
                        //TODO:Override compareTo for rule.getHead()
                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
                            queue.add(rule);
                    }
                }
            }
        }
        return null;
    }

    private boolean areAllMappingsExist(Mapping mapping, TemporalMapping temporalMapping,
                                        ImmutableList<AtomicExpression> atomicExpressionsList){

        if (atomicExpressionsList.stream().filter(ae-> !(ae instanceof ComparisonExpression))
                .allMatch(ae -> mapping.getDefinition(ae.getPredicate()).isPresent() || temporalMapping.getDefinitions().containsKey(ae.getPredicate())))
            return true;

        return false;
    }

    //expands into temporal mapping in the shape of named graph
    private TemporalMapping expandToTemporalMapping(IQ iq){

        return null;
    }

    private IQ saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata,
                              TemporalMapping temporalMapping, DBMetadata temporalDBMetadata){

        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
        Stack<DatalogMTLExpression> teStack = new Stack<>();
        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
        Stack<IQTree> iqTreeStack = new Stack<>();

        if(!teStack.empty()) {
            ImmutableMap<Variable, Term> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping, temporalMapping);
            AtomicExpression atomicExpression;

            if (rule.getHead() instanceof TemporalAtomicExpression) {
                atomicExpression = new TemporalAtomicExpressionImpl(rule.getHead().getPredicate(), varMap.values().asList());
            } else {
                atomicExpression = new StaticAtomicExpressionImpl(rule.getHead().getPredicate(), varMap.values().asList());
            }

            TargetAtom targetAtom = datalogMTLConversionTools
                    .convertFromDatalogDataAtom(termFactory.getFunction(atomicExpression.getPredicate(), ((List<Term>) atomicExpression.getTerms())));
            DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
            ConstructionNode constructionNode = TIQFactory.createConstructionNode(projectionAtom.getVariables(),
                    targetAtom.getSubstitution(), Optional.empty());

            IQTree newTree = null;
            while (!teStack.isEmpty()) {
                DatalogMTLExpression currentExpression = teStack.pop();

                //TODO: Coalesce Node is missing, implement it.
                if(currentExpression instanceof AtomicExpression) {

                    IntermediateQuery intermediateQuery;
                    IQTree newATree;
                    if (currentExpression instanceof ComparisonExpression){
                        continue;
                    } else if (currentExpression instanceof StaticAtomicExpression) {
                        intermediateQuery = mapping.getDefinition(((StaticAtomicExpression) currentExpression).getPredicate()).get();
                        newTree = ((UnaryIQTree)iqConverter.convert(intermediateQuery).getTree()).getChild();
                    } else {//TemporalAtomicExpression
                        intermediateQuery = temporalMapping.getDefinitions()
                                .get(((TemporalAtomicExpression) currentExpression).getPredicate()).getQuadruple().getIntermediateQuery();
                        newATree = ((UnaryIQTree)iqConverter.convert(intermediateQuery).getTree()).getChild();
                        TemporalCoalesceNode coalesceNode = TIQFactory.createTemporalCoalesceNode();
                        newTree = TIQFactory.createUnaryIQTree(coalesceNode, newATree);
                    }

                }else if(currentExpression instanceof TemporalJoinExpression){
                    IQTree iqTree1 = iqTreeStack.pop();
                    IQTree iqTree2 = iqTreeStack.pop();

                    TemporalJoinNode temporalJoinNode = TIQFactory.createTemporalJoinNode();
                    newTree = TIQFactory.createNaryIQTree(temporalJoinNode, ImmutableList.of(iqTree1, iqTree2));

                }else if(currentExpression instanceof StaticJoinExpression){
                    List<IQTree> iqtList = new ArrayList<>();
                    for(int i = 0; i <((StaticJoinExpression) currentExpression).getArity(); i++){
                        iqtList.add(iqTreeStack.pop());
                    }
                    InnerJoinNode innerJoinNode = TIQFactory.createInnerJoinNode();
                    newTree = TIQFactory.createNaryIQTree(innerJoinNode, ImmutableList.copyOf(iqtList));

                }else if (currentExpression instanceof FilterExpression){
                    FilterNode filterNode = TIQFactory.createFilterNode(comparisonExpToFilterCondition(((FilterExpression) currentExpression).getComparisonExpression()));
                    IQTree iqTree = iqTreeStack.pop();
                    newTree = TIQFactory.createUnaryIQTree(filterNode,iqTree);

                }else if(currentExpression instanceof UnaryTemporalExpression && currentExpression instanceof TemporalExpressionWithRange){

                    UnaryOperatorNode newNode;
                    if(currentExpression instanceof BoxMinusExpression)
                         newNode = TIQFactory.createBoxMinusNode(((BoxMinusExpression) currentExpression).getRange());

                    else if(currentExpression instanceof BoxPlusExpression)
                         newNode = TIQFactory.createBoxPlusNode(((BoxPlusExpression) currentExpression).getRange());

                    else if(currentExpression instanceof DiamondMinusExpression)
                         newNode = TIQFactory.createDiamondMinusNode(((DiamondMinusExpression)currentExpression).getRange());
                    else
                        newNode = TIQFactory.createDiamondPlusNode(((DiamondPlusExpression) currentExpression).getRange());

                    IQTree iqTree = iqTreeStack.pop();
                    newTree = TIQFactory.createUnaryIQTree(newNode, iqTree);

                    //TODO: fill here
                }else if(currentExpression instanceof BinaryTemporalExpression && currentExpression instanceof TemporalExpressionWithRange) {

                    if (currentExpression instanceof SinceExpression){

                    }
                    else{

                    }
                }
                if (newTree != null)
                    iqTreeStack.push(newTree);
            }


            return TIQFactory.createIQ(projectionAtom, TIQFactory.createUnaryIQTree(constructionNode, iqTreeStack.pop()));

        }else{
            //TODO:????
        }
        return null;
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

    private ImmutableMap<Variable, Term> retrieveMapForVariablesOccuringInTheHead(DatalogMTLRule rule, Mapping mapping, TemporalMapping temporalMapping){
        Map <Variable, Term> varMap = new HashMap<>();
        ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
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
                                } else if (temporalMapping.getPredicates().contains(ae.getPredicate())) {
                                    int varIdxInSub = 0;
                                    QuadrupleDefinition qd = temporalMapping.getDefinitions().get(ae.getPredicate());
                                    for(ImmutableTerm subTerm : ((ConstructionNode)qd.getQuadruple()
                                            .getIntermediateQuery().getRootNode()).getSubstitution().getImmutableMap().values()){
                                        if(varIdxInBody == varIdxInSub){
                                            varMap.put((Variable) t,(NonGroundFunctionalTerm) subTerm);
                                        }
                                        varIdxInSub++;
                                    }

                                }else{
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

    //TODO: fill this function
    private DBMetadata mergeStaticDBMetadataintoTemporalDBMetadata(DBMetadata staticDBMetadata, DBMetadata temporaDBMetadata){
        return null;
    }

    private ImmutableList<AtomicExpression> getAtomicExpressions(DatalogMTLRule rule){
        return TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                .filter(dMTLexp -> dMTLexp instanceof AtomicExpression)
                .map(dMTLexp -> (AtomicExpression) dMTLexp)
                .collect(ImmutableCollectors.toList());
    }

}
