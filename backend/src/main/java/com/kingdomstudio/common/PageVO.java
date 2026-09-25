package com.kingdomstudio.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

/**
 * 分页返回外壳。
 *
 * <p>与 {@link Result} 一样属于跨模块公用结构，所以放在 common 下；
 * 各模块的分页接口统一返回它，前端只认这一种分页结构。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageVO<T> {

	@Schema(description = "当前页数据")
	private List<T> records;

	@Schema(description = "总条数", example = "42")
	private Long total;

	@Schema(description = "当前页码", example = "1")
	private Long page;

	@Schema(description = "每页条数", example = "20")
	private Long size;

	@Schema(description = "总页数", example = "3")
	private Long pages;

	/** 把 MyBatis-Plus 的分页对象转换成前端用的 VO，顺带做实体到 VO 的映射 */
	public static <E, T> PageVO<T> of(IPage<E> source, Function<E, T> mapper) {
		return PageVO.<T>builder()
				.records(source.getRecords().stream().map(mapper).toList())
				.total(source.getTotal())
				.page(source.getCurrent())
				.size(source.getSize())
				.pages(source.getPages())
				.build();
	}
}
