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
     *
     */
    public static IntermediateQuery convertFromDatalog(DatalogProgram queryProgram) throws InvalidDatalogProgramException {
        List<CQIE> rules = queryProgram.getRules();

        DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
        List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

        if (topDownPredicates.size() == 0) {
            throw new InvalidDatalogProgramException("Datalog program without any rule!");
        }

        Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

        IntermediateQuery intermediateQuery = initIntermediateQuery(
                extractRootHeadAtom(topDownPredicates.get(0), ruleIndex),
                queryProgram.getQueryModifiers());

        /**
         * Rules
         */
        for (Predicate datalogAtomPredicate : topDownPredicates) {
            Optional<Rule> optionalRule = convertDatalogDefinitions(datalogAtomPredicate, ruleIndex);
            if (optionalRule.isPresent()) {
                try {
                    intermediateQuery.mergeRule(optionalRule.get());
                } catch (RuleMergingException e) {
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
     * TODO: implement and describe
     */
    public static DatalogProgram convertToDatalog(IntermediateQuery intermediateQuery) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * TODO: explain
     * TODO: find a better name
     */
    private static IntermediateQuery initIntermediateQuery(Function rootDatalogAtom,
                                                           OBDAQueryModifiers queryModifiers)
            throws InvalidDatalogProgramException {
        try {
            IntermediateQueryBuilder builder = new IntermediateQueryBuilderImpl();
            DataNode topDataNode = createDataNode(convertFromDatalogDataAtom(rootDatalogAtom, false));

            if (queryModifiers.hasModifiers()) {
                throw new RuntimeException("Modifiers not yet supported. TODO: implement it !");
            }
            else {
                builder.init(topDataNode);
            }

            return builder.build();
        }
        catch (IntermediateQueryBuilderException e) {
            throw new InvalidDatalogProgramException(e.getMessage());
        }
    }


    /**
     * TODO: explain and comment
     */
    private static Optional<Rule> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                                            Multimap<Predicate, CQIE> datalogRuleIndex) {
        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        List<Rule> convertedRules = new ArrayList<>();
        for (CQIE datalogAtomDefinition : atomDefinitions) {
            convertedRules.add(convertDatalogRule(datalogAtomDefinition));
        }

        return RuleUtils.mergeDefinitions(convertedRules);
    }

    private static Rule convertDatalogRule(CQIE datalogRule) {
        // Not extensional because of there is a rule :)
        boolean isExtensional = false;

        DataAtom headAtom = convertFromDatalogDataAtom(datalogRule.getHead(), isExtensional);
        IntermediateQuery body = convertDatalogBody(datalogRule.getBody());

        return new RuleImpl(headAtom, body);
    }

    private static IntermediateQuery convertDatalogBody(List<Function> bodyDatalogAtoms) {
        //TODO: deal with top filter conditions
        

    }

    private static DataAtom convertFromDatalogDataAtom(Function datalogAtom, boolean isExtensional) {
        Predicate datalogAtomPredicate = datalogAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = new AtomPredicateImpl(datalogAtomPredicate, isExtensional);

        return new DataAtomImpl(atomPredicate, datalogAtom.getTerms());
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
