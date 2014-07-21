package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.SQLQueryParser;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.ParsedSQLQuery;
import it.unibz.krdb.sql.api.RelationJSQL;
import it.unibz.krdb.sql.api.SelectJSQL;
import it.unibz.krdb.sql.api.SelectionJSQL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;



public class Mapping2DatalogConverter {

	private List<OBDAMappingAxiom> mappingAxioms;
	private DBMetadata dbMetaData;

	private SQLQueryParser sqlQueryParser;

	private static final OBDADataFactory factory = OBDADataFactoryImpl
			.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 */
	public Mapping2DatalogConverter(List<OBDAMappingAxiom> mappingAxioms,
                                    DBMetadata dbMetaData) {
		this.mappingAxioms = mappingAxioms;
		this.dbMetaData = dbMetaData;

		sqlQueryParser = new SQLQueryParser(dbMetaData);
	}

	public DatalogProgram constructDatalogProgram() {
		DatalogProgram datalogProgram = factory.getDatalogProgram();
		List<String> errorMessages = new ArrayList<>();
		for (OBDAMappingAxiom mappingAxiom : mappingAxioms) {
			try {
				// Obtain the target and source query from each mapping axiom in
				// the model.
				CQIE targetQuery = (CQIE) mappingAxiom.getTargetQuery();

				OBDASQLQuery sourceQuery = (OBDASQLQuery) mappingAxiom
						.getSourceQuery();

				// Parse the SQL query tree from the source query
				ParsedSQLQuery parsedSQLQuery = sqlQueryParser.parseDeeply(sourceQuery.toString());

				// Create a lookup table for variable swapping
				LookupTable lookupTable = createLookupTable(parsedSQLQuery);


				// Construct the body from the source query
				List<Function> bodyAtoms = new ArrayList<>();

                // For each table, creates an atom and adds it to the body
                addTableAtoms(bodyAtoms, parsedSQLQuery, lookupTable);

                // For each join condition, creates an atom and adds it to the body
                addJoinConditionAtoms(bodyAtoms, parsedSQLQuery, lookupTable);

                // For each where clause, creates an atom and adds it to the body
                addWhereClauseAtoms(bodyAtoms, parsedSQLQuery, lookupTable);

                // For each body atom in the target query,
                //  (1) renameVariables its variables and
                //  (2) use it as the head atom of a new rule
                for (Function atom : targetQuery.getBody()) {
                    // Construct the head from the target query.
                    Function head = createHeadAtom(atom, lookupTable);
                    // Create a new rule from the new head and the body
                    CQIE rule = factory.getCQIE(head, bodyAtoms);
                    datalogProgram.appendRule(rule);
                }

			} catch (Exception e) {
				errorMessages.add("Error in mapping with id: " + mappingAxiom.getId()
                        + " \n Description: " + e.getMessage()
                        + " \nMapping: [" + mappingAxiom.toString() + "]");

			}
		}

		if (errorMessages.size() > 0) {

			StringBuilder errors = new StringBuilder();
			for (String error : errorMessages) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error analyzing the following mappings. Please correct the issue(s) to continue.\n"
					+ errors.toString();

			throw  new IllegalArgumentException(msg);
		}
		return datalogProgram;
	}

    /**
     * Creates the head atom from the target query.
     *
     * @param atom an atom from the body of the target query
     * @param lookupTable
     * @return a head atom
     */
    private Function createHeadAtom(Function atom, LookupTable lookupTable) {
        List<Term> terms = atom.getTerms();
        List<Term> newTerms = new ArrayList<>();
        for (Term term : terms) {
            newTerms.add(renameVariables(term, lookupTable));
        }
        return factory.getFunction(atom.getFunctionSymbol(),
                newTerms);
    }

    private void addWhereClauseAtoms(List<Function> bodyAtoms, ParsedSQLQuery parsedSQLQuery, LookupTable lookupTable) throws JSQLParserException {
        // For the "where" clause
        SelectionJSQL whereClause = parsedSQLQuery.getWhereClause();
        if (whereClause != null) {
            Expression conditions = whereClause.getRawConditions();

            Function filterFunction = getFunction(conditions,
                    lookupTable);

            bodyAtoms.add(filterFunction);

        }
    }

    /**
     * For each join condition, creates an atom and adds it to the body
     */
    private void addJoinConditionAtoms(List<Function> bodyAtoms, ParsedSQLQuery parsedSQLQuery, LookupTable lookupTable) throws JSQLParserException {
        List<Expression> joinConditions = parsedSQLQuery.getJoinConditions();
        for (Expression condition : joinConditions) {
            Function atom = getFunction(condition, lookupTable);
            bodyAtoms.add(atom);
        }
    }

    /**
     * For each table, creates an atom and adds it to the body
     * @param bodyAtoms
     *  will be extended
     * @param parsedSQLQuery
     * @param lookupTable
     */
    private void addTableAtoms(List<Function> bodyAtoms, ParsedSQLQuery parsedSQLQuery, LookupTable lookupTable) throws JSQLParserException {
        // Tables mentioned in the SQL query
        List<RelationJSQL> tables = parsedSQLQuery.getTables();

        for (RelationJSQL table : tables) {
            // Construct the URI from the table name
            String tableName = table.getFullName();

            // Construct the predicate using the table name
            int arity = dbMetaData.getDefinition(tableName).getNumOfAttributes();
            Predicate predicate = factory.getPredicate(tableName, arity);

            // Swap the column name with a new variable from the lookup table
            List<Term> terms = new ArrayList<>();
            for (int i = 1; i <= arity; i++) {
                String columnName = dbMetaData
                        .getFullQualifiedAttributeName(tableName,
                                table.getAlias(), i);
                String termName = lookupTable.lookup(columnName);
                if (termName == null) {
                    throw new IllegalStateException("Column '" + columnName
                            + "'was not found in the lookup table: ");
                }
                Variable var = factory.getVariable(termName);
                terms.add(var);
            }
            // Create an atom for a particular table
            Function atom = factory.getFunction(predicate, terms);
            bodyAtoms.add(atom);
        }
    }

	/**
	 * Methods to create a {@link Function} starting from a
	 * {@link IsNullExpression}
	 * 
	 * @param expression
	 *            IsNullExpression
	 * @param lookupTable
	 * @return a function from the OBDADataFactory
	 */
	private Function getFunction(IsNullExpression expression, LookupTable lookupTable) {

		Expression column = expression.getLeftExpression();
		String columnName = column.toString();
		String variableName = lookupTable.lookup(columnName);
		if (variableName == null) {
			throw new RuntimeException(
					"Unable to find column name for variable: " + columnName);
		}
		Term var = factory.getVariable(variableName);

		if (!expression.isNot()) {
			return factory.getFunctionIsNull(var);
		} else {
			return factory.getFunctionIsNotNull(var);
		}
	}
	
	/**
	 * Methods to create a {@link Function} starting from a
	 * {@link IsNullExpression}
	 * 
	 * @param expression
	 *            IsNullExpression
	 * @param lookupTable
	 * @return a function from the OBDADataFactory
	 */
    // TODO:
	private Function getFunction(CastExpression expression, LookupTable lookupTable) {

		Expression column = expression.getLeftExpression();
		String columnName = column.toString();
		String variableName = lookupTable.lookup(columnName);
		if (variableName == null) {
			throw new RuntimeException(
					"Unable to find column name for variable: " + columnName);
		}
		Term var = factory.getVariable(variableName);

		ColDataType datatype = expression.getType();
		

		
		Term var2 = null;
		
		//first value is a column, second value is a datatype. It can  also have the size
		
		return factory.getFunctionCast(var, var2);
		
		
	}

	/**
	 * Recursive methods to create a {@link Function} starting from a
	 * {@link BinaryExpression} We consider all possible values of the left and
	 * right expressions
	 * 
	 * @param pred
	 * @param lookupTable
	 * @return
	 */
	private Function getFunction(Expression pred, LookupTable lookupTable) {
		if (pred instanceof BinaryExpression) {
			return getFunction((BinaryExpression) pred, lookupTable);
		} else if (pred instanceof IsNullExpression) {
			return getFunction((IsNullExpression) pred, lookupTable);
		} else if (pred instanceof Parenthesis) {
			Expression inside = ((Parenthesis) pred).getExpression();
			
			 //Consider the case of NOT(...)
			if(((Parenthesis) pred).isNot()){
				return factory.getFunctionNOT(getFunction(inside, lookupTable));
			}
			
			return getFunction(inside, lookupTable);
			
		} else if (pred instanceof Between) {
			Between between = (Between) pred;
			Expression left = between.getLeftExpression();
			Expression e1 = between.getBetweenExpressionStart();
			Expression e2 = between.getBetweenExpressionEnd();

			GreaterThanEquals gte = new GreaterThanEquals();
			gte.setLeftExpression(left);
			gte.setRightExpression(e1);

			MinorThanEquals mte = new MinorThanEquals();
			mte.setLeftExpression(left);
			mte.setRightExpression(e2);

			AndExpression ande = new AndExpression(gte, mte);
			return getFunction(ande, lookupTable);
		} else if (pred instanceof InExpression) {
			InExpression inExpr = (InExpression) pred;
			Expression left = inExpr.getLeftExpression();
			ExpressionList ilist = (ExpressionList) inExpr.getRightItemsList();

			List<EqualsTo> eqList = new ArrayList<EqualsTo>();
			for (Expression item : ilist.getExpressions()) {
				EqualsTo eq = new EqualsTo();
				eq.setLeftExpression(left);
				eq.setRightExpression(item);
				eqList.add(eq);
			}
			int size = eqList.size();
			if (size > 1) {
				OrExpression or = new OrExpression(eqList.get(size - 1),
						eqList.get(size - 2));
				for (int i = size - 3; i >= 0; i--) {
					OrExpression orexpr = new OrExpression(eqList.get(i), or);
					or = orexpr;
				}
				return getFunction(or, lookupTable);
			} else {
				return getFunction(eqList.get(0), lookupTable);
			}
		} else if (pred instanceof net.sf.jsqlparser.expression.Function) {
			
			
			net.sf.jsqlparser.expression.Function regex = (net.sf.jsqlparser.expression.Function) pred;
			if (regex.getName().toLowerCase().equals("regexp_like")) {
				return getFunctionRegexLike(regex, lookupTable);

			}

		}
		return null;
	}

	/**
	 * Recursive methods to create a {@link Function} starting from a
	 * {@link BinaryExpression} We consider all possible values of the left and
	 * right expressions
	 * 
	 * @param pred
	 * @param lookupTable
	 * @return
	 */

	private Function getFunction(BinaryExpression pred, LookupTable lookupTable) {
		Expression left = pred.getLeftExpression();
		Expression right = pred.getRightExpression();

		//left term can be function or column variable
		Term t1 = null;
		t1 = getFunction(left, lookupTable);
		if (t1 == null) {
			t1 = getVariable(left, lookupTable);
		}
		if(t1 == null)
			throw new RuntimeException("Unable to find column name for variable: " +left);

		//right term can be function, column variable or data value
		Term t2 = null;
		t2 = getFunction(right, lookupTable);
		if (t2 == null) {
			t2 = getVariable(right, lookupTable);
		}
		if (t2 == null) {
			t2 = getValueConstant(right);
		}
		
		//get boolean operation
		String op = pred.getStringExpression();
		Function funct = null;
		if (op.equals("="))
			funct = factory.getFunctionEQ(t1, t2);
		else if (op.equals(">"))
			funct = factory.getFunctionGT(t1, t2);
		else if (op.equals("<"))
			funct = factory.getFunctionLT(t1, t2);
		else if (op.equals(">="))
			funct = factory.getFunctionGTE(t1, t2);
		else if (op.equals("<="))
			funct = factory.getFunctionLTE(t1, t2);
		else if (op.equals("<>") || op.equals("!="))
			funct = factory.getFunctionNEQ(t1, t2);
		else if (op.equals("AND"))
			funct = factory.getFunctionAND(t1, t2);
		else if (op.equals("OR"))
			funct = factory.getFunctionOR(t1, t2);
		else if (op.equals("+"))
			funct = factory.getFunctionAdd(t1, t2);
		else if (op.equals("-"))
			funct = factory.getFunctionSubstract(t1, t2);
		else if (op.equals("*"))
			funct = factory.getFunctionMultiply(t1, t2);
		else if (op.equals("LIKE"))
			funct = factory.getFunctionLike(t1, t2);
		else if (op.equals("~"))
			funct = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral(""));
		else if (op.equals("~*"))
			funct = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral("i")); // i flag for case insensitivity
		else if (op.equals("!~"))
			funct = factory.getFunctionNOT(factory.getFunctionRegex(t1, t2,factory.getConstantLiteral("")));
		else if (op.equals("!~*"))
			funct = factory.getFunctionNOT(factory.getFunctionRegex(t1, t2,factory.getConstantLiteral("i")));
		else if (op.equals("REGEXP"))
			funct = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral("i"));
		else if (op.equals("REGEXP BINARY"))
			funct = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral(""));
		else
			throw new RuntimeException("Unknown opertor: " + op);

		return funct;

	}
	
	/**
	 * create a {@link Function} starting from a
	 * {@link net.sf.jsqlparser.expression.Function} containing a regexp_like (regex function for oracle)
	 *  We consider all possible values of the three parameters (source_string, pattern and match_parameter)
	 * @param regex
	 * @param lookupTable
	 * @return
	 */
			
	private Function getFunctionRegexLike(net.sf.jsqlparser.expression.Function regex, LookupTable lookupTable) {
		// left term can be function or column variable
		List<Expression> listExpression = regex.getParameters()
				.getExpressions();
		if (listExpression.size() == 2 || listExpression.size() == 3) {
			
			Term t1 = null; // first parameter is a source_string, generally a column
			Expression first = listExpression.get(0);
			t1 = getFunction(first, lookupTable);
			if (t1 == null) {
				t1 = getVariable(first, lookupTable);
			}
			if (t1 == null)
				throw new RuntimeException(
						"Unable to find column name for variable: "
								+ first);

			
			Term t2 = null; // second parameter is a pattern, so generally a regex string 
			Expression second = listExpression.get(1);
			t2 = getFunction(second, lookupTable);
			if (t2 == null) {
				t2 = getVariable(second, lookupTable);
			}
			if (t2 == null) {
				t2 = getValueConstant(second);
			}

			
			/*
			 * Term t3 is optional for match_parameter in regexp_like
			 */
			Term t3 = null;
			
			try {
				Expression third = listExpression.get(2);
				t3 = getValueConstant(third);

				return factory.getFunctionRegex(t1, t2, t3);
			} catch (IndexOutOfBoundsException e) {

				return factory.getFunctionRegex(t1, t2, factory.getConstantLiteral(""));
			}

		}
		return null;
		
	}

	private Term getVariable(Expression pred, LookupTable lookupTable) {
		String termName = "";
		if (pred instanceof Column) {
			termName = lookupTable.lookup(pred.toString());
			if (termName == null) {
				return null;
			}
			return factory.getVariable(termName);
		}
		return null;
	}

	/**
	 * Return a valueConstant or Variable constructed from the given expression
	 * 
	 * @param pred
	 *            the expression to process
	 * @param lookupTable
	 *            in case of variable
	 * @return constructed valueconstant or variable
	 */
	private Term getValueConstant(Expression pred) {
		String termRightName = "";
		if (pred instanceof Column) {
			// if the columns contains a boolean value
			String columnName = ((Column) pred).getColumnName();
			if (columnName.toLowerCase().equals("true")
					|| columnName.toLowerCase().equals("false")) {
				return factory
						.getConstantLiteral(columnName, COL_TYPE.BOOLEAN);
			}
			else 
				throw new RuntimeException(
						"Unable to find column name for variable: "
								+ columnName);

		} else if (pred instanceof StringValue) {
			termRightName = ((StringValue) pred).getValue();
			return factory.getConstantLiteral(termRightName, COL_TYPE.STRING);

		} else if (pred instanceof DateValue) {
			termRightName = ((DateValue) pred).getValue().toString();
			return factory.getConstantLiteral(termRightName, COL_TYPE.DATETIME);

		} else if (pred instanceof TimeValue) {
			termRightName = ((TimeValue) pred).getValue().toString();
			return factory.getConstantLiteral(termRightName, COL_TYPE.DATETIME);

		} else if (pred instanceof TimestampValue) {
			termRightName = ((TimestampValue) pred).getValue().toString();
			return factory.getConstantLiteral(termRightName, COL_TYPE.DATETIME);

		} else if (pred instanceof LongValue) {
			termRightName = ((LongValue) pred).getStringValue();
			return factory.getConstantLiteral(termRightName, COL_TYPE.INTEGER);

		} else if (pred instanceof DoubleValue) {
			termRightName = ((DoubleValue) pred).toString();
			return factory.getConstantLiteral(termRightName, COL_TYPE.DOUBLE);

		} else {
			termRightName = pred.toString();
			return factory.getConstantLiteral(termRightName, COL_TYPE.LITERAL);

		}
	}

    /**
     * Returns a new term by renaming variables occurring in the  {@code term}
     *  according to the {@code lookupTable}
     */
    private Term renameVariables(Term term, LookupTable lookupTable) {
        Term result = null;

        if (term instanceof Variable) {
            Variable var = (Variable) term;
            String varName = var.getName();
            String termName = lookupTable.lookup(varName);
            if (termName == null) {
                String messageFormat = "Error in identifying column name \"%s\", " +
                        "please check the query source in the mappings.\n" +
                        "Possible reasons:\n" +
                        "1. The name is ambiguous, or\n" +
                        "2. The name is not defined in the database schema.";
                final String msg = String.format(messageFormat, var);
                throw new RuntimeException(msg);
            }
            result = factory.getVariable(termName);

        } else if (term instanceof Function) {
            Function func = (Function) term;
            List<Term> terms = func.getTerms();
            List<Term> newTerms = new ArrayList<>();
            for (Term innerTerm : terms) {
                newTerms.add(renameVariables(innerTerm, lookupTable));
            }
            result = factory.getFunction(func.getFunctionSymbol(), newTerms);
        } else if (term instanceof Constant) {
            result = term.clone();
        }
        return result;
    }

	private LookupTable createLookupTable(ParsedSQLQuery queryParsed) throws JSQLParserException {
		LookupTable lookupTable = new LookupTable();

		// Collect all the possible column names from tables.
		List<RelationJSQL> tableList = queryParsed.getTables();

		// Collect all known column aliases
		Map<String, String> aliasMap = queryParsed.getAliasMap();
		
		int offset = 0; // the index offset

		for (RelationJSQL table : tableList) {
			
			String tableName = table.getTableName();
			String fullName = table.getFullName();
			String tableGivenName = table.getGivenName();
			DataDefinition def = dbMetaData.getDefinition(fullName);
			
			
				 if (def == null) {
					 throw new RuntimeException("Definition not found for table '" + tableGivenName + "'.");
				 }
			
			
			int size = def.getNumOfAttributes();

			for (int i = 1; i <= size; i++) {
				// assigned index number
				int index = i + offset;
				
				// simple attribute name
//				String columnName = dbMetaData.getAttributeName(tableName, i);
				String columnName = dbMetaData.getAttributeName(fullName, i);
				
				lookupTable.add(columnName, index);

				String lowercaseColumn = columnName.toLowerCase();

				if (aliasMap.containsKey(lowercaseColumn)) { // register the
																// alias name,
																// if any
					lookupTable.add(aliasMap.get(lowercaseColumn), columnName);
				}

				// attribute name with table name prefix
				String tableColumnName = tableName + "." + columnName;
				lookupTable.add(tableColumnName, index);

				// attribute name with table name prefix
				String tablecolumnname = tableColumnName.toLowerCase();
				if (aliasMap.containsKey(tablecolumnname)) { // register the
																// alias name,
																// if any
					lookupTable.add(aliasMap.get(tablecolumnname),
							tableColumnName);
				}

				// attribute name with table given name prefix
				String givenTableColumnName = tableGivenName + "." + columnName;
				lookupTable.add(givenTableColumnName, tableColumnName);

				String giventablecolumnname = givenTableColumnName
						.toLowerCase();
				if (aliasMap.containsKey(giventablecolumnname)) { // register
																	// the alias
																	// name, if
																	// any
					lookupTable.add(aliasMap.get(giventablecolumnname),
							tableColumnName);
				}

				// full qualified attribute name
				String qualifiedColumnName = dbMetaData
						.getFullQualifiedAttributeName(fullName, i);
				// String qualifiedColumnName =
				// dbMetaData.getFullQualifiedAttributeName(tableName, i);
				lookupTable.add(qualifiedColumnName, tableColumnName);
				String qualifiedcolumnname = qualifiedColumnName.toLowerCase();
				if (aliasMap.containsKey(qualifiedcolumnname)) { // register the
																	// alias
																	// name, if
																	// any
					lookupTable.add(aliasMap.get(qualifiedcolumnname),
							tableColumnName);
				}

				// full qualified attribute name using table alias
				String tableAlias = table.getAlias();
				if (tableAlias != null) {
					String qualifiedColumnAlias = dbMetaData
							.getFullQualifiedAttributeName(fullName,
									tableAlias, i);
					// String qualifiedColumnAlias =
					// dbMetaData.getFullQualifiedAttributeName(tableName,
					// tableAlias, i);
					lookupTable.add(qualifiedColumnAlias, index);
					String aliasColumnName = tableAlias.toLowerCase() + "."
							+ lowercaseColumn;
					if (aliasMap.containsKey(aliasColumnName)) { // register the
																	// alias
																	// name, if
																	// any
						lookupTable.add(aliasMap.get(aliasColumnName),
								qualifiedColumnAlias);
					}

				}
				
				//check if we do not have subselect with alias name assigned
				for(SelectJSQL subSelect: queryParsed.getSubSelects()){
					String subSelectAlias = subSelect.getAlias();
					if (subSelectAlias != null) {
						String aliasColumnName = subSelectAlias.toLowerCase()
								+ "." + lowercaseColumn;
						lookupTable.add(aliasColumnName, index);
						if (aliasMap.containsKey(aliasColumnName)) { // register the alias name, if any
							lookupTable.add(aliasMap.get(aliasColumnName), aliasColumnName);
						}
					}
				}
			}
			offset += size;
		}
		return lookupTable;
	}
}
