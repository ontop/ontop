package it.unibz.inf.ontop.iq.visit.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.function.Function;

public class DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator extends DefaultRecursiveIQTreeVisitingTransformer {
    protected final VariableGenerator variableGenerator;

    protected DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator(IntermediateQueryFactory iqFactory, VariableGenerator variableGenerator) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
    }

    protected DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator(IntermediateQueryFactory iqFactory, Function<IQTree, IQTree> postTransformer, VariableGenerator variableGenerator) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
    }
}
