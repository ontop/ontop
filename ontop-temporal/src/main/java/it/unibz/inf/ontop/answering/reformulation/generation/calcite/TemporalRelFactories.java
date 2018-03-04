package it.unibz.inf.ontop.answering.reformulation.generation.calcite;

import it.unibz.inf.ontop.answering.reformulation.generation.calcite.algebra.*;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexNode;

import java.util.List;

public class TemporalRelFactories  {

    public static final BoxMinusFactory DEFAULT_BOXMINUS_FACTORY =
            new BoxMinusFactoryImpl();

    public static final BoxPlusFactory DEFAULT_BOXPLUS_FACTORY =
            new BoxPlusFactoryImpl();

    public static final DiamondMinusFactory DEFAULT_DIAMONDMINUS_FACTORY =
            new DiamondMinusFactoryImpl();

    public static final DiamondPlusFactory DEFAULT_DIAMONDPLUS_FACTORY =
            new DiamondPlusFactoryImpl();

    public static final TemporalCoalesceFactory DEFAULT_TEMPORALCOALESCE_FACTORY =
            new TemporalCoalesceFactoryImpl();

    public static final TemporalJoinFactory DEFAULT_TEMPORALJOIN_FACTORY =
            new TemporalJoinFactoryImpl();

    public static final TemporalRangeFactory DEFAULT_TEMPORALRANGE_FACTORY =
            new TemporalRangeFactoryImpl();

    public interface BoxMinusFactory {
        RelNode createBoxMinus(RelNode input, TemporalRangeRelNode temporalRangeRelNode);
    }

    public interface BoxPlusFactory {
        RelNode createBoxPlus(RelNode input, TemporalRangeRelNode temporalRangeRelNode);
    }

    public interface DiamondMinusFactory {
        RelNode createDiamondMinus(RelNode input, TemporalRangeRelNode temporalRangeRelNode);
    }

    public interface DiamondPlusFactory {
        RelNode createDiamondPlus(RelNode input, TemporalRangeRelNode temporalRangeRelNode);
    }

    public interface TemporalCoalesceFactory {
        RelNode createTemporalCoalesce(RelNode input);
    }

    public interface TemporalJoinFactory {
        RelNode createTemporalJoin(RelNode left, RelNode right);
    }

    public interface TemporalRangeFactory{
        TemporalRangeRelNode createTemporalRange(RexNode beginInc, RexNode begin, RexNode end, RexNode endInc);
    }

    private static class BoxMinusFactoryImpl implements BoxMinusFactory {

        public RelNode createBoxMinus(RelNode operand, TemporalRangeRelNode temporalRangeRelNode) {

            return new BoxMinusRelNode(operand, temporalRangeRelNode);
        }
    }

    private static class BoxPlusFactoryImpl implements BoxPlusFactory {

        public RelNode createBoxPlus(RelNode operand, TemporalRangeRelNode temporalRangeRelNode) {

            return new BoxPlusRelNode(operand, temporalRangeRelNode);
        }
    }

    private static class DiamondMinusFactoryImpl implements DiamondMinusFactory {

        public RelNode createDiamondMinus(RelNode operand, TemporalRangeRelNode temporalRangeRelNode) {

            return new DiamondMinusRelNode(operand, temporalRangeRelNode);
        }
    }

    private static class DiamondPlusFactoryImpl implements DiamondPlusFactory {

        public RelNode createDiamondPlus(RelNode operand, TemporalRangeRelNode temporalRangeRelNode) {

            return new DiamondPlusRelNode(operand, temporalRangeRelNode);
        }
    }

    private static class TemporalCoalesceFactoryImpl implements TemporalCoalesceFactory {

        public RelNode createTemporalCoalesce(RelNode operand) {

            return new TemporalCoalesceRelNode(operand);
        }
    }

    private static class TemporalJoinFactoryImpl implements TemporalJoinFactory {

        public RelNode createTemporalJoin(RelNode left, RelNode right) {
            final RelOptCluster cluster = left.getCluster();
            final RelTraitSet traitSet = cluster.traitSetOf(Convention.NONE);
            return new TemporalJoinRelNode(cluster, traitSet, left, right);
        }

    }

    public static class TemporalRangeFactoryImpl implements TemporalRangeFactory{

        public TemporalRangeRelNode createTemporalRange(RexNode beginInc, RexNode begin, RexNode end, RexNode endInc){

            return new TemporalRangeRelNode(beginInc, begin, end, endInc);
        }
    }

}
