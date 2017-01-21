package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.openrdf.model.*;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by roman on 21/01/2017.
 */
public class MappingFqdnCleanup {


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




    public static void readOBDA() throws Exception {

        String file = "/Users/roman/IdeaProjects/salvo/ontop3/ontop/quest-test/src/test/resources/r2rml/npd-v2-ql_a.obda";

        File f = new File(file);
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
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

                    String selectClause = extractMappingColumnsFromSQL(args, source.substring("source".length()), mapping);
                    if (selectClause != null) {
                        //RelationalExpression re = null;
                        //try {
                        //	SelectQueryParser parser = new SelectQueryParser(metadata);
                        //	re = parser.parse(sql.replace(selectClause, "SELECT * FROM"));
                        //}
                        //catch (Exception e) {
                        //	// ignore all exceptions
                        //	System.err.println("WARNING: " + e);
                        //}

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
                                    //	if (re != null && re.getAttributes().containsKey(
                                    //			new QualifiedAttributeID(null,
                                    //					metadata.getQuotedIDFactory()
                                    //							.createAttributeID(chopped))))
                                    //		sqlRenaming.put(arg, chopped);
                                    //	else
                                    sqlRenaming.put(arg, arg + " AS " + chopped);
                                }
                            }
                        }
                        System.out.println(line);

                        String replacementSelectClause = getRenaming(sqlRenaming, selectClause, mapping);
                        System.out.println(source.replace(selectClause, replacementSelectClause));

                        for (Map.Entry<String, String> r : renaming.entrySet())
                            target = target.replace("{" + r.getKey() + "}", "{" + r.getValue() + "}");
                        System.out.println(target);
                    }
                    else
                        System.err.println("ERROR: cannot find the SELECT clause in " + mapping);
                }
                else
                    System.out.println(line);
            }
        }
    }


    public static void readR2RML() throws Exception {

        File ontDir = new File("src/test/resources");
        String path = ontDir.getAbsolutePath() + "/";

        //String file = path + "npd-factpages-map.r2rml";

        //String file = "/Users/roman/IdeaProjects/salvo/ontop3/ontop/quest-test/src/test/resources/r2rml/npd-v2-ql_test_a.ttl";
        String file = "/Users/roman/IdeaProjects/salvo/ontop3/ontop/quest-test/src/test/resources/example/npd-v2-ql_a.ttl";

        //Connection conn = setupDatabase();
        //final DBMetadata metadata = DBMetadataExtractor.createMetadata(conn);
        //DBMetadataExtractor.loadMetadata(metadata, conn, null);

        RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
        parser.setPreserveBNodeIDs(true);
        InputStream in = new FileInputStream(file);
        URL documentUrl = new URL("file://" + file);
        parser.setRDFHandler(new RDFHandler() {
            Map<BNode, List<org.openrdf.model.Statement>> bmap = new HashMap<>();

            Map<Resource, Resource> logicalTable = new HashMap<>();
            Map<Resource, String> sqlQuery = new HashMap<>();
            Map<Resource, Resource> subjectMap = new HashMap<>();
            Map<Resource, List<Resource>> predicateObjectMap = new HashMap<>();
            Map<Resource, List<Resource>> objectMap = new HashMap<>();
            Map<Resource, List<Resource>> predicateMap = new HashMap<>();
            Map<Resource, String> template = new HashMap<>();
            Map<Resource, String> column = new HashMap<>();
            Set<Resource> TripleMap = new HashSet<>();

            List<org.openrdf.model.Statement> statements = new ArrayList<>();

            final boolean PRETTY_PRINT = false;
            final boolean REDUCE_TEMPLATES_TO_COLUMNS = false;

            @Override
            public void startRDF() throws RDFHandlerException {
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                // renaming
                for (Resource mapping : logicalTable.keySet())
                    performRenamingInTemplates(mapping);


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
                            //RelationalExpression re = null;
                            //try {
                            //	SelectQueryParser parser = new SelectQueryParser(metadata);
                            //	re = parser.parse(sql.replace(selectClause, "SELECT * FROM"));
                            //}
                            //catch (Exception e) {
                            //	// ignore all exceptions
                            //	System.err.println("WARNING: " + e);
                            //}

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
                                        //	if (re != null && re.getAttributes().containsKey(
                                        //			new QualifiedAttributeID(null,
                                        //					metadata.getQuotedIDFactory()
                                        //							.createAttributeID(chopped))))
                                        //		sqlRenaming.put(arg, chopped);
                                        //	else
                                        sqlRenaming.put(arg, arg + " AS " + chopped);
                                    }
                                }
                            }

                            String replacementSelectClause = getRenaming(sqlRenaming, selectClause, outID(mapping));
                            sqlQuery.put(table, sql.replace(selectClause, replacementSelectClause));
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

}
