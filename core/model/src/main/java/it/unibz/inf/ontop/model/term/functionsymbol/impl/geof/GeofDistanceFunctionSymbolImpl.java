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

import java.util.List;
import java.util.Optional;

import static java.lang.Math.PI;

public class GeofDistanceFunctionSymbolImpl extends AbstractGeofDoubleFunctionSymbolImpl {

    FunctionSymbolFactory functionSymbolFactory;
    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    public static final String defaultEPSG = "http://www.opengis.net/def/crs/EPSG/0/4326";

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
        //ImmutableList subLexicalGeoms = ImmutableList.of(subLexicalTerms.get(0), subLexicalTerms.get(1));
        ImmutableTerm[] subLexicalGeoms = {subLexicalTerms.get(0), subLexicalTerms.get(1)};
        // Array to store SRIDs
        String[] sridString = new String[2];
        int sridStringIndex = 0;
        // Array to store geometries
        //ImmutableTerm[] geom = new ImmutableTerm[2];
        int nextGeomIndex = 0;

        for (ImmutableTerm term : subLexicalGeoms) {
            sridString[sridStringIndex] = getSRIDFromDbConstant(Optional.of(term))
                    .orElseGet(
                            // template
                            () -> getSRIDFromDbConstant(getArg0FromTemplate(term))
                                    // otherwise, returns the default SRID
                                    .orElse(defaultSRID));
            sridStringIndex++;
        }

        ImmutableTerm geom0 = getArg1FromDbConstant(subLexicalTerms.get(0), termFactory)
                .orElseGet(
                        // Manutal input
                        () -> getArg1FromTemplate(subLexicalTerms.get(0))
                            .orElse(subLexicalTerms.get(0)));
        //ImmutableTerm geom1 = getArg1FromTemplate(subLexicalTerms.get(1)).orElse(subLexicalTerms.get(1));
        ImmutableTerm geom1 = getArg1FromDbConstant(subLexicalTerms.get(1), termFactory)
                .orElseGet(
                        // Manutal input
                        () -> getArg1FromTemplate(subLexicalTerms.get(1))
                                .orElse(subLexicalTerms.get(1)));
        /*for (ImmutableTerm term : subLexicalGeoms) {
            geom[nextGeomIndex] = (getArg1FromTemplate(term));
            nextGeomIndex++;
        }*/


        //sridString[0] = "CRS84";
        //sridString[1] = "CRS84";

        if(sridString[0].equals(sridString[1])) {
            if (unit.equals(UOM.METRE.getIRIString())) {
                if (sridString[0].equals(defaultSRID)) {
                    // Reverse order for EPSG 4326
                    //return termFactory.getDBSTDistanceSphere(geom[0], geom[1]);
                    return termFactory.getDBSTDistanceSphere(geom0, geom1);
                } else if (sridString[0].equals(defaultEPSG)) {
                    //return termFactory.getDBSTDistanceSphere(geom[0], geom[1]);
                    return termFactory.getDBSTDistanceSphere(geom0, geom1);
                } else {
                    //return termFactory.getDBSTDistance(geom[0], geom[1]);
                    return termFactory.getDBSTDistance(geom0, geom1);
                }
            } else if (unit.equals(UOM.DEGREE.getIRIString())) {
                // ST_DISTANCE
                //return termFactory.getDBSTDistance(geom[0], geom[1]);
                return termFactory.getDBSTDistance(geom0, geom1);
            } else if (unit.equals(UOM.RADIAN.getIRIString())) {
                // ST_DISTANCE / 180 * PI
                double ratio = PI / 180;
                DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
                DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
                DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());
                //ImmutableTerm distanceInDegree = termFactory.getDBSTDistance(geom[0], geom[1]);
                ImmutableTerm distanceInDegree = termFactory.getDBSTDistance(geom0, geom1);
                return termFactory.getImmutableFunctionalTerm(times, distanceInDegree, termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType()));
            } else {
                throw new IllegalArgumentException("Unexpected unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("SRIDs do not match");
        }
        //return termFactory.getDBStringConstant(sridString);
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

    private Optional<ImmutableTerm> getArg1FromTemplate(ImmutableTerm term) {//ImmutableList<ImmutableTerm> newterms) {
        return Optional.of(term)
                //.stream()
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // the first argument is the string starting with the IRI of the SRID
                .map(t -> t.getTerm(1));
                //.orElse(term);
    }

    private Optional<ImmutableTerm> getArg1FromDbConstant(ImmutableTerm immutableTerm, TermFactory termFactory) {
        return Optional.of(immutableTerm)
                // the first argument has to be a constant
                .filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                .map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                .filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the SRID out of the string
                .map(v -> termFactory.getDBStringConstant(v.substring(v.indexOf(">")+1)));
                //.map(s -> (ImmutableTerm) termFactory.getDBStringConstant(s));
    }

   /* private ImmutableTerm getImmutable(Optional<ImmutableTerm> immutableTerm) {
        ImmutableTerm kx = getArg1FromTemplate(immutableTerm);
        return kx;*/
                //.map(t -> (ImmutableTerm) t);

                // set srid condition here

                //.map(t <- t.getFun);
                //.map(t -> (ImmutableTerm) term);
                // the first argument has to be a constant
                //.filter(t -> t instanceof DBConstant).map(t -> (DBConstant) t)
                //.map(Constant::getValue)
                // the SRID is enclosed by "<" and ">
                //.filter(v -> v.startsWith("<") && v.indexOf(">") > 0)
                // extract the SRID out of the string
                //.map(v -> v.substring(1, v.indexOf(">")));
    //}





        /*ImmutableTerm[] subLexicalGeoms = {subLexicalTerms.get(0), subLexicalTerms.get(1)};
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
    }*/
}
