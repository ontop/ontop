package it.unibz.inf.ontop.cli.utils;

import com.github.rvesse.airline.help.sections.HelpFormat;
import com.github.rvesse.airline.help.sections.HelpHint;
import com.github.rvesse.airline.restrictions.AbstractCommonRestriction;
import com.github.rvesse.airline.restrictions.ArgumentsRestriction;
import com.github.rvesse.airline.restrictions.OptionRestriction;
import com.github.rvesse.airline.restrictions.factories.ArgumentsRestrictionFactory;
import com.github.rvesse.airline.restrictions.factories.OptionRestrictionFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Option/argument restriction factory to integrate {@link Env} into {@code airline} command line parsing.
 * <p>
 * This class is registered via Java service provider mechanism (i.e., via files in {@code META-INF/services}) to
 * provide an implementation of interfaces {@link OptionRestrictionFactory} and {@link ArgumentsRestrictionFactory}.
 * In turn, this implementation generates {@link OptionRestriction} and {@link ArgumentsRestriction} instances for each
 * {@link Env} annotation used on option or argument-related fields of command objects. These restrictions serve only
 * a documentation purposes: by implementing {@link HelpHint}, they allow injecting help text specifying which (current
 * or deprecated) environment variables can be used to supply an option/argument, if not explicitly set on the command
 * line.
 * </p>
 * <p>
 * This class and its instance(s) are meant to be used by {@code airline} internal code and not directly by users of
 * this library that should be concerned only with {@link Env} and {@link EnvCli}. This class is declared public
 * (instead of package internal) only due to requirements of using the service provider mechanism.
 * </p>
 */
@SuppressWarnings("unused")
public class EnvRestrictionFactory implements OptionRestrictionFactory, ArgumentsRestrictionFactory {

    @Override
    public List<Class<? extends Annotation>> supportedOptionAnnotations() {
        return Collections.singletonList(Env.class);
    }

    @Override
    public OptionRestriction createOptionRestriction(Annotation annotation) {
        return doCreateRestriction(annotation);
    }

    public OptionRestriction createOptionRestriction(
            Iterable<String> valueVars, Iterable<String> fileVars, Iterable<String> deprecatedVars) {
        return doCreateRestriction(valueVars, fileVars, deprecatedVars);
    }

    @Override
    public List<Class<? extends Annotation>> supportedArgumentsAnnotations() {
        return Collections.singletonList(Env.class);
    }

    @Override
    public ArgumentsRestriction createArgumentsRestriction(Annotation annotation) {
        return doCreateRestriction(annotation);
    }

    public ArgumentsRestriction createArgumentsRestriction(
            Iterable<String> valueVars, Iterable<String> fileVars, Iterable<String> deprecatedVars) {
        return doCreateRestriction(valueVars, fileVars, deprecatedVars);
    }

    private EnvRestriction doCreateRestriction(Annotation annotation) {
        if (annotation instanceof Env) {
            Env a = (Env) annotation;
            return doCreateRestriction(Arrays.asList(a.value()), Arrays.asList(a.file()), Arrays.asList(a.deprecated()));
        }
        return null;
    }

    private EnvRestriction doCreateRestriction(
            Iterable<String> valueVars, Iterable<String> fileVars, Iterable<String> deprecatedVars) {

        // Deduplicate supplied variable lists and check they do not contain nulls or are themselves null
        Set<String> uniqueValueVars = ImmutableSet.copyOf(valueVars);
        Set<String> uniqueFileVars = ImmutableSet.copyOf(fileVars);
        Set<String> uniqueDeprecatedVars = ImmutableSet.copyOf(deprecatedVars);

        // Ensure at least a value or file variable was supplied
        if (uniqueValueVars.isEmpty() && uniqueFileVars.isEmpty()) {
            throw new IllegalArgumentException("At least a value or file variable must be specified");
        }

        // Treat duplicates as errors (help keeping annotation code clean)
        Preconditions.checkArgument(uniqueValueVars.size() == Iterables.size(valueVars),
                "Duplicate value variables in %s", valueVars);
        Preconditions.checkArgument(uniqueFileVars.size() == Iterables.size(fileVars),
                "Duplicate file variables in %s", fileVars);
        Preconditions.checkArgument(uniqueDeprecatedVars.size() == Iterables.size(deprecatedVars),
                "Duplicate deprecated variables in %s", deprecatedVars);

        // Treat variables marked both as value and file variables as errors (help keeping annotation code clean)
        Set<String> sharedVars = Sets.intersection(uniqueValueVars, uniqueFileVars);
        if (!sharedVars.isEmpty()) {
            throw new IllegalArgumentException(Joiner.on(", ").join(sharedVars)
                    + " listed both as value and file variables");
        }

        // Ensure that deprecated variables appear under value and file variables
        Set<String> undefVars = Sets.difference(uniqueDeprecatedVars, Sets.union(uniqueValueVars, uniqueFileVars));
        if (!undefVars.isEmpty()) {
            throw new IllegalArgumentException(Joiner.on(", ").join(undefVars)
                    + " listed as deprecated do not match any value/file variable");
        }

        // Create the restriction object
        return new EnvRestriction(uniqueValueVars, uniqueFileVars, uniqueDeprecatedVars);
    }

    private static final class EnvRestriction extends AbstractCommonRestriction implements HelpHint {

        private final String[] varList;

        public EnvRestriction(Set<String> valueVars, Set<String> fileVars, Set<String> deprecatedVars) {
            List<String> varList = Lists.newArrayList();
            for (String var : valueVars) {
                varList.add(var + (deprecatedVars.contains(var) ? " (deprecated)" : ""));
            }
            for (String var : fileVars) {
                varList.add(var + (deprecatedVars.contains(var) ?
                        " (denotes file holding option value, deprecated)" : " (denotes file holding option value)"));
            }
            this.varList = varList.toArray(String[]::new);
        }

        @Override
        public String getPreamble() {
            return varList.length > 1
                    ? "Equivalent environment variables:"
                    : null;
        }

        @Override
        public HelpFormat getFormat() {
            return this.varList.length > 1 ? HelpFormat.LIST : HelpFormat.PROSE;
        }

        @Override
        public int numContentBlocks() {
            return 1;
        }

        @Override
        public String[] getContentBlock(int blockNumber) {
            return (varList.length > 1) ? varList : new String[]{
                    String.format("Equivalent environment variable: %s", this.varList[0])};
        }

    }

}
