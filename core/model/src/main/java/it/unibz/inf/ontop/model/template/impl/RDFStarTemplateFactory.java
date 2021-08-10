package it.unibz.inf.ontop.model.template.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

public class RDFStarTemplateFactory extends AbstractTemplateFactory {
    public RDFStarTemplateFactory(TermFactory termFactory) {
        super(termFactory);
    }

    public ImmutableFunctionalTerm getRDFStarTerm(NonVariableTerm subject, NonVariableTerm predicate, NonVariableTerm object) {
        return termFactory.getNestedTripleFunctionalTerm(subject, predicate, object);
    }

    // As constant-, column-, and template-valued RDFStar term maps don't exist, these functions return null.
    @Override
    public NonVariableTerm getConstant(String constant) {
        return null;
    }

    @Override
    public ImmutableFunctionalTerm getColumn(String column) {
        return null;
    }

    @Override
    public NonVariableTerm getTemplateTerm(ImmutableList<Template.Component> components) {
        return null;
    }

    @Override
    public String serializeTemplateTerm(ImmutableFunctionalTerm functionalTerm) {
        throw new Error("To implement");
    }
}
