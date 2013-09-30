package it.unibz.krdb.obda.reformulation.owlapi3;

import static org.junit.Assert.assertFalse;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;

import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class InconsistencyCheckingTest {

	@Test
	public void testInconsistency() throws OWLOntologyCreationException {

		String prefix = "http://www.example.org/";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLObjectProperty r1 = ObjectProperty(IRI.create(prefix + "#R1"));
		OWLObjectProperty r1_1 = ObjectProperty(IRI.create(prefix + "#R1_1"));
		OWLObjectProperty r2 = ObjectProperty(IRI.create(prefix + "#R2"));

		OWLNamedIndividual a = NamedIndividual(IRI.create(prefix + "#a"));
		OWLNamedIndividual b = NamedIndividual(IRI.create(prefix + "#b"));

		OWLOntology ontology = Ontology(manager, //
				Declaration(r1), //
				Declaration(r1_1), //
				Declaration(r2), //
				SubObjectPropertyOf(r1_1, r2), //
				DisjointObjectProperties(r1, r2), //
				ObjectPropertyAssertion(r1_1, a, b), //
				ObjectPropertyAssertion(r2, a, b) //
		);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		QuestOWLFactory questOWLFactory = new QuestOWLFactory();
		questOWLFactory.setPreferenceHolder(p);
		OWLReasoner reasoner = questOWLFactory.createReasoner(ontology);
		boolean consistent = reasoner.isConsistent();
		assertFalse(consistent);

	}

}
