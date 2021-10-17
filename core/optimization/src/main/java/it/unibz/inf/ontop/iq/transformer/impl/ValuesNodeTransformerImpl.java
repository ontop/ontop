package it.unibz.inf.ontop.iq.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ValuesNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transformer.ValuesNodeTransformer;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.impl.DBConstantImpl;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ValuesNodeTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer
        implements ValuesNodeTransformer {

    private final CoreSingletons coreSingletons;

    @Inject
    protected ValuesNodeTransformerImpl(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.coreSingletons = coreSingletons;
    }

    /**
     * Ovverride transform Values method to handle case of VALUES [] () ()
     */
    @Override
    public IQTree transformValues(ValuesNode valuesNode) {
        return valuesNode.getValues().stream()
                .map(a -> a.size()).reduce(0, Integer::sum).equals(0)
                ? normalize(valuesNode)
                : valuesNode;
    }

    private ValuesNode normalize(ValuesNode valuesNode) {
        DBConstant placeholder = new DBConstantImpl("placeholder",
                coreSingletons.getTypeFactory().getDBTypeFactory().getDBStringType());
        List<Constant> list = new ArrayList<>();
        IntStream.range(0, valuesNode.getValues().size())
                .forEach(v -> list.add(placeholder));
        ImmutableList<ImmutableList<Constant>> newValues = ImmutableList.of(ImmutableList.copyOf(list));
        return iqFactory.createValuesNode(
                valuesNode.getOrderedVariables(),
                newValues);
    }
}
