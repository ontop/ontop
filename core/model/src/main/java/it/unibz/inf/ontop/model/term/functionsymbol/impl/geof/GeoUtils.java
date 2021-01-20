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


    static Optional<ImmutableTerm> tryExtractGeometryFromConstant(ImmutableTerm immutableTerm, TermFactory termFactory) {
        return Optional.of(immutableTerm)
                // template is NOT a NonGroundFunctionalTerm, but a string user input
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the geometry out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">") + 1).trim()));
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

    static WKTLiteralValue extractWKTLiteralValue(TermFactory termFactory, ImmutableTerm wktLiteralTerm) {
        // Get the respective SRIDs
        IRI srid = tryExtractSRIDFromDbConstant(Optional.of(wktLiteralTerm))
                .orElseGet(
                        // template
                        () -> tryExtractSRIDFromDbConstant(tryExtractArgFromTemplate(wktLiteralTerm, 0))
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID)
                );

        // Get the respective geometries
        ImmutableTerm geometry = tryExtractGeometryFromConstant(wktLiteralTerm, termFactory)
                .orElseGet(
                        // If template then
                        () -> tryExtractArgFromTemplate(wktLiteralTerm, 1)
                                .orElse(wktLiteralTerm)
                );

        return new WKTLiteralValue(srid, geometry);
    }

    public static String toProj4jName(String sridIRIString) {

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
//
//            if (proj4JUnit.equals(Units.METRES)) {
//                return DistanceUnit.METRE;
//            } else if (proj4JUnit.equals(Units.DEGREES)) {
//                return DistanceUnit.DEGREE;
//            } else if (proj4JUnit.equals(Units.RADIANS)) {
//                return DistanceUnit.RADIAN;
//            } else {
//                throw new IllegalArgumentException("Unsupported unit: " + proj4JUnit);
//            }
        }

    }
}
