package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

@Singleton
public class BasicSingleTermTypeExtractor implements SingleTermTypeExtractor {

    private final TermFactory termFactory;
    @Inject
    private BasicSingleTermTypeExtractor(TermFactory termFactory) {
        this.termFactory = termFactory;
    }

    @Override
    public Optional<TermType> extractSingleTermType(ImmutableTerm term, IQTree subTree) {
        return (term instanceof Variable)
                ? extractTypeFromVariable((Variable)term, subTree)
                : extractType((NonVariableTerm) term, subTree);
    }

    private Optional<TermType> extractTypeFromVariable(Variable variable, IQTree subTree) {
        return subTree.acceptVisitor(new TermTypeVariableVisitor(variable, this));
    }

    private Optional<TermType> extractType(NonVariableTerm nonVariableTerm, IQTree subTree) {
        return nonVariableTerm.inferType()
                .or(() -> inferByInjectingSubTermType(nonVariableTerm, subTree))
                .flatMap(i -> i.getTermType()
                        .or(() -> i.getRedirectionVariable()
                                .flatMap(v -> extractTypeFromVariable(v, subTree))));
    }

    private Optional<TermTypeInference> inferByInjectingSubTermType(NonVariableTerm nonVariableTerm,
                                                                    IQTree subTree) {
        if (!((nonVariableTerm instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) nonVariableTerm).getFunctionSymbol().canDeriveTypeFromInputTypes()))
            return Optional.empty();

        ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) nonVariableTerm;
        ImmutableList<? extends ImmutableTerm> terms = functionalTerm.getTerms();

        ImmutableList<? extends ImmutableTerm> newTerms = functionalTerm.getTerms().stream()
                .map(t -> extractSingleTermType(t, subTree)
                        .filter(tp -> tp instanceof DBTermType)
                        // Casts it to its own type as a way to convey the type
                        .<ImmutableTerm>map(tp -> termFactory.getDBCastFunctionalTerm((DBTermType) tp, t))
                        .orElse(t))
                .collect(ImmutableCollectors.toList());

        if (terms.equals(newTerms))
            return Optional.empty();

        return termFactory.getImmutableFunctionalTerm(functionalTerm.getFunctionSymbol(), newTerms)
                .inferType();
    }


    protected static class TermTypeVariableVisitor implements IQVisitor<Optional<TermType>> {

        protected final Variable variable;
        protected final SingleTermTypeExtractor typeExtractor;

        protected TermTypeVariableVisitor(Variable variable, SingleTermTypeExtractor typeExtractor) {
            this.variable = variable;
            this.typeExtractor = typeExtractor;
        }

        @Override
        public Optional<TermType> visitIntensionalData(IntensionalDataNode dataNode) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitExtensionalData(ExtensionalDataNode dataNode) {
            RelationDefinition relationDefinition = dataNode.getRelationDefinition();

            return dataNode.getArgumentMap().entrySet().stream()
                    .filter(e -> e.getValue().equals(variable))
                    .map(e -> relationDefinition.getAttribute(e.getKey() + 1))
                    .map(Attribute::getTermType)
                    .map(o -> (TermType) o)
                    .filter(o -> !o.isAbstract())
                    .findAny();
        }

        @Override
        public Optional<TermType> visitEmpty(EmptyNode node) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitTrue(TrueNode node) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitNative(NativeNode nativeNode) {
            return Optional.ofNullable(nativeNode.getTypeMap().get(variable));
        }

        @Override
        public Optional<TermType> visitValues(ValuesNode valuesNode) {
            ImmutableSet<TermType> termTypes = valuesNode.getValueStream(variable)
                    .flatMap(c -> c.getOptionalType().stream())
                    .collect(ImmutableCollectors.toSet());

            return termTypes.stream()
                    .findAny();
        }

        @Override
        public Optional<TermType> visitNonStandardLeafNode(LeafIQTree leafNode) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitConstruction(ConstructionNode rootNode, IQTree child) {
            return visitExtendedProjection(rootNode, child);
        }

        @Override
        public Optional<TermType> visitAggregation(AggregationNode rootNode, IQTree child) {
            return visitExtendedProjection(rootNode, child);
        }

        protected Optional<TermType> visitExtendedProjection(ExtendedProjectionNode rootNode, IQTree child) {
            return typeExtractor.extractSingleTermType(rootNode.getSubstitution().apply(variable), child);
        }

        @Override
        public Optional<TermType> visitFilter(FilterNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Optional<TermType> visitFlatten(FlattenNode flattenNode, IQTree child) {
            if (variable.equals(flattenNode.getOutputVariable())) {
                /* We prefer to rely on the data type provided in the lens rather than the inferred one,
                   because it is more accurate than what we can obtain from the JDBC.
                 */
                return flattenNode.inferOutputType(Optional.of(flattenNode.getFlattenedType()));
            }
            if (flattenNode.getIndexVariable().isPresent() && variable.equals(flattenNode.getIndexVariable().get())) {
                return flattenNode.getIndexVariableType();
            }
            return child.acceptVisitor(this);
        }

        @Override
        public Optional<TermType> visitDistinct(DistinctNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Optional<TermType> visitSlice(SliceNode sliceNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Optional<TermType> visitOrderBy(OrderByNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Optional<TermType> visitNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
            return Optional.empty();
        }

        /**
         * Only consider the right child for right-specific variables
         */
        @Override
        public Optional<TermType> visitLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            if (leftChild.getVariables().contains(variable)) {
                return leftChild.acceptVisitor(this);
            }
            else if (rightChild.getVariables().contains(variable)) {
                return rightChild.acceptVisitor(this);
            }
            else
                return Optional.empty();
        }

        @Override
        public Optional<TermType> visitNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode,
                                                                           IQTree leftChild, IQTree rightChild) {
            return Optional.empty();
        }

        /**
         * Returns the first type found for a variable.
         *
         * If multiple types could have been found -- not matching -- will be later eliminated
         *
         */
        @Override
        public Optional<TermType> visitInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return children.stream()
                    .map(c -> c.acceptVisitor(this))
                    .filter(Optional::isPresent)
                    .findAny()
                    .orElse(Optional.empty());
        }

        @Override
        public Optional<TermType> visitUnion(UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableSet<TermType> termTypes = children.stream()
                    .map(c -> c.acceptVisitor(this))
                    .flatMap(Optional::stream)
                    .collect(ImmutableCollectors.toSet());

            // Picks arbitrarily one of them
            return termTypes.stream()
                    .findAny();
        }

        @Override
        public Optional<TermType> visitNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return Optional.empty();
        }
    }
}
