package com.kingdomstudio.modules.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kingdomstudio.common.PageVO;
import com.kingdomstudio.common.ResultCode;
import com.kingdomstudio.common.exception.BusinessException;
import com.kingdomstudio.modules.knowledge.dto.CodeSnippetQueryDTO;
import com.kingdomstudio.modules.knowledge.dto.CodeSnippetSaveDTO;
import com.kingdomstudio.modules.knowledge.entity.CodeSnippet;
import com.kingdomstudio.modules.knowledge.mapper.CodeSnippetMapper;
import com.kingdomstudio.modules.knowledge.vo.CodeSnippetDetailVO;
import com.kingdomstudio.modules.knowledge.vo.CodeSnippetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码知识库：解决方案的沉淀与检索。
 *
 * <p>列表不返回代码正文：一页 20 条片段，正文加起来可能是几十 KB，
 * 前端列表也不需要它 —— 点开某一条时才取详情。
 */
@Service
@RequiredArgsConstructor
public class CodeSnippetService {

	private final CodeSnippetMapper codeSnippetMapper;

	public PageVO<CodeSnippetVO> page(CodeSnippetQueryDTO query) {
		Page<CodeSnippet> page = new Page<>(query.getPage(), query.getSize());
		String keyword = trim(query.getKeyword());
		String language = trim(query.getLanguage());
		LambdaQueryWrapper<CodeSnippet> wrapper = new LambdaQueryWrapper<CodeSnippet>()
				.eq(StringUtils.hasText(language), CodeSnippet::getLanguage, language)
				.and(StringUtils.hasText(keyword), w -> w
						.like(CodeSnippet::getTitle, keyword)
						.or().like(CodeSnippet::getDescription, keyword)
						.or().like(CodeSnippet::getTags, keyword)
						.or().like(CodeSnippet::getLanguage, keyword))
				.orderByDesc(CodeSnippet::getId);
		return PageVO.of(codeSnippetMapper.selectPage(page, wrapper), this::toListItem);
	}

	public CodeSnippetDetailVO detail(Long id) {
		return toDetail(require(id));
	}

	public Long create(CodeSnippetSaveDTO request) {
		checkTitle(request.getTitle(), null);
		CodeSnippet entity = new CodeSnippet();
		apply(entity, request);
		codeSnippetMapper.insert(entity);
		return entity.getId();
	}

	public void update(Long id, CodeSnippetSaveDTO request) {
		CodeSnippet entity = require(id);
		checkTitle(request.getTitle(), id);
		apply(entity, request);
		codeSnippetMapper.updateById(entity);
	}

	public void delete(Long id) {
		require(id);
		codeSnippetMapper.deleteById(id);
	}

	public CodeSnippet require(Long id) {
		CodeSnippet entity = codeSnippetMapper.selectById(id);
		if (entity == null) {
			throw new BusinessException(ResultCode.NOT_FOUND, "代码片段不存在：" + id);
		}
		return entity;
	}

	public void checkTitle(String title, Long selfId) {
		LambdaQueryWrapper<CodeSnippet> wrapper = new LambdaQueryWrapper<CodeSnippet>()
				.eq(CodeSnippet::getTitle, trim(title));
		if (selfId != null) {
			wrapper.ne(CodeSnippet::getId, selfId);
		}
		if (codeSnippetMapper.selectCount(wrapper) > 0) {
			throw new BusinessException(ResultCode.BAD_REQUEST, "已存在同名片段：" + trim(title));
		}
	}

	/** 代码行数：空串算 0 行，避免列表上显示「1 行」的空片段 */
	public static int lineCount(String codeContent) {
		if (!StringUtils.hasText(codeContent)) {
			return 0;
		}
		return codeContent.split("\r?\n", -1).length;
	}

	public static List<String> splitTags(String tags) {
		if (!StringUtils.hasText(tags)) {
			return List.of();
		}
		List<String> items = new ArrayList<>();
		for (String part : tags.split(",")) {
			String item = part.trim();
			if (!item.isEmpty()) {
				items.add(item);
			}
		}
		return items;
	}

	private void apply(CodeSnippet entity, CodeSnippetSaveDTO request) {
		entity.setTitle(trim(request.getTitle()));
		entity.setLanguage(trim(request.getLanguage()));
		entity.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
		entity.setCodeContent(request.getCodeContent());
		entity.setTags(request.getTags() == null ? "" : request.getTags().trim());
	}

	private CodeSnippetVO toListItem(CodeSnippet entity) {
		return CodeSnippetVO.builder()
				.id(entity.getId())
				.title(entity.getTitle())
				.language(entity.getLanguage())
				.description(entity.getDescription())
				.tags(splitTags(entity.getTags()))
				.lineCount(lineCount(entity.getCodeContent()))
				.createTime(entity.getCreateTime())
				.updateTime(entity.getUpdateTime())
				.build();
	}

	private CodeSnippetDetailVO toDetail(CodeSnippet entity) {
		return CodeSnippetDetailVO.builder()
				.id(entity.getId())
				.title(entity.getTitle())
				.language(entity.getLanguage())
				.description(entity.getDescription())
				.tags(splitTags(entity.getTags()))
				.codeContent(entity.getCodeContent())
				.lineCount(lineCount(entity.getCodeContent()))
				.createTime(entity.getCreateTime())
				.updateTime(entity.getUpdateTime())
				.build();
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}
}
