package it.unibz.inf.ontop.answering.reformulation.generation.calcite;

import org.apache.calcite.plan.Context;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.tools.RelBuilder;

public class TemporalRelBuilder extends RelBuilder {

    protected TemporalRelBuilder(Context context, RelOptCluster cluster, RelOptSchema relOptSchema) {
        super(context, cluster, relOptSchema);
    }
}
