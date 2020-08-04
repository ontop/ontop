package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

import static java.lang.Math.PI;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {
    FunctionSymbolFactory functionSymbolFactory;
    public static final String defaultSRID = "OGC/1.3/CRS84";
    public static final String defaultEPSG = "EPSG/0/4326";

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
        ImmutableTerm term0 = subLexicalTerms.get(0);

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());

        if (term0 instanceof NonGroundFunctionalTerm) {
            NonGroundFunctionalTerm f0 = (NonGroundFunctionalTerm) term0;
            FunctionSymbol fs0 = f0.getFunctionSymbol();
            if (fs0 instanceof DBConcatFunctionSymbol) {
                // DBConcatFunctionSymbol concat = (DBConcatFunctionSymbol) fs;
                if (f0.getTerm(0) instanceof DBConstant) {
                    // Retrieve IRI as string
                    DBConstant t0 = (DBConstant) f0.getTerm(0);
                    String tt0 = t0.getValue();
                    if (tt0.startsWith("<") && tt0.indexOf(">") > 0) {
                        // Retrieve SRIDs as strings
                        String srid0 = tt0.substring(32, tt0.indexOf(">"));
                        // Retrieve geometries
                        ImmutableTerm geom0 = f0.getTerm(1);
                        if (unit.equals(UOM.METRE.getIRIString())) {
                            final double EARTH_MEAN_RADIUS_METER = 6371008.7714;
                            final double ratio = 180 / PI / EARTH_MEAN_RADIUS_METER;
                            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
                            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);

                            if (srid0.equals(defaultSRID)) {
                                // Flip Coordinates for CRS84, since default is EPSG4326
                                return termFactory.getDBAsText(termFactory.getDBBuffer(
                                        termFactory.getDBSTFlipCoordinates(subLexicalTerms.get(0)), distanceInDegree));
                            } else if (srid0.equals(defaultEPSG)) {
                                // Reverse order for EPSG 4326
                                return termFactory.getDBAsText(termFactory.getDBBuffer(subLexicalTerms.get(0), distanceInDegree));
                            } else {
                                return termFactory.getDBAsText(termFactory.getDBBuffer(geom0, subLexicalTerms.get(1)));
                            }
                        } else if (unit.equals(UOM.DEGREE.getIRIString())) {
                            // ST_BUFFER
                            return termFactory.getDBAsText(termFactory.getDBBuffer(geom0, subLexicalTerms.get(1)));
                        } else if (unit.equals(UOM.RADIAN.getIRIString())) {
                            // ST_AsTexT(ST_BUFFER(geom, distance))
                            final double ratio = 180 / PI;
                            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
                            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
                            return termFactory.getDBAsText(termFactory.getDBBuffer(geom0, distanceInDegree));
                        } else {
                            throw new IllegalArgumentException("Unexpected unit: " + unit);
                        }
                    }
                }
            }
        }

        if (unit.equals(UOM.METRE.getIRIString())) {
            // ST_AsTexT(ST_BUFFER(geom, distance_m * 180 / (EARTH_MEAN_RADIUS_METER * PI)))
            /*
             * The International Union of Geodesy and Geophysics says the Earth's mean radius in M is:
             *
             * [1] http://en.wikipedia.org/wiki/Earth_radius
             */
            final double EARTH_MEAN_RADIUS_METER = 6371008.7714;
            final double ratio = 180 / PI / EARTH_MEAN_RADIUS_METER;
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
            return termFactory.getDBAsText(termFactory.getDBBuffer(subLexicalTerms.get(0), distanceInDegree));
        } else if (unit.equals(UOM.DEGREE.getIRIString())) {
            // ST_AsTexT(ST_BUFFER(geom, distance))
            return termFactory.getDBAsText(termFactory.getDBBuffer(subLexicalTerms.get(0), subLexicalTerms.get(1)));
        } else if (unit.equals(UOM.RADIAN.getIRIString())) {
            // ST_AsTexT(ST_BUFFER(geom, distance))
            final double ratio = 180 / PI;
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
            return termFactory.getDBAsText(termFactory.getDBBuffer(subLexicalTerms.get(0), distanceInDegree));
        } else {
            throw new IllegalArgumentException("Unexpected unit: " + unit);
        }
    }
}
