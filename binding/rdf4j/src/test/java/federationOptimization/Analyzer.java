package federationOptimization;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import federationOptimization.Tester.*;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopKGQueryException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.iq.node.impl.NativeNodeImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Analyzer {

    @SuppressWarnings("DataFlowIssue")
    public static void main(String... args) throws OBDASpecificationException, OntopKGQueryException, OntopReformulationException, FileNotFoundException {

        Federator federator = Federator.DENODO;
        PrintStream out = System.out;
//        PrintStream out = new PrintStream(new FileOutputStream("src/test/resources/federation/query_analysis_results_" + federator.name().toLowerCase() + ".tsv"));
        boolean tsv = true;

        Table<Optimization, Setting, List<Statistics>> stats = HashBasedTable.create();
        for (Optimization opt : Optimization.values()) {
            for (Setting setting : new Setting[]{Setting.HOM, Setting.HET}) {
                stats.put(opt, setting, evaluate(federator, setting, opt));
            }
        }

        Map<String, Function<Statistics, String>> properties = ImmutableMap.of(
                "# Joins", s -> Integer.toString(s.getNumJoins()),
                "# Fed. Joins", s -> Integer.toString(s.getNumJoinsFederated()),
                "# Unions", s -> Integer.toString(s.getNumUnions()),
                "# Fed. Unions", s -> Integer.toString(s.getNumUnionsFederated()),
                "# SQL Tokens", s -> Integer.toString(s.getNumSqlTokens()),
                "# Nodes", s -> Integer.toString(s.getNumNodes()),
                "Sources", s -> s.getSources().stream().map(Analyzer::rewriteSource).distinct().sorted()
                        .collect(Collectors.joining(",")),
                "Rules", s -> s.getAppliedRules().stream().filter(r -> r != Rule.SJE).map(r -> r.name().toLowerCase()).sorted()
                        .collect(Collectors.joining(",")),
                "Mat. Views", s -> s.getMatViews().stream()
                        .collect(Collectors.groupingBy(e -> e, TreeMap::new, Collectors.counting()))
                        .entrySet().stream()
                        .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                        .collect(Collectors.joining(", "))

        );

        emit(out, "property", "setting", Arrays.stream(Query.values()).map(Enum::name).collect(Collectors.toList()), tsv, false);
        for (Map.Entry<String, Function<Statistics, String>> e : properties.entrySet()) {
            String property = e.getKey();
            Function<Statistics, String> accessor = e.getValue();
            for (Optimization opt : Optimization.values()) {
                List<String> valuesHom = stats.get(opt, Setting.HOM).stream().map(accessor).collect(Collectors.toList());
                List<String> valuesHet = stats.get(opt, Setting.HET).stream().map(accessor).collect(Collectors.toList());
                boolean propertyStart = opt == Optimization.BASE;
                if (valuesHom.equals(valuesHet)) {
                    emit(out, property, opt.name().toLowerCase(), valuesHom, tsv, propertyStart);
                } else {
                    emit(out, property, opt.name().toLowerCase() + " (hom)", valuesHom, tsv, propertyStart);
                    emit(out, property, opt.name().toLowerCase() + " (het)", valuesHet, tsv, false);
                }
            }
        }
    }

    private static void emit(PrintStream out, String property, String setting, List<String> values, boolean tsv, boolean propertyStart) {
        if (tsv) {
            out.println(property + "\t" + setting + "\t" + Joiner.on('\t').join(values));
        } else {
            if (propertyStart) {
                out.println(Strings.repeat("-", 13 + 3 + 13 + 13 * values.size()));
            }
            out.printf("%-13s | %-13s", propertyStart ? property : "", setting);
            for (String value : values) {
                out.printf(" | %10s", value);
            }
            out.println();
        }
    }

    private static String rewriteSource(String source) {
        source = source.replace("\"", "").toLowerCase();
        source = source.equals("smatv") ? "matv" : source.replaceAll("[^0-9]", "");
        return source;
    }

    private static List<Statistics> evaluate(Federator federator, Setting setting, Optimization opt)
            throws OBDASpecificationException, OntopReformulationException, OntopKGQueryException {
        Tester tester = Tester.create(federator, setting, opt);
        List<Statistics> stats = Lists.newArrayListWithCapacity(Query.values().length);
        for (Query query : Query.values()) {
            AtomicReference<IQ> optimizedIq = new AtomicReference<>();
            AtomicReference<IQ> executableIq = new AtomicReference<>();
            Set<Rule> appliedRules = Sets.newHashSet();
            tester.reformulate(query.getSparql(), Listener.create(
                    null, null, optimizedIq::set, executableIq::set, null,
                    appliedRules::add));
            stats.add(new Statistics(optimizedIq.get(), executableIq.get(), appliedRules, tester.getSourceMap()));
        }
        return stats;
    }

    public static class Statistics {


        private int numNodes;

        private int numJoins;

        private int numJoinsFederated;

        private int numUnions;

        private int numUnionsFederated;

        private final int numSqlTokens;

        private final Set<String> sources;

        private final Set<Rule> appliedRules;

        private final List<String> matViews;

        private Statistics(IQ optimizedIq, IQ executableIq, Set<Rule> appliedRules, Map<String, String> sourceMap) {

            String sqlQuery = ((NativeNodeImpl) executableIq.getTree().getChildren().get(0)).getNativeQueryString();

            this.sources = getSources(optimizedIq.getTree(), sourceMap);
            this.numSqlTokens = sqlQuery.split("\\s+").length;
            this.appliedRules = ImmutableSet.copyOf(appliedRules);
            analyze(optimizedIq.getTree(), sourceMap);
            this.matViews = getMatViews(optimizedIq.getTree());
        }

        private void analyze(IQTree tree, Map<String, String> sourceMap) {

            for (IQTree childTree : tree.getChildren()) {
                analyze(childTree, sourceMap);
            }

            QueryNode node = tree.getRootNode();
            int arity = tree.getChildren().size();

            ++numNodes;

            if (node instanceof JoinLikeNode) {
                numJoins += arity - 1;
                if (getSources(tree, sourceMap).size() > 1) {
                    numJoinsFederated += arity - 1;
                }
            }

            if (node instanceof UnionNode) {
                numUnions += arity - 1;
                if (getSources(tree, sourceMap).size() > 1) {
                    numUnionsFederated += arity - 1;
                }
            }
        }

        private Set<String> getSources(IQTree tree, Map<String, String> sourceMap) {
            Set<String> sources = Sets.newHashSet();
            getSources(tree, sourceMap, sources);
            return ImmutableSet.copyOf(sources);
        }

        private void getSources(IQTree tree, Map<String, String> sourceMap, Set<String> sources) {
            QueryNode node = tree.getRootNode();
            if (!tree.isLeaf()) {
                for (IQTree childTree : tree.getChildren()) {
                    getSources(childTree, sourceMap, sources);
                }
            } else if (node instanceof ExtensionalDataNode) {
                String n = ((ExtensionalDataNode) node).getRelationDefinition().getAtomPredicate().toString();
                int idx = n.lastIndexOf('.');
                n = idx < 0 ? n : n.substring(idx + 1);
                String[] keys = new String[]{n, n.startsWith("\"") ? n.substring(1, n.length() - 1) : '"' + n + '"'};
                for (String key : keys) {
                    String source = sourceMap.get(key);
                    if (source != null) {
                        sources.add(source);
                    }
                }
            }
        }

        public int getNumNodes() {
            return numNodes;
        }

        public int getNumJoins() {
            return numJoins;
        }

        public int getNumJoinsFederated() {
            return numJoinsFederated;
        }

        public int getNumUnions() {
            return numUnions;
        }

        public int getNumUnionsFederated() {
            return numUnionsFederated;
        }

        public int getNumSqlTokens() {
            return numSqlTokens;
        }

        public Set<String> getSources() {
            return sources;
        }

        public List<String> getMatViews() {
            return matViews;
        }

        private List<String> getMatViews(IQTree tree) {
            List<String> matViews = Lists.newArrayList();
            getMatViews(tree, matViews);
            return ImmutableList.copyOf(matViews);
        }

        private void getMatViews(IQTree tree, List<String> matViews) {
            String pattern = "\\b\\w*matv\\w*\\b";
            Pattern r = Pattern.compile(pattern);
            String text= tree.toString();
            Matcher m = r.matcher(text);
            while (m.find()) {
                matViews.add(m.group());
            }
        }

        public Set<Rule> getAppliedRules() {
            return appliedRules;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Statistics)) {
                return false;
            }
            Statistics other = (Statistics) obj;
            return numNodes == other.numNodes &&
                    numJoins == other.numJoins &&
                    numJoinsFederated == other.numJoinsFederated &&
                    numUnions == other.numUnions &&
                    numUnionsFederated == other.numUnionsFederated &&
                    numSqlTokens == other.numSqlTokens &&
                    sources.equals(other.sources) &&
                    appliedRules.equals(other.appliedRules);
        }

        @Override
        public int hashCode() {
            return Objects.hash(numNodes, numJoins, numJoinsFederated, numUnions, numUnionsFederated, numSqlTokens,
                    sources, appliedRules);
        }

        @Override
        public String toString() {
            return "Statistics{" +
                    "numNodes=" + numNodes +
                    ", numJoins=" + numJoins +
                    ", numJoinsFederated=" + numJoinsFederated +
                    ", numUnions=" + numUnions +
                    ", numUnionsFederated=" + numUnionsFederated +
                    ", numSqlTokens=" + numSqlTokens +
                    ", sources=" + sources +
                    '}';
        }

    }

}
