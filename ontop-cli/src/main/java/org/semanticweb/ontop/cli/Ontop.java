package org.semanticweb.ontop.cli;

import io.airlift.airline.Arguments;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;

import java.util.List;

public class Ontop {

    public static void main(String[] args)
    {
        CliBuilder<Runnable> builder = Cli.<Runnable>builder("ontop")
                .withDescription("the stupid content tracker")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class, OntopMaterialize.class);

//        builder.withGroup("query")
//                .withDescription("Manage set of tracked repositories")
//                .withDefaultCommand(OntopQuery.class)
//                .withCommands(OntopQuery.class, RemoteMaterialize.class);

        Cli<Runnable> ontopParser = builder.build();

        ontopParser.parse(args).run();
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
