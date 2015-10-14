package org.semanticweb.ontop.cli;


import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.builder.CliBuilder;
import com.github.rvesse.airline.parser.ParseArgumentsUnexpectedException;
import com.github.rvesse.airline.parser.ParseCommandMissingException;
import com.github.rvesse.airline.parser.ParseCommandUnrecognizedException;
import com.github.rvesse.airline.parser.ParseOptionMissingException;

public class Ontop {

    public static void main(String... args)
    {
        CliBuilder<OntopCommand> builder = Cli.<OntopCommand>builder("ontop")
                .withDescription("Ontop system for Ontology based Data Access")
                .withCommands(
                        /**
                         * visible commands
                         */
                        OntopHelp.class,
                        OntopQuery.class,
                        OntopMaterialize.class,
                        OntopBootstrap.class,
                        /**
                         * hidden commands
                         */
                        OntopCompile.class
                        // OntopServer.class
                );


        builder.withGroup("mapping")
                .withDescription("Manipulate mapping files")
                .withCommand(OntopOBDAToR2RML.class)
                .withCommand(OntopR2RMLToOBDA.class)
                .withCommand(OntopR2RMLPrettify.class);

        Cli<OntopCommand> ontopParser = builder.build();

        OntopCommand command;

        try {
            command = ontopParser.parse(args);
            command.run();
        } catch (ParseCommandMissingException e){
            main("help");
        } catch (ParseCommandUnrecognizedException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Run `ontop help` to see the help");
        } catch (ParseArgumentsUnexpectedException | ParseOptionMissingException e ){
            System.err.println("Error: " + e.getMessage());
            String commandName = args[0];
            System.err.format("Run `ontop help %s` to see the help for the command `%s`\n", commandName, commandName);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
