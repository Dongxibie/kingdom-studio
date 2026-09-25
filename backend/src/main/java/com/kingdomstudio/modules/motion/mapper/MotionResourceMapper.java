package com.kingdomstudio.modules.motion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.motion.entity.MotionResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动效资源 Mapper。
 *
 * <p>项目没有配置 @MapperScan，所以按接口逐个标注 {@code @Mapper}（与 package-info 里的约定一致）。
 * 常规增删改查直接用 BaseMapper，无需写 XML。
 */
@Mapper
public interface MotionResourceMapper extends BaseMapper<MotionResource> {
}
