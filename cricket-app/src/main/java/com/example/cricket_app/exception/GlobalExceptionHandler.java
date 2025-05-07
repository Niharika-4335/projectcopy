package com.example.cricket_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateBetException.class)
    public ResponseEntity<ApiError> handleDuplicateBetException(DuplicateBetException duplicateBetException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), duplicateBetException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> handleInsufficientBalanceException(InsufficientBalanceException insufficientBalanceException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), insufficientBalanceException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MatchNotFoundException.class)
    public ResponseEntity<ApiError> handleMatchNotFoundException(MatchNotFoundException matchNotFoundException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), matchNotFoundException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MatchStartTimeInPastException.class)
    public ResponseEntity<ApiError> handleMatchStartTimeInPastException(MatchStartTimeInPastException matchStartTimeInPastException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), matchStartTimeInPastException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(NonPositiveAmountException.class)
    public ResponseEntity<ApiError> handleNonPositiveAmountException(NonPositiveAmountException nonPositiveAmountException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), nonPositiveAmountException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(SameTeamSelectionException.class)
    public ResponseEntity<ApiError> handleSameTeamSelectionException(SameTeamSelectionException sameTeamSelectionException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), sameTeamSelectionException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(UserNotFoundException userNotFoundException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), userNotFoundException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFoundException(WalletNotFoundException walletNotFoundException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), walletNotFoundException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(WinnerAlreadyDeclaredException.class)
    public ResponseEntity<ApiError> handleWinnerAlreadyDeclaredException(WinnerAlreadyDeclaredException winnerAlreadyDeclaredException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), winnerAlreadyDeclaredException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MatchNotStartedException.class)
    public ResponseEntity<ApiError> handleMatchNotStartedException(MatchNotStartedException matchNotStartedException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), matchNotStartedException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmailException(DuplicateEmailException duplicateEmailException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), duplicateEmailException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(MatchNotCompletedException.class)
    public ResponseEntity<ApiError> handleMatchNotCompletedException(MatchNotCompletedException matchNotCompletedException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), matchNotCompletedException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<ApiError> handleAdminNotFoundException(AdminNotFoundException adminNotFoundException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), adminNotFoundException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(OngoingMatchException.class)
    public ResponseEntity<ApiError> handleOngoingMatchException(OngoingMatchException ongoingMatchException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), ongoingMatchException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(InvalidTeamChosenException.class)
    public ResponseEntity<ApiError> handleInvalidTeamChosen(InvalidTeamChosenException invalidTeamChosenException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), invalidTeamChosenException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(PayoutNotFoundException.class)
    public ResponseEntity<ApiError> handlePayoutNotFound(PayoutNotFoundException payoutNotFoundException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(),payoutNotFoundException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(MatchWinnerNotDeclaredException.class)
    public ResponseEntity<ApiError> handleMatchWinnerNotDeclaredException(MatchWinnerNotDeclaredException matchWinnerNotDeclaredException) {

        ApiError apiError = new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), matchWinnerNotDeclaredException.getMessage(), null);

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleRunTimeException(Exception exception) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), null);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Iterate over all validation errors using a for-loop
        for (var error : ex.getBindingResult().getAllErrors()) {
            FieldError fieldError = (FieldError) error;
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errors.put(fieldName, message);
        }
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }


}
