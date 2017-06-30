package it.unibz.inf.ontop.mapping.trans;


import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;

public interface MappingCanonicalRewriter {

    Mapping rewrite(Mapping mapping, DBMetadata dbMetadata);
}
