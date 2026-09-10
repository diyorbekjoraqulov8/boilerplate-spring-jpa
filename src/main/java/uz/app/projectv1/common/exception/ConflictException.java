package uz.app.projectv1.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException{
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }
}

//BadRequestException, ForbiddenException ham yozish kerak