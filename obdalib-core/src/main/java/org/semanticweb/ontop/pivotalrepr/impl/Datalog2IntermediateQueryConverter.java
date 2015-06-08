package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import fj.P;
import fj.P2;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * TODO: describe
 */
public class Datalog2IntermediateQueryConverter {

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
    public static IntermediateQuery convertFromDatalog(DatalogProgram queryProgram, Collection<Predicate> tablePredicates) throws InvalidDatalogProgramException {
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

        IntermediateQuery intermediateQuery = initIntermediateQuery(
                extractRootHeadAtom(rootPredicate, ruleIndex),
                queryProgram.getQueryModifiers());

        /**
         * Rules
         */
        for (Predicate datalogAtomPredicate : topDownPredicates) {
            Optional<Rule> optionalRule = convertDatalogDefinitions(datalogAtomPredicate, ruleIndex, tablePredicates);
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
     * TODO: explain
     * TODO: find a better name
     */
    private static IntermediateQuery initIntermediateQuery(Function rootDatalogAtom,
                                                           OBDAQueryModifiers queryModifiers)
            throws InvalidDatalogProgramException {
        try {
            IntermediateQueryBuilder builder = new IntermediateQueryBuilderImpl();
            DataNode topDataNode = createDataNode(convertFromDatalogDataAtom(rootDatalogAtom),
                    ImmutableList.<Predicate>of());

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
                                                            Multimap<Predicate, CQIE> datalogRuleIndex,
                                                            Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {
        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        List<Rule> convertedRules = new ArrayList<>();
        for (CQIE datalogAtomDefinition : atomDefinitions) {
            convertedRules.add(convertDatalogRule(datalogAtomDefinition, tablePredicates));
        }

        return RuleUtils.mergeDefinitions(convertedRules);
    }

    private static Rule convertDatalogRule(CQIE datalogRule, Collection<Predicate> tablePredicates) throws InvalidDatalogProgramException {
        DataAtom headAtom = convertFromDatalogDataAtom(datalogRule.getHead());
        IntermediateQuery body = convertDatalogBody(datalogRule.getBody(), tablePredicates);

        return new RuleImpl(headAtom, body);
    }

    private static IntermediateQuery convertDatalogBody(List<Function> datalogBodyAtoms,
                                                        Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {

        P2<fj.data.List<Function>, fj.data.List<Function>> atomClassification = classifyAtoms(
                fj.data.List.iterableList(datalogBodyAtoms));
        fj.data.List<Function> dataAndCompositeAtoms = atomClassification._1();
        fj.data.List<Function> booleanAtoms = atomClassification._2();

        return createIntermediateQuery(tablePredicates, dataAndCompositeAtoms, booleanAtoms);
    }

    private static P2<fj.data.List<Function>, fj.data.List<Function>> classifyAtoms(fj.data.List<Function> atoms)
            throws InvalidDatalogProgramException {
        fj.data.List<Function> dataAndCompositeAtoms = DatalogTools.filterDataAndCompositeAtoms(atoms);
        fj.data.List<Function> otherAtoms = DatalogTools.filterNonDataAndCompositeAtoms(atoms);
        fj.data.List<Function> booleanAtoms = DatalogTools.filterBooleanAtoms(otherAtoms);

        if (dataAndCompositeAtoms.isEmpty())
            throw new InvalidDatalogProgramException("No data or composite atom in " + atoms);

        /**
         * May throw a NotSupportedConversionException
         */
        checkNonDataOrCompositeAtomSupport(otherAtoms, booleanAtoms);

        return P.p(dataAndCompositeAtoms, booleanAtoms);
    }



    /**
     * TODO: explain
     */
    private static IntermediateQuery createIntermediateQuery(Collection<Predicate> tablePredicates,
                                                             fj.data.List<Function> dataAndCompositeAtoms,
                                                             fj.data.List<Function> booleanAtoms)
            throws InvalidDatalogProgramException {
            /**
             * TODO: explain
             */
        Optional<QueryNode> optionalRootNode = createOptionalRootNode(dataAndCompositeAtoms, booleanAtoms);

        // Non final
        IntermediateQueryBuilder queryBuilder = new IntermediateQueryBuilderImpl();

        try {
            /**
             * TODO: explain
             */
            if (optionalRootNode.isPresent()) {
                queryBuilder.init(optionalRootNode.get());
            }

            /**
             * TODO: explain
             */
            queryBuilder = convertDataOrCompositeAtoms(dataAndCompositeAtoms, queryBuilder, optionalRootNode,
                        tablePredicates);
            return queryBuilder.build();
        }
        catch (IntermediateQueryBuilderException e) {
            throw new InvalidDatalogProgramException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    private static Optional<QueryNode> createOptionalRootNode(fj.data.List<Function> dataAndCompositeAtoms,
                                                              fj.data.List<Function> booleanAtoms) {
        Optional<BooleanExpression> optionalFilter = createFilterExpression(booleanAtoms);

        int dataAndCompositeAtomCount = dataAndCompositeAtoms.length();
        Optional<QueryNode> optionalRootNode;

        /**
         * Simple filter as a root node
         */
        if (optionalFilter.isPresent() && (dataAndCompositeAtomCount == 1)) {
            optionalRootNode = Optional.of((QueryNode) new SimpleFilterNodeImpl(optionalFilter.get()));
        }
        else if (dataAndCompositeAtomCount > 1) {
            optionalRootNode = Optional.of((QueryNode) new InnerJoinNodeImpl(optionalFilter));
        }
        /**
         * No need to create a special root node (will be the unique data atom)
         */
        else {
            optionalRootNode = Optional.absent();
        }
        return optionalRootNode;
    }


    private static Optional<BooleanExpression> createFilterExpression(fj.data.List<Function> booleanAtoms) {
        if (booleanAtoms.isEmpty())
            return Optional.absent();
        return Optional.of(DatalogTools.foldBooleanConditions(booleanAtoms));
    }

    /**
     * All the other atoms are currently presumed to be boolean atoms
     * TODO: go beyond this restriction to support GROUP
     */
    private static void checkNonDataOrCompositeAtomSupport(fj.data.List<Function> otherAtoms, fj.data.List<Function> booleanAtoms)
        throws NotSupportedConversionException{
        if (booleanAtoms.length() < otherAtoms.length()) {
            HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
            unsupportedAtoms.removeAll(booleanAtoms.toCollection());
            throw new NotSupportedConversionException("Conversion of the following atoms to the intermediate query is not (yet) supported: "
                    + unsupportedAtoms);
        }
    }

    /**
     * TODO: describe
     */
    private static IntermediateQueryBuilder convertDataOrCompositeAtoms(final fj.data.List<Function> atoms,
                                                                        IntermediateQueryBuilder queryBuilder,
                                                                        final Optional<QueryNode> optionalParentNode,
                                                                        Collection<Predicate> tablePredicates)
            throws IntermediateQueryBuilderException, InvalidDatalogProgramException {
        /**
         * For each atom
         */
        for (Function atom : atoms) {

            Optional<fj.data.List<Function>> optionalSubDataOrCompositeAtoms;
            Optional<BooleanExpression> optionalFilterCondition;

            /**
             * If the atom is composite, extracts sub atoms
             */
            if (atom.isAlgebraFunction()) {
                P2<fj.data.List<Function>, fj.data.List<Function>> atomClassification = classifyAtoms(atoms);
                optionalSubDataOrCompositeAtoms = Optional.of(atomClassification._1());
                fj.data.List<Function> booleanSubAtoms = atomClassification._2();

                optionalFilterCondition = createFilterExpression(booleanSubAtoms);
            }
            /**
             * Data atom: nothing to extract
             */
            else {
                optionalSubDataOrCompositeAtoms = Optional.absent();
                optionalFilterCondition = Optional.absent();
            }

            /**
             * Creates the node
             */
            QueryNode currentNode = createQueryNode(atom, tablePredicates, optionalFilterCondition);

            /**
             * If has a parent
             */
            if (optionalParentNode.isPresent()) {
                queryBuilder.addChild(optionalParentNode.get(), currentNode);
            }
            /**
             * Otherwise is supposed to be the root
             */
            else {
                queryBuilder.init(currentNode);
            }

            /**
             * Recursive call for composite atoms
             */
            if (optionalSubDataOrCompositeAtoms.isPresent()) {
                queryBuilder = convertDataOrCompositeAtoms(optionalSubDataOrCompositeAtoms.get(), queryBuilder,
                        Optional.of(currentNode), tablePredicates);
            }
        }

        return queryBuilder;
    }


    private static DataAtom convertFromDatalogDataAtom(Function datalogDataAtom) {
        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = new AtomPredicateImpl(datalogAtomPredicate);

        return new DataAtomImpl(atomPredicate, datalogDataAtom.getTerms());
    }

    private static QueryNode createQueryNode(Function dataOrCompositeAtom, Collection<Predicate> tablePredicates,
                                             Optional<BooleanExpression> optionalFilterCondition)
            throws InvalidDatalogProgramException {
        if (dataOrCompositeAtom.isDataFunction()) {
            return createDataNode(convertFromDatalogDataAtom(dataOrCompositeAtom),tablePredicates);
        }

        Predicate atomPredicate = dataOrCompositeAtom.getFunctionSymbol();
        if  (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
            throw new RuntimeException("TODO: add this class");
        }
        else if  (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
            return new InnerJoinNodeImpl(optionalFilterCondition);
        }
        else {
            throw new InvalidDatalogProgramException("Unsupported predicate: " + atomPredicate);
        }
    }

    /**
     * TODO: explain
     */
    private static DataNode createDataNode(DataAtom dataAtom, Collection<Predicate> tablePredicates) {

        if (tablePredicates.contains(dataAtom.getPredicate())) {
            return new TableNodeImpl(dataAtom);
        }

        return new OrdinaryDataNodeImpl(dataAtom);
    }
}
