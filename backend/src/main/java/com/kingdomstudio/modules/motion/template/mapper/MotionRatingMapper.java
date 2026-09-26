package com.kingdomstudio.modules.motion.template.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.motion.template.entity.MotionRating;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评分记录 Mapper。
 *
 * <p>与项目其余 Mapper 一致：逐个接口标注 @Mapper（项目没有配置 @MapperScan），常规增删改查直接用 BaseMapper。
 */
@Mapper
public interface MotionRatingMapper extends BaseMapper<MotionRating> {
}
