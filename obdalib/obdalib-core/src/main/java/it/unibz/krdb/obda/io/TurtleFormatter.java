package it.unibz.krdb.obda.io;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URITemplatePredicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to write a conjunctive query in Turtle syntax
 */
public class TurtleFormatter extends CQFormatter {

    public TurtleFormatter(PrefixManager pm) {
        super(pm);
    }

    public String print(CQIE query) {
        TurtleContainer container = new TurtleContainer();
        List<Atom> body = query.getBody();
        for (Atom atom : body) {
            String subject, predicate, object = "";
            String predicateName = atom.getPredicate().toString();
            if (isUnary(atom)) {
                Term term = atom.getTerm(0);
                subject = getDisplayString(term);
                predicate = "a";
                object = getAbbreviatedName(predicateName, false);
            } else {
                Term term1 = atom.getTerm(0);
                Term term2 = atom.getTerm(1);
                subject = getDisplayString(term1);
                predicate = getAbbreviatedName(predicateName, false);
                object = getDisplayString(term2);
            }
            container.put(subject, predicate, object);
        }
        return container.print();
    }

    /**
     * Utility method to print the term.
     */
    private String getDisplayString(Term term) {
        if (term instanceof Function) {
            Function function = (Function) term;
            Predicate functionSymbol = function.getFunctionSymbol();
            if (functionSymbol instanceof DataTypePredicate) {
                if (isLiteralDataType(functionSymbol)) {
                    // For literal data type
                    final int arity = function.getArity();
                    if (arity == 1) { // without the language tag
                        Term var = function.getTerms().get(0);
                        return String.format("%s^^%s", TermUtil.toString(var), getAbbreviatedName(functionSymbol.toString(), false));
                    } else if (arity == 2) { // with the language tag
                        Term var = function.getTerms().get(0);
                        Term lang = function.getTerms().get(1);
                        return String.format("%s@%s", TermUtil.toString(var), lang.toString());
                    }
                } else {
                    // For the other data types
                    Term var = function.getTerms().get(0);
                    return String.format("%s^^%s", TermUtil.toString(var), getAbbreviatedName(functionSymbol.toString(), false));
                }
            } else if (functionSymbol instanceof URITemplatePredicate) {
                Term uriTemplateConstant = function.getTerms().get(0);
                String uriTemplate = getAbbreviatedName(TermUtil.toString(uriTemplateConstant), true);
                StringBuffer template = new StringBuffer(uriTemplate);
                int startIndex = 0;
                for (Term uriTemplateArg : function.getTerms()) {
                    if (uriTemplateArg instanceof Variable) {
                        int insertIndex = uriTemplate.indexOf("{", startIndex);
                        template.insert(insertIndex + 1, TermUtil.toString(uriTemplateArg));
                        startIndex = insertIndex + 1; // update the start index to find the next '{}' placeholder
                    }
                }
                return String.format("<%s>", template.toString());
            }
        }
        // Use the default writing
        return TermUtil.toString(term);
    }

    /**
     * Checks if the atom is unary or not.
     */
    private boolean isUnary(Atom atom) {
        return atom.getArity() == 1 ? true : false;
    }

    /**
     * Checks if the datatype is literal or not.
     */
    private boolean isLiteralDataType(Predicate predicate) {
        return predicate.equals(OBDAVocabulary.RDFS_LITERAL);
    }

    /**
     * Prints the short form of the predicate (by omitting the complete URI and
     * replacing it by a prefix name).
     */
    private String getAbbreviatedName(String uri, boolean insideQuotes) {
        String shortForm = prefixManager.getShortForm(uri, insideQuotes);
        if (shortForm.equals(uri) && !insideQuotes) { // cannot be shorten
            return String.format("<%s>", uri);
        }
        return shortForm;
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
