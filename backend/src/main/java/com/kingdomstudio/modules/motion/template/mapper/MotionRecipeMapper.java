package com.kingdomstudio.modules.motion.template.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.motion.template.entity.MotionRecipe;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动效组合方案 Mapper。
 *
 * <p>与项目其余 Mapper 一致：逐个接口标注 @Mapper（项目没有配置 @MapperScan），常规增删改查直接用 BaseMapper。
 */
@Mapper
public interface MotionRecipeMapper extends BaseMapper<MotionRecipe> {
}
