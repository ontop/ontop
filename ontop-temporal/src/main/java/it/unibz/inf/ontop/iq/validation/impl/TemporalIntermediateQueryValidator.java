package it.unibz.inf.ontop.iq.validation.impl;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.temporal.iq.node.*;

public class TemporalIntermediateQueryValidator extends  StandardIntermediateQueryValidator {

    protected class TemporalValidationVisitor extends ValidationVisitor implements TemporalQueryNodeVisitor {

        protected TemporalValidationVisitor(IntermediateQuery query) {
            super(query);
        }

        @Override
        public void visit(TemporalJoinNode temporalJoinNode) {

        }

        @Override
        public void visit(BoxMinusNode boxMinusNode) {

        }

        @Override
        public void visit(BoxPlusNode boxPlusNode) {

        }

        @Override
        public void visit(DiamondMinusNode diamondMinusNode) {

        }

        @Override
        public void visit(DiamondPlusNode diamondPlusNode) {

        }

        @Override
        public void visit(SinceNode sinceNode) {

        }

        @Override
        public void visit(UntilNode untilNode) {

        }

        @Override
        public void visit(TemporalCoalesceNode temporalCoalesceNode) {

        }
    }

    @Override
    protected ValidationVisitor createVisitor(IntermediateQuery query) {
        return new TemporalValidationVisitor(query);
    }
}
