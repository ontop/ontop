package it.unibz.inf.ontop.owlapi.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.materialization.impl.DefaultOntopRDFMaterializer;
import it.unibz.inf.ontop.owlapi.OntopOWLAPIMaterializer;
import it.unibz.inf.ontop.owlapi.exception.OntopOWLException;
import it.unibz.inf.ontop.owlapi.resultset.MaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.impl.OntopMaterializedGraphOWLResultSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import javax.annotation.Nonnull;
import java.security.SecureRandom;

public class DefaultOntopOWLAPIMaterializer implements OntopOWLAPIMaterializer {

	private final OntopRDFMaterializer materializer;
	private final RDF rdfFactory;

	public DefaultOntopOWLAPIMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
		materializer = new DefaultOntopRDFMaterializer(configuration, materializationParams);
		rdfFactory = configuration.getInjector().getInstance(RDF.class);
	}

	/**
	 * Materializes the saturated RDF graph with the default options
	 */
	public DefaultOntopOWLAPIMaterializer(OntopSystemConfiguration configuration) throws OBDASpecificationException {
		this(configuration, MaterializationParams.defaultBuilder().build());
	}

	@Override
	public MaterializedGraphOWLResultSet materialize()
			throws OWLException {
		try {
			return wrap(materializer.materialize());
		} catch (OBDASpecificationException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public MaterializedGraphOWLResultSet materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary)
			throws OWLException {
		try {
			return wrap(
					materializer.materialize(
							selectedVocabulary.stream()
									.map(i -> rdfFactory.createIRI(i.toString()))
									.collect(ImmutableCollectors.toSet()))
			);
		} catch (OBDASpecificationException e) {
			throw new OntopOWLException(e);
		}
	}

	@Override
	public ImmutableSet<IRI> getClasses() {
		return materializer.getClasses().stream()
				.map(i -> IRI.create(i.getIRIString()))
				.collect(ImmutableCollectors.toSet());
	}

	@Override
	public ImmutableSet<IRI> getProperties() {
		return materializer.getProperties().stream()
				.map(i -> IRI.create(i.getIRIString()))
				.collect(ImmutableCollectors.toSet());
	}

	private MaterializedGraphOWLResultSet wrap(MaterializedGraphResultSet graphResultSet) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[20];
		random.nextBytes(salt);
		return new OntopMaterializedGraphOWLResultSet(graphResultSet, salt);
	}
}
