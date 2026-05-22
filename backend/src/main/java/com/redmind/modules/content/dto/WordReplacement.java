package com.redmind.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WordReplacement {

    private String oldWord;
    private String newWord;
}
