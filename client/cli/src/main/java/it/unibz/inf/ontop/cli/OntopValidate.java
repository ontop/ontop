package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.impl.FactsException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Command(name = "validate",
        description = "Validate Ontology and Mappings")
public class OntopValidate extends OntopMappingOntologyRelatedCommand {

    public OntopValidate() {
    }

    @Override
    public void run() {

        if (!Files.exists(Paths.get(owlFile))) {
            System.out.format("ERROR: The Ontology file %s does not exist\n", owlFile);
            System.exit(1);
        }

        OntopSQLOWLAPIConfiguration.Builder<?> builder =
                OntopSQLOWLAPIConfiguration.defaultBuilder()
                        .ontologyFile(owlFile)
                        .enableOntologyAnnotationQuerying(enableAnnotations);

        if(factFile != null)
            builder.factsFile(factFile);

        if(factFormat != null)
            builder.factFormat(factFormat.getExtension());

        if (factsBaseIRI != null)
            builder.factsBaseIRI(factsBaseIRI);

        if (propertiesFile != null)
            builder.propertyFile(propertiesFile);

        if (dbPassword != null)
            builder.jdbcPassword(dbPassword);

        if (dbUrl != null)
            builder.jdbcUrl(dbUrl);

        if (dbUser != null)
            builder.jdbcUser(dbUser);

        if (dbDriver != null)
            builder.jdbcDriver(dbDriver);

        if (isR2rmlFile(mappingFile))
            builder.r2rmlMappingFile(mappingFile);
        else
            builder.nativeOntopMappingFile(mappingFile);

        if (dbMetadataFile != null)
            builder.dbMetadataFile(dbMetadataFile);

        if (ontopLensesFile != null)
            builder.lensesFile(ontopLensesFile);

        if (xmlCatalogFile != null)
            builder.xmlCatalogFile(xmlCatalogFile);

        if (sparqlRulesFile != null)
            builder.sparqlRulesFile(sparqlRulesFile);

        OntopSQLOWLAPIConfiguration config = builder.build();

        OWLOntology ontology = null;
        try {
            ontology = config.loadProvidedInputOntology();
        }
        catch (OWLOntologyCreationException e) {
            System.out.format("ERROR: There is a problem loading the ontology file: %s\n", owlFile);
            e.printStackTrace();
            System.exit(1);
        }
        catch (Exception ex) {
            System.out.format("ERROR: OntopOWL reasoner cannot be initialized\n");
            ex.printStackTrace();
            System.exit(1);
        }

        try {
            config.loadInputFacts();
        }
        catch (OBDASpecificationException e) {
            System.out.format("ERROR: There is a problem loading the fact file: %s\n", factFile);
            e.printStackTrace();
            System.exit(1);
        }

        Set<IRI> classIRIs = ontology.getClassesInSignature().stream()
                .map(OWLNamedObject::getIRI).collect(toSet());

        Set<IRI> opIRIs = ontology.getObjectPropertiesInSignature().stream()
                .map(OWLNamedObject::getIRI).collect(toSet());

        Set<IRI> dpIRIs = ontology.getDataPropertiesInSignature().stream()
                .map(OWLNamedObject::getIRI).collect(toSet());

        ImmutableSet<IRI> class_op_intersections = Sets.intersection(classIRIs, opIRIs).immutableCopy();
        ImmutableSet<IRI> class_dp_intersections = Sets.intersection(classIRIs, dpIRIs).immutableCopy();
        ImmutableSet<IRI> op_dp_intersections = Sets.intersection(opIRIs, dpIRIs).immutableCopy();
        boolean punning = false;

        if (class_op_intersections.size() > 0) {
            System.out.println();
            System.out.format("ERROR: Class and Object property name sets are not disjoint. Violations: \n");
            class_op_intersections.forEach((x) -> System.out.println("- <"+ x + ">"));
            punning = true;
        }

        if (class_dp_intersections.size() > 0) {
            System.out.println();
            System.out.format("ERROR: Class and Data property name sets are not disjoint. Violations: \n");
            class_dp_intersections.forEach((x) -> System.out.println("- <"+ x + ">"));
            punning = true;
        }

        if (op_dp_intersections.size() > 0) {
            System.out.println();
            System.out.format("ERROR: Object and Data property name sets are not disjoint. Violations: \n");
            op_dp_intersections.forEach((x) -> System.out.println("- <"+ x + ">"));
            punning = true;
        }

        if (punning) System.exit(1);

        try {
            config.loadSpecification();
        } catch (OBDASpecificationException e) {
            System.out.format("ERROR: There is a problem loading the mapping file %s\n", mappingFile);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Validation completed");

    }
}
