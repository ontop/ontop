package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Guice;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 *
 * Accessible through {@link Guice} (recommended) or through {@link CoreSingletons#getIQFactory()}.
 *
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface IntermediateQueryFactory {

    IntermediateQueryBuilder createIQBuilder(ExecutorRegistry executorRegistry);

    ConstructionNode createConstructionNode(ImmutableSet<Variable> projectedVariables);

    ConstructionNode createConstructionNode(ImmutableSet<Variable> projectedVariables,
                                            ImmutableSubstitution<ImmutableTerm> substitution);

    UnionNode createUnionNode(ImmutableSet<Variable> projectedVariables);

    InnerJoinNode createInnerJoinNode();
    InnerJoinNode createInnerJoinNode(ImmutableExpression joiningCondition);
    InnerJoinNode createInnerJoinNode(Optional<ImmutableExpression> joiningCondition);

    LeftJoinNode createLeftJoinNode();
    LeftJoinNode createLeftJoinNode(ImmutableExpression joiningCondition);
    LeftJoinNode createLeftJoinNode(Optional<ImmutableExpression> joiningCondition);

    FilterNode createFilterNode(ImmutableExpression filterCondition);

    StrictFlattenNode createStrictFlattenNode(Variable arrayVariable,
                                              int arrayIndexIndex,
                                              DataAtom<RelationPredicate> dataAtom);

    RelaxedFlattenNode createRelaxedFlattenNode(Variable newArrayTerm,
                                                int arrayIndexIndex,
                                                DataAtom<RelationPredicate> dataAtom);

    IntensionalDataNode createIntensionalDataNode(DataAtom<AtomPredicate> atom);

    /**
     * NB: keys of the argumentMap are indices. They start at 0.
     */
    ExtensionalDataNode createExtensionalDataNode(RelationDefinition relationDefinition,
                                                  ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap);

    ExtensionalDataNode createExtensionalDataNode(RelationDefinition relationDefinition,
                                                  ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap,
                                                  VariableNullability variableNullability);

    EmptyNode createEmptyNode(ImmutableSet<Variable> projectedVariables);

    NativeNode createNativeNode(ImmutableSortedSet<Variable> variables,
                                @Assisted("variableTypeMap") ImmutableMap<Variable, DBTermType> variableTypeMap,
                                @Assisted("columnNames") ImmutableMap<Variable, QuotedID> columnNames,
                                String nativeQueryString, VariableNullability variableNullability);

    TrueNode createTrueNode();

    DistinctNode createDistinctNode();
    SliceNode createSliceNode(@Assisted("offset") long offset, @Assisted("limit") long limit);
    SliceNode createSliceNode(long offset);

    OrderByNode createOrderByNode(ImmutableList<OrderByNode.OrderComparator> comparators);
    OrderByNode.OrderComparator createOrderComparator(NonGroundTerm term, boolean isAscending);

    AggregationNode createAggregationNode(ImmutableSet<Variable> groupingVariables,
                                          ImmutableSubstitution<ImmutableFunctionalTerm> substitution);

    UnaryIQTree createUnaryIQTree(UnaryOperatorNode rootNode, IQTree child);
    UnaryIQTree createUnaryIQTree(UnaryOperatorNode rootNode, IQTree child, IQTreeCache treeCache);
    UnaryIQTree createUnaryIQTree(UnaryOperatorNode rootNode, IQTree child, IQProperties properties);

    BinaryNonCommutativeIQTree createBinaryNonCommutativeIQTree(BinaryNonCommutativeOperatorNode rootNode,
                                                                @Assisted("left") IQTree leftChild,
                                                                @Assisted("right") IQTree rightChild);
    BinaryNonCommutativeIQTree createBinaryNonCommutativeIQTree(BinaryNonCommutativeOperatorNode rootNode,
                                                                @Assisted("left") IQTree leftChild,
                                                                @Assisted("right") IQTree rightChild,
                                                                IQTreeCache treeCache);
    BinaryNonCommutativeIQTree createBinaryNonCommutativeIQTree(BinaryNonCommutativeOperatorNode rootNode,
                                                                @Assisted("left") IQTree leftChild,
                                                                @Assisted("right") IQTree rightChild,
                                                                IQProperties properties);

    NaryIQTree createNaryIQTree(NaryOperatorNode rootNode, ImmutableList<IQTree> children);
    NaryIQTree createNaryIQTree(NaryOperatorNode rootNode, ImmutableList<IQTree> children, IQTreeCache treeCache);
    NaryIQTree createNaryIQTree(NaryOperatorNode rootNode, ImmutableList<IQTree> children, IQProperties properties);

    IQ createIQ(DistinctVariableOnlyDataAtom projectionAtom, IQTree tree);

    IQProperties createIQProperties();
}
