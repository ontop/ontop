package it.unibz.inf.ontop.docker.lightweight.db2;

import it.unibz.inf.ontop.docker.lightweight.AbstractManifestSuiteTest;
import it.unibz.inf.ontop.docker.lightweight.DB2LightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@DB2LightweightTest
public class ManifestSuiteDB2Test extends AbstractManifestSuiteTest {

    private static final String PROPERTIES_FILE = "/stockexchange/db2/stockexchange-db2.properties";
    private static final String OBDA_FILE = "/stockexchange/db2/stockexchange-db2.obda";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testDecimalEq() {
        super.testPosDecimal2();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testDecimalNeq() {
        super.testPosDecimal2();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testDecimalGt() {
        super.testPosDecimal2();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testDecimalLte() {
        super.testPosDecimal2();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testPosDecimal2() {
        super.testPosDecimal2();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testPosDecimal3() {
        super.testPosDecimal3();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testNegDecimal2() {
        super.testPosDecimal2();
    }

    @Disabled("Cast to DECIMAL in DB2 returns by default DECIMAL with scale 0")
    @Override
    @Test
    public void testNegDecimal3() {
        super.testPosDecimal3();
    }

    //"datatypes-Q42: Datetime YYYY-MM-DDThh:mm:ssZ [in UTC] with xsd:datetime"
    @Disabled
    @Override
    @Test
    public void testDatetime3d() {
        super.testDatetime3d();
    }

    //"datatypes-Q43: Datetime YYYY-MM-DDThh:mm:ss-hh:mm [in UTC minus offset - var 1] with xsd:datetime"
    @Disabled
    @Override
    @Test
    public void testDatetime3e() {
        super.testDatetime3e();
    }

    //"datatypes-Q46: Datetime YYYY-MM-DDThh:mm:ss+hh:mm [in UTC plus offset - var 1] with xsd:datetime"
    @Disabled
    @Override
    @Test
    public void testDatetime3h() {
        super.testDatetime3h();
    }
}
