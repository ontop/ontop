package it.unibz.inf.ontop.dbschema;

public interface RDBMetadataProvider extends MetadataProvider {

    RelationID getRelationCanonicalID(RelationID id);

    DBParameters getDBParameters();
}
