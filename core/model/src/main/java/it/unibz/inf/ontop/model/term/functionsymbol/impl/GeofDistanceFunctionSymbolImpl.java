package it.unibz.inf.ontop.model.term.functionsymbol.impl;

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

    protected GeofDistanceFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype wktLiteralType, ObjectRDFType iriType, RDFDatatype xsdDoubleType) {
        super("GEOF_DISTANCE", functionIRI,
                ImmutableList.of(wktLiteralType, wktLiteralType, iriType),
                xsdDoubleType);
    }

    /**
     * @param subLexicalTerms (lat, lon, unit)
     *                        <p>
     *                        Assume the args are WGS 84, EPSG:4326 (lat lon)
     *                        <p>
     * @return if unit=meter, returns ST_DISTANCE(ST_Transform ( ST_SETSRID ( lat, 4326), 3857) ,
     * ST_Transform(ST_SETSRID(lon,4326), 3857) )
     */
    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        String unit = ((DBConstant) subLexicalTerms.get(2)).getValue();
        if (UOM.METRE.getIRIString().equals(unit)) {
            DBConstant SRID_4326 = termFactory.getDBIntegerConstant(4326);
            DBConstant SRID_3857 = termFactory.getDBIntegerConstant(3857);
            return termFactory.getDBSTDistance(
                    termFactory.getDBSTSTransform(
                            termFactory.getDBSTSetSRID(subLexicalTerms.get(0),
                                    SRID_4326
                            ),
                            SRID_3857
                    )
                    ,
                    termFactory.getDBSTSTransform(
                            termFactory.getDBSTSetSRID(subLexicalTerms.get(1),
                                    SRID_4326
                            ),
                            SRID_3857
                    )
            );
        } else if (UOM.RADIAN.getIRIString().equals(unit)) {
            return termFactory.getDBSTDistance(subLexicalTerms.subList(0, 2));
        }
        throw new IllegalStateException("Unexpected unit: " + unit);

    }
}
