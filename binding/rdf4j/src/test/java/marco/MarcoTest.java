package marco;

import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.junit.Test;

public class MarcoTest {

    private static final String PROPERTY =
            "src/test/resources/compareIRI/boot-multiple-inheritance.properties";
    private static final String OWL =
            "src/test/resources/compareIRI/boot-multiple-inheritance.owl";
    private static final String OBDA =
            "src/test/resources/compareIRI/boot-multiple-inheritance.obda";

    @Test
    public void testOntop() throws MappingException {
        OntopSQLOWLAPIConfiguration initialConfiguration =
                configure(PROPERTY, OWL, OBDA);

        SQLPPMapping mappings =
                initialConfiguration.loadProvidedPPMapping();

        for( SQLPPTriplesMap tripleMap : mappings.getTripleMaps() ){
            TargetAtom targetAtom1 = tripleMap.getTargetAtoms().get(0);
            TargetAtom targetAtom2 = tripleMap.getTargetAtoms().get(1);

            System.out.println(targetAtom1);
            ImmutableTerm t1 = targetAtom1.getSubstitutedTerm(0);
            ImmutableTerm t2 = targetAtom2.getSubstitutedTerm(0);

            System.out.println(t1);
            System.out.println(t2);
            System.out.println(t1.equals(t2));
            break;
        }
    }
    static OntopSQLOWLAPIConfiguration configure(String propertyPath,
                                                 String owlPath, String obdaPath) {

        OntopSQLOWLAPIConfiguration configuration =
                OntopSQLOWLAPIConfiguration.defaultBuilder()
                        .ontologyFile(owlPath)
                        .nativeOntopMappingFile(obdaPath)
                        .propertyFile(PROPERTY)
                        .enableTestMode()
                        .build();



        return configuration;
    }
}