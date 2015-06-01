package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO: describe
 */
public class DatalogIntermediateQueryConverter {

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
     */
    public static IntermediateQuery convertFromDatalog(DatalogProgram queryProgram) throws InvalidDatalogProgramException {
        try {
            List<CQIE> rules = queryProgram.getRules();

            DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
            List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

            if (topDownPredicates.size() == 0) {
                throw new InvalidDatalogProgramException("Datalog program without any rule!");
            }

            Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

            //Not final mutable object
            IntermediateQueryBuilder builder = initIntermediateQuery(
                    extractRootHeadAtom(topDownPredicates.get(0), ruleIndex),
                    queryProgram.getQueryModifiers());

            /**
             * Rules
             */
            for (int i = 0; i < topDownPredicates.size(); i++) {
                Predicate datalogAtomPredicate = topDownPredicates.get(i);
                Optional<Rule> optionalRule = convertDatalogDefinitions(datalogAtomPredicate, ruleIndex);
                if (optionalRule.isPresent()) {
                    builder.mergeRule(optionalRule.get());
                }

            }

            return builder.build();

        } catch (IntermediateQueryBuilderException e) {
            throw new InvalidDatalogProgramException(e.getMessage());
        }
    }

    /**
     * TODO: clean it !!!
     */
    private static Function extractRootHeadAtom(Predicate rootDatalogPredicate, Multimap<Predicate, CQIE> ruleIndex) {
        return ruleIndex.get(rootDatalogPredicate).iterator().next().getHead();
    }

    /**
     * TODO: implement and describe
     */
    public static DatalogProgram convertToDatalog(IntermediateQuery intermediateQuery) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * TODO: explain
     */
    private static IntermediateQueryBuilder initIntermediateQuery(Function rootDatalogAtom,
                                                                  OBDAQueryModifiers queryModifiers)
            throws IntermediateQueryBuilderException {

        IntermediateQueryBuilder builder = new IntermediateQueryBuilderImpl();
        DataNode topDataNode = createDataNode(convertFromDatalogDataAtom(rootDatalogAtom));

        if (queryModifiers.hasModifiers()) {
            throw new RuntimeException("Modifiers not yet supported. TODO: implement it !");
        }
        else {
            builder.init(topDataNode);
        }

        return builder;
    }


    /**
     * TODO: explain and comment
     */
    private static Optional<Rule> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                                            Multimap<Predicate, CQIE> datalogRuleIndex) {
        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        AtomPredicate atomPredicate = new AtomPredicateImpl(datalogAtomPredicate.getName(),
                datalogAtomPredicate.getArity(), true);

        List<Rule> convertedRules = new ArrayList<>();
        for (CQIE datalogAtomDefinition : atomDefinitions) {
            convertedRules.add(convertDatalogRule(atomPredicate, datalogAtomDefinition));
        }

        return RuleUtils.mergeDefinitions(convertedRules);
    }

    private static Rule convertDatalogRule(AtomPredicate atomPredicate, CQIE datalogRule) {
        // TODO: implement it
        return null;
    }

    private static DataAtom convertFromDatalogDataAtom(Function datalogAtom) {
        // TODO: implement it
        return null;
    }

    /**
     * TODO: move it
     *
     */
    private static DataNode createDataNode(DataAtom dataAtom) {
        // TODO: implement it
        return null;
    }
}
