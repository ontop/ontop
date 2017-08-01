package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import javafx.util.Pair;
import org.openrdf.model.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Command(name = "r2rml-cleanup",
        description = "Clean Mappings in R2RML format to be readable by Ontop")
public class OntopR2RMLCleanup extends OntopMappingCleanupRelatedCommand{

    @Option(type = OptionType.COMMAND, name = {"--prettify"}, title = "pretty print",
            description = "Group related triples in TTL together")
    protected boolean PRETTY_PRINT;

    @Option(type = OptionType.COMMAND, name = {"--templates-to-columns"}, title = "reduce templates to columns",
            description = "Replace rr:template with rr:column whenever possible")
    protected boolean REDUCE_TEMPLATES_TO_COLUMNS;

    @Option(type = OptionType.COMMAND, name = {"--replace-to-simple"}, title = "replace to simple SQL",
            description = "Replace rr:sqlQuery with rr:tableName whenever possible")
    protected boolean REPLACE_SIMPLE_SQL;


    @Override
    public void run() {
        try {
            processR2RML(mappingFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void processR2RML(String file) throws Exception {
        boolean noOutputFile = Strings.isNullOrEmpty(outputMappingFile);
        if (noOutputFile) {
            outputMappingFile = file+ ".tmp";
        }
        File tempFile = new File(outputMappingFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
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
                try {
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
                    writer.write(System.lineSeparator());

                    Resource currentSubject = null;
                    for (org.openrdf.model.Statement statement : statements) {
                        Resource subject = statement.getSubject();
                        if (TripleMap.contains(subject)) {
                            if (subject.equals(currentSubject)) {
                                System.out.print(" ;\n    " +  out(statement.getPredicate()) + " " + getO(statement));
                                writer.write(" ;"+System.lineSeparator()+"    " +  out(statement.getPredicate()) + " " + getO(statement));
                            }
                            else {
                                if (currentSubject != null) {
                                    System.out.print(" . \n\n");
                                    writer.write(" ."+System.lineSeparator()+System.lineSeparator());
                                }
                                System.out.print(outID(subject) + "\n    " +  out(statement.getPredicate()) + " " + getO(statement));
                                writer.write(outID(subject) + "\n    " +  out(statement.getPredicate()) + " " + getO(statement));
                                currentSubject = subject;
                            }
                        }
                    }
                    System.out.print(" .\n");
                    writer.write(" ."+System.lineSeparator());
                }
                else {
                    System.out.print("\n");
                    writer.write(System.lineSeparator());

                    Resource currentSubject = null;
                    URI currentPredicate = null;
                    for (org.openrdf.model.Statement statement : statements) {
                        Resource subject = statement.getSubject();
                        URI predicate = statement.getPredicate();
                        if (subject.equals(currentSubject)) {
                            if (predicate.equals(currentPredicate)) {
                                System.out.print(" , " + out(statement.getObject()));
                                writer.write(" , " + out(statement.getObject()));
                            }
                            else {
                                currentPredicate = predicate;
                                System.out.print(" ;\n\t" + out(predicate) + " " + getO(statement));
                                writer.write(" ;\n\t" + out(predicate) + " " + getO(statement));
                            }
                        }
                        else {
                            if (currentSubject != null) {
                                System.out.print(" . \n\n");
                                writer.write(" ."+System.lineSeparator()+System.lineSeparator());
                            }
                            System.out.print(out(subject) + " " + out(predicate) + " " + getO(statement));
                            writer.write(out(subject) + " " + out(predicate) + " " + getO(statement));
                            currentSubject = subject;
                            currentPredicate = predicate;
                        }
                    }
                    System.out.print(" .\n");
                    writer.write(" ."+System.lineSeparator());
                }

            } catch (IOException e) {
                e.printStackTrace();
                new RDFHandlerException(e);
            }
            }

            private final Map<String, String> PREFIXES = new HashMap<>();

            @Override
            public void handleNamespace(String s, String s1) throws RDFHandlerException {
                System.out.println("@prefix " + s + ": <" + s1 + "> .");
                try {
                    writer.write("@prefix " + s + ": <" + s1 + "> .");
                    PREFIXES.put(s1, s + ":");
                } catch (IOException e) {
                    e.printStackTrace();
                    new RDFHandlerException(e);
                }

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
                    for (Map.Entry<String, String> e : PREFIXES.entrySet())
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

        writer.flush();
        writer.close();
    }








}
