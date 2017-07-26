package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.owlrefplatform.core.abox.MaterializedGraphResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OntopOWLMaterializedGraphResultSet;

import java.net.URI;


public class DefaultOntopOWLMaterializedGraphResultSet extends DefaultOntopOWLGraphResultSet
        implements OntopOWLMaterializedGraphResultSet {

    private final MaterializedGraphResultSet graphResultSet;

    DefaultOntopOWLMaterializedGraphResultSet(MaterializedGraphResultSet graphResultSet) {
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
