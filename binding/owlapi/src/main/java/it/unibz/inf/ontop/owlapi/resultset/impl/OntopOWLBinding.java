package it.unibz.inf.ontop.owlapi.resultset.impl;

import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.answering.resultset.OntopBinding;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.RDFLiteralConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.OWLBinding;
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

    // TODO(xiao): how about null??
    @Override
    public OWLObject getValue() throws OWLException {
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
