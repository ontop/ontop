package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBinding;
import org.semanticweb.owlapi.model.OWLObject;

public class OntopOWLBinding implements OWLBinding {
    private OWLObject value;
    private String name;

    public OntopOWLBinding(String name, OWLObject value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OWLObject getValue() {
        return value;
    }
}
