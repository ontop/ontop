package it.unibz.inf.ontop.protege.core;


import it.unibz.inf.ontop.protege.utils.DialogUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Wrapper around OntopProtegeReasoner for use in the ontop Protege plugin
 * 
 * Used to provide an error message to the user whenever there is an exception during reasoner initialization
 * @author dagc
 *
 */
public class OntopProtegeOWLFactory implements OWLReasonerFactory {

	private void handleError(Exception e){
		DialogUtils.showPrettyMessageDialog(null,
				"Error during reasoner initialization: " + e,
				"Ontop Initialization Error",
				JOptionPane.ERROR_MESSAGE);
	}

	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(OntopProtegeOWLFactory.class);

	@Nonnull
	@Override
	public String getReasonerName() {
		return "Ontop";
	}

	@Nonnull
	@Override
	public OntopProtegeReasoner createNonBufferingReasoner(@Nonnull OWLOntology ontology) {
		UnsupportedOperationException e = new UnsupportedOperationException("Ontop is a buffering reasoner");
		handleError(e);
		throw e;
	}

	@Nonnull
	@Override
	public OntopProtegeReasoner createReasoner(@Nonnull OWLOntology ontology) {
		UnsupportedOperationException e = new UnsupportedOperationException("A configuration is required");
		handleError(e);
		throw e;
	}

	@Nonnull
	@Override
	public OntopProtegeReasoner createNonBufferingReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		UnsupportedOperationException e = new UnsupportedOperationException("Ontop is a buffering reasoner");
		handleError(e);
		throw e;
	}

	@Nonnull
	@Override
	public OntopProtegeReasoner createReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config) throws IllegalConfigurationException {
		try {
			checkArgument(config instanceof OntopProtegeOWLConfiguration, "config %s is not an instance of OntopProtegeOWLConfiguration", config);
			return new OntopProtegeReasoner(ontology, (OntopProtegeOWLConfiguration) config);
		}
		catch (Exception e){
			handleError(e);
			throw e;
		}
	}
}
