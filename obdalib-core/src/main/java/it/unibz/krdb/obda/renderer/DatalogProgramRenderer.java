package it.unibz.krdb.obda.renderer;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;

import java.util.Iterator;
import java.util.List;

/**
 * A utility class to render a Datalog Program object into its representational
 * string.
 */
public class DatalogProgramRenderer {

	/**
	 * Transforms the given <code>DatalogProgram</code> into a string
	 */
	public static String encode(DatalogProgram input) {
		List<CQIE> list = input.getRules();
		Iterator<CQIE> it =list.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()){
			CQIE q = it.next();
			if(sb.length()>0){
				sb.append("\n");
			}
			sb.append(q);
		}
		return sb.toString();
	}

	private DatalogProgramRenderer() {
		// Prevent initialization
	}
}
