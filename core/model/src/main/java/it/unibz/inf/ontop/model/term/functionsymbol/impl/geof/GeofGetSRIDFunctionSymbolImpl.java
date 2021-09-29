package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GeofGetSRIDFunctionSymbolImpl extends AbstractGeofIRIFunctionSymbolImpl {//AbstractGeofIRIFunctionSymbolImpl {

    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    public GeofGetSRIDFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype xsdAnyUri) {
        super("GEOF_GETSRID", functionIRI, ImmutableList.of(wktLiteralType), xsdAnyUri);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        ImmutableTerm term = subLexicalTerms.get(0);

        String sridString = getSRIDFromDbConstant(Optional.of(term))
                .orElseGet(
                        // template
                        () -> getSRIDFromDbConstant(getArg0FromTemplate(term))
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID));
        return termFactory.getDBStringConstant(sridString);
    }

    private Optional<ImmutableTerm> getArg0FromTemplate(ImmutableTerm term) {
        return Optional.of(term)
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // the first argument is the string starting with the IRI of the SRID
                .map(t -> t.getTerm(0));
    }

    private Optional<String> getSRIDFromDbConstant(Optional<ImmutableTerm> immutableTerm) {
        return immutableTerm
                // the first argument has to be a constant
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the SRID out of the string
                .map(v -> v.substring(1, v.indexOf(">")));
    }
}
