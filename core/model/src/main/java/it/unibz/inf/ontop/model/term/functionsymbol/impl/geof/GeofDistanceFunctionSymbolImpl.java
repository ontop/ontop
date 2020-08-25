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
     * NB: we assume that the geoms are WGS 84 (lat lon). Other SRIDs need to be implemented.
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        // Get unit of distance i.e. degree, radian, metric;
        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();

        List<SridGeomPair> sridGeomPairs = subLexicalTerms.subList(0, 2).stream().map(
                term -> GeoUtils.getSridGeomPair(termFactory, term)
        ).collect(Collectors.toList());

        String srid0 = sridGeomPairs.get(0).getSrid();
        String srid1 = sridGeomPairs.get(1).getSrid();
        ImmutableTerm geom0 = sridGeomPairs.get(0).getGeometry();
        ImmutableTerm geom1 = sridGeomPairs.get(1).getGeometry();

        String ellipsoidString = getEllipsoidString(srid0);

        // If SRIDs are not identical throw error
        if (!srid0.equals(srid1)) {
            throw new IllegalArgumentException("SRIDs do not match");
        } else {
            // Check unit
            switch (unit){
                case UOM.METRE_STRING:
                    if (ellipsoidString.equals(defaultEllipsoid)) {
                        //final String measurement_spheroid = "SPHEROID[\"WGS 84\",6378137,298.257223563]";
                        //return termFactory.getDBSTDistanceSpheroid(geom[0], geom[1],termFactory.getDBStringConstant(measurement_spheroid));
                        return termFactory.getDBSTDistanceSphere(geom0, geom1).simplify();
                        // If non-WGS84, use Cartesian distance
                    } else {
                        return termFactory.getDBSTDistance(geom0, geom1).simplify();
                    }
                case UOM.DEGREE_STRING:
                    // ST_DISTANCE
                    return termFactory.getDBSTDistance(geom0, geom1).simplify();
                case UOM.RADIAN_STRING:
                    // ST_DISTANCE / 180 * PI
                    double ratio = PI / 180;
                    DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
                    DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
                    DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());
                    ImmutableTerm distanceInDegree = termFactory.getDBSTDistance(geom0, geom1);
                    DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
                    return termFactory.getImmutableFunctionalTerm(times, distanceInDegree, ratioConstant);
                default:
                    throw new IllegalArgumentException("Unexpected unit: " + unit);
            }

        }
    }

    private String getEllipsoidString(String srid0) {
        // Given the SRID - retrieve the respective ellipsoid
        String ellipsoidString;
        String SRIDcode;

        // Check whether it is the default CRS
        if (srid0.contains("CRS84")) {
            //SRIDcode = "CRS:84";
            ellipsoidString = defaultEllipsoid;
        } else {
            //Other EPSG codes
            SRIDcode = "EPSG:" + srid0.substring(srid0.length()-4);
            try {
                ellipsoidString = getEllipsoid(SRIDcode);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported or invalid SRID provided");
            }
        }
        return ellipsoidString;
    }

    private String getEllipsoid(String v) throws Exception{
        // Retrieve coordinate reference system and respective ellipsoid
        CoordinateReferenceSystem source = CRS.forCode(v);
        return source.getName().getCode();
    }

}
