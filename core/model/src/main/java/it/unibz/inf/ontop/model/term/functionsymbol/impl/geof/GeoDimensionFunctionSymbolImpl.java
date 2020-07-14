package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeoDimensionFunctionSymbolImpl extends AbstractGeofIntegerFunctionSymbolImpl {
    public GeoDimensionFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdIntegerType) {
        super("GEO_DIMENSION", functionIRI, ImmutableList.of(wktLiteralType), xsdIntegerType);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBAsText(termFactory.getDBDimension(subLexicalTerms.get(0)));
    }
}
