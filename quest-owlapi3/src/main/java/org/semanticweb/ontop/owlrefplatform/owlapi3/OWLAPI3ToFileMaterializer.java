package org.semanticweb.ontop.owlrefplatform.owlapi3;

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

import java.io.*;
import java.net.URI;
import java.util.Properties;

import com.google.inject.Guice;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OntopCoreModule;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.owlapi3.QuestOWLIndividualIterator;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

/***
 * This is a first version of an NTriple materializer. Its incomplete and will
 * have problems with special characters. This will be fixed with the official
 * release.
 * 
 * @author mariano
 * 
 */
public class OWLAPI3ToFileMaterializer {

	static NativeQueryLanguageComponentFactory FACTORY = Guice.createInjector(
            new OntopCoreModule(new Properties())).getInstance(NativeQueryLanguageComponentFactory.class);

	public static int materialize(File outputFile, URI inputFile) throws Exception {
		File input = new File(inputFile);
		return materialize(outputFile, input);
	}

	public static int materialize(File outputFile, File inputFile) throws Exception {
        MappingParser mappingParser = FACTORY.create(inputFile);
        OBDAModel newModel = mappingParser.getOBDAModel();

		return materializeN3(outputFile, newModel);
	}
	public static int materializeN3(File outputFile, OBDAModel model) throws Exception {
		return materializeN3(new FileOutputStream(outputFile), model);
	}
	
	public static int materializeN3(OutputStream outputStream, OBDAModel model) throws Exception {
		return materializeN3(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), model);
	}

	public static int materializeN3(Writer bf, OBDAModel model) throws Exception {
		return materializeN3(bf, (new OWLAPI3Materializer(model)).getIterator());
	}
	
	public static int materializeN3(Writer bf, QuestOWLIndividualIterator iterator) throws Exception {

		String rdftype = OBDAVocabulary.RDF_TYPE;
		int count = 0;
		while (iterator.hasNext()) {
			OWLIndividualAxiom axiom = iterator.next();
			if (axiom instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom ax = (OWLClassAssertionAxiom) axiom;
				bf.append("<");
				bf.append(((OWLClassAssertionAxiom) axiom).getIndividual().asOWLNamedIndividual().getIRI().toString());
				bf.append("> <");
				bf.append(rdftype);
				bf.append("> <");
				bf.append(ax.getClassExpression().asOWLClass().getIRI().toString());
				bf.append(">");
			} else if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
				OWLObjectPropertyAssertionAxiom ax = (OWLObjectPropertyAssertionAxiom) axiom;

				bf.append("<");
				bf.append(ax.getSubject().asOWLNamedIndividual().getIRI().toString());
				bf.append("> <");
				bf.append(ax.getProperty().asOWLObjectProperty().getIRI().toString());
				bf.append("> <");
				bf.append(ax.getObject().asOWLNamedIndividual().getIRI().toString());
				bf.append(">");

			} else if (axiom instanceof OWLDataPropertyAssertionAxiom) {
				OWLDataPropertyAssertionAxiom ax = (OWLDataPropertyAssertionAxiom) axiom;

				bf.append("<");
				bf.append(ax.getSubject().asOWLNamedIndividual().getIRI().toString());
				bf.append("> <");
				bf.append(ax.getProperty().asOWLDataProperty().getIRI().toString());
				bf.append("> \"");

				OWLLiteral lit = ax.getObject();

				bf.append(lit.getLiteral().toString());
				bf.append("\"");
				if (lit.isRDFPlainLiteral()) {
					if (lit.hasLang()) {
						bf.append("@");
						bf.append(lit.getLang());
					}
				} else {
					bf.append("^^<");
					bf.append(lit.getDatatype().getIRI().toString());
					bf.append(">");
				}
			}
			bf.append(" .\n");
			count += 1;
		}
		bf.flush();
		bf.close();
		
		return count;
	}
}
