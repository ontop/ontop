package it.unibz.inf.ontop.owlapi.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import org.apache.commons.rdf.api.IRI;


public class OntopMaterializedGraphOWLResultSet extends OntopGraphOWLResultSet
        implements MaterializedGraphOWLResultSet {

    private final MaterializedGraphResultSet graphResultSet;

    public OntopMaterializedGraphOWLResultSet(MaterializedGraphResultSet graphResultSet, byte[] salt) {
        super(graphResultSet, salt);
        this.graphResultSet = graphResultSet;
    }

    @Override
    public long getTripleCountSoFar() {
        return graphResultSet.getTripleCountSoFar();
    }

    @Override
    public ImmutableList<IRI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
        return graphResultSet.getPossiblyIncompleteRDFPropertiesAndClassesSoFar();
    }

    @Override
    public ImmutableSet<IRI> getSelectedVocabulary() {
        return graphResultSet.getSelectedVocabulary();
    }
}
