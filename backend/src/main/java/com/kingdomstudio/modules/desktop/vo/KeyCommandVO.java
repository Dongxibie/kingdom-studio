package com.kingdomstudio.modules.desktop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 单条按键命令：一条按下或一条松开。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "按键命令")
public class KeyCommandVO {

	@Schema(description = "命令序号，从 1 开始")
	private Integer seq;

	@Schema(description = "要按的键")
	private String key;

	@Schema(description = "PRESS 按下 / RELEASE 松开")
	private String action;

	@Schema(description = "相对于曲首的时间（毫秒）")
	private Integer atMs;

	@Schema(description = "按下后保持多久松开（毫秒）；松开命令为 null")
	private Integer holdMs;

	@Schema(description = "这条命令属于哪一次按键动作")
	private Integer strokeSeq;

	@Schema(description = "是否经过了顺延或截断等调整")
	private Boolean adjusted;
}
