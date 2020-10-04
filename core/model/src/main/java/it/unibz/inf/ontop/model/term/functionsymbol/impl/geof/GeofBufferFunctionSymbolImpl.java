package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;
//import org.apache.sis.referencing.CRS;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.annotation.Nonnull;

import static java.lang.Math.PI;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {
    FunctionSymbolFactory functionSymbolFactory;
    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    //public static final String defaultEPSG = "http://www.opengis.net/def/crs/EPSG/0/4326";
    public static final String defaultEllipsoid = "WGS 84";

    public GeofBufferFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype decimalType, ObjectRDFType iriType) {
        super("GEOF_BUFFER", functionIRI, ImmutableList.of(wktLiteralType, decimalType, iriType), wktLiteralType);
    }

    /**
     * @param subLexicalTerms (geom, distance, unit)
     *                        NB: we assume that the geom is in WGS 84 (lat lon). Other SRIDs need to be implemented.
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();
        ImmutableTerm term = subLexicalTerms.get(0);

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());

        WKTLiteralValue WKTLiteralValue = GeoUtils.extractWKTLiteralValue(termFactory, term);
        IRI sridString = WKTLiteralValue.getSRID();
        ImmutableTerm geom = WKTLiteralValue.getGeometry();

        // Given the SRID - retrieve the respective ellipsoid
        String ellipsoidString;
        String SRIDcode;
        //if (getCRS(sridString)) {
            //SRIDcode = "CRS:84";
            ellipsoidString = defaultEllipsoid;
//        } else {
//            //Other EPSG codes
//            SRIDcode = "EPSG:" + sridString.substring(sridString.length()-4);
//            try {
//                ellipsoidString = getEllipsoid(SRIDcode);
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Unsupported or invalid SRID provided");
//            }
//        }

        if (unit.equals(UOM.METRE.getIRIString())) {
            final double EARTH_MEAN_RADIUS_METER = 6370986;
            final double ratio = 180 / PI / EARTH_MEAN_RADIUS_METER;
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
            // If WGS84, return spheroid
            if (ellipsoidString.equals(defaultEllipsoid)) {
                return termFactory.getDBBuffer(geom, distanceInDegree).simplify();
            } else {
                return termFactory.getDBBuffer(geom, subLexicalTerms.get(1)).simplify();
            }
        } else if (unit.equals(UOM.DEGREE.getIRIString())) {
            // ST_BUFFER
            return termFactory.getDBBuffer(geom, subLexicalTerms.get(1)).simplify();
        } else if (unit.equals(UOM.RADIAN.getIRIString())) {
            // ST_AsTexT(ST_BUFFER(geom, distance))
            final double ratio = 180 / PI;
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
            return termFactory.getDBBuffer(geom, distanceInDegree).simplify();
        } else {
            throw new IllegalArgumentException("Unexpected unit: " + unit);
        }

    }

}
