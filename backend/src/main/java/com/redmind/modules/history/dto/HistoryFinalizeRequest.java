package com.redmind.modules.history.dto;

import com.redmind.modules.generate.dto.GeneratedVersion;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HistoryFinalizeRequest {

    @Valid
    @NotNull(message = "最终采用版本不能为空")
    private GeneratedVersion version;
}
