package com.kingdomstudio.modules.technology.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.technology.entity.Technology;
import org.apache.ibatis.annotations.Mapper;

/** 技术表 Mapper */
@Mapper
public interface TechnologyMapper extends BaseMapper<Technology> {
}
