package it.unibz.inf.ontop.cli.utils;

import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.Context;
import com.github.rvesse.airline.model.GlobalMetadata;
import com.github.rvesse.airline.model.OptionMetadata;
import com.github.rvesse.airline.model.ParserMetadata;
import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.ParseState;
import com.github.rvesse.airline.parser.errors.ParseOptionUnexpectedException;
import com.github.rvesse.airline.parser.options.AbstractOptionParser;
import com.github.rvesse.airline.parser.options.OptionParser;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code Cli} wrapper adding support for assigning CLI options/arguments based on environment variables indicated by
 * {@link Env} annotations.
 * <p>
 * This class adds no public method and can be used similarly to a {@code Cli} instance. Call {@link #wrap(Cli)} on an
 * existing {@code Cli} object (e.g., obtained via its builder) to get an instance of this class.
 * </p>
 *
 * @param <C> the command type
 */
@SuppressWarnings("unused")
public class EnvCli<C> extends Cli<C> {

    /*
     * IMPLEMENTATION NOTE
     *
     * The strategy here adopted to inject environment variables relies on:
     * - appending a final command line argument INJECT_VARS_TOKEN to the argument list being parsed
     * - use a custom OptionParser that recognizes (and then discards) this special token and proceeds to inject
     *   environment variables into the options in scope for the current command
     *
     * This strategy has the benefit that environment variables are injected as soon as possible as part (actually, at
     * the very end) of iterating and processing one token at a time of CLI argument list. As a result, passing an
     * option/argument explicitly or via an environment variable is indistinguishable from the point of view of airline
     * and especially of its CLI validation logic. For instance, 'restriction' annotations like @Required,
     * @RequiredOnlyIf, @RequiredOnlyOne, and all the other will be seamlessly applied both to options supplied
     * explicitly or via environment variables.
     *
     * This strategy has the drawback of having to alter the argument list being parsed. As we use a custom OptionParser
     * to inject environment variables, we need at least a non-command argument for the custom OptionParser to be called
     * by airline. Otherwise, if the argument list is empty or just consists of a command name, airline will skip our
     * parser and proceed to validation, which will then not account for environment variables and possibly fail as a
     * consequence (e.g., if @Required is used for an option supplied via environment variable).
     *
     * An alternative strategy (also investigated but then discarded) is to post-process the command object produced by
     * airline. While avoiding the need for adding an extra INJECT_VARS_TOKEN to the argument list, this alternative
     * strategy implies:
     * - the need for explicitly converting and injecting the string values of environment variables into the command
     *   object fields (doable via duplication of airline logics)
     * - the need for explicitly evaluating option/argument restrictions on values supplied via environment variables,
     *   again via code duplication
     * - the impossibility of properly evaluating restrictions (e.g., @RequiredOnlyOne) that span multiple options or
     *   arguments
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvCli.class);

    private static final String INJECT_VARS_TOKEN = "\0";

    private EnvCli(GlobalMetadata<C> metadata) {
        super(metadata);
    }

    /**
     * {@inheritDoc}
     */
    public C parse(String... args) {
        String[] extArgs = Arrays.copyOf(args, args.length + 1);
        extArgs[args.length] = INJECT_VARS_TOKEN;
        return super.parse(extArgs);
    }

    /**
     * {@inheritDoc}
     */
    public ParseResult<C> parseWithResult(String... args) {
        String[] extArgs = Arrays.copyOf(args, args.length + 1);
        extArgs[args.length] = INJECT_VARS_TOKEN;
        return super.parseWithResult(extArgs);
    }

    /**
     * {@inheritDoc}
     */
    public ParseResult<C> parseWithResult(Iterable<String> args) {
        List<String> extArgs = Lists.newArrayList(args);
        extArgs.add(INJECT_VARS_TOKEN);
        return super.parseWithResult(extArgs);
    }

    /**
     * Wraps a regular {@code Cli} object, adding support for assigning options/arguments based on environment variables.
     *
     * @param cli the {@code Cli} object to wrap
     * @param <C> the command type
     * @return an {@code EnvCli} wrapper for the supplied argument (or the argument itself if already an {@code EnvCli}
     */
    public static <C> EnvCli<C> wrap(Cli<C> cli) {
        Objects.requireNonNull(cli);
        return cli instanceof EnvCli ? (EnvCli<C>) cli : new EnvCli<>(overrideOptionParsers(cli.getMetadata()));
    }

    private static <C> GlobalMetadata<C> overrideOptionParsers(GlobalMetadata<C> metadata) {

        ParserMetadata<C> parserConfig = metadata.getParserConfiguration();
        ParserMetadata<C> overriddenParserConfig = new ParserMetadata<>(
                parserConfig.getCommandFactory(),
                parserConfig.getCompositionAnnotations(),
                ImmutableList.of(new EnvOptionParser<>(parserConfig.getOptionParsers())),
                parserConfig.getTypeConverter(),
                parserConfig.getErrorHandler(),
                parserConfig.allowsAbbreviatedCommands(),
                parserConfig.allowsAbbreviatedOptions(),
                parserConfig.getAliases(),
                parserConfig.getUserAliasesSource(),
                parserConfig.aliasesOverrideBuiltIns(),
                parserConfig.aliasesMayChain(),
                parserConfig.getAliasForceBuiltInPrefix(),
                parserConfig.getArgumentsSeparator(),
                parserConfig.getFlagNegationPrefix());

        return new GlobalMetadata<>(
                metadata.getName(),
                metadata.getDescription(),
                metadata.getOptions(),
                metadata.getDefaultCommand(),
                metadata.getDefaultGroupCommands(),
                metadata.getCommandGroups(),
                metadata.getRestrictions(),
                metadata.getBaseHelpSections(),
                overriddenParserConfig);
    }

    private static final class EnvOptionParser<T> extends AbstractOptionParser<T> {

        private final List<OptionParser<T>> delegates;

        public EnvOptionParser(Iterable<? extends OptionParser<T>> delegates) {
            // Store the parsers to delegate to (at least one required)
            this.delegates = ImmutableList.copyOf(delegates);
            Preconditions.checkArgument(!this.delegates.isEmpty(),
                    "At least a delegate OptionParser is needed");
        }

        @Override
        public ParseState<T> parseOptions(PeekingIterator<String> tokens, ParseState<T> state, List<OptionMetadata> allowedOptions) {

            // If we reached the end of the token stream (i.e., we met the INJECT_VARS_TOKEN), look into environment
            // variables to set options not explicitly supplied from CLI
            if (tokens.hasNext() && INJECT_VARS_TOKEN.equals(tokens.peek())) {
                tokens.next(); // consume token
                return injectEnvVars(state, allowedOptions);
            }

            // Invoke delegate parsers until one reports to have processed the current token(s) or all of them were called
            ParseState<T> nextState = null;
            for (int i = 0; nextState == null && i < delegates.size(); ++i) {
                nextState = delegates.get(i).parseOptions(tokens, state, allowedOptions);
            }
            return nextState;
        }

        private ParseState<T> injectEnvVars(ParseState<T> state, List<OptionMetadata> allowedOptions) {

            // Iterate over all command options, trying to inject values coming from environment variables
            for (OptionMetadata option : allowedOptions) {

                // Obtain the environment variables associated to the option
                List<Env> annotations = option.getAccessors().stream()
                        .map(a -> a.getAnnotation(Env.class))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // Early abort in case there are no @Env annotations, and thus cannot possibly inject variable values
                if (annotations.isEmpty()) {
                    continue;
                }

                // Extract value/file/deprecated vars from the annotation(s), keeping their order (via ImmutableSet)
                Set<String> valueVars = annotations.stream()
                        .flatMap(a -> Arrays.stream(a.value()))
                        .collect(ImmutableSet.toImmutableSet());
                Set<String> fileVars = annotations.stream()
                        .flatMap(a -> Arrays.stream(a.file()))
                        .filter(var -> !valueVars.contains(var))
                        .collect(ImmutableSet.toImmutableSet());
                Set<String> deprecatedVars = annotations.stream()
                        .flatMap(a -> Arrays.stream(a.deprecated()))
                        .filter(var -> valueVars.contains(var) || fileVars.contains(var))
                        .collect(ImmutableSet.toImmutableSet());

                // Extract values of all variables, issuing warnings when deprecated variables are used
                Map<String, String> values = new LinkedHashMap<>();
                for (String var : Iterables.concat(valueVars, fileVars)) {
                    if (System.getenv().containsKey(var)) {
                        values.put(var, System.getenv(var));
                        if (deprecatedVars.contains(var) && LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Environment variable {} is deprecated. Please use {} instead",
                                    var, Joiner.on(", ").join(Sets.difference(Sets.union(valueVars, fileVars), deprecatedVars)));
                        }
                    }
                }

                // Skip the option if none of the associated environment variables (if any) is set
                if (values.isEmpty()) {
                    continue;
                }

                // Skip and report warning if the option was set explicitly from CLI
                String optionName = option.getOptions().iterator().next();
                if (state.getOptionValuesSeen(option) > 0) {
                    for (Map.Entry<String, String> entry : values.entrySet()) {
                        LOGGER.warn("Ignoring environment variable {}={} for explicitly specified option {}",
                                entry.getKey(), entry.getValue(), optionName);
                    }
                    continue;
                }

                // Choose the value to use preferring value variables over file ones and keeping their def. order
                Map.Entry<String, String> chosenEntry = values.entrySet().iterator().next();
                String chosenVar = chosenEntry.getKey();
                String chosenValue = chosenEntry.getValue();

                // Issue a warning if multiple variables were set, reporting which one will be used
                if (values.size() > 1 && LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Multiple environment variables {} set for option {}. Using {}={}",
                            Joiner.on(", ").withKeyValueSeparator('=').join(values),
                            optionName, chosenVar, chosenValue);
                }

                // For file variables, replace their value with the content of the file they denote
                if (fileVars.contains(chosenVar)) {
                    try {
                        chosenValue = Files.readString(Paths.get(chosenValue));
                    } catch (IOException ex) {
                        state.getParserConfiguration().getErrorHandler()
                                .handleError(new ParseOptionUnexpectedException(
                                        "Cannot obtain value of option %s from file %s pointed by %s: %s",
                                        option.getOptions().iterator().next(), chosenValue, chosenVar, ex.getMessage()));
                        return state;
                    }
                }

                // For boolean options, normalize the value so that it is either Boolean.TRUE or Boolean.FALSE
                if (option.getArity() == 0) {
                    chosenValue = Boolean.valueOf(!chosenValue.equalsIgnoreCase(Boolean.FALSE.toString())
                            && !chosenValue.equalsIgnoreCase("no")
                            && !chosenValue.equalsIgnoreCase("0")).toString();
                }

                // Inject the option value and log assignment
                state = state.pushContext(Context.OPTION)
                        .withOption(option)
                        .withOptionValue(option, chosenValue)
                        .popContext();
                LOGGER.debug("Set option {} via environment variable {}={}", optionName, chosenVar, chosenValue);
            }

            // Return the updated parser state that includes options set from environment variables
            return state;
        }

    }

}
