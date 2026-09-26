package com.kingdomstudio.modules.motion.template.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.motion.template.entity.MotionTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 官方动效模板 Mapper。
 *
 * <p>与项目其余 Mapper 一致：逐个接口标注 @Mapper（项目没有配置 @MapperScan），常规增删改查直接用 BaseMapper。
 */
@Mapper
public interface MotionTemplateMapper extends BaseMapper<MotionTemplate> {
}
