package it.unibz.krdb.obda.renderer;

import it.unibz.krdb.obda.model.OBDAQuery;

/**
 * A utility class to render a Source Query object into its representational
 * string.
 */
public class SourceQueryRenderer {
	
	/**
	 * Transforms the given <code>OBDAQuery</code> into a string.
	 */
	public static String encode(OBDAQuery input) {
		if (input == null) {
			return "";
		}
		return input.toString();
	}
	
	private SourceQueryRenderer() {
		// Prevent initialization
	}
}
