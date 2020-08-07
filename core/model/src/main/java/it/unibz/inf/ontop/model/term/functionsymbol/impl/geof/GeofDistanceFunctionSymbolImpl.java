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

//import org.ge

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

        // Get unit of distance i.e. degree, radian, metric;
        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();

        // Retrieve the SRIDs for each geometry
        String sridString0 = getSRIDFromDbConstant(Optional.of(subLexicalTerms.get(0)))
                .orElseGet(
                        // template
                        () -> getSRIDFromDbConstant(getArg0FromTemplate(subLexicalTerms.get(0)))
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID));

        String sridString1 = getSRIDFromDbConstant(Optional.of(subLexicalTerms.get(1)))
                .orElseGet(
                        // template
                        () -> getSRIDFromDbConstant(getArg0FromTemplate(subLexicalTerms.get(1)))
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID));


        // Get the first proper geometry i.e. if template remove IRI
        ImmutableTerm geom0 = getArg1FromUserInput(subLexicalTerms.get(0), termFactory)
                .orElseGet(
                        // If template then
                        () -> getArg1FromTemplate(subLexicalTerms.get(0))
                            .orElse(subLexicalTerms.get(0)));

        // Get the second proper geometry i.e. if template remove IRI
        ImmutableTerm geom1 = getArg1FromUserInput(subLexicalTerms.get(1), termFactory)
                .orElseGet(
                        // If template then
                        () -> getArg1FromTemplate(subLexicalTerms.get(1))
                                .orElse(subLexicalTerms.get(1)));

        // If SRIDs are not identical throw error
        if(sridString0.equals(sridString1)) {
            // Check unit
            if (unit.equals(UOM.METRE.getIRIString())) {
                // If metric and WGS84, use ST_DISTANCESPHERE
                if (sridString0.equals(defaultSRID) || sridString0.equals(defaultEPSG)) {
                    //final String measurement_spheroid = "SPHEROID[\"WGS 84\",6378137,298.257223563]";
                    //return termFactory.getDBSTDistanceSpheroid(geom0, geom1,termFactory.getDBStringConstant(measurement_spheroid));
                    return termFactory.getDBSTDistanceSphere(geom0, geom1);
                // If non-WGS84, use Cartesian distance
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
                // template is a NonGroundFunctionalTerm
                .filter(t -> t instanceof NonGroundFunctionalTerm).map(t -> (NonGroundFunctionalTerm) t)
                // template uses DBConcatFunctionSymbol as the functional symbol
                .filter(t -> t.getFunctionSymbol() instanceof DBConcatFunctionSymbol)
                // the first argument is the string starting with the IRI of the SRID
                .map(t -> t.getTerm(1));
                //.orElse(term);
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
}
