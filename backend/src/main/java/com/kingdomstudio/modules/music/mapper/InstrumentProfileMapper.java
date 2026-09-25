package com.kingdomstudio.modules.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.music.entity.InstrumentProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 乐器按键档案 Mapper。
 *
 * <p>与项目其余 Mapper 保持一致：逐个接口标注 @Mapper（项目没有配置 @MapperScan），
 * 常规增删改查直接用 BaseMapper，不写 XML。
 */
@Mapper
public interface InstrumentProfileMapper extends BaseMapper<InstrumentProfile> {
}
