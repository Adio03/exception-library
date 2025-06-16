package org.semicolonlab.domain.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();

    private int status;
    private String code;
    private String title;
    private String detail;
    private String instance;
    private Map<String, Object> debugInformation;


    public ResponseEntity<ErrorResponse> toResponseEntity(HttpStatus status) {
        return new ResponseEntity<>(this, status);
    }

}
