package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.TemporalOBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

public class TemporalOBDASpecificationImpl implements TemporalOBDASpecification {

    private final Mapping staticMapping;
    private final DBMetadata dbMetadata;
    private final TemporalMapping temporalMapping;
    private final DBMetadata temporalDBMetadata;
    private final ClassifiedTBox saturatedTBox;

    @Inject
    private TemporalOBDASpecificationImpl(@Assisted Mapping staticMapping, @Assisted("dbMetadata") DBMetadata dbMetadata, @Assisted TemporalMapping temporalMapping, @Assisted("temporalDBMetadata") DBMetadata temporalDBMetadata, @Assisted ClassifiedTBox saturatedTBox) {
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
