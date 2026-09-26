<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import type { MotionTemplateDetail } from '@/extensions/motion-lab/types/workbench'

interface Props {
	detail: MotionTemplateDetail | null
	/** 参数值：key 是 CSS 变量名 */
	paramValues: Record<string, number>
	playing: boolean
	speed: number
	scale: number
	/** 每次重建都需要变化的键，用于「重播」 */
	restartToken: number
}

const props = defineProps<Props>()

const frame = ref<HTMLIFrameElement | null>(null)

/**
 * 预览 HTML。
 *
 * 三条纪律（与既有的资源预览一致）：
 *  1. 走 sandbox iframe，且**不给 allow-same-origin**：iframe 处于不透明源，拿不到宿主页面的 DOM / Cookie；
 *  2. 不在宿主页面执行模板里的脚本，脚本只在沙箱内跑；
 *  3. 参数以 CSS 变量注入 :root，与导出的代码用的是同一批变量。
 *
 * 播放控制不走重建：iframe 内的监听器接收 postMessage，用 Web Animations API 调整
 * playbackRate 与 playState —— 这样「暂停/变速」是连续的，不会把动画从头再放一遍。
 */
const html = computed(() => {
	const detail = props.detail
	if (!detail) {
		return ''
	}
	const overrides = Object.entries(props.paramValues)
		.map(([key, value]) => `${key}: ${value};`)
		.join(' ')
	const style = [detail.cssCode, overrides ? `:root { ${overrides} }` : ''].filter(Boolean).join('\n')
	const bridge = `
    <script>
      (function () {
        function all() { return document.getAnimations ? document.getAnimations() : []; }
        function apply(state) {
          all().forEach(function (animation) {
            if (typeof state.speed === 'number') { animation.playbackRate = state.speed; }
            if (state.playing === false) { animation.pause(); }
            if (state.playing === true) { animation.play(); }
          });
        }
        window.addEventListener('message', function (event) {
          var data = event.data || {};
          if (data.type === 'mlab-control') { apply(data); }
          if (data.type === 'mlab-restart') { location.reload(); }
        });
        // 通知宿主已就绪，宿主随即把当前播放状态推一次（避免刚打开时状态不同步）
        parent.postMessage({ type: 'mlab-ready' }, '*');
      })();
    <\/script>`
	return [
		'<!DOCTYPE html><html lang="zh-CN"><head><meta charset="utf-8">',
		'<style>html,body{height:100%;margin:0;background:#06070a;overflow:hidden;',
		"font-family:Inter,'PingFang SC','Microsoft YaHei',system-ui,sans-serif}",
		style,
		'</style></head><body>',
		detail.previewHtml,
		detail.previewJs ? `<script>${detail.previewJs}<\/script>` : '',
		bridge,
		'</body></html>',
	].join('\n')
})

/** 宿主侧的信封键：参数、重播键、尺寸变化时都需要换一份 srcdoc */
const frameKey = computed(() => `${props.detail?.templateKey}-${props.restartToken}-${JSON.stringify(props.paramValues)}`)

function send(state: { speed?: number; playing?: boolean }) {
	frame.value?.contentWindow?.postMessage({ type: 'mlab-control', ...state }, '*')
}

function restart() {
	send({ playing: true })
	frame.value?.contentWindow?.postMessage({ type: 'mlab-restart' }, '*')
}

// 播放状态变化时推给沙箱（不重建）
watch(
	() => [props.playing, props.speed] as const,
	() => send({ playing: props.playing, speed: props.speed }),
)

// 沙箱就绪后同步一次状态
function onFrameLoad() {
	send({ playing: props.playing, speed: props.speed })
}

defineExpose({ restart })

onBeforeUnmount(() => {
	// iframe 会随组件卸载一起销毁，这里不需要额外清理
})
</script>

<template>
	<div class="stage" :class="{ empty: !detail }">
		<iframe
			v-if="detail"
			:key="frameKey"
			ref="frame"
			class="stage-frame"
			:srcdoc="html"
			sandbox="allow-scripts"
			:style="{ transform: `scale(${scale})` }"
			title="动效预览"
			@load="onFrameLoad" />
		<div v-else class="stage-hint">从左侧挑一个动效，或在上方描述你想要的页面风格</div>
	</div>
</template>

<style scoped>
.stage {
	position: relative;
	height: 340px;
	border-radius: var(--ext-radius);
	overflow: hidden;
	background:
		radial-gradient(120% 100% at 50% 0%, rgba(94, 234, 212, 0.06), transparent 60%),
		linear-gradient(180deg, #0b0d13, #05060a);
	border: 1px solid var(--ext-line);
	display: grid;
	place-items: center;
}

.stage-frame {
	width: 100%;
	height: 100%;
	border: 0;
	display: block;
	transform-origin: 50% 50%;
	transition: transform 0.18s ease;
}

.stage-hint {
	color: var(--ext-text-mute);
	font-size: 13px;
	text-align: center;
	padding: 0 24px;
	line-height: 1.9;
}
</style>
