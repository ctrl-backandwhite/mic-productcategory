package com.backandwhite.api.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkStatusUpdateDtoIn {

    @NotNull
    @Size(min = 1)
    private List<String> ids;

    @NotBlank
    private String status;
}
