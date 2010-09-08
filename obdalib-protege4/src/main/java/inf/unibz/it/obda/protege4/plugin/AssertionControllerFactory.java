package inf.unibz.it.obda.protege4.plugin;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.AssertionController;

/***
 * 
 * A factory for a specific type of assertion controller. Base for
 * AssertionControllerFactory Plugin
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public abstract class AssertionControllerFactory<T extends Assertion> {

	/***
	 * Returns a fresh instances of an Assertion Controller
	 * 
	 * @param <T>
	 * @return
	 */
	public abstract AssertionController<?> getControllerInstance();

	/***
	 * Gets the class of the assertions that this controller handles. It serves
	 * to identify the AssertionController
	 * 
	 * @return
	 */
	public abstract Class<?> getAssertionClass();

	/***
	 * Returns the XMLCodec that should be used by the data manger to code and
	 * decode the assertions that this assertion controller handles.
	 * 
	 * @param <T>
	 * @return
	 */
	public abstract AssertionXMLCodec<?> getXMLCodec();

	/***
	 * Indicates if this controller should trigger an "OntologyChanged" even in
	 * case it rises any "modified" related events.
	 * 
	 * @return
	 */
	public abstract boolean triggersOntologyChanged();

}
