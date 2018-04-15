package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeTraverser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MissingTemporalIntermediateQueryNodeException;
import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.injection.TemporalSpecificationFactory;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.optimizer.impl.PushUpBooleanExpressionOptimizerImpl;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.reformulation.RuleUnfolder;
import it.unibz.inf.ontop.spec.mapping.*;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;
import it.unibz.inf.ontop.spec.mapping.transformer.DatalogMTLToIntermediateQueryConverter;
import it.unibz.inf.ontop.spec.mapping.transformer.RedundantTemporalCoalesceEliminator;
import it.unibz.inf.ontop.spec.mapping.transformer.TemporalMappingSaturator;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.mapping.impl.TemporalMappingIntervalImpl;
import it.unibz.inf.ontop.temporal.model.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TemporalMappingSaturatorImpl implements TemporalMappingSaturator {

    private final DatalogMTLToIntermediateQueryConverter dMTLConverter;
    private final RuleUnfolder ruleUnfolder;
    private final ImmutabilityTools immutabilityTools;
    private final JoinLikeOptimizer joinLikeOptimizer;
    private final TemporalIntermediateQueryFactory TIQFactory;
    private PushUpBooleanExpressionOptimizer pushUpBooleanExpressionOptimizer;
    private ProjectionShrinkingOptimizer projectionShrinkingOptimizer;
    private final PullOutVariableOptimizer pullOutVariableOptimizer;
    private final BindingLiftOptimizer bindingLiftOptimizer;
    private final TemporalSpecificationFactory specificationFactory;
    private final TermFactory termFactory;
    private final RedundantTemporalCoalesceEliminator tcEliminator;

    private static final Logger log = LoggerFactory.getLogger(TemporalMappingSaturatorImpl.class);

    @Inject
    private TemporalMappingSaturatorImpl(DatalogMTLToIntermediateQueryConverter dMTLConverter,
                                         RuleUnfolder ruleUnfolder,
                                         ImmutabilityTools immutabilityTools,
                                         JoinLikeOptimizer joinLikeOptimizer,
                                         TemporalIntermediateQueryFactory tiqFactory,
                                         PullOutVariableOptimizer pullOutVariableOptimizer, BindingLiftOptimizer bindingLiftOptimizer,
                                         TemporalSpecificationFactory specificationFactory,
                                         TermFactory termFactory,
                                         RedundantTemporalCoalesceEliminator tcEliminator) {
        this.dMTLConverter = dMTLConverter;
        this.ruleUnfolder = ruleUnfolder;
        this.immutabilityTools = immutabilityTools;
        TIQFactory = tiqFactory;
        this.pullOutVariableOptimizer = pullOutVariableOptimizer;
        this.bindingLiftOptimizer = bindingLiftOptimizer;
        this.specificationFactory = specificationFactory;
        this.termFactory = termFactory;
        this.tcEliminator = tcEliminator;
        this.pushUpBooleanExpressionOptimizer = new PushUpBooleanExpressionOptimizerImpl(false, this.immutabilityTools);
        projectionShrinkingOptimizer = new ProjectionShrinkingOptimizer();
        this.joinLikeOptimizer = joinLikeOptimizer;
    }

    @Override
    public TemporalMapping saturate(Mapping mapping,
                                    TemporalMapping temporalMapping, DBMetadata temporalDBMetadata,
                                    DatalogMTLProgram datalogMTLProgram) {

        Queue<DatalogMTLRule> queue = new LinkedList<>();

        queue.addAll(datalogMTLProgram.getRules());
        Map <AtomPredicate, IntermediateQuery> mergedMap = mergeMappings(mapping,temporalMapping);

        while (!queue.isEmpty()) {
            DatalogMTLRule rule = queue.poll();
            if (!(rule.getBody() instanceof StaticJoinExpression) ||
                    ((rule.getBody() instanceof FilterExpression) &&
                            !(((FilterExpression) rule.getBody()).getExpression() instanceof StaticJoinExpression))) {
                IntermediateQuery intermediateQuery = dMTLConverter.dMTLToIntermediateQuery(rule,
                        temporalDBMetadata,temporalMapping.getExecutorRegistry());
                System.out.println(intermediateQuery.toString());

                ImmutableList<AtomicExpression> atomicExpressionsList = getAtomicExpressions(rule);
                if (areAllMappingsExist(mergedMap, atomicExpressionsList)) {
                    try {
                        IntermediateQuery iq = ruleUnfolder.unfold(intermediateQuery, ImmutableMap.copyOf(mergedMap));
                        log.debug("Unfolded temporal rule : \n" + iq.toString());
                        iq = bindingLiftOptimizer.optimize(iq);
                        log.debug("Binding lift optimizer (temporal rule) : \n" + iq.toString());
                        //iq = tcEliminator.removeRedundantTemporalCoalesces(iq,temporalDBMetadata,temporalMapping.getExecutorRegistry());
                        //log.debug("Remove redundant coalesces (temporal rule) : \n" + iq.toString());
                        mergedMap.put(iq.getProjectionAtom().getPredicate(), iq);
                    } catch (EmptyQueryException /*| MissingTemporalIntermediateQueryNodeException */ e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!queue.isEmpty()) {
                        //TODO:Override compareTo for rule.getHead()
                        if (queue.stream().anyMatch(qe -> qe.getHead().equals(rule.getHead())))
                            queue.add(rule);
                    }
                }
            }
        }
        return specificationFactory.createTemporalMapping(temporalMapping.getMetadata(), getOnlyTemporalMappings(mergedMap, mapping, temporalMapping), temporalMapping.getExecutorRegistry());
    }

    private ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> getOnlyTemporalMappings(Map<AtomPredicate, IntermediateQuery> mergedmap,
                                                                                     Mapping staticMapping, TemporalMapping temporalMapping){
        Map<AtomPredicate, IntervalAndIntermediateQuery> map = new HashMap<>();
        temporalMapping.getDefinitions().keySet().forEach(k -> map.put(k,temporalMapping.getDefinitions().get(k)));


        for(AtomPredicate predicate : mergedmap.keySet()){
            if (staticMapping.getPredicates().stream().noneMatch(p -> p.equals(predicate))){
                map.putIfAbsent(predicate, getIntvAndIQ(mergedmap.get(predicate), predicate));
            }
        }
        return ImmutableMap.copyOf(map);
    }

    private IntervalAndIntermediateQuery getIntvAndIQ(IntermediateQuery iq, AtomPredicate predicate){
        TemporalMappingInterval tmi = new TemporalMappingIntervalImpl(
                (termFactory.getVariable(predicate.getName() + "_beginInc")),
                (termFactory.getVariable(predicate.getName() + "_endInc")),
                termFactory.getVariable(predicate.getName() + "_begin"),
                termFactory.getVariable(predicate.getName() + "_end"));

        return new IntervalAndIntermediateQuery(tmi, iq);
    }

    private Map<AtomPredicate, IntermediateQuery> mergeMappings(Mapping mapping, TemporalMapping temporalMapping){
        Map <AtomPredicate, IntermediateQuery> mergedMap = new HashMap<>();
        mergedMap.putAll(mapping.getPredicates().stream()
                .collect(Collectors.toMap(p-> p, p-> mapping.getDefinition(p).get())));
        mergedMap.putAll(temporalMapping.getPredicates().stream()
                .collect(Collectors.toMap(p-> p, p -> temporalMapping.getDefinition(p).get())));
        return mergedMap;
    }

    private boolean areAllMappingsExist(Map<AtomPredicate, IntermediateQuery> mergedMap,
                                        ImmutableList<AtomicExpression> atomicExpressionsList) {

        if (atomicExpressionsList.stream().filter(ae -> !(ae instanceof ComparisonExpression))
                .allMatch(ae -> mergedMap.containsKey(ae.getPredicate())))
            return true;

        return false;
    }

    private ImmutableList<AtomicExpression> getAtomicExpressions(DatalogMTLRule rule) {
        return TreeTraverser.using(DatalogMTLExpression::getChildNodes).postOrderTraversal(rule.getBody()).stream()
                .filter(dMTLexp -> dMTLexp instanceof AtomicExpression)
                .map(dMTLexp -> (AtomicExpression) dMTLexp)
                .collect(ImmutableCollectors.toList());
    }
}
