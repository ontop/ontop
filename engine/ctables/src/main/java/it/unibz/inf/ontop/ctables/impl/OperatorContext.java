package it.unibz.inf.ontop.ctables.impl;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class OperatorContext {

    private Table<Operator, String, Object> attributes;

    private final Connection connection;

    public OperatorContext(final Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final Operator operator, final String name) {
        return (T) this.attributes.get(operator, name);
    }

    public <T> void setAttribute(final Operator operator, final String name, final T value) {
        this.attributes.put(operator, name, value);
    }

    public Entry<String[], List<Object[]>> evalQuery(final String sql, final Object... args)
            throws SQLException {

        // Allocate variables for query output (relation = signature + tuples)
        String[] signature = null;
        final List<Object[]> tuples = Lists.newArrayList();

        // Use a prepared statement where to inject parameters
        try (final PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            injectParameters(stmt, ImmutableList.of(args));
            try (ResultSet rs = stmt.executeQuery()) {

                // Extract signature from result set metadata
                final ResultSetMetaData meta = rs.getMetaData();
                final int numColumns = meta.getColumnCount();
                signature = new String[numColumns];
                for (int i = 1; i <= numColumns; ++i) {
                    signature[i - 1] = meta.getColumnLabel(i);
                }

                // Extract result tuples
                while (rs.next()) {
                    final Object[] tuple = new Object[numColumns];
                    for (int i = 1; i <= numColumns; ++i) {
                        tuple[i - 1] = rs.getObject(i);
                    }
                    tuples.add(tuple);
                }
            }
        }

        // Build and return the expected relation = signature + tuples object
        return new AbstractMap.SimpleEntry<>(signature, tuples);
    }

    public void evalUpdate(final String sql, final Object... args) throws SQLException {
        evalUpdate(sql, ImmutableList.of(args));
    }

    public void evalUpdate(final String sql, final Iterable<Object[]> args) throws SQLException {

        // Use a parameterized prepared statement
        try (final PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            injectParameters(stmt, args);
            if (Iterables.size(args) > 1) {
                stmt.executeBatch();
            } else {
                stmt.executeUpdate();
            }
        }
    }

    private static void injectParameters(final PreparedStatement stmt,
            final Iterable<Object[]> args) throws SQLException {

        final boolean isBatch = Iterables.size(args) > 1;

        for (final Object[] tuple : args) {
            for (int i = 0; i < tuple.length; ++i) {
                Object arg = tuple[i];
                if (arg instanceof Clob) {
                    // Map Clobs to Strings (to address issue with TEIID)
                    // TODO: fix in the TEIID embedded module
                    final Clob clob = (Clob) arg;
                    arg = clob.getSubString(1, (int) clob.length());
                }
                stmt.setObject(i + 1, arg);
            }
            if (isBatch) {
                stmt.addBatch();
            }
        }
    }

}
