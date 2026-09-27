package com.kingdomstudio.modules.timeline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.timeline.entity.TimelineEvent;
import org.apache.ibatis.annotations.Mapper;

/** 成长时间线 Mapper */
@Mapper
public interface TimelineEventMapper extends BaseMapper<TimelineEvent> {
}
