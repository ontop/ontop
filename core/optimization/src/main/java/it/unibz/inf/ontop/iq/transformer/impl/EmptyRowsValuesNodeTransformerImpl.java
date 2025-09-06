package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transformer.EmptyRowsValuesNodeTransformer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.stream.IntStream;

public class EmptyRowsValuesNodeTransformerImpl extends DelegatingIQTreeVariableGeneratorTransformer implements EmptyRowsValuesNodeTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    protected EmptyRowsValuesNodeTransformerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();

        this.transformer = IQTreeVariableGeneratorTransformer.of(Transformer::new);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }

    /**
     * Handles the case of VALUES [] () ()
     */
    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

        Transformer(VariableGenerator variableGenerator) {
            super(EmptyRowsValuesNodeTransformerImpl.this.iqFactory, variableGenerator);
        }

        @Override
        public IQTree transformValues(ValuesNode valuesNode) {
            if (valuesNode.getValues().stream().allMatch(ImmutableList::isEmpty)) {
                DBConstant placeholder = termFactory.getDBStringConstant("placeholder");

                ImmutableList<ImmutableList<Constant>> newValues = IntStream.range(0, valuesNode.getValues().size())
                        .mapToObj(i -> ImmutableList.<Constant>of(placeholder))
                        .collect(ImmutableCollectors.toList());

                return iqFactory.createValuesNode(ImmutableList.of(variableGenerator.generateNewVariable()), newValues);
            }

            return valuesNode;
        }
    }
}
