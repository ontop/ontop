package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.impl.DatalogConversionTools;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.OntopModelSingletons;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.QuadrupleDefinition;
import it.unibz.inf.ontop.spec.mapping.QuadrupleElements;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.temporal.model.impl.StaticAtomicExpressionImpl;
import it.unibz.inf.ontop.temporal.model.impl.TemporalAtomicExpressionImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

import static it.unibz.inf.ontop.model.OntopModelSingletons.*;

@Singleton
public class TemporalMappingSaturatorImpl implements TemporalMappingSaturator{

    TemporalIntermediateQueryFactory TIQFactory;

    @Inject
    private TemporalMappingSaturatorImpl(TemporalIntermediateQueryFactory TIQFactory) {
        this.TIQFactory = TIQFactory;
    }

    @Override
    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TBoxReasoner saturatedTBox) {
        return null;
    }

    public Mapping saturate(Mapping mapping, DBMetadata dbMetadata, TemporalMapping temporalMapping, DBMetadata temporalDBMetadata, DatalogMTLProgram datalogMTLProgram){


        //List<TargetQueryParser> parsers = OntopNativeTemporalMappingParser.createParsers(datalogMTLProgram.getPrefixes());

        TemporalIntermediateQueryBuilder TIQBuilder = TIQFactory.createTemporalIQBuilder(temporalDBMetadata, temporalMapping.getExecutorRegistry());

        Queue<DatalogMTLRule> queue = new LinkedList<>();

        queue.addAll(datalogMTLProgram.getRules());

        while(!queue.isEmpty()){
            DatalogMTLRule rule = queue.poll();
            ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
            if (areAllMappingsExist(mapping,temporalMapping,atomicExpressionsList)){
                saturateRule(rule, mapping, dbMetadata, temporalMapping, temporalDBMetadata, TIQBuilder);
            }else if(!queue.isEmpty()){
                //TODO:Override compareTo for rule.getHead()
                if (queue.stream().anyMatch(qe-> qe.getHead().equals(rule.getHead()))){
                    queue.add(rule);
                }
            }else{
                //TODO: ignore "rule"
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

    private void saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata, TemporalMapping temporalMapping, DBMetadata temporalDBMetadata, TemporalIntermediateQueryBuilder TIQBuilder){
        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
        Stack<DatalogMTLExpression> teStack = new Stack<>();
        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
        Stack<IntermediateQuery> mappingStack = new Stack<>();

        if(!teStack.empty()) {
            ImmutableMap<Variable, Term> varMap = retrieveMapForVariablesOccuringInTheHead(rule, mapping, temporalMapping);
            AtomicExpression atomicExpression;

            if (rule.getHead() instanceof TemporalAtomicExpression) {
                atomicExpression = new TemporalAtomicExpressionImpl(rule.getHead().getPredicate(), varMap.values().asList());
            } else {
                atomicExpression = new StaticAtomicExpressionImpl(rule.getHead().getPredicate(), varMap.values().asList());
            }

            TargetAtom targetAtom = DatalogMTLConversionTools.convertFromDatalogDataAtom(atomicExpression);
            DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();
            ConstructionNode constructionNode = TIQFactory.createConstructionNode(projectionAtom.getVariables(),
                    targetAtom.getSubstitution(), Optional.empty());
            TIQBuilder.init(projectionAtom, constructionNode);

            QueryNode currParentNode = constructionNode;

            while (!teStack.isEmpty()) {
                DatalogMTLExpression currentExpression = teStack.pop();
                if (currentExpression instanceof StaticAtomicExpression) {

                }

            }
        }else{
            //TODO:????
        }

    }

    private ImmutableMap<Variable, Term> retrieveMapForVariablesOccuringInTheHead(DatalogMTLRule rule, Mapping mapping, TemporalMapping temporalMapping){
        Map <Variable, Term> varMap = new HashMap<>();
        ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
        rule.getHead().getTerms().forEach(term ->{
            if(term instanceof Variable){
                for(AtomicExpression ae :atomicExpressionsList){
                    int varIdxInBody = 0;
                    for(Term t : ae.getTerms()){
                        if (t instanceof Variable) {
                            //TODO:Override compareTo for Variable
                            if(((Variable) t).equals(term)){
                                if(mapping.getPredicates().contains(ae.getPredicate())){
                                    int varIdxInSub = 0;
                                    Optional<IntermediateQuery> iq = mapping.getDefinition(ae.getPredicate());
                                        for(ImmutableTerm subTerm : iq.get().getRootConstructionNode().getSubstitution().getImmutableMap().values()){
                                            if(varIdxInBody == varIdxInSub){
                                                varMap.put((Variable) t,(NonGroundFunctionalTerm) subTerm);
                                            }
                                           varIdxInSub++;
                                    }
                                } else if (temporalMapping.getPredicates().contains(ae.getPredicate())) {
                                    int varIdxInSub = 0;
                                    QuadrupleDefinition qd = temporalMapping.getDefinitions().get(ae.getPredicate());
                                    for(ImmutableTerm subTerm : qd.getQuadruple().getIntermediateQuery().getRootConstructionNode().getSubstitution().getImmutableMap().values()){
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
        });
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
