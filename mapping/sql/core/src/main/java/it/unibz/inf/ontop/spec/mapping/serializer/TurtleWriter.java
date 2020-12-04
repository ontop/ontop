package it.unibz.inf.ontop.spec.mapping.serializer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

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

	private final Multimap<Optional<String>, String> graphToSubjects = ArrayListMultimap.create();
	private final HashMap<String, ArrayList<String>> subjectToPredicates = new HashMap<>();
	private final HashMap<String, LinkedHashSet<String>> predicateToObjects = new HashMap<>();

	/**
	 * Adding the subject, predicate, object and graph components to this container.
	 *
	 * @param subject of the quad/triple
	 * @param predicate of the quad/triple
	 * @param object of the quad/triple
	 * @param graph optional
	 */

	public void put(String subject, String predicate, String object, Optional<String> graph) {
		if (!graphToSubjects.containsEntry(graph, subject))
			graphToSubjects.put(graph, subject);

		if (predicate.equals("rdf:type"))
			predicate = "a";

		ArrayList<String> predicateList = subjectToPredicates.computeIfAbsent(
				subjectKeyOf(graph, subject),
				(k) -> new ArrayList<>());
		if (!predicateList.contains(predicate)) {
			if (predicate.equals("a")) {
				predicateList.add(0, predicate);
			} else {
				predicateList.add(predicate);
			}
		}

		LinkedHashSet<String> objectList = predicateToObjects.computeIfAbsent(
				predicateKeyOf(graph, subject, predicate),
				(k) -> new LinkedHashSet<>());
		objectList.add(object);
	}

	/**
	 * Prints the container.
	 *
	 * @return The Turtle short representation.
	 */
	String print() {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<Optional<String>, Collection<String>> e : graphToSubjects.asMap().entrySet()) {
			e.getKey().ifPresent(g -> sb.append(String.format("GRAPH %s { ", g)));

			sb.append(e.getValue().stream()
					.map(subject -> subject + " " +
							subjectToPredicates.get(subjectKeyOf(e.getKey(), subject)).stream()
							.map(predicate -> predicate + " " + String.join(" , ",
									predicateToObjects.get(predicateKeyOf(e.getKey(), subject, predicate))))
						.collect(Collectors.joining(" ; ")) + " . ")
					.collect(Collectors.joining("")));

			e.getKey().ifPresent(g -> sb.append("} "));
		}
		return sb.toString();
	}

	private static String subjectKeyOf(Optional<String> graph, String subject) {
		return graph.map(g -> subject + "_" + g).orElse(subject);
	}

	private static String predicateKeyOf(Optional<String> graph, String subject, String predicate) {
		return predicate + "_" + subjectKeyOf(graph, subject);
	}

}
