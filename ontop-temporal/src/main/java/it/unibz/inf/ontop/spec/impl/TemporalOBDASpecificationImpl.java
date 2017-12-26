package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.spec.TemporalOBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.TemporalMapping;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.spec.ImmutableTemporalVocabulary;

public class TemporalOBDASpecificationImpl implements TemporalOBDASpecification {

    DatalogMTLProgram program;

    @Override
    public DatalogMTLProgram getDatalogMTLProgram() {
        return program;
    }

    @Override
    public ImmutableTemporalVocabulary getTemporalVocabulary() {
        return null;
    }

    @Override
    public TemporalMapping getTemporalSaturatedMapping() {
        return null;
    }

    @Override
    public DBMetadata getTemporalDBMetadata() {
        return null;
    }

    @Override
    public Mapping getSaturatedMapping() {

        // ....

        return null;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return null;
    }

    @Override
    public TBoxReasoner getSaturatedTBox() {
        return null;
    }

    @Override
    public ImmutableOntologyVocabulary getVocabulary() {
        return null;
    }
}
