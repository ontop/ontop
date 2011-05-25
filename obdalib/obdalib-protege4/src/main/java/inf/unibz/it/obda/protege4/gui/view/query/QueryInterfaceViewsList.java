package inf.unibz.it.obda.protege4.gui.view.query;

import java.util.ArrayList;

import org.protege.editor.core.Disposable;

/***
 * This is a holder for all QueryView objects instantiated in protege. We keep
 * them to coordinate their query selectors.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class QueryInterfaceViewsList extends ArrayList<QueryInterfaceViewComponent> implements Disposable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7082548696764069555L;

	public void dispose() throws Exception {

	}

}
