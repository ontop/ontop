package it.unibz.inf.ontop.owlrefplatform.core.translator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.*;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.inf.ontop.parser.EncodeForURI;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.*;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/***
 * Translate a SPARQL algebra expression into a Datalog program that has the
 * same semantics. We use the built-in predicates Join and LeftJoin.
 * 
 * @author Roman Kontchakov
 */
public class SparqlAlgebraToDatalogTranslator {

	
	private static final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private static final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

    private static final Logger log = LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class);

	private final UriTemplateMatcher uriTemplateMatcher;
	private final SemanticIndexURIMap uriRef;

    private final DatalogProgram program;
    private int predicateIdx = 0;

    /**
	 * 
	 * @param uriTemplateMatcher matches URIs to templates (comes from mappings)
	 * @param uriRef maps URIs to their integer identifiers (used only in the Semantic Index mode)
	 */

	public SparqlAlgebraToDatalogTranslator(UriTemplateMatcher uriTemplateMatcher, SemanticIndexURIMap uriRef) {
		this.uriTemplateMatcher = uriTemplateMatcher;
		this.uriRef = uriRef;

        this.program = ofac.getDatalogProgram();
    }

	/**
	 * Translate a given SPARQL query object to datalog program.
     *
     * IMPORTANT: this method should be called only once on each instance of the class
	 *
	 * @pq     SPARQL query object
	 * @return our representation of the SPARQL query (as a datalog program)
	 */
	public SparqlQuery translate(ParsedQuery pq) {

        if (predicateIdx != 0 || !program.getRules().isEmpty())
            throw new RuntimeException("SparqlAlgebraToDatalogTranslator.translate can only be called once.");

		TupleExpr te = pq.getTupleExpr();
		log.debug("SPARQL algebra: \n{}", te);
        //System.out.println("SPARQL algebra: \n" + te);

        TranslationResult body = translate(te);

        List<Term> answerVariables;
		if (pq instanceof ParsedTupleQuery || pq instanceof ParsedGraphQuery) {
            // order elements of the set in some way by converting it into the list
            answerVariables = new ArrayList<>(body.variables);
        }
		else {
            // ASK queries have no answer variables
            answerVariables = Collections.emptyList();
        }

        Predicate pred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY, answerVariables.size());
        Function head = ofac.getFunction(pred, answerVariables);
        appendRule(head, body.atoms);

        List<String> signature = Lists.transform(answerVariables, t -> ((Variable)t).getName());

        //System.out.println("PROGRAM\n" + program.program);
		return new SparqlQuery(program, signature);
	}

    private static class TranslationResult {
        final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

        final ImmutableList<Function> atoms;
        final ImmutableSet<Variable> variables;
        final boolean isBGP;

        TranslationResult(ImmutableList<Function> atoms, ImmutableSet<Variable> variables, boolean isBGP) {
            this.atoms = atoms;
            this.variables = variables;
            this.isBGP = isBGP;
        }

        /**
         * Extends the current translation result with bindings coming from {@link Extension} (expr AS ?x) or {@link BindingSetAssignment} (VALUES in SPARQL)
         *
         * @param bindings   a stream of bindings. A binding is a pair of a variable, and a value/expression
         * @param varMapper  a function from bindings to {@link Variable}s
         * @param exprMapper a function maps a pair of a binding and a set variables to a {@link Term}
         * @param <T>        A class for binding. E.g. {@link org.openrdf.query.Binding} or {@link org.openrdf.query.algebra.ExtensionElem}
         * @return extended translation result
         */
        <T> TranslationResult extendWithBindings(Stream<T> bindings,
                                                 java.util.function.Function<? super T, Variable> varMapper,
                                                 java.util.function.BiFunction<? super T, ImmutableSet<Variable>, Term> exprMapper) {

            Set<Variable> vars = new HashSet<>(variables);
            Stream<Function> eqAtoms = bindings.map(b -> {
                Term expr = exprMapper.apply(b, ImmutableSet.copyOf(vars));

                Variable v = varMapper.apply(b);
                if (!vars.add(v))
                    throw new IllegalArgumentException("Duplicate binding for variable " + v);

                return ofac.getFunctionEQ(v, expr);
            });

            return new TranslationResult(getAtomsExtended(eqAtoms), ImmutableSet.copyOf(vars), false);
        }


        ImmutableList<Function> getAtomsExtendedWithNulls(ImmutableSet<Variable> allVariables) {
            Sets.SetView<Variable>  nullVariables = Sets.difference(allVariables, variables);
            if (nullVariables.isEmpty())
                return atoms;

            return getAtomsExtended(nullVariables.stream().map(v -> ofac.getFunctionEQ(v, OBDAVocabulary.NULL)));
        }

        /**
         * Extends the atoms in current translation result with {@code extension}
         *
         * @param extension a stream of functions to be added
         * @return extended list of atoms
         */
        ImmutableList<Function> getAtomsExtended(Stream<Function> extension) {
            return ImmutableList.copyOf(Iterables.concat(atoms, extension.collect(Collectors.toList())));

//            ImmutableList.Builder builder = ImmutableList.<Function>builder().addAll(atoms)
//                    .addAll(nullVariables.stream().map(v -> ofac.getFunctionEQ(v, OBDAVocabulary.NULL)).iterator());

//            return builder.build();
        }

    }

    private Function getFreshHead(List<Term> terms) {
        Predicate pred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY + predicateIdx, terms.size());
        predicateIdx++;
        return ofac.getFunction(pred, terms);
    }

    private TranslationResult createFreshNode(ImmutableSet<Variable> vars) {
        Function head = getFreshHead(new ArrayList<>(vars));
        return new TranslationResult(ImmutableList.of(head), vars, false);
    }

    private Function wrapNonTriplePattern(TranslationResult sub) {
        if (sub.atoms.size() > 1 || sub.atoms.get(0).isAlgebraFunction()) {
            Function head = getFreshHead(new ArrayList<>(sub.variables));
            appendRule(head, sub.atoms);
            return head;
        }
        return sub.atoms.get(0);
    }

    private void appendRule(Function head, List<Function> body) {
        CQIE rule = ofac.getCQIE(head, body);
        program.appendRule(rule);
    }


    private TranslationResult translate(TupleExpr node) {

        //System.out.println("node: \n" + node);

        if (node instanceof Slice) {   // SLICE algebra operation
            Slice slice = (Slice) node;
            OBDAQueryModifiers modifiers = program.getQueryModifiers();
            modifiers.setOffset(slice.getOffset());
            modifiers.setLimit(slice.getLimit());
            return translate(slice.getArg());
        }
        else if (node instanceof Distinct) { // DISTINCT algebra operation
            Distinct distinct = (Distinct) node;
            program.getQueryModifiers().setDistinct();
            return translate(distinct.getArg());
        }
        else if (node instanceof Reduced) {  // REDUCED algebra operation
            Reduced reduced = (Reduced) node;
            return translate(reduced.getArg());
        }
        else if (node instanceof Order) {   // ORDER algebra operation
            Order order = (Order) node;
            OBDAQueryModifiers modifiers = program.getQueryModifiers();
            for (OrderElem c : order.getElements()) {
                ValueExpr expression = c.getExpr();
                if (!(expression instanceof Var))
                    throw new IllegalArgumentException("Error translating ORDER BY. "
                            + "The current implementation can only sort by variables. "
                            + "This query has a more complex expression '" + expression + "'");

                Var v = (Var) expression;
                Variable var = ofac.getVariable(v.getName());
                int direction = c.isAscending() ? OrderCondition.ORDER_ASCENDING
                        : OrderCondition.ORDER_DESCENDING;
                modifiers.addOrderCondition(var, direction);
            }
            return translate(order.getArg());
        }
        else if (node instanceof StatementPattern) { // triple pattern
            return translateTriplePattern((StatementPattern) node);
        }
        else if (node instanceof SingletonSet) {
            // the empty BGP has no variables and gives a single solution mapping on every non-empty graph
            return new TranslationResult(ImmutableList.of(), ImmutableSet.of(), true);
        }
        else if (node instanceof Join) {     // JOIN algebra operation
            Join join = (Join) node;
            TranslationResult a1 = translate(join.getLeftArg());
            TranslationResult a2 = translate(join.getRightArg());
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            if (a1.isBGP && a2.isBGP) {             // collect triple patterns into BGPs
                ImmutableList<Function> atoms =
                        ImmutableList.<Function>builder().addAll(a1.atoms).addAll(a2.atoms).build();
                return new TranslationResult(atoms, vars, true);
            }
            else {
                Function body = ofac.getSPARQLJoin(wrapNonTriplePattern(a1),
                        wrapNonTriplePattern(a2));

                return new TranslationResult(ImmutableList.of(body), vars, false);
            }
        }
        else if (node instanceof LeftJoin) {  // OPTIONAL algebra operation
            LeftJoin lj = (LeftJoin) node;
            TranslationResult a1 = translate(lj.getLeftArg());
            TranslationResult a2 = translate(lj.getRightArg());
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            Function body = ofac.getSPARQLLeftJoin(wrapNonTriplePattern(a1),
                    wrapNonTriplePattern(a2));

            ValueExpr expr = lj.getCondition();
            if (expr != null) {
                Function f = getFilterExpression(expr, vars);
                body.getTerms().add(f);
            }

            return new TranslationResult(ImmutableList.of(body), vars, false);
        }
        else if (node instanceof Union) {   // UNION algebra operation
            Union union = (Union) node;
            TranslationResult a1 = translate(union.getLeftArg());
            TranslationResult a2 = translate(union.getRightArg());
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            TranslationResult res = createFreshNode(vars);
            appendRule(res.atoms.get(0), a1.getAtomsExtendedWithNulls(vars));
            appendRule(res.atoms.get(0), a2.getAtomsExtendedWithNulls(vars));
            return res;
        }
        else if (node instanceof Filter) {   // FILTER algebra operation
            Filter filter = (Filter) node;
            TranslationResult a = translate(filter.getArg());

            Function f = getFilterExpression(filter.getCondition(), a.variables);
            ImmutableList<Function> atoms = ImmutableList.<Function>builder().addAll(a.atoms).add(f).build();
            // TODO: split ANDs in the FILTER?

            return new TranslationResult(atoms, a.variables, false);
        }
        else if (node instanceof Projection) {  // PROJECT algebra operation
            Projection projection = (Projection) node;
            TranslationResult sub = translate(projection.getArg());

            List<ProjectionElem> pes = projection.getProjectionElemList().getElements();
            // the two lists are required to synchronise the order of variables
            List<Term> sVars = new ArrayList<>(pes.size());
            List<Term> tVars = new ArrayList<>(pes.size());
            boolean noRenaming = true;
            for (ProjectionElem pe : pes) {
                Variable sVar = ofac.getVariable(pe.getSourceName());
                if (!sub.variables.contains(sVar))
                    throw new IllegalArgumentException("Projection source of " + pe
                            + " not found in " + projection.getArg());
                sVars.add(sVar);

                Variable tVar = ofac.getVariable(pe.getTargetName());
                tVars.add(tVar);

                if (!sVar.equals(tVar))
                    noRenaming = false;
            }
            if (noRenaming && sVars.containsAll(sub.variables)) // neither projection nor renaming
                return sub;

            ImmutableSet<Variable> vars = ImmutableSet.copyOf(
                    tVars.stream().map(t -> (Variable) t).collect(Collectors.toSet()));

            if (noRenaming)
                return new TranslationResult(sub.atoms, vars, false);

            Function head = getFreshHead(sVars);
            appendRule(head, sub.atoms);

            Function atom = ofac.getFunction(head.getFunctionSymbol(), tVars);
            return new TranslationResult(ImmutableList.of(atom), vars, false);
        }
        else if (node instanceof Extension) {     // EXTEND algebra operation
            Extension extension = (Extension) node;
            TranslationResult sub = translate(extension.getArg());
            final Stream<ExtensionElem> nontrivialBindings = extension.getElements().stream()
                    // ignore EXTEND(P, v, v), which is sometimes introduced by Sesame SPARQL parser
                    .filter(ee -> !(ee.getExpr() instanceof Var && ee.getName().equals(((Var) ee.getExpr()).getName())));
            return sub.extendWithBindings(
                    nontrivialBindings,
                    ee -> ofac.getVariable(ee.getName()),
                    (ee, vars) -> getExpression(ee.getExpr(), vars));
        }
        else if (node instanceof BindingSetAssignment) { // VALUES in SPARQL
            BindingSetAssignment values = (BindingSetAssignment) node;

            TranslationResult empty = new TranslationResult(ImmutableList.of(), ImmutableSet.of(), false);
            List<TranslationResult> bindings =
                    StreamSupport.stream(values.getBindingSets().spliterator(), false)
                            .map(bs -> empty.extendWithBindings(
                                    StreamSupport.stream(bs.spliterator(), false),
                                    be -> ofac.getVariable(be.getName()),
                                    (be, vars) -> getTermForLiteralOrIri(be.getValue())))
                            .collect(Collectors.toList());

            ImmutableSet<Variable> allVars = bindings.stream()
                    .flatMap(s -> s.variables.stream())
                    .collect(ImmutableCollectors.toSet());

            TranslationResult res = createFreshNode(allVars);
            bindings.forEach(p ->
                    appendRule(res.atoms.get(0), p.getAtomsExtendedWithNulls(allVars)));
            return res;
        }
        else if (node instanceof Group) {
            throw new IllegalArgumentException("GROUP BY is not supported yet");
        }
        throw new IllegalArgumentException("Not supported: " + node);
    }

    /**
     *
     * @param expr  expression
     * @param variables the set of variables that can occur in the expression
     *                  (the rest will be replaced with NULL)
     * @return
     */

    private Function getFilterExpression(ValueExpr expr, ImmutableSet<Variable> variables) {
        Term term = getExpression(expr, variables);
        // Effective Boolean Value (EBV): wrap in isTrue function if it is not a (Boolean) expression
        if (term instanceof Function) {
            Function f = (Function) term;
            // TODO: check whether the return type is Boolean
            return f;
        }
        return ofac.getFunctionIsTrue(term);
    }

	private TranslationResult translateTriplePattern(StatementPattern triple) {

        // A triple pattern is member of the set (RDF-T + V) x (I + V) x (RDF-T + V)
        // VarOrTerm ::=  Var | GraphTerm
        // GraphTerm ::=  iri | RDFLiteral | NumericLiteral | BooleanLiteral | BlankNode | NIL

        ImmutableSet.Builder<Variable> variables = ImmutableSet.builder();
        Function atom;

        Value s = triple.getSubjectVar().getValue();
        Value p = triple.getPredicateVar().getValue();
        Value o = triple.getObjectVar().getValue();

        Term sTerm = (s == null) ? getTermForVariable(triple.getSubjectVar(), variables) : getTermForLiteralOrIri(s);

		if (p == null) {
			//  term variable term .
            Term pTerm = getTermForVariable(triple.getPredicateVar(), variables);
            Term oTerm = (o == null) ? getTermForVariable(triple.getObjectVar(), variables) : getTermForLiteralOrIri(o);
			atom = ofac.getTripleAtom(sTerm, pTerm, oTerm);
		}
		else if (p instanceof URI) {
			if (p.equals(RDF.TYPE)) {
				if (o == null) {
					// term rdf:type variable .
					Term pTerm = ofac.getUriTemplate(ofac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
                    Term oTerm = getTermForVariable(triple.getObjectVar(), variables);
					atom = ofac.getTripleAtom(sTerm, pTerm, oTerm);
				}
				else if (o instanceof URI) {
					// term rdf:type uri .
					Predicate.COL_TYPE type = dtfac.getDatatype((URI)o);
					if (type != null) // datatype
						atom = ofac.getFunction(dtfac.getTypePredicate(type), sTerm);
					else // class
						atom = ofac.getFunction(ofac.getClassPredicate(o.stringValue()), sTerm);
				}
				else
					throw new IllegalArgumentException("Unsupported query syntax");
			}
			else {
				// term uri term . (where uri is either an object or a datatype property)
				Term oTerm = (o == null) ? getTermForVariable(triple.getObjectVar(), variables) : getTermForLiteralOrIri(o);
				Predicate predicate = ofac.getPredicate(p.stringValue(), new COL_TYPE[] { null, null });
				atom = ofac.getFunction(predicate, sTerm, oTerm);
			}
		}
		else
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");

        return new TranslationResult(ImmutableList.of(atom), variables.build(), true);
	}

    private static Term getTermForVariable(Var v, ImmutableSet.Builder<Variable> variables) {
        Variable var = ofac.getVariable(v.getName());
        variables.add(var);
        return var;
    }

    private Term getTermForLiteralOrIri(Value v) {

        if (v instanceof Literal)
            return getTermForLiteral((Literal) v);
        else if (v instanceof URI)
            return getTermForIri((URI)v, false);

        throw new RuntimeException("The value " + v + " is not supported yet!");
    }

    private static Term getTermForLiteral(Literal literal) {
        URI typeURI = literal.getDatatype();
        String value = literal.getLabel();

        COL_TYPE type;
        if (typeURI == null)
            type = COL_TYPE.LITERAL;
        else {
            type = dtfac.getDatatype(typeURI);
            if (type == null)
                // ROMAN (27 June 2016): type1 in open-eq-05 test would not be supported in OWL
                // the actual value is LOST here
                return ofac.getUriTemplateForDatatype(typeURI.stringValue());
                // old strict version:
                // throw new RuntimeException("Unsupported datatype: " + typeURI);

            // check if the value is (lexically) correct for the specified datatype
            if (!XMLDatatypeUtil.isValidValue(value, typeURI))
                throw new RuntimeException("Invalid lexical form for datatype. Found: " + value);
        }

        Term constant = ofac.getConstantLiteral(value, type);

        // wrap the constant in its datatype function
        if (type == COL_TYPE.LITERAL) {
            // if the object has type LITERAL, check the language tag
            String lang = literal.getLanguage();
            if (lang != null && !lang.equals(""))
                return ofac.getTypedTerm(constant, lang);
        }
        return ofac.getTypedTerm(constant, type);
    }

    /**
     *
     * @param v URI object
     * @param unknownUrisToTemplates - the URIs are treated differently in triple patterns
     *                               and filter expressions (this will be normalised later)
     * @return term (URI template)
     */

    private Term getTermForIri(URI v, boolean unknownUrisToTemplates) {

        String uri = EncodeForURI.decodeURIEscapeCodes(v.stringValue());

        if (uriRef != null) {  // if in the Semantic Index mode
            int id = uriRef.getId(uri);
            if (id < 0 && unknownUrisToTemplates)  // URI is not found and need to wrap it in a template
                return ofac.getUriTemplateForDatatype(uri);
            else
                return ofac.getUriTemplate(ofac.getConstantLiteral(String.valueOf(id), COL_TYPE.INTEGER));
        }
        else {
            Function constantFunction = uriTemplateMatcher.generateURIFunction(uri);
            if (constantFunction.getArity() == 1 && unknownUrisToTemplates) {
                // ROMAN (27 June 2016: this means ZERO arguments, e.g., xsd:double or :z
                // despite the name, this is NOT necessarily a datatype
                constantFunction = ofac.getUriTemplateForDatatype(uri);
            }
            return constantFunction;
        }
    }

    /**
     *
     * @param expr expression
     * @param variables the set of variables that can occur in the expression
     *                  (the rest will be replaced with NULL)
     * @return term
     */

	private Term getExpression(ValueExpr expr, ImmutableSet<Variable> variables) {

        // PrimaryExpression ::= BrackettedExpression | BuiltInCall | iriOrFunction |
        //                          RDFLiteral | NumericLiteral | BooleanLiteral | Var
        // iriOrFunction ::= iri ArgList?

		if (expr instanceof Var) {
            Var v = (Var) expr;
            Variable var = ofac.getVariable(v.getName());
            return variables.contains(var) ? var : OBDAVocabulary.NULL;
		} 
		else if (expr instanceof ValueConstant) {
			Value v = ((ValueConstant) expr).getValue();
            if (v instanceof Literal)
                return getTermForLiteral((Literal) v);
            else if (v instanceof URI)
                return getTermForIri((URI)v, true);

            throw new RuntimeException("The value " + v + " is not supported yet!");
        }
        else if (expr instanceof Bound) {
            // BOUND (Sec 17.4.1.1)
            // xsd:boolean  BOUND (variable var)
            Var v = ((Bound) expr).getArg();
            Variable var = ofac.getVariable(v.getName());
            return variables.contains(var) ? ofac.getFunctionIsNotNull(var) : ofac.getBooleanConstant(false);
        }
        else if (expr instanceof UnaryValueOperator) {
            Term term = getExpression(((UnaryValueOperator) expr).getArg(), variables);

            if (expr instanceof Not) {
                return ofac.getFunctionNOT(term);
            }
            else if (expr instanceof IsLiteral) {
                return ofac.getFunction(ExpressionOperation.IS_LITERAL, term);
            }
            else if (expr instanceof IsURI) {
                return ofac.getFunction(ExpressionOperation.IS_IRI, term);
            }
            else if (expr instanceof Str) {
                return ofac.getFunction(ExpressionOperation.SPARQL_STR, term);
            }
            else if (expr instanceof Datatype) {
                return ofac.getFunction(ExpressionOperation.SPARQL_DATATYPE, term);
            }
            else if (expr instanceof IsBNode) {
                return ofac.getFunction(ExpressionOperation.IS_BLANK, term);
            }
            else if (expr instanceof Lang) {
                ValueExpr arg = ((UnaryValueOperator) expr).getArg();
                if (arg instanceof Var)
                    return ofac.getFunction(ExpressionOperation.SPARQL_LANG, term);
                else
                    throw new RuntimeException("A variable or a value is expected in " + expr);
            }
            // other subclasses
            // IRIFunction: IRI (Sec 17.4.2.8) for constructing IRIs
            // IsNumeric:  isNumeric (Sec 17.4.2.4) for checking whether the argument is a numeric value
            // AggregateOperatorBase: Avg, Min, Max, etc.
            // Like:  ??
            // IsResource: ??
            // LocalName: ??
            // Namespace: ??
            // Label: ??
		}
		else if (expr instanceof BinaryValueOperator) {
            BinaryValueOperator bexpr = (BinaryValueOperator) expr;
            Term term1 = getExpression(bexpr.getLeftArg(), variables);
            Term term2 = getExpression(bexpr.getRightArg(), variables);

            if (expr instanceof And) {
                return ofac.getFunctionAND(term1, term2);
            }
            else if (expr instanceof Or) {
                return ofac.getFunctionOR(term1, term2);
            }
            else if (expr instanceof SameTerm) {
                // sameTerm (Sec 17.4.1.8)
                // ROMAN (28 June 2016): strictly speaking it's not equality
                return ofac.getFunctionEQ(term1, term2);
            }
            else if (expr instanceof Regex) {
                // REGEX (Sec 17.4.3.14)
                // xsd:boolean  REGEX (string literal text, simple literal pattern)
                // xsd:boolean  REGEX (string literal text, simple literal pattern, simple literal flags)
                Regex reg = (Regex) expr;
                Term term3 = (reg.getFlagsArg() != null) ?
                        getExpression(reg.getFlagsArg(), variables) : OBDAVocabulary.NULL;
                return ofac.getFunction(ExpressionOperation.REGEX, term1, term2, term3);
            }
            else if (expr instanceof Compare) {
                ExpressionOperation p = RelationalOperations.get(((Compare) expr).getOperator());
                return ofac.getFunction(p, term1, term2);
            }
            else if (expr instanceof MathExpr) {
                ExpressionOperation p = NumericalOperations.get(((MathExpr)expr).getOperator());
                return ofac.getFunction(p, term1, term2);
            }
            else if (expr instanceof LangMatches) {
                if (term2 instanceof Function) {
                    Function f = (Function) term2;
                    if (f.isDataTypeFunction()) {
                        Term functionTerm = f.getTerm(0);
                        if (functionTerm instanceof Constant) {
                            Constant c = (Constant) functionTerm;
                            term2 = ofac.getFunction(f.getFunctionSymbol(),
                                    ofac.getConstantLiteral(c.getValue().toLowerCase(),
                                            c.getType()));
                        }
                    }
                }
                return ofac.getLANGMATCHESFunction(term1, term2);
            }
        }
		else if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;

            int arity = f.getArgs().size();
            List<Term> terms = new ArrayList<>(arity);
            for (ValueExpr a : f.getArgs())
                terms.add(getExpression(a, variables));

            OperationPredicate p = XPathFunctions.get(f.getURI());
            if (p != null) {
                if (arity != p.getArity())
                    throw new UnsupportedOperationException(
                            "Wrong number of arguments (found " + terms.size() + ", only " +
                                    p.getArity() + "supported) for SPARQL " + f.getURI() + "function");

                return ofac.getFunction(p, terms);
            }

            // these are all special cases with **variable** number of arguments

            switch (f.getURI()) {
                // CONCAT (Sec 17.4.3.12)
                // string literal  CONCAT(string literal ltrl1 ... string literal ltrln)
                case "http://www.w3.org/2005/xpath-functions#concat":
                    if (arity < 1)
                        throw new UnsupportedOperationException("Wrong number of arguments (found " + terms.size() +
                                ", at least 1) for SPARQL function CONCAT");

                    Term concat = terms.get(0);
                    for (int i = 1; i < arity; i++) // .get(i) is OK because it's based on an array
                        concat = ofac.getFunction(ExpressionOperation.CONCAT, concat, terms.get(i));
                    return concat;

                // REPLACE (Sec 17.4.3.15)
                //string literal  REPLACE (string literal arg, simple literal pattern, simple literal replacement )
                //string literal  REPLACE (string literal arg, simple literal pattern, simple literal replacement,  simple literal flags)
                case "http://www.w3.org/2005/xpath-functions#replace":
                    // TODO: the fourth argument is flags (see http://www.w3.org/TR/xpath-functions/#flags)
                    Term flags;
                    if (arity == 3)
                        flags = ofac.getConstantLiteral("");
                    else if (arity == 4)
                        flags = terms.get(3);
                    else
                        throw new UnsupportedOperationException("Wrong number of arguments (found "
                                + terms.size() + ", only 3 or 4 supported) for SPARQL function REPLACE");

                    return ofac.getFunction(ExpressionOperation.REPLACE, terms.get(0), terms.get(1), terms.get(2), flags);


                    // SUBSTR (Sec 17.4.3.3)
                    // string literal  SUBSTR(string literal source, xsd:integer startingLoc)
                    // string literal  SUBSTR(string literal source, xsd:integer startingLoc, xsd:integer length)
                case "http://www.w3.org/2005/xpath-functions#substring":
                    if (arity == 2)
                        return ofac.getFunction(ExpressionOperation.SUBSTR2, terms.get(0), terms.get(1));
                    else if (arity == 3)
                        return ofac.getFunction(ExpressionOperation.SUBSTR3, terms.get(0), terms.get(1), terms.get(2));

                    throw new UnsupportedOperationException("Wrong number of arguments (found "
                            + terms.size() + ", only 2 or 3 supported) for SPARQL function SUBSTRING");

                default:
                    throw new RuntimeException("Function " + f.getURI() + " is not supported yet!");
            }
		}
        // other subclasses
        // SubQueryValueOperator
        // If
        // BNodeGenerator
        // NAryValueOperator (ListMemberOperator and Coalesce)
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}

    // XPath 1.0 functions (XPath 1.1 has variants with more arguments)
    private static final ImmutableMap<String, OperationPredicate> XPathFunctions =
            new ImmutableMap.Builder<String, OperationPredicate>()
                    /*
                     * String functions
                     */
                    .put("http://www.w3.org/2005/xpath-functions#upper-case", ExpressionOperation.UCASE)
                    .put("http://www.w3.org/2005/xpath-functions#lower-case", ExpressionOperation.LCASE)
                    .put("http://www.w3.org/2005/xpath-functions#string-length", ExpressionOperation.STRLEN)
                    .put("http://www.w3.org/2005/xpath-functions#substring-before", ExpressionOperation.STRBEFORE)
                    .put("http://www.w3.org/2005/xpath-functions#substring-after", ExpressionOperation.STRAFTER)
                    .put("http://www.w3.org/2005/xpath-functions#starts-with", ExpressionOperation.STR_STARTS)
                    .put("http://www.w3.org/2005/xpath-functions#ends-with", ExpressionOperation.STR_ENDS)
                    .put("http://www.w3.org/2005/xpath-functions#encode-for-uri", ExpressionOperation.ENCODE_FOR_URI)
                    .put("http://www.w3.org/2005/xpath-functions#contains", ExpressionOperation.CONTAINS)
                    .put("UUID", ExpressionOperation.UUID)
                    .put("STRUUID", ExpressionOperation.STRUUID)
                    /*
                     * Numerical functions
                     */
                    .put("http://www.w3.org/2005/xpath-functions#numeric-abs", ExpressionOperation.ABS)
                    .put("http://www.w3.org/2005/xpath-functions#numeric-ceil", ExpressionOperation.CEIL)
                    .put("http://www.w3.org/2005/xpath-functions#numeric-floor", ExpressionOperation.FLOOR)
                    .put("http://www.w3.org/2005/xpath-functions#numeric-round", ExpressionOperation.ROUND)
                    .put("RAND", ExpressionOperation.RAND)
                    /*
                     * Datetime functions
                     */
                    .put("http://www.w3.org/2005/xpath-functions#year-from-dateTime", ExpressionOperation.YEAR)
                    .put("http://www.w3.org/2005/xpath-functions#day-from-dateTime", ExpressionOperation.DAY)
                    .put("http://www.w3.org/2005/xpath-functions#month-from-dateTime", ExpressionOperation.MONTH)
                    .put("http://www.w3.org/2005/xpath-functions#hours-from-dateTime", ExpressionOperation.HOURS)
                    .put("http://www.w3.org/2005/xpath-functions#minutes-from-dateTime", ExpressionOperation.MINUTES)
                    .put("http://www.w3.org/2005/xpath-functions#seconds-from-dateTime", ExpressionOperation.SECONDS)
                    .put("NOW", ExpressionOperation.NOW)
                    .put("TZ", ExpressionOperation.TZ)
                    /*
                     * Hash functions
                     */
                    .put("MD5", ExpressionOperation.MD5)
                    .put("SHA1", ExpressionOperation.SHA1)
                    .put("SHA256", ExpressionOperation.SHA256)
                    .put("SHA512", ExpressionOperation.SHA512)
                    .build();


	private static final ImmutableMap<Compare.CompareOp, ExpressionOperation> RelationalOperations =
			new ImmutableMap.Builder<Compare.CompareOp, ExpressionOperation>()
				.put(Compare.CompareOp.EQ, ExpressionOperation.EQ)
				.put(Compare.CompareOp.GE, ExpressionOperation.GTE)
				.put(Compare.CompareOp.GT, ExpressionOperation.GT)
				.put(Compare.CompareOp.LE, ExpressionOperation.LTE)
				.put(Compare.CompareOp.LT, ExpressionOperation.LT)
				.put(Compare.CompareOp.NE, ExpressionOperation.NEQ)
				.build();

	private static final ImmutableMap<MathExpr.MathOp, ExpressionOperation> NumericalOperations =
			new ImmutableMap.Builder<MathExpr.MathOp, ExpressionOperation>()
			.put(MathExpr.MathOp.PLUS, ExpressionOperation.ADD)
			.put(MathExpr.MathOp.MINUS, ExpressionOperation.SUBTRACT)
			.put(MathExpr.MathOp.MULTIPLY, ExpressionOperation.MULTIPLY)
			.put(MathExpr.MathOp.DIVIDE, ExpressionOperation.DIVIDE)
			.build();
}