package it.unibz.inf.ontop.cli;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.parser.errors.*;


public class Ontop {

    public static void main(String... args) {

        Cli<OntopCommand> ontopCommandCLI = getOntopCommandCLI();

        OntopCommand command;

        try {
            command = ontopCommandCLI.parse(args);
            command.run();
        } catch (ParseCommandMissingException e) {
            main("help");
        } catch (ParseArgumentsUnexpectedException | ParseOptionMissingException e) {
            System.err.println("Error: " + e.getMessage());
            String commandName = args[0];
            System.err.format("Run `ontop help %s` to see the help for the command `%s`\n", commandName, commandName);
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Run `ontop help` to see the help");
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError();
        }
    }

    static Cli<OntopCommand> getOntopCommandCLI() {
        //noinspection unchecked
        CliBuilder<OntopCommand> builder = Cli.<OntopCommand>builder("ontop")
                .withDescription("Ontop system for Ontology based Data Access")
                .withCommands(
                        /**
                         * visible commands
                         */
                        OntopVersion.class,
                        OntopHelp.class,
                        OntopQuery.class,
                        OntopMaterialize.class,
                        OntopBootstrap.class,
                        OntopValidate.class,
                        OntopEndpoint.class,
                        OntopExtractDBMetadata.class,
                        /**
                         * hidden commands
                         */
                        OntopCompile.class
                );


        builder.withGroup("mapping")
                .withDescription("Manipulate mapping files")
                .withCommand(OntopOBDAToR2RML.class)
                .withCommand(OntopR2RMLToOBDA.class)
                .withCommand(OntopR2RMLPrettify.class)
                .withCommand(OntopMappingV1ToV3.class);

        return builder.build();
    }

}
