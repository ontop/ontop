package it.unibz.inf.ontop.answering.reformulation.generation.calcite.algebra;

import org.apache.calcite.plan.*;
import org.apache.calcite.rel.*;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.metadata.Metadata;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexShuttle;
import org.apache.calcite.util.ImmutableBitSet;
import org.apache.calcite.util.Litmus;

import java.util.List;
import java.util.Set;

public class BoxMinusRelNode implements RelNode {

    private final RelNode operand;
    private final TemporalRangeRelNode temporalRangeRelNode;

    public BoxMinusRelNode(RelNode operand, TemporalRangeRelNode temporalRangeRelNode) {
        this.operand = operand;
        this.temporalRangeRelNode = temporalRangeRelNode;
    }

    @Override
    public List<RexNode> getChildExps() {
        throw new UnsupportedOperationException("getChildExps is not implemented");
    }

    @Override
    public Convention getConvention() {
        throw new UnsupportedOperationException("getConvention is not implemented");
    }

    @Override
    public String getCorrelVariable() {
        throw new UnsupportedOperationException("getCorrelVariable is not implemented");
    }

    @Override
    public boolean isDistinct() {
        throw new UnsupportedOperationException("isDistinct is not implemented");
    }

    @Override
    public RelNode getInput(int i) {
        throw new UnsupportedOperationException("getInput is not implemented");
    }

    @Override
    public RelOptQuery getQuery() {
        throw new UnsupportedOperationException("getQuery is not implemented");
    }

    @Override
    public int getId() {
        throw new UnsupportedOperationException("getId is not implemented");
    }

    @Override
    public String getDigest() {
        throw new UnsupportedOperationException("getDigest is not implemented");
    }

    @Override
    public RelTraitSet getTraitSet() {
        throw new UnsupportedOperationException("getTraitSet is not implemented");
    }

    @Override
    public RelDataType getRowType() {
        throw new UnsupportedOperationException("getRowType is not implemented");
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("getDescription is not implemented");
    }

    @Override
    public RelDataType getExpectedInputRowType(int ordinalInParent) {
        throw new UnsupportedOperationException("getExpectedInputRowType is not implemented");
    }

    @Override
    public List<RelNode> getInputs() {
        throw new UnsupportedOperationException("getInputs is not implemented");
    }

    @Override
    public RelOptCluster getCluster() {
        throw new UnsupportedOperationException("getCluster is not implemented");
    }

    @Override
    public double estimateRowCount(RelMetadataQuery mq) {
        throw new UnsupportedOperationException("estimateRowCount is not implemented");
    }

    @Override
    public double getRows() {
        throw new UnsupportedOperationException("getRows is not implemented");
    }

    @Override
    public Set<String> getVariablesStopped() {
        throw new UnsupportedOperationException("getVariablesStopped is not implemented");
    }

    @Override
    public Set<CorrelationId> getVariablesSet() {
        throw new UnsupportedOperationException("getVariablesSet is not implemented");
    }

    @Override
    public void collectVariablesUsed(Set<CorrelationId> variableSet) {
        throw new UnsupportedOperationException("collectVariablesUsed is not implemented");
    }

    @Override
    public void collectVariablesSet(Set<CorrelationId> variableSet) {
        throw new UnsupportedOperationException("collectVariablesSet is not implemented");
    }

    @Override
    public void childrenAccept(RelVisitor visitor) {
        throw new UnsupportedOperationException("childrenAccept is not implemented");
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        throw new UnsupportedOperationException("computeSelfCost is not implemented");
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner) {
        throw new UnsupportedOperationException("computeSelfCost is not implemented");
    }

    @Override
    public <M extends Metadata> M metadata(Class<M> metadataClass, RelMetadataQuery mq) {
        throw new UnsupportedOperationException("metadata is not implemented");
    }

    @Override
    public void explain(RelWriter pw) {
        throw new UnsupportedOperationException("explain is not implemented");
    }

    @Override
    public RelNode onRegister(RelOptPlanner planner) {
        throw new UnsupportedOperationException("onRegister is not implemented");
    }

    @Override
    public String recomputeDigest() {
        throw new UnsupportedOperationException("recomputeDigest is not implemented");
    }

    @Override
    public void replaceInput(int ordinalInParent, RelNode p) {
        throw new UnsupportedOperationException("replaceInput is not implemented");
    }

    @Override
    public RelOptTable getTable() {
        throw new UnsupportedOperationException("getTable is not implemented");
    }

    @Override
    public String getRelTypeName() {
        throw new UnsupportedOperationException("getRelTypeName is not implemented");
    }

    @Override
    public boolean isValid(Litmus litmus, Context context) {
        throw new UnsupportedOperationException("isValid is not implemented");
    }

    @Override
    public boolean isValid(boolean fail) {
        throw new UnsupportedOperationException("isValid is not implemented");
    }

    @Override
    public List<RelCollation> getCollationList() {
        throw new UnsupportedOperationException("getCollationList is not implemented");
    }

    @Override
    public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
        throw new UnsupportedOperationException("copy is not implemented");
    }

    @Override
    public void register(RelOptPlanner planner) {
        throw new UnsupportedOperationException("register is not implemented");
    }

    @Override
    public boolean isKey(ImmutableBitSet columns) {
        throw new UnsupportedOperationException("isKey is not implemented");
    }

    @Override
    public RelNode accept(RelShuttle shuttle) {
        throw new UnsupportedOperationException("accept is not implemented");
    }

    @Override
    public RelNode accept(RexShuttle shuttle) {
        throw new UnsupportedOperationException("accept is not implemented");
    }
}
