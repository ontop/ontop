package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.ComparisonPredicate.Operator;
import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.Literal;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.Selection;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class MappingAnalyzer {

	private ArrayList<OBDAMappingAxiom> mappingList;
	private DBMetadata dbMetaData;

	private SQLQueryTranslator translator;
	
	private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 * 
	 * @param model
	 */
	public MappingAnalyzer(ArrayList<OBDAMappingAxiom> mappingList, DBMetadata dbMetaData) {
		this.mappingList = mappingList;
		this.dbMetaData = dbMetaData;
		
		translator = new SQLQueryTranslator(dbMetaData);
	}

	public DatalogProgram constructDatalogProgram() {
		
		DatalogProgram datalog = dataFactory.getDatalogProgram();
		
		for (OBDAMappingAxiom axiom : mappingList) {
			CQIE targetQuery = (CQIE)axiom.getTargetQuery();
			OBDASQLQuery sourceQuery = (OBDASQLQuery)axiom.getSourceQuery();

			QueryTree queryTree = translator.contructQueryTree(sourceQuery.toString());
			LookupTable lookupTable = createLookupTable(queryTree);
			
			ArrayList<Relation> tableList = queryTree.getTableSet();

			// Construct the body from the source query
			ArrayList<Atom> atoms = new ArrayList<Atom>();
			for (Relation table : tableList) {
				String tableName = table.getName();
				URI predicateName = URI.create(tableName);
				
				int arity = dbMetaData.getDefinition(tableName).countAttribute();
				Predicate predicate = dataFactory.getPredicate(predicateName, arity);

				List<Term> terms = new ArrayList<Term>();
				for (int i = 1; i <= arity; i++) {
					String columnName = dbMetaData.getFullQualifiedAttributeName(tableName, i);
					String termName = lookupTable.lookup(columnName);
					Term term = dataFactory.getVariable(termName);
					terms.add(term);
				}
				Atom atom = dataFactory.getAtom(predicate, terms);
				atoms.add(atom);
			}

			// For the join conditions
			ArrayList<String> joinConditions = queryTree.getJoinCondition();
			for (String predicate : joinConditions) {
				String[] value = predicate.split("=");
				Term t1 = dataFactory.getVariable(lookupTable.lookup(value[0]));
				Term t2 = dataFactory.getVariable(lookupTable.lookup(value[1]));
				Atom atom = dataFactory.getEQAtom(t1, t2);
				atoms.add(atom);
			}
			
			// For the selection "where" clause conditions
			Selection selection = queryTree.getSelection();
			if (selection != null) {
				for (int i = 0; i < selection.conditionSize(); i++) {					
					ComparisonPredicate pred = selection.getCondition(i);					
					IValueExpression left = pred.getValueExpressions()[0];
					IValueExpression right = pred.getValueExpressions()[1];

					String termLeftName = lookupTable.lookup(left.toString());
					Term t1 = dataFactory.getVariable(termLeftName);

					String termRightName = "";
					Term t2 = null;
					if (right instanceof ReferenceValueExpression) {
						termRightName = lookupTable.lookup(right.toString());
						t2 = dataFactory.getVariable(termRightName);
					} else if (right instanceof Literal) {
						Literal literal = (Literal) right;
						termRightName = literal.get().toString();
						t2 = dataFactory.getValueConstant(termRightName);
					}

					Operator op = pred.getOperator();
					
					Atom atom = null;
					switch (op) {
					case EQ:
						atom = dataFactory.getEQAtom(t1, t2);
						break;
					case GT:
						atom = dataFactory.getGTAtom(t1, t2);
						break;
					case LE:
						atom = dataFactory.getLTEAtom(t1, t2);
						break;
					case GE:
						atom = dataFactory.getGTEAtom(t1, t2);
						break;
					case LT:
						atom = dataFactory.getLTAtom(t1, t2);
						break;
					case NE:
						atom = dataFactory.getNEQAtom(t1, t2);
						break;
					}
					atoms.add(atom);
				}
			}

			// Construct the head from the target query.
			List<Atom> atomList = targetQuery.getBody();
			for (Atom atom : atomList) {
				List<Term> terms = atom.getTerms();
				for (Term term : terms) {
					updateTerm(term, lookupTable);
				}
				CQIE rule = dataFactory.getCQIE(atom, atoms);
				datalog.appendRule(rule);
			}
		}
		return datalog;
	}
	
	private void updateTerm(Term term, LookupTable lookupTable) {
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			String termName = lookupTable.lookup(var.getName());
			var.setName(termName);
		} else if (term instanceof Function) {
			Function func = (Function) term;
			List<Term> terms = func.getTerms();
			for (Term innerTerm : terms) {
				updateTerm(innerTerm, lookupTable);
			}
		}
	}
	
	private LookupTable createLookupTable(QueryTree queryTree) {
		LookupTable lookupTable = new LookupTable();

		// Collect all the possible column names from tables.
		ArrayList<Relation> tableList = queryTree.getTableSet();
		for (Relation table : tableList) {
			String tableName = table.getName();
			
			int size = dbMetaData.getDefinition(tableName).countAttribute();

			String[] columnList = new String[2];
			for (int i = 1; i <= size; i ++) {
				columnList[0] = dbMetaData.getAttributeName(tableName, i);
				columnList[1] = dbMetaData.getFullQualifiedAttributeName(tableName, i);
				lookupTable.add(columnList);
			}
		}

		// Add the aliases
		ArrayList<String> aliasMap = queryTree.getAliasMap();
		for (String alias : aliasMap) {
			String[] reference = alias.split("=");
			lookupTable.add(reference[0], reference[1]);
		}

		// Alter the table for the equality in the join condition
//		ArrayList<String> comparisonList = queryTree.getJoinCondition();
//		for (String predicate : comparisonList) {
//			String[] reference = predicate.split("=");
//			lookupTable.asEqualTo(reference[0], reference[1]);
//		}

		return lookupTable;
	}
}
