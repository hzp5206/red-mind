package com.redmind.modules.generate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redmind.modules.generate.entity.GenerationHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GenerationHistoryMapper extends BaseMapper<GenerationHistory> {
}
