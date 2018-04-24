package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.term.functionsymbol.URITemplatePredicate;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDAParser.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.unibz.inf.ontop.model.IriConstants.RDF_TYPE;
import static it.unibz.inf.ontop.model.OntopModelSingletons.*;

public abstract class AbstractTurtleOBDAVisitor extends TurtleOBDABaseVisitor implements TurtleOBDAVisitor {

    // Column placeholder pattern
    private static final Pattern varPattern = Pattern.compile("\\{([^\\}]+)\\}");
    private static final Pattern constantBnodePattern = Pattern.compile("^_:(.*)");

    protected abstract boolean validateAttributeName(String value);

    /**
     * Map of directives
     */
    private HashMap<String, String> directives = new HashMap<>();

    /**
     * The current subject term
     */
    private Term currentSubject;

    protected String error = "";

    public String getError() {
        return error;
    }

    private String removeBrackets(String text) {
        return text.substring(1, text.length() - 1);
    }

    private Term typeTerm(String text, COL_TYPE col_type) {
        ValueConstant integerConstant = TERM_FACTORY.getConstantLiteral(text, COL_TYPE.LITERAL);
        return TERM_FACTORY.getTypedTerm(integerConstant, col_type);
    }

    protected Term constructIRI(String text) {
        Term toReturn = null;
        final String PLACEHOLDER = "{}";
        List<Term> terms = new LinkedList<>();
        List<FormatString> tokens = parseIRI(text);
        int size = tokens.size();
        if (size == 1) {
            FormatString token = tokens.get(0);
            if (token instanceof FixedString) {
                ValueConstant uriTemplate = TERM_FACTORY.getConstantLiteral(token.toString()); // a single URI template
                toReturn = TERM_FACTORY.getUriTemplate(uriTemplate);
            } else if (token instanceof ColumnString) {
                // a single URI template
                Variable column = TERM_FACTORY.getVariable(token.toString());
                toReturn = TERM_FACTORY.getUriTemplate(column);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (FormatString token : tokens) {
                if (token instanceof FixedString) { // if part of URI template
                    sb.append(token.toString());
                } else if (token instanceof ColumnString) {
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


    private List<FormatString> parseIRI(String text) {
        List<FormatString> toReturn = new ArrayList<>();
        Matcher m = varPattern.matcher(text);
        int i = 0;
        while (i < text.length()) {
            if (m.find(i)) {
                if (m.start() != i) {
                    toReturn.add(new FixedString(text.substring(i, m.start())));
                }
                String value = m.group(1);
                if(validateAttributeName(value)) {
                    toReturn.add(new ColumnString(value));
                    i = m.end();
                }
            } else {
                toReturn.add(new FixedString(text.substring(i)));
                break;
            }
        }
        return toReturn;
    }

    private Term constructConstantBNode(String text) {
        Matcher m = constantBnodePattern.matcher(text);
        return TERM_FACTORY.getConstantBNode(m.group(1));
    }

    private Term constructBnodeFunction(String text) {
        ImmutableList.Builder<ImmutableTerm> args = ImmutableList.builder();
        Matcher m = varPattern.matcher(text);
        while (m.find()) {
            args.add(TERM_FACTORY.getVariable(m.group(1)));
        }
        return TERM_FACTORY.getImmutableBNodeTemplate(args.build());
    }

    private interface FormatString {
        int index();

        String toString();
    }

    private class FixedString implements FormatString {
        private String s;

        FixedString(String s) {
            this.s = s;
        }

        @Override
        public int index() {
            return -1;
        }  // flag code for fixed string

        @Override
        public String toString() {
            return s;
        }
    }

    private class ColumnString implements FormatString {
        private String s;

        ColumnString(String s) {
            this.s = s;
        }

        @Override
        public int index() {
            return 0;
        }  // flag code for column string

        @Override
        public String toString() {
            return s;
        }
    }

    //this function distinguishes curly bracket with
    //back slash "\{" from curly bracket "{"
    private int getIndexOfCurlyB(String str) {
        int i;
        int j;
        i = str.indexOf("{");
        j = str.indexOf("\\{");
        while ((i - 1 == j) && (j != -1)) {
            i = str.indexOf("{", i + 1);
            j = str.indexOf("\\{", j + 1);
        }
        return i;
    }

    //in case of concat this function parses the literal
    //and adds parsed constant literals and template literal to terms list
    private ArrayList<Term> addToTermsList(String str) {
        ArrayList<Term> terms = new ArrayList<>();
        int i, j;
        String st;
        str = str.substring(1, str.length() - 1);
        while (str.contains("{")) {
            i = getIndexOfCurlyB(str);
            if (i > 0) {
                st = str.substring(0, i);
                st = st.replace("\\\\", "");
                terms.add(TERM_FACTORY.getConstantLiteral(st));
                str = str.substring(str.indexOf("{", i), str.length());
            } else if (i == 0) {
                j = str.indexOf("}");
                terms.add(TERM_FACTORY.getVariable(str.substring(1, j)));
                str = str.substring(j + 1, str.length());
            } else {
                break;
            }
        }
        if (!str.equals("")) {
            str = str.replace("\\\\", "");
            terms.add(TERM_FACTORY.getConstantLiteral(str));
        }
        return terms;
    }

    //this function returns nested concats
    //in case of more than two terms need to be concatted
    private Term getNestedConcat(String str) {
        ArrayList<Term> terms;
        terms = addToTermsList(str);
        if (terms.size() == 1) {
            return terms.get(0);
        }

        Function f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, terms.get(0), terms.get(1));
        for (int j = 2; j < terms.size(); j++) {
            f = TERM_FACTORY.getFunction(ExpressionOperation.CONCAT, f, terms.get(j));
        }
        return f;
    }

    /**
     * This methods construct an atom from a triple
     * <p>
     * For the input (subject, pred, object), the result is
     * <ul>
     * <li> object(subject), if pred == rdf:type and subject is grounded ; </li>
     * <li> predicate(subject, object), if pred != rdf:type and predicate is grounded ; </li>
     * <li> triple(subject, pred, object), otherwise (it is a higher order atom). </li>
     * </ul>
     */
    private Function makeAtom(Term subject, Term pred, Term object) {
        Function atom;

        if (isRDFType(pred)) {
            if (object instanceof Function) {
                if (QueryUtils.isGrounded(object)) {
                    ValueConstant c = ((ValueConstant) ((Function) object).getTerm(0));  // it has to be a URI constant
                    Predicate predicate = TERM_FACTORY.getClassPredicate(c.getValue());
                    atom = TERM_FACTORY.getFunction(predicate, subject);
                } else {
                    atom = ATOM_FACTORY.getTripleAtom(subject, pred, object);
                }
            } else if (object instanceof Variable) {
                Term uriOfPred = TERM_FACTORY.getUriTemplate(pred);
                Term uriOfObject = TERM_FACTORY.getUriTemplate(object);
                atom = ATOM_FACTORY.getTripleAtom(subject, uriOfPred, uriOfObject);
            } else {
                throw new IllegalArgumentException("parser cannot handle object " + object);
            }
        } else if (!QueryUtils.isGrounded(pred)) {
            atom = ATOM_FACTORY.getTripleAtom(subject, pred, object);
        } else {
            Predicate predicate;
            if (pred instanceof Function) {
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
                    } else {
                        throw new IllegalArgumentException("parser cannot handle object " + object);
                    }
                }
            } else {
                throw new IllegalArgumentException("predicate should be a URI Function");
            }
            atom = TERM_FACTORY.getFunction(predicate, subject, object);
        }
        return atom;
    }


    private static boolean isRDFType(Term pred) {
        if (pred instanceof Function && ((Function) pred).getTerm(0) instanceof Constant) {
            String c = ((Constant) ((Function) pred).getTerm(0)).getValue();
            return c.equals(RDF_TYPE);
        }
        return false;
    }

    private String concatPrefix(String prefixedName) {
        String[] tokens = prefixedName.split(":", 2);
        String uri = directives.get(tokens[0]);  // the first token is the prefix
        return uri + tokens[1];  // the second token is the local name
    }

    @Override
    public List<Function> visitParse(ParseContext ctx) {
        ctx.directiveStatement().forEach(this::visit);
        return ctx.triplesStatement().stream()
                .flatMap(c -> visitTriplesStatement(c).stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public Void visitDirectiveStatement(DirectiveStatementContext ctx) {
        visit(ctx.directive());
        return null;
    }

    @Override
    public Void visitDirective(DirectiveContext ctx) {
        visit(ctx.prefixID());
        return null;
    }

    @Override
    public List<Function> visitTriplesStatement(TriplesStatementContext ctx) {
        return visitTriples(ctx.triples());
    }

    @Override
    public Void visitPrefixID(PrefixIDContext ctx) {
        String iriref = removeBrackets(ctx.IRIREF().getText());
        String ns = ctx.PNAME_NS().getText();
        directives.put(ns.substring(0, ns.length() - 1), iriref); // remove the end colon
        return null;
    }

    @Override
    public Object visitBase(BaseContext ctx) {
        String iriRef = removeBrackets(ctx.IRIREF().getText());
        directives.put("", iriRef);
        return null;
    }

    @Override
    public List<Function> visitTriples(TriplesContext ctx) {
        currentSubject = visitSubject(ctx.subject());
        return visitPredicateObjectList(ctx.predicateObjectList());
    }

    @Override
    public List<Function> visitPredicateObjectList(PredicateObjectListContext ctx) {
        return ctx.predicateObject().stream()
                .flatMap(c -> visitPredicateObject(c).stream())
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public List<Function> visitPredicateObject(PredicateObjectContext ctx) {
        return visitObjectList(ctx.objectList()).stream()
                .map(t -> makeAtom(currentSubject, visitVerb(ctx.verb()), t))
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public Term visitVerb(VerbContext ctx) {
        ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        return TERM_FACTORY.getUriTemplate(TERM_FACTORY.getConstantLiteral(RDF_TYPE));
    }

    @Override
    public List<Term> visitObjectList(ObjectListContext ctx) {
        return ctx.object().stream()
                .map(this::visitObject)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public Term visitSubject(SubjectContext ctx) {
        ResourceContext rc = ctx.resource();
        if (rc != null) {
            return visitResource(rc);
        }
        VariableContext vc = ctx.variable();
        if (vc != null) {
            return visitVariable(vc);
        }
        BlankContext bc = ctx.blank();
        if (bc != null) {
            return visitBlank(bc);
        }
        return null;
    }

    @Override
    public Term visitObject(ObjectContext ctx) {
        return (Term) visit(ctx.children.iterator().next());
    }

    @Override
    public Term visitResource(ResourceContext ctx) {
        if (ctx.iriExt() != null) {
            return visitIriExt(ctx.iriExt());
        }
        return constructIRI(this.visitIri(ctx.iri()));
    }

    public Term visitIriExt(IriExtContext ctx) {
        if (ctx.IRIREF_EXT() != null) {
            return constructIRI(removeBrackets(ctx.IRIREF_EXT().getText()));
        }
        return constructIRI(concatPrefix(ctx.PREFIXED_NAME_EXT().getText()));
    }

    @Override
    public Function visitVariableLiteral_1(VariableLiteral_1Context ctx) {
        return TERM_FACTORY.getTypedTerm(visitVariable(ctx.variable()), visitLanguageTag(ctx.languageTag()));
    }

    @Override
    public Function visitVariableLiteral_2(VariableLiteral_2Context ctx) {
        Variable var = visitVariable(ctx.variable());
        String iri = visitIri(ctx.iri());
        Optional<COL_TYPE> type = TYPE_FACTORY.getDatatype(iri);
        if (type.isPresent()) {
            return TERM_FACTORY.getTypedTerm(var, type.get());
        }
        throw new RuntimeException("ERROR. A mapping involves an unsupported datatype. \nOffending datatype:" + iri);
    }

    @Override
    public String visitIri(IriContext ctx) {
        TerminalNode token = ctx.PREFIXED_NAME();
        if (token != null) {
            return concatPrefix(token.getText());
        }
        return removeBrackets(ctx.IRIREF().getText());
    }

    @Override
    public Variable visitVariable(VariableContext ctx) {
        String variableName = removeBrackets(ctx.STRING_WITH_CURLY_BRACKET().getText());
        validateAttributeName(variableName);
        return TERM_FACTORY.getVariable(variableName);
    }

    @Override
    public Term visitBlank(BlankContext ctx) {
        if (ctx.BLANK_NODE_FUNCTION() != null){
            return constructBnodeFunction(ctx.BLANK_NODE_FUNCTION().getText());
        }
        if (ctx.BLANK_NODE_LABEL() != null){
            return constructConstantBNode (ctx.BLANK_NODE_LABEL().getText());
        }
        throw new IllegalArgumentException("Anonymous blank nodes not supported yet in mapping targets");
    }

    @Override
    public Term visitLanguageTag(LanguageTagContext ctx) {
        VariableContext vc = ctx.variable();
        if (vc != null) {
            return visitVariable(vc);
        }
        return TERM_FACTORY.getConstantLiteral(ctx.LANGTAG().getText().substring(1).toLowerCase(), COL_TYPE.STRING);
    }

    @Override
    public Term visitLiteral(LiteralContext ctx) {
        StringLiteralContext slc = ctx.stringLiteral();
        if (slc != null) {
            Term literal = visitStringLiteral(slc);
            LanguageTagContext lc = ctx.languageTag();
            //if variable we cannot assign a datatype yet
            if (literal instanceof Variable) {
                return TERM_FACTORY.getTypedTerm(literal, COL_TYPE.STRING);
            }
            if (lc != null) {
                return TERM_FACTORY.getTypedTerm(literal, visitLanguageTag(lc));
            }
            return TERM_FACTORY.getTypedTerm(literal, COL_TYPE.STRING);
        }
        return (Term) visitChildren(ctx);
    }

    @Override
    public Term visitStringLiteral(StringLiteralContext ctx) {
        String str = ctx.STRING_LITERAL_QUOTE().getText();
        if (str.contains("{")) {
            return getNestedConcat(str);
        }
        return TERM_FACTORY.getConstantLiteral(str.substring(1, str.length() - 1), COL_TYPE.STRING); // without the double quotes
    }

    @Override
    public Term visitTypedLiteral(TypedLiteralContext ctx) {
        Term stringValue = visitStringLiteral(ctx.stringLiteral());
        String iriRef = visitIri(ctx.iri());
        Optional<COL_TYPE> type = TYPE_FACTORY.getDatatype(iriRef);
        if (type.isPresent()) {
            return TERM_FACTORY.getTypedTerm(stringValue, type.get());
        }
        throw new RuntimeException("Unsupported datatype: " + iriRef);
    }

    @Override
    public Term visitNumericLiteral(NumericLiteralContext ctx) {
        return (Term) visitChildren(ctx);
    }

    @Override
    public Term visitBooleanLiteral(BooleanLiteralContext ctx) {
        return typeTerm(ctx.BOOLEAN_LITERAL().getText(), COL_TYPE.BOOLEAN);
    }

    @Override
    public Term visitNumericUnsigned(NumericUnsignedContext ctx) {

        TerminalNode token = ctx.INTEGER();
        if (token != null) {
            return typeTerm(token.getText(), COL_TYPE.INTEGER);
        }
        token = ctx.DOUBLE();
        if (token != null) {
            return typeTerm(token.getText(), COL_TYPE.DOUBLE);
        }
        return typeTerm(ctx.DECIMAL().getText(), COL_TYPE.DECIMAL);
    }

    @Override
    public Term visitNumericPositive(NumericPositiveContext ctx) {
        TerminalNode token = ctx.INTEGER_POSITIVE();
        if (token != null) {
            return typeTerm(token.getText(), COL_TYPE.INTEGER);
        }
        token = ctx.DOUBLE_POSITIVE();
        if (token != null) {
            return typeTerm(token.getText(), COL_TYPE.DOUBLE);
        }
        return typeTerm(ctx.DECIMAL_POSITIVE().getText(), COL_TYPE.DECIMAL);
    }

    @Override
    public Term visitNumericNegative(NumericNegativeContext ctx) {
        TerminalNode token = ctx.INTEGER_NEGATIVE();
        if (token != null) {
            return typeTerm(token.getText(), COL_TYPE.INTEGER);
        }
        token = ctx.DOUBLE_NEGATIVE();
        if (token != null) {
            return typeTerm(token.getText(), COL_TYPE.DOUBLE);
        }
        return typeTerm(ctx.DECIMAL_NEGATIVE().getText(), COL_TYPE.DECIMAL);
    }
}
