<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCrawlKeywords, runCrawl } from '@/extensions/motion-lab/api/crawl'
import type { CrawlResult } from '@/extensions/motion-lab/types/crawl'

interface Props {
	modelValue: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
	'update:modelValue': [value: boolean]
	/** 真正写库成功后才通知外部刷新列表 */
	crawled: []
}>()

const presetKeywords = ref<string[]>([])
const selected = ref<string[]>([])
const customKeyword = ref('')
const limitPerKeyword = ref(5)
const dryRun = ref(true)
const running = ref(false)
const result = ref<CrawlResult | null>(null)

const keywords = computed(() => {
	const list = [...selected.value]
	const custom = customKeyword.value.trim()
	if (custom) {
		list.push(custom)
	}
	return list
})

// 每次打开都回到「先试算」的安全默认值：正式写库要用户自己点掉试算开关
watch(
	() => props.modelValue,
	async (open) => {
		if (!open) {
			return
		}
		result.value = null
		customKeyword.value = ''
		dryRun.value = true
		if (!presetKeywords.value.length) {
			try {
				presetKeywords.value = await fetchCrawlKeywords()
			} catch {
				// 关键词拉不到不影响使用，用户可以自己输入
				presetKeywords.value = []
			}
		}
		selected.value = presetKeywords.value.slice(0, 1)
	},
)

async function execute() {
	if (!keywords.value.length) {
		ElMessage.warning('请至少选择一个关键词')
		return
	}
	running.value = true
	try {
		result.value = await runCrawl({
			keywords: keywords.value,
			limitPerKeyword: limitPerKeyword.value,
			dryRun: dryRun.value,
		})
		if (dryRun.value) {
			ElMessage.success('试算完成，未写入数据库')
		} else if (result.value.created > 0) {
			ElMessage.success(`已新增 ${result.value.created} 条动效资源`)
			emit('crawled')
		} else {
			ElMessage.info('没有新增：命中的都已在库里')
		}
	} catch (error) {
		ElMessage.error(error instanceof Error ? error.message : '采集失败')
	} finally {
		running.value = false
	}
}
</script>

<template>
	<el-dialog
		:model-value="modelValue"
		title="从 GitHub 采集动效"
		width="680px"
		append-to-body
		@update:model-value="(value: boolean) => emit('update:modelValue', value)">
		<div class="crawl-form">
			<div class="crawl-row">
				<span class="crawl-label">关键词</span>
				<el-checkbox-group v-model="selected" class="crawl-keywords">
					<el-checkbox v-for="item in presetKeywords" :key="item" :value="item">{{ item }}</el-checkbox>
				</el-checkbox-group>
			</div>
			<div class="crawl-row">
				<span class="crawl-label">补充关键词</span>
				<el-input v-model="customKeyword" placeholder="可留空，例如 glassmorphism card hover" />
			</div>
			<div class="crawl-row">
				<span class="crawl-label">每词条数</span>
				<el-input-number v-model="limitPerKeyword" :min="1" :max="30" />
				<span class="crawl-label crawl-gap">试算（不写库）</span>
				<el-switch v-model="dryRun" />
			</div>
			<div class="crawl-note">
				采集会按关键词逐个请求 GitHub，词与词之间有固定间隔，命中限流会立即停下并如实汇报；
				试算开关默认打开，确认结果没问题再关掉它写库。
			</div>
		</div>

		<div v-if="result" class="crawl-result">
			<div class="crawl-stats">
				<span>扫描 {{ result.scanned }}</span>
				<span>新增 {{ result.created }}</span>
				<span>重复来源 {{ result.skippedByUrl }}</span>
				<span>重复指纹 {{ result.skippedByContent }}</span>
				<span>文本相似 {{ result.skippedBySimilarity }}</span>
				<span>失败 {{ result.failed }}</span>
				<span class="crawl-mode">{{ result.dryRun ? '试算' : '已写库' }}</span>
			</div>
			<div v-if="Object.keys(result.byCategory).length" class="crawl-categories">
				分类：
				<span v-for="(count, name) in result.byCategory" :key="name" class="crawl-chip">{{ name }} {{ count }}</span>
			</div>
			<ul class="crawl-notes">
				<li v-for="(note, index) in result.notes" :key="index">{{ note }}</li>
			</ul>
		</div>

		<template #footer>
			<el-button @click="emit('update:modelValue', false)">关闭</el-button>
			<el-button type="primary" :loading="running" @click="execute">
				{{ dryRun ? '试算' : '开始采集' }}
			</el-button>
		</template>
	</el-dialog>
</template>

<style scoped>
.crawl-form {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.crawl-row {
	display: flex;
	align-items: center;
	gap: 10px;
}

.crawl-label {
	width: 76px;
	flex: none;
	color: var(--ext-text-dim);
	font-size: 13px;
}

.crawl-gap {
	width: auto;
	margin-left: 12px;
}

.crawl-keywords {
	display: flex;
	flex-wrap: wrap;
	gap: 4px 12px;
}

.crawl-note {
	color: var(--ext-text-mute);
	font-size: 12px;
	line-height: 1.7;
	border-left: 2px solid var(--ext-line);
	padding-left: 10px;
}

.crawl-result {
	margin-top: 14px;
	border-top: 1px solid var(--ext-line);
	padding-top: 12px;
}

.crawl-stats {
	display: flex;
	flex-wrap: wrap;
	gap: 6px 14px;
	font-family: var(--ext-font-mono);
	font-size: 12px;
	color: var(--ext-text-dim);
}

.crawl-mode {
	color: var(--ext-gold);
}

.crawl-categories {
	margin-top: 8px;
	font-size: 12px;
	color: var(--ext-text-dim);
}

.crawl-chip {
	display: inline-block;
	margin-right: 6px;
	padding: 1px 8px;
	border-radius: 999px;
	background: var(--ext-gold-soft);
	color: var(--ext-gold-light);
	font-family: var(--ext-font-mono);
}

.crawl-notes {
	margin: 10px 0 0;
	padding-left: 18px;
	max-height: 130px;
	overflow: auto;
	font-size: 12px;
	line-height: 1.8;
	color: var(--ext-text-mute);
}
</style>
