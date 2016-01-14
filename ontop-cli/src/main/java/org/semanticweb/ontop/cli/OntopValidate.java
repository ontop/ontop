package org.semanticweb.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Command(name = "validate",
        description = "Validate Ontology and Mappings")
public class OntopValidate extends OntopReasoningCommandBase {

    public OntopValidate() {
    }

    @Override
    public void run() {

        if (!Files.exists(Paths.get(owlFile))) {
            System.err.format("The Ontology file %s does not exist\n", owlFile);
            System.exit(1);
        }

        OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
        OWLOntology ontology = null;

        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(this.owlFile));
        } catch (OWLOntologyCreationException e) {
            System.err.format("There is a problem loading the ontology file: %s\n", owlFile);
            e.printStackTrace();
            System.exit(1);
        }

        Set<IRI> classIRIs = ontology.getClassesInSignature().stream()
                .map(OWLNamedObject::getIRI).collect(toSet());

        Set<IRI> opIRIs = ontology.getObjectPropertiesInSignature().stream()
                .map(OWLNamedObject::getIRI).collect(toSet());

        Set<IRI> dpIRIs = ontology.getDataPropertiesInSignature().stream()
                .map(OWLNamedObject::getIRI).collect(toSet());

        ImmutableSet<IRI> class_op_Intersections = Sets.intersection(classIRIs, opIRIs).immutableCopy();
        ImmutableSet<IRI> class_dp_intersections = Sets.intersection(classIRIs, dpIRIs).immutableCopy();
        ImmutableSet<IRI> op_dp_Intersections = Sets.intersection(opIRIs, dpIRIs).immutableCopy();
        boolean punning = false;

        if (class_op_Intersections.size() > 0) {
            System.err.format("Class and Object property name sets are not disjoint. Violations: \n");
            class_dp_intersections.forEach(System.out::println);
            punning = true;
        }

        if (class_dp_intersections.size() > 0) {
            System.err.format("Class and Data property name sets are not disjoint. Violations: \n");
            class_dp_intersections.forEach(System.out::println);
            punning = true;
        }

        if (op_dp_Intersections.size() > 0) {
            System.err.format("Object and Data property name sets are not disjoint. Violations: \n");
            op_dp_Intersections.forEach(System.out::println);
            punning = true;
        }

        if (punning) System.exit(1);



    }
}
