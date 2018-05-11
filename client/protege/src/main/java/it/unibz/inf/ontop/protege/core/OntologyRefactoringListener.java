package it.unibz.inf.ontop.protege.core;

import com.google.common.base.Optional;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.RemoveImportData;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.*;

/***
 * This ontology change listener has some euristics that determine if the
 * user is refactoring his ontology. In particular, this listener will try
 * to determine if some add/remove axioms are in fact a "renaming"
 * operation. This happens when a list of axioms has a
 * remove(DeclarationAxiom(x)) immediatly followed by an
 * add(DeclarationAxiom(y)), in this case, y is a renaming for x.
 */
public class OntologyRefactoringListener implements OWLOntologyChangeListener {
    private static final Logger log = LoggerFactory.getLogger(OntologyRefactoringListener.class);
    private OWLEditorKit owlEditorKit;
    private OBDAModelManager obdaModelManager;

    OntologyRefactoringListener(OWLEditorKit owlEditorKit, OBDAModelManager obdaModelManager) {
        this.owlEditorKit = owlEditorKit;
        this.obdaModelManager = obdaModelManager;
    }

    @Override
    public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) {
        Map<OWLEntity, OWLEntity> renamings = new HashMap<OWLEntity, OWLEntity>();
        Set<OWLEntity> removals = new HashSet<OWLEntity>();

        for (int idx = 0; idx < changes.size(); idx++) {
            OWLOntologyChange change = changes.get(idx);
            if (change instanceof SetOntologyID) {

                updateOntologyID((SetOntologyID) change);

                continue;
            } else if (change instanceof AddImport) {

                AddImportData addedImport = ((AddImport) change).getChangeData();
                IRI addedOntoIRI = addedImport.getDeclaration().getIRI();

                OWLOntology addedOnto = owlEditorKit.getModelManager().getOWLOntologyManager().getOntology(addedOntoIRI);
                OBDAModel activeOBDAModel = obdaModelManager.getActiveOBDAModel();

                // Setup the entity declarations
                for (OWLClass c : addedOnto.getClassesInSignature())
                    activeOBDAModel.getCurrentVocabulary().classes().declare(c.getIRI());

                for (OWLObjectProperty r : addedOnto.getObjectPropertiesInSignature())
                    activeOBDAModel.getCurrentVocabulary().objectProperties().declare(r.getIRI());

                for (OWLDataProperty p : addedOnto.getDataPropertiesInSignature())
                    activeOBDAModel.getCurrentVocabulary().dataProperties().declare(p.getIRI());

                for (OWLAnnotationProperty p : addedOnto.getAnnotationPropertiesInSignature())
                    activeOBDAModel.getCurrentVocabulary().annotationProperties().declare(p.getIRI());

                continue;
            } else if (change instanceof RemoveImport) {

                RemoveImportData removedImport = ((RemoveImport) change).getChangeData();
                IRI removedOntoIRI = removedImport.getDeclaration().getIRI();

                OWLOntology removedOnto = owlEditorKit.getModelManager().getOWLOntologyManager().getOntology(removedOntoIRI);
                OBDAModel activeOBDAModel = obdaModelManager.getActiveOBDAModel();

                for (OWLClass c : removedOnto.getClassesInSignature())
                    activeOBDAModel.getCurrentVocabulary().classes().remove(c.getIRI());

                for (OWLObjectProperty r : removedOnto.getObjectPropertiesInSignature())
                    activeOBDAModel.getCurrentVocabulary().objectProperties().remove(r.getIRI());

                for (OWLDataProperty p : removedOnto.getDataPropertiesInSignature())
                    activeOBDAModel.getCurrentVocabulary().dataProperties().remove(p.getIRI());

                for (OWLAnnotationProperty p : removedOnto.getAnnotationPropertiesInSignature())
                    activeOBDAModel.getCurrentVocabulary().annotationProperties().remove(p.getIRI());

                continue;
            } else if (change instanceof AddAxiom) {
                OWLAxiom axiom = change.getAxiom();
                if (axiom instanceof OWLDeclarationAxiom) {

                    OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
                    OBDAModel activeOBDAModel = obdaModelManager.getActiveOBDAModel();
                    if (entity instanceof OWLClass) {
                        OWLClass oc = (OWLClass) entity;
                        activeOBDAModel.getCurrentVocabulary().classes().declare(oc.getIRI());
                    } else if (entity instanceof OWLObjectProperty) {
                        OWLObjectProperty or = (OWLObjectProperty) entity;
                        activeOBDAModel.getCurrentVocabulary().objectProperties().declare(or.getIRI());
                    } else if (entity instanceof OWLDataProperty) {
                        OWLDataProperty op = (OWLDataProperty) entity;
                        activeOBDAModel.getCurrentVocabulary().dataProperties().declare(op.getIRI());
                    } else if (entity instanceof OWLAnnotationProperty) {
                        OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
                        activeOBDAModel.getCurrentVocabulary().annotationProperties().declare(ap.getIRI());
                    }
                }
            } else if (change instanceof RemoveAxiom) {
                OWLAxiom axiom = change.getAxiom();
                if (axiom instanceof OWLDeclarationAxiom) {
                    OWLEntity entity = ((OWLDeclarationAxiom) axiom).getEntity();
                    OBDAModel activeOBDAModel = obdaModelManager.getActiveOBDAModel();
                    if (entity instanceof OWLClass) {
                        OWLClass oc = (OWLClass) entity;
                        activeOBDAModel.getCurrentVocabulary().classes().remove(oc.getIRI());
                    } else if (entity instanceof OWLObjectProperty) {
                        OWLObjectProperty or = (OWLObjectProperty) entity;
                        activeOBDAModel.getCurrentVocabulary().objectProperties().remove(or.getIRI());
                    } else if (entity instanceof OWLDataProperty) {
                        OWLDataProperty op = (OWLDataProperty) entity;
                        activeOBDAModel.getCurrentVocabulary().dataProperties().remove(op.getIRI());
                    } else if (entity instanceof OWLAnnotationProperty) {
                        OWLAnnotationProperty ap = (OWLAnnotationProperty) entity;
                        activeOBDAModel.getCurrentVocabulary().annotationProperties().remove(ap.getIRI());
                    }

                }
            }

            if (idx + 1 < changes.size() && change instanceof RemoveAxiom && changes.get(idx + 1) instanceof AddAxiom) {

                // Found the pattern of a renaming refactoring
                RemoveAxiom remove = (RemoveAxiom) change;
                AddAxiom add = (AddAxiom) changes.get(idx + 1);

                if (!(remove.getAxiom() instanceof OWLDeclarationAxiom && add.getAxiom() instanceof OWLDeclarationAxiom)) {
                    continue;
                }
                // Found the patter we are looking for, a remove and add of
                // declaration axioms
                OWLEntity olde = ((OWLDeclarationAxiom) remove.getAxiom()).getEntity();
                OWLEntity newe = ((OWLDeclarationAxiom) add.getAxiom()).getEntity();
                renamings.put(olde, newe);

            } else if (change instanceof RemoveAxiom && change.getAxiom() instanceof OWLDeclarationAxiom) {
                // Found the pattern of a deletion
                OWLDeclarationAxiom declaration = (OWLDeclarationAxiom) change.getAxiom();
                OWLEntity removedEntity = declaration.getEntity();

                if (removedEntity.getIRI().toQuotedString().equals("<http://www.unibz.it/inf/obdaplugin#RandomClass6677841155>")) {
                    //Hack this has been done just to trigger a change int the ontology
                    continue;
                }
                removals.add(removedEntity);
            }
        }

        // Applying the renaming to the OBDA model
        OBDAModel obdamodel = obdaModelManager.getActiveOBDAModel();
        for (OWLEntity olde : renamings.keySet()) {
            OWLEntity removedEntity = olde;
            OWLEntity newEntity = renamings.get(removedEntity);

            // This set of changes appears to be a "renaming" operation,
            // hence we will modify the OBDA model accordingly
            Predicate removedPredicate = getPredicate(removedEntity);
            Predicate newPredicate = getPredicate(newEntity);

            obdamodel.renamePredicate(removedPredicate, newPredicate);
        }

        // Applying the deletions to the obda model
        for (OWLEntity removede : removals) {
            Predicate removedPredicate = getPredicate(removede);
            obdamodel.deletePredicate(removedPredicate);
        }
    }

    private void updateOntologyID(SetOntologyID change) {
        // original ontology id
        OWLOntologyID originalOntologyID = change.getOriginalOntologyID();
        com.google.common.base.Optional<IRI> oldOntologyIRI = originalOntologyID.getOntologyIRI();

        URI oldiri = null;
        if (oldOntologyIRI.isPresent()) {
            oldiri = oldOntologyIRI.get().toURI();
        } else {
            oldiri = URI.create(originalOntologyID.toString());
        }

        log.debug("Ontology ID changed");
        log.debug("Old ID: {}", oldiri);

        // new ontology id
        OWLOntologyID newOntologyID = change.getNewOntologyID();
        Optional<IRI> optionalNewIRI = newOntologyID.getOntologyIRI();

        URI newiri = null;
        if (optionalNewIRI.isPresent()) {
            newiri = optionalNewIRI.get().toURI();
            obdaModelManager.getActiveOBDAModel().addPrefix(it.unibz.inf.ontop.spec.mapping.PrefixManager.DEFAULT_PREFIX,
                    OBDAModelManager.getProperPrefixURI(newiri.toString()));
        } else {
            newiri = URI.create(newOntologyID.toString());
            obdaModelManager.getActiveOBDAModel().addPrefix(PrefixManager.DEFAULT_PREFIX, "");
        }

        log.debug("New ID: {}", newiri);
    }

    private Predicate getPredicate(OWLEntity entity) {
        Predicate p = null;
        if (entity instanceof OWLClass) {
            /* We ignore TOP and BOTTOM (Thing and Nothing) */
            if (((OWLClass) entity).isOWLThing() || ((OWLClass) entity).isOWLNothing()) {
                return null;
            }
            String uri = entity.getIRI().toString();

            p = obdaModelManager.getAtomFactory().getClassPredicate(uri);
        } else if (entity instanceof OWLObjectProperty) {
            String uri = entity.getIRI().toString();

            p = obdaModelManager.getAtomFactory().getObjectPropertyPredicate(uri);
        } else if (entity instanceof OWLDataProperty) {
            String uri = entity.getIRI().toString();

            p = obdaModelManager.getAtomFactory().getDataPropertyPredicate(uri);

        } else if (entity instanceof OWLAnnotationProperty) {
            String uri = entity.getIRI().toString();

            p = obdaModelManager.getAtomFactory().getAnnotationPropertyPredicate(uri);
        }
        return p;
    }
}
