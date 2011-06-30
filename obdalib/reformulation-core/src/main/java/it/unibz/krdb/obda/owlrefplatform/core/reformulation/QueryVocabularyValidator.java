package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.PredicateAtom;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryVocabularyValidator
{
  /** The source ontology for validating the target query */
  private Collection<OWLOntology> ontologies;

  /** List of invalid predicates */
  private Vector<String> invalidPredicates = new Vector<String>();

  Logger log = LoggerFactory.getLogger(QueryVocabularyValidator.class);

  public QueryVocabularyValidator(Collection<OWLOntology> ontologies) {
    this.ontologies = ontologies;
  }

  public boolean validate(DatalogProgram input) {
    // Reset the invalid list
    invalidPredicates.clear();

    List<CQIE> rules = input.getRules();
    for (CQIE query : rules) {
      validate(query);
    }

    boolean isValid = true;
    if(!invalidPredicates.isEmpty()) {
      isValid = false; // if the list is not empty means the string is invalid!
    }
    return isValid;
  }

  private void validate(CQIE query) {
    // Get the predicates in the target query.
    Iterator<Atom> iterAtom = query.getBody().iterator();
    while(iterAtom.hasNext()) {
      Atom a1 = iterAtom.next();
      if (!(a1 instanceof PredicateAtom)) {
        continue;
      }
      PredicateAtom atom = (PredicateAtom)a1;

      URI predicate = atom.getPredicate().getName();

      // TODO Add a predicate type for better identification.
      
      boolean isClass = false;
      boolean isObjectProp = false;
      boolean isDataProp = false;
      
      for (OWLOntology ontology: ontologies) {
    	  isClass = isClass || ontology.containsClassReference(predicate);
          isObjectProp = isObjectProp || ontology.containsObjectPropertyReference(predicate);
          isDataProp = isDataProp || ontology.containsDataPropertyReference(predicate);
      }
      

      // Check if the predicate contains in the ontology vocabulary as one
      // of these components (i.e., class, object property, data property).
      boolean isPredicateValid = isClass || isObjectProp || isDataProp;

      String debugMsg = "The predicate: [" + predicate.toString() + "]";
      if (isPredicateValid) {
        if (isClass) {
          debugMsg += " is a Class.";
        }
        else if (isObjectProp) {
          debugMsg += " is an Object property.";
        }
        else if (isDataProp) {
          debugMsg += " is a Data property.";
        }
        log.debug(debugMsg);
      }
      else {
        invalidPredicates.add(predicate.toString());
        log.warn("WARNING: " + debugMsg + " is missing in the ontology!");
      }
    }
  }

  public Vector<String> getInvalidPredicates() {
    return invalidPredicates;
  }
}
