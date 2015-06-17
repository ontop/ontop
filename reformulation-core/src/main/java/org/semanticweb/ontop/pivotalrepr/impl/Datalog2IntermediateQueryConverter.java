package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import fj.P;
import fj.P2;
import fj.P3;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.DatalogTools;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.IndempotentVar2VarSubstitutionImpl;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.VariableDispatcher;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.utils.DatalogDependencyGraphGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.semanticweb.ontop.model.impl.ImmutabilityTools.convertIntoImmutableTerm;

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
    public static IntermediateQuery convertFromDatalog(DatalogProgram queryProgram,
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
     * TODO: explain
     * TODO: find a better name
     */
    private static IntermediateQuery createTopIntermediateQuery(Function rootDatalogAtom,
                                                                OBDAQueryModifiers queryModifiers)
            throws InvalidDatalogProgramException {
        try {
            IntermediateQueryBuilder builder = new IntermediateQueryBuilderImpl();

            /**
             * TODO: is there a problem with a top rule with non-distinct variables in its head?
             * TODO: remove this consideration (simplify)
             */
            final boolean allowVariableCreation = false;
            P3<PureDataAtom, ImmutableSubstitution<GroundTerm>, ImmutableSubstitution<ImmutableFunctionalTerm>> decomposition =
                    convertFromDatalogDataAtom(rootDatalogAtom, allowVariableCreation);

            PureDataAtom dataAtom = decomposition._1();
            ImmutableSubstitution<GroundTerm> groundBindings = decomposition._2();
            ImmutableSubstitution<ImmutableFunctionalTerm> functionalBindings = decomposition._3();

            ConstructionNode rootNode;
            if (queryModifiers.hasModifiers()) {
                // TODO: explain
                ImmutableQueryModifiers immutableQueryModifiers = new ImmutableQueryModifiersImpl(queryModifiers);
                rootNode = new ConstructionNodeImpl(dataAtom, groundBindings, functionalBindings,
                        immutableQueryModifiers);
            } else {
                rootNode = new ConstructionNodeImpl(dataAtom, groundBindings, functionalBindings);
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
            convertedDefinitions.add(convertDatalogRule(datalogAtomDefinition, tablePredicates));
        }

        try {
            return IntermediateQueryUtils.mergeDefinitions(convertedDefinitions);
        } catch (QueryMergingException e) {
            throw new InvalidDatalogProgramException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: describe
     */
    private static IntermediateQuery convertDatalogRule(CQIE datalogRule, Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {

        ConstructionNode rootNode = createProjectionNodeWithoutModifier(datalogRule);

        fj.data.List<Function> bodyAtoms = fj.data.List.iterableList(datalogRule.getBody());
        P2<fj.data.List<Function>, fj.data.List<Function>> atomClassification = classifyAtoms(
                bodyAtoms);
        fj.data.List<Function> dataAndCompositeAtoms = atomClassification._1();
        fj.data.List<Function> booleanAtoms = atomClassification._2();

        return createDefinition(rootNode, tablePredicates, dataAndCompositeAtoms, booleanAtoms);
    }

    /**
     * For non-top datalog rules (that do not access to query modifiers)
     *
     * TODO: make sure the GROUP atom cannot be used for such rules.
     *
     */
    private static ConstructionNode createProjectionNodeWithoutModifier(CQIE datalogRule) throws InvalidDatalogProgramException {
        // Non-top rule so ok.
        final boolean allowVariableCreation = true;

        P3<PureDataAtom, ImmutableSubstitution<GroundTerm>, ImmutableSubstitution<ImmutableFunctionalTerm>> decomposition =
                convertFromDatalogDataAtom(datalogRule.getHead(), allowVariableCreation);

        return new ConstructionNodeImpl(decomposition._1(), decomposition._2(), decomposition._3());
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
    private static IntermediateQuery createDefinition(ConstructionNode rootNode, Collection<Predicate> tablePredicates,
                                                      fj.data.List<Function> dataAndCompositeAtoms,
                                                      fj.data.List<Function> booleanAtoms)
            throws InvalidDatalogProgramException {
        /**
         * TODO: explain
         */
        Optional<QueryNode> optionalViceTopNode = createViceTopNode(dataAndCompositeAtoms, booleanAtoms);

        // Non final
        IntermediateQueryBuilder queryBuilder = new IntermediateQueryBuilderImpl();

        try {
            queryBuilder.init(rootNode);

            /**
             * TODO: explain
             */
            QueryNode quasiTopNode;
            if (optionalViceTopNode.isPresent()) {
                quasiTopNode = optionalViceTopNode.get();
                queryBuilder.addChild(rootNode, quasiTopNode);
            }
            else {
                quasiTopNode = rootNode;
            }

            /**
             * TODO: explain
             */
            queryBuilder = convertDataOrCompositeAtoms(dataAndCompositeAtoms, queryBuilder, quasiTopNode,
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
    private static Optional<QueryNode> createViceTopNode(fj.data.List<Function> dataAndCompositeAtoms,
                                                         fj.data.List<Function> booleanAtoms) {
        Optional<BooleanExpression> optionalFilter = createFilterExpression(booleanAtoms);

        int dataAndCompositeAtomCount = dataAndCompositeAtoms.length();
        Optional<QueryNode> optionalRootNode;

        /**
         * Simple filter as a root node
         */
        if (optionalFilter.isPresent() && (dataAndCompositeAtomCount == 1)) {
            optionalRootNode = Optional.of((QueryNode) new FilterNodeImpl(optionalFilter.get()));
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
    private static void checkNonDataOrCompositeAtomSupport(fj.data.List<Function> otherAtoms,
                                                           fj.data.List<Function> booleanAtoms)
        throws NotSupportedConversionException{
        if (booleanAtoms.length() < otherAtoms.length()) {
            HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
            unsupportedAtoms.removeAll(booleanAtoms.toCollection());
            throw new NotSupportedConversionException("Conversion of the following atoms to the intermediate query " +
                    "is not (yet) supported: " + unsupportedAtoms);
        }
    }

    /**
     * TODO: describe
     */
    private static IntermediateQueryBuilder convertDataOrCompositeAtoms(final fj.data.List<Function> atoms,
                                                                        IntermediateQueryBuilder queryBuilder,
                                                                        final QueryNode parentNode,
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
            queryBuilder.addChild(parentNode, currentNode);

            /**
             * Recursive call for composite atoms
             */
            if (optionalSubDataOrCompositeAtoms.isPresent()) {
                queryBuilder = convertDataOrCompositeAtoms(optionalSubDataOrCompositeAtoms.get(), queryBuilder,
                        currentNode, tablePredicates);
            }
        }

        return queryBuilder;
    }


    /**
     * TODO: explain
     */
    private static P3<PureDataAtom, ImmutableSubstitution<GroundTerm>, ImmutableSubstitution<ImmutableFunctionalTerm>>
                        convertFromDatalogDataAtom(Function datalogDataAtom, boolean allowVariableCreation)
        throws InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        AtomPredicate atomPredicate = new AtomPredicateImpl(datalogAtomPredicate);

        ImmutableList.Builder<VariableImpl> varListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<VariableImpl, ImmutableTerm> allBindingBuilder = ImmutableMap.builder();

        /**
         * Replaces all the terms by variables.
         * Makes sure these variables are unique.
         *
         * Creates allBindings entries if needed (in case of constant of a functional term)
         */
        VariableDispatcher variableDispatcher = new VariableDispatcher();
        for (Term term : datalogDataAtom.getTerms()) {
            VariableImpl newVariableArgument;

            /**
             * Keep the same variable.
             */
            if (term instanceof VariableImpl) {
                newVariableArgument = (VariableImpl) term;
            }
            /**
             * TODO: could we consider a sub-class of Function instead?
             */
            else if ((term instanceof Constant) || (term instanceof Function)) {
                if (!allowVariableCreation) {
                    throw new InvalidDatalogProgramException("Constant/Function " + term +
                            " in a data atom that should only contain variables " + datalogDataAtom);
                }
                newVariableArgument = variableDispatcher.generateNewVariable();
                allBindingBuilder.put(newVariableArgument, convertIntoImmutableTerm(term));
            } else {
                throw new InvalidDatalogProgramException("Unexpected term found in a data atom: " + term);
            }
            varListBuilder.add(newVariableArgument);
        }

        PureDataAtom dataAtom = new PureDataAtomImpl(atomPredicate, varListBuilder.build());

        P2<ImmutableSubstitution<GroundTerm>, ImmutableSubstitution<ImmutableFunctionalTerm>> decomposition =
                QueryNodeTools.sortBindings(allBindingBuilder.build());


        return P.p(dataAtom, decomposition._1(), decomposition._2());
    }

    /**
     * TODO: describe
     */
    private static VariableImpl convertAtomVariable(VariableImpl formerVariable,
                                                    VariableDispatcher variableDispatcher,
                                                    boolean allowVariableCreation,
                                                    Function datalogDataAtom)
            throws InvalidDatalogProgramException {
        /**
         * If the variable was unknown of the variable dispatcher, the variable is not renamed.
         * Otherwise a new variable is created.
         */
        VariableImpl newVariable = variableDispatcher.renameDataAtomVariable(formerVariable);

        boolean hasCreatedVariable = ! newVariable.equals(formerVariable);

        if ((!allowVariableCreation) && (hasCreatedVariable)) {
            throw new InvalidDatalogProgramException("Multiple occurrences of the variable " + formerVariable
            + " in the atom " + datalogDataAtom);
        }
        return newVariable;
    }

    /**
     * TODO: explain
     */
    private static QueryNode createQueryNode(Function dataOrCompositeAtom, Collection<Predicate> tablePredicates,
                                             Optional<BooleanExpression> optionalFilterCondition)
            throws InvalidDatalogProgramException {
        if (dataOrCompositeAtom.isDataFunction()) {
            // No problem here
            final boolean allowVariableCreation = true;
            PureDataAtom dataAtom = convertFromDatalogDataAtom(dataOrCompositeAtom, allowVariableCreation)._1();
            return createDataNode(dataAtom,tablePredicates);
        }

        Predicate atomPredicate = dataOrCompositeAtom.getFunctionSymbol();
        if  (atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
            return new LeftJoinNodeImpl(optionalFilterCondition);
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
    private static DataNode createDataNode(PureDataAtom dataAtom, Collection<Predicate> tablePredicates) {

        if (tablePredicates.contains(dataAtom.getPredicate())) {
            return new TableNodeImpl(dataAtom);
        }

        return new OrdinaryDataNodeImpl(dataAtom);
    }
}
