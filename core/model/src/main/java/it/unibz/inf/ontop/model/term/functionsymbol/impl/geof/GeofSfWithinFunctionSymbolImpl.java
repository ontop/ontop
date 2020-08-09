package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonGroundFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GeofSfWithinFunctionSymbolImpl extends AbstractGeofBooleanFunctionSymbolImpl {

    public GeofSfWithinFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdBooleanType) {
        super("GEOF_SF_WITHIN", functionIRI, ImmutableList.of(wktLiteralType, wktLiteralType), xsdBooleanType);
    }

    @Override
    protected ImmutableTerm computeDBBooleanTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        return termFactory.getDBSTWithin(unwrapSTAsText(subLexicalTerms.get(0)), unwrapSTAsText(subLexicalTerms.get(1))).simplify();
    }

    private ImmutableTerm unwrapSTAsText(ImmutableTerm term) {
        return Optional.of(term)
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(NonGroundFunctionalTerm.class::cast)
                .filter(t -> t.getFunctionSymbol().getName().startsWith("ST_ASTEXT"))
                .map(t -> t.getTerm(0))
                .orElse(term);
    }
}
