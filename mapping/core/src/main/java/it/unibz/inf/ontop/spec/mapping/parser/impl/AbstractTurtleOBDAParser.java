package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.model.term.Function;
import it.unibz.inf.ontop.model.term.Term;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenStream;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.model.IriConstants.RDF_TYPE;
import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

/**
 * Extended by the class generated with antlr.
 * Use the "superclass" option in the .g file in order to extend this class
 */
public abstract class AbstractTurtleOBDAParser extends Parser {

    /** Map of directives */
    protected HashMap<String, String> directives = new HashMap<String, String>();

    /** The current subject term */
    protected Term currentSubject;

    /** All variables */
    protected Set<Term> variableSet = new HashSet<Term>();

    protected String error = "";

    public AbstractTurtleOBDAParser(TokenStream input) {
        super(input);
    }

    public String getError() {
        return error;
    }


    public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
        throw e;
    }

    protected String removeBrackets(String text) {
        return text.substring(1, text.length()-1);
    }

    protected Term construct(String text) {
        Term toReturn = null;
        final String PLACEHOLDER = "{}";
        List<Term> terms = new LinkedList<Term>();
        List<FormatString> tokens = parse(text);
        int size = tokens.size();
        if (size == 1) {
            FormatString token = tokens.get(0);
            if (token instanceof FixedString) {
                ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(token.toString()); // a single URI template
                toReturn = TERM_FACTORY.getUriTemplate(uriTemplate);
            }
            else if (token instanceof ColumnString) {
                // a single URI template
                Variable column = TERM_FACTORY.getVariable(token.toString());
                toReturn = TERM_FACTORY.getUriTemplate(column);
            }
        }
        else {
            StringBuilder sb = new StringBuilder();
            for(FormatString token : tokens) {
                if (token instanceof FixedString) { // if part of URI template
                    sb.append(token.toString());
                }
                else if (token instanceof ColumnString) {
                    sb.append(PLACEHOLDER);
                    Variable column = TERM_FACTORY.getVariable(token.toString());
                    terms.add(column);
                }
            }
            ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(sb.toString()); // complete URI template
            terms.add(0, uriTemplate);
            toReturn = TERM_FACTORY.getUriTemplate(terms);
        }
        return toReturn;
    }

    // Column placeholder pattern
    private static final String formatSpecifier = "\\{([^\\}]+)?\\}";
    private static Pattern chPattern = Pattern.compile(formatSpecifier);

    private List<FormatString> parse(String text) {
        List<FormatString> toReturn = new ArrayList<FormatString>();
        Matcher m = chPattern.matcher(text);
        int i = 0;
        while (i < text.length()) {
            if (m.find(i)) {
                if (m.start() != i) {
                    toReturn.add(new FixedString(text.substring(i, m.start())));
                }
                String value = m.group(1);
                if(value.contains(".")){
                    throw new IllegalArgumentException("Fully qualified columns are not accepted.");
                }
                toReturn.add(new ColumnString(value));
                i = m.end();
            }
            else {
                toReturn.add(new FixedString(text.substring(i)));
                break;
            }
        }
        return toReturn;
    }

    private interface FormatString {
        int index();
        String toString();
    }

    private class FixedString implements FormatString {
        private String s;
        FixedString(String s) { this.s = s; }
        @Override public int index() { return -1; }  // flag code for fixed string
        @Override public String toString() { return s; }
    }

    private class ColumnString implements FormatString {
        private String s;
        ColumnString(String s) { this.s = s; }
        @Override public int index() { return 0; }  // flag code for column string
        @Override public String toString() { return s; }
    }

    //this function distinguishes curly bracket with
    //back slash "\{" from curly bracket "{"
    private int getIndexOfCurlyB(String str){
        int i;
        int j;
        i = str.indexOf("{");
        j = str.indexOf("\\{");
        while((i-1 == j) &&(j != -1)){
            i = str.indexOf("{",i+1);
            j = str.indexOf("\\{",j+1);
        }
        return i;
    }

    //in case of concat this function parses the literal
    //and adds parsed constant literals and template literal to terms list
    private ArrayList<Term> addToTermsList(String str){
        ArrayList<Term> terms = new ArrayList<Term>();
        int i,j;
        String st;
        str = str.substring(1, str.length()-1);
        while(str.contains("{")){
            i = getIndexOfCurlyB(str);
            if (i > 0){
                st = str.substring(0,i);
                st = st.replace("\\\\", "");
                terms.add(TERM_FACTORY.getConstantLiteral(st));
                str = str.substring(str.indexOf("{", i), str.length());
            }else if (i == 0){
                j = str.indexOf("}");
                terms.add(TERM_FACTORY.getVariable(str.substring(1,j)));
                str = str.substring(j+1,str.length());
            } else {
                break;
            }
        }
        if(!str.equals("")){
            str = str.replace("\\\\", "");
            terms.add(TERM_FACTORY.getConstantLiteral(str));
        }
        return terms;
    }

    //this function returns nested concats
    //in case of more than two terms need to be concatted
    protected Term getNestedConcat(String str){
        ArrayList<Term> terms = new ArrayList<Term>();
        terms = addToTermsList(str);
        if(terms.size() == 1){
            Variable v = (Variable) terms.get(0);
            variableSet.add(v);
            return v;
        }

        Function f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, terms.get(0), terms.get(1));
        for(int j=2;j<terms.size();j++) {
            f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, f, terms.get(j));
        }
        return f;
    }

    /**
     * This methods construct an atom from a triple
     *
     * For the input (subject, pred, object), the result is
     * <ul>
     *  <li> object(subject), if pred == rdf:type and subject is grounded ; </li>
     *  <li> predicate(subject, object), if pred != rdf:type and predicate is grounded ; </li>
     *  <li> triple(subject, pred, object), otherwise (it is a higher order atom). </li>
     * </ul>
     */
    protected Function makeAtom(Term subject, Term pred, Term object) {
        Function atom = null;

        if (isRDFType(pred)) {
            if (object instanceof  Function) {
                if(QueryUtils.isGrounded(object)) {
                    ValueConstant c = ((ValueConstant) ((Function) object).getTerm(0));  // it has to be a URI constant
                    Predicate predicate = TERM_FACTORY.getClassPredicate(c.getValue());
                    atom = TERM_FACTORY.getFunction(predicate, subject);
                } else {
                    atom = ATOM_FACTORY.getTripleAtom(subject, pred, object);
                }
            }
            else if (object instanceof Variable){
                Term uriOfPred = TERM_FACTORY.getUriTemplate(pred);
                Term uriOfObject = TERM_FACTORY.getUriTemplate(object);
                atom = ATOM_FACTORY.getTripleAtom(subject, uriOfPred,  uriOfObject);
            }
            else {
                throw new IllegalArgumentException("parser cannot handle object " + object);
            }
        } else if( ! QueryUtils.isGrounded(pred) ){
            atom = ATOM_FACTORY.getTripleAtom(subject, pred,  object);
        } else {
            //Predicate predicate = TERM_FACTORY.getPredicate(pred.toString(), 2); // the data type cannot be determined here!
            Predicate predicate;
            if(pred instanceof Function) {
                ValueConstant pr = (ValueConstant) ((Function) pred).getTerm(0);
                if (object instanceof Variable) {
                    predicate = TERM_FACTORY.getDataPropertyPredicate(pr.getValue());
                } else {
                    if (object instanceof Function) {
                        if (((Function) object).getFunctionSymbol() instanceof URITemplatePredicate) {

                            predicate = TERM_FACTORY.getObjectPropertyPredicate(pr.getValue());
                        } else {
                            predicate = TERM_FACTORY.getDataPropertyPredicate(pr.getValue());
                        }
                    }
                    else {
                        throw new IllegalArgumentException("parser cannot handle object " + object);
                    }
                }
            }else {
                throw new IllegalArgumentException("predicate should be a URI Function");
            }
            atom = TERM_FACTORY.getFunction(predicate, subject, object);
        }
        return atom;
    }


    private static boolean isRDFType(Term pred) {
//		if (pred instanceof Constant && ((Constant) pred).getValue().equals(RDF_TYPE)) {
//			return true;
//		}
        if (pred instanceof Function && ((Function) pred).getTerm(0) instanceof Constant) {
            String c= ((Constant) ((Function) pred).getTerm(0)).getValue();
            return c.equals(RDF_TYPE);
        }
        return false;
    }
}
