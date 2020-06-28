package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {
    public GeofBufferFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype decimalType) {
        super("GEOF_BUFFER", functionIRI, ImmutableList.of(wktLiteralType, decimalType), wktLiteralType);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        return termFactory.getDBAsText(
                termFactory.getDBBuffer(subLexicalTerms.get(0), subLexicalTerms.get(1))
        );

    }
}
