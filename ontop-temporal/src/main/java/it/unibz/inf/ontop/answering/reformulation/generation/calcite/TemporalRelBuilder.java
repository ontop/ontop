package it.unibz.inf.ontop.answering.reformulation.generation.calcite;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.generation.calcite.algebra.TemporalRangeRelNode;
import org.apache.calcite.plan.Context;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.server.CalciteServerStatement;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;
import org.apache.calcite.util.Util;

public class TemporalRelBuilder extends RelBuilder {

    private final TemporalRelFactories.BoxMinusFactory boxMinusFactory;
    private final TemporalRelFactories.BoxPlusFactory boxPlusFactory;
    private final TemporalRelFactories.DiamondMinusFactory diamondMinusFactory;
    private final TemporalRelFactories.DiamondPlusFactory diamondPlusFactory;
    private final TemporalRelFactories.TemporalCoalesceFactory temporalCoalesceFactory;
    private final TemporalRelFactories.TemporalJoinFactory temporalJoinFactory;
    private final TemporalRelFactories.TemporalRangeFactory temporalRangeFactory;

    protected TemporalRelBuilder(Context context, RelOptCluster cluster, RelOptSchema relOptSchema) {

        super(context, cluster, relOptSchema);

        if (context == null) {
            context = Contexts.EMPTY_CONTEXT;
        }
        this.boxMinusFactory =
                Util.first(context.unwrap(TemporalRelFactories.BoxMinusFactory.class),
                        TemporalRelFactories.DEFAULT_BOXMINUS_FACTORY);

        this.boxPlusFactory =
                Util.first(context.unwrap(TemporalRelFactories.BoxPlusFactory.class),
                        TemporalRelFactories.DEFAULT_BOXPLUS_FACTORY);

        this.diamondMinusFactory =
                Util.first(context.unwrap(TemporalRelFactories.DiamondMinusFactory.class),
                        TemporalRelFactories.DEFAULT_DIAMONDMINUS_FACTORY);

        this.diamondPlusFactory =
                Util.first(context.unwrap(TemporalRelFactories.DiamondPlusFactory.class),
                        TemporalRelFactories.DEFAULT_DIAMONDPLUS_FACTORY);

        this.temporalCoalesceFactory =
                Util.first(context.unwrap(TemporalRelFactories.TemporalCoalesceFactory.class),
                        TemporalRelFactories.DEFAULT_TEMPORALCOALESCE_FACTORY);

        this.temporalJoinFactory =
                Util.first(context.unwrap(TemporalRelFactories.TemporalJoinFactory.class),
                        TemporalRelFactories.DEFAULT_TEMPORALJOIN_FACTORY);

        this.temporalRangeFactory =
                Util.first(context.unwrap(TemporalRelFactories.TemporalRangeFactory.class),
                        TemporalRelFactories.DEFAULT_TEMPORALRANGE_FACTORY);
    }

    /** Creates a RelBuilder. */
    public static TemporalRelBuilder create(FrameworkConfig config) {
        final RelOptCluster[] clusters = {null};
        final RelOptSchema[] relOptSchemas = {null};
        Frameworks.withPrepare(
                new Frameworks.PrepareAction<Void>(config) {
                    public Void apply(RelOptCluster cluster, RelOptSchema relOptSchema,
                                      SchemaPlus rootSchema, CalciteServerStatement statement) {
                        clusters[0] = cluster;
                        relOptSchemas[0] = relOptSchema;
                        return null;
                    }
                });
        return new TemporalRelBuilder(config.getContext(), clusters[0], relOptSchemas[0]);
    }

    public TemporalRelBuilder boxMinus(RelNode input, TemporalRangeRelNode range){
        push (boxMinusFactory.createBoxMinus(input, range));
        return this;
    }

    public TemporalRelBuilder boxPlus(RelNode input, TemporalRangeRelNode range){
        push (boxPlusFactory.createBoxPlus(input, range));
        return this;
    }

    public TemporalRelBuilder diamondMinus(RelNode input, TemporalRangeRelNode range){
        push (diamondMinusFactory.createDiamondMinus(input, range));
        return this;
    }

    public TemporalRelBuilder diamondPlus(RelNode input, TemporalRangeRelNode range){
        push (diamondPlusFactory.createDiamondPlus(input, range));
        return this;
    }

    public TemporalRelBuilder temporalCoalesce(RelNode operand){
        push (temporalCoalesceFactory.createTemporalCoalesce(operand));
        return this;
    }

    public TemporalRelBuilder temporalJoin(){
        RelNode right = this.build();
        RelNode left = this.build();
        final RelNode temporalJoin;
        temporalJoin = temporalJoinFactory.createTemporalJoin(left, right);
        push(temporalJoin);
        return this;
    }

    public TemporalRangeRelNode temporalRange(RexNode beginInc, RexNode begin, RexNode end, RexNode endInc){
        return temporalRangeFactory.createTemporalRange(beginInc, begin, end, endInc);
    }
}
