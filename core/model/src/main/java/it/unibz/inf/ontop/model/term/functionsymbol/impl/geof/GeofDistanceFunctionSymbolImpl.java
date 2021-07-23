package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBMathBinaryOperator;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.FunctionSymbolFactoryImpl;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.DistanceUnit.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.impl.geof.GeoUtils.EARTH_MEAN_RADIUS_METER;

public class GeofDistanceFunctionSymbolImpl extends AbstractGeofDoubleFunctionSymbolImpl {

    FunctionSymbolFactory functionSymbolFactory;

    public GeofDistanceFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, ObjectRDFType iriType, RDFDatatype xsdDoubleType, FunctionSymbolFactoryImpl functionSymbolFactory) {
        super("GEOF_DISTANCE", functionIRI,
                ImmutableList.of(wktLiteralType, wktLiteralType, iriType),
                xsdDoubleType);
        this.functionSymbolFactory = functionSymbolFactory;
    }


    /**
     * @param subLexicalTerms (geom1, geom2, unit)
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {


        List<WKTLiteralValue> wktLiteralValues = subLexicalTerms.subList(0, 2).stream().map(
                term -> GeoUtils.extractWKTLiteralValue(termFactory, term)
        ).collect(Collectors.toList());

        IRI srid0 = wktLiteralValues.get(0).getSRID();
        IRI srid1 = wktLiteralValues.get(1).getSRID();

        if (!srid0.equals(srid1)) {
            throw new IllegalArgumentException("SRIDs do not match: " + srid0 + ", " + srid1);
        }

        ImmutableTerm geom0 = wktLiteralValues.get(0).getGeometry();
        ImmutableTerm geom1 = wktLiteralValues.get(1).getGeometry();

        DistanceUnit inputUnit = GeoUtils.getUnitFromSRID(srid0.getIRIString());
        DistanceUnit outputUnit = DistanceUnit.findByIRI(((DBConstant) subLexicalTerms.get(2)).getValue());

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator divides = dbFunctionSymbolFactory.getDBMathBinaryOperator("/", dbTypeFactory.getDBDoubleType());


        if (inputUnit == METRE && outputUnit == METRE) {
            return termFactory.getDBSTDistance(geom0, geom1).simplify();
        } else if (inputUnit == METRE && outputUnit == RADIAN) {
            ImmutableTerm distanceInMetre = termFactory.getDBSTDistance(geom0, geom1).simplify();
            DBConstant radiusConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, radiusConstant);
        } else if (inputUnit == METRE && outputUnit == DEGREE) {
            ImmutableTerm distanceInMetre = termFactory.getDBSTDistance(geom0, geom1).simplify();
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER / 180 * Math.PI), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, ratioConstant);
        } else if (inputUnit == DEGREE && outputUnit == DEGREE) {
            // NOTE: supportsDBDistanceSphere() refers to official support i.e. PostGIS and not experimental i.e. H2GIS
            ImmutableTerm distanceInMetre = dbTypeFactory.supportsDBDistanceSphere()
                    ? termFactory.getDBSTDistanceSphere(geom0, geom1).simplify()
                    : termFactory.getDBSTDistanceSphere(
                    removeSetSRID(geom0, termFactory, subLexicalTerms.get(0)),
                    removeSetSRID(geom1, termFactory, subLexicalTerms.get(1))).simplify();
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER / 180 * Math.PI), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, ratioConstant);
        } else if (inputUnit == DEGREE && outputUnit == RADIAN) {
            ImmutableTerm distanceInMetre = dbTypeFactory.supportsDBDistanceSphere()
                    ? termFactory.getDBSTDistanceSphere(geom0, geom1).simplify()
                    : termFactory.getDBSTDistanceSphere(
                    removeSetSRID(geom0, termFactory, subLexicalTerms.get(0)),
                    removeSetSRID(geom1, termFactory, subLexicalTerms.get(1))).simplify();
            DBConstant ratioConstant = termFactory.getDBConstant(String.valueOf(EARTH_MEAN_RADIUS_METER), dbTypeFactory.getDBDoubleType());
            return termFactory.getImmutableFunctionalTerm(divides, distanceInMetre, ratioConstant);
        } else if (inputUnit == DEGREE && outputUnit == METRE) {
            // TODO: consider using getDBSTDistanceSpheroid to get more accurate results
            return dbTypeFactory.supportsDBDistanceSphere()
                    ? termFactory.getDBSTDistanceSphere(geom0, geom1).simplify()
                    : termFactory.getDBSTDistanceSphere(
                    removeSetSRID(geom0, termFactory, subLexicalTerms.get(0)),
                    removeSetSRID(geom1, termFactory, subLexicalTerms.get(1))).simplify();
        } else {
            throw new IllegalArgumentException(String.format("Unsupported combination of units for distance. input: %s, output: %s", inputUnit, outputUnit));
        }
    }

    /**
     * H2GIS does not support SETSRID within DISTANCESPHERE unlike PostGIS
     * e.g. ST_DISTANCESPHRE(geom1, ST_SETSRID(geom2)) fails
     * Method returns the actual geometry instead for these cases
     * H2GIS does not distinguish SRID 0 from SRID 4326, so most use cases still work
     *
     * @return Geometry if input from the user at query time
     */
    // TODO: Consider limitations of DISTANCESPHERE for H2GIS
    static ImmutableTerm removeSetSRID(ImmutableTerm geom, TermFactory termFactory, ImmutableTerm subLexicalTerm) {
        return geom.isGround()
                ? GeoUtils.extractConstantWKTLiteralValue(termFactory, subLexicalTerm).get()
                : geom;
    }

}
