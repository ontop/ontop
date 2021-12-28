package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LiteralImpl;
import org.eclipse.rdf4j.model.impl.StatementImpl;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.rio.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command(name = "v1-to-v3",
        description = "Clean mapping file (from Ontop v1) to be readable by Ontop v3. \n" +
                "You may still need to manually adjust the converted files. \n" +
                "It does the following jobs at our best effort: \n" +
                "  (1) for Ontop native OBDA files, it extracts the jdbc connection info into a separate properties file\n" +
                "  (2) for both Ontop native and R2RML files, for each full qualified columns (e.g `table1.col1`), it generates an alias (e.g `table1.col1 AS table1_col1`")

public class OntopMappingV1ToV3 implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mapping file",
            description = "Mapping file in R2RML (.ttl) or in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.obda",
            description = "Output mapping file in R2RML (.ttl) or in Ontop native format (.obda)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String outputMappingFile;


    @Option(type = OptionType.COMMAND, name = {"--simplify-projection"},
            description = "Replace projection with * whenever possible in SQL queries")
    protected boolean SIMPLIFY_SQL_PROJECTIONS;

    @Option(type = OptionType.COMMAND, name = {"--overwrite"},
            description = "Overwrite the mapping file given as input")
    protected boolean overwriteFile;

    private final Pattern selectPattern = Pattern.compile("SELECT\\W.*\\WFROM", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private final Pattern asPattern = Pattern.compile("\\WAS\\W", Pattern.CASE_INSENSITIVE);
    private enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }

    //Group related triples in TTL together
    private final boolean PRETTY_PRINT = true;

    //Replace rr:template with rr:column whenever possible
    private final boolean REDUCE_TEMPLATES_TO_COLUMNS = true;

    //Replace rr:sqlQuery with rr:tableName whenever possible
    private final boolean REPLACE_SIMPLE_SQL = true;


    @Override
    public void run() {
        try {
            boolean noOutputFile = Strings.isNullOrEmpty(outputMappingFile);
            if (noOutputFile && overwriteFile) {
                outputMappingFile = mappingFile + ".tmp";
            }

            Objects.requireNonNull(outputMappingFile, "Output mapping file cannot be null");

            File inputFile = new File(mappingFile);
            File outputFile = new File(outputMappingFile);

            if (mappingFile.endsWith(".obda")) {
                processOBDA(inputFile, outputFile);
            } else {
                processR2RML(outputFile);
            }

            //rename tmp file with the input file
            if (overwriteFile) {
                if (!outputFile.renameTo(inputFile)) {
                    System.err.println("Could not rename the file");
                }
                outputMappingFile = inputFile.getName();
            }

            System.out.printf("New mapping file %s%n", outputMappingFile);

        } catch (Exception e) {
            System.err.println("Error occurred during v1-to-v3 mapping conversion: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }
    }

    public void processOBDA(File inputFile, File outputFile) throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        try (Scanner sc = new Scanner(inputFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("[SourceDeclaration]")) {
                    if (overwriteFile) {
                        readSourceDeclaration(sc, mappingFile);
                    } else {
                        readSourceDeclaration(sc, outputMappingFile);
                    }
                    continue;
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
                                } else if (renaming.containsValue(chopped)) {
                                    String other = renaming.inverse().get(chopped);
                                    renaming.put(other, chopped + "1");
                                    sqlRenaming.put(other, other + " AS " + chopped + "1");
                                    duplicates.put(chopped, 2);
                                    renaming.put(arg, chopped + "2");
                                    sqlRenaming.put(arg, arg + " AS " + chopped + "2");
                                } else {
                                    renaming.put(arg, chopped);
                                    sqlRenaming.put(arg, arg + " AS " + chopped);
                                }
                            }
                        }
                        writer.write(line + System.getProperty("line.separator"));

                        String replacementSelectClause = getRenaming(sqlRenaming, selectClause, mapping);
                        String resultingSql = sql.replace(selectClause, replacementSelectClause);
                        if (SIMPLIFY_SQL_PROJECTIONS)
                            resultingSql = getSimplifiedProjection(resultingSql);

                        for (Map.Entry<String, String> r : renaming.entrySet())
                            target = target.replace("{" + r.getKey() + "}", "{" + r.getValue() + "}");

                        writer.write(target + System.getProperty("line.separator"));

                        writer.write("source" + resultingSql + System.getProperty("line.separator"));
                    } else
                        System.err.println("ERROR: cannot find the SELECT clause in " + mapping);
                } else {

                    writer.write(line + System.getProperty("line.separator"));
                }
            }
        }

        writer.flush();
        writer.close();


    }

    //read and store datasource information
    private void readSourceDeclaration(Scanner sc, String f) throws IOException {
        String line;
        Properties dataSourceProperties = new Properties();

        while (!(line = sc.nextLine()).isEmpty()) {
            String[] tokens = line.split("[\t| ]+", 2);

            final String parameter = tokens[0].trim();
            final String inputParameter = tokens.length > 1 ? tokens[1].trim() : "";

            if (parameter.equals(Label.connectionUrl.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_URL, inputParameter);
            } else if (parameter.equals(Label.username.name())) {
                dataSourceProperties.put(OntopSQLCredentialSettings.JDBC_USER, inputParameter);
            } else if (parameter.equals(Label.password.name())) {
                dataSourceProperties.put(OntopSQLCredentialSettings.JDBC_PASSWORD, inputParameter);
            } else if (parameter.equals(Label.driverClass.name())) {
                dataSourceProperties.put(OntopSQLCoreSettings.JDBC_DRIVER, inputParameter);

            } else {
                String msg = String.format("Unknown parameter name \"%s\"", parameter);
                throw new IOException(msg);
            }
        }

        String propertyFilePath = f.substring(0, f.lastIndexOf(".")) + ".properties";
        try (FileOutputStream outputStream = new FileOutputStream(new File(propertyFilePath))) {
            dataSourceProperties.store(outputStream, null);
        }
    }

    private void processR2RML(File outputFile) throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.setPreserveBNodeIDs(true);
        InputStream in = new FileInputStream(mappingFile);
        URL documentUrl = new URL("file://" + mappingFile);
        parser.setRDFHandler(new RDFHandler() {
            Map<BNode, List<Statement>> bmap = new HashMap<>();

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

            List<Statement> statements = new ArrayList<>();

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
                try {
                    // renaming
                    for (Resource mapping : logicalTable.keySet())
                        performRenamingInTemplates(mapping);

                    Map<String, List<Resource>> idmap = new HashMap<>();
                    for (Resource mapping : TripleMap) {
                        Resource lt = logicalTable.get(mapping);
                        String sql = sqlQuery.get(lt);
                        if (sql == null)
                            sql = tableName.get(lt);

                        Resource subj = subjectMap.get(mapping);
                        String tmpl = template.get(subj);
                        if (tmpl == null)
                            tmpl = column.get(subj);

                        String key = sql+tmpl;
                        if (!idmap.containsKey(key))
                            idmap.put(key, new ArrayList<>());
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
                                } else if (statement.getPredicate().toString().equals("http://www.w3.org/ns/r2rml#subjectMap")) {
                                    mergeBmapInto((Resource) object, sm);
                                    object = sm;
                                }
                                Statement st = new StatementImpl(to, statement.getPredicate(), object);
                                if (!lst.contains(st))
                                    lst.add(st);
                            }
                        }
                    }

                    lst.sort(Comparator.comparing((Statement s) -> s.getSubject().toString())
                            .thenComparing(s -> s.getPredicate().toString())
                            .thenComparing(s -> s.getObject().toString()));

                    statements = lst;

                    if (PRETTY_PRINT) {
                        writer.write(System.lineSeparator());

                        Resource currentSubject = null;
                        for (Statement statement : statements) {
                            Resource subject = statement.getSubject();
                            if (TripleMap.contains(subject)) {
                                if (subject.equals(currentSubject)) {

                                    writer.write(" ;" + System.lineSeparator() + "    " + out(statement.getPredicate()) + " " + getO(statement));
                                } else {
                                    if (currentSubject != null) {
                                        writer.write(" ." + System.lineSeparator() + System.lineSeparator());
                                    }
                                    writer.write(outID(subject) + "\n    " + out(statement.getPredicate()) + " " + getO(statement));
                                    currentSubject = subject;
                                }
                            }
                        }

                        writer.write(" ." + System.lineSeparator());
                    } else {

                        writer.write(System.lineSeparator());

                        Resource currentSubject = null;
                        URI currentPredicate = null;
                        for (Statement statement : statements) {
                            Resource subject = statement.getSubject();
                            URI predicate = statement.getPredicate();
                            if (subject.equals(currentSubject)) {
                                if (predicate.equals(currentPredicate)) {

                                    writer.write(" , " + out(statement.getObject()));
                                } else {
                                    currentPredicate = predicate;

                                    writer.write(" ;\n\t" + out(predicate) + " " + getO(statement));
                                }
                            } else {
                                if (currentSubject != null) {
                                    writer.write(" ." + System.lineSeparator() + System.lineSeparator());
                                }

                                writer.write(out(subject) + " " + out(predicate) + " " + getO(statement));
                                currentSubject = subject;
                                currentPredicate = predicate;
                            }
                        }

                        writer.write(" ." + System.lineSeparator());
                    }

                } catch (IOException e) {
                    System.err.println("Error during process: " + e.getMessage());
                    e.printStackTrace();

                }
            }

            private final Map<String, String> PREFIXES = new HashMap<>();

            @Override
            public void handleNamespace(String s, String s1) throws RDFHandlerException {

                try {
                    writer.write("@prefix " + s + ": <" + s1 + "> .");
                    PREFIXES.put(s1, s + ":");
                } catch (IOException e) {
                    System.err.println("Error during process: " + e.getMessage());
                    e.printStackTrace();

                }

            }

            @Override
            public void handleStatement(Statement statement) throws RDFHandlerException {

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
                    List<Statement> list = bmap.computeIfAbsent((BNode) statement.getSubject(), k -> new ArrayList<>());
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
                List<Resource> list = map.computeIfAbsent(subject, k -> new ArrayList<>());
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
                                    } else if (renaming.containsValue(chopped)) {
                                        String other = renaming.inverse().get(chopped);
                                        renaming.put(other, chopped + "1");
                                        sqlRenaming.put(other, other + " AS " + chopped + "1");
                                        duplicates.put(chopped, 2);
                                        renaming.put(arg, chopped + "2");
                                        sqlRenaming.put(arg, arg + " AS " + chopped + "2");
                                    } else {
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
                        } else
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

            private String getO(Statement statement) {
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
                    String uri = ((URI) value).toString();
                    if (uri.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        return "a";
                    for (Map.Entry<String, String> e : PREFIXES.entrySet())
                        if (uri.startsWith(e.getKey()))
                            return e.getValue() + uri.substring(e.getKey().length());
                    return "<" + uri + ">";
                } else if (value instanceof BNode) {
                    if (PRETTY_PRINT) {
                        String s = "";
                        String saved_pref = pref;
                        pref += "    ";
                        List<Statement> list = bmap.get(value);
                        boolean first = true;
                        URI current_pred = null;
                        for (Statement st : list) {
                            if (current_pred == st.getPredicate()) {
                                s += ", " + getO(st);
                                continue;
                            }
                            current_pred = st.getPredicate();
                            if (!first)
                                s += " ;\n";
                            first = false;

                            s += pref + out(current_pred) + " " + getO(st);
                        }
                        pref = saved_pref;
                        return "[\n" + s + "\n" + pref + "]";
                    }
                } else if (value instanceof Literal) {
                    Literal literal = (Literal) value;
//                    String s = literal.toString();
//                    if (s.contains("\n"))
//                        return "\"\"" + s + "\"\"";
                    String s = literal.stringValue();
                    // this list might need to be expanded

                    s = s.replace("\"", "\\\"");

//                    if(s.contains("\n") || s.contains("\"")){
//                        s = "\"\"\"" + s + "\"\"\"";
//                    } else{
                    s = "\"" + s + "\"";
                    //}

                    // TODO: add property datatypes when present

                    return s;
                }
                return value.toString();
            }

            @Override
            public void handleComment(String s) throws RDFHandlerException {

            }
        });
        parser.parse(in, documentUrl.toString());

        writer.flush();
        writer.close();
    }

      /*
        returns colum names in a given template
     */

    protected  Set<String> getTemplateColumns(String template) {
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

    protected   String extractMappingColumnsFromSQL(Set<String> args, String sql, String mapping) {
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

    protected String getRenaming(Map<String, String> renaming, String selectClause, String mapping) {
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

    protected  String getSimplifiedProjection(String sql) {
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

    protected  String extractSimpleTable(String sql) {
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
