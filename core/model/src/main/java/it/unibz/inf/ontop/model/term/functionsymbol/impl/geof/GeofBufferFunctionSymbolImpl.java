package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

import java.util.Optional;

import static it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.DistanceUnit.*;
import static java.lang.Math.PI;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {

    public GeofBufferFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, RDFDatatype decimalType, ObjectRDFType iriType) {
        super("GEOF_BUFFER", functionIRI, ImmutableList.of(wktLiteralType, decimalType, iriType), wktLiteralType);
    }

    /**
     * @param subLexicalTerms (geom, dist, unit)
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {

        ImmutableTerm term = subLexicalTerms.get(0);
        WKTLiteralValue wktLiteralValue = GeoUtils.extractWKTLiteralValue(termFactory, term);

        DistanceUnit inputUnit = GeoUtils.getUnitFromSRID(wktLiteralValue.getSRID().getIRIString());// DistanceUnit.fromIRI(srid0);
        ImmutableTerm distance = subLexicalTerms.get(1);
        DistanceUnit distanceUnit = DistanceUnit.findByIRI(((DBConstant) subLexicalTerms.get(2)).getValue());

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());
        final double EARTH_MEAN_RADIUS_METER = 6370986;
        final double degree_metre_ratio = 180 / PI / EARTH_MEAN_RADIUS_METER;
        final double degree_radian_ratio = 180 / PI;

        ImmutableTerm geom = unwrapSTAsText(wktLiteralValue.getGeometry());

        // ST_AsTexT(ST_BUFFER(geom, distance))
        if (inputUnit == DEGREE && distanceUnit == METRE) {
            if (dbTypeFactory.supportsDBGeographyType()) {
                // see <https://postgis.net/workshops/postgis-intro/geography.html>
                // TODO termFactory.getDBAsText might be redundant. Similarly in other cases
                return termFactory.getDBAsText(
                        termFactory.getDBBuffer(
                                termFactory.getDBCastFunctionalTerm(dbTypeFactory.getDBGeographyType(), geom),
                                distance))
                        .simplify();
            } else {
                // Less accurate
                DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(degree_metre_ratio), dbTypeFactory.getDBDoubleType());
                ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, distance, ratioConstant);
                return termFactory.getDBAsText(
                        termFactory.getDBBuffer(geom, distanceInDegree))
                        .simplify();
            }
        } else if (inputUnit == DEGREE && distanceUnit == DEGREE) {
            // ST_BUFFER
            return termFactory.getDBAsText(termFactory.getDBBuffer(geom, distance)).simplify();
        } else if (inputUnit == DEGREE && distanceUnit == RADIAN) {
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(degree_radian_ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInDegree = termFactory.getImmutableFunctionalTerm(times, distance, ratioConstant);
            return termFactory.getDBAsText(termFactory.getDBBuffer(geom, distanceInDegree)).simplify();
        } else if (inputUnit == METRE && distanceUnit == METRE) {
            // ST_BUFFER
            return termFactory.getDBAsText(termFactory.getDBBuffer(geom, distance)).simplify();
        } else if (inputUnit == RADIAN && distanceUnit == RADIAN) {
            return termFactory.getDBAsText(termFactory.getDBBuffer(geom, distance)).simplify();
        } else if (inputUnit == METRE && distanceUnit == DEGREE) {
            // Less accurate
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(1.0 / degree_metre_ratio), dbTypeFactory.getDBDoubleType());
            ImmutableFunctionalTerm distanceInMetre = termFactory.getImmutableFunctionalTerm(times, distance, ratioConstant);
            return termFactory.getDBAsText(
                            termFactory.getDBBuffer(geom, distanceInMetre))
                    .simplify();
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported unit combination for geof:buffer. inputUnit=%s, outputUnit=%s ",
                            inputUnit, distanceUnit));
        }

    }

    // if term is ST_ASTEXT(arg), returns arg, otherwise the term itself
    private ImmutableTerm unwrapSTAsText(ImmutableTerm term) {
        return Optional.of(term)
                // term is a function
                .filter(t -> t instanceof ImmutableFunctionalTerm).map(ImmutableFunctionalTerm.class::cast)
                // the function symbol is ST_ASTEXT
                .filter(t -> t.getFunctionSymbol().getName().startsWith("ST_ASTEXT"))
                // extract the 0-th argument
                .map(t -> t.getTerm(0))
                // otherwise the term itself
                .orElse(term);
    }

}
