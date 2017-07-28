package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.MaterializedGraphOWLResultSet;

import java.net.URI;


public class OntopMaterializedGraphOWLResultSet extends OntopGraphOWLResultSet
        implements MaterializedGraphOWLResultSet {

    private final MaterializedGraphResultSet graphResultSet;

    OntopMaterializedGraphOWLResultSet(MaterializedGraphResultSet graphResultSet) {
        super(graphResultSet);
        this.graphResultSet = graphResultSet;
    }

    @Override
    public long getTripleCountSoFar() {
        return graphResultSet.getTripleCountSoFar();
    }

    @Override
    public ImmutableList<URI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
        return graphResultSet.getPossiblyIncompleteRDFPropertiesAndClassesSoFar();
    }

    @Override
    public ImmutableSet<URI> getSelectedVocabulary() {
        return graphResultSet.getSelectedVocabulary();
    }
}
