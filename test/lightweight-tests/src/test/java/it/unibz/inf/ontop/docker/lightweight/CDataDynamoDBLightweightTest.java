package it.unibz.inf.ontop.docker.lightweight;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Target({ TYPE, METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("cdatadynamodblighttests")
@Test
public @interface CDataDynamoDBLightweightTest {
}
