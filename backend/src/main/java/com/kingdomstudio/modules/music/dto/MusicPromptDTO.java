package com.kingdomstudio.modules.music.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 音乐助手的自然语言输入 */
@Data
@Schema(description = "音乐助手对话输入")
public class MusicPromptDTO {

	@NotBlank(message = "先说说你想要什么样的演奏")
	@Schema(description = "使用者原话，例如「帮我把这首歌变成适合 15 键口风琴演奏」",
			example = "让它听起来简单一点")
	private String prompt;
}
