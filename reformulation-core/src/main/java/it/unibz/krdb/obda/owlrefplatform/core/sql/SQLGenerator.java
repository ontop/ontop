package it.unibz.krdb.obda.owlrefplatform.core.sql;

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

import com.google.common.collect.ImmutableMap;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.model.impl.TermUtils;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.obda.owlrefplatform.core.abox.XsdDatatypeConverter;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.DatalogNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.basicoperations.EQNormalizer;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.DB2SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.obda.parser.EncodeForURI;
import it.unibz.krdb.sql.*;
import org.openrdf.model.Literal;

import java.sql.Types;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


public class SQLGenerator implements SQLQueryGenerator {

	private static final long serialVersionUID = 7477161929752147045L;

	/**
	 * Formatting template
	 */
	private static final String INDENT = "    ";

	private static final String typeStrForSELECT = "%s AS %s";
	private static final String typeSuffix = "QuestType";
	private static final String langStrForSELECT = "%s AS %s";
	private static final String langSuffix = "Lang";
	
	private static final String VIEW_NAME = "QVIEW%s";
	private static final String VIEW_NAME_PREFIX = "QVIEW";

	private final DBMetadata metadata;
	private final SQLDialectAdapter sqladapter;

	private final boolean distinctResultSet;
	private final String replace1, replace2;

	private boolean isDistinct = false;
	private boolean isOrderBy = false;
	
	private final SemanticIndexURIMap uriRefIds; // non-null in the Semantic Index mode
	
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	private final ImmutableMap<ExpressionOperation, String> operations;

	public SQLGenerator(DBMetadata metadata, SQLDialectAdapter sqladapter) {
		this(metadata, sqladapter, false, true, null);
	}

	/**
	 * 
	 * @param metadata
	 * @param sqladapter
	 * @param sqlGenerateReplace
	 * @param uriid is null in case we are not in the SI mode
	 */

	public SQLGenerator(DBMetadata metadata, SQLDialectAdapter sqladapter, boolean sqlGenerateReplace, boolean distinctResultSet, SemanticIndexURIMap uriid) {
		this.metadata = metadata;
		this.sqladapter = sqladapter;
		this.distinctResultSet = distinctResultSet;
		this.uriRefIds = uriid;
		
		ImmutableMap.Builder<ExpressionOperation, String> builder = new ImmutableMap.Builder<ExpressionOperation, String>()
				.put(ExpressionOperation.ADD, "%s + %s")
				.put(ExpressionOperation.SUBTRACT, "%s - %s")
				.put(ExpressionOperation.MULTIPLY, "%s * %s")
				.put(ExpressionOperation.ABS, "ABS(%s)")
				.put(ExpressionOperation.CEIL, sqladapter.ceil())
				.put(ExpressionOperation.FLOOR, "FLOOR(%s)")
				.put(ExpressionOperation.ROUND, sqladapter.round())
				.put(ExpressionOperation.RAND, sqladapter.rand())
				
				.put(ExpressionOperation.EQ, "%s = %s")
				.put(ExpressionOperation.NEQ, "%s <> %s")
				.put(ExpressionOperation.GT, "%s > %s")
				.put(ExpressionOperation.GTE, "%s >= %s")
				.put(ExpressionOperation.LT, "%s < %s")
				.put(ExpressionOperation.LTE, "%s <= %s")
				.put(ExpressionOperation.AND, "%s AND %s")
				.put(ExpressionOperation.OR, "%s OR %s")
				.put(ExpressionOperation.NOT, "NOT %s")
				.put(ExpressionOperation.IS_NULL, "%s IS NULL")
				.put(ExpressionOperation.IS_NOT_NULL, "%s IS NOT NULL")
				.put(ExpressionOperation.IS_TRUE, "%s IS TRUE")
				.put(ExpressionOperation.SQL_LIKE, "%s LIKE %s")
				.put(ExpressionOperation.STR_STARTS, sqladapter.strStartsOperator())
				.put(ExpressionOperation.STR_ENDS, sqladapter.strEndsOperator())
				.put(ExpressionOperation.CONTAINS, sqladapter.strContainsOperator())
				
				.put(ExpressionOperation.NOW, sqladapter.dateNow());
				
		try {
			builder.put(ExpressionOperation.STRUUID, sqladapter.strUuid());
		} catch (UnsupportedOperationException e) {
			// ignore 
		}
		try {
			builder.put(ExpressionOperation.UUID, sqladapter.uuid());
		} catch (UnsupportedOperationException e) {
			// ignore
		}
	
		operations = builder.build();
		
        if (sqlGenerateReplace) {
        	StringBuilder sb1 = new StringBuilder();
        	StringBuilder sb2 = new StringBuilder();
        	for (Entry<String, String> e : EncodeForURI.TABLE.entrySet()) {
        		sb1.append("REPLACE(");
        		sb2.append(", '").append(e.getValue()).append("', '").append(e.getKey()).append("')");
        	}
        	replace1 = sb1.toString();
        	replace2 = sb2.toString();
        } 
        else {
            replace1 = replace2 = "";
        }
	}


	/**
	 * Generates and SQL query ready to be executed by Quest. Each query is a
	 * SELECT FROM WHERE query. To know more about each of these see the inner
	 * method descriptions.
	 */
	@Override
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws OBDAException {
		isDistinct = hasSelectDistinctStatement(query);
		isOrderBy = hasOrderByClause(query);
		if (query.getQueryModifiers().hasModifiers()) {
			final String indent = "   ";
			final String outerViewName = "SUB_QVIEW";
			String subquery = generateQuery(query, signature, indent);

			String modifier = "";
			List<OrderCondition> conditions = query.getQueryModifiers().getSortConditions();
			long limit = query.getQueryModifiers().getLimit();
			long offset = query.getQueryModifiers().getOffset();
			modifier = sqladapter.sqlOrderByAndSlice(conditions,outerViewName,limit, offset) + "\n";

			String sql = "SELECT *\n";
			sql += "FROM (\n";
			sql += subquery + "\n";
			sql += ") " + outerViewName + "\n";
			sql += modifier;
			return sql;
		} else {
			return generateQuery(query, signature, "");
		}
	}

	@Override
	public boolean hasDistinctResultSet() {
		return distinctResultSet;
	}

	private boolean hasSelectDistinctStatement(DatalogProgram query) {
		boolean toReturn = false;
		if (query.getQueryModifiers().hasModifiers()) {
			toReturn = query.getQueryModifiers().isDistinct();
		}
		return toReturn;
	}
	
	private boolean hasOrderByClause(DatalogProgram query) {
		boolean toReturn = false;
		if (query.getQueryModifiers().hasModifiers()) {
			final List<OrderCondition> conditions = query.getQueryModifiers().getSortConditions();
			toReturn = (conditions.isEmpty()) ? false : true;
		}
		return toReturn;
	}

	/**
	 * Main method. Generates the full query, taking into account
	 * limit/offset/order by.
	 */
	private String generateQuery(DatalogProgram query, List<String> signature,
			String indent) throws OBDAException {

		int numberOfQueries = query.getRules().size();

		List<String> queriesStrings = new LinkedList<>();
		/* Main loop, constructing the SPJ query for each CQ */
		for (CQIE cq : query.getRules()) {

			/*
			 * Here we normalize so that the form of the CQ is as close to the
			 * form of a normal SQL algebra as possible, particularly, no shared
			 * variables, only joins by means of equality. Also, equalities in
			 * nested expressions (JOINS) are kept at their respective levels to
			 * generate correct ON and wHERE clauses.
			 */
//			log.debug("Before pushing equalities: \n{}", cq);

			EQNormalizer.enforceEqualities(cq);

//			log.debug("Before folding Joins: \n{}", cq);

			DatalogNormalizer.foldJoinTrees(cq);

//			log.debug("Before pulling out equalities: \n{}", cq);
			
			DatalogNormalizer.pullOutEqualities(cq);
			
//			log.debug("Before pulling out Left Join Conditions: \n{}", cq);
			
			DatalogNormalizer.pullOutLeftJoinConditions(cq);
			
//			log.debug("Before pulling up nested references: \n{}", cq);

			DatalogNormalizer.pullUpNestedReferences(cq);

//			log.debug("Before adding trivial equalities: \n{}, cq);", cq);

			DatalogNormalizer.addMinimalEqualityToLeftJoin(cq);

//			log.debug("Normalized CQ: \n{}", cq);

			Predicate headPredicate = cq.getHead().getFunctionSymbol();
			if (!headPredicate.getName().toString().equals(OBDAVocabulary.QUEST_QUERY)) {
				// not a target query, skip it.
				continue;
			}

			QueryAliasIndex index = new QueryAliasIndex(cq);

			boolean innerdistincts = false;
			if (isDistinct && !distinctResultSet && numberOfQueries == 1) {
				innerdistincts = true;
			}

			String FROM = getFROM(cq, index);
			String WHERE = getWHERE(cq, index);
			String SELECT = getSelectClause(signature, cq, index, innerdistincts);

			String querystr = SELECT + FROM + WHERE;
			queriesStrings.add(querystr);
		}

		Iterator<String> queryStringIterator = queriesStrings.iterator();
		StringBuilder result = new StringBuilder();
		if (queryStringIterator.hasNext()) {
			result.append(queryStringIterator.next());
		}

		String UNION;
		if (isDistinct && !distinctResultSet) {
			UNION = "\nUNION\n";
		} else {
			UNION = "\nUNION ALL\n";
		}
		while (queryStringIterator.hasNext()) {
			result.append(UNION);
			result.append(queryStringIterator.next());
		}

		return result.toString();
	}

	/***
	 * Returns a string with boolean conditions formed with the boolean atoms
	 * found in the atoms list.
	 */
	private Set<String> getBooleanConditionsString(List<Function> atoms, QueryAliasIndex index) {
		Set<String> conditions = new LinkedHashSet<String>();
		for (int atomidx = 0; atomidx < atoms.size(); atomidx++) {
			Term innerAtom = atoms.get(atomidx);
			Function innerAtomAsFunction = (Function) innerAtom;
			if (innerAtomAsFunction.isOperation()) {
				String condition = getSQLCondition(innerAtomAsFunction, index);
				conditions.add(condition);
			}else if (innerAtomAsFunction.isDataTypeFunction()) {
				String condition = getSQLString(innerAtom, index, false);
				conditions.add(condition);
			}
		}
		return conditions;
	}

	/***
	 * Returns the SQL for an atom representing an SQL condition (booleans).
	 */
	private String getSQLCondition(Function atom, QueryAliasIndex index) {
		Predicate functionSymbol = atom.getFunctionSymbol();
		if (atom.getArity() == 1) {
			// For unary boolean operators, e.g., NOT, IS NULL, IS NOT NULL.
			// added also for IS TRUE
			String expressionFormat = operations.get(functionSymbol);
			if (expressionFormat == null)
				throw new RuntimeException("The unary function "  + functionSymbol + " is not supported yet!");

			Term term = atom.getTerm(0);
			String column = getSQLString(term, index, false);
			if (functionSymbol == ExpressionOperation.NOT) {
				if (term instanceof Function) {
					Function f = (Function) term;
					if (!f.isDataTypeFunction()) 
						return String.format(expressionFormat, column);
				}
				int type = getVariableDataType(term); // ROMAN (23 Dec 2015): the lists of types are incomplete
				if (type == Types.INTEGER || type == Types.BIGINT || type == Types.FLOAT || type == Types.DOUBLE) 
					return String.format("NOT %s > 0", column);
				else if (type == Types.BOOLEAN) 
					return String.format("NOT %s", column);
				else if (type == Types.VARCHAR) 
					return String.format("NOT LENGTH(%s) > 0", column);
				return "0;";
			}
			else if (functionSymbol == ExpressionOperation.IS_TRUE) {
				int type = getVariableDataType(term); // ROMAN (23 Dec 2015): the lists of types are incomplete
				if (type == Types.INTEGER || type == Types.BIGINT || type == Types.FLOAT || type == Types.DOUBLE) 
					return String.format("%s > 0", column);
				else if (type == Types.BOOLEAN) 
					return String.format("%s", column);
				else if (type == Types.VARCHAR) 
					return String.format("LENGTH(%s) > 0", column);
				return "1;";
			}
			else
				return String.format(expressionFormat, column);
		} 
		else if (atom.getArity() == 2) {
			if (operations.containsKey(functionSymbol)) {
				// For binary operators, e.g., AND, OR, EQ, GT, LT, etc. _
				String expressionFormat = operations.get(functionSymbol);
				Term left = atom.getTerm(0);
				Term right = atom.getTerm(1);
				String leftOp = getSQLString(left, index, true);
				String rightOp = getSQLString(right, index, true);
				return String.format("(" + expressionFormat + ")", leftOp, rightOp);
			} 
			else 
				throw new RuntimeException("The binary function "  + functionSymbol + " is not supported yet!");
		} 
		else {
			if (functionSymbol == ExpressionOperation.REGEX) {
				boolean caseinSensitive = false;
				boolean multiLine = false;
				boolean dotAllMode = false;
				if (atom.getArity() == 3) {
					String flags = atom.getTerm(2).toString();
					if (flags.contains("i")) 
						caseinSensitive = true;
					if (flags.contains("m")) 
						multiLine = true;
					if (flags.contains("s")) 
						dotAllMode = true;
				}
				String column = getSQLString(atom.getTerm(0), index, false);
				String pattern = getSQLString(atom.getTerm(1), index, false);
				return sqladapter.sqlRegex(column, pattern, caseinSensitive, multiLine, dotAllMode);
			} 
			else {
				throw new RuntimeException("The builtin function " + functionSymbol + " is not supported yet!");
			}
		}
	}

	/**
	 * Returns the table definition for these atoms. By default, a list of atoms
	 * represents JOIN or LEFT JOIN of all the atoms, left to right. All boolean
	 * atoms in the list are considered conditions in the ON clause of the JOIN.
	 * 
	 * <p>
	 * If the list is a LeftJoin, then it can only have 2 data atoms, and it HAS
	 * to have 2 data atoms.
	 * 
	 * <p>
	 * If process boolean operators is enabled, all boolean conditions will be
	 * added to the ON clause of the first JOIN.
	 * 
	 * @param atoms
	 * @param index
	 * @param isTopLevel
	 *            indicates if the list of atoms is actually the main body of
	 *            the conjunctive query. If it is, no JOIN is generated, but a
	 *            cross product with WHERE clause. Moreover, the isLeftJoin
	 *            argument will be ignored.
	 * 
	 * @return
	 */
	private String getTableDefinitions(List<Function> atoms,
			QueryAliasIndex index, boolean isTopLevel, boolean isLeftJoin,
			String indent) {
		/*
		 * We now collect the view definitions for each data atom each
		 * condition, and each each nested Join/LeftJoin
		 */
		List<String> tableDefinitions = new LinkedList<>();
		for (Function a : atoms) {
			String definition = getTableDefinition(a, index, indent + INDENT);
			if (!definition.isEmpty()) {
				tableDefinitions.add(definition);
			}
		}

		/*
		 * Now we generate the table definition, this will be either a comma
		 * separated list for TOP level (FROM clause) or a Join/LeftJoin
		 * (possibly nested if there are more than 2 table definitions in the
		 * current list) in case this method was called recursively.
		 */
		StringBuilder tableDefinitionsString = new StringBuilder();

		int size = tableDefinitions.size();
		if (isTopLevel) {
			if (size == 0) {
				tableDefinitionsString.append("(" + sqladapter.getDummyTable() + ") tdummy ");
				
			} else {
			Iterator<String> tableDefinitionsIterator = tableDefinitions.iterator();
			tableDefinitionsString.append(indent);
			tableDefinitionsString.append(tableDefinitionsIterator.next());
			while (tableDefinitionsIterator.hasNext()) {
				tableDefinitionsString.append(",\n");
				tableDefinitionsString.append(indent);
				tableDefinitionsString.append(tableDefinitionsIterator.next());
			}
			}
		} else {
			/*
			 * This is actually a Join or LeftJoin, so we form the JOINs/LEFT
			 * JOINs and the ON clauses
			 */
			String JOIN_KEYWORD;
			if (isLeftJoin) {
				JOIN_KEYWORD = "LEFT OUTER JOIN";
			} else {
				JOIN_KEYWORD = "JOIN";
			}
			String JOIN = "\n" + indent + "(\n" + indent + "%s\n" + indent
					+ JOIN_KEYWORD + "\n" + indent + "%s\n" + indent + ")";

			if (size == 0) {
				throw new RuntimeException("Cannot generate definition for empty data");
			}
			if (size == 1) {
				return tableDefinitions.get(0);
			}

			/*
			 * To form the JOIN we will cycle through each data definition,
			 * nesting the JOINs as we go. The conditions in the ON clause will
			 * go on the TOP level only.
			 */
			String currentJoin = String.format(JOIN,
					tableDefinitions.get(size - 2),
					tableDefinitions.get(size - 1));
			tableDefinitions.remove(size - 1);
			tableDefinitions.remove(size - 2);

			int currentSize = tableDefinitions.size();
			while (currentSize > 0) {
				currentJoin = String.format(JOIN, tableDefinitions.get(currentSize - 1), currentJoin);
				tableDefinitions.remove(currentSize - 1);
				currentSize = tableDefinitions.size();
			}
			tableDefinitions.add(currentJoin);

			tableDefinitionsString.append(currentJoin);
			/*
			 * If there are ON conditions we add them now. We need to remove the
			 * last parenthesis ')' and replace it with ' ON %s)' where %s are
			 * all the conditions
			 */
			String conditions = getConditionsString(atoms, index, true, indent);

			if (conditions.length() > 0 && tableDefinitionsString.lastIndexOf(")") != -1) {
				int lastidx = tableDefinitionsString.lastIndexOf(")");
				tableDefinitionsString.delete(lastidx, tableDefinitionsString.length());
				String ON_CLAUSE = String.format("ON\n%s\n " + indent + ")", conditions);
				tableDefinitionsString.append(ON_CLAUSE);
			}
		}
		return tableDefinitionsString.toString();
	}

	/**
	 * Returns the table definition for the given atom. If the atom is a simple
	 * table or view, then it returns the value as defined by the
	 * QueryAliasIndex. If the atom is a Join or Left Join, it will call
	 * getTableDefinitions on the nested term list.
	 */
	private String getTableDefinition(Function atom, QueryAliasIndex index, String indent) {
		if (atom.isOperation() || atom.isDataTypeFunction()) {
			// These don't participate in the FROM clause
			return "";
		} 
		else if (atom.isAlgebraFunction()) {
			List<Function> innerTerms = new ArrayList<>(atom.getTerms().size());
			for (Term innerTerm : atom.getTerms()) {
				innerTerms.add((Function) innerTerm);
			}
			Predicate predicate = atom.getFunctionSymbol();
			if (predicate == OBDAVocabulary.SPARQL_JOIN) {
				return getTableDefinitions(innerTerms, index, false, false, indent + INDENT);
			} else if (predicate == OBDAVocabulary.SPARQL_LEFTJOIN) {
				return getTableDefinitions(innerTerms, index, false, true, indent + INDENT);
			}
		}

		/*
		 * This is a data atom
		 */
		String def = index.getViewDefinition(atom);
		return def;
	}

	private String getFROM(CQIE query, QueryAliasIndex index) {
		String tableDefinitions = getTableDefinitions(query.getBody(), index, true, false, "");
		return "\n FROM \n" + tableDefinitions;
	}

	/**
	 * Generates all the conditions on the given atoms, e.g., shared variables
	 * and boolean conditions. This string can then be used to form a WHERE or
	 * an ON clause.
	 * 
	 * <p>
	 * The method assumes that no variable in this list (or nested ones) referes
	 * to an upper level one.
	 */
	private String getConditionsString(List<Function> atoms,
			QueryAliasIndex index, boolean processShared, String indent) {

		Set<String> equalityConditions = new LinkedHashSet<>();

		// if (processShared)
		equalityConditions.addAll(getConditionsSharedVariablesAndConstants(atoms, index, processShared));
		Set<String> booleanConditions = getBooleanConditionsString(atoms, index);

		Set<String> conditions = new LinkedHashSet<>();
		conditions.addAll(equalityConditions);
		conditions.addAll(booleanConditions);

		/*
		 * Collecting all the conditions in a single string for the ON or WHERE
		 * clause
		 */
		StringBuilder conditionsString = new StringBuilder();
		Iterator<String> conditionsIterator = conditions.iterator();
		if (conditionsIterator.hasNext()) {
			conditionsString.append(indent);
			conditionsString.append(conditionsIterator.next());
		}
		while (conditionsIterator.hasNext()) {
			conditionsString.append(" AND\n");
			conditionsString.append(indent);
			conditionsString.append(conditionsIterator.next());
		}
		return conditionsString.toString();
	}

	/**
	 * Returns the set of variables that participate data atoms (either in this
	 * atom directly or in nested ones). This will recursively collect the
	 * variables references in in this atom, exlcuding those on the right side
	 * of left joins.
	 * 
	 * @param atom
	 * @return
	 */
	private Set<Variable> getVariableReferencesWithLeftJoin(Function atom) {
		
		if (atom.isDataFunction()) {
			Set<Variable> variables = new LinkedHashSet<>();
			TermUtils.addReferencedVariablesTo(variables, atom);
			return variables;
		}
		else if (atom.isOperation()) {
			return Collections.emptySet();
		}
		else if (atom.isDataTypeFunction()) {
			return Collections.emptySet();
		}
		/*
		 * we have an alebra opertaor (join or left join) if its a join, we need
		 * to collect all the varaibles of each nested atom., if its a left
		 * join, only of the first data/algebra atom (the left atom).
		 */
		boolean isLeftJoin = false;
		boolean foundFirstDataAtom = false;

		if (atom.getFunctionSymbol() == OBDAVocabulary.SPARQL_LEFTJOIN) {
			isLeftJoin = true;
		}
		Set<Variable> innerVariables = new LinkedHashSet<>();
		for (Term t : atom.getTerms()) {
			if (isLeftJoin && foundFirstDataAtom) {
				break;
			}
			Function asFunction = (Function) t;
			if (asFunction.isOperation()) {
				continue;
			}
			innerVariables.addAll(getVariableReferencesWithLeftJoin(asFunction));
			foundFirstDataAtom = true;
		}
		return innerVariables;

	}

	/**
	 * Returns a list of equality conditions that reflect the semantics of the
	 * shared variables in the list of atoms.
	 * <p>
	 * The method assumes that no variables are shared across deeper levels of
	 * nesting (through Join or LeftJoin atoms), it will not call itself
	 * recursively. Nor across upper levels.
	 * 
	 * <p>
	 * When generating equalities recursively, we will also generate a minimal
	 * number of equalities. E.g., if we have A(x), Join(R(x,y), Join(R(y,
	 * x),B(x))
	 * 
	 */
	private Set<String> getConditionsSharedVariablesAndConstants(
			List<Function> atoms, QueryAliasIndex index, boolean processShared) {
		Set<String> equalities = new LinkedHashSet<>();

		Set<Variable> currentLevelVariables = new LinkedHashSet<>();
		if (processShared) {
			for (Function atom : atoms) {
				currentLevelVariables.addAll(getVariableReferencesWithLeftJoin(atom));
			}
		}
		
		/*
		 * For each variable we collect all the columns that should be equated
		 * (due to repeated positions of the variable). then we form atoms of
		 * the form "COL1 = COL2"
		 */
		for (Variable var : currentLevelVariables) {
			Set<QualifiedAttributeID> references = index.getColumnReferences(var);
			if (references.size() < 2) {
				// No need for equality
				continue;
			}
			Iterator<QualifiedAttributeID> referenceIterator = references.iterator();
			QualifiedAttributeID leftColumnReference = referenceIterator.next();
			while (referenceIterator.hasNext()) {
				QualifiedAttributeID rightColumnReference = referenceIterator.next();
				String leftColumnString = leftColumnReference.getSQLRendering();
				String rightColumnString = rightColumnReference.getSQLRendering();
				String equality = String.format("(%s = %s)", leftColumnString, rightColumnString);
				equalities.add(equality);
				leftColumnReference = rightColumnReference;
			}
		}

		for (Function atom : atoms) {
			if (!atom.isDataFunction()) {
				continue;
			}
			for (int idx = 0; idx < atom.getArity(); idx++) {
				Term l = atom.getTerm(idx);
				if (l instanceof Constant) {
					String value = getSQLString(l, index, false);
					String columnReference = index.getColumnReference(atom, idx);
					equalities.add(String.format("(%s = %s)", columnReference, value));
				}
			}

		}
		return equalities;
	}

	
	// return variable SQL data type
	private int getVariableDataType (Term term) {
		Function f = (Function) term;
		if (f.isDataTypeFunction()) {
			Predicate p = f.getFunctionSymbol();
			Predicate.COL_TYPE type = dtfac.getDatatype(p.toString());
			return OBDADataFactoryImpl.getInstance().getJdbcTypeMapper().getSQLType(type);
		}
		// Return varchar for unknown
		return Types.VARCHAR;
	}

	private String getWHERE(CQIE query, QueryAliasIndex index) {
		String conditions = getConditionsString(query.getBody(), index, false, "");
		if (conditions.length() == 0) {
			return "";
		}
		return "\nWHERE \n" + conditions;
	}

	/**
	 * produces the select clause of the sql query for the given CQIE
	 * 
	 * @param query
	 *            the query
	 * @return the sql select clause
	 */
	private String getSelectClause(List<String> signature, CQIE query,
			QueryAliasIndex index, boolean distinct) throws OBDAException {
		/*
		 * If the head has size 0 this is a boolean query.
		 */
		List<Term> headterms = query.getHead().getTerms();
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT ");
		if (distinct && !distinctResultSet) {
			sb.append("DISTINCT ");
		}
		//Only for ASK
		if (headterms.size() == 0) {
			sb.append("'true' as x");
			return sb.toString();
		}

		/**
		 * Set that contains all the variable names created on the top query.
		 * It helps the dialect adapter to generate variable names according to its possible restrictions.
		 * Currently, this is needed for the Oracle adapter (max. length of 30 characters).
		 */
		Set<String> sqlVariableNames = new HashSet<>();

		Iterator<Term> hit = headterms.iterator();
		int hpos = 0;
		while (hit.hasNext()) {

			Term ht = hit.next();
			String typeColumn = getTypeColumnForSELECT(ht, signature, hpos, sqlVariableNames);
			String langColumn = getLangColumnForSELECT(ht, signature, hpos,	index, sqlVariableNames);
			String mainColumn = getMainColumnForSELECT(ht, signature, hpos, index, sqlVariableNames);

			sb.append("\n   ");
			sb.append(typeColumn);
			sb.append(", ");
			sb.append(langColumn);
			sb.append(", ");
			sb.append(mainColumn);
			if (hit.hasNext()) {
				sb.append(", ");
			}
			hpos++;
		}
		return sb.toString();
	}

	private String getMainColumnForSELECT(Term ht,
			List<String> signature, int hpos, QueryAliasIndex index, Set<String> sqlVariableNames) {

		/**
		 * Creates a variable name that fits to the restrictions of the SQL dialect.
		 */
		String variableName = sqladapter.nameTopVariable(signature.get(hpos), "", sqlVariableNames);
		sqlVariableNames.add(variableName);

		String mainColumn = null;

		String mainTemplate = "%s AS %s";

		if (ht instanceof URIConstant) {
			URIConstant uc = (URIConstant) ht;
			mainColumn = sqladapter.getSQLLexicalFormString(uc.getURI());
		} 
		else if (ht == OBDAVocabulary.NULL) {
			mainColumn = "NULL";
		} 
		else if (ht instanceof Function) {
			/*
			 * if it's a function we need to get the nested value if its a
			 * datatype function or we need to do the CONCAT if its URI(....).
			 */
			Function ov = (Function) ht;
			Predicate function = ov.getFunctionSymbol();

			/*
			 * Adding the column(s) with the actual value(s)
			 */
			if (ov.isDataTypeFunction()) {
				/*
				 * Case where we have a typing function in the head (this is the
				 * case for all literal columns
				 */
				int size = ov.getTerms().size();
				if ((function instanceof Literal) || size > 2 ) {
					mainColumn = getSQLStringForTemplateFunction(ov, index);
				}
				else {
					Term term = ov.getTerms().get(0);
					if (term instanceof ValueConstant) {
						mainColumn = getSQLLexicalForm((ValueConstant) term);
					} else {
						mainColumn = getSQLString(term, index, false);
					}
				}
			}
			else if (function instanceof URITemplatePredicate) {
				// New template based URI building functions
				mainColumn = getSQLStringForTemplateFunction(ov, index);
			}
			else if (function instanceof BNodePredicate) {
				// New template based BNODE building functions
				mainColumn = getSQLStringForTemplateFunction(ov, index);
			}
			else if (ov.isOperation()) {
				mainColumn = getSQLString(ov, index, false); 
			}
            else 
				throw new IllegalArgumentException("Error generating SQL query. Found an invalid function during translation: " + ov);
		} 
		else 
			throw new RuntimeException("Cannot generate SELECT for term: " + ht);

		/*
		 * If we have a column we need to still CAST to VARCHAR
		 */
		if (mainColumn.charAt(0) != '\'' && mainColumn.charAt(0) != '(') {
			if (!isStringColType(ht, index)) {
				mainColumn = sqladapter.sqlCast(mainColumn, Types.VARCHAR);
			}
		}
		return String.format(mainTemplate, mainColumn, variableName);
	}
	
	private String getLangColumnForSELECT(Term ht, List<String> signature, int hpos, QueryAliasIndex index,
										  Set<String> sqlVariableNames) {

		/**
		 * Creates a variable name that fits to the restrictions of the SQL dialect.
		 */
		String langVariableName = sqladapter.nameTopVariable(signature.get(hpos), langSuffix, sqlVariableNames);
		sqlVariableNames.add(langVariableName);

		if (ht instanceof Function) {
            Function ov = (Function) ht;
            String lang = getLangType(ov, index);
            return (String.format(langStrForSELECT, lang, langVariableName));
        }

        return  (String.format(langStrForSELECT, "NULL", langVariableName));

	}
	
	/** 
	 * http://www.w3.org/TR/sparql11-query/#idp1915512
	 * 
	 * Functions that return a string literal do so with the string literal of the same kind as the first argument 
	 * (simple literal, plain literal with same language tag, xsd:string). This includes SUBSTR, STRBEFORE and STRAFTER.
	 * 
	 * The function CONCAT returns a string literal based on the details of all its arguments.
	 * 
	 * @param func1
	 * @param index
	 * @return
	 */

    private String getLangType(Function func1, QueryAliasIndex index) {

        Predicate pred1 = func1.getFunctionSymbol();

        if (dtfac.isLiteral(pred1) && func1.getArity() == 2) {
            Term langTerm = func1.getTerm(1);
            if (langTerm == OBDAVocabulary.NULL) {
                return  "NULL";
            } 
            else if (langTerm instanceof ValueConstant) {
                return getSQLLexicalForm((ValueConstant) langTerm);
            } 
            else {
                return getSQLString(langTerm, index, false);
            }
        }
        else if (func1.isOperation()) {
            if (pred1 == ExpressionOperation.CONCAT) {
                Term term1 = func1.getTerm(0);
                Term term2 = func1.getTerm(1);

                if (term1 instanceof Function && term2 instanceof Function) {
                    String lang1 = getLangType((Function) term1, index);
                    String lang2 = getLangType((Function) term2, index);
                    if (lang1.equals(lang2)) 
                        return lang1;
                    else 
                    	return "NULL";
                }
            }
            else if (pred1 == ExpressionOperation.REPLACE || pred1 == ExpressionOperation.SUBSTR ||
            		pred1 == ExpressionOperation.UCASE || pred1 == ExpressionOperation.LCASE ||
            		pred1 == ExpressionOperation.STRBEFORE || pred1 == ExpressionOperation.STRAFTER) {
                Term rep1 = func1.getTerm(0);
                if (rep1 instanceof Function) {
                    String lang1 = getLangType((Function) rep1, index);
                    return lang1;
                }
            }
            return "NULL";
        }
        return "NULL";
    }

	

	/**
	 * Beware: a new entry will be added to sqlVariableNames (is thus mutable).
	 */
	private String getTypeColumnForSELECT(Term ht, List<String> signature, int hpos,
										  Set<String> sqlVariableNames) {
		

		COL_TYPE type = getTypeColumn(ht);
		int code = type.getQuestCode();
		
        // Creates a variable name that fits to the restrictions of the SQL dialect.
        String typeVariableName = sqladapter.nameTopVariable(signature.get(hpos), typeSuffix, sqlVariableNames);
        sqlVariableNames.add(typeVariableName);    

        return String.format(typeStrForSELECT, code, typeVariableName);
	}

    private COL_TYPE getTypeColumn(Term ht) {
        COL_TYPE type;

        if (ht instanceof Function) {
			Function ov = (Function) ht;
			Predicate function = ov.getFunctionSymbol();

			/*
			 * Adding the ColType column to the projection (used in the result
			 * set to know the type of constant)
			 *
			 * NOTE NULL is IDENTIFIER 0 in QuestResultSet do not USE for any
			 * type
			 */

			if (function instanceof URITemplatePredicate) {
				type = COL_TYPE.OBJECT;
			} 
			else if (function instanceof BNodePredicate) {
				type = COL_TYPE.BNODE;
			} 
			else if (ov.isOperation()) {
				if (function == ExpressionOperation.CONCAT) {
					COL_TYPE type1 = getTypeColumn(ov.getTerm(0));
					COL_TYPE type2 = getTypeColumn(ov.getTerm(1));
					if (type1 == COL_TYPE.STRING && type2 == COL_TYPE.STRING) 
						type = COL_TYPE.STRING; 
					else 
						type = COL_TYPE.LITERAL;
				} 
				else if (function == ExpressionOperation.REPLACE || function == ExpressionOperation.SUBSTR || 
						function == ExpressionOperation.LCASE || function == ExpressionOperation.UCASE || 
						function == ExpressionOperation.STRBEFORE || function == ExpressionOperation.STRAFTER) {
					// ROMAN (23 Dec 2015): STRBEFORE AND STRAFTER RETURN THE TYPE OF THE FIRST ARGUMENT
					COL_TYPE type1 = getTypeColumn(ov.getTerm(0));
					if (type1 == COL_TYPE.STRING) 
						type = COL_TYPE.STRING;
					else 
						type = COL_TYPE.LITERAL;
				} 
				else if (function == ExpressionOperation.ABS || function == ExpressionOperation.ROUND ||
							function == ExpressionOperation.CEIL || function == ExpressionOperation.FLOOR) {
					type = getTypeColumn(ov.getTerm(0));
				}
				else if (function == ExpressionOperation.ADD || function == ExpressionOperation.SUBTRACT || 
						function == ExpressionOperation.MULTIPLY || function == ExpressionOperation.DIVIDE) {
					// ROMAN (23 Dec 2015): TODO implement proper type promotion 
					type = COL_TYPE.LITERAL;
				}
				else {
					type = ((ExpressionOperation)function).getExpressionType();
				}
			} 
			else {
				type = function.getType(0);
			}
		}
        else if (ht instanceof URIConstant) {
            type = COL_TYPE.OBJECT;
		}
		else if (ht == OBDAVocabulary.NULL) {  // NULL is an instance of ValueConstant
            type = COL_TYPE.NULL;
        }
        else
            throw new RuntimeException("Cannot generate SELECT for term: " + ht);


        return type;
    }


    public String getSQLStringForTemplateFunction(Function ov, QueryAliasIndex index) {
		/*
		 * The first inner term determines the form of the result
		 */
		Term t = ov.getTerms().get(0);
	
		if (t instanceof ValueConstant || t instanceof BNode) {
			/*
			 * The function is actually a template. The first parameter is a
			 * string of the form http://.../.../ or empty "{}" with place holders of the form
			 * {}. The rest are variables or constants that should be put in
			 * place of the place holders. We need to tokenize and form the
			 * CONCAT
			 */
			String literalValue;			
			if (t instanceof BNode) {
				literalValue = ((BNode) t).getName();
			} else {
				literalValue = ((ValueConstant) t).getValue();
			}		


            String template = trimLiteral(literalValue);
            
			String[] split = template.split("[{][}]");
			
			List<String> vex = new LinkedList<>();
			if (split.length > 0 && !split[0].isEmpty()) {
				vex.add(sqladapter.getSQLLexicalFormString(split[0]));
			}
			
			/*
			 * New we concat the rest of the function, note that if there is only 1 element
			 * there is nothing to concatenate
			 */
			if (ov.getTerms().size() > 1) {
				int size = ov.getTerms().size();
				Predicate pred = ov.getFunctionSymbol();
				if (dtfac.isLiteral(pred)) {
					size--;
				}
				for (int termIndex = 1; termIndex < size; termIndex++) {
					Term currentTerm = ov.getTerms().get(termIndex);
					String repl;

					if (isStringColType(currentTerm, index)) {
						//empty place holders: the correct uri is in the column of DB no need to replace
						if(split.length == 0)
						{
							repl = getSQLString(currentTerm, index, false) ;
						}
						else
						{
							repl = replace1 + (getSQLString(currentTerm, index, false)) + replace2;
						}

					} else {
						if(split.length == 0)
						{
							repl = sqladapter.sqlCast(getSQLString(currentTerm, index, false), Types.VARCHAR) ;
						}
						else {
							repl = replace1 + sqladapter.sqlCast(getSQLString(currentTerm, index, false), Types.VARCHAR) + replace2;
						}
					}
					vex.add(repl);
					if (termIndex < split.length) {
						vex.add(sqladapter.getSQLLexicalFormString(split[termIndex]));
					}
				}
			}
		
			if (vex.size() == 1) {	
				return vex.get(0);
			}
			return getStringConcatenation(vex.toArray(new String[]{}));
			
		} 
		else if (t instanceof Variable) {
			/*
			 * The function is of the form uri(x), we need to simply return the
			 * value of X
			 */
			return sqladapter.sqlCast(getSQLString(t, index, false), Types.VARCHAR);
			
		} 
		else if (t instanceof URIConstant) {
			/*
			 * The function is of the form uri("http://some.uri/"), i.e., a
			 * concrete URI, we return the string representing that URI.
			 */
			URIConstant uc = (URIConstant) t;
			return sqladapter.getSQLLexicalFormString(uc.getURI());
		}

		/*
		 * Unsupported case
		 */
		throw new IllegalArgumentException("Error, cannot generate URI constructor clause for a term: " + ov);

	}

	// TODO: move to SQLAdapter
	private String getStringConcatenation(String[] params) {
		String toReturn = sqladapter.strConcat(params);
		if (sqladapter instanceof DB2SQLDialectAdapter) {
			/*
			 * A work around to handle DB2 (>9.1) issue SQL0134N: Improper use of a string column, host variable, constant, or function name.
			 * http://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic=%2Fcom.ibm.db2.luw.messages.sql.doc%2Fdoc%2Fmsql00134n.html
			 */
			if (isDistinct || isOrderBy) {
				return sqladapter.sqlCast(toReturn, Types.VARCHAR);
			}
		}
		return toReturn;
	}

	private boolean isStringColType(Term term, QueryAliasIndex index) {
		if (term instanceof Function) {
			Function function = (Function) term;
			if (function.getFunctionSymbol() instanceof URITemplatePredicate) {
				/*
				 * A URI function always returns a string, thus it is a string column type.
				 */
				return (uriRefIds == null); // uriRefIds is non-null in the Semantic Index mode
			} 
			else {
				if (function.getArity() == 1) {

					/*
					 * Update the term with the parent term's first parameter.
					 * Note: this method is confusing :(
					 */
					term = function.getTerm(0);
					return isStringColType(term, index);
				}
			}
		} 
		else if (term instanceof Variable) {
			Set<QualifiedAttributeID> viewdef = index.getColumnReferences((Variable) term);
			QualifiedAttributeID def = viewdef.iterator().next();
			QuotedID attributeId = def.getAttribute();
			RelationID tableId = null;
			// ROMAN (8 Oct 2015)
			// case conversion to be removed
			if (def.getRelation().getTableName().toUpperCase().startsWith(VIEW_NAME_PREFIX)) {
				for (Map.Entry<Function, RelationID> entry : index.viewNames.entrySet()) {
					RelationID value = entry.getValue();
					if (value.equals(def.getRelation())) {
						tableId = Relation2DatalogPredicate
									.createRelationFromPredicateName(metadata.getQuotedIDFactory(), entry.getKey().getFunctionSymbol());
						break;
					}
				}
			}
			DatabaseRelationDefinition table = metadata.getDatabaseRelation(tableId);
			if (table != null) {
				// ROMAN (15 Oct 2015): i'm not sure what to do if it is a view (i.e., a complex subquery)
	 			Attribute a = table.getAttribute(attributeId);
				switch (a.getType()) {
					case Types.VARCHAR:
					// case Types.CHAR: // ROMAN (10 Oct 2015) -- otherwise PgsqlDatatypeTest.all fails 
					case Types.LONGNVARCHAR:
					case Types.LONGVARCHAR:
					case Types.NVARCHAR:
					case Types.NCHAR:
						return true;
					default:
						return false;
				}
			}
		}
		return false;
	}

	private static final Pattern pQuotes = Pattern.compile("[\"`\\['][^\\.]*[\"`\\]']");
	
	private static String trimLiteral(String string) {
		while (pQuotes.matcher(string).matches()) {
			string = string.substring(1, string.length() - 1);
		}
		return string;
	}

	/**
	 * Generates the SQL string that forms or retrieves the given term. The
	 * function takes as input either: a constant (value or URI), a variable, or
	 * a Function (i.e., uri(), eq(..), ISNULL(..), etc)).
	 * <p>
	 * If the input is a constant, it will return the SQL that generates the
	 * string representing that constant.
	 * <p>
	 * If its a variable, it returns the column references to the position where
	 * the variable first appears.
	 * <p>
	 * If its a function uri(..) it returns the SQL string concatenation that
	 * builds the result of uri(...)
	 * <p>
	 * If its a boolean comparison, it returns the corresponding SQL comparison.
	 */
	public String getSQLString(Term term, QueryAliasIndex index, boolean useBrackets) {
		if (term == null) {
			return "";
		}
		if (term instanceof ValueConstant) {
			ValueConstant ct = (ValueConstant) term;
			if (uriRefIds != null) { // in the Semantic Index mode
				if (ct.getType() == COL_TYPE.OBJECT || ct.getType() == COL_TYPE.LITERAL) {
					int id = uriRefIds.getId(ct.getValue());
					if (id >= 0)
						return sqladapter.getSQLLexicalFormString(String.valueOf(id));
				}
			}
			return getSQLLexicalForm(ct);
		} 
		else if (term instanceof URIConstant) {
			if (uriRefIds != null) { // in the Semantic Index mode
				String uri = term.toString();
				int id = uriRefIds.getId(uri);
				return sqladapter.getSQLLexicalFormString(String.valueOf(id));
			}
			URIConstant uc = (URIConstant) term;
			return sqladapter.getSQLLexicalFormString(uc.toString());
		} 
		else if (term instanceof Variable) {
			Variable var = (Variable) term;
			Set<QualifiedAttributeID> posList = index.getColumnReferences(var);
			if (posList == null || posList.size() == 0) {
				throw new RuntimeException("Unbound variable found in WHERE clause: " + term);
			}
			return posList.iterator().next().getSQLRendering();
		}

		/* If its not constant, or variable its a function */

		Function function = (Function) term;
		Predicate functionSymbol = function.getFunctionSymbol();

		if (function.isDataTypeFunction()) {
			if (functionSymbol.getType(0) == COL_TYPE.UNSUPPORTED) {
				throw new RuntimeException("Unsupported type in the query: " + function);
			}
			if (function.getArity() == 1) {
				// atoms of the form integer(x)
				return getSQLString(function.getTerm(0), index, false);
			} else 	{
				return getSQLStringForTemplateFunction(function, index);
			}
		} 
		else if (operations.containsKey(functionSymbol)) {
			// atoms of the form EQ(x,y)
			String expressionFormat = operations.get(functionSymbol);
			if (function.getArity() == 1) {
				// for unary functions, e.g., NOT, IS NULL, IS NOT NULL
				// also added for IS TRUE
				Term term1 = function.getTerm(0);
				if (functionSymbol == ExpressionOperation.IS_TRUE) {
					// find data type of term and evaluate accordingly
					String column = getSQLString(term1, index, false);
					int type = getVariableDataType(term1);
					if (type == Types.INTEGER || type == Types.BIGINT || type == Types.DOUBLE || type == Types.FLOAT) 
						return String.format("%s > 0", column);
					else if (type == Types.BOOLEAN) 
						return String.format("%s", column);
					else if (type == Types.VARCHAR) 
						return String.format("LENGTH(%s) > 0", column);
					return "1";
				}
				String op = getSQLString(term1, index, true);
				return String.format(expressionFormat, op);		
			} 
			else if (function.getArity() == 2) {
				// for binary functions, e.g., AND, OR, EQ, NEQ, GT etc.
				String leftOp = getSQLString(function.getTerm(0), index, true);
				String rightOp = getSQLString(function.getTerm(1), index, true);
				String result = String.format(expressionFormat, leftOp, rightOp);
				if (useBrackets)
					return String.format("(%s)", result);
				else 
					return result;
			} 
			else if (function.getArity() == 0) { 
				return expressionFormat;
			}
			else
				throw new RuntimeException("Cannot translate boolean function: " + functionSymbol);			
		} 
		else {
			if (functionSymbol == ExpressionOperation.REGEX) {
				boolean caseinSensitive = false;
				boolean multiLine = false;
				boolean dotAllMode = false;
				if (function.getArity() == 3) {
					String flags = function.getTerm(2).toString();
					if (flags.contains("i")) 
						caseinSensitive = true;
					if (flags.contains("m")) 
						multiLine = true;
					if (flags.contains("s")) 
						dotAllMode = true;
				}
				
				String column = getSQLString(function.getTerm(0), index, false);
				String pattern = getSQLString(function.getTerm(1), index, false);
				return sqladapter.sqlRegex(column, pattern, caseinSensitive, multiLine, dotAllMode);
			}
			else if (functionSymbol == ExpressionOperation.QUEST_CAST) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				String datatype = ((Constant) function.getTerm(1)).getValue();
				int sqlDatatype = -1;
				if (datatype.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
					sqlDatatype = Types.VARCHAR;
				}
				if (isStringColType(function, index)) {
					return columnName;
				} else {
					return sqladapter.sqlCast(columnName, sqlDatatype);
				}
			} 
			else if (functionSymbol == ExpressionOperation.SPARQL_STR) {
				String columnName = getSQLString(function.getTerm(0), index, false);
				if (isStringColType(function, index)) {
					return columnName;
				} else {
					return sqladapter.sqlCast(columnName, Types.VARCHAR);
				}
			} 
			else if (functionSymbol == ExpressionOperation.REPLACE) {
				String orig = getSQLString(function.getTerm(0), index, false);
				String out_str = getSQLString(function.getTerm(1), index, false);
				String in_str = getSQLString(function.getTerm(2), index, false);
				String result = sqladapter.strReplace(orig, out_str, in_str);
				return result;
			} 
			else if (functionSymbol ==  ExpressionOperation.CONCAT) {
				String left = getSQLString(function.getTerm(0), index, false);
				String right = getSQLString(function.getTerm(1), index, false);
				String result = sqladapter.strConcat(new String[]{left, right});
				return result;
			}
			else if (functionSymbol == ExpressionOperation.STRLEN) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.strLength(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.YEAR) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateYear(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.MINUTES) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateMinutes(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.DAY) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateDay(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.MONTH) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateMonth(literal);
				return result;
			}  
			else if (functionSymbol == ExpressionOperation.SECONDS) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateSeconds(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.HOURS) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateHours(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.TZ) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.dateTZ(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.ENCODE_FOR_URI) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.strEncodeForUri(literal);
				return result;
			}
			else if (functionSymbol == ExpressionOperation.UCASE) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.strUcase(literal);
				return result;
			}
			else if (functionSymbol == ExpressionOperation.MD5) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.MD5(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.SHA1) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.SHA1(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.SHA256) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.SHA256(literal);
				return result;
			} 
			else if (functionSymbol == ExpressionOperation.SHA512) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.SHA512(literal); //TODO FIX
				return result;
			}
			else if (functionSymbol == ExpressionOperation.LCASE) {
				String literal = getSQLString(function.getTerm(0), index, false);
				String result = sqladapter.strLcase(literal);
				return result;
			}  
			else if (functionSymbol == ExpressionOperation.SUBSTR) {
				String string = getSQLString(function.getTerm(0), index, false);
				String start = getSQLString(function.getTerm(1), index, false);
				if (function.getTerms().size() == 2){
					return sqladapter.strSubstr(string, start);
				}
				String end = getSQLString(function.getTerm(2), index, false);
				String result = sqladapter.strSubstr(string, start, end);

				return result;
			}
			else if (functionSymbol == ExpressionOperation.STRBEFORE) {
				String string = getSQLString(function.getTerm(0), index, false);
				String before = getSQLString(function.getTerm(1), index, false);
				String result = sqladapter.strBefore(string, before);
				return result;
			}
			else if (functionSymbol == ExpressionOperation.STRAFTER) {
				String string = getSQLString(function.getTerm(0), index, false);
				String after = getSQLString(function.getTerm(1), index, false);
				String result = sqladapter.strAfter(string, after);
				return result;
			}
		}

		/*
		 * The atom must be of the form uri("...", x, y)
		 */
		// String functionName = function.getFunctionSymbol().toString();
		if ((functionSymbol instanceof URITemplatePredicate) || (functionSymbol instanceof BNodePredicate)) {
			return getSQLStringForTemplateFunction(function, index);
		} 
		else {
			throw new RuntimeException("Unexpected function in the query: " + function);
		}
	}

	/***
	 * Returns the valid SQL lexical form of rdf literals based on the current
	 * database and the datatype specified in the function predicate.
	 * 
	 * <p>
	 * For example, if the function is xsd:boolean, and the current database is
	 * H2, the SQL lexical form would be for "true" "TRUE" (or any combination
	 * of lower and upper case) or "1" is always
	 * 
	 * @param constant
	 * @return
	 */
	private String getSQLLexicalForm(ValueConstant constant) {
		switch (constant.getType()) {
		case BNODE:
		case LITERAL:
		case OBJECT:
		case STRING:
			return "'" + constant.getValue() + "'";
		case BOOLEAN:
			boolean v = XsdDatatypeConverter.parseXsdBoolean(constant.getValue());
			return sqladapter.getSQLLexicalFormBoolean(v);		
		case DATETIME:
			return sqladapter.getSQLLexicalFormDatetime(constant.getValue());
		case DATETIME_STAMP:
			return sqladapter.getSQLLexicalFormDatetimeStamp(constant.getValue());
		case DECIMAL:
		case DOUBLE:
		case INTEGER:
		case LONG:
		case FLOAT:
		case NON_POSITIVE_INTEGER:
		case INT:
		case UNSIGNED_INT:
		case NEGATIVE_INTEGER:
		case POSITIVE_INTEGER:
		case NON_NEGATIVE_INTEGER:
			return constant.getValue();
		default:
			return "'" + constant.getValue() + "'";
		}
	}



    /**
	 * Utility class to resolve "database" atoms to view definitions ready to be
	 * used in a FROM clause, and variables, to column references defined over
	 * the existing view definitions of a query.
	 */
	public class QueryAliasIndex {

		final Map<Function, RelationID> viewNames = new HashMap<>();
		final Map<Function, RelationDefinition> dataDefinitions = new HashMap<>();
		final Map<Variable, Set<QualifiedAttributeID>> columnReferences = new HashMap<>();
		
		int dataTableCount = 0;
		boolean isEmpty = false;

		public QueryAliasIndex(CQIE query) {
			List<Function> body = query.getBody();
			generateViews(body);
		}

		private void generateViews(List<Function> atoms) {
			for (Function atom : atoms) {
				/*
				 * This will be called recursively if necessary
				 */
				generateViewsIndexVariables(atom);
			}
		}

		/***
		 * We assiciate each atom to a view definition. This will be
		 * <p>
		 * "tablename" as "viewX" or
		 * <p>
		 * (some nested sql view) as "viewX"
		 * 
		 * <p>
		 * View definitions are only done for data atoms. Join/LeftJoin and
		 * boolean atoms are not associated to view definitions.
		 * 
		 * @param atom
		 */
		private void generateViewsIndexVariables(Function atom) {
			if (atom.isOperation()) {
				return;
			} 
			else if (atom.isAlgebraFunction()) {
				for (Term subatom : atom.getTerms()) {
					if (subatom instanceof Function) {
						generateViewsIndexVariables((Function) subatom);
					}
				}
			}

			RelationID id = Relation2DatalogPredicate.createRelationFromPredicateName(metadata.getQuotedIDFactory(), atom.getFunctionSymbol());
			RelationDefinition def = metadata.getRelation(id);
			if (def == null) {
				// There is no definition for this atom, its not a database
				// predicate, the query is empty.
				isEmpty = true;
				return;
			}
			dataTableCount++;
			viewNames.put(atom, metadata.getQuotedIDFactory().createRelationID(null, String.format(VIEW_NAME, dataTableCount)));
			dataDefinitions.put(atom, def);
			
			indexVariables(atom);
		}

		private void indexVariables(Function atom) {
			RelationDefinition def = dataDefinitions.get(atom);
			RelationID viewName = viewNames.get(atom);
			for (int index = 0; index < atom.getTerms().size(); index++) {
				Term term = atom.getTerms().get(index);
				if (!(term instanceof Variable)) {
					continue;
				}
				Set<QualifiedAttributeID> references = columnReferences.get(term);
				if (references == null) {
					references = new LinkedHashSet<>();
					columnReferences.put((Variable) term, references);
				}
				QuotedID columnId = def.getAttribute(index + 1).getID();   // indexes from 1
				QualifiedAttributeID qualifiedId = new QualifiedAttributeID(viewName, columnId);
				references.add(qualifiedId);
			}
		}

		/***
		 * Returns all the column aliases that correspond to this variable,
		 * across all the DATA atoms in the query (not algebra operators or
		 * boolean conditions.
		 * 
		 * @param var
		 *            The variable we want the referenced columns.
		 */
		public Set<QualifiedAttributeID> getColumnReferences(Variable var) {
			return columnReferences.get(var);
		}

		/***
		 * Generates the view definition, i.e., "tablename viewname".
		 */
		public String getViewDefinition(Function atom) {
			RelationDefinition def = dataDefinitions.get(atom);
			if (def instanceof DatabaseRelationDefinition) {
				return sqladapter.sqlTableName(dataDefinitions.get(atom).getID().getSQLRendering(), 
									viewNames.get(atom).getSQLRendering());
			} 
			else if (def instanceof ParserViewDefinition) {
				return String.format("(%s) %s", ((ParserViewDefinition) def).getStatement(), 
								viewNames.get(atom).getSQLRendering());
			}
			throw new RuntimeException("Impossible to get data definition for: " + atom + ", type: " + def);
		}

		public String getColumnReference(Function atom, int column) {
			RelationID viewName = viewNames.get(atom);
			RelationDefinition def = dataDefinitions.get(atom);
			QuotedID columnname = def.getAttribute(column + 1).getID(); // indexes from 1
			return new QualifiedAttributeID(viewName, columnname).getSQLRendering();
		}
	}
}
