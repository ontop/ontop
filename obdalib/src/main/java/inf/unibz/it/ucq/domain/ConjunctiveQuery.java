/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.ucq.domain;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.ucq.parser.datalog.DatalogCQLexer;
import inf.unibz.it.ucq.parser.datalog.DatalogCQParser;
import inf.unibz.it.ucq.parser.exception.QueryParseException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

// TODO: Remove Target query form here, this is an OBDA specific thing

public class ConjunctiveQuery extends TargetQuery {

	private ArrayList<QueryAtom>	atoms;

	private ArrayList<QueryTerm>	headTerms	= null;

	/***
	 * 
	 * @deprecated dont use anymore, use empty constructor 
	 * @param inputquery
	 * @param apic
	 * @throws QueryParseException
	 */
	public ConjunctiveQuery(String inputquery, APIController apic) throws QueryParseException {
		super(inputquery, apic);
		headTerms = new ArrayList<QueryTerm>();
		
	}

	public ConjunctiveQuery() {
		super();
		headTerms = new ArrayList<QueryTerm>();
		atoms = new ArrayList<QueryAtom>();
	}

	public void addHeadTerm(VariableTerm variable) {
		if (variable == null)
			throw new IllegalArgumentException("received null");

		getHeadTerms().add(variable);
	}

	public void addHeadTerms(ArrayList<QueryTerm> terms) {
		if (terms == null)
			throw new IllegalArgumentException("received null");
		getHeadTerms().addAll(terms);
	}

	/***
	 * Adds a new conjuncted atom to the query's body.
	 * 
	 * @param atom
	 */
	public void addQueryAtom(QueryAtom atom) {
		if (atom == null)
			throw new IllegalArgumentException("received null");
		atoms.add(atom);
		inputquery = toString();
	}

	public ArrayList<QueryAtom> getAtoms() {
		return atoms;
	}

	public ArrayList<QueryTerm> getHeadTerms() {
		if (headTerms == null) {
			headTerms = new ArrayList<QueryTerm>();
		}
		return headTerms;
	}

	/***************************************************************************
	 * Makes sure all the terms in the queries body appear in the head of the
	 * query.
	 */
	public void addAllQueryTermsToHead() {
		ArrayList<QueryTerm> head_variables = getHeadTerms();

		for (int i = 0; i < atoms.size(); i++) {
			QueryAtom atom = atoms.get(i);

			// getting the variables in this atom
			ArrayList<QueryTerm> terms = atom.getTerms();
			for (QueryTerm queryTerm : terms) {

				if (queryTerm instanceof VariableTerm) {
					if ((head_variables.indexOf(queryTerm) == -1)) {
						head_variables.add((VariableTerm) queryTerm);
					}
				} else if (queryTerm instanceof FunctionTerm) {
					FunctionTerm fterm = (FunctionTerm) queryTerm;
					ArrayList<QueryTerm> innnerterms = fterm.getParameters();
					for (QueryTerm innerTerm : innnerterms) {
						if (innerTerm instanceof VariableTerm) {
							if ((head_variables.indexOf(innerTerm) == -1)) {
								head_variables.add((VariableTerm) innerTerm);
							}
						}
					}
				}
			}
		}
	}

	@Override
	/***************************************************************************
	 * 
	 * 
	 * Builds an OntologyQuery object from its String representation
	 * 
	 * @param query
	 * @return The OntologyQuery object from the string or NULL if there's any
	 *         syntax error.
	 * 
	 */
	public ConjunctiveQuery parse(String query, APIController apic) throws QueryParseException {
		// OntologyQueryParserParser parser = null;
		// String input = query;
		// byte currentBytes[] = input.getBytes();
		// ByteArrayInputStream byteArrayInputStream = new
		// ByteArrayInputStream(currentBytes);
		// ANTLRInputStream inputst = null;
		// try {
		// inputst = new ANTLRInputStream(byteArrayInputStream);
		//
		// } catch (IOException e) {
		// e.printStackTrace(System.err);
		// return null;
		// }
		// OntologyQueryParserLexer lexer = new
		// OntologyQueryParserLexer(inputst);
		// CommonTokenStream tokens = new CommonTokenStream(lexer);
		// parser = new OntologyQueryParserParser(tokens);
		// boolean result = false;
		// try {
		// result = parser.parse();
		// } catch (RecognitionException e) {
		// return null;
		// }
		//		
		ArrayList<QueryAtom> atoms = getQueryAtomsFromString(query, apic);

		if ((atoms != null) && (atoms.size() > 0)) {
			ConjunctiveQuery query_object = new ConjunctiveQuery(query, apic);
			// ArrayList<QueryAtom> atoms = parser.getQueryAtoms();
			// for (int i = 0; i < atoms.size(); i++) {
			// query_object.addQueryAtom(atoms.get(i));
			// }
			return query_object;
		} else {
			return null;
		}
	}

	private ArrayList<QueryAtom> getQueryAtomsFromString(String query, APIController apic) throws QueryParseException {
		DatalogCQParser parser = null;
		String input = query;
		byte currentBytes[] = input.getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
		ANTLRInputStream inputst = null;
		try {
			inputst = new ANTLRInputStream(byteArrayInputStream);

		} catch (IOException e) {
			e.printStackTrace(System.err);
			return null;
		}
		DatalogCQLexer lexer = new DatalogCQLexer(inputst);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		parser = new DatalogCQParser(tokens);
		parser.setOBDAAPIController(apic);
		try {
			parser.parse();
		} catch (RecognitionException e) {
			return null;
		}
		if ((parser.getErrors().size() == 0) && (lexer.getErrors().size() == 0)) {
			return parser.getQueryAtoms();
		} else {
			throw new QueryParseException(parser.getErrors().toString());
		}
	}

	// public static boolean isValid(String query) {
	// OntologyQueryParserParser parser = null;
	// String input = query;
	// byte currentBytes[] = input.getBytes();
	// ByteArrayInputStream byteArrayInputStream = new
	// ByteArrayInputStream(currentBytes);
	// try {
	// ANTLRInputStream inputst = new ANTLRInputStream(byteArrayInputStream);
	// OntologyQueryParserLexer lexer = new OntologyQueryParserLexer(inputst);
	//
	// CommonTokenStream tokens = new CommonTokenStream(lexer);
	// parser = new OntologyQueryParserParser(tokens);
	// boolean result = parser.parse();
	// return result;
	//
	// } catch (Exception e) {
	// System.err.println(e.getMessage());
	// return false;
	// }
	// }

	public String toString() {

		if ((atoms == null) || (atoms.size() == 0))
			return "";
		StringBuffer str = new StringBuffer();
		str.append(atoms.get(0).toString());
		for (int i = 1; i < atoms.size(); i++) {
			str.append(", " + atoms.get(i).toString());
		}
		return str.toString();
	}

	public static void main(String[] args) {
//		ConjunctiveQuery query = null;
//		try {
//			query = new ConjunctiveQuery("C(funct(x))");
//
//			System.out.println(query.toString());
//			System.out.println(query.getInputQuString());
//			System.out.println(query.isInputQueryValid());
//			System.out.println("Updating input query");
//
//			query.setInputQuery("C(funct(x)), errror");
//
//			System.out.println(query.toString());
//			System.out.println(query.getInputQuString());
//			System.out.println(query.isInputQueryValid());
//			System.out.println("Updating internal representation");
//
//			query.updateObjectToInputQuery();
//
//			System.out.println(query.toString());
//			System.out.println(query.getInputQuString());
//			System.out.println(query.isInputQueryValid());
//		} catch (QueryParseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}

	@Override
	public ConjunctiveQuery clone() {
		ConjunctiveQuery clone;
		// TODO change so as to really clone each atom/term/etc
		try {
			clone = new ConjunctiveQuery(inputquery, apic);
		} catch (QueryParseException e) {
			// should not happen, since any CQ object should be clonable by
			// definition
			throw new IllegalArgumentException(e);
		}
		return clone;
	}

	@Override
	protected void updateObjectToInputQuery(APIController apic) throws QueryParseException {
		ArrayList<QueryAtom> newatoms = getQueryAtomsFromString(inputquery, apic);
		if (newatoms == null)
			throw new QueryParseException();
		atoms = new ArrayList<QueryAtom>();
		atoms.addAll(newatoms);
	}
}
