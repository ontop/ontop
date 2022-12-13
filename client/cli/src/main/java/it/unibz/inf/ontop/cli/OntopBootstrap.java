package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.BootConf;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.JoinPairs;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.bootconfparser.BootConfParser;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.dictionary.Dictionary;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.mpbootstrapper.sqlparser.WorkloadParser;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.OntopNativeMappingSerializer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper;
import it.unibz.inf.ontop.spec.mapping.bootstrap.Bootstrapper.BootstrappingResults;
import net.sf.jsqlparser.JSQLParserException;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Command(name = "bootstrap",
        description = "Bootstrap ontology and mapping from the database")
public class OntopBootstrap extends AbstractOntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-b", "--base-iri"}, title = "base IRI",
            description = "Base IRI of the generated mapping")
    @Required
    protected String baseIRI;

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology file",
            description = "Output OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Required
    String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-m", "--mapping"}, title = "mapping file",
            description = "Output mapping file in the Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String mappingFile;

    @Option(type = OptionType.COMMAND, name = {"-a", "--advanced"}, title = "Advanced Mode (MPBoot)",
            description = "Enable patterns-based generation (MPBoot)")
    protected boolean patterns = false;

    @Option(type = OptionType.COMMAND, name = {"-w", "--workload"}, title = "Workload JSON file",
            description = "Query workload [with -a only]")
    protected String workloadFile = "";

    @Option(type = OptionType.COMMAND, name = {"-d", "--definitions"}, title = "Bootstrapper Definitions File",
            description = "Path to the bootstrapper configuration file")
    protected String confFile = "";

    @Override
    public void run() {

        try {
            if (baseIRI.contains("#")) {
                System.err.println("Base IRI cannot contain the character '#'!");
                throw new IllegalArgumentException("Base IRI cannot contain the character '#'!");
            }

            Objects.requireNonNull(owlFile, "ontology file must not be null");

            OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder();

            if (propertiesFile != null)
                builder.propertyFile(propertiesFile);

            if (dbPassword != null)
                builder.jdbcPassword(dbPassword);

            if (dbUrl != null)
                builder.jdbcUrl(dbUrl);

            if (dbUser != null)
                builder.jdbcUser(dbUser);

            if (dbDriver != null)
                builder.jdbcDriver(dbDriver);

            OntopSQLOWLAPIConfiguration configuration = builder.build();

            BootstrappingResults results;
            if( patterns ){
                JoinPairs pairs = new JoinPairs();
                Dictionary dictionary = new Dictionary();
                BootConf.NullValue nullValue = null;
                boolean shEnabled = false;
                String schema = "";
                if( workloadFile.length() > 0 ){
                    pairs = WorkloadFileParser.getWorkloadJoinPairs(workloadFile);
                }
                if( confFile.length() > 0 ){
                    dictionary = BootConfParser.parseDictionary(confFile);
                    shEnabled = BootConfParser.parseEnableSH(confFile);
                    nullValue = BootConfParser.parseNullValue(confFile);
                    schema = BootConfParser.parseSchema(confFile);
                }
                BootConf conf = new BootConf.Builder()
                        .dictionary(dictionary)
                        .joinPairs(pairs)
                        .enableSH(shEnabled)
                        .nullValue(nullValue)
                        .schema(schema)
                        .build();
                Bootstrapper bootstrapper = Bootstrapper.mpBootstrapper();
                results = bootstrapper.bootstrap(configuration, baseIRI, conf);
            } else {
                // We can still specify a DM generation, but on one schema only
                Bootstrapper bootstrapper = Bootstrapper.defaultBootstrapper();
                BootConf.Builder bootConfBuilder = new BootConf.Builder();
                if( confFile.length() > 0){
                    // Only schema option available
                    String schema = BootConfParser.parseSchema(confFile);
                    bootConfBuilder.schema(schema);
                }
                results = bootstrapper.bootstrap(configuration, baseIRI,
                        bootConfBuilder.build());
            }

            File ontologyFile = new File(owlFile);
            File obdaFile = new File(mappingFile);

            OntopNativeMappingSerializer writer = new OntopNativeMappingSerializer();
            writer.write(obdaFile, results.getPPMapping());

            OWLOntology onto = results.getOntology();
            onto.getOWLOntologyManager().saveOntology(onto, new FileDocumentTarget(ontologyFile));
            
        } catch (Exception e) {
            System.err.println("Error occurred during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }

    }
}

class WorkloadFileParser {

    private static class WorkloadJsonEntry {
        private final String query;

        public WorkloadJsonEntry(String query) {
            this.query = query;
        }

        public String getQuery() {
            return this.query;
        }
    }

    public static JoinPairs getWorkloadJoinPairs(String workloadFile) throws IOException, JSQLParserException, InvalidQueryException {
        String json = Files.lines(Paths.get(workloadFile)).collect(Collectors.joining(" "));

        List<String> workload = new ArrayList<>();

        JsonElement jsonElement = JsonParser.parseString(json);

        Gson g = new Gson();

        if (jsonElement.isJsonArray()) {

            JsonArray jsonArray = jsonElement.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                String jsonStringElement = element.toString();
                WorkloadJsonEntry workloadJsonEntry = g.fromJson(jsonStringElement, WorkloadJsonEntry.class);
                workload.add(workloadJsonEntry.getQuery());
            }
        }

        WorkloadParser parser = new WorkloadParser();

        JoinPairs pairs = new JoinPairs();
        for (String query : workload) {
            pairs.unite(parser.parseQuery(query)); // Side effect on empty
        }

        return pairs;
    }
}