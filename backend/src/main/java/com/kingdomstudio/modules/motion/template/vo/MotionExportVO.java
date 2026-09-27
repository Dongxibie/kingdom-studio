package com.kingdomstudio.modules.motion.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码导出结果：一套方案的完整代码产物。
 *
 * <p>和「单模板代码面板」的区别：单模板给的是**一个动效**的代码片段，
 * 这里给的是**一整套方案的工程文件**（组件 + 样式 + 配置），拷进项目就能跑。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "动效方案代码导出结果")
public class MotionExportVO {

	@Schema(description = "方案名")
	private String planName;

	private String recipeKey;

	@Schema(description = "导出格式：VUE / REACT / HTML")
	private String format;

	@Schema(description = "格式的中文名")
	private String formatLabel;

	@Schema(description = "方案里有几步")
	private Integer stepCount;

	@Schema(description = "文件清单：路径 / 语言 / 内容")
	private List<FileVO> files;

	@Schema(description = "组合预览：这张页面就是导出结果的样子，前端直接塞进沙箱 iframe")
	private String previewHtml;

	@Schema(description = "导出说明：做了哪些处理、有哪些要注意的")
	private List<String> notes;

	/** 一个产物文件 */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "导出的一个文件")
	public static class FileVO {

		@Schema(description = "建议的路径，例如 src/components/MotionPlan.vue")
		private String path;

		@Schema(description = "语言：vue / tsx / ts / css / html")
		private String language;

		@Schema(description = "这个文件是干嘛的")
		private String role;

		private String content;

		@Schema(description = "字节数（按 UTF-8 算）")
		private Integer bytes;
	}
}
