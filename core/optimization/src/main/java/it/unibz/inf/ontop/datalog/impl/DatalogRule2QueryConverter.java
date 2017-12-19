package it.unibz.inf.ontop.datalog.impl;

import java.util.Optional;

import com.google.inject.Inject;
import fj.P2;
import fj.data.List;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.PullOutEqualityNormalizer;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.datalog.TargetAtom;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

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
        private final DatalogFactory datalogFactory;
        private final DatalogTools datalogTools;

        protected AtomClassification(List<Function> atoms, DatalogFactory datalogFactory, DatalogTools datalogTools)
                throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
            this.datalogFactory = datalogFactory;
            this.datalogTools = datalogTools;
            dataAndCompositeAtoms = datalogTools.filterDataAndCompositeAtoms(atoms);
            List<Function> otherAtoms = datalogTools.filterNonDataAndCompositeAtoms(atoms);
            booleanAtoms = datalogTools.filterBooleanAtoms(otherAtoms);

            if (dataAndCompositeAtoms.isEmpty())
                throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("No data or composite atom in " + atoms);

            optionalGroupAtom = extractOptionalGroupAtom(otherAtoms);

            /*
             * May throw a NotSupportedConversionException
             */
            checkNonDataOrCompositeAtomSupport(otherAtoms, booleanAtoms, optionalGroupAtom);
        }

        private Optional<Function> extractOptionalGroupAtom(List<Function> atoms)
                throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
            List<Function> groupAtoms = atoms.filter(atom -> atom.getFunctionSymbol().equals(
                    datalogFactory.getSparqlGroupPredicate()));

            switch(groupAtoms.length()) {
                case 0:
                    return Optional.empty();
                case 1:
                    return Optional.of(groupAtoms.head());
                default:
                    throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Multiple GROUP atoms found in the same body! " +
                            groupAtoms);
            }
        }

        /**
         * All the other atoms are currently presumed to be boolean atoms
         */
        private static void checkNonDataOrCompositeAtomSupport(List<Function> otherAtoms,
                                                               List<Function> booleanAtoms,
                                                               Optional<Function> optionalGroupAtom)
                throws DatalogProgram2QueryConverterImpl.NotSupportedConversionException {

            int groupCount = optionalGroupAtom.isPresent()? 1 : 0;

            if (booleanAtoms.length() + groupCount < otherAtoms.length()) {
                HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
                unsupportedAtoms.removeAll(booleanAtoms.toCollection());
                if (groupCount == 1)
                    unsupportedAtoms.remove(optionalGroupAtom.get());

                throw new DatalogProgram2QueryConverterImpl.NotSupportedConversionException(
                        "Conversion of the following atoms to the intermediate query is not (yet) supported: "
                                + unsupportedAtoms);
            }
        }

    }


    private static final Optional<ArgumentPosition> NO_POSITION = Optional.empty();
    private static final Optional<ArgumentPosition> LEFT_POSITION = Optional.of(ArgumentPosition.LEFT);
    private static final Optional<ArgumentPosition> RIGHT_POSITION = Optional.of(ArgumentPosition.RIGHT);
    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;
    private final DatalogConversionTools datalogConversionTools;
    private final DatalogTools datalogTools;
    private final PullOutEqualityNormalizer pullOutEqualityNormalizer;

    @Inject
    private DatalogRule2QueryConverter(TermFactory termFactory, DatalogFactory datalogFactory,
                                       DatalogConversionTools datalogConversionTools,
                                       DatalogTools datalogTools,
                                       PullOutEqualityNormalizerImpl pullOutEqualityNormalizer) {
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.datalogConversionTools = datalogConversionTools;
        this.datalogTools = datalogTools;
        this.pullOutEqualityNormalizer = pullOutEqualityNormalizer;
    }

    /**
     * TODO: describe
     */
    public IntermediateQuery convertDatalogRule(DBMetadata dbMetadata, CQIE datalogRule,
                                                       Collection<Predicate> tablePredicates,
                                                       Optional<ImmutableQueryModifiers> optionalModifiers,
                                                       IntermediateQueryFactory iqFactory,
                                                       ExecutorRegistry executorRegistry)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        TargetAtom targetAtom = datalogConversionTools.convertFromDatalogDataAtom(datalogRule.getHead());

        DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();

        ConstructionNode rootNode = iqFactory.createConstructionNode(projectionAtom.getVariables(),
                targetAtom.getSubstitution(), optionalModifiers);

        List<Function> bodyAtoms = List.iterableList(datalogRule.getBody());
        if (bodyAtoms.isEmpty()) {
            return createFact(dbMetadata, rootNode, projectionAtom, executorRegistry, iqFactory);
        }
        else {
            AtomClassification atomClassification = new AtomClassification(bodyAtoms, datalogFactory, datalogTools);

            return createDefinition(dbMetadata, rootNode, projectionAtom, tablePredicates,
                    atomClassification.dataAndCompositeAtoms, atomClassification.booleanAtoms,
                    atomClassification.optionalGroupAtom, iqFactory, executorRegistry);
        }
    }

    private static IntermediateQuery createFact(DBMetadata dbMetadata, ConstructionNode rootNode,
                                                DistinctVariableOnlyDataAtom projectionAtom, ExecutorRegistry executorRegistry,
                                                IntermediateQueryFactory modelFactory) {
        IntermediateQueryBuilder queryBuilder = modelFactory.createIQBuilder(dbMetadata, executorRegistry);
        queryBuilder.init(projectionAtom, rootNode);
        queryBuilder.addChild(rootNode, modelFactory.createTrueNode());
        return queryBuilder.build();
    }


    /**
     * TODO: explain
     */
    private IntermediateQuery createDefinition(DBMetadata dbMetadata, ConstructionNode rootNode,
                                                      DistinctVariableOnlyDataAtom projectionAtom,
                                                      Collection<Predicate> tablePredicates,
                                                      List<Function> dataAndCompositeAtoms,
                                                      List<Function> booleanAtoms, Optional<Function> optionalGroupAtom,
                                                      IntermediateQueryFactory iqFactory,
                                                      ExecutorRegistry executorRegistry)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
        /*
         * TODO: explain
         */
        Optional<JoinOrFilterNode> optionalFilterOrJoinNode = createFilterOrJoinNode(iqFactory, dataAndCompositeAtoms, booleanAtoms);

        // Non final
        IntermediateQueryBuilder queryBuilder = iqFactory.createIQBuilder(dbMetadata, executorRegistry);

        try {
            queryBuilder.init(projectionAtom, rootNode);

            /*
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


            /*
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

            /*
             * TODO: explain
             */
            queryBuilder = convertDataOrCompositeAtoms(dataAndCompositeAtoms, queryBuilder, bottomNode, NO_POSITION,
                    tablePredicates);
            return queryBuilder.build();
        }
        catch (IntermediateQueryBuilderException e) {
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    private Optional<JoinOrFilterNode> createFilterOrJoinNode(IntermediateQueryFactory iqFactory,
                                                                     List<Function> dataAndCompositeAtoms,
                                                                     List<Function> booleanAtoms) {
        Optional<ImmutableExpression> optionalFilter = createFilterExpression(booleanAtoms);

        int dataAndCompositeAtomCount = dataAndCompositeAtoms.length();
        Optional<JoinOrFilterNode> optionalRootNode;

        /*
         * Filter as a root node
         */
        if (optionalFilter.isPresent() && (dataAndCompositeAtomCount == 1)) {
            optionalRootNode = Optional.of(iqFactory.createFilterNode(optionalFilter.get()));
        }
        else if (dataAndCompositeAtomCount > 1) {
            optionalRootNode = Optional.of(iqFactory.createInnerJoinNode(optionalFilter));
        }
        /*
         * No need to create a special root node (will be the unique data atom)
         */
        else {
            optionalRootNode = Optional.empty();
        }
        return optionalRootNode;
    }


    private Optional<ImmutableExpression> createFilterExpression(List<Function> booleanAtoms) {
        if (booleanAtoms.isEmpty())
            return Optional.empty();
        return Optional.of(termFactory.getImmutableExpression(datalogTools.foldBooleanConditions(booleanAtoms)));
    }

    /**
     * TODO: describe
     */
    private IntermediateQueryBuilder convertDataOrCompositeAtoms(final List<Function> atoms,
                                                                        IntermediateQueryBuilder queryBuilder,
                                                                        final QueryNode parentNode,
                                                                        Optional<ArgumentPosition> optionalPosition,
                                                                        Collection<Predicate> tablePredicates)
            throws IntermediateQueryBuilderException, DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
        /*
         * For each atom
         */
        for (Function atom : atoms) {
            /*
             * If the atom is composite, extracts sub atoms
             */
            if (atom.isAlgebraFunction()) {
                List<Function> subAtoms = List.iterableList(
                        (java.util.List<Function>)(java.util.List<?>)atom.getTerms());

                Predicate atomPredicate = atom.getFunctionSymbol();
                if (atomPredicate.equals(datalogFactory.getSparqlJoinPredicate())) {
                    queryBuilder = convertJoinAtom(queryBuilder, parentNode, subAtoms, optionalPosition,
                            tablePredicates);
                }
                else if(atomPredicate.equals(datalogFactory.getSparqlLeftJoinPredicate())) {
                    queryBuilder = convertLeftJoinAtom(queryBuilder, parentNode, subAtoms, optionalPosition,
                            tablePredicates);
                }
                else {
                    throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Unsupported predicate: " + atomPredicate);
                }
            }
            /*
             * Data atom: creates a DataNode and adds it to the tree
             */
            else if (atom.isDataFunction()) {

                /*
                 * Creates the node
                 */
                TargetAtom targetAtom = datalogConversionTools.convertFromDatalogDataAtom(atom);
                ImmutableSubstitution<ImmutableTerm> bindings = targetAtom.getSubstitution();
                DataAtom dataAtom = bindings.applyToDataAtom(targetAtom.getProjectionAtom());
                DataNode currentNode = datalogConversionTools.createDataNode(queryBuilder.getFactory(),
                        dataAtom, tablePredicates);
                queryBuilder.addChild(parentNode, currentNode, optionalPosition);
            }
            else {
                throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Unsupported non-data atom: " + atom);
            }
        }

        return queryBuilder;
    }

    private IntermediateQueryBuilder convertLeftJoinAtom(IntermediateQueryBuilder queryBuilder,
                                                                QueryNode parentNodeOfTheLJ,
                                                                List<Function> subAtomsOfTheLJ,
                                                                Optional<ArgumentPosition> optionalPosition,
                                                                Collection<Predicate> tablePredicates)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        P2<List<Function>, List<Function>> decomposition = pullOutEqualityNormalizer.splitLeftJoinSubAtoms(subAtomsOfTheLJ);
        final List<Function> leftAtoms = decomposition._1();
        final List<Function> rightAtoms = decomposition._2();

        /*
         * TODO: explain why we just care about the right
         */
        AtomClassification rightSubAtomClassification = new AtomClassification(rightAtoms, datalogFactory, datalogTools);

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                rightSubAtomClassification.booleanAtoms);

        LeftJoinNode ljNode = queryBuilder.getFactory().createLeftJoinNode(optionalFilterCondition);
        queryBuilder.addChild(parentNodeOfTheLJ, ljNode, optionalPosition);

        /*
         * Adds the left part
         */
        queryBuilder = convertJoinAtom(queryBuilder, ljNode, leftAtoms, LEFT_POSITION, tablePredicates);

        /*
         * Adds the right part
         */
        return convertDataOrCompositeAtoms(rightSubAtomClassification.dataAndCompositeAtoms, queryBuilder, ljNode,
                RIGHT_POSITION, tablePredicates);
    }

    /**
     * TODO: explain
     *
     */
    private IntermediateQueryBuilder convertJoinAtom(IntermediateQueryBuilder queryBuilder,
                                                            QueryNode parentNodeOfTheJoinNode,
                                                            List<Function> subAtomsOfTheJoin,
                                                            Optional<ArgumentPosition> optionalPosition,
                                                            Collection<Predicate> tablePredicates)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        AtomClassification classification = new AtomClassification(subAtomsOfTheJoin, datalogFactory, datalogTools);
        if (classification.optionalGroupAtom.isPresent()) {
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("GROUP atom found inside a LJ meta-atom");
        }

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                classification.booleanAtoms);

        if (classification.dataAndCompositeAtoms.isEmpty()) {
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Empty join found");
        }
        /*
         * May happen because this method can also be called after the LJ conversion
         */
        else if (classification.dataAndCompositeAtoms.length() == 1) {
            if (optionalFilterCondition.isPresent()) {
                FilterNode filterNode = queryBuilder.getFactory().createFilterNode(optionalFilterCondition.get());
                queryBuilder.addChild(parentNodeOfTheJoinNode, filterNode, optionalPosition);

                return convertDataOrCompositeAtoms(classification.dataAndCompositeAtoms, queryBuilder, filterNode,
                        NO_POSITION, tablePredicates);
            }
            /*
             * Otherwise, no need for intermediate query node.
             */
            else {
                return convertDataOrCompositeAtoms(classification.dataAndCompositeAtoms, queryBuilder,
                        parentNodeOfTheJoinNode, optionalPosition, tablePredicates);
            }
        }
        /*
         * Normal case
         */
        else {
            InnerJoinNode joinNode = queryBuilder.getFactory().createInnerJoinNode(optionalFilterCondition);
            queryBuilder.addChild(parentNodeOfTheJoinNode, joinNode, optionalPosition);

            /*
             * Indirect recursive call for composite atoms
             */
            return convertDataOrCompositeAtoms(classification.dataAndCompositeAtoms, queryBuilder, joinNode, NO_POSITION,
                    tablePredicates);
        }

    }

}
