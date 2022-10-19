package conveyor.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ResponseStatus(value = UNPROCESSABLE_ENTITY)
public class RefusalOfLoanException extends Exception {

    public RefusalOfLoanException(String message) {
        super(message);
    }
}