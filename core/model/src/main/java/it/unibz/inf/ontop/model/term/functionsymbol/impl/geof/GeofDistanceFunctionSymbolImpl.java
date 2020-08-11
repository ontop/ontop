package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolFactoryImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

import java.util.Optional;

import static java.lang.Math.PI;

import org.apache.sis.internal.referencing.provider.EPSGName;
//import org.apache.sis.referencing.*;
//import org.apache.sis.referencing.factory.sql.EPSGFactory;
import org.apache.sis.referencing.CRS;
//import org.apache.sis.non-free:sis-epsg;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import org.opengis.referencing.crs.GeographicCRS;
//import org.apache.sis.referencing.factory.*;
//import org.apache.sis.referencing.factory.IdentifiedObjectFinder;
//import org.opengis.util.FactoryException.*;

public class GeofDistanceFunctionSymbolImpl extends AbstractGeofDoubleFunctionSymbolImpl {

    FunctionSymbolFactory functionSymbolFactory;
    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    //public static final String defaultEPSG = "http://www.opengis.net/def/crs/EPSG/0/4326";
    public static final String defaultEllipsoid = "WGS 84";

    public GeofDistanceFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, ObjectRDFType iriType, RDFDatatype xsdDoubleType, FunctionSymbolFactoryImpl functionSymbolFactory) {
        super("GEOF_DISTANCE", functionIRI,
                ImmutableList.of(wktLiteralType, wktLiteralType, iriType),
                xsdDoubleType);
        this.functionSymbolFactory = functionSymbolFactory;
    }


    /**
     * @param subLexicalTerms (geom1, geom2, unit)
     * NB: we assume that the geoms are WGS 84 (lat lon). Other SRIDs need to be implemented.
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        // Get unit of distance i.e. degree, radian, metric;
        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();

        ImmutableTerm[] subLexicalGeoms = {subLexicalTerms.get(0), subLexicalTerms.get(1)};
        String[] sridString = new String[2];
        ImmutableTerm[] geom = new ImmutableTerm[2];
        int sridIndex = 0;

        // Retrieve the SRIDs for each geometry
        for (ImmutableTerm term : subLexicalGeoms) {
            // Get the respective SRIDs
            sridString[sridIndex] = getSRIDFromDbConstant(Optional.of(term))
                    .orElseGet(
                            // template
                            () -> getSRIDFromDbConstant(getArg0FromTemplate(term))
                                    // otherwise, returns the default SRID
                                    .orElse(defaultSRID));

            // Get the respective geometries
            geom[sridIndex] = getArg1FromUserInput(term, termFactory)
                    .orElseGet(
                            // If template then
                            () -> getArg1FromTemplate(term)
                                    .orElse(term));
            sridIndex++;
        }


        // Given the SRID - retrieve the respective ellipsoid
        String ellipsoidString;
        String SRIDcode;
        if (getCRS(sridString[0])) {
            //SRIDcode = "CRS:84";
            ellipsoidString = defaultEllipsoid;
        } else {

            long start = System.currentTimeMillis();
            //execute logic in between

            //Other EPSG codes
            SRIDcode = "EPSG:" + sridString[0].substring(sridString[0].length()-4);
            try {
                ellipsoidString = getEllipsoid(SRIDcode);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported or invalid SRID provided");
            }
            long end = System.currentTimeMillis();
            System.out.println("DEBUG: Logic A took " + (end - start) + " MilliSeconds");
        }


        // If SRIDs are not identical throw error
        if(sridString[0].equals(sridString[1])) {
            // Check unit
            if (unit.equals(UOM.METRE.getIRIString())) {
                // If metric and WGS84, use ST_DISTANCESPHERE
                if (ellipsoidString.equals(defaultEllipsoid)) {
                    //final String measurement_spheroid = "SPHEROID[\"WGS 84\",6378137,298.257223563]";
                    //return termFactory.getDBSTDistanceSpheroid(geom[0], geom[1],termFactory.getDBStringConstant(measurement_spheroid));
                    return termFactory.getDBSTDistanceSphere(geom[0], geom[1]);
                // If non-WGS84, use Cartesian distance
                } else {
                    return termFactory.getDBSTDistance(geom[0], geom[1]);
                }
            } else if (unit.equals(UOM.DEGREE.getIRIString())) {
                // ST_DISTANCE
                return termFactory.getDBSTDistance(geom[0], geom[1]);
            } else if (unit.equals(UOM.RADIAN.getIRIString())) {
                // ST_DISTANCE / 180 * PI
                double ratio = PI / 180;
                DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
                DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
                DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());
                ImmutableTerm distanceInDegree = termFactory.getDBSTDistance(geom[0], geom[1]);
                return termFactory.getImmutableFunctionalTerm(times, distanceInDegree, termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType()));
            } else {
                throw new IllegalArgumentException("Unexpected unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("SRIDs do not match");
        }
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

    private Optional<ImmutableTerm> getArg1FromTemplate(ImmutableTerm term) {
        return Optional.of(term)
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // the first argument is the string starting with the IRI of the SRID
                .map(t -> t.getTerm(1));
    }

    private Optional<ImmutableTerm> getArg1FromUserInput(ImmutableTerm immutableTerm, TermFactory termFactory) {
        return Optional.of(immutableTerm)
                // template is NOT a NonGroundFunctionalTerm, but a string user input
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the geometry out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">")+1)));
    }

    private String getEllipsoid(String v) throws Exception{
        // Retrieve coordinate reference system and respective ellipsoid
        CoordinateReferenceSystem source = CRS.forCode(v);
        return (source.getName().getCode());
    }

    private boolean getCRS(String sridval) {
        // Check whether it is the default CRS
        return sridval
                .contains("CRS84");
    }
}
