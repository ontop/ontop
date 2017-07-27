package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import it.unibz.inf.ontop.model.TupleResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBinding;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;

public class OntopOWLBinding implements OWLBinding {
    private OntopOWLBindingSet owlBindingSet;
    private String name;

    public OntopOWLBinding(OntopOWLBindingSet owlBindingSet, String name) {
        this.owlBindingSet = owlBindingSet;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OWLObject getValue() throws OWLException {
        return owlBindingSet.getOWLObject(name);
    }
}
