package it.unibz.inf.ontop.spec.mapping.parser.impl.listener;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.LoggerFactory;

public class ThrowingErrorListener extends BaseErrorListener {

    public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ThrowingErrorListener.class);

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {

        log.debug("Syntax error location: column " + charPositionInLine + "\n" + msg);
        throw new ParseCancellationException("Syntax error location: column " + charPositionInLine + "\n" + msg);

    }
}
