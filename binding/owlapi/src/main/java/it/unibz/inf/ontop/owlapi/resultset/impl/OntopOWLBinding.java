package it.unibz.inf.ontop.owlapi.resultset.impl;

import it.unibz.inf.ontop.query.resultset.OntopBinding;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.owlapi.resultset.OWLBinding;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;

public class OntopOWLBinding implements OWLBinding {

    private final OntopBinding ontopBinding;

    private final OWLAPIIndividualTranslator translator;


    public OntopOWLBinding(OntopBinding ontopBinding, OWLAPIIndividualTranslator translator){
        this.ontopBinding = ontopBinding;
        this.translator = translator;
    }

    @Override
    public String getName() {
        return ontopBinding.getName();
    }

    // TODO(xiao): how about null??
    @Override
    public OWLObject getValue() {
        return translate(ontopBinding.getValue());
    }

    // TODO(xiao): duplicated code
    private OWLPropertyAssertionObject translate(Constant c) {
        if (c instanceof ObjectConstant)
            return translator.translate((ObjectConstant) c);
        else
            return translator.translate((RDFLiteralConstant) c);
    }
}
