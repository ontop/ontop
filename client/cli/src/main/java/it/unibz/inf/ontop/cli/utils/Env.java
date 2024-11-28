package it.unibz.inf.ontop.cli.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Annotation that indicates that an option/argument can be also supplied through environment variable(s).
 * <p>
 * Three sets of environment variables can be listed through this annotation:
 * <ul>
 * <li><i>value</i> variables that directly specify the argument/option value;</li>
 * <li><i>file</i> variables that indirectly specify the argument/option value by pointing to a file from which the
 * value can be read (useful, e.g., for passwords and other confidential data), this set being disjoint from the one
 * of value variables;</li>
 * <li><i>deprecated</i> variables, as a subset of the former two value/file variables, that denote variables still
 * supported for backward compatibility but whose use is discouraged and will lead to warnings being reported.</li>
 * </ul>
 * At least a <i>value</i> or a <i>file</i> variable has to be specified. Note that the values obtained through
 * environment variables will be used only if there is no explicit option/argument value supplied via the command line.
 * </p>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Env {

    /**
     * Specifies the name(s) of environment variables directly supplying an option/argument value. Variables listed here
     * must be all different and disjoint from those listed in {@link #file()}.
     *
     * @return environment variable(s) directly denoting the option/argument value
     */
    String[] value() default {};

    /**
     * Specifies the name(s) of the environment variables indirectly supplying an option/argument value by denoting a
     * file whose content is the value to use. Variables listed here must be all different and disjoint from those
     * listed in {@link #value()}.
     *
     * @return environment variables denoting a file with option/argument value
     */
    String[] file() default {};

    /**
     * Specifies the name(s) of deprecated environment variables among the ones listed in {@link #value()} and
     * {@link #file()}, whose use is supported for backward compatibility but leads to warnings being reported.
     * Variables listed here (if any) must be a subset of variables appearing in {@code value()} or {@code file()}.
     *
     * @return name(s) of deprecated environment variable(s)
     */
    String[] deprecated() default {};

}
