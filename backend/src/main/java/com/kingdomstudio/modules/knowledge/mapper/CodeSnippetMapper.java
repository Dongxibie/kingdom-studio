package com.kingdomstudio.modules.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kingdomstudio.modules.knowledge.entity.CodeSnippet;
import org.apache.ibatis.annotations.Mapper;

/** 代码片段 Mapper */
@Mapper
public interface CodeSnippetMapper extends BaseMapper<CodeSnippet> {
}
