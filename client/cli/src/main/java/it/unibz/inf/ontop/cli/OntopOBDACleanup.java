package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.io.*;
import java.util.*;

@Command(name = "obda-cleanup",
        description = "Clean Mappings in .obda format to be readable by Ontop")
public class OntopOBDACleanup extends OntopMappingCleanupRelatedCommand {


    private enum Label {
        /* Source decl.: */sourceUri, connectionUrl, username, password, driverClass,
        /* Mapping decl.: */mappingId, target, source
    }

    @Override
    public void run() {
        try {
            processOBDA(mappingFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processOBDA(String file) throws Exception {

        boolean noOutputFile = Strings.isNullOrEmpty(outputMappingFile);
        if (noOutputFile) {
            outputMappingFile = file+ ".tmp";
        }
        File inputFile = new File(file);
        File tempFile = new File(outputMappingFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        try (Scanner sc = new Scanner(inputFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("[SourceDeclaration]")){
                    if (noOutputFile) {
                        readSourceDeclaration(sc, file);
                    }
                    else {
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

//        renampe tmp file with the input file
//        if(noOutputFile) {
//            if (!tempFile.renameTo(inputFile))
//                System.out.println("Could not rename the file");
//        }

}
    //read and store datasource information
    private void readSourceDeclaration(Scanner sc, String f) throws IOException {
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

        String propertyFilePath = f.substring(0, f.lastIndexOf("."))+ ".properties";
        File propertyFile = new File(propertyFilePath);
        FileOutputStream outputStream = new FileOutputStream(propertyFile);
        dataSourceProperties.store(outputStream, null);
        outputStream.flush();
        outputStream.close();

    }





}
