package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.owlrefplatform.core.abox.MaterializedGraphResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLMaterializedGraphResultSet;

import java.net.URI;


public class DefaultOWLMaterializedGraphResultSet extends DefaultOWLGraphResultSet
        implements OWLMaterializedGraphResultSet {

    private final MaterializedGraphResultSet graphResultSet;

    DefaultOWLMaterializedGraphResultSet(MaterializedGraphResultSet graphResultSet) {
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
