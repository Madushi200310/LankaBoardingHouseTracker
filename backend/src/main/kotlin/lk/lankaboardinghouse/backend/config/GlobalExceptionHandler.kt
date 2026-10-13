package lk.lankaboardinghouse.backend.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {

    // Existing behaviour: bad request from IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("message" to (e.message ?: "Bad request")))

    // Bean validation errors on @Valid request bodies
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.badRequest().body(
            mapOf(
                "message" to "Validation failed",
                "errors" to errors
            )
        )
    }

    // Ownership violations -> 403
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(mapOf("message" to (e.message ?: "Forbidden")))

    // Upload too large -> 400
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUpload(e: MaxUploadSizeExceededException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest()
            .body(mapOf("message" to "Uploaded file is too large"))
}