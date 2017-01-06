package it.unibz.inf.ontop.pivotalrepr.datalog;

import java.util.Optional;

import com.google.inject.Injector;
import fj.P2;
import fj.data.List;
import it.unibz.inf.ontop.model.impl.DatalogTools;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.PullOutEqualityNormalizerImpl;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.ConstructionNodeImpl;

import java.util.Collection;
import java.util.HashSet;

/**
 * Converts a Datalog rule into an intermediate query.
 *
 * Note here List are from Functional Java, not java.util.List.
 */
public class DatalogRule2QueryConverter {

    /**
     * TODO: explain
     */
    private static class AtomClassification {
        private final List<Function> dataAndCompositeAtoms;
        private final List<Function> booleanAtoms;
        private final Optional<Function> optionalGroupAtom;

        protected AtomClassification(List<Function> atoms) throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {
            dataAndCompositeAtoms = DatalogTools.filterDataAndCompositeAtoms(atoms);
            List<Function> otherAtoms = DatalogTools.filterNonDataAndCompositeAtoms(atoms);
            booleanAtoms = DatalogTools.filterBooleanAtoms(otherAtoms);

            if (dataAndCompositeAtoms.isEmpty())
                throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("No data or composite atom in " + atoms);

            optionalGroupAtom = extractOptionalGroupAtom(otherAtoms);

            /**
             * May throw a NotSupportedConversionException
             */
            checkNonDataOrCompositeAtomSupport(otherAtoms, booleanAtoms, optionalGroupAtom);
        }

        private static Optional<Function> extractOptionalGroupAtom(List<Function> atoms)
                throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {
            List<Function> groupAtoms = atoms.filter(atom -> atom.getFunctionSymbol().equals(OBDAVocabulary.SPARQL_GROUP));

            switch(groupAtoms.length()) {
                case 0:
                    return Optional.empty();
                case 1:
                    return Optional.of(groupAtoms.head());
                default:
                    throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("Multiple GROUP atoms found in the same body! " +
                            groupAtoms);
            }
        }

        /**
         * All the other atoms are currently presumed to be boolean atoms
         */
        private static void checkNonDataOrCompositeAtomSupport(List<Function> otherAtoms,
                                                               List<Function> booleanAtoms,
                                                               Optional<Function> optionalGroupAtom)
                throws DatalogProgram2QueryConverter.NotSupportedConversionException {

            int groupCount = optionalGroupAtom.isPresent()? 1 : 0;

            if (booleanAtoms.length() + groupCount < otherAtoms.length()) {
                HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
                unsupportedAtoms.removeAll(booleanAtoms.toCollection());
                if (groupCount == 1)
                    unsupportedAtoms.remove(optionalGroupAtom.get());

                throw new DatalogProgram2QueryConverter.NotSupportedConversionException(
                        "Conversion of the following atoms to the intermediate query is not (yet) supported: "
                                + unsupportedAtoms);
            }
        }

    }


    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private static final Optional<ArgumentPosition> NO_POSITION = Optional.empty();
    private static final Optional<ArgumentPosition> LEFT_POSITION = Optional.of(ArgumentPosition.LEFT);
    private static final Optional<ArgumentPosition> RIGHT_POSITION = Optional.of(ArgumentPosition.RIGHT);

    /**
     * TODO: describe
     */
    public static IntermediateQuery convertDatalogRule(MetadataForQueryOptimization metadata, CQIE datalogRule,
                                                       Collection<Predicate> tablePredicates,
                                                       Optional<ImmutableQueryModifiers> optionalModifiers,
                                                       Injector injector)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {

        P2<DistinctVariableOnlyDataAtom, ImmutableSubstitution<ImmutableTerm>> decomposition =
                DatalogConversionTools.convertFromDatalogDataAtom(datalogRule.getHead());

        DistinctVariableOnlyDataAtom projectionAtom = decomposition._1();

        ConstructionNode rootNode = new ConstructionNodeImpl(projectionAtom.getVariables(), decomposition._2(),
                optionalModifiers);

        List<Function> bodyAtoms = List.iterableList(datalogRule.getBody());
        if (bodyAtoms.isEmpty()) {
            return createFact(metadata, rootNode, projectionAtom, injector);
        }
        else {
            AtomClassification atomClassification = new AtomClassification(bodyAtoms);

            return createDefinition(metadata, rootNode, projectionAtom, tablePredicates,
                    atomClassification.dataAndCompositeAtoms, atomClassification.booleanAtoms,
                    atomClassification.optionalGroupAtom, injector);
        }
    }

    private static IntermediateQuery createFact(MetadataForQueryOptimization metadata, ConstructionNode rootNode,
                                                DistinctVariableOnlyDataAtom projectionAtom, Injector injector) {
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, injector);
        queryBuilder.init(projectionAtom, rootNode);
        return queryBuilder.build();
    }


    /**
     * TODO: explain
     */
    private static IntermediateQuery createDefinition(MetadataForQueryOptimization metadata, ConstructionNode rootNode,
                                                      DistinctVariableOnlyDataAtom projectionAtom,
                                                      Collection<Predicate> tablePredicates,
                                                      List<Function> dataAndCompositeAtoms,
                                                      List<Function> booleanAtoms, Optional<Function> optionalGroupAtom, Injector injector)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException {
        /**
         * TODO: explain
         */
        Optional<JoinOrFilterNode> optionalFilterOrJoinNode = createFilterOrJoinNode(dataAndCompositeAtoms, booleanAtoms);

        // Non final
        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, injector);

        try {
            queryBuilder.init(projectionAtom, rootNode);

            /**
             * Intermediate node: ConstructionNode root or GROUP node
             */
            QueryNode intermediateNode;
            if (optionalGroupAtom.isPresent()) {
                throw new RuntimeException("Conversion of the GROUP atom is not supported yet");
                // intermediateNode = createGroupNode(optionalGroupAtom.get());
                // queryBuilder.addChild(rootNode, intermediateNode);
            }
            else {
                intermediateNode = rootNode;
            }


            /**
             * Bottom node: intermediate node or JOIN or FILTER
             */
            QueryNode bottomNode;
            if (optionalFilterOrJoinNode.isPresent()) {
                bottomNode = optionalFilterOrJoinNode.get();
                queryBuilder.addChild(intermediateNode, bottomNode);
            }
            else {
                bottomNode = rootNode;
            }

            /**
             * TODO: explain
             */
            queryBuilder = convertDataOrCompositeAtoms(dataAndCompositeAtoms, queryBuilder, bottomNode, NO_POSITION,
                    tablePredicates);
            return queryBuilder.build();
        }
        catch (IntermediateQueryBuilderException e) {
            throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    private static Optional<JoinOrFilterNode> createFilterOrJoinNode(List<Function> dataAndCompositeAtoms,
                                                              List<Function> booleanAtoms) {
        Optional<ImmutableExpression> optionalFilter = createFilterExpression(booleanAtoms);

        int dataAndCompositeAtomCount = dataAndCompositeAtoms.length();
        Optional<JoinOrFilterNode> optionalRootNode;

        /**
         * Filter as a root node
         */
        if (optionalFilter.isPresent() && (dataAndCompositeAtomCount == 1)) {
            optionalRootNode = Optional.of((JoinOrFilterNode) new FilterNodeImpl(optionalFilter.get()));
        }
        else if (dataAndCompositeAtomCount > 1) {
            optionalRootNode = Optional.of((JoinOrFilterNode) new InnerJoinNodeImpl(optionalFilter));
        }
        /**
         * No need to create a special root node (will be the unique data atom)
         */
        else {
            optionalRootNode = Optional.empty();
        }
        return optionalRootNode;
    }


    private static Optional<ImmutableExpression> createFilterExpression(List<Function> booleanAtoms) {
        if (booleanAtoms.isEmpty())
            return Optional.empty();
        return Optional.of(DATA_FACTORY.getImmutableExpression(DatalogTools.foldBooleanConditions(booleanAtoms)));
    }

    /**
     * TODO: describe
     */
    private static IntermediateQueryBuilder convertDataOrCompositeAtoms(final List<Function> atoms,
                                                                        IntermediateQueryBuilder queryBuilder,
                                                                        final QueryNode parentNode,
                                                                        Optional<ArgumentPosition> optionalPosition,
                                                                        Collection<Predicate> tablePredicates)
            throws IntermediateQueryBuilderException, DatalogProgram2QueryConverter.InvalidDatalogProgramException {
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
                    queryBuilder = convertJoinAtom(queryBuilder, parentNode, subAtoms, optionalPosition,
                            tablePredicates);
                }
                else if(atomPredicate.equals(OBDAVocabulary.SPARQL_LEFTJOIN)) {
                    queryBuilder = convertLeftJoinAtom(queryBuilder, parentNode, subAtoms, optionalPosition,
                            tablePredicates);
                }
                else {
                    throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("Unsupported predicate: " + atomPredicate);
                }
            }
            /**
             * Data atom: creates a DataNode and adds it to the tree
             */
            else if (atom.isDataFunction()) {

                /**
                 * Creates the node
                 */
                P2<DistinctVariableOnlyDataAtom, ImmutableSubstitution<ImmutableTerm>> convertionResults = DatalogConversionTools.convertFromDatalogDataAtom(atom);
                ImmutableSubstitution<ImmutableTerm> bindings = convertionResults._2();
                DataAtom dataAtom = bindings.applyToDataAtom(convertionResults._1());
                DataNode currentNode = DatalogConversionTools.createDataNode(dataAtom, tablePredicates);
                queryBuilder.addChild(parentNode, currentNode, optionalPosition);
            }
            else {
                throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("Unsupported non-data atom: " + atom);
            }
        }

        return queryBuilder;
    }

    private static IntermediateQueryBuilder convertLeftJoinAtom(IntermediateQueryBuilder queryBuilder,
                                                                QueryNode parentNodeOfTheLJ,
                                                                List<Function> subAtomsOfTheLJ,
                                                                Optional<ArgumentPosition> optionalPosition,
                                                                Collection<Predicate> tablePredicates)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        P2<List<Function>, List<Function>> decomposition = PullOutEqualityNormalizerImpl.splitLeftJoinSubAtoms(subAtomsOfTheLJ);
        final List<Function> leftAtoms = decomposition._1();
        final List<Function> rightAtoms = decomposition._2();

        /**
         * TODO: explain why we just care about the right
         */
        AtomClassification rightSubAtomClassification = new AtomClassification(rightAtoms);

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                rightSubAtomClassification.booleanAtoms);

        LeftJoinNode ljNode = new LeftJoinNodeImpl(optionalFilterCondition);
        queryBuilder.addChild(parentNodeOfTheLJ, ljNode, optionalPosition);

        /**
         * Adds the left part
         */
        queryBuilder = convertJoinAtom(queryBuilder, ljNode, leftAtoms, LEFT_POSITION, tablePredicates);

        /**
         * Adds the right part
         */
        return convertDataOrCompositeAtoms(rightSubAtomClassification.dataAndCompositeAtoms, queryBuilder, ljNode,
                RIGHT_POSITION, tablePredicates);
    }

    /**
     * TODO: explain
     *
     */
    private static IntermediateQueryBuilder convertJoinAtom(IntermediateQueryBuilder queryBuilder,
                                                            QueryNode parentNodeOfTheJoinNode,
                                                            List<Function> subAtomsOfTheJoin,
                                                            Optional<ArgumentPosition> optionalPosition,
                                                            Collection<Predicate> tablePredicates)
            throws DatalogProgram2QueryConverter.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        AtomClassification classification = new AtomClassification(subAtomsOfTheJoin);
        if (classification.optionalGroupAtom.isPresent()) {
            throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("GROUP atom found inside a LJ meta-atom");
        }

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                classification.booleanAtoms);

        if (classification.dataAndCompositeAtoms.isEmpty()) {
            throw new DatalogProgram2QueryConverter.InvalidDatalogProgramException("Empty join found");
        }
        /**
         * May happen because this method can also be called after the LJ conversion
         */
        else if (classification.dataAndCompositeAtoms.length() == 1) {
            if (optionalFilterCondition.isPresent()) {
                FilterNode filterNode = new FilterNodeImpl(optionalFilterCondition.get());
                queryBuilder.addChild(parentNodeOfTheJoinNode, filterNode, optionalPosition);

                return convertDataOrCompositeAtoms(classification.dataAndCompositeAtoms, queryBuilder, filterNode,
                        NO_POSITION, tablePredicates);
            }
            /**
             * Otherwise, no need for intermediate query node.
             */
            else {
                return convertDataOrCompositeAtoms(classification.dataAndCompositeAtoms, queryBuilder,
                        parentNodeOfTheJoinNode, optionalPosition, tablePredicates);
            }
        }
        /**
         * Normal case
         */
        else {
            InnerJoinNode joinNode = new InnerJoinNodeImpl(optionalFilterCondition);
            queryBuilder.addChild(parentNodeOfTheJoinNode, joinNode, optionalPosition);

            /**
             * Indirect recursive call for composite atoms
             */
            return convertDataOrCompositeAtoms(classification.dataAndCompositeAtoms, queryBuilder, joinNode, NO_POSITION,
                    tablePredicates);
        }

    }

}
