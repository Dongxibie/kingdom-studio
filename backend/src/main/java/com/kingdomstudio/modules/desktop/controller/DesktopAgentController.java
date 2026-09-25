package com.kingdomstudio.modules.desktop.controller;

import com.kingdomstudio.common.Result;
import com.kingdomstudio.modules.desktop.dto.DispatchRequestDTO;
import com.kingdomstudio.modules.desktop.service.DesktopAgentService;
import com.kingdomstudio.modules.desktop.vo.AgentStatusVO;
import com.kingdomstudio.modules.desktop.vo.DispatchPlanVO;
import com.kingdomstudio.modules.desktop.vo.ProtocolVO;
import com.kingdomstudio.modules.music.dto.MappingRequestDTO;
import com.kingdomstudio.modules.music.service.MusicTaskService;
import com.kingdomstudio.modules.music.vo.KeySequenceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 桌面代理接口（Phase 6，模拟）。
 *
 * <p>两条边界写在最前面：
 * <ol>
 *   <li><b>不产生真实系统输入</b>：这个 Controller 返回的是「会按什么」的计划，不是执行结果；</li>
 *   <li><b>按键由服务端算</b>：请求里只接受 taskId + profileId，不接受前端直接提交按键列表，
 *       避免绕过映射规则构造任意输入。</li>
 * </ol>
 */
@Tag(name = "12. 桌面代理", description = "WebSocket 协议与派发计划（模拟）：不驱动真实系统输入")
@RestController
@RequestMapping("/desktop")
@RequiredArgsConstructor
public class DesktopAgentController {

	private final DesktopAgentService desktopAgentService;
	private final MusicTaskService musicTaskService;

	@Operation(summary = "代理状态", description = "本机是否接入真实代理；本期固定为模拟模式")
	@GetMapping("/ping")
	public Result<AgentStatusVO> ping() {
		return Result.success(desktopAgentService.status());
	}

	@Operation(summary = "WebSocket 协议", description = "消息信封、消息类型清单与安全约束")
	@GetMapping("/protocol")
	public Result<ProtocolVO> protocol() {
		return Result.success(desktopAgentService.protocol());
	}

	@Operation(summary = "生成派发计划（模拟）",
			description = "按键序列由服务端按乐器档案重新计算；返回按下/松开成对的命令流，并在 warnings 里说明被顺延或截断的地方")
	@PostMapping("/dispatch")
	public Result<DispatchPlanVO> dispatch(@Valid @RequestBody DispatchRequestDTO request) {
		MappingRequestDTO mappingRequest = new MappingRequestDTO();
		mappingRequest.setProfileId(request.getProfileId());
		mappingRequest.setStrategy(request.getStrategy());
		KeySequenceVO sequence = musicTaskService.mapKeys(request.getTaskId(), mappingRequest);
		if (sequence.getUnmappedCount() > 0) {
			// 不是错误，但要让人看见：有音落不下键，派发出去的演奏会缺音
			sequence.setExportText(sequence.getExportText()
					+ "\n# 注意：有 " + sequence.getUnmappedCount() + " 个音未能落键，本次派发的演奏会缺这些音。\n");
		}
		return Result.success(desktopAgentService.plan(sequence, request.getClientId()));
	}
}
