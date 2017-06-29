package it.unibz.inf.ontop.mapping;


import it.unibz.inf.ontop.model.DBMetadata;

public interface MappingCanonicalRewriter {

    Mapping rewrite(Mapping mapping, DBMetadata dbMetadata);
}
