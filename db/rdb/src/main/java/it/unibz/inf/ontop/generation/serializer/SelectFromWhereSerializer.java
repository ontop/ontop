package it.unibz.inf.ontop.generation.serializer;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.model.term.Variable;

public interface SelectFromWhereSerializer {

    QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters);

    interface QuerySerialization {
        String getString();
        ImmutableMap<Variable, QualifiedAttributeID> getColumnIDs();
    }
}
