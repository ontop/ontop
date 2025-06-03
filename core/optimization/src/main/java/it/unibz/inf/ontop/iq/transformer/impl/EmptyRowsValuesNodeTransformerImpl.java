package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.EmptyRowsValuesNodeTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.impl.DBConstantImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.AbstractCollection;
import java.util.stream.IntStream;

public class EmptyRowsValuesNodeTransformerImpl implements EmptyRowsValuesNodeTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final DBTypeFactory dbTypeFactory;

    @Inject
    protected EmptyRowsValuesNodeTransformerImpl(CoreSingletons coreSingletons) {
        this.iqFactory = coreSingletons.getIQFactory();
        this.dbTypeFactory = coreSingletons.getTypeFactory().getDBTypeFactory();
    }

    @Override
    public IQTree transform(IQTree iqTree, VariableGenerator variableGenerator) {
        return iqTree.acceptVisitor(new Transformer(variableGenerator));
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final VariableGenerator variableGenerator;

        Transformer(VariableGenerator variableGenerator) {
            super(EmptyRowsValuesNodeTransformerImpl.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        /**
         * Override transform Values method to handle the case of VALUES [] () ()
         */
        @Override
        public IQTree transformValues(ValuesNode valuesNode) {
            return valuesNode.getValues().stream().allMatch(AbstractCollection::isEmpty)
                    ? normalize(valuesNode)
                    : valuesNode;
        }

        private ValuesNode normalize(ValuesNode valuesNode) {
            DBConstant placeholder = new DBConstantImpl("placeholder", dbTypeFactory.getDBStringType());

            ImmutableList<ImmutableList<Constant>> newValues = IntStream.range(0, valuesNode.getValues().size())
                    .mapToObj(i -> ImmutableList.<Constant>of(placeholder))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createValuesNode(ImmutableList.of(variableGenerator.generateNewVariable()), newValues);
        }
    }
}
