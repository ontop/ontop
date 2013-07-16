package it.unibz.krdb.obda.renderer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A utility class to store the Turtle main components, i.e., subject, predicate
 * and object. The data structure simulates a tree structure where the subjects
 * are the roots, the predicates are the intermediate nodes and the objects are
 * the leaves.
 * <p>
 * An example:
 * 
 * <pre>
 *   $s1 :p1 $o1
 *   $s1 :p2 $o2
 *   $s1 :p2 $o3
 * </pre>
 * 
 * The example is stored to the TurtleContainer as shown below.
 * 
 * <pre>
 *         :p1 - $o1    
 *        / 
 *   $s1 <      $o2
 *        :p2 <
 *              $o3
 * </pre>
 * <p>
 * This data structure helps in printing the short Turtle syntax by traversing
 * the tree.
 * 
 * <pre>
 * $s1 :p1 $o1; :p2 $o2, $o3 .
 * </pre>
 */
class TurtleWriter {

	private HashMap<String, ArrayList<String>> subjectToPredicates = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> predicateToObjects = new HashMap<String, ArrayList<String>>();

	/**
	 * Adding the subject, predicate and object components to this container.
	 * 
	 * @param subject
	 *            The subject term of the Function.
	 * @param predicate
	 *            The Function predicate.
	 * @param object
	 *            The object term of the Function.
	 */
	void put(String subject, String predicate, String object) {
		// Subject to Predicates map
		ArrayList<String> predicateList = subjectToPredicates.get(subject);
		if (predicateList == null) {
			predicateList = new ArrayList<String>();
		}
		insert(predicateList, predicate);
		subjectToPredicates.put(subject, predicateList);

		// Predicate to Objects map
		ArrayList<String> objectList = predicateToObjects.get(predicate);
		if (objectList == null) {
			objectList = new ArrayList<String>();
		}
		objectList.add(object);
		predicateToObjects.put(predicate, objectList);
	}

	// Utility method to insert the predicate
	private void insert(ArrayList<String> list, String input) {
		if (!list.contains(input)) {
			if (input.equals("a") || input.equals("rdf:type")) {
				list.add(0, input);
			} else {
				list.add(input);
			}
		}
	}

	/**
	 * Prints the container.
	 * 
	 * @return The Turtle short representation.
	 */
	String print() {
		StringBuilder sb = new StringBuilder();
		for (String subject : subjectToPredicates.keySet()) {
			sb.append(subject);
			sb.append(" ");
			boolean semiColonSeparator = false;
			for (String predicate : subjectToPredicates.get(subject)) {
				if (semiColonSeparator) {
					sb.append(" ; ");
				}
				sb.append(predicate);
				sb.append(" ");
				semiColonSeparator = true;

				boolean commaSeparator = false;
				for (String object : predicateToObjects.get(predicate)) {
					if (commaSeparator) {
						sb.append(" , ");
					}
					sb.append(object);
					commaSeparator = true;
				}
			}
			sb.append(" ");
			sb.append(".");
			sb.append(" ");
		}
		return sb.toString();
	}
}