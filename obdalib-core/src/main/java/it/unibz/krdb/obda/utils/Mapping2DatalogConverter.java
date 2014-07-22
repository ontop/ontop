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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SubSelect;


public class Mapping2DatalogConverter {

	private List<OBDAMappingAxiom> mappingAxioms;
	private DBMetadata dbMetadata;

	private SQLQueryParser sqlQueryParser;

	private static final OBDADataFactory factory = OBDADataFactoryImpl
			.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 */
	public Mapping2DatalogConverter(List<OBDAMappingAxiom> mappingAxioms,
                                    DBMetadata dbMetadata) {
		this.mappingAxioms = mappingAxioms;
		this.dbMetadata = dbMetadata;

		sqlQueryParser = new SQLQueryParser(dbMetadata);
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

            Expression2FunctionConverter converter = new Expression2FunctionConverter(lookupTable);
            Function filterFunction =  converter.convert(conditions);

            bodyAtoms.add(filterFunction);
        }
    }

    /**
     * For each join condition, creates an atom and adds it to the body
     */
    private void addJoinConditionAtoms(List<Function> bodyAtoms, ParsedSQLQuery parsedSQLQuery, LookupTable lookupTable) throws JSQLParserException {
        List<Expression> joinConditions = parsedSQLQuery.getJoinConditions();
        for (Expression condition : joinConditions) {
            Expression2FunctionConverter visitor = new Expression2FunctionConverter(lookupTable);
            Term atom = visitor.visitEx(condition);
            bodyAtoms.add((Function) atom);
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
            int arity = dbMetadata.getDefinition(tableName).getNumOfAttributes();
            Predicate predicate = factory.getPredicate(tableName, arity);

            // Swap the column name with a new variable from the lookup table
            List<Term> terms = new ArrayList<>();
            for (int i = 1; i <= arity; i++) {
                String columnName = dbMetadata
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

    /**
     * Creates a lookupTable:
     * (1) Collects all the possible column names from the tables mentioned in the query, and aliases.
     * (2) Assigns new variables to them
      */
    private LookupTable createLookupTable(ParsedSQLQuery queryParsed) throws JSQLParserException {
		LookupTable lookupTable = new LookupTable();

		List<RelationJSQL> tables = queryParsed.getTables();

		// Collect all known column aliases
		Map<String, String> aliasMap = queryParsed.getAliasMap();
		
		int offset = 0; // the index offset

		for (RelationJSQL table : tables) {
			
			String tableName = table.getTableName();
			String fullName = table.getFullName();
			String tableGivenName = table.getGivenName();
			DataDefinition tableDefinition = dbMetadata.getDefinition(fullName);

            if (tableDefinition == null) {
                throw new RuntimeException("Definition not found for table '" + tableGivenName + "'.");
            }

            int size = tableDefinition.getNumOfAttributes();

			for (int i = 1; i <= size; i++) {
				// assigned index number
				int index = i + offset;
				
				// simple attribute name
				String columnName = dbMetadata.getAttributeName(fullName, i);
				
				lookupTable.add(columnName, index);

				String lowercaseColumn = columnName.toLowerCase();

                // register the alias name, if any
                if (aliasMap.containsKey(lowercaseColumn)) {
					lookupTable.add(aliasMap.get(lowercaseColumn), columnName);
				}

				// attribute name with table name prefix
				String tableColumnName = tableName + "." + columnName;
				lookupTable.add(tableColumnName, index);

				// attribute name with table name prefix
				String tablecolumnname = tableColumnName.toLowerCase();

                // register the alias name, if any
				if (aliasMap.containsKey(tablecolumnname)) {
					lookupTable.add(aliasMap.get(tablecolumnname),
							tableColumnName);
				}

				// attribute name with table given name prefix
				String givenTableColumnName = tableGivenName + "." + columnName;
				lookupTable.add(givenTableColumnName, tableColumnName);

				String giventablecolumnname = givenTableColumnName.toLowerCase();

                // register the alias name, if any
                if (aliasMap.containsKey(giventablecolumnname)) {
					lookupTable.add(aliasMap.get(giventablecolumnname),
							tableColumnName);
				}

				// full qualified attribute name
				String qualifiedColumnName = dbMetadata.getFullQualifiedAttributeName(fullName, i);

				lookupTable.add(qualifiedColumnName, tableColumnName);
				String qualifiedcolumnname = qualifiedColumnName.toLowerCase();

                // register the alias name, if any
                if (aliasMap.containsKey(qualifiedcolumnname)) {
					lookupTable.add(aliasMap.get(qualifiedcolumnname),
							tableColumnName);
				}

				// full qualified attribute name using table alias
				String tableAlias = table.getAlias();
				if (tableAlias != null) {
					String qualifiedColumnAlias = dbMetadata
							.getFullQualifiedAttributeName(fullName,
                                    tableAlias, i);
					lookupTable.add(qualifiedColumnAlias, index);
					String aliasColumnName = tableAlias.toLowerCase() + "." + lowercaseColumn;

                    // register the alias name, if any
                    if (aliasMap.containsKey(aliasColumnName)) {
                        lookupTable.add(aliasMap.get(aliasColumnName), qualifiedColumnAlias);
                    }
				}
				
				//check if we do not have subselect with alias name assigned
				for(SelectJSQL subSelect: queryParsed.getSubSelects()){
					String subSelectAlias = subSelect.getAlias();
					if (subSelectAlias != null) {
						String aliasColumnName = subSelectAlias.toLowerCase()
								+ "." + lowercaseColumn;
						lookupTable.add(aliasColumnName, index);

                        // register the alias name, if any
						if (aliasMap.containsKey(aliasColumnName)) {
							lookupTable.add(aliasMap.get(aliasColumnName), aliasColumnName);
						}
					}
				}
			}
			offset += size;
		}
		return lookupTable;
	}

    /**
     * This visitor class converts the SQL Expression to a Function
     */
    private class Expression2FunctionConverter implements ExpressionVisitor {

        private final LookupTable lookupTable;

        private Term result;

        public Expression2FunctionConverter(LookupTable lookupTable) {
            this.lookupTable = lookupTable;
        }

        public Function convert(Expression expression){
            expression.accept(this);
            return (Function)result;
        }

        /**
         * Visits the expression and gets the result
         */
        private Term visitEx(Expression expression) {
            expression.accept(this);
            return this.result;
        }

        public void visitBinaryExpression(BinaryExpression expression){
            Expression left = expression.getLeftExpression();
            Expression right = expression.getRightExpression();

            Term t1 = visitEx(left);

            if(t1 == null)
                throw new RuntimeException("Unable to find column name for variable: " +left);

            Term t2 = visitEx(right);

            //get boolean operation
            String op = expression.getStringExpression();
            Function compositeTerm;
            switch (op) {
                case "=":
                    compositeTerm = factory.getFunctionEQ(t1, t2);
                    break;
                case ">":
                    compositeTerm = factory.getFunctionGT(t1, t2);
                    break;
                case "<":
                    compositeTerm = factory.getFunctionLT(t1, t2);
                    break;
                case ">=":
                    compositeTerm = factory.getFunctionGTE(t1, t2);
                    break;
                case "<=":
                    compositeTerm = factory.getFunctionLTE(t1, t2);
                    break;
                case "<>":
                case "!=":
                    compositeTerm = factory.getFunctionNEQ(t1, t2);
                    break;
                case "AND":
                    compositeTerm = factory.getFunctionAND(t1, t2);
                    break;
                case "OR":
                    compositeTerm = factory.getFunctionOR(t1, t2);
                    break;
                case "+":
                    compositeTerm = factory.getFunctionAdd(t1, t2);
                    break;
                case "-":
                    compositeTerm = factory.getFunctionSubstract(t1, t2);
                    break;
                case "*":
                    compositeTerm = factory.getFunctionMultiply(t1, t2);
                    break;
                case "LIKE":
                    compositeTerm = factory.getFunctionLike(t1, t2);
                    break;
                case "~":
                    compositeTerm = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral(""));
                    break;
                case "~*":
                    compositeTerm = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral("i")); // i flag for case insensitivity
                    break;
                case "!~":
                    compositeTerm = factory.getFunctionNOT(factory.getFunctionRegex(t1, t2, factory.getConstantLiteral("")));
                    break;
                case "!~*":
                    compositeTerm = factory.getFunctionNOT(factory.getFunctionRegex(t1, t2, factory.getConstantLiteral("i")));
                    break;
                case "REGEXP":
                    compositeTerm = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral("i"));
                    break;
                case "REGEXP BINARY":
                    compositeTerm = factory.getFunctionRegex(t1, t2, factory.getConstantLiteral(""));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown operator: " + op);
            }

            result = compositeTerm;
        }

        @Override
        public void visit(NullValue nullValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(net.sf.jsqlparser.expression.Function expression) {
            net.sf.jsqlparser.expression.Function regex = expression;
            if (regex.getName().toLowerCase().equals("regexp_like")) {

                List<Expression> expressions = regex.getParameters().getExpressions();
                if (expressions.size() == 2 || expressions.size() == 3) {

                    Term t1; // first parameter is a source_string, generally a column
                    Expression first = expressions.get(0);
                    t1 = visitEx(first);

                    if (t1 == null)
                        throw new RuntimeException("Unable to find column name for variable: "
                                + first);

                    Term t2; // second parameter is a pattern, so generally a regex string
                    Expression second = expressions.get(1);

                    t2 = visitEx(second);

                    /*
                     * Term t3 is optional for match_parameter in regexp_like
			         */
                    Term t3;
                    if(expressions.size() == 3){
                        Expression third = expressions.get(2);
                        t3 = visitEx(third);
                    } else {
                        t3 = factory.getConstantLiteral("");
                    }
                    result = factory.getFunctionRegex(t1, t2, t3);
                }
            } else {
                throw new UnsupportedOperationException("Unsupported expression " + expression);
            }
        }

        @Override
        public void visit(SignedExpression signedExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(JdbcParameter jdbcParameter) {
            // do nothing
        }

        @Override
        public void visit(JdbcNamedParameter jdbcNamedParameter) {
            // do nothing
        }

        @Override
        public void visit(DoubleValue expression) {
            String termRightName = expression.toString();
            result = factory.getConstantLiteral(termRightName, COL_TYPE.DOUBLE);
        }

        @Override
        public void visit(LongValue expression) {
            String termRightName = expression.getStringValue();
            result = factory.getConstantLiteral(termRightName, COL_TYPE.INTEGER);
        }

        @Override
        public void visit(DateValue expression) {
            String termRightName = expression.getValue().toString();
            result = factory.getConstantLiteral(termRightName, COL_TYPE.DATETIME);
        }

        @Override
        public void visit(TimeValue expression) {
            String termRightName = expression.getValue().toString();
            result = factory.getConstantLiteral(termRightName, COL_TYPE.DATETIME);
        }

        @Override
        public void visit(TimestampValue expression) {
            String termRightName = expression.getValue().toString();
            result = factory.getConstantLiteral(termRightName, COL_TYPE.DATETIME);
        }

        @Override
        public void visit(Parenthesis expression) {
            Expression inside = expression.getExpression();

            //Consider the case of NOT(...)
            if(expression.isNot()){
                result = factory.getFunctionNOT(visitEx(inside));
            } else {
                result = visitEx(inside);
            }
        }

        @Override
        public void visit(StringValue expression) {
            String termRightName = expression.getValue();
            result = factory.getConstantLiteral(termRightName, COL_TYPE.STRING);
        }

        @Override
        public void visit(Addition addition) {
            visitBinaryExpression(addition);
        }

        @Override
        public void visit(Division division) {
            visitBinaryExpression(division);
        }

        @Override
        public void visit(Multiplication multiplication) {
            visitBinaryExpression(multiplication);
        }

        @Override
        public void visit(Subtraction subtraction) {
            visitBinaryExpression(subtraction);
        }

        @Override
        public void visit(AndExpression andExpression) {
            visitBinaryExpression(andExpression);
        }

        @Override
        public void visit(OrExpression orExpression) {
            visitBinaryExpression(orExpression);
        }

        @Override
        public void visit(Between expression) {
            Expression left = expression.getLeftExpression();
            Expression e1 = expression.getBetweenExpressionStart();
            Expression e2 = expression.getBetweenExpressionEnd();

            GreaterThanEquals gte = new GreaterThanEquals();
            gte.setLeftExpression(left);
            gte.setRightExpression(e1);

            MinorThanEquals mte = new MinorThanEquals();
            mte.setLeftExpression(left);
            mte.setRightExpression(e2);

            AndExpression e = new AndExpression(gte, mte);

            result = visitEx(e);
        }

        @Override
        public void visit(EqualsTo expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(GreaterThan expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(GreaterThanEquals expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(InExpression expression) {
            Expression left = expression.getLeftExpression();
            ExpressionList rightItemsList = (ExpressionList) expression.getRightItemsList();

            List<EqualsTo> equalsToList = new ArrayList<>();
            for (Expression item : rightItemsList.getExpressions()) {
                EqualsTo eq = new EqualsTo();
                eq.setLeftExpression(left);
                eq.setRightExpression(item);
                equalsToList.add(eq);
            }
            int size = equalsToList.size();
            if (size > 1) {
                OrExpression or = new OrExpression(equalsToList.get(size - 1),
                        equalsToList.get(size - 2));
                for (int i = size - 3; i >= 0; i--) {
                    or = new OrExpression(equalsToList.get(i), or);
                }
                result = visitEx(or);
            } else {
                result = visitEx(equalsToList.get(0));
            }
        }

        @Override
        public void visit(IsNullExpression expression) {
            Expression column = expression.getLeftExpression();
            String columnName = column.toString();
            String variableName = lookupTable.lookup(columnName);
            if (variableName == null) {
                throw new RuntimeException(
                        "Unable to find column name for variable: " + columnName);
            }
            Term var = factory.getVariable(variableName);

            if (!expression.isNot()) {
                result = factory.getFunctionIsNull(var);
            } else {
                result = factory.getFunctionIsNotNull(var);
            }
        }

        @Override
        public void visit(LikeExpression likeExpression) {
            visitBinaryExpression(likeExpression);
        }

        @Override
        public void visit(MinorThan minorThan) {
            visitBinaryExpression(minorThan);
        }

        @Override
        public void visit(MinorThanEquals minorThanEquals) {
            visitBinaryExpression(minorThanEquals);
        }

        @Override
        public void visit(NotEqualsTo notEqualsTo) {
            visitBinaryExpression(notEqualsTo);
        }

        @Override
        public void visit(Column expression) {
            String termName = lookupTable.lookup(expression.toString());

            if (termName != null) {
                /*
                 * If the termName is not null, create a variable
                 */
                result = factory.getVariable(termName);
            } else {
                // Constructs constant
                // if the columns contains a boolean value
                String columnName = expression.getColumnName();
                if (columnName.toLowerCase().equals("true")
                        || columnName.toLowerCase().equals("false")) {
                    result = factory.getConstantLiteral(columnName, COL_TYPE.BOOLEAN);
                }
                else
                    throw new RuntimeException(
                            "Unable to find column name for variable: "
                                    + columnName);
            }

        }

        @Override
        public void visit(SubSelect subSelect) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(CaseExpression caseExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(WhenClause whenClause) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(ExistsExpression existsExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AllComparisonExpression allComparisonExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AnyComparisonExpression anyComparisonExpression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(Concat concat) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(Matches matches) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(BitwiseAnd bitwiseAnd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(BitwiseOr bitwiseOr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(BitwiseXor bitwiseXor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(CastExpression expression) {
            // TODO
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

            result = factory.getFunctionCast(var, var2);

        }

        @Override
        public void visit(Modulo modulo) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(AnalyticExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(ExtractExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(IntervalExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(OracleHierarchicalExpression expression) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(RegExpMatchOperator expression) {
            visitBinaryExpression(expression);
        }

        @Override
        public void visit(JsonExpression jsonExpr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visit(RegExpMySQLOperator regExpMySQLOperator) {
            visitBinaryExpression(regExpMySQLOperator);
        }
    }


}
