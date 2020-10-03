package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.units.Unit;
import org.locationtech.proj4j.units.Units;
//import org.osgeo.proj4j.CRSFactory;
//import org.osgeo.proj4j.CoordinateReferenceSystem;
//import org.osgeo.proj4j.CoordinateTransform;
//import org.osgeo.proj4j.CoordinateTransformFactory;


import java.util.Optional;

public class GeoUtils {
    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
//
//    private final CRSAuthorityFactory crsFactory;
//
//    /**
//     * The factory to use for finding operations between pairs of Coordinate Reference Systems.
//     * This factory must be provided by a GeoAPI implementation.
//     */
//    private final CoordinateOperationFactory opFactory;
//
//    /**
//     * Creates an instance using a GeoAPI implementation found on classpath.
//     * This initialization should be done only once and the factories reused
//     * as many times as necessary.
//     */
//    public MyApp() {
//        // Note: in GeoAPI 3.1/4.0, those two factories will be merged in a single one.
//        crsFactory = ServiceLoader.load(CRSAuthorityFactory.class).findFirst()
//                .orElseThrow(() -> new IllegalStateException("No GeoAPI implementation found"));
//        opFactory = ServiceLoader.load(CoordinateOperationFactory.class).findFirst()
//                .orElseThrow(() -> new IllegalStateException("No GeoAPI implementation found"));
//    }


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

    public static String toProj4jName(String sridIRIString) {

        String crsPrefix = "http://www.opengis.net/def/crs/OGC/1.3/CRS";

        String epsgPrefix = "http://www.opengis.net/def/crs/EPSG/0/";

        if (sridIRIString.startsWith(crsPrefix)) {
            return "CRS:" + sridIRIString.substring(crsPrefix.length());
        } else if (sridIRIString.startsWith(epsgPrefix)) {
            return "EPSG:" + sridIRIString.substring(epsgPrefix.length());
        }

        throw new IllegalArgumentException("Unknown SRID IRI");
    }

    public static Unit getUnit(String sridIRIString) {
        String csName = toProj4jName(sridIRIString);

        if(csName.startsWith("CRS:")){
            return Units.DEGREES;
        }

        CRSFactory csFactory = new CRSFactory();
        CoordinateReferenceSystem crs = csFactory.createFromName(csName);
        return crs.getProjection().getUnits();
    }
}
