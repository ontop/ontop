package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator extends DefaultRecursiveIQTreeVisitingTransformer {
    protected final VariableGenerator variableGenerator;

    protected DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator(IntermediateQueryFactory iqFactory, VariableGenerator variableGenerator) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
    }
}
