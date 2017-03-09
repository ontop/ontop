package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import org.semanticweb.owlapi.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Command(name = "validate",
        description = "Validate Ontology and Mappings")
public class OntopValidate extends OntopReasoningCommandBase {

    public OntopValidate() {
    }

    @Override
    public void run() {

        if (!Files.exists(Paths.get(owlFile))) {
            System.out.format("ERROR: The Ontology file %s does not exist\n", owlFile);
            System.exit(1);
        }

        QuestConfiguration config;
        if (isR2rmlFile(mappingFile)) {
            config = QuestConfiguration.defaultBuilder()
                    .ontologyFile(owlFile)
                    .r2rmlMappingFile(mappingFile)
                    .build();
        }
        else {
            config = QuestConfiguration.defaultBuilder()
                    .ontologyFile(owlFile)
                    .nativeOntopMappingFile(mappingFile)
                    .build();
        }

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
            System.out.format("ERROR: QuestOWL reasoner cannot be initialized\n");
            ex.printStackTrace();
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
            config.loadMapping();
        } catch (IOException | InvalidDataSourceException | InvalidMappingException e) {
            System.out.format("ERROR: There is a problem loading the mapping file %s\n", mappingFile);
            e.printStackTrace();
            System.exit(1);
        }

    }
}
