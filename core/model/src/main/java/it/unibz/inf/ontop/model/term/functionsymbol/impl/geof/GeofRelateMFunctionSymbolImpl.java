package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeofRelateMFunctionSymbolImpl extends AbstractGeofStringFunctionSymbolImpl {

    public GeofRelateMFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdStringType) {
        super("GEOF_RELATEM", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType), xsdStringType);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBRelateMatrix(subLexicalTerms.get(0), subLexicalTerms.get(1));
    }
}
