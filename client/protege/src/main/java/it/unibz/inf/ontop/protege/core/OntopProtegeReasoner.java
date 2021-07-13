package it.unibz.inf.ontop.protege.core;

import it.unibz.inf.ontop.owlapi.OntopOWLFactory;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.validation.OntopOWLEmptyEntitiesChecker;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.util.Version;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Wrapper around OntopOWLReasoner to use inside Protege
 */
public class OntopProtegeReasoner extends OWLReasonerBase implements AutoCloseable {

    private final OntopOWLFactory factory = OntopOWLFactory.defaultFactory();
    private final OntopProtegeOWLConfiguration configuration;

    private OntopOWLReasoner reasoner;
    private OntopOWLConnection owlConnection;

    protected OntopProtegeReasoner(OWLOntology rootOntology, OntopProtegeOWLConfiguration configuration) throws IllegalConfigurationException {
        super(rootOntology, configuration, BufferingMode.BUFFERING);
        this.configuration = configuration;

        reasonerSetup();
    }

    public OntopOWLStatement getStatement() throws OWLException {
        if (owlConnection == null)
            owlConnection = reasoner.getConnection();

        return owlConnection.createStatement();
    }


    @Nonnull
    @Override
    public String getReasonerName() {
        return reasoner.getReasonerName();
    }

    @Nonnull
    @Override
    public Version getReasonerVersion() {
        return reasoner.getReasonerVersion();
    }

    @Override
    public void flush() {
        super.flush();
        reasonerSetup();
    }

    private void reasonerSetup() {
        try {
            reasoner = factory.createReasoner(configuration.getOntopConfiguration(getRootOntology()));
            owlConnection = reasoner.getConnection();
        }
        catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        reasoner.interrupt();
    }

    @Override
    public void precomputeInferences(@Nonnull InferenceType... inferenceTypes) {
        reasoner.precomputeInferences(inferenceTypes);
    }

    @Override
    public boolean isPrecomputed(@Nonnull InferenceType inferenceType) {
        return reasoner.isPrecomputed(inferenceType);
    }

    @Nonnull
    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return reasoner.getPrecomputableInferenceTypes();
    }

    @Override
    protected void handleChanges(@Nonnull Set<OWLAxiom> addAxioms, @Nonnull Set<OWLAxiom> removeAxioms) {
        reasoner.flush();
    }


    //to avoid connection to the database everytime this method is called,
    // we test consistency using the method isQuestConsistent

    @Override
    public boolean isConsistent() {
        return true;
    }


    public boolean isQuestConsistent() throws ReasonerInterruptedException, TimeOutException {
        return reasoner.isConsistent();
    }

    //info to return which axiom was inconsistent during the check in isQuestConsistent
    public Object getInconsistentAxiom() {
        return reasoner.getInconsistentAxiom();
    }

    @Override
    public boolean isSatisfiable(@Nonnull OWLClassExpression classExpression) {
        return reasoner.isSatisfiable(classExpression);
    }

    @Nonnull
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return reasoner.getUnsatisfiableClasses();
    }

    @Override
    public boolean isEntailed(@Nonnull OWLAxiom axiom) {
        return reasoner.isEntailed(axiom);
    }

    @Override
    public boolean isEntailed(@Nonnull Set<? extends OWLAxiom> axioms) {
        return reasoner.isEntailed(axioms);
    }

    @Override
    public boolean isEntailmentCheckingSupported(@Nonnull AxiomType<?> axiomType) {
        return reasoner.isEntailmentCheckingSupported(axiomType);
    }

    @Nonnull
    @Override
    public Node<OWLClass> getTopClassNode() {
        return reasoner.getTopClassNode();
    }

    @Nonnull
    @Override
    public Node<OWLClass> getBottomClassNode() {
        return reasoner.getBottomClassNode();
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getSubClasses(@Nonnull OWLClassExpression ce, boolean direct) {
        return reasoner.getSubClasses(ce, direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getSuperClasses(@Nonnull OWLClassExpression ce, boolean direct) {
        return reasoner.getSuperClasses(ce,direct);
    }

    @Nonnull
    @Override
    public Node<OWLClass> getEquivalentClasses(@Nonnull OWLClassExpression ce) {
        return reasoner.getEquivalentClasses(ce);
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getDisjointClasses(@Nonnull OWLClassExpression ce) {
        return reasoner.getDisjointClasses(ce);
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return reasoner.getTopObjectPropertyNode();
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return reasoner.getBottomObjectPropertyNode();
    }

    @Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return reasoner.getSubObjectProperties(pe,direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return reasoner.getSuperObjectProperties(pe, direct);
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(@Nonnull OWLObjectPropertyExpression pe) {
        return reasoner.getEquivalentObjectProperties(pe);
    }

    @Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(@Nonnull OWLObjectPropertyExpression pe) {
        return reasoner.getDisjointObjectProperties(pe);
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(@Nonnull OWLObjectPropertyExpression pe) {
        return reasoner.getInverseObjectProperties(pe);
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return reasoner.getObjectPropertyDomains(pe, direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return reasoner.getObjectPropertyRanges(pe, direct);
    }

    @Nonnull
    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return reasoner.getTopDataPropertyNode();
    }

    @Nonnull
    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return reasoner.getBottomDataPropertyNode();
    }

    @Nonnull
    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(@Nonnull OWLDataProperty pe, boolean direct) {
        return reasoner.getSubDataProperties(pe, direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(@Nonnull OWLDataProperty pe, boolean direct) {
        return reasoner.getSuperDataProperties(pe,direct);
    }

    @Nonnull
    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(@Nonnull OWLDataProperty pe) {
        return reasoner.getEquivalentDataProperties(pe);
    }

    @Nonnull
    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(@Nonnull OWLDataPropertyExpression pe) {
        return reasoner.getDisjointDataProperties(pe);
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(@Nonnull OWLDataProperty pe, boolean direct) {
        return reasoner.getDataPropertyDomains(pe, direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getTypes(@Nonnull OWLNamedIndividual ind, boolean direct) {
        return reasoner.getTypes(ind,direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getInstances(@Nonnull OWLClassExpression ce, boolean direct) {
        return reasoner.getInstances(ce,direct);
    }

    @Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(@Nonnull OWLNamedIndividual ind, @Nonnull OWLObjectPropertyExpression pe) {
        return reasoner.getObjectPropertyValues(ind,pe);
    }

    @Nonnull
    @Override
    public Set<OWLLiteral> getDataPropertyValues(@Nonnull OWLNamedIndividual ind, @Nonnull OWLDataProperty pe) {
        return reasoner.getDataPropertyValues(ind,pe);
    }

    @Nonnull
    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(@Nonnull OWLNamedIndividual ind) {
        return reasoner.getSameIndividuals(ind);
    }

    @Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(@Nonnull OWLNamedIndividual ind) {
        return reasoner.getDifferentIndividuals(ind);
    }

    @Override
    public void close() throws Exception {
        owlConnection.close();
        reasoner.close();
    }

    /**
     * Methods to get the empty concepts and roles in the ontology using the given mappings.
     * It generates SPARQL queries to check for entities.
     * @return OntopOWLEmptyEntitiesChecker class to get empty concepts and roles
     */
    public OntopOWLEmptyEntitiesChecker getEmptyEntitiesChecker() {
        OWLAPITranslatorOWL2QL owlapiTranslator = configuration.getOntopConfiguration(getRootOntology())
                .getInjector().getInstance(OWLAPITranslatorOWL2QL.class);
        Ontology mergeOntology = owlapiTranslator.translateAndClassify(getRootOntology());
        ClassifiedTBox tBox = mergeOntology.tbox();

        return new OntopOWLEmptyEntitiesChecker(tBox, owlConnection);
    }
}
