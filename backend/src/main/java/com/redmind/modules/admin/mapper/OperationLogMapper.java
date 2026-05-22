package com.redmind.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redmind.modules.admin.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
