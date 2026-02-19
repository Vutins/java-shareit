package ru.practicum.server.validation;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.dto.exception.InternalServerException;
import ru.practicum.dto.exception.NotFoundException;
import ru.practicum.dto.exception.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return new ErrorResponse("ERROR[409]: Email already exists", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException e) {
        return new ErrorResponse("ERROR[400]: Validation Failed", e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationException(final ValidationException e) {
        return new ErrorResponse("ERROR[400]: Произошла ошибка ValidationException", e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException e) {
        return new ErrorResponse("ERROR[400]: Произошла ошибка HttpMediaTypeNotSupportedException", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse httpMessageNotReadableException(final HttpMessageNotReadableException e) {
        return new ErrorResponse("ERROR[400]: Произошла ошибка HttpMessageNotReadableException", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        return new ErrorResponse("ERROR[400]: Произошла ошибка MethodArgumentTypeMismatchException", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFoundException(final NotFoundException e) {
        return new ErrorResponse("ERROR[404]: Произошла ошибка NotFoundException", e.getMessage());
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse internalServerException(final InternalServerException e) {
        return new ErrorResponse("ERROR[500]: Произошла ошибка InternalServerException", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
        return new ErrorResponse("ERROR[400]: Произошла ошибка IllegalArgumentException", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception e) {
        return new ErrorResponse("ERROR[500]: Internal Server Error", e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse throwableError(final Throwable e) {
        return new ErrorResponse("ERROR[500]: Произошла ошибка Throwable", e.getMessage());
    }
}