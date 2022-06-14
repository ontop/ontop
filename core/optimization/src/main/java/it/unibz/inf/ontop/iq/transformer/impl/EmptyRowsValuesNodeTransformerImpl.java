package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.iq.transformer.EmptyRowsValuesNodeTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.impl.DBConstantImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.stream.IntStream;

public class EmptyRowsValuesNodeTransformerImpl extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator>
        implements EmptyRowsValuesNodeTransformer {

    private final CoreSingletons coreSingletons;

    @Inject
    protected EmptyRowsValuesNodeTransformerImpl(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.coreSingletons = coreSingletons;
    }

    /**
     * Ovverride transform Values method to handle case of VALUES [] () ()
     */
    @Override
    public IQTree transformValues(ValuesNode valuesNode, VariableGenerator variableGenerator) {
        return valuesNode.getValues().stream()
                .map(a -> a.size()).reduce(0, Integer::sum).equals(0)
                ? normalize(valuesNode, variableGenerator)
                : valuesNode;
    }

    private ValuesNode normalize(ValuesNode valuesNode, VariableGenerator variableGenerator) {
        DBConstant placeholder = new DBConstantImpl("placeholder",
                coreSingletons.getTypeFactory().getDBTypeFactory().getDBStringType());

        ImmutableList<ImmutableList<Constant>> newValues = IntStream.range(0, valuesNode.getValues().size())
                        .mapToObj(i -> ImmutableList.of((Constant) placeholder))
                        .collect(ImmutableCollectors.toList());

        return iqFactory.createValuesNode(ImmutableList.of(variableGenerator.generateNewVariable()), newValues);
    }
}
