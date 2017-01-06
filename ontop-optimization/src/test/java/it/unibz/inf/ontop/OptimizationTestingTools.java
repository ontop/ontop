package it.unibz.inf.ontop;


import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopOptimizationConfiguration;

public class OptimizationTestingTools {

    public static final Injector INJECTOR = OntopOptimizationConfiguration.defaultBuilder()
            .build()
            .getInjector();

}
