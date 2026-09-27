package com.kingdomstudio.modules.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 乐器匹配结果：一套「游戏乐器档案」对这首曲子合不合适。
 *
 * <p>它不是配置表的静态查询，而是把这套档案真的跑一遍映射之后的结论 ——
 * 所以带着「能全落下吗、漏几个音、能落几个」这些只有试算过才知道的事实。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "游戏乐器匹配结果")
public class ProfileMatchVO {

	private Long profileId;
	private String profileName;

	@Schema(description = "所属游戏，例如 光遇 Sky / Minecraft / 通用")
	private String game;

	private String instrument;
	private Integer keyCount;

	@Schema(description = "覆盖音域，例如 C4–C6")
	private String octaveRange;

	@Schema(description = "这个游戏乐器的特殊规则")
	private String specialRules;

	@Schema(description = "是否所有音都能落下")
	private Boolean coversAll;

	private Integer unmappedCount;
	private Integer mappedCount;

	@Schema(description = "匹配分：越高越合适")
	private Integer score;

	@Schema(description = "为什么推荐它，逐条可读")
	private List<String> reasons;
}
