package it.unibz.inf.ontop.answering.reformulation.generation.serializer;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhere;
import it.unibz.inf.ontop.model.term.Variable;

public interface SelectFromWhereSerializer {

    QuerySerialization serialize(SelectFromWhere selectFromWhere);

    interface QuerySerialization {
        String getString();
        ImmutableMap<Variable, String> getVariableNames();
    }
}
