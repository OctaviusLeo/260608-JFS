package com.theblind.todo.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from the service layer.
     *
     * @param e the thrown {@link IllegalArgumentException}
     * @return ResponseEntity with HTTP status 400 (Bad Request) and a problem detail body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Invalid Request");
        return ResponseEntity.badRequest().body(pd);
    }
}
