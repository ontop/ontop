package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
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
        fj.data.List<Function> bodyAtoms = fj.data.List.iterableList(datalogBodyAtoms);
        fj.data.List<Function> dataAndCompositeAtoms = DatalogTools.filterDataAndCompositeAtoms(bodyAtoms);
        fj.data.List<Function> otherAtoms = DatalogTools.filterNonDataAndCompositeAtoms(bodyAtoms);

        int dataAndCompositeAtomCount = dataAndCompositeAtoms.length();

        if (dataAndCompositeAtomCount == 0)
            throw new InvalidDatalogProgramException("No data or composite atom in " + datalogBodyAtoms);

        IntermediateQueryBuilder queryBuilder;
        try {
            // TODO: add the filter conditions
            boolean hasFilter = false;
            if ((!hasFilter) && dataAndCompositeAtomCount == 1) {
                queryBuilder = convertDataOrCompositeAtom(dataAndCompositeAtoms.head(), tablePredicates);
            } else {
                QueryNode rootNode;
                if (dataAndCompositeAtomCount > 1) {
                    rootNode = new InnerJoinNodeImpl();
                }
                /**
                 * Simple filter
                 */
                else {
                    throw new RuntimeException("Not implemented yet");
                }
                queryBuilder = new IntermediateQueryBuilderImpl();
                queryBuilder.init(rootNode);
                queryBuilder = convertSubAtoms(dataAndCompositeAtoms.toJavaList(), queryBuilder, rootNode,
                        tablePredicates);
            }

            return queryBuilder.build();
        }
        catch (IntermediateQueryBuilderException e) {
            throw new InvalidDatalogProgramException(e.getMessage());
        }
    }

    /**
     * When the atom is the root of the intermediate query
     */
    private static IntermediateQueryBuilder convertDataOrCompositeAtom(Function dataOrCompositeAtom,
                                                                       Collection<Predicate> tablePredicates)
            throws IntermediateQueryBuilderException, InvalidDatalogProgramException {
        QueryNode currentNode = createQueryNode(dataOrCompositeAtom, tablePredicates);

        IntermediateQueryBuilder queryBuilder = new IntermediateQueryBuilderImpl();
        queryBuilder.init(currentNode);

        if (dataOrCompositeAtom.isDataFunction()) {
            return queryBuilder;

        } else {
            // TODO: what about filter conditions????
            return convertSubAtoms((List<Function>)(List<?>)dataOrCompositeAtom.getTerms(), queryBuilder, currentNode,
                    tablePredicates);
        }
    }

    /**
     * TODO: describe
     */
    private static IntermediateQueryBuilder convertSubAtoms(final List<Function> subAtoms,
                                                            IntermediateQueryBuilder queryBuilder,
                                                            final QueryNode parentNode,
                                                            Collection<Predicate> tablePredicates)
            throws IntermediateQueryBuilderException, InvalidDatalogProgramException {
        for (Function subAtom : subAtoms) {
            QueryNode currentNode = createQueryNode(subAtom, tablePredicates);
            queryBuilder.addChild(parentNode, currentNode);

            /**
             * Recursive call for composite atoms
             */
            if (!subAtom.isDataFunction()) {
                queryBuilder = convertSubAtoms((List<Function>)(List<?>)subAtom.getTerms(), queryBuilder, currentNode,
                        tablePredicates);
            }
        }

        return queryBuilder;
    }


    private static DataAtom convertFromDatalogDataAtom(Function datalogAtom) {
        Predicate datalogAtomPredicate = datalogAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = new AtomPredicateImpl(datalogAtomPredicate);

        return new DataAtomImpl(atomPredicate, datalogAtom.getTerms());
    }

    private static QueryNode createQueryNode(Function dataOrCompositeAtom, Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {
        if (dataOrCompositeAtom.isDataFunction()) {
            return createDataNode(convertFromDatalogDataAtom(dataOrCompositeAtom),tablePredicates);
        }

        Predicate atomPredicate = dataOrCompositeAtom.getFunctionSymbol();
        if  (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
            throw new RuntimeException("TODO: add this class");
        }
        else if  (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
            return new InnerJoinNodeImpl();
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

        return new TripleNodeImpl(dataAtom);
    }
}
