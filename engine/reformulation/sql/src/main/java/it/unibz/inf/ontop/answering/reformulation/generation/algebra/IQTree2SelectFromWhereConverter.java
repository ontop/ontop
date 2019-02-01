package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import it.unibz.inf.ontop.iq.IQTree;

public interface IQTree2SelectFromWhereConverter {

    SelectFromWhere convert(IQTree iqTree);
}
