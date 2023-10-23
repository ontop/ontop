package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.function.Function;

public class BnodeStringTemplateFunctionSymbolWithSalt extends ObjectStringTemplateFunctionSymbolImpl
        implements BnodeStringTemplateFunctionSymbol {

    private final UUID salt;

    protected BnodeStringTemplateFunctionSymbolWithSalt(ImmutableList<Template.Component> template, UUID salt,
                                                        TypeFactory typeFactory) {
        super(template, "-salted", typeFactory);
        this.salt = salt;
    }

    @Override
    protected ImmutableTerm simplifyWithAllParametersConstant(ImmutableList<DBConstant> newTerms,
                                                              TermFactory termFactory,
                                                              VariableNullability variableNullability) {
        String originalLabel = buildString(newTerms, termFactory, variableNullability);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.toString().getBytes());
            md.update(originalLabel.getBytes());
            String externalLabel = Hex.encodeHexString(md.digest());

            return termFactory.getDBStringConstant(externalLabel.toLowerCase());

        } catch (NoSuchAlgorithmException e) {
            throw new MinorOntopInternalBugException(e.getMessage());
        }
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        DBConstant currentTermPlaceholder = termFactory.getDBStringConstant(UUID.randomUUID().toString());
        String labelSQLExpression = super.getNativeDBString(terms, termConverter, termFactory);

        Function<ImmutableTerm, String> newConverter = t -> t == currentTermPlaceholder
                ? labelSQLExpression
                : termConverter.apply(t);

        ImmutableFunctionalTerm newTerm = termFactory.getDBSha256(termFactory.getNullRejectingDBConcatFunctionalTerm(
                ImmutableList.of(
                        termFactory.getDBStringConstant(salt.toString()),
                        currentTermPlaceholder)));

        return newConverter.apply(newTerm);
    }
}
