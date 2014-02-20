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

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlapi3.QuestOWLIndividualIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

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

	static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	public static int materialize(File outputFile, URI inputFile) throws Exception {
		File input = new File(inputFile);
		return materialize(outputFile, input);
	}

	public static int materialize(File outputFile, File inputFile) throws Exception {
		OBDAModel newModel = ofac.getOBDAModel();
		ModelIOManager io = new ModelIOManager(newModel);
		io.load(inputFile);
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
