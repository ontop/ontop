package it.unibz.inf.ontop.endpoint.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-local context to track whether the current request has an Authorization header.
 * This is used to coordinate JDBC URL modification between the SPARQL query processor
 * and the JDBC connection creation layer.
 */
public class AuthorizationContext {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationContext.class);
    
    private static final ThreadLocal<String> AUTHORIZATION_TOKEN = new ThreadLocal<>();
    
    /**
     * Sets the Authorization header value for the current request.
     */
    public static void setAuthorizationHeader(String authHeader) {
        logger.info("Setting authorization header: {}", authHeader != null ? "[TOKEN]" : "null");
        AUTHORIZATION_TOKEN.set(authHeader);
    }
    
    /**
     * Returns whether the current request has an Authorization header.
     */
    public static boolean hasAuthorizationHeader() {
        String value = AUTHORIZATION_TOKEN.get();
        boolean result = value != null && !value.trim().isEmpty();
        logger.debug("Has authorization header: {}", result);
        return result;
    }
    
    /**
     * Returns the Authorization header value, extracting the token if it's a Bearer token.
     */
    public static String getAuthorizationToken() {
        String value = AUTHORIZATION_TOKEN.get();
        if (value != null && value.trim().toLowerCase().startsWith("bearer ")) {
            return value.trim().substring(7); // Remove "Bearer " prefix
        }
        return value;
    }
    
    /**
     * Clears the thread-local context.
     */
    public static void clear() {
        AUTHORIZATION_TOKEN.remove();
    }
}