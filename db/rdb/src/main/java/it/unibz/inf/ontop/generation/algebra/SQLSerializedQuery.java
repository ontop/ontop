package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Already serialized
 *
 * See SQLAlgebraFactory for creating a new instance.
 */
public interface SQLSerializedQuery extends SQLExpression {

    String getSQLString();

    ImmutableMap<Variable, QuotedID> getColumnNames();
}
