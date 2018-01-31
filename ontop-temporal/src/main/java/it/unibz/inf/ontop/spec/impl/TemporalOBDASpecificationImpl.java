package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.TemporalOBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.mapping.TemporalQuadrupleMapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.temporal.spec.ImmutableTemporalVocabulary;

import java.time.temporal.Temporal;

public class TemporalOBDASpecificationImpl implements TemporalOBDASpecification {

    private final Mapping staticMapping;
    private final DBMetadata dbMetadata;
    private final TemporalMapping temporalMapping;
    private final DBMetadata temporalDBMetadata;
    private final ClassifiedTBox saturatedTBox;

    @Inject
    private TemporalOBDASpecificationImpl(Mapping staticMapping, DBMetadata dbMetadata, TemporalMapping temporalMapping, DBMetadata temporalDBMetadata, ClassifiedTBox saturatedTBox) {
        this.staticMapping = staticMapping;
        this.dbMetadata = dbMetadata;
        this.temporalMapping = temporalMapping;
        this.temporalDBMetadata = temporalDBMetadata;
        this.saturatedTBox = saturatedTBox;
    }


    @Override
    public TemporalMapping getTemporalSaturatedMapping() { return temporalMapping; }

    @Override
    public DBMetadata getTemporalDBMetadata() {
        return temporalDBMetadata;
    }

    @Override
    public Mapping getSaturatedMapping() { return staticMapping; }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }

    @Override
    public ClassifiedTBox getSaturatedTBox() {
        return saturatedTBox;
    }

}
