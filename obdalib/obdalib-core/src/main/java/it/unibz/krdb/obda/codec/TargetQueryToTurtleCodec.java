package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class TargetQueryToTurtleCodec extends ObjectToTextCodec<OBDAQuery> {

	private static final long serialVersionUID = 1L;

	/**
	 * The Prefix Manager to obtain the prefix name and its complete URI.
	 */
	private PrefixManager prefMan = apic.getPrefixManager();

	/**
	 * Default constructor.
	 * 
	 * @param apic
	 *            The OBDA model.
	 */
	public TargetQueryToTurtleCodec(OBDAModel apic) {
		super(apic);
	}

	@Override
	public String encode(OBDAQuery input) {
		if (!(input instanceof CQIE)) {
			return "";
		}
		TurtleContainer turtle = new TurtleContainer();
		List<Atom> body = ((CQIE) input).getBody();
		for (Atom atom : body) {
			String subject, predicate, object = "";
			String originalString = atom.getPredicate().toString();
			if (isUnary(atom)) {
				subject = getDisplayName(atom.getTerm(0));
				predicate = "a";
				object = getAbbreviatedName(originalString, false);
				if (originalString.equals(object))
					object = "<" + object + ">";
			} else {
				subject = getDisplayName(atom.getTerm(0));
				predicate = getAbbreviatedName(originalString, false);
				if (originalString.equals(predicate))
					predicate = "<" + predicate + ">";
				object = getDisplayName(atom.getTerm(1));
			}
			
			turtle.put(subject, predicate, object);
		}
		return turtle.print();
	}

	/**
	 * Checks if the atom is unary or not.
	 */
	private boolean isUnary(Atom atom) {
		return atom.getArity() == 1 ? true : false;
	}

	/**
	 * Prints the short form of the predicate (by omitting the complete URI and
	 * replacing it by a prefix name).
	 */
	private String getAbbreviatedName(String uri, boolean isLiteral) {
		return prefMan.getShortForm(uri, true, isLiteral);
	}

	/**
	 * Prints the text representation of different terms.
	 */
	private String getDisplayName(Term term) {
		StringBuffer sb = new StringBuffer();
		if (term instanceof FunctionalTermImpl) {
			FunctionalTermImpl function = (FunctionalTermImpl) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			String fname = getAbbreviatedName(functionSymbol.toString(), false);
			if (functionSymbol instanceof DataTypePredicate) {
				// if the function symbol is a data type predicate
				if (isLiteralDataType(functionSymbol)) {
					// if it is rdfs:Literal
					int arity = function.getArity();
					if (arity == 1) {
						// without the language tag
						Term var = function.getTerms().get(0);
						sb.append(getDisplayName(var));
						sb.append("^^rdfs:Literal");
					} else if (arity == 2) {
						// with the language tag
						Term var = function.getTerms().get(0);
						Term lang = function.getTerms().get(1);
						sb.append(getDisplayName(var));
						sb.append("@");
						sb.append(lang.toString());
					}
				} else {
					// for the other data types
					Term var = function.getTerms().get(0);
					sb.append(getDisplayName(var));
					sb.append("^^");
					sb.append(fname);
				}
			} else {
				if (fname.equals("quest:uri")) { // TODO: Make this as a BuildinPredicate
					String uriTemplate = function.getTerms().get(0).toString();
					
					// Shorten the URI if possible
					uriTemplate = getAbbreviatedName(uriTemplate, true);
					
					sb.append("<");
					sb.append("\"");					
					StringTokenizer st = new StringTokenizer(uriTemplate, "}", true);
					for (Term innerTerm : function.getTerms()) {
						if (innerTerm instanceof Variable) {
							while (st.hasMoreTokens()) {
								String token = st.nextToken();								
								if (token.equals("}")) {
									sb.append(getDisplayName(innerTerm));
									sb.append("}");
									break;
								} else {
									sb.append(token);
								}
							}
						}
					}					
					while (st.hasMoreTokens()) {
						// Append the rest of the tokens, if exist
						sb.append(st.nextToken());
					}
					sb.append("\"");
					sb.append(">");
				} else {
					// for any ordinary function symbol
					sb.append(fname);
					sb.append("(");
					boolean separator = false;
					for (Term innerTerm : function.getTerms()) {
						if (separator) {
							sb.append(", ");
						}
						sb.append(getDisplayName(innerTerm));
						separator = true;
					}
					sb.append(")");
				}
			}
		} else if (term instanceof Variable) {
			sb.append("$");
			sb.append(term.toString());
		} else if (term instanceof URIConstant) {
			String originalUri = term.toString();			
			String abbreviatedUri = getAbbreviatedName(originalUri, false); // Shorten the URI if possible
			
			if (!abbreviatedUri.equals(originalUri)) {
				sb.append(abbreviatedUri);
			} else {
				// If the URI can't be shorten then use the full URI within brackets
				sb.append("<");
				sb.append(originalUri);
				sb.append(">");
			}
		} else if (term instanceof ValueConstant) {
			sb.append("\"");
			sb.append(term.toString());
			sb.append("\"");
		}
		return sb.toString();
	}
	
	private boolean isLiteralDataType(Predicate predicate) {
		return predicate.equals(OBDAVocabulary.RDFS_LITERAL);
	}

	@Override
	public OBDAQuery decode(String input) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * A utility class to store the Turtle main components, i.e., subject,
	 * predicate and object. The data structure simulates a tree structure where
	 * the subjects are the roots, the predicates are the intermediate nodes and
	 * the objects are the leaves.
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
	 * This data structure helps in printing the short Turtle syntax by
	 * traversing the tree.
	 * 
	 * <pre>
	 * $s1 :p1 $o1; :p2 $o2, $o3 .
	 * </pre>
	 */
	class TurtleContainer {

		private HashMap<String, ArrayList<String>> subjectToPredicates = new HashMap<String, ArrayList<String>>();
		private HashMap<String, ArrayList<String>> predicateToObjects = new HashMap<String, ArrayList<String>>();

		TurtleContainer() { /* NO-OP */
		}

		/**
		 * Adding the subject, predicate and object components to this
		 * container.
		 * 
		 * @param subject
		 *            The subject term of the Atom.
		 * @param predicate
		 *            The Atom predicate.
		 * @param object
		 *            The object term of the Atom.
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
			StringBuffer sb = new StringBuffer();
			for (String subject : subjectToPredicates.keySet()) {
				sb.append(subject);
				sb.append(" ");
				boolean semiColonSeparator = false;
				for (String predicate : subjectToPredicates.get(subject)) {
					if (semiColonSeparator) {
						sb.append("; ");
					}
					sb.append(predicate);
					sb.append(" ");
					semiColonSeparator = true;

					boolean commaSeparator = false;
					for (String object : predicateToObjects.get(predicate)) {
						if (commaSeparator) {
							sb.append(", ");
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
}
