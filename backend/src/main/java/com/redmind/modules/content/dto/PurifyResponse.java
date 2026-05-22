package com.redmind.modules.content.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PurifyResponse {

    private String cleanContent;
    private List<WordReplacement> replacedWords;
}
