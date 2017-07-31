package it.unibz.inf.ontop.cli;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import javafx.util.Pair;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OntopMappingCleanup {

    // global options
    final static boolean SIMPLIFY_SQL_PROJECTIONS = true; // replace projection with * whenever possible in SQL queries
    // R2RML options only
    final static boolean PRETTY_PRINT = true; // group related triples in TTL together
    final static boolean REDUCE_TEMPLATES_TO_COLUMNS = true;  // replace rr:template with rr:column whenever possible
    final static boolean REPLACE_SIMPLE_SQL = true;  // replace rr:sqlQuery with rr:tableName whenever possible

    private enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }
    // TWO ENTRY POINTS: processOBDA and processR2RML for obda and r2rml/ttl files, respectively

    public static void main(String[] args) {
        try {
            //processOBDA("/Users/roman/Project/code/ontop3/ontop/ontop-cli/target/test-classes/npd-v2-ql-mysql-ontop1.17.obda");

            File ontDir = new File("src/test/resources");
            String path = ontDir.getAbsolutePath() + "/";
            String file = path + "bootstrapped-univ-benchQL.obda";

            processOBDA(file);
//            processR2RML(file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void processOBDA(String file) throws Exception {

        File inputFile = new File(file);
        File tempFile = new File(file+ ".tmp");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        try (Scanner sc = new Scanner(inputFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains("[SourceDeclaration]")){
                    readSourceDeclaration(sc, inputFile);
                }
                if (line.startsWith("mappingId")) {
                    String target = sc.nextLine();
                    String source = sc.nextLine();
                    if (!target.startsWith("target") || !source.startsWith("source")) {
                        System.err.println("ERROR: badly formatted " + line);
                        continue;
                    }
                    String mapping = line.substring("mappingId".length());

                    Set<String> args = new HashSet<>();
                    for (String template : target.split("\\s"))
                        args.addAll(getTemplateColumns(template));

                    String sql = source.substring("source".length());
                    String selectClause = extractMappingColumnsFromSQL(args, sql, mapping);
                    if (selectClause != null) {
                        BiMap<String, String> renaming = HashBiMap.create();
                        Map<String, String> sqlRenaming = new HashMap<>();
                        Map<String, Integer> duplicates = new HashMap<>();
                        for (String arg : args) {
                            if (arg.contains(".")) {
                                String chopped = arg.substring(arg.lastIndexOf('.') + 1);
                                if (duplicates.containsKey(chopped)) {
                                    int count = duplicates.get(chopped) + 1;
                                    renaming.put(arg, chopped + count);
                                    sqlRenaming.put(arg, arg + " AS " + chopped + count);
                                    duplicates.put(chopped, count);
                                }
                                else if (renaming.containsValue(chopped)) {
                                    String other = renaming.inverse().get(chopped);
                                    renaming.put(other, chopped + "1");
                                    sqlRenaming.put(other, other + " AS " + chopped + "1");
                                    duplicates.put(chopped, 2);
                                    renaming.put(arg, chopped + "2");
                                    sqlRenaming.put(arg, arg + " AS " + chopped + "2");
                                }
                                else {
                                    renaming.put(arg, chopped);
                                    sqlRenaming.put(arg, arg + " AS " + chopped);
                                }
                            }
                        }
                        System.out.println(line);
                        writer.write(line + System.getProperty("line.separator"));

                        String replacementSelectClause = getRenaming(sqlRenaming, selectClause, mapping);
                        String resultingSql = sql.replace(selectClause, replacementSelectClause);
                        if (SIMPLIFY_SQL_PROJECTIONS)
                            resultingSql = getSimplifiedProjection(resultingSql);
                        System.out.println("source" + resultingSql);
                        writer.write("source" + resultingSql + System.getProperty("line.separator"));

                        for (Map.Entry<String, String> r : renaming.entrySet())
                            target = target.replace("{" + r.getKey() + "}", "{" + r.getValue() + "}");
                        System.out.println(target);
                        writer.write( target + System.getProperty("line.separator"));
                    }
                    else
                        System.err.println("ERROR: cannot find the SELECT clause in " + mapping);
                }
                else {
                    System.out.println(line);
                    writer.write(line + System.getProperty("line.separator"));
                }
            }
        }

        writer.flush();
        writer.close();
        if (!tempFile.renameTo(inputFile))
            System.out.println("Could not rename file");


}
    //read and store datasource information
    private static void readSourceDeclaration(Scanner sc, File f) throws IOException {
        String line;
        Properties dataSourceProperties  = new Properties();

        while (!(line = sc.nextLine()).isEmpty()) {
            String[] tokens = line.split("[\t| ]+", 2);

            final String parameter = tokens[0].trim();
            final String inputParameter = tokens[1].trim();

            if (parameter.equals(Label.sourceUri.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_NAME, inputParameter);
            } else if (parameter.equals(Label.connectionUrl.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_URL, inputParameter);
            } else if (parameter.equals(Label.username.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_USER, inputParameter);
            } else if (parameter.equals(Label.password.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_PASSWORD, inputParameter);
            } else if (parameter.equals(Label.driverClass.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_DRIVER, inputParameter);

            } else {
                String msg = String.format("Unknown parameter name \"%s\"", parameter);
                throw new IOException(msg);
            }
        }
        String absolutePath = f.getAbsolutePath();
        String propertyFilePath = absolutePath.substring(0, absolutePath.lastIndexOf("."))+ ".properties";
        File propertyFile = new File(propertyFilePath);
        FileOutputStream outputStream = new FileOutputStream(propertyFile);
        dataSourceProperties.store(outputStream, null);
        outputStream.flush();
        outputStream.close();

    }

    public static void processR2RML(String file) throws Exception {
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.setPreserveBNodeIDs(true);
        InputStream in = new FileInputStream(file);
        URL documentUrl = new URL("file://" + file);
        parser.setRDFHandler(new RDFHandler() {
            Map<BNode, List<org.openrdf.model.Statement>> bmap = new HashMap<>();

            Map<Resource, Resource> logicalTable = new HashMap<>();
            Map<Resource, String> sqlQuery = new HashMap<>();
            Map<Resource, String> tableName = new HashMap<>();
            Map<Resource, Resource> subjectMap = new HashMap<>();
            Map<Resource, List<Resource>> predicateObjectMap = new HashMap<>();
            Map<Resource, List<Resource>> objectMap = new HashMap<>();
            Map<Resource, List<Resource>> predicateMap = new HashMap<>();
            Map<Resource, String> template = new HashMap<>();
            Map<Resource, String> column = new HashMap<>();
            Set<Resource> TripleMap = new HashSet<>();

            List<org.openrdf.model.Statement> statements = new ArrayList<>();

            private void addToSetIgnoreSubject(List<Statement> list, Statement st) {
                for (Statement s : list)
                    if (s.getPredicate().equals(st.getPredicate()) && s.getObject().equals(st.getObject()))
                        return;
                list.add(st);
            }

            private void mergeBmapInto(Resource s, Resource t) {
                List<Statement> list = bmap.get(t);
                for (Statement statement : bmap.get(s))
                    addToSetIgnoreSubject(list, statement);
            }

            @Override
            public void startRDF() throws RDFHandlerException {
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                // renaming
                for (Resource mapping : logicalTable.keySet())
                    performRenamingInTemplates(mapping);

                Map<Pair<String, String>, List<Resource>> idmap = new HashMap<>();
                for (Resource mapping : TripleMap) {
                    Resource lt = logicalTable.get(mapping);
                    String sql = sqlQuery.get(lt);
                    if (sql == null)
                        sql = tableName.get(lt);

                    Resource subj = subjectMap.get(mapping);
                    String tmpl = template.get(subj);
                    if (tmpl == null)
                        tmpl = column.get(subj);

                    Pair<String, String> key = new Pair(sql, tmpl);
                    if (!idmap.containsKey(key))
                        idmap.put(key, new ArrayList<Resource>());
                    idmap.get(key).add(mapping);
                }

                List<Statement> lst = new ArrayList<>();


                // REORDER / GROUP
                for (List<Resource> ids : idmap.values()) {
                    ids.sort(Comparator.comparing(this::outID));
                    Resource id = ids.get(0);
                    String nid = id.toString();
                    nid = nid.substring(0, nid.length() - 5);
                    nid += Joiner.on("-").join(ids.stream()
                            .map(i -> i.toString().substring(i.toString().length() - 5, i.toString().length()))
                            .collect(Collectors.toList()));
                    URIImpl to = new URIImpl(nid);
                    Resource lt = logicalTable.get(id);
                    logicalTable.put(to, lt);
                    Resource sm = subjectMap.get(id);
                    subjectMap.put(to, sm);
                    TripleMap.add(to);

                    for (Statement statement : statements) {
                        Resource s = statement.getSubject();
                        if (ids.contains(s)) {
                            Value object = statement.getObject();
                            if (statement.getPredicate().toString().equals("http://www.w3.org/ns/r2rml#logicalTable")) {
                                mergeBmapInto((Resource) object, lt);
                                object = lt;
                            }
                            else if (statement.getPredicate().toString().equals("http://www.w3.org/ns/r2rml#subjectMap")) {
                                mergeBmapInto((Resource) object, sm);
                                object = sm;
                            }
                            Statement st = new StatementImpl(to, statement.getPredicate(), object);
                            if (!lst.contains(st))
                                lst.add(st);
                        }
                    }
                }

                lst.sort((s1, s2) -> {
                    int i = s1.getSubject().toString().compareTo(s2.getSubject().toString());
                    if (i == 0)
                        i = s1.getPredicate().toString().compareTo(s2.getPredicate().toString());
                    if (i == 0)
                        i = s1.getObject().toString().compareTo(s2.getObject().toString());
                    return i;
                });

                statements = lst;


                if (PRETTY_PRINT) {
                    System.out.print("\n");

                    Resource currentSubject = null;
                    for (org.openrdf.model.Statement statement : statements) {
                        Resource subject = statement.getSubject();
                        if (TripleMap.contains(subject)) {
                            if (subject.equals(currentSubject)) {
                                System.out.print(" ;\n    " +  out(statement.getPredicate()) + " " + getO(statement));
                            }
                            else {
                                if (currentSubject != null)
                                    System.out.print(" . \n\n");
                                System.out.print(outID(subject) + "\n    " +  out(statement.getPredicate()) + " " + getO(statement));
                                currentSubject = subject;
                            }
                        }
                    }
                    System.out.print(" .\n");
                }
                else {
                    System.out.print("\n");

                    Resource currentSubject = null;
                    URI currentPredicate = null;
                    for (org.openrdf.model.Statement statement : statements) {
                        Resource subject = statement.getSubject();
                        URI predicate = statement.getPredicate();
                        if (subject.equals(currentSubject)) {
                            if (predicate.equals(currentPredicate))
                                System.out.print(" , " + out(statement.getObject()));
                            else {
                                currentPredicate = predicate;
                                System.out.print(" ;\n\t" + out(predicate) + " " + getO(statement));
                            }
                        }
                        else {
                            if (currentSubject != null)
                                System.out.print(" . \n\n");
                            System.out.print(out(subject) + " " + out(predicate) + " " + getO(statement));
                            currentSubject = subject;
                            currentPredicate = predicate;
                        }
                    }
                    System.out.print(" .\n");
                }
            }

            private final Map<String, String> PREFXIES = new HashMap<>();

            @Override
            public void handleNamespace(String s, String s1) throws RDFHandlerException {
                System.out.println("@prefix " + s + ": <" + s1 + "> .");
                PREFXIES.put(s1, s + ":");
            }

            @Override
            public void handleStatement(org.openrdf.model.Statement statement) throws RDFHandlerException {

                // replace trivial templates

                if (REDUCE_TEMPLATES_TO_COLUMNS &&
                        statement.getPredicate().toString().equals("http://www.w3.org/ns/r2rml#template")) {
                    String template = statement.getObject().stringValue();
                    if (template.startsWith("{") &&
                            template.endsWith("}") &&
                            !template.substring(1, template.length() - 1).contains("{")) {
                        statement = new StatementImpl(statement.getSubject(),
                                new URIImpl("http://www.w3.org/ns/r2rml#column"),
                                new LiteralImpl(template.substring(1, template.length() - 1)));
                    }
                }

                if (REPLACE_SIMPLE_SQL &&
                        statement.getPredicate().toString().equals("http://www.w3.org/ns/r2rml#sqlQuery")) {
                    String sql = statement.getObject().stringValue();
                    String table = extractSimpleTable(sql);
                    if (table != null)
                        statement = new StatementImpl(statement.getSubject(),
                                new URIImpl("http://www.w3.org/ns/r2rml#tableName"),
                                new LiteralImpl(table));
                }

                if (statement.getSubject() instanceof BNode) {
                    List<org.openrdf.model.Statement> list = bmap.get(statement.getSubject());
                    if (list == null) {
                        list = new ArrayList<>();
                        bmap.put((BNode)statement.getSubject(), list);
                    }
                    list.add(statement);
                }

                // first pass - collecting all the info

                String predicate = statement.getPredicate().toString();
                Value object = statement.getObject();
                Resource subject = statement.getSubject();

                if (object instanceof URI &&
                        object.toString().equals("http://www.w3.org/ns/r2rml#TriplesMap")) {
                    TripleMap.add(subject);
                }

                switch (predicate) {
                    case "http://www.w3.org/ns/r2rml#logicalTable":
                        logicalTable.put(subject, (Resource) object);
                        break;
                    case "http://www.w3.org/ns/r2rml#sqlQuery":
                        sqlQuery.put(subject, object.stringValue());
                        break;
                    case "http://www.w3.org/ns/r2rml#tableName":
                        tableName.put(subject, object.stringValue());
                        break;
                    case "http://www.w3.org/ns/r2rml#subjectMap":
                        subjectMap.put(subject, (Resource) object);
                        break;
                    case "http://www.w3.org/ns/r2rml#column":
                        column.put(subject, object.stringValue());
                        break;
                    case "http://www.w3.org/ns/r2rml#template":
                        template.put(subject, object.stringValue());
                        break;
                    case "http://www.w3.org/ns/r2rml#predicateObjectMap":
                        addToList(predicateObjectMap, subject, object);
                        break;
                    case "http://www.w3.org/ns/r2rml#objectMap":
                        addToList(objectMap, subject, object);
                        break;
                    case "http://www.w3.org/ns/r2rml#predicateMap":
                        addToList(predicateMap, subject, object);
                        break;
                }

                statements.add(statement);
            }

            private void addToList(Map<Resource, List<Resource>> map, Resource subject, Value object) {
                List<Resource> list = map.get(subject);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(subject, list);
                }
                list.add((Resource) object);
            }

            private String pref = "    ";


            private Set<String> getMappingColumnsFromTemplates(Resource mapping) {
                Set<String> columns = new HashSet<>();
                extractMappingColumnsFromTemplates(columns, ImmutableList.of(subjectMap.get(mapping)));
                List<Resource> pomaps = predicateObjectMap.get(mapping);
                if (pomaps != null)
                    for (Resource pomap : pomaps) {
                        extractMappingColumnsFromTemplates(columns, predicateMap.get(pomap));
                        extractMappingColumnsFromTemplates(columns, objectMap.get(pomap));
                    }
                return columns;
            }

            private void extractMappingColumnsFromTemplates(Set<String> args, List<Resource> resources) {
                if (resources != null)
                    for (Resource resource : resources) {
                        String temp = template.get(resource);
                        if (temp != null)
                            args.addAll(getTemplateColumns(temp));
                        String col = column.get(resource);
                        if (col != null)
                            args.add(col);
                    }
            }

            private void performRenamingInTemplates(Resource mapping) {

                Resource table = logicalTable.get(mapping);
                if (table != null) {
                    String sql = sqlQuery.get(table);
                    if (sql != null) {

                        Set<String> args = getMappingColumnsFromTemplates(mapping);

                        String selectClause = extractMappingColumnsFromSQL(args, sql, outID(mapping));
                        if (selectClause != null) {
                            BiMap<String, String> renaming = HashBiMap.create();
                            Map<String, String> sqlRenaming = new HashMap<>();
                            Map<String, Integer> duplicates = new HashMap<>();
                            for (String arg : args) {
                                if (arg.contains(".")) {
                                    String chopped = arg.substring(arg.lastIndexOf('.') + 1);
                                    if (duplicates.containsKey(chopped)) {
                                        int count = duplicates.get(chopped) + 1;
                                        renaming.put(arg, chopped + count);
                                        sqlRenaming.put(arg, arg + " AS " + chopped + count);
                                        duplicates.put(chopped, count);
                                    }
                                    else if (renaming.containsValue(chopped)) {
                                        String other = renaming.inverse().get(chopped);
                                        renaming.put(other, chopped + "1");
                                        sqlRenaming.put(other, other + " AS " + chopped + "1");
                                        duplicates.put(chopped, 2);
                                        renaming.put(arg, chopped + "2");
                                        sqlRenaming.put(arg, arg + " AS " + chopped + "2");
                                    }
                                    else {
                                        renaming.put(arg, chopped);
                                        sqlRenaming.put(arg, arg + " AS " + chopped);
                                    }
                                }
                            }

                            String replacementSelectClause = getRenaming(sqlRenaming, selectClause, outID(mapping));
                            String resultingSql = sql.replace(selectClause, replacementSelectClause);
                            if (SIMPLIFY_SQL_PROJECTIONS)
                                resultingSql = getSimplifiedProjection(resultingSql);
                            sqlQuery.put(table, resultingSql);

                            performRenamingInTemplates(renaming, mapping);
                        }
                        else
                            System.err.println("ERROR: cannot find the SELECT clause for " + outID(mapping));
                    }
                }
            }


            private void performRenamingInTemplates(BiMap<String, String> renaming, Resource mapping) {
                performRenamingInTemplates(renaming, ImmutableList.of(subjectMap.get(mapping)));
                List<Resource> pomaps = predicateObjectMap.get(mapping);
                if (pomaps != null)
                    for (Resource resource : predicateObjectMap.get(mapping)) {
                        performRenamingInTemplates(renaming, predicateMap.get(resource));
                        performRenamingInTemplates(renaming, objectMap.get(resource));
                    }

            }

            private void performRenamingInTemplates(BiMap<String, String> renaming, List<Resource> resources) {
                if (resources != null) {
                    for (Resource resource : resources) {
                        String t = template.get(resource);
                        if (t != null) {
                            for (Map.Entry<String, String> r : renaming.entrySet())
                                t = t.replace("{" + r.getKey() + "}", "{" + r.getValue() + "}");

                            template.put(resource, t);
                        }
                        t = column.get(resource);
                        if (t != null) {
                            for (Map.Entry<String, String> r : renaming.entrySet())
                                if (t.equals(r.getKey()))
                                    t = r.getValue();
                            column.put(resource, t);
                        }
                    }
                }
            }

            private String getO(org.openrdf.model.Statement statement) {
                URI predicate = statement.getPredicate();
                Value object;

                // second pass - actual renaming
                switch (predicate.toString()) {
                    case "http://www.w3.org/ns/r2rml#column":
                        object = new LiteralImpl(column.get(statement.getSubject()));
                        break;
                    case "http://www.w3.org/ns/r2rml#template":
                        object = new LiteralImpl(template.get(statement.getSubject()));
                        break;
                    case "http://www.w3.org/ns/r2rml#sqlQuery":
                        object = new LiteralImpl(sqlQuery.get(statement.getSubject()));
                        break;
                    case "http://www.w3.org/ns/r2rml#tableName":
                        object = new LiteralImpl(tableName.get(statement.getSubject()));
                        break;
                    default:
                        object = statement.getObject();
                }

                return out(object);
            }

            private String outID(Resource resource) {
                if (resource instanceof URI)
                    return out(resource);
                else
                    return resource.toString();
            }

            private String out(Value value) {
                if (value instanceof URI) {
                    String uri = ((URI)value).toString();
                    if (uri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        return "a";
                    for (Map.Entry<String, String> e : PREFXIES.entrySet())
                        if (uri.startsWith(e.getKey()))
                            return e.getValue() + uri.substring(e.getKey().length());
                    return "<" + uri + ">";
                }
                else if (value instanceof BNode) {
                    if (PRETTY_PRINT) {
                        String s = "";
                        String saved_pref = pref;
                        pref += "    ";
                        List<org.openrdf.model.Statement> list = bmap.get(value);
                        boolean first = true;
                        URI current_pred = null;
                        for (org.openrdf.model.Statement st : list) {
                            if (current_pred == st.getPredicate()) {
                                s += ", " + getO(st);
                                continue;
                            }
                            current_pred = st.getPredicate();
                            if (!first)
                                s += " ;\n";
                            first = false;

                            s += pref +  out(current_pred) + " " + getO(st);
                        }
                        pref = saved_pref;
                        return "[\n" + s + "\n" + pref + "]";
                    }
                }
                else if (value instanceof Literal) {
                    Literal literal = (Literal)value;
                    String s = literal.toString();
                    if (s.contains("\n"))
                        return "\"\"" + s + "\"\"";
                }
                return value.toString();
            }

            @Override
            public void handleComment(String s) throws RDFHandlerException {

            }
        });
        parser.parse(in, documentUrl.toString());
    }



    /*
        returns colum names in a given template
     */

    private static Set<String> getTemplateColumns(String template) {
        Set<String> set = new HashSet<>();
        int pos = 0;
        while (true) {
            pos = template.indexOf('{', pos);
            if (pos == -1)
                break;

            if (pos > 0 && template.charAt(pos - 1) == '\\') {
                pos++;
                continue;
            }
            int endpos;
            do {
                endpos = template.indexOf('}', pos);
                if (endpos == -1) {
                    System.err.println("FAIL " + template + " AT " + pos);
                    return set;
                }
            } while (template.charAt(endpos - 1) == '\\');
            set.add(template.substring(pos + 1, endpos));
            pos++;
        }
        return set;
    }

    private static final Pattern selectPattern = Pattern.compile("SELECT\\W.*\\WFROM", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern asPattern = Pattern.compile("\\WAS\\W", Pattern.CASE_INSENSITIVE);

    private static String extractMappingColumnsFromSQL(Set<String> args, String sql, String mapping) {
        Matcher m = selectPattern.matcher(sql);
        if (m.find()) {
            String selectClause = m.group();
            String projection = selectClause.substring("SELECT ".length(), selectClause.length() - " FROM".length());
            String[] columns = projection.split(",");
            for (String column : columns) {
                Matcher mp = asPattern.matcher(column);
                String alias = (mp.find()) ? column.substring(mp.end()) : column;
                alias = alias.replaceAll("\\s", "");
                //if (!args.contains(alias))
                //	System.err.println("WARNING: column \"" + alias + "\" is not used in mapping " + mapping);

                args.add(alias);
            }
            return selectClause;
        }
        return null;
    }

    private static String getRenaming(Map<String, String> renaming, String selectClause, String mapping) {
        String replacementSelectClause = selectClause;

        for (Map.Entry<String, String> r : renaming.entrySet()) {
            Pattern p = Pattern.compile("\\b" + r.getKey() + "\\b", Pattern.DOTALL);
            Matcher mp = p.matcher(replacementSelectClause);
            if (mp.find())
                replacementSelectClause = mp.replaceFirst(r.getValue());
            else
                System.err.println("ERROR: cannot find column " + r.getKey() + " in " + replacementSelectClause + " in mapping " + mapping);
        }

        return replacementSelectClause;
    }



    /*
        replaces projection with * whenever possible
    */

    private static String getSimplifiedProjection(String sql) {
        try {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = (Select)statement;

                PlainSelect plainSelect = (PlainSelect)select.getSelectBody();

                if (plainSelect.getJoins() == null) {
                    boolean ok = true;
                    for (SelectItem si : plainSelect.getSelectItems()) {
                        if (!(si instanceof SelectExpressionItem)) {
                            ok = false;
                            break;
                        }
                        SelectExpressionItem sei = (SelectExpressionItem) si;
                        if (sei.getAlias() != null) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        plainSelect.setSelectItems(ImmutableList.of(new AllColumns()));

                        Matcher m = selectPattern.matcher(sql);
                        if (m.find()) {
                            String selectClause = m.group();
                            String projection = selectClause.substring("SELECT".length(), selectClause.length() - "FROM".length());
                            Pattern endSpaces = Pattern.compile("\\S\\s+\\z", Pattern.DOTALL);
                            Matcher es = endSpaces.matcher(projection);
                            if (es.find())
                                projection = projection.substring(0, projection.length() - es.group().length() + 1);
                            return sql.replace(projection, " *");
                        }
                    }
                }
            }
        }
        catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return sql;
    }

    /*
        returns
            table name if the SQL query contains no join, no where and no column aliases
            null otherwise
     */

    private static String extractSimpleTable(String sql) {
        try {
            net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                Select select = (Select)statement;
                PlainSelect plainSelect = (PlainSelect)select.getSelectBody();
                if (plainSelect.getJoins() == null) {
                    boolean ok = true;
                    for (SelectItem si : plainSelect.getSelectItems()) {
                        if (!(si instanceof SelectExpressionItem)) {
                            ok = false;
                            break;
                        }
                        SelectExpressionItem sei = (SelectExpressionItem) si;
                        if (sei.getAlias() != null) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok && (plainSelect.getWhere() == null))
                        return ((Table)plainSelect.getFromItem()).getName();
                }
            }
        }
        catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return null;
    }


}
