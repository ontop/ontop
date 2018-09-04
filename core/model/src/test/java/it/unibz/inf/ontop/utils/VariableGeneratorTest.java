package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.impl.LegacyVariableGenerator;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class VariableGeneratorTest {


    @Test
    public void testLegacy() {
        testVariableGenerator(LegacyVariableGenerator.class);
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
    }


}
