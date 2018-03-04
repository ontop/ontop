package it.unibz.inf.ontop.answering.reformulation.generation.calcite.algebra;

import org.apache.calcite.plan.*;
import org.apache.calcite.rel.*;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.sql.validate.SqlValidatorUtil;

import java.util.Collections;
import java.util.List;

public class TemporalJoinRelNode extends BiRel {


    public TemporalJoinRelNode(RelOptCluster cluster,
                               RelTraitSet traitSet,
                               RelNode left,
                               RelNode right) {
        super(cluster, traitSet, left, right);
    }

    //TODO: replace joinType with a reasonable type
    @Override protected RelDataType deriveRowType() {
        return SqlValidatorUtil.deriveJoinRowType(left.getRowType(),
                right.getRowType(), JoinRelType.INNER, getCluster().getTypeFactory(), null,
                getSystemFieldList());
    }

    public List<RelDataTypeField> getSystemFieldList() {
        return Collections.emptyList();
    }
}
