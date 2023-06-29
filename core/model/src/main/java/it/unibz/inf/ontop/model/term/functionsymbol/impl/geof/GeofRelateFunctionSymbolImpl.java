package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;


public class GeofRelateFunctionSymbolImpl extends AbstractGeofBooleanFunctionSymbolImpl {

    public GeofRelateFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdStringType, RDFDatatype xsdBooleanType) {
        super("GEOF_RELATE", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType, xsdStringType), xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBRelate(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2));
    }
}
