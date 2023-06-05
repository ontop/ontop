package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.Properties;

public class QuotedIDFactoryInjectionTest {

    /**
     * Test injection of a qualified {@link QuotedIDFactory} through an injected {@link QuotedIDFactory.Supplier}.
     */
    @Test
    public void testQuotedIDFactoryInjection() {

        Properties props = new Properties();
        props.setProperty("noisy-property", "useless-value"); // to stress settings parsing method

        OntopModelConfiguration configuration = OntopModelConfiguration.defaultBuilder().properties(props).build();

        QuotedIDFactory.Supplier supplier = configuration.getInjector().getInstance(QuotedIDFactory.Supplier.class);
        Optional<QuotedIDFactory> factory = supplier.get("STANDARD");

        Assert.assertTrue(factory.isPresent());
        Assert.assertEquals(SQLStandardQuotedIDFactory.class, factory.get().getClass());
        Assert.assertEquals("STANDARD", factory.get().getIDFactoryType());
    }

}
