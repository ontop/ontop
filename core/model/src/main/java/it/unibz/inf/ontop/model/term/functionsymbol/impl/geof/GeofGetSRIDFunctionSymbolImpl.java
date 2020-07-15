package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeofGetSRIDFunctionSymbolImpl extends AbstractGeofIRIFunctionSymbolImpl {

    public GeofGetSRIDFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdAnyUri) {
        super("GEOF_GETSRID", functionIRI, ImmutableList.of(wktLiteralType), xsdAnyUri);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBAsText(termFactory.getDBGetSRID(subLexicalTerms.get(0)));
        //return termFactory.getDBGetSRID(subLexicalTerms.get(0));
    }
}