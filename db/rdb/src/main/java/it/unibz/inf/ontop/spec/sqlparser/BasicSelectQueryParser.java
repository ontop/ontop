package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class BasicSelectQueryParser<T> {

    protected final ExpressionParser expressionParser;
    protected final TermFactory termFactory;

    protected final QuotedIDFactory idfac;
    protected final MetadataLookup metadata;

    protected final RAOperations<T> operations;

    private int relationIndex = 0;

    protected abstract T create(NamedRelationDefinition relation);

    protected BasicSelectQueryParser(MetadataLookup metadata, CoreSingletons coreSingletons, RAOperations<T> operations) {
        this.expressionParser = new ExpressionParser(metadata.getQuotedIDFactory(), coreSingletons);
        this.idfac = metadata.getQuotedIDFactory();
        this.metadata = metadata;
        this.termFactory = coreSingletons.getTermFactory();
        this.operations = operations;
    }

    protected T parseJSqlSelectQuery(String sql) throws InvalidQueryException, UnsupportedSelectQueryException, QueryParseException {
        try {
            Select select = JSqlParserTools.parse(sql, !idfac.supportsSquareBracketQuotation());
            return translateSelect(select, select.getWithItemsList());
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidQueryException(e.getMessage(), e.getObject());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), e.getObject());
        }
    }


    protected abstract T translateSelect(Select selectBody, List<WithItem<?>> withItemsList);

    /**
     *
     * @param selectBody
     * @return
     * @throws UnsupportedSelectQueryRuntimeException
     * @throws InvalidSelectQueryRuntimeException
     */

    protected PlainSelect getPlainSelect(Select selectBody) {
        // other subclasses of Select are
        //      SelectOperationList (INTERSECT, EXCEPT, MINUS, UNION),
        //      ValuesStatement (VALUES)
        //      WithItem ([RECURSIVE]...)

        if (!(selectBody instanceof PlainSelect))
            throw new UnsupportedSelectQueryRuntimeException("Complex SELECT statements are not supported", selectBody);

        PlainSelect plainSelect = (PlainSelect) selectBody;

        if (plainSelect.getIntoTables() != null)
            throw new InvalidSelectQueryRuntimeException("SELECT INTO is not allowed in mappings", selectBody);

        return plainSelect;
    }



    /**
     * main method for analysing FROM clauses
     *
     * @param left
     * @param joins
     * @return
     * @throws IllegalJoinException
     */
    protected T translateJoins(FromItem left, List<Join> joins) throws IllegalJoinException {
        if (left == null)
            return operations.create();

        Alias leftAlias = left.getAlias();
        // 5.4 interprets the old malformed "P LEFT, Q" join modifier as an implicit alias.
        if (leftAlias != null && !leftAlias.isUseAs() && "LEFT".equalsIgnoreCase(leftAlias.getName())
                && joins != null && !joins.isEmpty() && joins.get(0).isSimple())
            throw new InvalidSelectQueryRuntimeException("Invalid simple join", joins.get(0));

        T current = translateFromItem(left);
        if (joins != null)
            for (Join join : joins) // no reduce - exception handling
                current = join(current, join);

        return current;
    }

    private T translateFromItem(FromItem fromItem) {
        return new FromItemProcessor().translate(fromItem);
    }

    /**
     * can be overridden to add additional checks
     *
     * @param left expression
     * @param join JSQLParser's Join
     * @return resulting expression
     * @throws IllegalJoinException if incorrect combination of modifiers is used
     */
    protected T join(T left, Join join) throws IllegalJoinException {

        /*  JSQLParser modifiers grammar
          [   LEFT SEMI
            | LEFT [ OUTER ]
            | RIGHT [ OUTER ]
            | FULL [ OUTER ]
            | INNER
            | NATURAL
            | CROSS
            | OUTER ]
          (   JOIN
            | simple "," (OUTER)?
            | STRAIGHT_JOIN
            | APPLY )
         */

        T right = translateFromItem(join.getRightItem());
        if (join.isApply()) {
            // https://docs.microsoft.com/en-us/sql/t-sql/queries/from-transact-sql?view=sql-server-ver15
            // left_table_source { CROSS | OUTER } APPLY right_table_source
            // Specifies that the right_table_source of the APPLY operator is evaluated against every row of
            // the left_table_source. This functionality is useful when the right_table_source contains
            // a table-valued function that takes column values from the left_table_source as one of its arguments.
            if (join.isLeft() || join.isRight() || join.isFull() || join.isSemi()
                    || join.isInner() || join.isNatural()
                    || !join.getOnExpressions().isEmpty() || !join.getUsingColumns().isEmpty())
                throw new InvalidSelectQueryRuntimeException("Invalid APPLY join", join);

            if (!join.isCross() && !join.isOuter())
                throw new InvalidSelectQueryRuntimeException("APPLY must be either CROSS or OUTER", join);

            return operations.crossJoin(left, right);
        }
        if (join.isStraight()) {
            // https://dev.mysql.com/doc/refman/8.0/en/join.html
            // STRAIGHT_JOIN is similar to JOIN, except that the left table is always read before the right table.
            // This can be used for those (few) cases for which the join optimizer processes the tables in a
            // suboptimal order.
            if (join.isLeft() || join.isRight() || join.isFull() || join.isSemi() || join.isOuter()
                    || join.isInner() || join.isNatural() || join.isCross())
                throw new InvalidSelectQueryRuntimeException("Invalid STRAIGHT_JOIN", join);

            // covered below
        }

        if (join.isSimple()) {
            // JSQLParser apparently allows weird combinations like SELECT * FROM P, LEFT Q
            if (join.isLeft() || join.isRight() || join.isFull() || join.isSemi()
                    || join.isInner() || join.isNatural() || join.isCross()
                    || !join.getOnExpressions().isEmpty() || !join.getUsingColumns().isEmpty())
                throw new InvalidSelectQueryRuntimeException("Invalid simple join", join);

            // but FROM P, OUTER Q is supported by Informix
            // https://www.oninit.com/manual/informix/100/sqlt/sqltmst104.htm
            // SELECT c.customer_num, c.lname, c.company,
            //     c.phone, u.call_dtime, u.call_descr
            //   FROM customer c, OUTER cust_calls u
            //   WHERE c.customer_num = u.customer_num
            if (join.isOuter())
                throw new UnsupportedSelectQueryRuntimeException("Simple OUTER join is not supported", join);

            return operations.crossJoin(left, right);
        }
        else if (join.isCross()) {
            if (!join.getOnExpressions().isEmpty() || !join.getUsingColumns().isEmpty())
                throw new InvalidSelectQueryRuntimeException("CROSS JOIN cannot have USING/ON conditions", join);

            return operations.crossJoin(left, right);
        }
        else if (join.isNatural()) {
            if (!join.getOnExpressions().isEmpty() || !join.getUsingColumns().isEmpty())
                throw new InvalidSelectQueryRuntimeException("NATURAL JOIN cannot have USING/ON conditions", join);

            return operations.naturalJoin(left, right);
        }
        else {
            // also covers STRAIGHT_JOIN
            if (!join.getOnExpressions().isEmpty()) {
                if (!join.getUsingColumns().isEmpty())
                    throw new InvalidSelectQueryRuntimeException("JOIN cannot have both USING and ON", join);

                if (join.isSemi()) {
                    // https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Joins
                    // table_reference LEFT SEMI JOIN table_reference ON expression
                    // TODO: write a proper operation if supported
                    return left;
                }

                Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression =
                        attributes -> join.getOnExpressions().stream()
                                .map(exp -> expressionParser.parseBooleanExpression(exp, attributes))
                                .reduce(termFactory::getConjunction);

                if (join.isLeft())
                    return leftJoinOn(left, right, getAtomOnExpression, join);
                if (join.isRight())
                    return leftJoinOn(right, left, getAtomOnExpression, join);
                if (join.isFull())
                    return fullJoinOn(right, left, getAtomOnExpression, join);

                return operations.joinOn(left, right, getAtomOnExpression);
            }
            else if (!join.getUsingColumns().isEmpty()) {
                if (join.isSemi())
                    throw new InvalidSelectQueryRuntimeException("Invalid SEMI JOIN", join);

                if (join.getUsingColumns().stream().anyMatch(p -> p.getTable() != null))
                    throw new InvalidSelectQueryRuntimeException("JOIN USING columns cannot be qualified", join);

                ImmutableSet<QuotedID> using = join.getUsingColumns().stream()
                        .map(p -> idfac.createAttributeID(p.getColumnName()))
                        .collect(ImmutableCollectors.toSet());

                if (join.isLeft())
                    return leftJoinUsing(left, right, using, join);
                if (join.isRight())
                    return leftJoinUsing(right, left, using, join);
                if (join.isFull())
                    return fullJoinUsing(right, left, using, join);

                return operations.joinUsing(left, right, using);
            }
            else
                throw new InvalidSelectQueryRuntimeException("[INNER|OUTER] JOIN requires either ON or USING", join);
        }
    }

    protected T leftJoinUsing(T left, T right, ImmutableSet<QuotedID> using, Join join) {
        throw new UnsupportedSelectQueryRuntimeException("[LEFT|RIGHT] OUTER join is not supported", join);
    }

    protected T fullJoinUsing(T left, T right, ImmutableSet<QuotedID> using, Join join) {
        throw new UnsupportedSelectQueryRuntimeException("FULL OUTER join is not supported", join);
    }

    protected T leftJoinOn(T left, T right,
                           Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression,
                           Join join) {
        throw new UnsupportedSelectQueryRuntimeException("[LEFT|RIGHT] OUTER join is not supported", join);
    }

    protected T fullJoinOn(T left, T right,
                           Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression,
                           Join join) {
        throw new UnsupportedSelectQueryRuntimeException("FULL OUTER join is not supported", join);
    }

    public ImmutableList<Variable> createAttributeVariables(RelationDefinition relation) {
        relationIndex++;
        return relation.getAttributes().stream()
                .map(attribute -> termFactory.getVariable(attribute.getID().getName() + relationIndex))
                .collect(ImmutableCollectors.toList());
    }

    protected void validateFromItem(Table table) {  }

    private class FromItemProcessor {

        private T result;

        T translate(FromItem fromItem) {
            if (fromItem instanceof Table)
                visit((Table) fromItem);
            else if (fromItem instanceof LateralSubSelect)
                visit((LateralSubSelect) fromItem);
            else if (fromItem instanceof ParenthesedSelect)
                visit((ParenthesedSelect) fromItem);
            else if (fromItem instanceof ParenthesedFromItem)
                visit((ParenthesedFromItem) fromItem);
            else if (fromItem instanceof TableFunction)
                visit((TableFunction) fromItem);
            else
                throw new UnsupportedSelectQueryRuntimeException("FROM item is not supported", fromItem);
            return result;
        }

        public void visit(Table table) {
            if (table.getPivot() != null || table.getUnPivot() != null)
                throw new UnsupportedSelectQueryRuntimeException("PIVOT/UNPIVOT are not supported", table);

            validateFromItem(table);

            RelationID id = JSqlParserTools.getRelationId(idfac, table);
            try {
                NamedRelationDefinition relation = metadata.getRelation(id);
                T rae = create(relation);
                result = (table.getAlias() == null)
                        ? rae
                        : alias(rae, table.getAlias());
            }
            catch (MetadataExtractionException e) {
                throw new InvalidSelectQueryRuntimeException(e.getMessage(), id);
            }
        }


        public void visit(ParenthesedSelect subSelect) {
            if (subSelect.getAlias() == null || subSelect.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-SELECT must have an alias", subSelect);

            if (subSelect.getPivot() != null || subSelect.getUnPivot() != null)
                throw new UnsupportedSelectQueryRuntimeException("PIVOT/UNPIVOT are not supported", subSelect);

            Select select = subSelect.getSelect();
            T rae = translateSelect(select, select.getWithItemsList());
            result = alias(rae, subSelect.getAlias());
        }

        public void visit(ParenthesedFromItem subjoin) {
            if (subjoin.getFromItem() instanceof Values)
                throw new UnsupportedSelectQueryRuntimeException("ValuesLists are not supported", subjoin);

            if (subjoin.getJoins() == null || subjoin.getJoins().isEmpty())
                throw new UnsupportedSelectQueryRuntimeException("ParenthesisFromItem are not supported", subjoin);

            if (subjoin.getAlias() == null || subjoin.getAlias().getName() == null)
                throw new InvalidSelectQueryRuntimeException("SUB-JOIN must have an alias", subjoin);

            if (subjoin.getPivot() != null || subjoin.getUnPivot() != null)
                throw new UnsupportedSelectQueryRuntimeException("PIVOT/UNPIVOT are not supported", subjoin);

            try {
                T rae = translateJoins(subjoin.getFromItem(), subjoin.getJoins());
                result = alias(rae, subjoin.getAlias());
            }
            catch (IllegalJoinException e) {
                throw new InvalidSelectQueryRuntimeException(e.toString(), subjoin);
            }
        }

        public void visit(LateralSubSelect lateralSubSelect) {
            throw new UnsupportedSelectQueryRuntimeException("LateralSubSelects are not supported", lateralSubSelect);
        }

        public void visit(TableFunction tableFunction) {
            throw new UnsupportedSelectQueryRuntimeException("TableFunction are not supported", tableFunction);
        }

        private T alias(T rae, Alias alias) {
            if (alias.getAliasColumns() != null)
                throw new UnsupportedSelectQueryRuntimeException("Alias columns are not supported", alias);

            RelationID aliasId = idfac.createRelationID(alias.getName());
            return operations.withAlias(rae, aliasId);
        }
    }
}
