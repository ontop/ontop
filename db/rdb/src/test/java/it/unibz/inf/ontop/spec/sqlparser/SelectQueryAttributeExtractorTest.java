package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import org.junit.Test;

import static it.unibz.inf.ontop.spec.sqlparser.SQLTestingTools.*;
import static org.junit.Assert.assertEquals;

public class SelectQueryAttributeExtractorTest {

    @Test
    public void test_no_from() throws Exception {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        MetadataLookup metadataLookup = builder.build();
        QuotedIDFactory idfac = metadataLookup.getQuotedIDFactory();
        DefaultSelectQueryAttributeExtractor ae = new DefaultSelectQueryAttributeExtractor(metadataLookup, CORE_SINGLETONS);
        RAExpressionAttributes r = ae.getRAExpressionAttributes(JSqlParserTools.parse("SELECT 1 AS A"));
        assertEquals(ImmutableSet.of(idfac.createAttributeID("A")), r.getUnqualifiedAttributes().keySet());
    }

    @Test // issue 184
    public void test_order() throws Exception {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        builder.createDatabaseRelation("demographics", "STUDY_ID", builder.getDBTypeFactory().getDBLargeIntegerType(), false);
        MetadataLookup metadataLookup = builder.build();
        QuotedIDFactory idfac = metadataLookup.getQuotedIDFactory();
        DefaultSelectQueryAttributeExtractor ae = new DefaultSelectQueryAttributeExtractor(metadataLookup, CORE_SINGLETONS);
        RAExpressionAttributes r = ae.getRAExpressionAttributes(JSqlParserTools.parse("select STUDY_ID, patient_name(STUDY_ID) as label from demographics order by STUDY_ID limit 50"));
        assertEquals(ImmutableSet.of(idfac.createAttributeID("study_id"), idfac.createAttributeID("label")), r.getUnqualifiedAttributes().keySet());
    }

    @Test
    public void test_approximation() throws InvalidQueryException {
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
                "LEFT JOIN ALESFO001 ON ALMAES001.IDART = ALESFO001.IDART"
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

    // issue 366
    @Test
    public void test_approximation_distinct() throws InvalidQueryException {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        QuotedIDFactory idfac = builder.getQuotedIDFactory();

        ApproximateSelectQueryAttributeExtractor aex = new ApproximateSelectQueryAttributeExtractor(idfac);

        ImmutableList<QuotedID> res = aex.getAttributes("select \n distinct \n rotorID from\n" +
                "(select zpolrotorid as rotorID from LinkData\n" +
                "union\n" +
                "select abomSerialNumberMale as rotorID from AssemblyData\n" +
                "union\n" +
                "select abomSerialNumberFemale as rotorID from AssemblyData) as R"
        );
        assertEquals(ImmutableList.of(
                idfac.createAttributeID("rotorID")), res);
    }

    @Test(expected = UnsupportedSelectQueryException.class) // issue 366
    public void test_distinct_union() throws Exception {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        builder.createDatabaseRelation("LinkData", "zpolrotorid", builder.getDBTypeFactory().getDBLargeIntegerType(), false);
        builder.createDatabaseRelation("AssemblyData",
                "abomSerialNumberMale", builder.getDBTypeFactory().getDBLargeIntegerType(), false,
                "abomSerialNumberFemale", builder.getDBTypeFactory().getDBLargeIntegerType(), false);
        MetadataLookup metadataLookup = builder.build();
        QuotedIDFactory idfac = metadataLookup.getQuotedIDFactory();
        DefaultSelectQueryAttributeExtractor ae = new DefaultSelectQueryAttributeExtractor(metadataLookup, CORE_SINGLETONS);
        RAExpressionAttributes r = ae.getRAExpressionAttributes(JSqlParserTools.parse("select \n distinct \n rotorID from\n" +
                "(select zpolrotorid as rotorID from LinkData\n" +
                "union\n" +
                "select abomSerialNumberMale as rotorID from AssemblyData\n" +
                "union\n" +
                "select abomSerialNumberFemale as rotorID from AssemblyData) as R"));
    }


}
