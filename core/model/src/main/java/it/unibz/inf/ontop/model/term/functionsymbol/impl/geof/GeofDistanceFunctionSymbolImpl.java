package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.UOM;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class GeofDistanceFunctionSymbolImpl extends AbstractGeofDoubleFunctionSymbolImpl {

    public GeofDistanceFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, ObjectRDFType iriType, RDFDatatype xsdDoubleType) {
        super("GEOF_DISTANCE", functionIRI,
                ImmutableList.of(wktLiteralType, wktLiteralType, iriType),
                xsdDoubleType);
    }

    /**
     * @param subLexicalTerms (lat, lon, unit)
     *                        Assume the args are WGS 84 (lat lon)
     * @return if unit=uom:metre, returns
     * <pre>
     *      ST_DISTANCE_SPHERE(arg1, arg2)
     * </pre>
     * <p> if unit=uom:radian, returns
     * <pre>
     *         ST_DISTANCE(arg1, arg2)
     *       </pre>
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();
        if (UOM.METRE.getIRIString().equals(unit)) {
            return termFactory.getDBSTDistanceSphere(subLexicalTerms.get(0), subLexicalTerms.get(1));
        } else if (UOM.RADIAN.getIRIString().equals(unit)) {
            // TODO: distance(p1, p2) / 180 * PI
            return termFactory.getDBSTDistance(subLexicalTerms.get(0), subLexicalTerms.get(1));
//            return termFactory
//                    .getDBSTDistance(
//                    termFactory.getDBSTSTransform(
//                            termFactory.getDBSTSetSRID(subLexicalTerms.get(0),
//                                    SRID_4326
//                            ),
//                            SRID_3857
//                    )
//                    ,
//                    termFactory.getDBSTSTransform(
//                            termFactory.getDBSTSetSRID(subLexicalTerms.get(1),
//                                    SRID_4326
//                            ),
//                            SRID_3857
//                    )
//            );
        } else if (UOM.DEGREE.getIRIString().equals(unit)) {
            return termFactory.getDBSTDistance(subLexicalTerms.get(0), subLexicalTerms.get(1));
        } else {
            throw new IllegalArgumentException("Unexpected unit: " + unit);
        }


    }
}
