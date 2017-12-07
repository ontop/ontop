package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.datalog.Datalog2QueryMappingConverter;
import it.unibz.inf.ontop.datalog.Mapping2DatalogConverter;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.mapping.TargetAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.impl.MappingImpl;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.datalog.impl.DatalogMTLConversionTools;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.mapping.OntopNativeTemporalMappingParser;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;

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

        Map<String, IntermediateQuery> saturatedMappingMap = new HashMap<>();
        temporalMapping.getQueries().forEach(q-> saturatedMappingMap.put( q.getProjectionAtom().getFunctionSymbol().getName(),q));
        mapping.getQueries().forEach(q-> saturatedMappingMap.put( q.getProjectionAtom().getFunctionSymbol().getName(),q));

        Queue<DatalogMTLRule> queue = new LinkedList<>();

        queue.addAll(datalogMTLProgram.getRules());

        while(!queue.isEmpty()){
            DatalogMTLRule rule = queue.poll();
            ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
            atomicExpressionsList.stream()
                    .filter(ae-> !(ae.getPredicate().getName().equals(ExpressionOperation.LT.getName())) && !(ae.getPredicate().getName().equals(ExpressionOperation.GT.getName())))
                    .forEach(ae-> System.out.println(ae.getPredicate().getName() + " "+ saturatedMappingMap.get(ae.getPredicate().getName())));
//            if (atomicExpressionsList.stream()
//                    //TODO: replace this filter condition with a reasonable one
//                    .filter(ae-> !(ae.getPredicate().getName().equals(ExpressionOperation.LT.getName())) && !(ae.getPredicate().getName().equals(ExpressionOperation.GT.getName())))
//                    .allMatch(ae -> saturatedMappingMap.containsKey(ae.getPredicate().getName())))
//                saturateRule(rule, mapping, dbMetadata, temporalMapping, temporalDBMetadata, TIQBuilder);
//            else queue.add(rule);
        }










//        for (DatalogMTLRule rule : datalogMTLProgram.getRules()) {
//           Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
//           Stack<DatalogMTLExpression> teStack = new Stack<DatalogMTLExpression>();
//           Stack<IntermediateQuery> mappingStack = new Stack<IntermediateQuery>();
//
//            //TODO: merge dbMetadata and temporalDBMetadata
//            //TemporalIntermediateQueryBuilder TIQBuilder = (TemporalIntermediateQueryBuilder) TIQFactory.createIQBuilder(temporalDBMetadata, temporalMapping.getExecutorRegistry());
//
//            TargetAtom ta = DatalogMTLConversionTools.convertFromDatalogDataAtom(rule.getHead());
//            //TIQBuilder.init(((TemporalAtomicExpression)rule.getHead()).getPredicate(), TIQFactory.createConstructionNode());
//           it.forEach(te-> teStack.push(te));

//           while(!teStack.isEmpty()){
//               saturateRule();
//           }

//            it.forEach(te -> System.out.println(te.render()));
//            System.out.println("---");
//        }

        return null;
    }

    private void saturateRule(DatalogMTLRule rule, Mapping mapping, DBMetadata dbMetadata, Mapping temporalMapping, DBMetadata temporalDBMetadata, TemporalIntermediateQueryBuilder TIQBuilder){
        TreeTraverser treeTraverser = TreeTraverser.using(DatalogMTLExpression::getChildNodes);
        Iterable<DatalogMTLExpression> it = treeTraverser.preOrderTraversal(rule.getBody());
        Stack<DatalogMTLExpression> teStack = new Stack<>();
        it.iterator().forEachRemaining(dMTLexp -> teStack.push(dMTLexp));
        Stack<IntermediateQuery> mappingStack = new Stack<>();

        while(!teStack.isEmpty()){
            DatalogMTLExpression currentExpression = teStack.pop();

            if(currentExpression instanceof StaticAtomicExpression){
                TargetAtom ta = DatalogMTLConversionTools.convertFromDatalogDataAtom(rule.getHead());
                TIQBuilder.init((DistinctVariableOnlyDataAtom) (rule.getHead()).getPredicate(), TIQFactory.createConstructionNode(((List<Variable>)rule.getHead().getTerms()).stream().collect(ImmutableCollectors.toSet())));

            }

        }

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
