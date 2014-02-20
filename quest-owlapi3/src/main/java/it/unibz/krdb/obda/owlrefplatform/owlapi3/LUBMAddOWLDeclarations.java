package it.unibz.krdb.obda.owlrefplatform.owlapi3;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

public class LUBMAddOWLDeclarations {

	public static void main(String args[]) {

		try {

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(
					"src/test/resources/test/lubm-ex-20-uni1/University0-imports.owl"));
			
			Set<OWLOntology> closure = manager.getImportsClosure(ontology);
			
			
			OWLOntologyMerger merger = new OWLOntologyMerger(manager);
			ontology = merger.createMergedOntology(manager, IRI.create("http://www.unibz.it/krdb/obda/ontop/bench/lubmex20/declarations.owl"));
			
			OWLOntology dOntology = manager.createOntology();
			OWLDataFactory f = manager.getOWLDataFactory();
			
			Set<OWLAxiom> axioms = ontology.getTBoxAxioms(true);
			for (OWLAxiom ax: axioms) {
				
				Set<OWLClass> classes= ax.getClassesInSignature();
				Set<OWLObjectProperty> oprops = ax.getObjectPropertiesInSignature();
				Set<OWLDataProperty> dprops = ax.getDataPropertiesInSignature();
				
				for (OWLClass c : classes) {
					manager.addAxiom(dOntology, f.getOWLDeclarationAxiom(c));
				}
				
				for (OWLObjectProperty c : oprops) {
					manager.addAxiom(dOntology, f.getOWLDeclarationAxiom(c));
				}
				
				for (OWLDataProperty c : dprops) {
					manager.addAxiom(dOntology, f.getOWLDeclarationAxiom(c));
				}
				
			}
			
			Set<OWLAxiom> annotation = ontology.getAxioms();
			HashSet<OWLAxiom> declarations = new HashSet<OWLAxiom>();
			for (OWLAxiom ann: annotation) {
				if (!(ann instanceof OWLAnnotationAssertionAxiom))
					continue;
				OWLAnnotationAssertionAxiom ax = (OWLAnnotationAssertionAxiom)ann;
				
				OWLAnnotationValue v = ax.getValue();
				if (v instanceof OWLLiteral) {
					OWLAnnotationProperty property = ax.getProperty();
					OWLDataProperty p = f.getOWLDataProperty(property.getIRI());
					declarations.add(f.getOWLDeclarationAxiom(p));
					OWLAxiom assertion = f.getOWLDataPropertyAssertionAxiom(p, f.getOWLNamedIndividual((IRI)ax.getSubject()), (OWLLiteral)v);
					declarations.add(assertion);
				} else {
					OWLAnnotationProperty property = ax.getProperty();
					OWLObjectProperty p = f.getOWLObjectProperty(property.getIRI());
					declarations.add(f.getOWLDeclarationAxiom(p));
					OWLAxiom assertion = f.getOWLObjectPropertyAssertionAxiom(p, f.getOWLNamedIndividual((IRI)ax.getSubject()), f.getOWLNamedIndividual((IRI)v));
					declarations.add(assertion);
				}
			}
			FileDocumentTarget file = new FileDocumentTarget(new File("src/test/resources/test/lubm-ex-20-uni1/merge.owl"));
			OWLOntology merge = manager.createOntology();
			manager.addAxioms(merge, ontology.getABoxAxioms(true));
			manager.addAxioms(merge, declarations);
			manager.saveOntology(merge, file);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
