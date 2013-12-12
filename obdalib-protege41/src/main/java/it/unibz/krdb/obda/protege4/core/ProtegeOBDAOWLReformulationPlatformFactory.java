package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import java.util.Properties;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ProtegeOBDAOWLReformulationPlatformFactory extends AbstractProtegeOWLReasonerInfo {

	QuestOWLFactory factory = new QuestOWLFactory();

	@Override
	public BufferingMode getRecommendedBuffering() {
		return BufferingMode.BUFFERING;
	}

	@Override
	public OWLReasonerFactory getReasonerFactory() {
		return factory;
	}

	public void setPreferences(Properties preferences) {
		factory.setPreferenceHolder(preferences);
	}

	public void setOBDAModel(OBDAModel model) {
		factory.setOBDAController(model);
	}

}
