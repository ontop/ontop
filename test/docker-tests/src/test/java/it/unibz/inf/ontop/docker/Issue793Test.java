package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.commons.util.ReflectionUtils;

public class Issue793Test {
  private final Properties properties = new Properties();
  private final Graph emptyMappingGraph = new RDF4J().asGraph(new LinkedHashModel());
  private OntopModelConfiguration startingConfigurationObject;

  @Before
  public void before() throws SQLException {
    properties.put("jdbc.url", "jdbc:h2:mem:issue793Test");
    properties.put("jdbc.driver", "org.h2.Driver");

    startingConfigurationObject = OntopSQLOWLAPIConfiguration.defaultBuilder()
        .properties(properties)
        .r2rmlMappingGraph(emptyMappingGraph)
        .enableTestMode()
        .build();
  }

  @Test
  public void testInjectorPropagationBetweenInheritanceBranch() throws IllegalAccessException {
    testInjectorPropagationBetweenInheritanceBranch(startingConfigurationObject);
  }

  public void testInjectorPropagationBetweenInheritanceBranch(
      OntopModelConfiguration configurationObjectUnderTest) throws IllegalAccessException {
    List<Field> innerConfigurationFields = ReflectionUtils.findFields(
        configurationObjectUnderTest.getClass(),
        field -> OntopModelConfiguration.class.isAssignableFrom(field.getType()),
        ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP);
    for (Field field : innerConfigurationFields) {
      field.setAccessible(true);
      OntopModelConfiguration innerConfigurationObject = (OntopModelConfiguration) field.get(
          configurationObjectUnderTest);

      // The injector must be propagated to the inner configuration object
      Assertions.assertSame(startingConfigurationObject.getInjector(),
          innerConfigurationObject.getInjector(),
          String.format(
              "The injector must be propagated to the inner configuration object of %s (field name = %s)",
              field.getDeclaringClass(), field.getName()));

      // The injector also must be propagated to any branch in the inheritance tree
      testInjectorPropagationBetweenInheritanceBranch(innerConfigurationObject);
    }
  }
}
