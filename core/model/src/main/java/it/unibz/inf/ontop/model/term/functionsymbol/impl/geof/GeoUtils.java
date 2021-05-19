package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.units.Unit;

import java.util.Optional;

public class GeoUtils {

    static final double EARTH_MEAN_RADIUS_METER = 6370986;

    private static final RDF rdfFactory = new SimpleRDF();
    public static final IRI defaultSRID = rdfFactory.createIRI("http://www.opengis.net/def/crs/OGC/1.3/CRS84");

    static WKTLiteralValue extractWKTLiteralValue(TermFactory termFactory, ImmutableTerm wktLiteralTerm) {
        // Get the respective SRID
        IRI srid = tryExtractSRIDFromDbConstant(Optional.of(wktLiteralTerm))
                .orElseGet(
                        // template
                        () -> tryExtractSRIDFromTemplate(wktLiteralTerm)
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID)
                );

        // Get the respective geometry
        ImmutableTerm geometry = tryExtractGeometryFromConstant(wktLiteralTerm, termFactory, srid)
                .orElseGet(
                        // If template then
                        () -> tryExtractGeometryFromTemplate(termFactory, wktLiteralTerm)
                                .orElse(wktLiteralTerm)                         
                );

        return new WKTLiteralValue(srid, geometry);
    }

    static Optional<IRI> tryExtractSRIDFromDbConstant(Optional<ImmutableTerm> immutableTerm) {
        return immutableTerm
                // the first argument has to be a constant
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the SRID out of the string
                .map(v -> v.substring(1, v.indexOf(">")))
                .map(rdfFactory::createIRI);
    }

    private static Optional<IRI> tryExtractSRIDFromTemplate(ImmutableTerm wktLiteralTerm) {
        Optional<ImmutableTerm> firstTermFromTemplate = Optional.of(wktLiteralTerm)
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // Always return the first argument
                .map(t -> t.getTerm(0));
        // check if the first argument is the string starting with the IRI of the SRID
        return tryExtractSRIDFromDbConstant(firstTermFromTemplate);
    }

    static Optional<ImmutableTerm> tryExtractGeometryFromConstant(ImmutableTerm immutableTerm, TermFactory termFactory, IRI srid) {

        // Find which SRID to set, constants have SRID=0 in PostG
        final String EPSG_PREFIX = "EPSG:";
        String sridProj4j = toProj4jName(srid.getIRIString());
        DBConstant newEPSG = (sridProj4j.equals("CRS:84"))
                ? termFactory.getDBStringConstant("4326")
                : termFactory.getDBStringConstant(sridProj4j.substring(EPSG_PREFIX.length()));

        return Optional.of(immutableTerm)
                // template is NOT a NonGroundFunctionalTerm, but a string user input
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // extract the geometry out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">") + 1).trim()))
                // Use GeomFromText with one argument to convert text into geometry
                .map(termFactory::getDBSTGeomFromText)
                // Set SRID
                .map(v -> termFactory.getDBSTSetSRID(v, newEPSG))
                // Convert to text
                .map(termFactory::getDBAsText);
    }

    private static Optional<ImmutableTerm> tryExtractGeometryFromTemplate(TermFactory termFactory, ImmutableTerm wktLiteralTerm) {
        return Optional.of(wktLiteralTerm)
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // check if the first argument is the string starting with the IRI of the SRID
                .map(t -> {
                    // if the term does not start with the SRID, the whole term is geometry
                    if (!templateStartsWithSRID(t.getTerm(0))) {
                        return wktLiteralTerm;
                    }
                    // SRID + geometry
                    else {
                        // return geometry which is index = 1 (special case since DBConcat below reqs 2 + args)
                        if (t.getTerms().size() == 2)
                            return t.getTerm(1);
                        else
                            // Otherwise drop the SRID template, and return everything else
                            return termFactory.getDBSTMakePoint(t.getTerms().get(1), t.getTerms().get(3));
                    }
                });
    }

    /**
     * Check whether the template starts with SRID e.g. {@code <http://www.opengis.net/def/crs/OGC/1.3/CRS84>}
     */
    private static boolean templateStartsWithSRID(ImmutableTerm immutableTerm) {
        return Optional.of(immutableTerm)
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                .isPresent();
    }

    static Optional<ImmutableTerm> extractConstantWKTLiteralValue(TermFactory termFactory, ImmutableTerm immutableTerm) {
        // Get the respective geometry
        return Optional.of(immutableTerm)
                // template is NOT a NonGroundFunctionalTerm, but a string user input
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // extract the geometry out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">") + 1).trim()));
    }
    
    static String toProj4jName(String sridIRIString) {
        final String CRS_PREFIX = "http://www.opengis.net/def/crs/OGC/1.3/CRS";
        final String EPSG_PREFIX = "http://www.opengis.net/def/crs/EPSG/0/";

        if (sridIRIString.startsWith(CRS_PREFIX)) {
            return "CRS:" + sridIRIString.substring(CRS_PREFIX.length());
        } else if (sridIRIString.startsWith(EPSG_PREFIX)) {
            return "EPSG:" + sridIRIString.substring(EPSG_PREFIX.length());
        }

        // TODO: other cases
        throw new IllegalArgumentException("Unknown SRID IRI: " + sridIRIString);
    }

    public static DistanceUnit getUnitFromSRID(String sridIRIString) {
        String csName = toProj4jName(sridIRIString);
        if (csName.equals("CRS:84")) {
            return DistanceUnit.DEGREE;
        } else if (csName.startsWith("CRS:")) {
            throw new IllegalArgumentException("Unknown SRID IRI: " + sridIRIString);
        } else {
            CRSFactory csFactory = new CRSFactory();
            CoordinateReferenceSystem crs = csFactory.createFromName(csName);
            Unit proj4JUnit = crs.getProjection().getUnits();
            return DistanceUnit.findByName(proj4JUnit.name);
        }
    }
}
