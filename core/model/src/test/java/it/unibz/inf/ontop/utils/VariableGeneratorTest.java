package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.impl.LegacyVariableGenerator;
import it.unibz.inf.ontop.utils.impl.VariableGeneratorImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static org.junit.Assert.*;

public class VariableGeneratorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VariableGeneratorTest.class);


    @Test
    public void testLegacy() {
        testVariableGenerator(LegacyVariableGenerator.class);
    }

    @Test
    public void testDefault() {
        testVariableGenerator(VariableGeneratorImpl.class);
    }

    private void testVariableGenerator(Class<? extends VariableGenerator> klass) {
        Properties properties = new Properties();
        properties.put(VariableGenerator.class.getCanonicalName(), klass.getCanonicalName());

        OntopModelConfiguration configuration = OntopModelConfiguration.defaultBuilder()
                .properties(properties)
                .enableTestMode()
                .build();
        Injector injector = configuration.getInjector();

        CoreUtilsFactory utilsFactory = injector.getInstance(CoreUtilsFactory.class);
        testVariableGenerator(utilsFactory, configuration.getTermFactory());
    }

    private void testVariableGenerator(CoreUtilsFactory utilsFactory, TermFactory termFactory) {
        VariableGenerator variableGenerator = utilsFactory.createVariableGenerator(ImmutableSet.of());

        Variable v1 = variableGenerator.generateNewVariable();
        Variable v2 = variableGenerator.generateNewVariableFromVar(v1);
        assertNotEquals(v1, v2);
        Variable v3 = termFactory.getVariable("myIndependentVariable");
        Variable v4 = variableGenerator.generateNewVariableIfConflicting(v3);
        assertEquals(v3, v4);
        Variable v5 = variableGenerator.generateNewVariableIfConflicting(v3);
        assertNotEquals(v3, v5);

        Variable v6 = termFactory.getVariable("mySecondIndependentVariable");
        variableGenerator.registerAdditionalVariables(ImmutableSet.of(v6));

        Variable v7 = variableGenerator.generateNewVariableIfConflicting(v6);
        assertNotEquals(v6, v7);

        Variable v8 = termFactory.getVariable("myThirdIndependentVariable");

        assertEquals(ImmutableSet.of(v1, v2, v3, v5, v6, v7), variableGenerator.getKnownVariables());

        VariableGenerator newVariableGenerator = variableGenerator.createSnapshot();
        assertEquals(variableGenerator.getKnownVariables(), newVariableGenerator.getKnownVariables());

        newVariableGenerator.registerAdditionalVariables(ImmutableSet.of(v8));
        assertNotEquals(variableGenerator.getKnownVariables(), newVariableGenerator.getKnownVariables());

        Variable v9 = variableGenerator.generateNewVariable();
        Variable v10 = variableGenerator.generateNewVariableIfConflicting(v9);
        assertNotEquals(v9, v10);

        Variable v11 = newVariableGenerator.generateNewVariableIfConflicting(v9);
        assertEquals(v9, v11);

        Variable v12 = termFactory.getVariable("myFourthIndependentVariable");
        Variable v13 = variableGenerator.generateNewVariableFromVar(v12);
        assertNotEquals(v12, v13);
        Variable v14 = variableGenerator.generateNewVariableIfConflicting(v12);
        assertNotEquals(v14, v12);
        assertNotEquals(v13, v14);

        Variable v15 = termFactory.getVariable("myVar15-100");
        Variable v16 = newVariableGenerator.generateNewVariableFromVar(v15);
        assertNotEquals(v16, v15);

        LOGGER.debug("Fresh variable derived from " + v15 + ": " + v16);
    }


}
