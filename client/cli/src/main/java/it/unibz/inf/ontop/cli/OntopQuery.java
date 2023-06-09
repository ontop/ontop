package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Command(name = "query",
        description = "Query the RDF graph exposed by the mapping and the OWL ontology")
public class OntopQuery extends OntopMappingOntologyRelatedCommand {

    @Option(type = OptionType.COMMAND, name = {"-q", "--query"}, title = "queryFile",
            description = "SPARQL SELECT query file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Required
    private String queryFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"},
            title = "output", description = "output file in the CSV format. If not specified, will print the results in the standard output.")
    //@BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    protected String outputFile;

    public OntopQuery() {
    }

    @Override
    public void run() {

        OntopSQLOWLAPIConfiguration.Builder<?> configurationBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .enableOntologyAnnotationQuerying(enableAnnotations);

        if (owlFile != null)
            configurationBuilder.ontologyFile(owlFile);

        if (factFile != null)
            configurationBuilder.factsFile(factFile);

        if (factFormat != null)
            configurationBuilder.factFormat(factFormat.getExtension());

        if (factsBaseIRI != null)
            configurationBuilder.factsBaseIRI(factsBaseIRI);

        if (propertiesFile != null) {
            configurationBuilder.propertyFile(propertiesFile);
        }

        if (isR2rmlFile(mappingFile)) {
            configurationBuilder.r2rmlMappingFile(mappingFile);
        } else {
            configurationBuilder.nativeOntopMappingFile(mappingFile);
        }

        if (dbMetadataFile != null)
            configurationBuilder.dbMetadataFile(dbMetadataFile);

        if (ontopLensesFile != null)
            configurationBuilder.lensesFile(ontopLensesFile);

        if (sparqlRulesFile != null)
            configurationBuilder.sparqlRulesFile(sparqlRulesFile);

        if (dbPassword != null)
            configurationBuilder.jdbcPassword(dbPassword);

        if (dbUrl != null)
            configurationBuilder.jdbcUrl(dbUrl);

        if (dbUser != null)
            configurationBuilder.jdbcUser(dbUser);

        if (dbDriver != null)
            configurationBuilder.jdbcDriver(dbDriver);

        try (OntopRepository repo = OntopRepository.defaultRepository(configurationBuilder.build())) {
            repo.init();
            OntopRepositoryConnection connection = repo.getConnection();

            String query = Files.lines(Paths.get(queryFile)).collect(joining("\n"));

            try (TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query)
                    .evaluate()) {

                OutputStream out = outputFile == null
                        ? System.out
                        : new FileOutputStream(outputFile);

                printResult(out, result);
                if (outputFile != null)
                    out.close();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void printResult(OutputStream out, TupleQueryResult result) throws Exception {
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

        /*
         * Printing the header
         */
        List<String> signature = result.getBindingNames();
        wr.write(String.join(",", signature));
        wr.newLine();

        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            ImmutableList.Builder<String> valueListBuilder = ImmutableList.builder();
            for (String columnName : signature) {
                valueListBuilder.add(
                        Optional.ofNullable(bindingSet.getValue(columnName))
                                .map(Value::stringValue)
                                .map(StringEscapeUtils::escapeCsv)
                                .orElse(""));
            }
            wr.append(String.join(",", valueListBuilder.build()));
            wr.newLine();
        }
        wr.flush();
        wr.close();
    }
}
