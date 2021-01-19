package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolFactoryImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

import static it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.DistanceUnit.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.GeoUtils.EARTH_MEAN_RADIUS_METER;

//import org.apache.sis.referencing.*;
//import org.apache.sis.referencing.CRS;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.List;
import java.util.stream.Collectors;

public class GeofDistanceFunctionSymbolImpl extends AbstractGeofDoubleFunctionSymbolImpl {

    FunctionSymbolFactory functionSymbolFactory;
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
     *                        NB: we assume that the geoms are WGS 84 (lat lon). Other SRIDs need to be implemented.
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {


        List<WKTLiteralValue> wktLiteralValues = subLexicalTerms.subList(0, 2).stream().map(
                term -> GeoUtils.extractWKTLiteralValue(termFactory, term)
        ).collect(Collectors.toList());

        IRI srid0 = wktLiteralValues.get(0).getSRID();
        IRI srid1 = wktLiteralValues.get(1).getSRID();

        if (!srid0.equals(srid1)) {
            throw new IllegalArgumentException("SRIDs do not match: " + srid0 + ", " + srid1);
        }

        ImmutableTerm geom0 = wktLiteralValues.get(0).getGeometry();
        ImmutableTerm geom1 = wktLiteralValues.get(1).getGeometry();

        //String ellipsoidString = getEllipsoidString(srid0);
        DistanceUnit inputUnit = GeoUtils.getUnitFromSRID(srid0.getIRIString());// DistanceUnit.fromIRI(srid0);
        DistanceUnit outputUnit = DistanceUnit.findByIRI(((DBConstant) subLexicalTerms.get(2)).getValue());

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator divides = dbFunctionSymbolFactory.getDBMathBinaryOperator("/", dbTypeFactory.getDBDoubleType());

        if (inputUnit == METRE && outputUnit == METRE) {
            return termFactory.getDBSTDistance(geom0, geom1).simplify();
        } else if (inputUnit == METRE && outputUnit == RADIAN) {
            ImmutableTerm distanceInMetre = termFactory.getDBSTDistance(geom0, geom1).simplify();
            DBConstant radiusConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, radiusConstant);
        } else if (inputUnit == METRE && outputUnit == DEGREE) {
            ImmutableTerm distanceInMetre = termFactory.getDBSTDistance(geom0, geom1).simplify();
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER / 180 * Math.PI), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, ratioConstant);
        } else if (inputUnit == DEGREE && outputUnit == DEGREE) {
            ImmutableTerm distanceInMetre = termFactory.getDBSTDistanceSphere(geom0, geom1).simplify();
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER / 180 * Math.PI), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, ratioConstant);
        } else if (inputUnit == DEGREE && outputUnit == RADIAN) {
            ImmutableTerm distanceInMetre = termFactory.getDBSTDistanceSphere(geom0, geom1).simplify();
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, ratioConstant);
        } else if (inputUnit == DEGREE && outputUnit == METRE) {
            // TODO: consider using getDBSTDistanceSpheroid to get more accurate results
            return termFactory.getDBSTDistanceSphere(geom0, geom1).simplify();
        } else {
            throw new IllegalArgumentException(String.format("Unsupported combination of units for distance. input: %s, output: %s", inputUnit, outputUnit));
        }

    }

//    private String getEllipsoidString(String srid0) {
//        // Given the SRID - retrieve the respective ellipsoid
//        String ellipsoidString;
//        String SRIDcode;
//
//        // Check whether it is the default CRS
//        if (srid0.contains("CRS84")) {
//            //SRIDcode = "CRS:84";
//            ellipsoidString = defaultEllipsoid;
//        } else {
//            //Other EPSG codes
//            SRIDcode = "EPSG:" + srid0.substring(srid0.length()-4);
//            try {
//                ellipsoidString = getEllipsoid(SRIDcode);
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Unsupported or invalid SRID provided");
//            }
//        }
//        return ellipsoidString;
//    }

//    private String getEllipsoid(String v) throws Exception{
//        // Retrieve coordinate reference system and respective ellipsoid
//        CoordinateReferenceSystem source = CRS.forCode(v);
//        return source.getName().getCode();
//    }

}
