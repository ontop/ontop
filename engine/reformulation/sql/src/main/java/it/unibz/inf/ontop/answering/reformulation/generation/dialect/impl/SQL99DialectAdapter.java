package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;


import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.Set;

public class SQL99DialectAdapter implements SQLDialectAdapter {

    public SQL99DialectAdapter() {
    }

    protected String escapedSingleQuote() {
        return "''";
    }

    protected String strConcat(String[] strings) {
        if (strings.length == 0)
            throw new IllegalArgumentException("Cannot concatenate 0 strings");

        if (strings.length == 1)
            return strings[0];

        StringBuilder sql = new StringBuilder();

        sql.append(String.format("(%s", strings[0]));
        for (int i = 1; i < strings.length; i++) {
            sql.append(String.format(" || %s", strings[i]));
        }
        sql.append(")");
        return sql.toString();
    }

    @Override
    public String sqlQuote(String name) {
        //TODO: This should depend on quotes in the sql in the mappings
        return String.format("\"%s\"", name);
//		return name;
    }

    /**
     * There is no standard for this part.
     * <p>
     * Arbitrary default implementation proposed
     * (may not work with many DB engines).
     */
    @Override
    public String sqlSlice(long limit, long offset) {
        if ((limit < 0) && (offset < 0)) {
            return "";
        } else if ((limit >= 0) && (offset >= 0)) {
            return String.format("LIMIT %d, %d", offset, limit);
        } else if (offset < 0) {
            return String.format("LIMIT %d", limit);
        }
        // Else -> (limit < 0)
        else {
            return String.format("OFFSET %d", offset);
        }
    }

    @Override
    public String nameTopVariable(String signatureVariableName, Set<String> sqlVariableNames) {
        return sqlQuote(buildDefaultName("", signatureVariableName, ""));
    }

    @Override
    public String render(DBConstant constant) {
        DBTermType dbType = constant.getType();

        switch (dbType.getCategory()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT_DOUBLE:
                // TODO: handle the special case of not-a-number!
            case BOOLEAN:
                return constant.getValue();
            default:
                return getSQLLexicalFormString(constant.getValue());
        }
    }

    /**
     * Concatenates the strings.
     * Default way to name a variable or a view.
     * <p>
     * Returns an UNQUOTED string.
     */
    protected final String buildDefaultName(String prefix, String intermediateName, String suffix) {
        return prefix + intermediateName + suffix;
    }

    @Override
    public Optional<String> getTrueTable() {
        return Optional.empty();
    }

    /**
     * By default, quotes and escapes isolated single quotes
     */
    protected String getSQLLexicalFormString(String constant) {

        return "'" + constant.replaceAll("(?<!')'(?!')", escapedSingleQuote()) + "'";
    }

    protected String getSQLLexicalFormBoolean(boolean value) {
        // TODO: check whether this implementation inherited from JDBCUtility is correct
        return value ? "TRUE" : "FALSE";
    }

    /***
     * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
     * The method will strip any fractional seconds found in the date time
     * (since we haven't found a nice way to support them in all databases). It
     * will also normalize the use of Z to the timezome +00:00 and last, if the
     * database is H2, it will remove all timezone information, since this is
     * not supported there.
     *
     */
    protected String getSQLLexicalFormDatetime(String v) {
        // TODO: check whether this implementation inherited from JDBCUtility is correct

        String datetime = v.replace('T', ' ');
        int dotlocation = datetime.indexOf('.');
        int zlocation = datetime.indexOf('Z');
        int minuslocation = datetime.indexOf('-', 10); // added search from 10th pos, because we need to ignore minuses in date
        int pluslocation = datetime.indexOf('+');
        StringBuilder bf = new StringBuilder(datetime);
        if (zlocation != -1) {
            /*
             * replacing Z by +00:00
             */
            bf.replace(zlocation, bf.length(), "+00:00");
        }

        if (dotlocation != -1) {
            /*
             * Stripping the string from the presicion that is not supported by
             * SQL timestamps.
             */
            // TODO we need to check which databases support fractional
            // sections (e.g., oracle,db2, postgres)
            // so that when supported, we use it.
            int endlocation = Math.max(zlocation, Math.max(minuslocation, pluslocation));
            if (endlocation == -1) {
                endlocation = datetime.length();
            }
            bf.replace(dotlocation, endlocation, "");
        }
        bf.insert(0, "'");
        bf.append("'");

        return bf.toString();
    }
}
