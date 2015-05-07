package org.semanticweb.ontop.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;

import io.airlift.airline.Option;
import io.airlift.airline.ParseArgumentsUnexpectedException;
import io.airlift.airline.ParseCommandMissingException;
import io.airlift.airline.ParseCommandUnrecognizedException;
import io.airlift.airline.ParseOptionMissingException;
import io.airlift.airline.help.Help;

import java.util.List;

public class Ontop {

    public static void main(String... args)
    {
        CliBuilder<OntopCommand> builder = Cli.<OntopCommand>builder("ontop")
                .withDescription("Ontop system for OBDA")
                //.withDefaultCommand(Help.class)
                .withCommands(OntopHelp.class, OntopMaterialize.class);

//        builder.withGroup("query")
//                .withDescription("Manage set of tracked repositories")
//                .withDefaultCommand(OntopQuery.class)
//                .withCommands(OntopQuery.class, RemoteMaterialize.class);

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

//
//
//    @Command(name = "query", description = "Gives some information about the query <name>")
//    public static class OntopQuery extends OntopCommand
//    {
//        @Option(name = "-n", description = "Do not query query heads")
//        public boolean noQuery;
//
//        @Arguments(description = "query")
//        public String query;
//    }
//
//    @Command(name = "materialize", description = "Materializes a query")
//    public static class RemoteMaterialize extends OntopCommand
//    {
//        @Option(name = "-t", description = "Track only a specific branch")
//        public String branch;
//
//        @Arguments(description = "Remote repository to materialize")
//        public List<String> query;
//    }
}
