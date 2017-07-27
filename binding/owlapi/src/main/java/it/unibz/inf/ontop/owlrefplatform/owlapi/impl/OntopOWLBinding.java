package it.unibz.inf.ontop.owlrefplatform.owlapi.impl;

import it.unibz.inf.ontop.model.OntopBinding;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.owlapi.OWLAPIIndividualTranslator;
import it.unibz.inf.ontop.owlrefplatform.owlapi.OWLBinding;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class OntopOWLBinding implements OWLBinding {

    private final OntopBinding ontopBinding;

    private static OWLAPIIndividualTranslator translator = new OWLAPIIndividualTranslator();


    public OntopOWLBinding(OntopBinding ontopBinding){
        this.ontopBinding = ontopBinding;
    }

    @Override
    public String getName() {
        return ontopBinding.getName();
    }

    @Override
    public OWLObject getValue() throws OWLException {
        return translate(ontopBinding.getValue());
    }

    // TODO(xiao): duplicated code
    private OWLPropertyAssertionObject translate(Constant c) {
        if (c instanceof ObjectConstant)
            return translator.translate((ObjectConstant) c);
        else
            return translator.translate((ValueConstant) c);
    }
}
