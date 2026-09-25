package com.kingdomstudio.modules.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.music.entity.MusicNote;
import org.apache.ibatis.annotations.Mapper;

/**
 * 音符明细 Mapper。
 *
 * <p>与项目其余 Mapper 保持一致：逐个接口标注 @Mapper（项目没有配置 @MapperScan），
 * 常规增删改查直接用 BaseMapper，不写 XML。
 */
@Mapper
public interface MusicNoteMapper extends BaseMapper<MusicNote> {
}
