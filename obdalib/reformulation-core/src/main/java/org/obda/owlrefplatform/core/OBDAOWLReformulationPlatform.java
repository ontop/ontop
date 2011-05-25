package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.DatasourcesController;
import inf.unibz.it.obda.api.inference.reasoner.DataQueryReasoner;
import inf.unibz.it.obda.queryanswering.Statement;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.obda.owlrefplatform.core.ontology.DLLiterOntology;
import org.obda.owlrefplatform.core.ontology.imp.DLLiterOntologyImpl;
import org.obda.owlrefplatform.core.ontology.imp.OWLAPITranslator;
import org.obda.owlrefplatform.exception.OBDAOWLReformulationPlatformException;
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

/**
 * The OBDAOWLReformulationPlatform implements the OWL reasoner interface
 * and is the implementation of the reasoning method in the reformulation
 * project.
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class OBDAOWLReformulationPlatform implements OWLReasoner, DataQueryReasoner, MonitorableOWLReasoner {

	private static final String		NOT_IMPLEMENTED_STR		= "Service not available.";

	private APIController apic = null;
	private OWLOntologyManager ontoManager = null;
	private DLLiterOntology ontology = null;
	private TechniqueWrapper techwrapper = null;
	private HashSet<OWLOntology>	loadedOntologies		= null;
	private ProgressMonitor progressMonitor = new NullProgressMonitor();
	private DatasourcesController dscon = null;

	private boolean isClassified = false;


	protected OBDAOWLReformulationPlatform(APIController apic, OWLOntologyManager manager, TechniqueWrapper wrapper) throws Exception{

		this.apic = apic;
		this.ontoManager = manager;
		this.techwrapper = wrapper;
		dscon = apic.getDatasourcesController();
//		ABoxToDBDumper.getInstance().addListener(this);
		loadOntologies(ontoManager.getOntologies());
	}

	/**
	 * Set the technique wrapper which specifies which rewriting, unfolding 
	 * and evaluation techniques are used.
	 * @param newTechnique the technique wrapper
	 */
	public void setTechniqueWrapper(TechniqueWrapper newTechnique){
		techwrapper = newTechnique;
	}

	@Override
	public Statement getStatement() throws Exception {
		return techwrapper.getStatement();
	}

	/**
	 * translate the a owl ontology into a dlliter ontology
	 * @return a dlliter ontology
	 */
	public DLLiterOntology getTranslatedOntology(){

		if(ontology== null){
			throw new NullPointerException("No ontology has been loaded.");
		}
		return ontology;
	}

	public boolean isConsistent(OWLOntology ontology)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
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
	}


	public Set<OWLOntology> getLoadedOntologies() {
		return loadedOntologies;
	}


	public boolean isClassified() throws OWLReasonerException {
		return isClassified;
	}


	public boolean isDefined(OWLClass cls) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isDefined(OWLObjectProperty prop)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isDefined(OWLDataProperty prop) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isDefined(OWLIndividual ind) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isRealised() throws OWLReasonerException {
		return isClassified;
	}


	public void loadOntologies(Set<OWLOntology> ontologies)
			throws OWLReasonerException {

		OWLAPITranslator translator = new OWLAPITranslator();
		URI uri = null;
		if(ontologies.size() >0){
			uri = ontologies.iterator().next().getURI();
		}
		ontology= new DLLiterOntologyImpl(uri);

		Set<URI> uris = new HashSet<URI>();
		Iterator<OWLOntology> it = ontologies.iterator();
		while(it.hasNext()){
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


	public void unloadOntologies(Set<OWLOntology> ontologies)
			throws OWLReasonerException {
		boolean result = loadedOntologies.removeAll(ontologies);
		// if no ontologies where removed
		if (!result)
			return;

		// otherwise clear everything and update
		HashSet<OWLOntology> resultSet = loadedOntologies;
		clearOntologies();
		loadOntologies(resultSet);
	}


	public Set<Set<OWLClass>> getAncestorClasses(OWLDescription clsC)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLClass>> getDescendantClasses(OWLDescription clsC)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLClass> getEquivalentClasses(OWLDescription clsC)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLClass> getInconsistentClasses() throws OWLReasonerException {
//		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);{
		return new HashSet<OWLClass>();
	}


	public Set<Set<OWLClass>> getSubClasses(OWLDescription clsC)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLClass>> getSuperClasses(OWLDescription clsC)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isEquivalentClass(OWLDescription clsC, OWLDescription clsD)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isSubClassOf(OWLDescription clsC, OWLDescription clsD)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isSatisfiable(OWLDescription description)
			throws OWLReasonerException {
		return true;
	}


	public Map<OWLDataProperty, Set<OWLConstant>> getDataPropertyRelationships(
			OWLIndividual individual) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLIndividual> getIndividuals(OWLDescription clsC, boolean direct)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationships(
			OWLIndividual individual) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLIndividual> getRelatedIndividuals(OWLIndividual subject,
			OWLObjectPropertyExpression property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLConstant> getRelatedValues(OWLIndividual subject,
			OWLDataPropertyExpression property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLClass>> getTypes(OWLIndividual individual, boolean direct)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean hasDataPropertyRelationship(OWLIndividual subject,
			OWLDataPropertyExpression property, OWLConstant object)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean hasObjectPropertyRelationship(OWLIndividual subject,
			OWLObjectPropertyExpression property, OWLIndividual object)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean hasType(OWLIndividual individual, OWLDescription type,
			boolean direct) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLObjectProperty>> getAncestorProperties(
			OWLObjectProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLDataProperty>> getAncestorProperties(
			OWLDataProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLObjectProperty>> getDescendantProperties(
			OWLObjectProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLDataProperty>> getDescendantProperties(
			OWLDataProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLDescription>> getDomains(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLDescription>> getDomains(OWLDataProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLObjectProperty> getEquivalentProperties(
			OWLObjectProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLObjectProperty>> getInverseProperties(
			OWLObjectProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLDescription> getRanges(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<OWLDataRange> getRanges(OWLDataProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLObjectProperty>> getSubProperties(
			OWLObjectProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLObjectProperty>> getSuperProperties(
			OWLObjectProperty property) throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isAntiSymmetric(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isFunctional(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isFunctional(OWLDataProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isInverseFunctional(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isIrreflexive(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isReflexive(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isSymmetric(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public boolean isTransitive(OWLObjectProperty property)
			throws OWLReasonerException {
		throw new OBDAOWLReformulationPlatformException(NOT_IMPLEMENTED_STR);
	}


	public OWLEntity getCurrentEntity() {
		return ontoManager.getOWLDataFactory().getOWLThing();
	}

	public void setProgressMonitor(ProgressMonitor progressMonitor) {

//		this.progressMonitor = progressMonitor;
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
//		getProgressMonitor().setMessage(msg);
//		getProgressMonitor().setIndeterminate(true);
//		getProgressMonitor().setStarted();

	}
}
