package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

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

	private Multimap<Optional<String>, String> graphToSubjects = ArrayListMultimap.create();
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
		Optional<String> graph = Optional.empty();
		if (!graphToSubjects.containsEntry(graph, subject))
			graphToSubjects.put(graph, subject);
		insertSPO(subject, predicate, object);
	}

	void insertSPO(String subjectKey, String predicate, String object) {
		// Subject to Predicates map
		ArrayList<String> predicateList = subjectToPredicates.get(subjectKey);
		if (predicateList == null) {
			predicateList = new ArrayList<String>();
		}
		insert(predicateList, predicate);
		subjectToPredicates.put(subjectKey, predicateList);

		String predicateKey = computePredicateKey(subjectKey, predicate);

		// Predicate to Objects map
		ArrayList<String> objectList = predicateToObjects.get(predicateKey); // predicate that appears in 2 different subjects should not have all objects assigned  to both subjects
		if (objectList == null) {
			objectList = new ArrayList<String>();
		}
		if (!objectList.contains(object))
			objectList.add(object);
		predicateToObjects.put(predicateKey, objectList);
	}

	public void put(String subject, String predicate, String object, String graph) {
		Optional<String> optionalGraph = Optional.of(graph);
		if (!graphToSubjects.containsEntry(optionalGraph, subject))
			graphToSubjects.put(optionalGraph, subject);
		insertSPO(computeSubjectKey(graph, subject), predicate, object);
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

		for (Optional<String> graph : graphToSubjects.keySet()) {
			graph.ifPresent(g -> sb.append(String.format("GRAPH %s { ", g)));

			for (String subject : graphToSubjects.get(graph)) {
				String subjectKey = graph
						.map(g -> computeSubjectKey(g, subject))
						.orElse(subject);
				sb.append(subject);
				sb.append(" ");
				boolean semiColonSeparator = false;
				for (String predicate : subjectToPredicates.get(subjectKey)) {
					if (semiColonSeparator) {
						sb.append(" ; ");
					}
					sb.append(predicate);
					sb.append(" ");
					semiColonSeparator = true;

					boolean commaSeparator = false;
					for (String object : predicateToObjects.get(computePredicateKey(subjectKey, predicate))) {
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
			graph.ifPresent(g -> sb.append("} "));
		}
		return sb.toString();
	}

	private String computeSubjectKey(String graph, String subject) {
		return subject + "_" + graph;
	}

	private String computePredicateKey(String subjectKey, String predicate) {
		return predicate+ "_" + subjectKey;
	}

}
