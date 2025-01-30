package brack.bernardo.simplify_tech_desafio.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiErrorResponse {
    private String timestamp;
    private String message;
    private String path;
    private int status;
}
