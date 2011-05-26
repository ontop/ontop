package inf.unibz.it.obda.api.controller;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.obda.codec.XMLEncodable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class AssertionController<AssertionClass extends Assertion> implements XMLEncodable {

	private ArrayList<AssertionClass>					assertions	= new ArrayList<AssertionClass>();

	private ArrayList<AssertionControllerListener<AssertionClass>>	listeners	= new ArrayList<AssertionControllerListener<AssertionClass>>();
	
	public abstract AssertionController<AssertionClass> getInstance();
	
	public AssertionController(){
	}
	
	public Collection<AssertionClass> getAssertions() {
		return assertions;
	}

	/***************************************************************************
	 * Adds a new assertion to the controller. Posts an assertion add event to
	 * all the listeners of the controller.
	 * 
	 * @param a
	 */
	public void addAssertion(AssertionClass a) {
		if (assertions.contains(a)) {
			return;
		}
		assertions.add(a);
		fireAssertionAdded(a);
	}

	/***************************************************************************
	 * Removes an assertion based on .equals. Posts a assertion removed event
	 * for all the listeners.
	 * 
	 * @param a
	 */
	public void removeAssertion(AssertionClass a) {
		assertions.remove(a);
		fireAssertionRemoved(a);
	}
	
	public void clear() {
		assertions.clear();
	}

	public void addControllerListener(AssertionControllerListener<AssertionClass> listener) {
		listeners.add(listener);
	}

	public void removeControllerListener(AssertionControllerListener<AssertionClass> listener) {
		listeners.remove(listener);
	}

	/***************************************************************************
	 * Fires a assertion added event. Notice that add assertion already calls
	 * this method for you. Avoid calling this method yourself to keep listeners
	 * consistent.
	 * 
	 * @param assertion
	 */
	public void fireAssertionAdded(AssertionClass assertion) {
		for (Iterator<AssertionControllerListener<AssertionClass>> iterator = listeners.iterator(); iterator.hasNext();) {
			AssertionControllerListener<AssertionClass> listener = iterator.next();
			listener.assertionAdded(assertion);
		}
	}

	/***************************************************************************
	 * Fires a assertion removed event. Notice that remove assertion already
	 * calls this method for you. Avoid calling this method yourself to keep
	 * listeners consistent.
	 * 
	 * @param assertion
	 */
	public void fireAssertionRemoved(AssertionClass assertion) {
		for (Iterator<AssertionControllerListener<AssertionClass>> iterator = listeners.iterator(); iterator.hasNext();) {
			AssertionControllerListener<AssertionClass> listener = iterator.next();
			listener.assertionRemoved(assertion);
		}
	}

	/***************************************************************************
	 * Fires a assertion changed event. Notice that the update assertion method
	 * already calls this method for you. Avoid calling this method yourself to
	 * keep listeners consistent.
	 * 
	 * @param assertion
	 */
	public void fireAssertionChanged(AssertionClass oldAssertion, AssertionClass newAssertion) {
		for (Iterator<AssertionControllerListener<AssertionClass>> iterator = listeners.iterator(); iterator.hasNext();) {
			AssertionControllerListener<AssertionClass> listener = iterator.next();
			listener.assertionChanged(oldAssertion, newAssertion);
		}
	}
	
	public void fireAssertionsCleared() {
		for (Iterator<AssertionControllerListener<AssertionClass>> iterator = listeners.iterator(); iterator.hasNext();) {
			AssertionControllerListener<AssertionClass> listener = iterator.next();
			listener.assertionsCleared();
		}
	}
}
