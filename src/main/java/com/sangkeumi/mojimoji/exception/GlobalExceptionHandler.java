// package com.sangkeumi.mojimoji.exception;

// import java.util.concurrent.ExecutionException;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.MethodArgumentNotValidException;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;

// import lombok.extern.slf4j.Slf4j;

// @ControllerAdvice
// @Slf4j
// public class GlobalExceptionHandler {

//     @ExceptionHandler(ExecutionException.class)
//     public ResponseEntity<String> handleExecutionException(ExecutionException ex) {
//         log.error("Execution error: {}", ex.getMessage());
//         return ResponseEntity.internalServerError().body("비동기 작업 중 오류 발생: " + ex.getCause().getMessage());
//     }

//     @ExceptionHandler(InterruptedException.class)
//     public ResponseEntity<String> handleInterruptedException(InterruptedException ex) {
//         log.error("Interrupted error: {}", ex.getMessage());
//         Thread.currentThread().interrupt(); // 인터럽트 상태 복원
//         return ResponseEntity.internalServerError().body("요청이 인터럽트되었습니다.");
//     }

//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
//         log.error("Validation error: {}", ex.getMessage());
//         return ResponseEntity.badRequest().body("Validation error: " + ex.getMessage());
//     }

//     @ExceptionHandler(NumberFormatException.class)
//     public ResponseEntity<String> handleNumberFormatException(NumberFormatException ex) {
//         log.error("Number format error: {}", ex.getMessage());
//         return ResponseEntity.badRequest().body("Number format error: " + ex.getMessage());
//     }

//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<String> handleAllExceptions(Exception ex) {
//         log.error("An error occurred: {}", ex.getMessage());
//         return ResponseEntity.internalServerError().body("An error occurred: " + ex.getMessage());
//     }
// }