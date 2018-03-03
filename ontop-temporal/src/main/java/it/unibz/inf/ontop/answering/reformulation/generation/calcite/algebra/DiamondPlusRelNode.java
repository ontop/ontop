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

public class DiamondPlusRelNode implements RelNode {

    private final RelNode operand;
    private final TemporalRangeRelNode temporalRangeRelNode;

    public DiamondPlusRelNode(RelNode operand, TemporalRangeRelNode temporalRangeRelNode) {
        this.operand = operand;
        this.temporalRangeRelNode = temporalRangeRelNode;
    }

    @Override
    public List<RexNode> getChildExps() {
        return null;
    }

    @Override
    public Convention getConvention() {
        return null;
    }

    @Override
    public String getCorrelVariable() {
        return null;
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public RelNode getInput(int i) {
        return null;
    }

    @Override
    public RelOptQuery getQuery() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getDigest() {
        return null;
    }

    @Override
    public RelTraitSet getTraitSet() {
        return null;
    }

    @Override
    public RelDataType getRowType() {
        return operand.getRowType();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public RelDataType getExpectedInputRowType(int ordinalInParent) {
        return null;
    }

    @Override
    public List<RelNode> getInputs() {
        return null;
    }

    @Override
    public RelOptCluster getCluster() {
        return null;
    }

    @Override
    public double estimateRowCount(RelMetadataQuery mq) {
        return 0;
    }

    @Override
    public double getRows() {
        return 0;
    }

    @Override
    public Set<String> getVariablesStopped() {
        return null;
    }

    @Override
    public Set<CorrelationId> getVariablesSet() {
        return null;
    }

    @Override
    public void collectVariablesUsed(Set<CorrelationId> variableSet) {

    }

    @Override
    public void collectVariablesSet(Set<CorrelationId> variableSet) {

    }

    @Override
    public void childrenAccept(RelVisitor visitor) {

    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        return null;
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner) {
        return null;
    }

    @Override
    public <M extends Metadata> M metadata(Class<M> metadataClass, RelMetadataQuery mq) {
        return null;
    }

    @Override
    public void explain(RelWriter pw) {

    }

    @Override
    public RelNode onRegister(RelOptPlanner planner) {
        return null;
    }

    @Override
    public String recomputeDigest() {
        return null;
    }

    @Override
    public void replaceInput(int ordinalInParent, RelNode p) {

    }

    @Override
    public RelOptTable getTable() {
        return null;
    }

    @Override
    public String getRelTypeName() {
        return null;
    }

    @Override
    public boolean isValid(Litmus litmus, Context context) {
        return false;
    }

    @Override
    public boolean isValid(boolean fail) {
        return false;
    }

    @Override
    public List<RelCollation> getCollationList() {
        return null;
    }

    @Override
    public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
        return null;
    }

    @Override
    public void register(RelOptPlanner planner) {

    }

    @Override
    public boolean isKey(ImmutableBitSet columns) {
        return false;
    }

    @Override
    public RelNode accept(RelShuttle shuttle) {
        return null;
    }

    @Override
    public RelNode accept(RexShuttle shuttle) {
        return null;
    }
}
