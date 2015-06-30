package org.semanticweb.ontop.pivotalrepr.datalog;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.NeutralSubstitution;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.impl.*;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.semanticweb.ontop.pivotalrepr.datalog.DatalogConversionTools.convertFromDatalogDataAtom;
import static org.semanticweb.ontop.pivotalrepr.datalog.DatalogConversionTools.createDataNode;
import static org.semanticweb.ontop.pivotalrepr.datalog.DatalogRule2QueryConverter.convertDatalogRule;

/**
 * Converts a datalog program into an intermediate query
 */
public class DatalogProgram2QueryConverter {

    /**
     * TODO: explain
     */
    public static class NotSupportedConversionException extends RuntimeException {
        public NotSupportedConversionException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     */
    public static class InvalidDatalogProgramException extends Exception {
        public InvalidDatalogProgramException(String message) {
            super(message);
        }
    }

    /**
     * TODO: explain
     *
     */
    public static IntermediateQuery convertDatalogProgram(DatalogProgram queryProgram,
                                                          Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {
        List<CQIE> rules = queryProgram.getRules();

        DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
        List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

        if (topDownPredicates.size() == 0) {
            throw new InvalidDatalogProgramException("Datalog program without any rule!");
        }

        Predicate rootPredicate = topDownPredicates.get(0);
        if (tablePredicates.contains(rootPredicate))
            throw new InvalidDatalogProgramException("The root predicate must not be a table predicate");

        Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

        IntermediateQuery intermediateQuery = createTopIntermediateQuery(
                extractRootHeadAtom(rootPredicate, ruleIndex),
                queryProgram.getQueryModifiers());

        /**
         * Rules (sub-queries)
         */
        for (Predicate datalogAtomPredicate : topDownPredicates) {
            Optional<IntermediateQuery> optionalSubQuery = convertDatalogDefinitions(datalogAtomPredicate, ruleIndex,
                    tablePredicates);
            if (optionalSubQuery.isPresent()) {
                try {
                    intermediateQuery.mergeSubQuery(optionalSubQuery.get());
                } catch (QueryMergingException e) {
                    throw new InvalidDatalogProgramException(e.getMessage());
                }
            }

        }

        return intermediateQuery;

    }

    /**
     * TODO: clean it !!!
     */
    private static Function extractRootHeadAtom(Predicate rootDatalogPredicate, Multimap<Predicate, CQIE> ruleIndex) {
        return ruleIndex.get(rootDatalogPredicate).iterator().next().getHead();
    }


    /**
     * TODO: explain and comment
     */
    private static Optional<IntermediateQuery> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                                            Multimap<Predicate, CQIE> datalogRuleIndex,
                                                            Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {
        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        List<IntermediateQuery> convertedDefinitions = new ArrayList<>();
        for (CQIE datalogAtomDefinition : atomDefinitions) {
            convertedDefinitions.add(
                    convertDatalogRule(datalogAtomDefinition, tablePredicates));
        }

        try {
            return IntermediateQueryUtils.mergeDefinitions(convertedDefinitions);
        } catch (QueryMergingException e) {
            throw new InvalidDatalogProgramException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: explain
     * TODO: find a better name
     */
    private static IntermediateQuery createTopIntermediateQuery(Function rootDatalogAtom,
                                                                OBDAQueryModifiers queryModifiers)
            throws InvalidDatalogProgramException {
        try {
            IntermediateQueryBuilder builder = new JgraphtIntermediateQueryBuilder();

            DataAtom dataAtom = convertFromDatalogDataAtom(rootDatalogAtom)._1();
            /**
             * Empty substitution for the top construction node (stay abstract).
             */
            ImmutableSubstitution<ImmutableTerm> substitution = new NeutralSubstitution();

            ConstructionNode rootNode;
            if (queryModifiers.hasModifiers()) {
                // TODO: explain
                ImmutableQueryModifiers immutableQueryModifiers = new ImmutableQueryModifiersImpl(queryModifiers);
                rootNode = new ConstructionNodeImpl(dataAtom, substitution, immutableQueryModifiers);
            } else {
                rootNode = new ConstructionNodeImpl(dataAtom, substitution);
            }

            DataNode dataNode = createDataNode(dataAtom, ImmutableList.<Predicate>of());

            builder.init(rootNode);
            builder.addChild(rootNode, dataNode);

            return builder.build();
        }
        catch (IntermediateQueryBuilderException e) {
            throw new InvalidDatalogProgramException(e.getMessage());
        }
    }
}
