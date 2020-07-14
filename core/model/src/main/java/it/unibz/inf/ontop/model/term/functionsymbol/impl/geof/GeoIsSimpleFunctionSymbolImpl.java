package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.AbstractGeofBooleanFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeoIsSimpleFunctionSymbolImpl extends AbstractGeofBooleanFunctionSymbolImpl {
    public GeoIsSimpleFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdBooleanType) {
        super("GEO_ISSIMPLE", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType), xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBIsSimple(subLexicalTerms.get(0));
    }
}
