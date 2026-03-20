package com.backandwhite.domain.model;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResponse {

    private String code;
    private String message;
    private List<String> details;
    private ZonedDateTime dateTime;
}
