package it.unibz.inf.ontop.generation.serializer;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.serializer.impl.SQLTermSerializer;
import it.unibz.inf.ontop.model.term.Variable;

public interface SelectFromWhereSerializer {

    QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters);

    /**
     * Needed by third-party applications. See the ConstantSerializer tool.
     */
    SQLTermSerializer getTermSerializer();

    interface QuerySerialization {
        String getString();
        ImmutableMap<Variable, QualifiedAttributeID> getColumnIDs();
    }
}
