package it.unibz.inf.ontop.spec.mapping.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.spec.mapping.type.TermTypeExtractor;

import java.util.Optional;
import java.util.stream.IntStream;

public class BasicTermTypeExtractor implements TermTypeExtractor {

    @Inject
    private BasicTermTypeExtractor() {
    }

    @Override
    public Optional<TermType> extractType(ImmutableTerm term, IQTree subTree) {
        return (term instanceof Variable)
                ? extractTypeFromVariable((Variable)term, subTree)
                : extractType((NonVariableTerm) term);
    }

    private Optional<TermType> extractTypeFromVariable(Variable variable, IQTree subTree) {
        return subTree.acceptVisitor(new TermTypeVariableVisitor(variable, this));
    }

    /**
     * At the moment, we only extract types from:
     *    - ground terms
     *    - non ground functional terms that are able to define their target type independently
     *      of the children variable types
     */
    private Optional<TermType> extractType(NonVariableTerm nonVariableTerm) {
        return nonVariableTerm.inferType()
                // TODO: shall we care here about non fatal error?
                .flatMap(TermTypeInference::getTermType);
    }


    protected static class TermTypeVariableVisitor implements IQVisitor<Optional<TermType>> {

        protected final Variable variable;
        protected final TermTypeExtractor typeExtractor;

        protected TermTypeVariableVisitor(Variable variable, TermTypeExtractor typeExtractor) {
            this.variable = variable;
            this.typeExtractor = typeExtractor;
        }

        @Override
        public Optional<TermType> visitIntensionalData(IntensionalDataNode dataNode) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitExtensionalData(ExtensionalDataNode dataNode) {
            DataAtom<RelationPredicate> projectionAtom = dataNode.getProjectionAtom();
            RelationDefinition relationDefinition = projectionAtom.getPredicate().getRelationDefinition();

            ImmutableList<? extends VariableOrGroundTerm> arguments = projectionAtom.getArguments();

            return IntStream.range(0, arguments.size())
                    .filter(i -> arguments.get(i).equals(variable))
                    .boxed()
                    .map(i -> relationDefinition.getAttribute(i + 1))
                    .map(Attribute::getTermType)
                    .map(t -> (TermType) t)
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
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitNonStandardLeafNode(LeafIQTree leafNode) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitConstruction(ConstructionNode rootNode, IQTree child) {
            return typeExtractor.extractType(rootNode.getSubstitution().apply(variable), child);
        }

        @Override
        public Optional<TermType> visitFilter(FilterNode rootNode, IQTree child) {
            return child.acceptVisitor(this);
        }

        @Override
        public Optional<TermType> visitDistinct(DistinctNode rootNode, IQTree child) {
            return child.acceptVisitor(this);        }

        @Override
        public Optional<TermType> visitSlice(SliceNode sliceNode, IQTree child) {
            return child.acceptVisitor(this);        }

        @Override
        public Optional<TermType> visitOrderBy(OrderByNode rootNode, IQTree child) {
            return child.acceptVisitor(this);        }

        @Override
        public Optional<TermType> visitNonStandardUnaryNode(UnaryOperatorNode rootNode, IQTree child) {
            return Optional.empty();
        }

        /**
         * TODO: implement seriously
         */
        @Override
        public Optional<TermType> visitLeftJoin(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitNonStandardBinaryNonCommutativeNode(BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return children.stream()
                    .map(c -> c.acceptVisitor(this))
                    .findAny()
                    .orElse(Optional.empty());
        }

        /**
         * TODO: implement seriously
         */
        @Override
        public Optional<TermType> visitUnion(UnionNode rootNode, ImmutableList<IQTree> children) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> visitNonStandardNaryNode(NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return Optional.empty();
        }
    }
}
