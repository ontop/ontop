package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.optimizer.OrderBySimplifier;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transformer.impl.RDFTypeDependentSimplifyingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class OrderBySimplifierImpl implements OrderBySimplifier {

    private final OptimizationSingletons optimizationSingletons;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected OrderBySimplifierImpl(OptimizationSingletons optimizationSingletons, IntermediateQueryFactory iqFactory) {
        this.optimizationSingletons = optimizationSingletons;
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTreeTransformer transformer = createTransformer(query.getVariableGenerator());
        IQTree newTree = transformer.transform(query.getTree());
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected IQTreeTransformer createTransformer(VariableGenerator variableGenerator) {
        return new OrderBySimplifyingTransformer(variableGenerator, optimizationSingletons);
    }


    protected static class OrderBySimplifyingTransformer extends RDFTypeDependentSimplifyingTransformer {

        protected final VariableGenerator variableGenerator;
        protected final TermFactory termFactory;
        protected final TypeFactory typeFactory;
        protected final ImmutableSet<RDFDatatype> nonLexicallyOrderedDatatypes;

        protected OrderBySimplifyingTransformer(VariableGenerator variableGenerator,
                                                OptimizationSingletons optimizationSingletons) {
            super(optimizationSingletons);
            this.variableGenerator = variableGenerator;
            CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
            this.termFactory = coreSingletons.getTermFactory();
            this.typeFactory = coreSingletons.getTypeFactory();
            this.nonLexicallyOrderedDatatypes = ImmutableSet.of(typeFactory.getAbstractOntopNumericDatatype(),
                    typeFactory.getXsdBooleanDatatype(), typeFactory.getXsdDatetimeDatatype());

        }

        @Override
        public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {

            ImmutableList<ComparatorSimplification> simplifications = rootNode.getComparators().stream()
                    .flatMap(c -> simplifyComparator(c, child))
                    .collect(ImmutableCollectors.toList());

            ImmutableList<OrderByNode.OrderComparator> newConditions = simplifications.stream()
                    .map(s -> s.newComparator)
                    .collect(ImmutableCollectors.toList());

            Stream<DefinitionPushDownRequest> definitionsToPushDown = simplifications.stream()
                    .map(s -> s.request)
                    .filter(Optional::isPresent)
                    .map(Optional::get);

            if (newConditions.isEmpty())
                return child;

            OrderByNode newNode = iqFactory.createOrderByNode(newConditions);

            IQTree pushDownChildTree = pushDownDefinitions(child, definitionsToPushDown);

            UnaryIQTree orderByTree = iqFactory.createUnaryIQTree(newNode, pushDownChildTree.acceptTransformer(this));

            // Makes sure no new variable is projected by the returned tree
            ImmutableSet<Variable> childVariables = child.getVariables();
            return pushDownChildTree.getVariables().equals(childVariables)
                    ? orderByTree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(childVariables),
                            orderByTree);
        }

        protected Stream<ComparatorSimplification> simplifyComparator(OrderByNode.OrderComparator comparator,
                                                                         IQTree child) {
            ImmutableTerm term = comparator.getTerm().simplify();
            if (term instanceof Constant)
                return Stream.empty();

            if ((term instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
                ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

                boolean isAscending = comparator.isAscending();

                return simplifyRDFTerm(
                        functionalTerm.getTerm(0),
                        unwrapIfElseNull(functionalTerm.getTerm(1)),
                        child,
                        isAscending);
            }
            else
                return Stream.of(new ComparatorSimplification(comparator));
        }

        protected Stream<ComparatorSimplification> simplifyRDFTerm(ImmutableTerm lexicalTerm, ImmutableTerm rdfTypeTerm,
                                                                   IQTree childTree, boolean isAscending) {


            Optional<ImmutableSet<RDFTermType>> possibleTypes = extractPossibleTypes(rdfTypeTerm, childTree);

            /*
             * Mono-typed case
             *
             * Either the type term is a constant or it is a functional term that either return NULL or always the same constant.
             *
             * If it is functional term, we can rely on the lexical term to handle the case where the type term is null,
             * as the type and lexical terms are expected to be "on-sync" (both nulls, or non is null)
             *
             */
            if (possibleTypes.isPresent() && possibleTypes.get().size() == 1) {
                RDFTermType possibleType = possibleTypes.get().iterator().next();

                return lexicalTerm.isGround()
                        ? Stream.empty()
                        : Stream.of(computeDBTerm((NonGroundTerm) lexicalTerm,
                                possibleType, childTree))
                            .map(t -> iqFactory.createOrderComparator(t, isAscending))
                            .map(ComparatorSimplification::new);
            }

            /*
             * Possibly multi-typed case
             */
            return possibleTypes
                    // All types extracted
                    .map(types -> computeSimplifications(lexicalTerm, rdfTypeTerm, types, childTree, isAscending))
                    // Cannot extract all the types --> postpone
                    .orElseGet(() -> Stream.of(new ComparatorSimplification(
                            iqFactory.createOrderComparator(
                                    (NonGroundTerm) termFactory.getRDFFunctionalTerm(lexicalTerm, rdfTypeTerm),
                                    isAscending))));
        }

        protected NonGroundTerm computeDBTerm(ImmutableTerm lexicalTerm, RDFTermType rdfType, IQTree childTree) {
            return (NonGroundTerm) termFactory.getConversionFromRDFLexical2DB(lexicalTerm, rdfType)
                    .simplify(childTree.getVariableNullability());
        }

        /**
         * SPARQL ascending order: UNBOUND (NULL), Bnode, IRI and literals.
         *
         * The order between literals is partially defined (between comparable datatypes)
         *
         * Here
         *
         */
        private Stream<ComparatorSimplification> computeSimplifications(ImmutableTerm lexicalTerm,
                                                                        ImmutableTerm rdfTypeTerm,
                                                                        ImmutableSet<RDFTermType> possibleTypes,
                                                                        IQTree childTree,
                                                                        boolean isAscending) {
            java.util.function.Function<RDFTermType, Optional<ComparatorSimplification>> fct =
                    t -> computeSimplification(lexicalTerm, rdfTypeTerm, possibleTypes, t, childTree, isAscending);

            return Stream.of(
                    fct.apply(typeFactory.getXsdDatetimeDatatype()),
                    fct.apply(typeFactory.getXsdBooleanDatatype()),
                    computeSimplification(lexicalTerm, rdfTypeTerm, possibleTypes, typeFactory.getAbstractOntopNumericDatatype(),
                            // TODO: improve it
                            typeFactory.getXsdDoubleDatatype(),childTree, isAscending),
                    computeOtherLiteralSimplification(lexicalTerm, rdfTypeTerm, possibleTypes, childTree, isAscending),
                    fct.apply(typeFactory.getIRITermType()),
                    fct.apply(typeFactory.getBlankNodeType()))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }

        private Optional<ComparatorSimplification> computeSimplification(ImmutableTerm lexicalTerm, ImmutableTerm rdfTypeTerm,
                                                                         ImmutableSet<RDFTermType> possibleTypes,
                                                                         RDFTermType type,
                                                                         IQTree childTree, boolean isAscending) {
            return computeSimplification(lexicalTerm, rdfTypeTerm, possibleTypes, type, type, childTree, isAscending);
        }

        private Optional<ComparatorSimplification> computeSimplification(ImmutableTerm lexicalTerm, ImmutableTerm rdfTypeTerm,
                                                                         ImmutableSet<RDFTermType> possibleTypes,
                                                                         RDFTermType type, RDFTermType referenceCastType,
                                                                         IQTree childTree, boolean isAscending) {
            return possibleTypes.stream()
                    .filter(t -> t.isA(type))
                    .findAny()
                    .map(t -> computeSimplificationForSelectedType(lexicalTerm, referenceCastType, childTree, isAscending,
                            termFactory.getIsAExpression(rdfTypeTerm, type)));
        }

        private ComparatorSimplification computeSimplificationForSelectedType(ImmutableTerm lexicalTerm,
                                                                              RDFTermType referenceCastType,
                                                                              IQTree childTree, boolean isAscending,
                                                                              ImmutableExpression condition) {
            Variable v = variableGenerator.generateNewVariable();

            DefinitionPushDownRequest request = DefinitionPushDownRequest.create(v,
                    computeDBTerm(lexicalTerm, referenceCastType, childTree),
                    condition);

            return new ComparatorSimplification(
                    iqFactory.createOrderComparator(v, isAscending),
                    request);
        }

        private Optional<ComparatorSimplification> computeOtherLiteralSimplification(ImmutableTerm lexicalTerm,
                                                                                     ImmutableTerm rdfTypeTerm,
                                                                                     ImmutableSet<RDFTermType> possibleTypes,
                                                                                     IQTree childTree, boolean isAscending) {
            RDFDatatype rdfsLiteral = typeFactory.getAbstractRDFSLiteral();

            return possibleTypes.stream()
                    .filter(t -> t.isA(rdfsLiteral))
                    .filter(t -> nonLexicallyOrderedDatatypes.stream()
                            .noneMatch(t::isA))
                    .findAny()
                    // Condition: is a literal but its datatype is different from the one in the "non-lexically ordered" set
                    .flatMap(t -> termFactory.getConjunction(Stream.concat(
                            Stream.of(termFactory.getIsAExpression(rdfTypeTerm, rdfsLiteral)),
                            nonLexicallyOrderedDatatypes.stream()
                                    .map(st -> termFactory.getIsAExpression(rdfTypeTerm, st))
                                    .map(e -> e.negate(termFactory))
                            )))
                    .map(c -> computeSimplificationForSelectedType(lexicalTerm, typeFactory.getXsdStringDatatype(),
                            childTree, isAscending, c));
        }

    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class ComparatorSimplification {
        protected final OrderByNode.OrderComparator newComparator;
        protected final Optional<DefinitionPushDownRequest> request;

        protected ComparatorSimplification(OrderByNode.OrderComparator newComparator, DefinitionPushDownRequest request) {
            this.newComparator = newComparator;
            this.request = Optional.of(request);
        }

        protected ComparatorSimplification(OrderByNode.OrderComparator newComparator) {
            this.newComparator = newComparator;
            this.request = Optional.empty();
        }
    }

}
