package it.unibz.inf.ontop.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No such Order")  // 404

public class InvalidQueryException extends RuntimeException
{
}
