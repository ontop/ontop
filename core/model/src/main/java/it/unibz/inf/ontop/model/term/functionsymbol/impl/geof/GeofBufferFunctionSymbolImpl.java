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
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

import static java.lang.Math.PI;

public class GeofBufferFunctionSymbolImpl extends AbstractGeofWKTFunctionSymbolImpl {
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

        DBFunctionSymbolFactory dbFunctionSymbolFactory = termFactory.getDBFunctionSymbolFactory();
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();
        DBMathBinaryOperator times = dbFunctionSymbolFactory.getDBMathBinaryOperator("*", dbTypeFactory.getDBDoubleType());

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
