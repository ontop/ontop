//package it.unibz.inf.ontop.spec.mapping.impl;
//
//import it.unibz.inf.ontop.dbschema.DBMetadata;
//import it.unibz.inf.ontop.spec.mapping.Mapping;
//import it.unibz.inf.ontop.spec.mapping.TemporalQuadrupleMapping;
//import it.unibz.inf.ontop.spec.mapping.TemporalMappingExtractor;
//
//public class TemporalQuadrupleMappingAndDBMetadataImpl implements TemporalMappingExtractor.MappingAndDBMetadata {
//    private final TemporalQuadrupleMapping temporalQuadrupleMapping;
//    private final DBMetadata dbMetadata;
//
//    public TemporalQuadrupleMappingAndDBMetadataImpl(TemporalQuadrupleMapping mapping, DBMetadata dbMetadata) {
//        this.temporalQuadrupleMapping = mapping;
//        this.dbMetadata = dbMetadata;
//    }
//
//    @Override
//    public Mapping getMapping() {
//        return null;
//    }
//
//    @Override
//    public DBMetadata getDBMetadata() {
//        return dbMetadata;
//    }
//
//    @Override
//    public TemporalQuadrupleMapping getTemporalQuadrupleMapping() {
//        return temporalQuadrupleMapping;
//    }
//
//}
