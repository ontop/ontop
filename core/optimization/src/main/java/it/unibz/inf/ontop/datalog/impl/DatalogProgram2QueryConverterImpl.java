package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import fj.P2;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.IntermediateQueryBuilderException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIntensionalQueryMerger;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Converts a datalog program into an intermediate query
 */
public class DatalogProgram2QueryConverterImpl implements DatalogProgram2QueryConverter {

    private final IntermediateQueryFactory iqFactory;
    private final UnionBasedQueryMerger queryMerger;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final QueryTransformerFactory transformerFactory;

    private final TermFactory termFactory;
    private final DatalogFactory datalogFactory;
    private final DatalogConversionTools datalogConversionTools;
    private final DatalogTools datalogTools;
    private final PullOutEqualityNormalizer pullOutEqualityNormalizer;

    @Inject
    private DatalogProgram2QueryConverterImpl(IntermediateQueryFactory iqFactory,
                                              UnionBasedQueryMerger queryMerger,
                                              SubstitutionFactory substitutionFactory,
                                              CoreUtilsFactory coreUtilsFactory,
                                              QueryTransformerFactory transformerFactory,
                                              TermFactory termFactory,
                                              DatalogFactory datalogFactory,
                                              DatalogConversionTools datalogConversionTools,
                                              DatalogTools datalogTools,
                                              PullOutEqualityNormalizer pullOutEqualityNormalizer) {
        this.iqFactory = iqFactory;
        this.queryMerger = queryMerger;
        this.termFactory = termFactory;
        this.datalogFactory = datalogFactory;
        this.datalogConversionTools = datalogConversionTools;
        this.datalogTools = datalogTools;
        this.pullOutEqualityNormalizer = pullOutEqualityNormalizer;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.transformerFactory = transformerFactory;
    }



    private static final Optional<ImmutableQueryModifiers> NO_QUERY_MODIFIER = Optional.empty();

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
    public static class InvalidDatalogProgramException extends OntopInternalBugException {
        public InvalidDatalogProgramException(String message) {
            super(message);
        }
    }

    @Override
    public IQ convertDatalogProgram(DatalogProgram queryProgram,
                                    ImmutableList<Variable> signature) throws EmptyQueryException {

        List<CQIE> rules = queryProgram.getRules();

        DatalogDependencyGraphGenerator dependencyGraph = new DatalogDependencyGraphGenerator(rules);
        List<Predicate> topDownPredicates = Lists.reverse(dependencyGraph.getPredicatesInBottomUp());

        if (topDownPredicates.isEmpty())
            throw new EmptyQueryException();


        Predicate rootPredicate = topDownPredicates.get(0);

        Multimap<Predicate, CQIE> ruleIndex = dependencyGraph.getRuleIndex();

        Optional<ImmutableQueryModifiers> topQueryModifiers = convertModifiers(queryProgram.getQueryModifiers());

        /*
         * TODO: explain
         */
        // Non-final
        IQ iq = convertDatalogDefinitions(rootPredicate, ruleIndex, topQueryModifiers).get();

        /*
         * Rules (sub-queries)
         */
        for (int j = 1; j < topDownPredicates.size() ; j++) {
            Predicate datalogAtomPredicate  = topDownPredicates.get(j);
            Optional<IQ> optionalSubQuery = convertDatalogDefinitions(datalogAtomPredicate,
                    ruleIndex, NO_QUERY_MODIFIER);
            if (optionalSubQuery.isPresent()) {

                IntensionalQueryMerger intensionalQueryMerger = new IntensionalQueryMerger(
                        ImmutableMap.of(datalogAtomPredicate, optionalSubQuery.get()));
                iq = intensionalQueryMerger.optimize(iq);
            }
        }

        return enforceSignature(iq, signature);
    }

    /**
     * Hacked logic: because of ORDER conditions that are expected to use signature variables,
     * this method DOES NOT look for conflicts between signature variables and variables only appearing in the sub-tree.
     *
     * See the history for a better logic breaking this ugly hack.
     *
     * TODO: after getting rid of Datalog for encoding SPARQL queries, could try to clean it
     */
    private IQ enforceSignature(IQ iq, ImmutableList<Variable> signature) {

        ImmutableList<Variable> projectedVariables = iq.getProjectionAtom().getArguments();

        if (projectedVariables.equals(signature))
            return iq;

        if (projectedVariables.size() != signature.size())
            throw new IllegalArgumentException("The arity of the signature does not match the iq");

        InjectiveVar2VarSubstitution renamingSubstitution = substitutionFactory.getInjectiveVar2VarSubstitution(
                IntStream.range(0, projectedVariables.size())
                        .boxed()
                        .map(i -> Maps.immutableEntry(projectedVariables.get(i), signature.get(i)))
                        .filter(e -> !e.getKey().equals(e.getValue()))
                        .collect(ImmutableCollectors.toMap()));

        return transformerFactory.createRenamer(renamingSubstitution)
                .transform(iq);
    }


    /**
     * TODO: explain and comment
     */
    private Optional<IQ> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                                  Multimap<Predicate, CQIE> datalogRuleIndex,
                                                  Optional<ImmutableQueryModifiers> optionalModifiers)
            throws InvalidDatalogProgramException {

        Collection<CQIE> atomDefinitions = datalogRuleIndex.get(datalogAtomPredicate);

        ImmutableList<IQ> convertedDefinitions = atomDefinitions.stream()
                .map(d -> convertDatalogRule(d, iqFactory))
                .collect(ImmutableCollectors.toList());

        return optionalModifiers.isPresent()
                ? queryMerger.mergeDefinitions(convertedDefinitions, optionalModifiers.get())
                : queryMerger.mergeDefinitions(convertedDefinitions);
    }

    /**
     * TODO: explain
     */
    private static Optional<ImmutableQueryModifiers> convertModifiers(MutableQueryModifiers queryModifiers) {
        if (queryModifiers.hasModifiers()) {
            ImmutableQueryModifiers immutableQueryModifiers = new ImmutableQueryModifiersImpl(queryModifiers);
            return Optional.of(immutableQueryModifiers);
        } else {
            return Optional.empty();
        }
    }


    private class IntensionalQueryMerger extends AbstractIntensionalQueryMerger {

        private final ImmutableMap<Predicate, IQ> map;

        private IntensionalQueryMerger(ImmutableMap<Predicate, IQ> map) {
            super(DatalogProgram2QueryConverterImpl.this.iqFactory);
            this.map = map;
        }

        @Override
        protected QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables) {
            return new DatalogQueryMergingTransformer(coreUtilsFactory.createVariableGenerator(knownVariables));
        }

        private class DatalogQueryMergingTransformer extends AbstractIntensionalQueryMerger.QueryMergingTransformer {

            protected DatalogQueryMergingTransformer(VariableGenerator variableGenerator) {
                super(variableGenerator, DatalogProgram2QueryConverterImpl.this.iqFactory, substitutionFactory, transformerFactory);
            }

            @Override
            protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
                return Optional.ofNullable(map.get(dataNode.getProjectionAtom().getPredicate()));
            }

            @Override
            protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
                return dataNode;
            }
        }
    }






/**
 * Converts a Datalog rule into an intermediate query.
 *
 * Note here List are from Functional Java, not java.util.List.
 */

    /**
     * TODO: explain
     */
    private static class AtomClassification {
        private final fj.data.List<Function> dataAndCompositeAtoms;
        private final fj.data.List<Function> booleanAtoms;

        protected AtomClassification(fj.data.List<Function> atoms, DatalogTools datalogTools)
                throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {
            dataAndCompositeAtoms = datalogTools.filterDataAndCompositeAtoms(atoms);
            fj.data.List<Function> otherAtoms = datalogTools.filterNonDataAndCompositeAtoms(atoms);
            booleanAtoms = datalogTools.filterBooleanAtoms(otherAtoms);

            if (booleanAtoms.length() < otherAtoms.length()) {
                HashSet<Function> unsupportedAtoms = new HashSet<>(otherAtoms.toCollection());
                unsupportedAtoms.removeAll(booleanAtoms.toCollection());

                throw new DatalogProgram2QueryConverterImpl.NotSupportedConversionException(
                        "Conversion of the following atoms to the intermediate query is not (yet) supported: "
                                + unsupportedAtoms);
            }
        }
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

        fj.data.List<Function> bodyAtoms = fj.data.List.iterableList(datalogRule.getBody());
        try {
            IQTree bodyTree = bodyAtoms.isEmpty() ? iqFactory.createTrueNode() : convertAtoms(bodyAtoms, iqFactory);
            IQTree constructionTree = iqFactory.createUnaryIQTree(topConstructionNode, bodyTree);
            return iqFactory.createIQ(projectionAtom, constructionTree);
        }
        catch (IntermediateQueryBuilderException e) {
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException(e.getMessage());
        }
    }

    private Optional<ImmutableExpression> createFilterExpression(fj.data.List<Function> booleanAtoms) {
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

        // If the atom is composite, extracts sub atoms
        if (atom.isAlgebraFunction()) {
            fj.data.List<Function> subAtoms = fj.data.List.iterableList(
                    (java.util.List<Function>)(java.util.List<?>)atom.getTerms());

            Predicate atomPredicate = atom.getFunctionSymbol();
            if (atomPredicate.equals(datalogFactory.getSparqlJoinPredicate())) {
                return convertAtoms(subAtoms, iqFactory);
            }
            else if (atomPredicate.equals(datalogFactory.getSparqlLeftJoinPredicate())) {
                return convertLeftJoinAtom(subAtoms, iqFactory);
            }
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Unsupported predicate: " + atomPredicate);
        }
        // Data atom: creates a DataNode and adds it to the tree
        else if (atom.isDataFunction()) {
            TargetAtom targetAtom = datalogConversionTools.convertFromDatalogDataAtom(atom);
            ImmutableSubstitution<ImmutableTerm> bindings = targetAtom.getSubstitution();
            DataAtom dataAtom = bindings.applyToDataAtom(targetAtom.getProjectionAtom());
            return iqFactory.createIntensionalDataNode(dataAtom);
        }
        throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("Unsupported non-data atom: " + atom);
    }

    private IQTree convertLeftJoinAtom(fj.data.List<Function> subAtomsOfTheLJ,
                                       IntermediateQueryFactory iqFactory)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, IntermediateQueryBuilderException {

        P2<fj.data.List<Function>, fj.data.List<Function>> decomposition = pullOutEqualityNormalizer.splitLeftJoinSubAtoms(subAtomsOfTheLJ);
        final fj.data.List<Function> leftAtoms = decomposition._1();
        final fj.data.List<Function> rightAtoms = decomposition._2();

        /*
         * TODO: explain why we just care about the right
         */
        AtomClassification rightSubAtomClassification = new AtomClassification(rightAtoms, datalogTools);

        Optional<ImmutableExpression> optionalFilterCondition = createFilterExpression(
                rightSubAtomClassification.booleanAtoms);

        LeftJoinNode ljNode = iqFactory.createLeftJoinNode(optionalFilterCondition);

        IQTree leftTree = convertAtoms(leftAtoms, iqFactory);
        IQTree rightTree = convertAtoms(rightSubAtomClassification.dataAndCompositeAtoms, iqFactory);
        return iqFactory.createBinaryNonCommutativeIQTree(ljNode, leftTree, rightTree);
    }

    /**
     * TODO: explain
     */
    private IQTree convertAtoms(fj.data.List<Function> atoms, IntermediateQueryFactory iqFactory)
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
        // May happen because this method can also be called after the LJ conversion
        else if (classification.dataAndCompositeAtoms.length() == 1) {
            if (optionalFilterCondition.isPresent()) {
                FilterNode filterNode = iqFactory.createFilterNode(optionalFilterCondition.get());
                IQTree childTree = convertDataOrCompositeAtom(classification.dataAndCompositeAtoms.index(0),
                        iqFactory);

                return iqFactory.createUnaryIQTree(filterNode, childTree);
            }
            else {
                // no need for intermediate query node.
                return convertDataOrCompositeAtom(classification.dataAndCompositeAtoms.index(0), iqFactory);
            }
        }
        else {
            // Normal case
            InnerJoinNode joinNode = iqFactory.createInnerJoinNode(optionalFilterCondition);

            // Indirect recursive call for composite atoms
            ImmutableList<IQTree> children = classification.dataAndCompositeAtoms.toJavaList().stream()
                    .map(a -> convertDataOrCompositeAtom(a, iqFactory))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(joinNode, children);
        }
    }
}
