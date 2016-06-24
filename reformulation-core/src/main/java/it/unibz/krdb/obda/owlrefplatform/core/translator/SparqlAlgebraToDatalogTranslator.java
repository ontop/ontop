package it.unibz.krdb.obda.owlrefplatform.core.translator;

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
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.UriTemplateMatcher;
import it.unibz.krdb.obda.parser.EncodeForURI;
import javafx.util.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.*;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 * Translate a SPARQL algebra expression into a Datalog program that has the
 * same semantics. We use the built-in predicates Join and LeftJoin.
 * 
 * @author Roman Kontchakov
 */
public class SparqlAlgebraToDatalogTranslator {

	
	private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();

	private final UriTemplateMatcher uriTemplateMatcher;
	private final SemanticIndexURIMap uriRef;  
	
	private static final Logger log = LoggerFactory.getLogger(SparqlAlgebraToDatalogTranslator.class);
	
	/**
	 * 
	 * @param uriTemplateMatcher
	 * @param uriRef is used only in the Semantic Index mode
	 */
	
	public SparqlAlgebraToDatalogTranslator(UriTemplateMatcher uriTemplateMatcher, SemanticIndexURIMap uriRef) {
		this.uriTemplateMatcher = uriTemplateMatcher;
		this.uriRef = uriRef;
	}
	
	/**
	 * Translate a given SPARQL query object to datalog program.
	 * 
	 *
	 *            The Query object.
	 * @return Datalog program that represents the construction of the SPARQL
	 *         query.
	 */

	public SparqlQuery translate(ParsedQuery pq) {
		
		TupleExpr te = pq.getTupleExpr();
		log.debug("SPARQL algebra: \n{}", te);
		//System.out.println("SPARQL algebra:\n" + te);

        TranslationProgram program = new TranslationProgram();
        TranslationResult body = tran(te, program);

        List<Term> answerVariables;
		if (pq instanceof ParsedTupleQuery || pq instanceof ParsedGraphQuery) {
            // order elements of the set in some way by converting it into the list
            answerVariables = new ArrayList<Term>(body.variables);
        }
		else {
            // ASK queries have no answer variables
            answerVariables = Collections.emptyList();
        }

        Predicate pred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY, answerVariables.size());
        Function head = ofac.getFunction(pred, answerVariables);
        program.appendRule(head, body.atoms);

        List<String> signature = new ArrayList<>(answerVariables.size());
        for (Term v : answerVariables)
            signature.add(((Variable)v).getName());

		return new SparqlQuery(program.program, signature);
	}

    private static class TranslationResult {
        private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

        final ImmutableList<Function> atoms;
        final ImmutableSet<Variable> variables;
        final boolean isBGP;

        TranslationResult(ImmutableList<Function> atoms, ImmutableSet<Variable> variables, boolean isBGP) {
            this.atoms = atoms;
            this.variables = variables;
            this.isBGP = isBGP;
        }

        private ImmutableList<Function> getAtomsExtendedWithNulls(ImmutableSet<Variable> allVariables) {
            Sets.SetView<Variable>  nullVariables = Sets.difference(allVariables, variables);
            if (nullVariables.isEmpty())
                return atoms;

            ImmutableList.Builder builder = ImmutableList.<Function>builder().addAll(atoms);
            for (Variable v : nullVariables)
                builder.add(ofac.getFunctionEQ(v, OBDAVocabulary.NULL));
            return builder.build();
        }

    }

    private static class TranslationProgram {
        private final OBDADataFactory ofac = OBDADataFactoryImpl.getInstance();

        private final DatalogProgram program;
        private int predicateIdx = 0;

        TranslationProgram() {
            this.program = ofac.getDatalogProgram();
        }

        OBDAQueryModifiers getQueryModifiers() {
            return program.getQueryModifiers();
        }

        Function getFreshHead(List<Term> terms) {
            Predicate pred = ofac.getPredicate(OBDAVocabulary.QUEST_QUERY + predicateIdx, terms.size());
            predicateIdx++;
            Function head = ofac.getFunction(pred, terms);
            return head;
        }

        void appendRule(Function head, List<Function> body) {
            CQIE rule = ofac.getCQIE(head, body);
            program.appendRule(rule);
        }

        Function wrapNonTriplePattern(TranslationResult sub) {
            if (sub.atoms.size() > 1 || sub.atoms.get(0).isAlgebraFunction()) {
                Function head = getFreshHead(new ArrayList<>(sub.variables));
                appendRule(head, sub.atoms);
                return head;
            }
            return sub.atoms.get(0);
        }
    }

    private TranslationResult tran(TupleExpr currentNode, TranslationProgram program) {

        //System.out.println(currentNode + "\n\n");

        if (currentNode instanceof Slice) {   // SLICE algebra operation
            Slice slice = (Slice) currentNode;
            OBDAQueryModifiers modifiers = program.getQueryModifiers();
            modifiers.setOffset(slice.getOffset());
            modifiers.setLimit(slice.getLimit());
            return tran(slice.getArg(), program);
        }
        else if (currentNode instanceof Distinct) { // DISTINCT algebra operation
            Distinct distinct = (Distinct) currentNode;
            program.getQueryModifiers().setDistinct();
            return tran(distinct.getArg(), program);
        }
        else if (currentNode instanceof Reduced) {  // REDUCED algebra operation
            Reduced reduced = (Reduced) currentNode;
            return tran(reduced.getArg(), program);
        }
        else if (currentNode instanceof Order) {   // ORDER algebra operation
            Order order = (Order) currentNode;
            OBDAQueryModifiers modifiers = program.getQueryModifiers();
            for (OrderElem c : order.getElements()) {
                ValueExpr expression = c.getExpr();
                if (!(expression instanceof Var))
                    throw new IllegalArgumentException("Error translating ORDER BY. "
                            + "The current implementation can only sort by variables. "
                            + "This query has a more complex expression '" + expression + "'");

                Var v = (Var) expression;
                Variable var = ofac.getVariable(v.getName());
                int direction =  c.isAscending() ? OrderCondition.ORDER_ASCENDING
                        : OrderCondition.ORDER_DESCENDING;
                modifiers.addOrderCondition(var, direction);
            }
            return tran(order.getArg(), program);
        }
        else if (currentNode instanceof StatementPattern) { // triple pattern
            Function atom = translate((StatementPattern) currentNode);
            Set<Variable> vars = new HashSet<>();
            TermUtils.addReferencedVariablesTo(vars, atom);
            return new TranslationResult(ImmutableList.of(atom), ImmutableSet.copyOf(vars), true);
        }
        else if (currentNode instanceof Join) {     // JOIN algebra operation
            Join join = (Join) currentNode;
            TranslationResult a1 = tran(join.getLeftArg(), program);
            TranslationResult a2 = tran(join.getRightArg(), program);
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            if (a1.isBGP && a2.isBGP) {             // collect triple patterns into BGPs
                ImmutableList<Function> atoms =
                        ImmutableList.<Function>builder().addAll(a1.atoms).addAll(a2.atoms).build();
                return new TranslationResult(atoms, vars, true);
            }
            else {
                Function body = ofac.getSPARQLJoin(program.wrapNonTriplePattern(a1),
                        program.wrapNonTriplePattern(a2));

                return new TranslationResult(ImmutableList.of(body), vars, false);
            }
        }
        else if (currentNode instanceof LeftJoin) {  // OPTIONAL algebra operation
            LeftJoin lj = (LeftJoin) currentNode;
            TranslationResult a1 = tran(lj.getLeftArg(), program);
            TranslationResult a2 = tran(lj.getRightArg(), program);
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            Function body = ofac.getSPARQLLeftJoin(program.wrapNonTriplePattern(a1),
                    program.wrapNonTriplePattern(a2));

            ValueExpr expr = lj.getCondition();
            if (expr != null) {
                Function f = getFilterExpression(expr, vars);
                body.getTerms().add(f);
            }

            return new TranslationResult(ImmutableList.of(body), vars, false);
        }
        else if (currentNode instanceof Union) {   // UNION algebra operation
            Union union = (Union) currentNode;
            TranslationResult a1 = tran(union.getLeftArg(), program);
            TranslationResult a2 = tran(union.getRightArg(), program);
            ImmutableSet<Variable> vars = Sets.union(a1.variables, a2.variables).immutableCopy();

            Function head = program.getFreshHead(new ArrayList<>(vars)); // sort variables in some way
            program.appendRule(head, a1.getAtomsExtendedWithNulls(vars));
            program.appendRule(head, a2.getAtomsExtendedWithNulls(vars));
            return new TranslationResult(ImmutableList.of(head), vars, false);
        }
        else if (currentNode instanceof Filter) {   // FILTER algebra operation
            Filter filter = (Filter) currentNode;
            TranslationResult a = tran(filter.getArg(), program);

            Function f = getFilterExpression(filter.getCondition(), a.variables);
            ImmutableList<Function> atoms = ImmutableList.<Function>builder().addAll(a.atoms).add(f).build();
            // TODO: split ANDs in f

            return new TranslationResult(atoms, a.variables, false);
        }
        else if (currentNode instanceof Projection) {  // PROJECT algebra operation
            Projection projection = (Projection) currentNode;
            TranslationResult sub = tran(projection.getArg(), program);

            List<ProjectionElem> pes = projection.getProjectionElemList().getElements();
            // the two lists are required to synchronise the order of variables
            List<Term> sVars = new ArrayList<>(pes.size());
            List<Term> tVars = new ArrayList<>(pes.size());
            boolean noRenaming = true;
            for (ProjectionElem pe : pes)  {
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

            ImmutableSet.Builder varsBuilder = ImmutableSet.<Variable>builder();
            for (Term t : tVars)
                varsBuilder.add((Variable)t);
            ImmutableSet<Variable> vars = varsBuilder.build();

            if (noRenaming)
                return new TranslationResult(sub.atoms, vars, false);

            Function head = program.getFreshHead(sVars);
            program.appendRule(head, sub.atoms);

            Function atom = ofac.getFunction(head.getFunctionSymbol(), tVars);
            return new TranslationResult(ImmutableList.of(atom), vars, false);
        }
        else if (currentNode instanceof Extension) {     // EXTEND algebra operation
            Extension extension = (Extension) currentNode;
            TranslationResult sub = tran(extension.getArg(), program);

            Set<Variable> vars = new HashSet<>(sub.variables);
            List<Term> terms = new LinkedList<>(sub.variables);
            ImmutableList.Builder bodyBuilder = ImmutableList.<Function>builder().addAll(sub.atoms);
            for (ExtensionElem ee : extension.getElements()) {
                String varName = ee.getName();
                // ignore EXTEND(P, v, v), which is sometimes introduced by Sesame SPARQL parser
                if (ee.getExpr() instanceof Var && varName.equals(((Var)ee.getExpr()).getName()))
                    continue;

                Variable v = ofac.getVariable(ee.getName());
                if (!vars.add(v))
                    throw new IllegalArgumentException("Duplicate binding for variable " + v
                            + " in " + extension);

                terms.add(v);
                Term expr = getExpression(ee.getExpr()); // TODO: add a fix for the range of variables

                bodyBuilder.add(ofac.getFunctionEQ(v, expr));
            }
            return new TranslationResult(bodyBuilder.build(), ImmutableSet.copyOf(vars), false);
        }
        else if (currentNode instanceof BindingSetAssignment) { // VALUES in SPARQL
            BindingSetAssignment values = (BindingSetAssignment) currentNode;
            ImmutableSet.Builder allVarsBuilder = ImmutableSet.<Variable>builder();
            List<TranslationResult> bindings = new LinkedList<>();
            for (BindingSet bs : values.getBindingSets()) {
                ImmutableList.Builder binding = ImmutableList.<Function>builder();
                Set<Variable> vars = new HashSet<>();
                for (Binding b : bs) {
                    Variable v = ofac.getVariable(b.getName());
                    allVarsBuilder.add(v);
                    if (!vars.add(v))
                        throw new IllegalArgumentException("Duplicate binding for variable " + v
                                + " in " + bs);
                    Term expr = getConstantExpression(b.getValue());
                    binding.add(ofac.getFunctionEQ(v, expr));
                }
                bindings.add(new TranslationResult(binding.build(), ImmutableSet.copyOf(vars), false));
            }
            ImmutableSet<Variable> allVars = allVarsBuilder.build();
            Function head = program.getFreshHead(new LinkedList<>(allVars)); // sort in some way
            for (TranslationResult p : bindings)
                program.appendRule(head, p.getAtomsExtendedWithNulls(allVars));

            return new TranslationResult(ImmutableList.of(head), allVars, false);
        }
        else if (currentNode instanceof Group) {
            throw new IllegalArgumentException("GROUP BY is not supported yet");
        }
        throw new IllegalArgumentException("Not supported: " + currentNode);
    }

	private Function getFilterExpression(ValueExpr condition, ImmutableSet<Variable> vars) {
        // TODO: add check for the variable ranges
		Function filterAtom;
		if (condition instanceof Var)
			filterAtom = ofac.getFunctionIsTrue(getOntopTerm((Var) condition));
		else
			filterAtom = (Function) getExpression(condition);

//		TermUtils.addReferencedVariablesTo(vars, filterAtom);
		return filterAtom;
	}

	/***
	 * This translates a single triple. 
	 * 
	 * @param triple
	 * @return
	 */
	private Function translate(StatementPattern triple) {
		
		Term sTerm = getOntopTerm(triple.getSubjectVar());

		Value p = triple.getPredicateVar().getValue();
		if (p == null) {
			//  term variable term .
			Term oTerm = getOntopTerm(triple.getObjectVar());
			return ofac.getTripleAtom(sTerm, ofac.getVariable(triple.getPredicateVar().getName()), oTerm);
		}
		else if (p instanceof URI) {
			if (p.equals(RDF.TYPE)) {
				Value o = triple.getObjectVar().getValue();
				if (o == null) {
					// term rdf:type variable .
					Function rdfTypeConstant = ofac.getUriTemplate(ofac.getConstantLiteral(OBDAVocabulary.RDF_TYPE));
					return ofac.getTripleAtom(sTerm, rdfTypeConstant, ofac.getVariable(triple.getObjectVar().getName()));
				}
				else if (o instanceof URI) {
					// term rdf:type uri .
					Predicate.COL_TYPE type = dtfac.getDatatype((URI)o);
					if (type != null) // datatype
						return ofac.getFunction(dtfac.getTypePredicate(type), sTerm);
					else // class
						return ofac.getFunction(ofac.getClassPredicate(o.stringValue()), sTerm);
				}
				else
					throw new IllegalArgumentException("Unsupported query syntax");
			}
			else {
				// term uri term . (where uri is either an object or a datatype property)
				Term oTerm = getOntopTerm(triple.getObjectVar());
				Predicate predicate = ofac.getPredicate(p.stringValue(), new COL_TYPE[] { null, null });
				return ofac.getFunction(predicate, sTerm, oTerm);
			}
		}
		else {
			// if predicate is a variable or literal
			throw new RuntimeException("Unsupported query syntax");
		}
	}
	
	private Term getOntopTerm(Var term) {
		Value s = term.getValue();
		if (s == null) {
			return ofac.getVariable(term.getName());
		} 
		else if (s instanceof Literal) {
			Literal literal = (Literal) s;
			URI typeURI = literal.getDatatype();
			String value = literal.getLabel();
	
			COL_TYPE type;
			if (typeURI == null)
				type = COL_TYPE.LITERAL;
			else {
				type = dtfac.getDatatype(typeURI);
		        if (type == null)
					throw new RuntimeException("Unsupported datatype: " + typeURI.stringValue());

				// check if the value is (lexically) correct for the specified datatype
				if (!XMLDatatypeUtil.isValidValue(value, typeURI))
					throw new RuntimeException("Invalid lexical form for datatype. Found: " + value);

				// special case for decimal
				if ((type == COL_TYPE.DECIMAL) && !value.contains("."))
					// put the type as integer (decimal without fractions)
					type = COL_TYPE.INTEGER;
			}
			

			ValueConstant constant = ofac.getConstantLiteral(value, type);

			// v1.7: We extend the syntax such that the data type of a
			// constant is defined using a functional symbol.
			if (type == COL_TYPE.LITERAL) {
				// if the object has type LITERAL, check the language tag
				String lang = literal.getLanguage();
				if (lang != null && !lang.equals(""))
					return ofac.getTypedTerm(constant, lang);
				else
					return ofac.getTypedTerm(constant, type);
			} 
			else {
				return ofac.getTypedTerm(constant, type);
			}
		} 
		else if (s instanceof URI) {
			if (uriRef != null) {
				// if in the Semantic Index mode 
				int id = uriRef.getId(s.stringValue());
				return ofac.getUriTemplate(ofac.getConstantLiteral(String.valueOf(id), COL_TYPE.INTEGER));
			} 
			else {
				String subject_URI = EncodeForURI.decodeURIEscapeCodes(s.stringValue());
				return uriTemplateMatcher.generateURIFunction(subject_URI);
			}
		}
		throw new RuntimeException("Unsupported term " + term);
	}





	private Term getExpression(ValueExpr expr) {
		if (expr instanceof Var) {
			return getOntopTerm((Var) expr);
		} 
		else if (expr instanceof org.openrdf.query.algebra.ValueConstant) {
			return getConstantExpression(((org.openrdf.query.algebra.ValueConstant) expr).getValue());
		} 
		else if (expr instanceof UnaryValueOperator) {
			return getUnaryExpression((UnaryValueOperator) expr);
		} 
		else if (expr instanceof BinaryValueOperator) {
			return getBinaryExpression((BinaryValueOperator) expr);
		} 
		else if (expr instanceof Bound) {	
			return ofac.getFunctionIsNotNull(getOntopTerm(((Bound) expr).getArg()));
		} 
		else if (expr instanceof FunctionCall) {
            return getFunctionCallTerm((FunctionCall)expr);
		} 
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}

	private Term getConcat(List<ValueExpr> values) {
		
        Iterator<ValueExpr> iterator = values.iterator();
        Term topConcat = getExpression(iterator.next());
        
        if (!iterator.hasNext())
            throw new UnsupportedOperationException("Wrong number of arguments (found " + values.size() + 
            					", at least 1) of SQL function CONCAT");
 	  	
        while (iterator.hasNext()) {
            Term second_string = getExpression(iterator.next());
            topConcat = ofac.getFunctionConcat(topConcat, second_string);                	
        }
        
        return topConcat;		
	}
	
	private Term getSubstring(List<ValueExpr> args) {

		if (args.size() == 2) {
			Term str = getExpression(args.get(0));
			Term st = getExpression(args.get(1));
			return ofac.getFunctionSubstring(str, st);
		}
		else if (args.size() == 3) {
			Term str = getExpression(args.get(0));
			Term st = getExpression(args.get(1));
			Term en = getExpression(args.get(2));
			return ofac.getFunctionSubstring(str, st, en);
		}
		else 
			throw new UnsupportedOperationException("Wrong number of arguments (found "
					+ args.size() + ", only 2 or 3 supported) for SPARQL function SUBSTRING");
	}
		
	
	private Term getReplace(List<ValueExpr> args) {
		
		if (args.size() == 2) {
            Term t1 = getExpression(args.get(0));
            Term out_string = getExpression(args.get(1));
            Term in_string = ofac.getConstantLiteral("");
            return ofac.getFunctionReplace(t1, out_string, in_string);
		}
		else if (args.size() == 3) {
            Term t1 = getExpression(args.get(0));
            Term out_string = getExpression(args.get(1));
            Term in_string = getExpression(args.get(2));
            return ofac.getFunctionReplace(t1, out_string, in_string);
		}
        else
            throw new UnsupportedOperationException("Wrong number of arguments (found " 
            		+ args.size() + ", only 2 or 3 supported) to SPARQL function REPLACE");
	}
	
	// XPath 1.0 functions (XPath 1.1 has variants with more arguments)
	private static final ImmutableMap<String, OperationPredicate> XPathFunctions =
				new ImmutableMap.Builder<String, OperationPredicate>()
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

						.put("http://www.w3.org/2005/xpath-functions#numeric-abs", ExpressionOperation.ABS) 
						.put("http://www.w3.org/2005/xpath-functions#numeric-ceil", ExpressionOperation.CEIL) 
						.put("http://www.w3.org/2005/xpath-functions#numeric-floor", ExpressionOperation.FLOOR) 
						.put("http://www.w3.org/2005/xpath-functions#numeric-round", ExpressionOperation.ROUND) 
						.put("RAND", ExpressionOperation.RAND) 
						
						.put("http://www.w3.org/2005/xpath-functions#year-from-dateTime", ExpressionOperation.YEAR) 
						.put("http://www.w3.org/2005/xpath-functions#day-from-dateTime", ExpressionOperation.DAY) 
						.put("http://www.w3.org/2005/xpath-functions#month-from-dateTime", ExpressionOperation.MONTH) 
						.put("http://www.w3.org/2005/xpath-functions#hours-from-dateTime", ExpressionOperation.HOURS) 
						.put("http://www.w3.org/2005/xpath-functions#minutes-from-dateTime", ExpressionOperation.MINUTES) 
						.put("http://www.w3.org/2005/xpath-functions#seconds-from-dateTime", ExpressionOperation.SECONDS) 
						.put("NOW", ExpressionOperation.NOW) 
						.put("TZ", ExpressionOperation.TZ) 
					
						.put("MD5", ExpressionOperation.MD5) 
						.put("SHA1", ExpressionOperation.SHA1) 
						.put("SHA256", ExpressionOperation.SHA256) 
						.put("SHA512", ExpressionOperation.SHA512) 
						.build();
	
	
	
	
	
    /** Return the Functions supported at the moment only
     * concat and replace
     * @param expr
     * @return
     */
    private Term getFunctionCallTerm(FunctionCall expr) {
    	
    	OperationPredicate p = XPathFunctions.get(expr.getURI());
    	if (p != null) {
    		List<ValueExpr> args = expr.getArgs();
    		if (args.size() != p.getArity()) {
                throw new UnsupportedOperationException(
                		"Wrong number of arguments (found " + args.size() + ", only " +
                			 p.getArity() + "supported) for SPARQL " + expr.getURI() + "function");					
    		}
    		List<Term> terms = new ArrayList<>(args.size());
    		for (ValueExpr a : args)
    			terms.add(getExpression(a));
    		Term fun = ofac.getFunction(p, terms);
    		return fun;   		
    	}
    	
    	// these are all special cases with **variable** number of arguments
  
        switch (expr.getURI()) {
        	// at least one argument 
            case "http://www.w3.org/2005/xpath-functions#concat":
                return getConcat(expr.getArgs());

            // 3 or 4 arguments (ROMAN 16 Dec 2015): check the actual implementation
            case "http://www.w3.org/2005/xpath-functions#replace":
                return getReplace(expr.getArgs());
                
            // 2 or 3 arguments    
            case "http://www.w3.org/2005/xpath-functions#substring":
            	return getSubstring(expr.getArgs()); 
            	
            default:
                throw new RuntimeException("Function " + expr.getURI() + " is not supported yet!");
        }
    }


	private Term getConstantExpression(Value v) {

		if (v instanceof Literal) {
			Literal lit = (Literal)v;
			URI type = lit.getDatatype();
			COL_TYPE tp;
			if (type == null) {
				tp = COL_TYPE.LITERAL;
			}
			else {
				tp = dtfac.getDatatype(type);
				if (tp == null)
					return ofac.getUriTemplateForDatatype(type.stringValue());
			}
			
			String constantString;
			switch (tp) {
				case INTEGER:
				case NEGATIVE_INTEGER:
				case NON_POSITIVE_INTEGER:
				case POSITIVE_INTEGER:
				case NON_NEGATIVE_INTEGER:
					constantString = lit.integerValue().toString();
					break;
				case DECIMAL:
					constantString = lit.decimalValue().toString();
					break;
				case INT:
				case UNSIGNED_INT:
					constantString = Integer.toString(lit.intValue());
					break;
				case LONG:
					constantString = Long.toString(lit.longValue());
					break;
				case FLOAT:
					constantString = Float.toString(lit.floatValue());
					break;
				case DOUBLE:
					constantString = Double.toString(lit.doubleValue());
					break;
				case BOOLEAN:
					constantString = Boolean.toString(lit.booleanValue());
					break;
				case DATETIME_STAMP:
				case DATETIME:
				case YEAR:
				case DATE:
				case TIME:
					constantString = lit.calendarValue().toString();
					break;
				case STRING:
				case LITERAL:
					constantString = lit.stringValue();
					break;
				default:
					throw new RuntimeException("Undefined datatype: " + tp);
			}
			ValueConstant constant = ofac.getConstantLiteral(constantString, tp);
			return ofac.getTypedTerm(constant, tp);	
		} 
		else if (v instanceof URI) {
            Function constantFunction = uriTemplateMatcher.generateURIFunction(v.stringValue());
            if (constantFunction.getArity() == 1)
                constantFunction = ofac.getUriTemplateForDatatype(v.stringValue());
            return constantFunction;
		}
		
		throw new RuntimeException("The value " + v + " is not supported yet!");
	}

	private Term getUnaryExpression(UnaryValueOperator expr) {

		Term term = getExpression(expr.getArg());

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
			ValueExpr arg = expr.getArg();
			if (arg instanceof Var) 
				return ofac.getFunction(ExpressionOperation.SPARQL_LANG, term);
			else
				throw new RuntimeException("A variable or a value is expected in " + expr);
		}
		
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}
	
	private static final ImmutableMap<Compare.CompareOp, ExpressionOperation> relationalOperations = 
			new ImmutableMap.Builder<Compare.CompareOp, ExpressionOperation>()
				.put(Compare.CompareOp.EQ, ExpressionOperation.EQ)
				.put(Compare.CompareOp.GE, ExpressionOperation.GTE)
				.put(Compare.CompareOp.GT, ExpressionOperation.GT)
				.put(Compare.CompareOp.LE, ExpressionOperation.LTE)
				.put(Compare.CompareOp.LT, ExpressionOperation.LT)
				.put(Compare.CompareOp.NE, ExpressionOperation.NEQ)
				.build();

	private static final ImmutableMap<MathExpr.MathOp, ExpressionOperation> numericalOperations = 
			new ImmutableMap.Builder<MathExpr.MathOp, ExpressionOperation>()
			.put(MathExpr.MathOp.PLUS, ExpressionOperation.ADD)
			.put(MathExpr.MathOp.MINUS, ExpressionOperation.SUBTRACT)
			.put(MathExpr.MathOp.MULTIPLY, ExpressionOperation.MULTIPLY)
			.put(MathExpr.MathOp.DIVIDE, ExpressionOperation.DIVIDE)
			.build();
			
	
	private Term getBinaryExpression(BinaryValueOperator expr) {
		
		Term term1 = getExpression(expr.getLeftArg());
		Term term2 = getExpression(expr.getRightArg());
		
		if (expr instanceof And) {
			return ofac.getFunctionAND(term1, term2);
		} 
		else if (expr instanceof Or) {
			return ofac.getFunctionOR(term1, term2);
		}
		else if (expr instanceof SameTerm) {
			return ofac.getFunctionEQ(term1, term2);
		} 
		else if (expr instanceof Regex) { // sesame regex is Binary, Jena N-ary
			Regex reg = (Regex) expr;
			ValueExpr flags = reg.getFlagsArg();
			Term term3 = (flags != null) ? getExpression(flags) : OBDAVocabulary.NULL;
			return ofac.getFunction(ExpressionOperation.REGEX, term1, term2, term3);
		}
		else if (expr instanceof Compare) {
			ExpressionOperation p = relationalOperations.get(((Compare) expr).getOperator());
			return ofac.getFunction(p, term1, term2);
		} 
		else if (expr instanceof MathExpr) {
			ExpressionOperation p = numericalOperations.get(((MathExpr)expr).getOperator());
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
		
		throw new RuntimeException("The expression " + expr + " is not supported yet!");
	}
}
