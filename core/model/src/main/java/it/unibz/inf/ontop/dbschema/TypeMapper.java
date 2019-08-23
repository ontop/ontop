package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.TermType;

public interface TypeMapper {

    TermType getTermType(int typeCode, String typeName);
}
