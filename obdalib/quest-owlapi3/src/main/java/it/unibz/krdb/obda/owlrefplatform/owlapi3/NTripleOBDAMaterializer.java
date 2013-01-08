package it.unibz.krdb.obda.owlrefplatform.owlapi3;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
public class NTripleOBDAMaterializer {

	static OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

	public static int materialize(File outputFile, URI inputFile) throws Exception {
		File input = new File(inputFile);
		return materialize(outputFile, input);
	}

	public static int materialize(File outputFile, File inputFile) throws Exception {
		OBDAModel newModel = ofac.getOBDAModel();
		ModelIOManager io = new ModelIOManager(newModel);
		io.load(inputFile);
		return materialize(outputFile, newModel);
	}

	public static int materialize(File outputFile, OBDAModel model) throws Exception {
		OWLAPI3IndividualIterator individuals = new OWLAPI3IndividualIterator(model);
		BufferedWriter bf = new BufferedWriter(new FileWriter(outputFile));

		String rdftype = OBDAVocabulary.RDF_TYPE;
		int count = 0;
		while (individuals.hasNext()) {
			OWLIndividualAxiom axiom = individuals.next();
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
