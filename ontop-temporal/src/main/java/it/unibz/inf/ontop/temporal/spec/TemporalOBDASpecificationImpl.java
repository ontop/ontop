package it.unibz.inf.ontop.temporal.spec;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;

public class TemporalOBDASpecificationImpl implements TemporalOBDASpecification{

    DatalogMTLProgram program;

    @Override
    public DatalogMTLProgram getDatalogMTLProgram() {
        return program;
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
