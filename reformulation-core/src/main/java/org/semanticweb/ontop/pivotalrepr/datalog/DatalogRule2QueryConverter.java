package org.semanticweb.ontop.pivotalrepr.datalog;

import com.google.common.base.Optional;
import fj.P;
import fj.P2;
import fj.data.List;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.*;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.datalog.DatalogProgram2QueryConverter.InvalidDatalogProgramException;
import org.semanticweb.ontop.pivotalrepr.impl.*;

import java.util.Collection;
import java.util.HashSet;

import static org.semanticweb.ontop.model.impl.ImmutabilityTools.convertIntoImmutableBooleanExpression;
import static org.semanticweb.ontop.owlrefplatform.core.basicoperations.PullOutEqualityNormalizerImpl.splitLeftJoinSubAtoms;
import static org.semanticweb.ontop.pivotalrepr.datalog.DatalogConversionTools.convertFromDatalogDataAtom;
import static org.semanticweb.ontop.pivotalrepr.datalog.DatalogConversionTools.createDataNode;

/**
 * Converts a Datalog rule into an intermediate query.
 *
 * Note here List are from Functional Java, not java.util.List.
 */
public class DatalogRule2QueryConverter {

    /**
     * TODO: describe
     */
    public static IntermediateQuery convertDatalogRule(CQIE datalogRule, Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException {

        ConstructionNode rootNode = createConstructionNodeWithoutModifier(datalogRule);

        List<Function> bodyAtoms = List.iterableList(datalogRule.getBody());
        P2<List<Function>, List<Function>> atomClassification = classifyJoinSubAtoms(
                bodyAtoms);
        List<Function> dataAndCompositeAtoms = atomClassification._1();
        List<Function> booleanAtoms = atomClassification._2();

        return createDefinition(rootNode, tablePredicates, dataAndCompositeAtoms, booleanAtoms);
    }

    /**
     * For non-top datalog rules (that do not access to query modifiers)
     *
     * TODO: make sure the GROUP atom cannot be used for such rules.
     *
     */
    private static ConstructionNode createConstructionNodeWithoutModifier(CQIE datalogRule) throws
            InvalidDatalogProgramException {
        P2<DataAtom, ImmutableSubstitution<ImmutableTerm>> decomposition =
                convertFromDatalogDataAtom(datalogRule.getHead());

        return new ConstructionNodeImpl(decomposition._1(), decomposition._2());
    }

    private static P2<List<Function>, List<Function>> classifyJoinSubAtoms(List<Function> atoms)
            throws InvalidDatalogProgramException {
        List<Function> dataAndCompositeAtoms = DatalogTools.filterDataAndCompositeAtoms(atoms);
        List<Function> otherAtoms = DatalogTools.filterNonDataAndCompositeAtoms(atoms);
        List<Function> booleanAtoms = DatalogTools.filterBooleanAtoms(otherAtoms);

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
                                                      List<Function> dataAndCompositeAtoms,
                                                      List<Function> booleanAtoms)
            throws InvalidDatalogProgramException {
        /**
         * TODO: explain
         */
        Optional<QueryNode> optionalViceTopNode = createViceTopNode(dataAndCompositeAtoms, booleanAtoms);

        // Non final
        IntermediateQueryBuilder queryBuilder = new JgraphtIntermediateQueryBuilder();

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
    private static Optional<QueryNode> createViceTopNode(List<Function> dataAndCompositeAtoms,
                                                         List<Function> booleanAtoms) {
        Optional<ImmutableBooleanExpression> optionalFilter = createFilterExpression(booleanAtoms);

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


    private static Optional<ImmutableBooleanExpression> createFilterExpression(List<Function> booleanAtoms) {
        if (booleanAtoms.isEmpty())
            return Optional.absent();
        return Optional.of(convertIntoImmutableBooleanExpression(DatalogTools.foldBooleanConditions(booleanAtoms)));
    }

    /**
     * All the other atoms are currently presumed to be boolean atoms
     * TODO: go beyond this restriction to support GROUP
     */
    private static void checkNonDataOrCompositeAtomSupport(List<Function> otherAtoms,
                                                           List<Function> booleanAtoms)
            throws DatalogProgram2QueryConverter.NotSupportedConversionException {
        if (booleanAtoms.length() < otherAtoms.length()) {
            HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
            unsupportedAtoms.removeAll(booleanAtoms.toCollection());
            throw new DatalogProgram2QueryConverter.NotSupportedConversionException("Conversion of the following atoms to the intermediate query " +
                    "is not (yet) supported: " + unsupportedAtoms);
        }
    }

    /**
     * TODO: describe
     */
    private static IntermediateQueryBuilder convertDataOrCompositeAtoms(final List<Function> atoms,
                                                                        IntermediateQueryBuilder queryBuilder,
                                                                        final QueryNode parentNode,
                                                                        Collection<Predicate> tablePredicates)
            throws IntermediateQueryBuilderException, InvalidDatalogProgramException {
        /**
         * For each atom
         */
        for (Function atom : atoms) {
            /**
             * If the atom is composite, extracts sub atoms
             */
            if (atom.isAlgebraFunction()) {
                List<Function> subAtoms = List.iterableList(
                        (java.util.List<Function>)(java.util.List<?>)atom.getTerms());

                Predicate atomPredicate = atom.getFunctionSymbol();
                if (atomPredicate.equals(OBDAVocabulary.SPARQL_JOIN)) {
                    queryBuilder = convertJoinAtom(queryBuilder, parentNode, subAtoms, tablePredicates);
                }
                else if(atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
                    queryBuilder = convertLeftJoinAtom(queryBuilder, parentNode, subAtoms, tablePredicates);
                }
                else {
                    throw new InvalidDatalogProgramException("Unsupported predicate: " + atomPredicate);
                }
            }
            /**
             * Data atom: creates a DataNode and adds it to the tree
             */
            else if (atom.isDataFunction()) {

                /**
                 * Creates the node
                 */
                DataAtom dataAtom = convertFromDatalogDataAtom(atom)._1();
                DataNode currentNode = createDataNode(dataAtom,tablePredicates);
                queryBuilder.addChild(parentNode, currentNode);
            }
            else {
                throw new InvalidDatalogProgramException("Unsupported non-data atom: " + atom);
            }
        }

        return queryBuilder;
    }

    private static IntermediateQueryBuilder convertLeftJoinAtom(IntermediateQueryBuilder queryBuilder,
                                                                QueryNode parentNodeOfTheLJ,
                                                                List<Function> subAtomsOfTheLJ,
                                                                Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException, IntermediateQueryBuilderException {

        P2<List<Function>, List<Function>> decomposition = splitLeftJoinSubAtoms(subAtomsOfTheLJ);
        final List<Function> leftAtoms = decomposition._1();
        final List<Function> rightAtoms = decomposition._2();

        /**
         * TODO: explain why we just care about the right
         */
        P2<List<Function>, List<Function>> rightSubAtomClassification = classifyJoinSubAtoms(rightAtoms);
        List<Function> rightSubDataOrCompositeAtoms = rightSubAtomClassification._1();
        List<Function> rightBooleanSubAtoms = rightSubAtomClassification._2();

        Optional<ImmutableBooleanExpression> optionalFilterCondition = createFilterExpression(rightBooleanSubAtoms);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(optionalFilterCondition);
        queryBuilder.addChild(parentNodeOfTheLJ, ljNode);

        /**
         * Adds the left part
         *
         * TODO: Is this approach enough for distinguishing the left from the right????????
         */
        queryBuilder = convertJoinAtom(queryBuilder, ljNode, leftAtoms, tablePredicates);

        /**
         * Adds the right part
         */
        return convertDataOrCompositeAtoms(rightSubDataOrCompositeAtoms, queryBuilder, ljNode, tablePredicates);
    }

    /**
     * TODO: explain
     *
     */
    private static IntermediateQueryBuilder convertJoinAtom(IntermediateQueryBuilder queryBuilder,
                                                            QueryNode parentNodeOfTheJoinNode,
                                                            List<Function> subAtomsOfTheJoin,
                                                            Collection<Predicate> tablePredicates)
            throws InvalidDatalogProgramException, IntermediateQueryBuilderException {

        P2<List<Function>, List<Function>> subAtomClassification = classifyJoinSubAtoms(subAtomsOfTheJoin);
        List<Function> subDataOrCompositeAtoms = subAtomClassification._1();
        List<Function> booleanSubAtoms = subAtomClassification._2();

        Optional<ImmutableBooleanExpression> optionalFilterCondition = createFilterExpression(booleanSubAtoms);

        if (subDataOrCompositeAtoms.isEmpty()) {
            throw new InvalidDatalogProgramException("Empty join found");
        }
        /**
         * May happen because this method can also be called after the LJ conversion
         */
        else if (subDataOrCompositeAtoms.length() == 1) {
            if (optionalFilterCondition.isPresent()) {
                FilterNode filterNode = new FilterNodeImpl(optionalFilterCondition.get());
                queryBuilder.addChild(parentNodeOfTheJoinNode, filterNode);

                return convertDataOrCompositeAtoms(subDataOrCompositeAtoms, queryBuilder, filterNode,
                        tablePredicates);
            }
            /**
             * Otherwise, no need for intermediate query node.
             */
            else {
                return convertDataOrCompositeAtoms(subDataOrCompositeAtoms, queryBuilder, parentNodeOfTheJoinNode,
                        tablePredicates);
            }
        }
        /**
         * Normal case
         */
        else {
            InnerJoinNode joinNode = new InnerJoinNodeImpl(optionalFilterCondition);
            queryBuilder.addChild(parentNodeOfTheJoinNode, joinNode);

            /**
             * Indirect recursive call for composite atoms
             */
            return convertDataOrCompositeAtoms(subDataOrCompositeAtoms, queryBuilder, joinNode, tablePredicates);
        }



    }

    /**
     * TODO: explain
     */
    private static QueryNode createQueryNode(Function dataOrCompositeAtom, Collection<Predicate> tablePredicates,
                                             Optional<ImmutableBooleanExpression> optionalFilterCondition)
            throws InvalidDatalogProgramException {
        if (dataOrCompositeAtom.isDataFunction()) {
            DataAtom dataAtom = convertFromDatalogDataAtom(dataOrCompositeAtom)._1();
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

}
