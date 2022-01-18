package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.OptionType;
import com.github.rvesse.airline.annotations.help.BashCompletion;
import com.github.rvesse.airline.annotations.restrictions.Required;
import com.github.rvesse.airline.help.cli.bash.CompletionBehaviour;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.CachingMetadataLookup;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMappingConverter;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingConverterImpl;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.R2RMLMappingSerializer;
import it.unibz.inf.ontop.spec.sqlparser.RAExpression;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.Var2VarSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Command(name = "to-r2rml",
        description = "Convert ontop native mapping format (.obda) to R2RML format")
public class OntopOBDAToR2RML implements OntopCommand {

    @Option(type = OptionType.COMMAND, name = {"-i", "--input"}, title = "mapping.obda",
            description = "Input mapping file in Ontop native format (.obda)")
    @Required
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String inputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-t", "--ontology"}, title = "ontology.owl",
            description = "OWL ontology file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Nullable // optional
    private String owlFile;

    @Option(type = OptionType.COMMAND, name = {"-o", "--output"}, title = "mapping.ttl",
            description = "Output mapping file in R2RML format (.ttl)")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    private String outputMappingFile;

    @Option(type = OptionType.COMMAND, name = {"-p", "--properties"}, title = "properties file",
            description = "Properties file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Nullable // optional
    private String propertiesFile;

    @Option(type = OptionType.COMMAND, name = {"-d", "--db-metadata"}, title = "db-metadata file",
            description = "User-supplied db-metadata file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String dbMetadataFile;

    @Option(type = OptionType.COMMAND, name = {"-v", "--ontop-views"}, title = "Ontop view file",
            description = "User-supplied view file")
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    String ontopViewFile;

    @Option(type = OptionType.COMMAND, name = {"--force"}, title = "Force the conversion",
            description = "Force the conversion in the absence of DB metadata", arity = 0)
    @BashCompletion(behaviour = CompletionBehaviour.FILENAMES)
    @Nullable // optional
    private Boolean force;

    @Override
    public void run() {

        if (Strings.isNullOrEmpty(outputMappingFile)) {
            outputMappingFile = inputMappingFile.substring(0, inputMappingFile.length() - ".obda".length())
                    .concat(".ttl");
        }

        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder<?>> configBuilder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(inputMappingFile);

        if (!Strings.isNullOrEmpty(propertiesFile)) {
            configBuilder.propertyFile(propertiesFile);
        }
        else {
            configBuilder.jdbcDriver("dummy")
                    .jdbcUrl("dummy")
                    .jdbcUser("")
                    .jdbcPassword("");
        }

        if (!Strings.isNullOrEmpty(owlFile)) {
            configBuilder.ontologyFile(owlFile);
        }

        OntopSQLOWLAPIConfiguration config = configBuilder.build();

        try {
            SQLPPMapping ppMapping = extractAndNormalizePPMapping(config);

            R2RMLMappingSerializer converter = new R2RMLMappingSerializer(config.getRdfFactory());
            converter.write(new File(outputMappingFile), ppMapping);
            System.out.println("R2RML mapping file " + outputMappingFile + " written!");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private SQLPPMapping extractAndNormalizePPMapping(OntopSQLOWLAPIConfiguration config) throws Exception {
        SQLPPMapping ppMapping = config.loadProvidedPPMapping();

        if (!Strings.isNullOrEmpty(dbMetadataFile)) {
            return normalizeWithDBMetadataFile(ppMapping, config);
        }
        else if (!Strings.isNullOrEmpty(propertiesFile)) {
            return normalizeByConnectingToDB(ppMapping, config);
        }
        else if (force != null) {
            return ppMapping;
        }
        else {
            System.err.println("Access to DB metadata is required by default to respect column quoting rules of R2RML.\n" +
                    "Please provide a properties file containing the info to connect to the database.\n" +
                    "Specify the option --force to bypass this requirement.");
            System.exit(2);
            // Not reached
            return null;
        }
    }

    private SQLPPMapping normalizeWithDBMetadataFile(SQLPPMapping ppMapping, OntopSQLOWLAPIConfiguration config) throws IOException, MetadataExtractionException {
        try (Reader dbMetadataReader = new FileReader(dbMetadataFile)) {
            MetadataProvider dbMetadataProvider = config.getInjector().getInstance(SerializedMetadataProvider.Factory.class)
                    .getMetadataProvider(dbMetadataReader);

            return normalize(ppMapping, dbMetadataProvider, config);
        }
    }

    private SQLPPMapping normalizeByConnectingToDB(SQLPPMapping ppMapping, OntopSQLOWLAPIConfiguration config) throws MetadataExtractionException, SQLException, IOException {
        try (Connection connection = LocalJDBCConnectionUtils.createConnection(config.getSettings())) {
            JDBCMetadataProviderFactory metadataProviderFactory = config.getInjector().getInstance(JDBCMetadataProviderFactory.class);

            DBMetadataProvider dbMetadataProvider = metadataProviderFactory.getMetadataProvider(connection);

            return normalize(ppMapping, dbMetadataProvider, config);
        }
    }

    private SQLPPMapping normalize(SQLPPMapping ppMapping, MetadataProvider dbMetadataProvider, OntopSQLOWLAPIConfiguration config)
            throws IOException, MetadataExtractionException {
        Injector injector = config.getInjector();

        // DB metadata + view metadata
        final MetadataProvider metadataProvider;
        if (!Strings.isNullOrEmpty(ontopViewFile)) {
            try(Reader viewReader = new FileReader(ontopViewFile)) {
                metadataProvider = injector.getInstance(OntopViewMetadataProvider.Factory.class)
                        .getMetadataProvider(dbMetadataProvider, viewReader);
            }
        }
        else
            metadataProvider = dbMetadataProvider;

        CachingMetadataLookup metadataLookup = new CachingMetadataLookup(metadataProvider);
        OntopNativeMappingIdentifierNormalizer normalizer = new OntopNativeMappingIdentifierNormalizer(config, metadataLookup);

        SQLPPMappingFactory sqlppMappingFactory = injector.getInstance(SQLPPMappingFactory.class);
        return sqlppMappingFactory.createSQLPreProcessedMapping(
                ppMapping.getTripleMaps().stream()
                        .map(normalizer::normalize)
                        .collect(ImmutableCollectors.toList()),
                ppMapping.getPrefixManager());
    }

    private static class OntopNativeMappingIdentifierNormalizer {
        final SubstitutionFactory substitutionFactory;
        final TargetAtomFactory targetAtomFactory;
        final TermFactory termFactory;
        final SQLPPMappingConverterImpl converter;
        final MetadataLookup metadataLookup;
        final QuotedIDFactory idFactory, rawIdFactory;

        private OntopNativeMappingIdentifierNormalizer(OntopSQLOWLAPIConfiguration config, MetadataLookup metadataLookup) {
            substitutionFactory = config.getInjector().getInstance(SubstitutionFactory.class);
            targetAtomFactory = config.getInjector().getInstance(TargetAtomFactory.class);
            termFactory = config.getTermFactory();
            converter = (SQLPPMappingConverterImpl)config.getInjector().getInstance(SQLPPMappingConverter.class);
            this.metadataLookup = metadataLookup;
            this.idFactory = metadataLookup.getQuotedIDFactory();
            this.rawIdFactory = new RawQuotedIDFactory(idFactory);
        }

        private SQLPPTriplesMap normalize(SQLPPTriplesMap triplesMap) {
            try {
                RAExpression re = converter.getRAExpression(triplesMap, metadataLookup);
                ImmutableMap<QuotedID, ImmutableTerm> attributeMap = re.getUnqualifiedAttributes();
                Function<Variable, Optional<QuotedID>> lookup = var -> {
                    QuotedID standardId = idFactory.createAttributeID(var.getName());
                    if (attributeMap.containsKey(standardId))
                        return Optional.of(standardId);
                    QuotedID rawId = rawIdFactory.createAttributeID(var.getName());
                    if (attributeMap.containsKey(rawId))
                        return Optional.of(rawId);
                    return Optional.empty();
                };

                return new OntopNativeSQLPPTriplesMap(
                        triplesMap.getId(),
                        triplesMap.getSourceQuery(),
                        triplesMap.getTargetAtoms().stream()
                                .map(t -> normalize(t, lookup))
                                .collect(ImmutableCollectors.toList()));
            }
            catch (InvalidMappingSourceQueriesException | MetadataExtractionException e) {
               throw new RuntimeException(e);
            }
        }


        private TargetAtom normalize(TargetAtom target, Function<Variable, Optional<QuotedID>> lookup) {
            ImmutableMap<Variable, Optional<QuotedID>> targetPreMap = target.getProjectionAtom().getArguments().stream()
                    .map(v -> target.getSubstitution().applyToVariable(v))
                    .flatMap(ImmutableTerm::getVariableStream)
                    .distinct()
                    .collect(ImmutableCollectors.toMap(Function.identity(), lookup));

            if (targetPreMap.values().stream().anyMatch(t -> !t.isPresent()))
                throw new RuntimeException(targetPreMap.entrySet().stream()
                        .filter(e -> !e.getValue().isPresent())
                        .map(Map.Entry::getKey)
                        .map(Variable::getName)
                        .collect(Collectors.joining(", ",
                                "The placeholder(s) ",
                                " in the target do(es) not occur in source query of the mapping assertion\n[" + target + "]")));

            //noinspection OptionalGetWithoutIsPresent
            ImmutableMap<Variable, Variable> targetMap = targetPreMap.entrySet().stream()
                    .filter(e -> !e.getKey().getName().equals(e.getValue().get().getSQLRendering()))
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> termFactory.getVariable(e.getValue().get().getSQLRendering())));

            Var2VarSubstitution sub = substitutionFactory.getVar2VarSubstitution(targetMap);
            ImmutableSubstitution<ImmutableTerm> newSubstitution = target.getSubstitution().transform(sub::apply);
            return targetAtomFactory.getTargetAtom(target.getProjectionAtom(), newSubstitution);
        }

    }


}
