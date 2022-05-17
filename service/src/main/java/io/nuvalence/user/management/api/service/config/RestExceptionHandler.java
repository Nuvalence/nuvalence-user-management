package io.nuvalence.user.management.api.service.config;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.InternalServerException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Custom REST Exception handler.
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({HttpServerErrorException.class, InternalServerException.class})
    protected ResponseEntity<Object> handleServiceException(Exception ex) {
        log.error("There was an error processing the request:", ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException illArgEx) {
        log.error("There was an error processing the request:", illArgEx);
        return new ResponseEntity<>(new ApiError(illArgEx.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalStateException.class})
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException illegalStateException) {
        log.error("There was an error processing the request: {}", illegalStateException.getMessage());
        return new ResponseEntity<>(new ApiError(illegalStateException.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleNotFoundException(Exception ex) {
        log.error("There was an error processing the request:", ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessLogicException.class)
    protected ResponseEntity<Object> handleBusinessLogicException(Exception ex) {
        log.error("There was an error processing the request:", ex);
        return new ResponseEntity<>(new ApiError(ex.getMessage()), HttpStatus.CONFLICT);
    }


}
