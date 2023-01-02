package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class GeofConvexHullFunctionSymbolImpl extends AbstractUnaryGeofWKTFunctionSymbolDirectImpl {
    public GeofConvexHullFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType) {
        super("GEOF_CONVEXHULL", functionIRI, ImmutableList.of(wktLiteralType), wktLiteralType);
    }

    @Override
    public Function<ImmutableTerm, ImmutableTerm> getDBFunction(TermFactory termFactory) {
        return termFactory::getDBConvexHull;
    }

//    @Override
//    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
//        return termFactory.getDBAsText(termFactory.getDBConvexHull(subLexicalTerms.get(0)));
//    }
}
