package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;

import java.util.Optional;

public class GeoUtils {
    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    static Optional<ImmutableTerm> tryExtractGeometryFromConstant(ImmutableTerm immutableTerm, TermFactory termFactory) {
        return Optional.of(immutableTerm)
                // template is NOT a NonGroundFunctionalTerm, but a string user input
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the geometry out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">") + 1)));
    }

    static Optional<ImmutableTerm> tryExtractArgFromTemplate(ImmutableTerm term, int index) {
        return Optional.of(term)
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // the first argument is the string starting with the IRI of the SRID
                .map(t -> t.getTerm(index));
    }

    static Optional<String> tryExtractSRIDFromDbConstant(Optional<ImmutableTerm> immutableTerm) {
        return immutableTerm
                // the first argument has to be a constant
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the SRID out of the string
                .map(v -> v.substring(1, v.indexOf(">")));
    }

    static SridGeomPair getSridGeomPair(TermFactory termFactory, ImmutableTerm term) {
        // Get the respective SRIDs
        String srid = tryExtractSRIDFromDbConstant(Optional.of(term))
                .orElseGet(
                        // template
                        () -> tryExtractSRIDFromDbConstant(tryExtractArgFromTemplate(term, 0))
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID)
                );

        // Get the respective geometries
        ImmutableTerm geometry = tryExtractGeometryFromConstant(term, termFactory)
                .orElseGet(
                        // If template then
                        () -> tryExtractArgFromTemplate(term, 1)
                                .orElse(term)
                );

        return new SridGeomPair(srid, geometry);
    }
}
