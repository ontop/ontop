package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
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

    /**
     * The default GeoSPARQL SRID - CRS84
     */
    public static final IRI defaultSRID = rdfFactory.createIRI("http://www.opengis.net/def/crs/OGC/1.3/CRS84");


    /**
     * Method returns the WKTLiteralValue constructed using the SRID and geometry
     * SRID: If no SRID defined, default CRS84 SRID is assigned. If the input is provided at query time
     * the SRID is extracted from a constant. Otherwise minimal parsing used to extract from template.
     * Geometry: If constant input at query tie {@link #tryExtractGeometryFromConstant(ImmutableTerm, TermFactory, IRI)}
     * Geometry: If DB attribute {@link #tryExtractGeometryFromTemplate(TermFactory, ImmutableTerm)}
     *
     * @param termFactory
     * @param wktLiteralTerm
     * @return WKTLiteralValue which is composed of the SRID and geometry
     */
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

    /**
     * Extracts the SRID from a geometry input at SPARQL query time by a user
     * e.g. from input "<http://www.opengis.net/def/crs/EPSG/0/3044> POINT(6.6441878 49.7596208)"
     * output is 3044
     *
     * @param immutableTerm
     * @return SRID
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
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

    /**
     * Extract SRID from any geospatial template
     *
     * @param wktLiteralTerm
     * @return SRID
     */
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

    /**
     * Method extracts geometry from constant input at query time by user.
     * The SRID of the input is set to match that of the SRID IRI template or default SRID if none provided.
     * e.g. for geof:distance("<http://www.opengis.net/def/crs/EPSG/0/3044> POINT(6.6441878 49.7596208)", geom1)
     * the SRID of POINT(6.6441878 49.7596208) has to be set to 3044
     * Method used in DB is ST_SETSRID and must be specified with the geometry in this instance
     * If no SRID IRI is provided, the default SRID of 4326 is set (since some DBs would otherwise set this to 0)
     *
     * @param immutableTerm
     * @param termFactory
     * @param srid
     * @return geometry with respective srid setting (either derived from SRID IRI or defaultSRID)
     */
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

    /**
     * Method extracts geometry from non-constant database data.
     * Drop any SRID template provided by user at query time by removing the first term.
     *
     * @param termFactory
     * @param wktLiteralTerm
     * @return geometry without SRID
     */
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
                        // return geometry which is index = 1 (special case since DBNullRejConcat needs 2+ args)
                        if (t.getTerms().size() == 2)
                            return t.getTerm(1);
                        else
                            // Otherwise drop the SRID template, and return everything else
                            return termFactory.getNullRejectingDBConcatFunctionalTerm(
                                    t.getTerms()
                                            .stream()
                                            // If argument with SRID, drop SRID, otherwise keep terms
                                            .map(subterm -> subterm.toString().contains("<")
                                                    ? extractConstantWKTLiteralValue(termFactory, subterm).get()
                                                    : subterm)
                                            .collect(ImmutableCollectors.toList()));
                    }
                });
    }

    /**
     * Check whether the template starts with SRID e.g. {@code <http://www.opengis.net/def/crs/OGC/1.3/CRS84>}
     *
     * @param immutableTerm
     * @return boolean yes=template starts with SRID
     */
    private static boolean templateStartsWithSRID(ImmutableTerm immutableTerm) {
        return Optional.of(immutableTerm)
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                .isPresent();
    }

    /**
     * Method extracts geometry without any SRID information.
     * This includes any template {GEOM} or {Lng}{Lat} which has a preceding SRID IRI
     *
     * @param termFactory
     * @param immutableTerm
     * @return geometry string without SRID
     */
    static Optional<ImmutableTerm> extractConstantWKTLiteralValue(TermFactory termFactory, ImmutableTerm immutableTerm) {
        // Get the respective geometry
        return Optional.of(immutableTerm)
                // template is NOT a NonGroundFunctionalTerm, but a string user input
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // extract the geometry out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">") + 1).trim()));
    }

    /**
     * Generates the abbreviated SRID from the full SRID IRI
     * Distinguishes between the default SRID CRS84 and other SRIDs with prefix EPSG
     *
     * @param sridIRIString
     * @return Abbreviated SRID string
     */
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

    /**
     * Based on the abbreviated SRID denomination deduces the unit of the distance measure
     * The units generally include metre, degree and radian
     *
     * @param sridIRIString
     * @return unit of distance
     */
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
