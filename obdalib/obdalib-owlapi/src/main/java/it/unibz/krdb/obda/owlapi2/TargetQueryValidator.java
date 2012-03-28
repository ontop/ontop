package it.unibz.krdb.obda.owlapi2;

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.Iterator;
import java.util.Vector;

import org.semanticweb.owl.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetQueryValidator implements TargetQueryVocabularyValidator
{
  /** The source ontology for validating the target query */
  private OWLOntology ontology;
  
  /** Data factory **/
  private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();
  
  /** List of invalid predicates */
  private Vector<String> invalidPredicates = new Vector<String>();
  
  /** Logger */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  
  public TargetQueryValidator(OWLOntology ontology) {
    this.ontology = ontology;
  }
  
  /* (non-Javadoc)
 * @see it.unibz.krdb.obda.owlapi2.TargetQueryVocabularyValidator#validate(it.unibz.krdb.obda.model.CQIE)
 */
@Override
public boolean validate(CQIE targetQuery) {
    // Reset the invalid list
    invalidPredicates.clear();
    
    // Get the predicates in the target query.
    Iterator<Atom> iterAtom = targetQuery.getBody().iterator();
    while(iterAtom.hasNext()) {
    	
      Atom atom = iterAtom.next();
	      
      URI predicateUri = atom.getPredicate().getName();
 
      // TODO Add a predicate type for better identification.
      boolean isClass = ontology.containsClassReference(predicateUri);      
      boolean isObjectProp = ontology.containsObjectPropertyReference(predicateUri);
      boolean isDataProp = ontology.containsDataPropertyReference(predicateUri);
      
      // Check if the predicate contains in the ontology vocabulary as one
      // of these components (i.e., class, object property, data property).
      boolean isPredicateValid = isClass || isObjectProp || isDataProp;
      
      String debugMsg = "The predicate: [" + predicateUri.toString() + "]";
      if (isPredicateValid) {
        COL_TYPE colType[] = null; 
    	if (isClass) {
    	  colType = new COL_TYPE[]{COL_TYPE.OBJECT};
          debugMsg += " is a Class.";
        }
        else if (isObjectProp) {
          colType =  new COL_TYPE[]{COL_TYPE.OBJECT, COL_TYPE.OBJECT};
          debugMsg += " is an Object property.";
        }
        else if (isDataProp) {
          colType =  new COL_TYPE[]{COL_TYPE.OBJECT, COL_TYPE.LITERAL};
          debugMsg += " is a Data property.";
        }
    	Predicate predicate = dataFactory.getPredicate(predicateUri, atom.getArity(), colType);
    	atom.setPredicate(predicate);  // TODO Fix the API!
        log.debug(debugMsg);
      }
      else {
        invalidPredicates.add(predicateUri.toString());
        log.warn("WARNING: " + debugMsg + " is missing in the ontology!");
      }
    }
    
    boolean isValid = true;
    if(!invalidPredicates.isEmpty()) {
      isValid = false; // if the list is not empty means the string is invalid!
    }
    return isValid;
  }
  
  /* (non-Javadoc)
 * @see it.unibz.krdb.obda.owlapi2.TargetQueryVocabularyValidator#getInvalidPredicates()
 */
@Override
public Vector<String> getInvalidPredicates() {
    return invalidPredicates;
  }

@Override
public boolean isClass(URI predicate) {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean isObjectProperty(URI predicate) {
	// TODO Auto-generated method stub
	return false;
}

@Override
public boolean isDataProperty(URI predicate) {
	// TODO Auto-generated method stub
	return false;
}
}
