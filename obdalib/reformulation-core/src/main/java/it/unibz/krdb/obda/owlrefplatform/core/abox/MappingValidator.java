package it.unibz.krdb.obda.owlrefplatform.core.abox;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingValidator
{
  /** The source ontology for validating the target query */
  private OWLOntology ontology;

  /** List of invalid predicates */
  private Vector<String> invalidPredicates;

  /** List of invalid mappings-predicates */
  private HashMap<String, Vector<String>> invalidMappings = new HashMap<String, Vector<String>>();

  /** Logger */
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  public MappingValidator(OWLOntology ontology) {
    this.ontology = ontology;
  }

  public boolean validate(List<OBDAMappingAxiom> mappings) {

    boolean isValid = true; // We are optimistic!

    for (OBDAMappingAxiom mapping : mappings) {
      // Get the target query from the mapping.
      final String mappingId = mapping.getId();

      log.debug("Checking mapping " + mappingId + "...");
      CQIE targetQuery = (CQIE)mapping.getTargetQuery();
      isValid = validate(targetQuery);

      if(!isValid) {
        invalidMappings.put(mappingId, invalidPredicates);
      }
    }
    return isValid;
  }

  public boolean validate(CQIE targetQuery) {
    // Create a new invalid list
    invalidPredicates = new Vector<String>();

    Iterator<Atom> iterAtom = targetQuery.getBody().iterator();

    while(iterAtom.hasNext()) {
      Atom atom = iterAtom.next();

      URI predicate = atom.getPredicate().getName();

      // TODO Add a predicate type for better identification.
      boolean isClass = ontology.containsClassReference(predicate);
      boolean isObjectProp = ontology.containsObjectPropertyReference(predicate);
      boolean isDataProp = ontology.containsDataPropertyReference(predicate);

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

    boolean isValid = true;
    if(!invalidPredicates.isEmpty()) {
      isValid = false; // if the list is not empty means the string is invalid!
    }
    return isValid;
  }

  /**
   * Use this method after the validate(CQIE) method has been called. If
   * you use the method validate(List<OBDAMappingAxiom>) then the predicates
   * in the last invalid mapping are returned.
   *
   * @return Returns a vector of invalid predicate names.
   */
  public Vector<String> getListOfInvalidPredicates() {
    if (invalidPredicates == null) {
      return new Vector<String>();  // returns an empty vector.
    }
    return invalidPredicates;
  }

  /**
   * Use this method after the validate(List<OBDAMappingAxiom>) method has
   * been called.
   *
   * @return Returns a map of invalid mappings with the corresponding wrong
   * predicates.
   */
  public HashMap<String, Vector<String>> getListOfInvalidMappings() {
    return invalidMappings;
  }
}
