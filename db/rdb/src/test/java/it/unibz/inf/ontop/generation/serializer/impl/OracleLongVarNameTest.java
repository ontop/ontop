package it.unibz.inf.ontop.generation.serializer.impl;

import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test how the Oracle SQL dialect adapter behaves with long aliases.
 */
public class OracleLongVarNameTest {

    private final QuotedIDFactory idFactory = new SQLStandardQuotedIDFactory();
    private final AttributeAliasFactory defaultAdapter = new DefaultAttributeAliasFactory(idFactory);
    private final AttributeAliasFactory oracleAdapter = new LimitLengthAttributeAliasFactory(idFactory, 30, 3);

    private static final String veryLongSignatureVarName = "veryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVerylongVarName";
    private static final String limitSignatureVarName = "v23456789012345678901234";
    private static final String defaultSuffix = "Suffix";

    @Test
    public void testDefaultAdapter() {
        QuotedID veryLongVarName = defaultAdapter.createAttributeAlias(veryLongSignatureVarName + defaultSuffix);
        assertEquals( veryLongSignatureVarName + defaultSuffix, veryLongVarName.getName());
    }

    @Test
    public void testOracleOneShot() {
        QuotedID veryLongVarName = oracleAdapter.createAttributeAlias(veryLongSignatureVarName + defaultSuffix);
        assertTrue(veryLongVarName.getName().length() <= 30);
        //assertTrue(veryLongVarName.contains(defaultSuffix));
        assertTrue(veryLongVarName.getName().contains("veryVery"));
        assertEquals("veryVeryVeryVeryVeryVeryVer0", veryLongVarName.getName());
    }

    @Test
    public void testOracleTenSimilarVars() {
        Set<QuotedID> assignedVars = new HashSet<>();
        int createdVarNb =  1000;
        for(int i = 0; i < createdVarNb; i++) {
            assignedVars.add(oracleAdapter.createAttributeAlias(veryLongSignatureVarName + defaultSuffix));
        }
        assertEquals(createdVarNb, assignedVars.size());
    }

    @Test
    public void testOracleTooMuchSimilarVars() {
        int createdVarNb = 1001;
        assertThrows(RuntimeException.class, () -> {
            for (int i = 0; i < createdVarNb; i++) {
                oracleAdapter.createAttributeAlias(veryLongSignatureVarName + defaultSuffix);
            }
        });
    }

    @Test
    public void testOracleMaxNonModifiedVarName() {
        QuotedID limitVarName = oracleAdapter.createAttributeAlias(limitSignatureVarName + defaultSuffix);
        assertEquals(limitSignatureVarName + defaultSuffix, limitVarName.getName());
    }
}
