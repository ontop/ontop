package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.iq.optimizer.OrderBySimplifier;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
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

public class OrderBySimplifierImpl extends AbstractIQOptimizer implements OrderBySimplifier {

    private final OptimizationSingletons optimizationSingletons;
    private final IQTreeTools iqTreeTools;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final ImmutableSet<RDFDatatype> nonLexicallyOrderedDatatypes;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    protected OrderBySimplifierImpl(OptimizationSingletons optimizationSingletons) {
        // no equality check
        super(optimizationSingletons.getCoreSingletons().getIQFactory());
        this.optimizationSingletons = optimizationSingletons;
        CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
        this.termFactory = coreSingletons.getTermFactory();
        this.typeFactory = coreSingletons.getTypeFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.nonLexicallyOrderedDatatypes = ImmutableSet.of(typeFactory.getAbstractOntopNumericDatatype(),
                typeFactory.getXsdBooleanDatatype(), typeFactory.getXsdDatetimeDatatype());

        this.transformer = IQTreeVariableGeneratorTransformer.of(OrderBySimplifyingTransformer::new);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    private class OrderBySimplifyingTransformer extends RDFTypeDependentSimplifyingTransformer {

        OrderBySimplifyingTransformer(VariableGenerator variableGenerator) {
            super(optimizationSingletons, variableGenerator);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {

            ImmutableList<ComparatorSimplification> simplifications = rootNode.getComparators().stream()
                    .flatMap(c -> simplifyComparator(c, child))
                    .collect(ImmutableCollectors.toList());

            if (simplifications.isEmpty())
                return child;

            Stream<DefinitionPushDownRequest> definitionsToPushDown = simplifications.stream()
                    .map(s -> s.request)
                    .flatMap(Optional::stream);

            IQTree pushDownChildTree = pushDownDefinitions(child, definitionsToPushDown);

            ImmutableList<OrderByNode.OrderComparator> newComparators = simplifications.stream()
                    .map(s -> s.newComparator)
                    .collect(ImmutableCollectors.toList());

            // Makes sure no new variable is projected by the returned tree
            return iqTreeTools.unaryIQTreeBuilder(child.getVariables())
                    .append(iqFactory.createOrderByNode(newComparators))
                    .build(transformChild(pushDownChildTree));
        }

        private Stream<ComparatorSimplification> simplifyComparator(OrderByNode.OrderComparator comparator,
                                                                         IQTree child) {
            ImmutableTerm term = comparator.getTerm().simplify();
            if (term instanceof Constant)
                return Stream.empty();

            if ((term instanceof ImmutableFunctionalTerm)
                    && ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
                ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;

                return new RDFTermSimplification(
                        child,
                        comparator.isAscending(),
                        functionalTerm.getTerm(0),
                        unwrapIfElseNull(functionalTerm.getTerm(1))).simplifyRDFTerm();
            }
            else
                return Stream.of(new ComparatorSimplification(comparator));
        }

        private class RDFTermSimplification {
            private final IQTree childTree;
            private final boolean isAscending;
            private final ImmutableTerm lexicalTerm;
            private final ImmutableTerm rdfTypeTerm;

            private RDFTermSimplification(IQTree childTree, boolean isAscending, ImmutableTerm lexicalTerm, ImmutableTerm rdfTypeTerm) {
                this.childTree = childTree;
                this.isAscending = isAscending;
                this.lexicalTerm = lexicalTerm;
                this.rdfTypeTerm = rdfTypeTerm;
            }

            private Stream<ComparatorSimplification> simplifyRDFTerm() {

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
                            : computeDBTerm(possibleType).stream()
                            .map(this::createOrderComparator)
                            .map(ComparatorSimplification::new);
                }

                /*
                 * Possibly multi-typed case
                 */
                return possibleTypes
                        // All types extracted
                        .map(this::computeSimplifications)
                        // Cannot extract all the types --> postpone
                        .orElseGet(() -> Stream.of(new ComparatorSimplification(
                                createOrderComparator(
                                        (NonGroundTerm) termFactory.getRDFFunctionalTerm(lexicalTerm, rdfTypeTerm)))));
            }


            /**
             * SPARQL ascending order: UNBOUND (NULL), Bnode, IRI and literals.
             *
             * The order between literals is partially defined (between comparable datatypes)
             *
             * Here
             *
             */
            private Stream<ComparatorSimplification> computeSimplifications(ImmutableSet<RDFTermType> possibleTypes) {
                java.util.function.Function<RDFTermType, Optional<ComparatorSimplification>> fct =
                        t -> computeSimplification(possibleTypes, t, t);

                return Stream.of(
                                fct.apply(typeFactory.getXsdDatetimeDatatype()),
                                fct.apply(typeFactory.getXsdBooleanDatatype()),
                                computeSimplification(possibleTypes, typeFactory.getAbstractOntopNumericDatatype(),
                                        // TODO: improve it
                                        typeFactory.getXsdDoubleDatatype()),
                                computeOtherLiteralSimplification(possibleTypes),
                                fct.apply(typeFactory.getIRITermType()),
                                fct.apply(typeFactory.getBlankNodeType()))
                        .flatMap(Optional::stream);
            }

            private Optional<ComparatorSimplification> computeSimplification(ImmutableSet<RDFTermType> possibleTypes,
                                                                             RDFTermType type, RDFTermType referenceCastType) {
                return possibleTypes.stream()
                        .filter(t -> t.isA(type))
                        .findAny()
                        .flatMap(t -> computeSimplificationForSelectedType(referenceCastType, termFactory.getIsAExpression(rdfTypeTerm, type)));
            }

            private Optional<ComparatorSimplification> computeOtherLiteralSimplification(ImmutableSet<RDFTermType> possibleTypes) {
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
                        .flatMap(c -> computeSimplificationForSelectedType(typeFactory.getXsdStringDatatype(), c));
            }

            private Optional<ComparatorSimplification> computeSimplificationForSelectedType(RDFTermType referenceCastType,
                                                                                            ImmutableExpression condition) {
                Variable v = variableGenerator.generateNewVariable();
                return computeDBTerm(referenceCastType)
                        .map(t -> new ComparatorSimplification(
                                createOrderComparator(v),
                                DefinitionPushDownRequest.create(v, t, condition)));
            }

            private Optional<NonGroundTerm> computeDBTerm(RDFTermType rdfType) {
                ImmutableTerm orderTerm = termFactory.getConversionFromRDFLexical2DB(lexicalTerm, rdfType)
                        .simplify(childTree.getVariableNullability());

                return orderTerm.isGround()
                        ? Optional.empty()
                        : Optional.of((NonGroundTerm) orderTerm);
            }

            OrderByNode.OrderComparator createOrderComparator(NonGroundTerm term) {
                return iqFactory.createOrderComparator(term, isAscending);
            }
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class ComparatorSimplification {
        private final OrderByNode.OrderComparator newComparator;
        private final Optional<DefinitionPushDownRequest> request;

        ComparatorSimplification(OrderByNode.OrderComparator newComparator, DefinitionPushDownRequest request) {
            this.newComparator = newComparator;
            this.request = Optional.of(request);
        }

        ComparatorSimplification(OrderByNode.OrderComparator newComparator) {
            this.newComparator = newComparator;
            this.request = Optional.empty();
        }
    }

}
