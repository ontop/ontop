package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.IQTree2SelectFromWhereConverter;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhere;
import it.unibz.inf.ontop.iq.IQTree;

public class IQTree2SelectFromWhereConverterImpl implements IQTree2SelectFromWhereConverter {

    @Inject
    private IQTree2SelectFromWhereConverterImpl() {
    }

    @Override
    public SelectFromWhere convert(IQTree iqTree) {
        throw new RuntimeException("TODO: implement IQTree2SelectFromWhereConverterImpl");
    }
}
