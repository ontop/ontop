package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
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

public class GeofDistanceFunctionSymbolImpl extends AbstractGeofDoubleFunctionSymbolImpl {

    FunctionSymbolFactory functionSymbolFactory;
    public static final String defaultSRID = "OGC/1.3/CRS84";
    public static final String defaultEPSG = "EPSG/0/4326";

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

        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();
        ImmutableTerm term0 = subLexicalTerms.get(0);
        ImmutableTerm term1 = subLexicalTerms.get(1);

        //CASE 1: SRIDs re both defined
        //NOTE 1: CRS84 & EPSG4326 not interchangeable, CRS84 & NoSRID not interchangeable
        //NOTE 2: WGS84 considered equivalent only to CRS84 and EPSG4326
        // Check if both are non-ground functional terms
        if (term0 instanceof NonGroundFunctionalTerm && term1 instanceof NonGroundFunctionalTerm) {
            NonGroundFunctionalTerm f0 = (NonGroundFunctionalTerm) term0;
            FunctionSymbol fs0 = f0.getFunctionSymbol();
            NonGroundFunctionalTerm f1 = (NonGroundFunctionalTerm) term1;
            FunctionSymbol fs1 = f1.getFunctionSymbol();
            if (fs0 instanceof DBConcatFunctionSymbol && fs1 instanceof DBConcatFunctionSymbol) {
                // DBConcatFunctionSymbol concat = (DBConcatFunctionSymbol) fs;
                if (f0.getTerm(0) instanceof DBConstant && f1.getTerm(0) instanceof DBConstant) {
                    // Retrieve IRI as string
                    DBConstant t0 = (DBConstant) f0.getTerm(0);
                    String tt0 = t0.getValue();
                    DBConstant t1 = (DBConstant) f1.getTerm(0);
                    String tt1 = t1.getValue();
                    if (tt0.startsWith("<") && tt0.indexOf(">") > 0 &&
                            tt1.startsWith("<") && tt1.indexOf(">") > 0) {
                        // Retrieve SRIDs as strings
                        String srid0 = tt0.substring(32, tt0.indexOf(">"));
                        String srid1 = tt1.substring(32, tt1.indexOf(">"));
                        // Check srid-s are identical
                        if(srid0.equals(srid1)) {
                            // Retrieve geometries
                            ImmutableTerm geom0 = f0.getTerm(1);
                            ImmutableTerm geom1 = f1.getTerm(1);
                            if (unit.equals(UOM.METRE.getIRIString())) {
                                if (srid0.equals(defaultSRID)) {
                                    // Flip Coordinates for CRS84, since default is EPSG4326
                                    return termFactory.getDBSTDistanceSphere(
                                            termFactory.getDBSTFlipCoordinates(geom0),
                                            termFactory.getDBSTFlipCoordinates(geom1));
                                } else if (srid0.equals(defaultEPSG)) {
                                    // Reverse order for EPSG 4326
                                    return termFactory.getDBSTDistanceSphere(geom0, geom1);
                                } else {
                                    return termFactory.getDBSTDistance(geom0, geom1);
                                }
                            } else if (unit.equals(UOM.DEGREE.getIRIString())) {
                                // ST_DISTANCE
                                return termFactory.getDBSTDistance(geom0, geom1);
                            } else if (unit.equals(UOM.RADIAN.getIRIString())) {
                                // ST_DISTANCE / 180 * PI
                                double ratio = PI / 180;
                                DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
                                DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
                                DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());
                                ImmutableTerm distanceInDegree = termFactory.getDBSTDistance(geom0, geom1);
                                return termFactory.getImmutableFunctionalTerm(times, distanceInDegree, termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType()));
                            } else {
                                throw new IllegalArgumentException("Unexpected unit: " + unit);
                            }
                        } else {
                            throw new IllegalArgumentException("SRIDs do not match");
                        }
                    }
                }
            }
        }

        // CASE 2 - No SRID is defined
        if (unit.equals(UOM.METRE.getIRIString())) {
            // ST_DISTANCESPHERE
            // Flip Coordinates for CRS84, since default is EPSG4326
            return termFactory.getDBSTDistanceSphere(//subLexicalTerms.get(0), subLexicalTerms.get(1));
                    termFactory.getDBSTFlipCoordinates(subLexicalTerms.get(0)),
                    termFactory.getDBSTFlipCoordinates(subLexicalTerms.get(1)));
        } else if (unit.equals(UOM.DEGREE.getIRIString())) {
            // ST_DISTANCE
            return termFactory.getDBSTDistance(subLexicalTerms.get(0), subLexicalTerms.get(1));
        } else if (unit.equals(UOM.RADIAN.getIRIString())) {
            // ST_DISTANCE / 180 * PI
            double ratio = PI / 180;
            DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
            DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
            DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());
            ImmutableTerm distanceInDegree = termFactory.getDBSTDistance(subLexicalTerms.get(0), subLexicalTerms.get(1));
            return termFactory.getImmutableFunctionalTerm(times, distanceInDegree, termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType()));
        } else {
            throw new IllegalArgumentException("Unexpected unit: " + unit);
        }

    }
}
