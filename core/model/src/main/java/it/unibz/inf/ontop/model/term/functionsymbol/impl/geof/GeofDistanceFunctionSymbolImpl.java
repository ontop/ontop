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
import org.apache.commons.rdf.api.Literal;

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
        ImmutableTerm[] subLexicalGeoms = {subLexicalTerms.get(0), subLexicalTerms.get(1)};
        // Array to store SRIDs
        String[] srids = new String[2];
        int nextSRIDIndex = 0;
        // Array to store geometries
        ImmutableTerm[] geom = new ImmutableTerm[2];
        int nextGeomIndex = 0;

        //STEP 1: Retrieve SRIDs and the respective geometry representation
        //NOTE 1: CRS84 & EPSG4326 considered different SRID, but CRS84 & NoSRID the same
        //NOTE 2: WGS84 considered equivalent only to CRS84 and EPSG4326
        for (ImmutableTerm term : subLexicalGeoms) {
            if (term instanceof NonGroundFunctionalTerm) {
                NonGroundFunctionalTerm f0 = (NonGroundFunctionalTerm) term;
                FunctionSymbol fs0 = f0.getFunctionSymbol();
                // For cases where a template is defined, retrieve SRID and add to array
                if (fs0 instanceof DBConcatFunctionSymbol) {
                    // DBConcatFunctionSymbol concat = (DBConcatFunctionSymbol) fs;
                    if (f0.getTerm(0) instanceof DBConstant) {
                        // Retrieve IRI as string
                        DBConstant t0 = (DBConstant) f0.getTerm(0);
                        String tt0 = t0.getValue();
                        if (tt0.startsWith("<") && tt0.indexOf(">") > 0) {
                            // Retrieve SRIDs as strings
                            srids[nextSRIDIndex] = tt0.substring(32, tt0.indexOf(">"));
                        } else {
                            srids[nextSRIDIndex] = defaultSRID;
                        }
                        nextSRIDIndex++;
                    }
                    // For cases with a template, save the geometry for analysis
                    geom[nextGeomIndex] = f0.getTerm(1);
                } else {
                    // Cases with no template, set SRID to default and save geometry
                    srids[nextSRIDIndex] = defaultSRID;
                    geom[nextGeomIndex] = subLexicalTerms.get(nextGeomIndex);
                    nextSRIDIndex++;
                }
            } else {
                // Cases with user geometry input in query
                // NOTE: Cannot deal with cases when there is a template
                srids[nextSRIDIndex] = defaultSRID;
                geom[nextGeomIndex] = subLexicalTerms.get(nextGeomIndex);
                nextSRIDIndex++;
            }
            nextGeomIndex++;
        }

        // STEP 2: If the SRIDs do match, calculate distance, otherwise throw exception
        if(srids[0].equals(srids[1])) {
            if (unit.equals(UOM.METRE.getIRIString())) {
                if (srids[0].equals(defaultSRID)) {
                    // Reverse order for EPSG 4326
                    return termFactory.getDBSTDistanceSphere(geom[0], geom[1]);
                } else if (srids[0].equals(defaultEPSG)) {
                    return termFactory.getDBSTDistanceSphere(geom[0], geom[1]);
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
}
