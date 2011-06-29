package it.unibz.krdb.obda.owlrefplatform.core;

import it.unibz.krdb.obda.model.DataQueryReasoner;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.DLLiterOntology;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.semanticweb.owl.inference.MonitorableOWLReasoner;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.NullProgressMonitor;
import org.semanticweb.owl.util.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface and is
 * the implementation of the reasoning method in the reformulation project.
 * 
 * 
 */

public class OBDAOWLReformulationPlatform implements OWLReasoner, DataQueryReasoner, MonitorableOWLReasoner {

	private static final String		NOT_IMPLEMENTED_STR	= "Service not available.";

	private OWLOntologyManager		ontoManager			= null;
	private DLLiterOntology			ontology			= null;
	private TechniqueWrapper		techwrapper			= null;
	private HashSet<OWLOntology>	loadedOntologies	= null;
	private ProgressMonitor			progressMonitor		= new NullProgressMonitor();
	
	private Logger log = LoggerFactory.getLogger(OBDAOWLReformulationPlatform.class);

	private boolean					isClassified		= false;

	protected OBDAOWLReformulationPlatform(OBDAModel apic, OWLOntologyManager manager, TechniqueWrapper wrapper) throws Exception {

		this.ontoManager = manager;
		this.techwrapper = wrapper;
		loadOntologies(ontoManager.getOntologies());
	}

	/**
	 * Set the technique wrapper which specifies which rewriting, unfolding and
	 * evaluation techniques are used.
	 * 
	 * @param newTechnique
	 *            the technique wrapper
	 */
	public void setTechniqueWrapper(TechniqueWrapper newTechnique) {
		techwrapper = newTechnique;
	}

	@Override
	public Statement getStatement() throws Exception {
		return techwrapper.getStatement();
	}

	/**
	 * translate the a owl ontology into a dlliter ontology
	 * 
	 * @return a dlliter ontology
	 */
	public DLLiterOntology getTranslatedOntology() {

		if (ontology == null) {
			throw new NullPointerException("No ontology has been loaded.");
		}
		return ontology;
	}

	public boolean isConsistent(OWLOntology ontology) throws OWLReasonerException {
		// TODO implement OWL
		return true;
	}

	public void classify() throws OWLReasonerException {

		getProgressMonitor().setIndeterminate(true);
		getProgressMonitor().setMessage("Classifying...");
		getProgressMonitor().setStarted();

		clearOntologies();
		loadOntologies(ontoManager.getOntologies());

		isClassified = true;
		getProgressMonitor().setFinished();
	}

	public void clearOntologies() throws OWLReasonerException {
		loadedOntologies = new HashSet<OWLOntology>();
		ontology = null;
		isClassified = false;
	}

	public void dispose() throws OWLReasonerException {
		techwrapper.dispose();
	}

	public Set<OWLOntology> getLoadedOntologies() {
		return loadedOntologies;
	}

	public boolean isClassified() throws OWLReasonerException {
		return isClassified;
	}

	public boolean isDefined(OWLClass cls) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isDefined(OWLObjectProperty prop) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isDefined(OWLDataProperty prop) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isDefined(OWLIndividual ind) throws OWLReasonerException {
		// TODO implement
		return true;
	}

	public boolean isRealised() throws OWLReasonerException {
		return isClassified;
	}

	public void loadOntologies(Set<OWLOntology> ontologies) throws OWLReasonerException {
		log.debug("OBDAOWLReformulationPlatform is loading ontologies...");

		OWLAPITranslator translator = new OWLAPITranslator();
		URI uri = null;
		if (ontologies.size() > 0) {
			uri = ontologies.iterator().next().getURI();
		}
		ontology = new DLLiterOntologyImpl(uri);

		Set<URI> uris = new HashSet<URI>();
		Iterator<OWLOntology> it = ontologies.iterator();
		while (it.hasNext()) {
			OWLOntology o = it.next();
			uris.add(o.getURI());
			DLLiterOntology aux;
			try {
				aux = translator.translate(o);
				ontology.addAssertions(aux.getAssertions());

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "FAILURE", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	public void realise() throws OWLReasonerException {

	}

	public void unloadOntologies(Set<OWLOntology> ontologies) throws OWLReasonerException {
		boolean result = loadedOntologies.removeAll(ontologies);
		// if no ontologies where removed
		if (!result)
			return;

		// otherwise clear everything and update
		HashSet<OWLOntology> resultSet = loadedOntologies;
		clearOntologies();
		loadOntologies(resultSet);
	}

	public Set<Set<OWLClass>> getAncestorClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public Set<Set<OWLClass>> getDescendantClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public Set<OWLClass> getEquivalentClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLClass>();
	}

	public Set<OWLClass> getInconsistentClasses() throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLClass>();
	}

	public Set<Set<OWLClass>> getSubClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public Set<Set<OWLClass>> getSuperClasses(OWLDescription clsC) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();
	}

	public boolean isEquivalentClass(OWLDescription clsC, OWLDescription clsD) throws OWLReasonerException {
		// TODO implement owl
		return true;
	}

	public boolean isSubClassOf(OWLDescription clsC, OWLDescription clsD) throws OWLReasonerException {
		// TODO implement owl
		return true;
	}

	public boolean isSatisfiable(OWLDescription description) throws OWLReasonerException {
		// TODO implement owl
		return true;
	}

	public Map<OWLDataProperty, Set<OWLConstant>> getDataPropertyRelationships(OWLIndividual individual) throws OWLReasonerException {
		// TODO implement owl
		return new HashMap<OWLDataProperty, Set<OWLConstant>>();
	}

	public Set<OWLIndividual> getIndividuals(OWLDescription clsC, boolean direct) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLIndividual>();
	}

	public Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) throws OWLReasonerException {
		// TODO implement owl
		return new HashMap<OWLObjectProperty, Set<OWLIndividual>>();
	}

	public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject, OWLObjectPropertyExpression property)
			throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLIndividual>();
	}

	public Set<OWLConstant> getRelatedValues(OWLIndividual subject, OWLDataPropertyExpression property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLConstant>();
	}

	public Set<Set<OWLClass>> getTypes(OWLIndividual individual, boolean direct) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLClass>>();

	}

	public boolean hasDataPropertyRelationship(OWLIndividual subject, OWLDataPropertyExpression property, OWLConstant object)
			throws OWLReasonerException {
		//TODO implement
		return false;
	}

	public boolean hasObjectPropertyRelationship(OWLIndividual subject, OWLObjectPropertyExpression property, OWLIndividual object)
			throws OWLReasonerException {
		//TODO implement
		return false;
	}

	public boolean hasType(OWLIndividual individual, OWLDescription type, boolean direct) throws OWLReasonerException {
		//TODO implement
		return false;
	}

	public Set<Set<OWLObjectProperty>> getAncestorProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();

	}

	public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();

	}

	public Set<Set<OWLObjectProperty>> getDescendantProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();

	}

	public Set<Set<OWLDataProperty>> getDescendantProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();
	}

	public Set<Set<OWLDescription>> getDomains(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDescription>>();
	}

	public Set<Set<OWLDescription>> getDomains(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDescription>>();

	}

	public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLObjectProperty>();

	}

	public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLDataProperty>();
	}

	public Set<Set<OWLObjectProperty>> getInverseProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();
	}

	public Set<OWLDescription> getRanges(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLDescription>();
	}

	public Set<OWLDataRange> getRanges(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<OWLDataRange>();
	}

	public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();

	}

	public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();
	}

	public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLObjectProperty>>();
	}

	public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property) throws OWLReasonerException {
		// TODO implement owl
		return new HashSet<Set<OWLDataProperty>>();
	}

	public boolean isAntiSymmetric(OWLObjectProperty property) throws OWLReasonerException {
		// TODO implement owl
		return false;
	}

	public boolean isFunctional(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isFunctional(OWLDataProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isInverseFunctional(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isIrreflexive(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isReflexive(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isSymmetric(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public boolean isTransitive(OWLObjectProperty property) throws OWLReasonerException {
		return false;
	}

	public OWLEntity getCurrentEntity() {
		return ontoManager.getOWLDataFactory().getOWLThing();
	}

	public void setProgressMonitor(ProgressMonitor progressMonitor) {

		// this.progressMonitor = progressMonitor;
	}

	private ProgressMonitor getProgressMonitor() {
		if (progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		return progressMonitor;
	}

	public void finishProgressMonitor() {

		getProgressMonitor().setFinished();
	}

	public void startProgressMonitor(String msg) {
		// getProgressMonitor().setMessage(msg);
		// getProgressMonitor().setIndeterminate(true);
		// getProgressMonitor().setStarted();

	}
}
