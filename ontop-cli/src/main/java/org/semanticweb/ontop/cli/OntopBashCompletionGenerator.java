package org.semanticweb.ontop.cli;


import com.github.rvesse.airline.help.cli.bash.BashCompletionGenerator;

public class OntopBashCompletionGenerator {
    public static void main(String[] args) {
        BashCompletionGenerator<Object> generator = new BashCompletionGenerator<>();
        generator.usage(metadata, output);
    }
}
