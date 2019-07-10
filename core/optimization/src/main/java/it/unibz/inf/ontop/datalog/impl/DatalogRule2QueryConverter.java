package it.unibz.inf.ontop.datalog.impl;

import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import fj.P2;
import fj.data.List;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

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

        protected AtomClassification(List<Function> atoms, DatalogTools datalogTools)
                throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
            dataAndCompositeAtoms = datalogTools.filterDataAndCompositeAtoms(atoms);
            List<Function> otherAtoms = datalogTools.filterNonDataAndCompositeAtoms(atoms);
            booleanAtoms = datalogTools.filterBooleanAtoms(otherAtoms);

            /*
             * May throw a NotSupportedConversionException
             */
            checkNonDataOrCompositeAtomSupport(otherAtoms, booleanAtoms);
        }

        /**
         * All the other atoms are currently presumed to be boolean atoms
         */
        private static void checkNonDataOrCompositeAtomSupport(List<Function> otherAtoms,
                                                               List<Function> booleanAtoms)
                throws DatalogProgram2QueryConverterImpl.NotSupportedConversionException {

            if (booleanAtoms.length() < otherAtoms.length()) {
                HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
                unsupportedAtoms.removeAll(booleanAtoms.toCollection());

                throw new DatalogProgram2QueryConverterImpl.NotSupportedConversionException(
                        "Conversion of the following atoms to the intermediate query is not (yet) supported: "
                                + unsupportedAtoms);
            }
        }

    }

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

    public IQ convertDatalogRule(CQIE datalogRule, IntermediateQueryFactory iqFactory)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        TargetAtom targetAtom = datalogConversionTools.convertFromDatalogDataAtom(datalogRule.getHead());

        DistinctVariableOnlyDataAtom projectionAtom = targetAtom.getProjectionAtom();

        ConstructionNode topConstructionNode = iqFactory.createConstructionNode(projectionAtom.getVariables(),
                targetAtom.getSubstitution());

        List<Function> bodyAtoms = List.iterableList(datalogRule.getBody());
        if (bodyAtoms.isEmpty()) {
            return createFact(topConstructionNode, projectionAtom, iqFactory);
        }
        else {
            return createDefinition(topConstructionNode, projectionAtom, bodyAtoms, iqFactory);
        }
    }

    private static IQ createFact(ConstructionNode topConstructionNode,
                                 DistinctVariableOnlyDataAtom projectionAtom, IntermediateQueryFactory iqFactory) {
        IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, iqFactory.createTrueNode());
        return iqFactory.createIQ(projectionAtom, constructionTree);
    }


    private IQ createDefinition(ConstructionNode topConstructionNode,
                                DistinctVariableOnlyDataAtom projectionAtom,
                                List<Function> bodyAtoms,
                                IntermediateQueryFactory iqFactory)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        try {
            IQTree bodyTree = convertAtoms(bodyAtoms, iqFactory);
            IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, bodyTree);

            return iqFactory.createIQ(projectionAtom, constructionTree);
        }
        catch (IntermediateQueryBuilderException e) {
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException(e.getMessage());
        }
    }

    private Optional<ImmutableExpression> createFilterExpression(List<Function> booleanAtoms) {
        if (booleanAtoms.isEmpty())
            return Optional.empty();
        return Optional.of(termFactory.getImmutableExpression(datalogTools.foldBooleanConditions(booleanAtoms)));
    }

    /**
     * TODO: describe
     */
    private IQTree convertDataOrCompositeAtom(final Function atom,
                                              IntermediateQueryFactory iqFactory)
            throws IntermediateQueryBuilderException, DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
        /*
         * If the atom is composite, extracts sub atoms
         */
        if (atom.isAlgebraFunction()) {
            List<Function> subAtoms = List.iterableList(
                    (java.util.List<Function>)(java.util.List<?>)atom.getTerms());

            Predicate atomPredicate = atom.getFunctionSymbol();
            if (atomPredicate.equals(datalogFactory.getSparqlJoinPredicate())) {
                return convertAtoms(subAtoms, iqFactory);
            }
            else if(atomPredicate.equals(datalogFactory.getSparqlLeftJoinPredicate())) {
                return convertLeftJoinAtom(subAtoms, iqFactory);
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
            return iqFactory.createIntensionalDataNode(dataAtom);
        }
        else {
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Unsupported non-data atom: " + atom);
        }
    }

    private IQTree convertLeftJoinAtom(List<Function> subAtomsOfTheLJ,
                                       IntermediateQueryFactory iqFactory)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        P2<List<Function>, List<Function>> decomposition = pullOutEqualityNormalizer.splitLeftJoinSubAtoms(subAtomsOfTheLJ);
        final List<Function> leftAtoms = decomposition._1();
        final List<Function> rightAtoms = decomposition._2();

        /*
         * TODO: explain why we just care about the right
         */
        AtomClassification rightSubAtomClassification = new AtomClassification(rightAtoms, datalogTools);

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                rightSubAtomClassification.booleanAtoms);

        LeftJoinNode ljNode = iqFactory.createLeftJoinNode(optionalFilterCondition);

        IQTree leftTree = convertAtoms(leftAtoms, iqFactory);
        IQTree rightTree = convertAtoms(rightSubAtomClassification.dataAndCompositeAtoms,
                iqFactory);
        return iqFactory.createBinaryNonCommutativeIQTree(ljNode, leftTree, rightTree);
    }

    /**
     * TODO: explain
     *
     */
    private IQTree convertAtoms(List<Function> atoms,
                                IntermediateQueryFactory iqFactory)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        AtomClassification classification = new AtomClassification(atoms, datalogTools);

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                classification.booleanAtoms);

        if (classification.dataAndCompositeAtoms.isEmpty()) {
            return optionalFilterCondition
                    .map(ImmutableFunctionalTerm::getVariables)
                    .map(iqFactory::createEmptyNode)
                    .orElseGet(() -> iqFactory.createEmptyNode(ImmutableSet.of()));
        }
        /*
         * May happen because this method can also be called after the LJ conversion
         */
        else if (classification.dataAndCompositeAtoms.length() == 1) {
            if (optionalFilterCondition.isPresent()) {
                FilterNode filterNode = iqFactory.createFilterNode(optionalFilterCondition.get());
                IQTree childTree = convertDataOrCompositeAtom(classification.dataAndCompositeAtoms.index(0),
                        iqFactory);

                return iqFactory.createUnaryIQTree(filterNode, childTree);
            }
            /*
             * Otherwise, no need for intermediate query node.
             */
            else {
                return convertDataOrCompositeAtom(classification.dataAndCompositeAtoms.index(0), iqFactory);
            }
        }
        /*
         * Normal case
         */
        else {
            InnerJoinNode joinNode = iqFactory.createInnerJoinNode(optionalFilterCondition);

            /*
             * Indirect recursive call for composite atoms
             */
            ImmutableList<IQTree> children = classification.dataAndCompositeAtoms.toJavaList().stream()
                    .map(a -> convertDataOrCompositeAtom(a, iqFactory))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(joinNode, children);
        }

    }

}
