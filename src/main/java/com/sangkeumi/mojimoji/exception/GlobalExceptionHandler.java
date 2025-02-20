package com.sangkeumi.mojimoji.exception;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 비동기 실행 중 발생하는 예외 처리 */
    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> handleExecutionException(ExecutionException ex) {
        log.error("Execution error occurred", ex);
        return ResponseEntity.internalServerError().body("An error occurred during asynchronous execution: "
                + (ex.getCause() != null ? ex.getCause().getMessage() : "Unknown error"));
    }

    /** 인터럽트 예외 처리 */
    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException(InterruptedException ex) {
        log.error("Interrupted error occurred", ex);
        Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        return ResponseEntity.internalServerError().body("The request was interrupted.");
    }

    /** 요청 값 검증 실패 예외 처리 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error occurred", ex);
        return ResponseEntity.badRequest().body("Validation error: " + ex.getBindingResult().toString());
    }

    /** 숫자 형식 변환 오류 처리 */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<String> handleNumberFormatException(NumberFormatException ex) {
        log.error("Number format error occurred", ex);
        return ResponseEntity.badRequest().body("Invalid number format: " + ex.getMessage());
    }

    /** Null 참조 오류 처리 */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException occurred", ex);
        return ResponseEntity.internalServerError().body("A null value was referenced.");
    }

    /** 잘못된 인자 전달 오류 처리 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException occurred", ex);
        return ResponseEntity.badRequest().body("Invalid argument: " + ex.getMessage());
    }

    /** 잘못된 상태에서의 요청 처리 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        log.error("IllegalStateException occurred", ex);
        return ResponseEntity.internalServerError().body("The request was executed in an invalid state.");
    }

    /** 입출력 예외 처리 */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        log.error("IOException occurred", ex);
        return ResponseEntity.internalServerError().body("An I/O error occurred during processing.");
    }

    /** 기타 모든 예외 처리 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return ResponseEntity.internalServerError().body("An unexpected error occurred: " + ex.getMessage());
    }
}
