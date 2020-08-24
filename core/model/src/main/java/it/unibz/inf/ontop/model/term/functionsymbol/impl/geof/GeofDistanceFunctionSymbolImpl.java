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
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

import static java.lang.Math.PI;

//import org.apache.sis.referencing.*;
import org.apache.sis.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
            SridGeomPair pair = GeoUtils.getSridGeomPair(termFactory, term);
            sridString[sridIndex] = pair.getSrid();
            geom[sridIndex] = pair.getGeometry();
            sridIndex++;
        }

        // Given the SRID - retrieve the respective ellipsoid
        String ellipsoidString;
        String SRIDcode;
        if (getCRS(sridString[0])) {
            //SRIDcode = "CRS:84";
            ellipsoidString = defaultEllipsoid;
        } else {
            //Other EPSG codes
            SRIDcode = "EPSG:" + sridString[0].substring(sridString[0].length()-4);
            try {
                ellipsoidString = getEllipsoid(SRIDcode);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported or invalid SRID provided");
            }
        }


        // If SRIDs are not identical throw error
        if(sridString[0].equals(sridString[1])) {
            // Check unit
            if (unit.equals(UOM.METRE.getIRIString())) {
                // If metric and WGS84, use ST_DISTANCESPHERE
                if (ellipsoidString.equals(defaultEllipsoid)) {
                    //final String measurement_spheroid = "SPHEROID[\"WGS 84\",6378137,298.257223563]";
                    //return termFactory.getDBSTDistanceSpheroid(geom[0], geom[1],termFactory.getDBStringConstant(measurement_spheroid));
                    return termFactory.getDBSTDistanceSphere(geom[0], geom[1]).simplify();
                // If non-WGS84, use Cartesian distance
                } else {
                    return termFactory.getDBSTDistance(geom[0], geom[1]).simplify();
                }
            } else if (unit.equals(UOM.DEGREE.getIRIString())) {
                // ST_DISTANCE
                return termFactory.getDBSTDistance(geom[0], geom[1]).simplify();
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

    private String getEllipsoid(String v) throws Exception{
        // Retrieve coordinate reference system and respective ellipsoid
        CoordinateReferenceSystem source = CRS.forCode(v);
        return source.getName().getCode();
    }

    private boolean getCRS(String sridval) {
        // Check whether it is the default CRS
        return sridval.contains("CRS84");
    }
}
