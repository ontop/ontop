package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryException;
import org.junit.Test;

import static it.unibz.inf.ontop.utils.SQLMappingTestingTools.*;
import static org.junit.Assert.assertEquals;

public class SelectQueryAttributeExtractorTest {

    @Test
    public void test_no_from() throws Exception {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        MetadataLookup metadataLookup = builder.build();
        QuotedIDFactory idfac = metadataLookup.getQuotedIDFactory();
        DefaultSelectQueryAttributeExtractor ae = new DefaultSelectQueryAttributeExtractor(metadataLookup, TERM_FACTORY);
        RAExpressionAttributes r = ae.getRAExpressionAttributes("SELECT 1 AS A");
        assertEquals(ImmutableSet.of(new QualifiedAttributeID(null, idfac.createAttributeID("A"))), r.getAttributes().keySet());
    }

    @Test
    public void test_approximation() throws InvalidSelectQueryException {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        QuotedIDFactory idfac = builder.getQuotedIDFactory();

        ApproximateSelectQueryAttributeExtractor aex = new ApproximateSelectQueryAttributeExtractor(idfac);

        ImmutableList<QuotedID> res = aex.getAttributes("SELECT ALMAES001.IDART,\n"+
                "     ALMAES001.UPC,\n"+
                "     ALMAES001.PVP1, ALMAES001.PVP2, ALMAES001.PVP3, \n"+
                "     to_char(ALMAES001.FECALTA,'YYYY-MM-DD') AS FECALTA,\n"+
                "     to_char(ALMAES001.FECBLO,'YYYY-MM-DD') AS FECBLO,\n"+
                "     to_char((ALMAES001.FECBLO),('YYYY-MM-DD')) AS FECBLO1,\n"+
                "     ALMAES001.STCMIN,\n"+
                "     ALESFO001.PRECIOFINAL\n"+
                "\t             \n"+
                "FROM ALMAES001 "+
                "LEFT JOIN ALESFO001 ON ALMAES001.IDART = ALESFO001.IDART" +
                ""
        );
        assertEquals(ImmutableList.of(
                idfac.createAttributeID("IDART"),
                idfac.createAttributeID("UPC"),
                idfac.createAttributeID("PVP1"),
                idfac.createAttributeID("PVP2"),
                idfac.createAttributeID("PVP3"),
                idfac.createAttributeID("FECALTA"),
                idfac.createAttributeID("FECBLO"),
                idfac.createAttributeID("FECBLO1"),
                idfac.createAttributeID("STCMIN"),
                idfac.createAttributeID("PRECIOFINAL")), res);
    }
}
