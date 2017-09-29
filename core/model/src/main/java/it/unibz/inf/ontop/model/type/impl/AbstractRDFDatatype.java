package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractRDFDatatype extends AbstractTermType implements RDFDatatype {

    protected AbstractRDFDatatype(Predicate.COL_TYPE colType) {
        super(colType);
    }

    @Override
    public boolean isCompatibleWith(IRI baseDatatypeIri) {
        throw new RuntimeException("TODO: implement isCompatibleWith(IRI)");
    }
}
