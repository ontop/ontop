package it.unibz.inf.ontop.iq.type.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.iq.visit.impl.AbstractIQVisitor;
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
        return subTree.acceptVisitor(new TermTypeVariableVisitor(variable));
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

        ImmutableList<ImmutableTerm> newTerms = functionalTerm.getTerms().stream()
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


    private class TermTypeVariableVisitor extends AbstractIQVisitor<Optional<TermType>> {

        private final Variable variable;

        private TermTypeVariableVisitor(Variable variable) {
            this.variable = variable;
        }

        @Override
        public Optional<TermType> transformIntensionalData(IntensionalDataNode dataNode) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> transformExtensionalData(ExtensionalDataNode dataNode) {
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
        public Optional<TermType> transformEmpty(EmptyNode node) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> transformTrue(TrueNode node) {
            return Optional.empty();
        }

        @Override
        public Optional<TermType> transformNative(NativeNode nativeNode) {
            return Optional.ofNullable(nativeNode.getTypeMap().get(variable));
        }

        @Override
        public Optional<TermType> transformValues(ValuesNode valuesNode) {
            ImmutableSet<TermType> termTypes = valuesNode.getValueStream(variable)
                    .map(Constant::getOptionalType)
                    .flatMap(Optional::stream)
                    .collect(ImmutableCollectors.toSet());

            return termTypes.stream()
                    .findAny();
        }

        @Override
        public Optional<TermType> transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            return extractSingleTermType(rootNode.getSubstitution().apply(variable), child);
        }

        @Override
        public Optional<TermType> transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            return extractSingleTermType(rootNode.getSubstitution().apply(variable), child);
        }

        @Override
        public Optional<TermType> transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            return transformChild(child);
        }

        @Override
        public Optional<TermType> transformFlatten(UnaryIQTree tree, FlattenNode flattenNode, IQTree child) {
            if (variable.equals(flattenNode.getOutputVariable())) {
                /* We prefer to rely on the data type provided in the lens rather than the inferred one,
                   because it is more accurate than what we can obtain from the JDBC.
                 */
                return flattenNode.inferOutputType(Optional.of(flattenNode.getFlattenedType()));
            }
            if (flattenNode.getIndexVariable().isPresent() && variable.equals(flattenNode.getIndexVariable().get())) {
                return flattenNode.getIndexVariableType();
            }
            return transformChild(child);
        }

        @Override
        public Optional<TermType> transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return transformChild(child);
        }

        @Override
        public Optional<TermType> transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            return transformChild(child);
        }

        @Override
        public Optional<TermType> transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return transformChild(child);
        }

        /**
         * Only consider the right child for right-specific variables
         */
        @Override
        public Optional<TermType> transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            if (leftChild.getVariables().contains(variable)) {
                return transformChild(leftChild);
            }
            else if (rightChild.getVariables().contains(variable)) {
                return transformChild(rightChild);
            }
            else
                return Optional.empty();
        }

        /**
         * Returns the first type found for a variable.
         *
         * If multiple types could have been found -- not matching -- will be later eliminated
         *
         */
        @Override
        public Optional<TermType> transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return children.stream()
                    .map(this::transformChild)
                    .flatMap(Optional::stream)
                    .findAny(); // pick any of them
        }

        @Override
        public Optional<TermType> transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return children.stream()
                    .map(this::transformChild)
                    .flatMap(Optional::stream)
                    .findAny(); // pick any of them
        }
    }
}
