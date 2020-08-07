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

import java.util.Optional;

import static java.lang.Math.PI;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {
    FunctionSymbolFactory functionSymbolFactory;
    public static final String defaultSRID = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
    public static final String defaultEPSG = "http://www.opengis.net/def/crs/EPSG/0/4326";

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
        //ImmutableTerm geom0 = term0;

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());

        String sridString = getSRIDFromDbConstant(Optional.of(term))
                .orElseGet(
                        // template
                        () -> getSRIDFromDbConstant(getArg0FromTemplate(term))
                                // otherwise, returns the default SRID
                                .orElse(defaultSRID));

        ImmutableTerm geom = getArg1FromUserInput(term, termFactory)
                .orElseGet(
                        // Manutal input
                        () -> getArg1FromTemplate(subLexicalTerms.get(0))
                                .orElse(subLexicalTerms.get(0)));

        if (unit.equals(UOM.METRE.getIRIString())) {
            final double EARTH_MEAN_RADIUS_METER = 6370986;
            final double ratio = 180 / PI / EARTH_MEAN_RADIUS_METER;
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
            // If WGS84, return spheroid
            if (sridString.equals(defaultSRID) || sridString.equals(defaultEPSG)) {
                return termFactory.getDBAsText(termFactory.getDBBuffer(geom, distanceInDegree));
            } else {
                return termFactory.getDBAsText(termFactory.getDBBuffer(geom, subLexicalTerms.get(1)));
            }
        } else if (unit.equals(UOM.DEGREE.getIRIString())) {
            // ST_BUFFER
            return termFactory.getDBAsText(termFactory.getDBBuffer(geom, subLexicalTerms.get(1)));
        } else if (unit.equals(UOM.RADIAN.getIRIString())) {
            // ST_AsTexT(ST_BUFFER(geom, distance))
            final double ratio = 180 / PI;
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, subLexicalTerms.get(1), ratioConstant);
            return termFactory.getDBAsText(termFactory.getDBBuffer(geom, distanceInDegree));
        } else {
            throw new IllegalArgumentException("Unexpected unit: " + unit);
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
