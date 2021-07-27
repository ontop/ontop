package it.unibz.inf.ontop.ctables;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Engine {

    private final DataSource source;

    private final Ruleset ruleset;

    private AtomicInteger iteration;

    public Engine(final DataSource source, final Ruleset ruleset) {
        this.source = Objects.requireNonNull(source);
        this.ruleset = Objects.requireNonNull(ruleset);
    }

    public void update() throws SQLException {
        // Ensure that multiple threads concurrently calling this method will execute
        // updateInternal only once
        final int iteration = this.iteration.get();
        synchronized (this.iteration) {
            if (this.iteration.get() == iteration) {
                try {
                    updateInternal();
                } finally {
                    this.iteration.incrementAndGet();
                }
            }
        }
    }

    private void updateInternal() throws SQLException {

        try (Connection conn = this.source.getConnection()) {

            for (final String target : this.ruleset.getTargets()) {
                evalUpdate(conn, "DELETE FROM " + target);
            }

            for (final Rule rule : this.ruleset.getRules()) {

                final Relation relation = evalQuery(conn, rule.getSource());

                final String updateSql = new StringBuilder() //
                        .append("INSERT INTO ").append(rule.getTarget()).append(" (") //
                        .append(Joiner.on(", ").join(relation.signature)) //
                        .append(") VALUES (")
                        .append(Joiner.on(", ")
                                .join(Collections.nCopies(relation.signature.length, "?")))
                        .append(")").toString();

                for (final Object[] tuple : relation.tuples) {
                    evalUpdate(conn, updateSql, tuple);
                }
            }
        }
    }

    private static int evalUpdate(final Connection conn, final String sql, final Object... args)
            throws SQLException {
        try (final PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; ++i) {
                stmt.setObject(i + 1, args[i]);
            }
            return stmt.executeUpdate();
        }
    }

    private static Relation evalQuery(final Connection conn, final String sql,
            final Object... args) throws SQLException {

        String[] signature = null;
        final List<Object[]> tuples = Lists.newArrayList();

        try (final PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; ++i) {
                stmt.setObject(i + 1, args[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final ResultSetMetaData meta = rs.getMetaData();
                    final int numColumns = meta.getColumnCount();

                    if (signature == null) {
                        signature = new String[numColumns];
                        for (int i = 1; i <= numColumns; ++i) {
                            signature[i - 1] = meta.getColumnName(i);
                        }
                    }

                    final Object[] tuple = new Object[numColumns];
                    for (int i = 1; i <= numColumns; ++i) {
                        tuple[i - 1] = rs.getObject(i);
                    }

                    tuples.add(tuple);
                }
            }
        }

        return new Relation(signature, tuples);
    }

    public static void main(final String... args) throws IOException {

        final Ruleset ruleset = Ruleset.create(Paths
                .get("/home/corcoglio/projects/2020_huawei_linstar_vkg/code/git/vkg/linstar.yml")
                .toUri().toURL());

        System.out.println(ruleset.toString(true));
        System.out.println(ruleset.getTargets());
    }

    private static class Relation {

        final String[] signature;

        final List<Object[]> tuples;

        Relation(final String[] signature, final List<Object[]> tuples) {
            this.signature = signature;
            this.tuples = tuples;
        }

    }

}
