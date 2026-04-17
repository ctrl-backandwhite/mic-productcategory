package com.backandwhite.api.dto.out;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutocompleteSuggestion {

    private String text;
    private String pid;
    private String imageUrl;
    private Float price;
}
