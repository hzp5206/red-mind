package com.redmind.modules.history.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HistoryPageResponse {

    private Long total;
    private List<HistoryDetailResponse> records;
}
