package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Already serialized
 */
public interface SQLSerializedQuery extends SQLRelation {

    String getSQLString();

    ImmutableMap<Variable, String> getColumnNames();
}
