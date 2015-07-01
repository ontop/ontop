package org.semanticweb.ontop.pivotalrepr.datalog;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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

    private static final Optional<ImmutableQueryModifiers> NO_QUERY_MODIFIER = Optional.absent();

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

        Optional<ImmutableQueryModifiers> topQueryModifiers = convertModifiers(queryProgram.getQueryModifiers());

        /**
         * TODO: explain
         */
        IntermediateQuery intermediateQuery = convertDatalogDefinitions(rootPredicate, ruleIndex, tablePredicates,
                topQueryModifiers).get();

        /**
         * Rules (sub-queries)
         */
        for (int i=1; i < topDownPredicates.size() ; i++) {
            Predicate datalogAtomPredicate  = topDownPredicates.get(i);
            Optional<IntermediateQuery> optionalSubQuery = convertDatalogDefinitions(datalogAtomPredicate, ruleIndex,
                    tablePredicates, NO_QUERY_MODIFIER);
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
     * TODO: explain and comment
     */
    private static Optional<IntermediateQuery> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                                                         Multimap<Predicate, CQIE> datalogRuleIndex,
                                                                         Collection<Predicate> tablePredicates,
                                                                         Optional<ImmutableQueryModifiers> optionalModifiers)
            throws InvalidDatalogProgramException {
        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        List<IntermediateQuery> convertedDefinitions = new ArrayList<>();
        for (CQIE datalogAtomDefinition : atomDefinitions) {
            convertedDefinitions.add(
                    convertDatalogRule(datalogAtomDefinition, tablePredicates, optionalModifiers));
        }

        try {
            return IntermediateQueryUtils.mergeDefinitions(convertedDefinitions);
        } catch (QueryMergingException e) {
            throw new InvalidDatalogProgramException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> convertModifiers(OBDAQueryModifiers queryModifiers) {
        if (queryModifiers.hasModifiers()) {
            ImmutableQueryModifiers immutableQueryModifiers = new ImmutableQueryModifiersImpl(queryModifiers);
            return Optional.of(immutableQueryModifiers);
        } else {
            return Optional.absent();
        }
    }
}
