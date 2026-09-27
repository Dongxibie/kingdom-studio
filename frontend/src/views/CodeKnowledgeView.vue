<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentCopy, Plus, Search } from '@element-plus/icons-vue'
import {
	createCodeSnippet,
	deleteCodeSnippet,
	fetchCodeSnippet,
	fetchCodeSnippets,
	updateCodeSnippet,
	type CodeSnippetDetail,
	type CodeSnippetItem,
	type CodeSnippetSavePayload
} from '@/api/knowledge'

const loading = ref(false)
const saving = ref(false)
const items = ref<CodeSnippetItem[]>([])
const total = ref(0)
const keyword = ref('')
const language = ref('')

/** 分类是自由文本（Java / Vue / AI / SQL / 工具…），标签页按已有数据实时汇总 */
const languages = computed(() => {
	const set = new Set<string>()
	for (const item of items.value) {
		set.add(item.language)
	}
	return ['', ...[...set].sort()]
})

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const detail = ref<CodeSnippetDetail | null>(null)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<CodeSnippetSavePayload>({
	title: '',
	language: 'Java',
	description: '',
	codeContent: '',
	tags: ''
})

const dialogTitle = computed(() => (editingId.value ? '编辑片段' : '新增片段'))

async function load() {
	loading.value = true
	try {
		const page = await fetchCodeSnippets({ keyword: keyword.value, language: language.value, size: 60 })
		items.value = page.records
		total.value = page.total
	} catch {
		// 请求层已提示
	} finally {
		loading.value = false
	}
}

function pick(value: string) {
	if (language.value === value) {
		return
	}
	language.value = value
	load()
}

async function openDetail(item: CodeSnippetItem) {
	drawerVisible.value = true
	drawerLoading.value = true
	detail.value = null
	try {
		detail.value = await fetchCodeSnippet(item.id)
	} catch {
		drawerVisible.value = false
	} finally {
		drawerLoading.value = false
	}
}

function openCreate() {
	editingId.value = null
	Object.assign(form, { title: '', language: language.value || 'Java', description: '', codeContent: '', tags: '' })
	dialogVisible.value = true
}

function openEdit(item: CodeSnippetItem) {
	editingId.value = item.id
	Object.assign(form, {
		title: item.title,
		language: item.language,
		description: item.description,
		codeContent: '',
		tags: item.tags.join(',')
	})
	// 列表不带正文，编辑时先取详情，避免一保存就把代码清空
	void fillCode(item.id)
	dialogVisible.value = true
}

async function fillCode(id: number) {
	try {
		const value = await fetchCodeSnippet(id)
		form.codeContent = value.codeContent
	} catch {
		// 取不到就保持空串，用户自己粘贴
	}
}

async function submit() {
	if (!form.title.trim() || !form.codeContent.trim()) {
		ElMessage.warning('标题与代码内容都不能为空')
		return
	}
	saving.value = true
	try {
		if (editingId.value) {
			await updateCodeSnippet(editingId.value, { ...form })
			ElMessage.success('片段已更新')
		} else {
			await createCodeSnippet({ ...form })
			ElMessage.success('片段已保存')
		}
		dialogVisible.value = false
		await load()
	} catch {
		// 重名等业务错误由请求层提示
	} finally {
		saving.value = false
	}
}

async function remove(item: CodeSnippetItem) {
	try {
		await ElMessageBox.confirm(`确定删除「${item.title}」吗？`, '删除片段', {
			confirmButtonText: '删除',
			cancelButtonText: '取消',
			type: 'warning'
		})
	} catch {
		return
	}
	try {
		await deleteCodeSnippet(item.id)
		ElMessage.success('片段已删除')
		if (detail.value?.id === item.id) {
			drawerVisible.value = false
		}
		await load()
	} catch {
		// 同上
	}
}

/** 详情抽屉里直接编辑：把详情转成列表项的形状复用同一个表单 */
function editFromDrawer() {
	const value = detail.value
	if (!value) {
		return
	}
	openEdit({
		id: value.id,
		title: value.title,
		language: value.language,
		description: value.description,
		tags: value.tags,
		lineCount: value.lineCount,
		createTime: value.createTime,
		updateTime: value.updateTime
	})
}

/** 详情抽屉里直接删除 */
function removeFromDrawer() {
	const value = detail.value
	if (!value) {
		return
	}
	void remove({
		id: value.id,
		title: value.title,
		language: value.language,
		description: value.description,
		tags: value.tags,
		lineCount: value.lineCount,
		createTime: value.createTime,
		updateTime: value.updateTime
	})
}

async function copyCode() {
	const code = detail.value?.codeContent
	if (!code) {
		return
	}
	try {
		await navigator.clipboard.writeText(code)
		ElMessage.success('代码已复制')
	} catch {
		// 非安全上下文里剪贴板不可用，退化成提示用户手动选择
		ElMessage.warning('当前浏览器不允许自动复制，请手动选择代码')
	}
}

onMounted(load)
</script>

<template>
	<div class="ks-page">
		<div class="ks-head">
			<div>
				<div class="ks-eyebrow">Code Library</div>
				<h2>代码知识库</h2>
				<p class="ks-sub">
					这里存的是解决方案而不是仓库：一段能直接抄走的代码，加一句「什么场景下用」。共 {{ total }} 段。
				</p>
			</div>
			<div class="ks-toolbar">
				<el-button type="primary" :icon="Plus" @click="openCreate">新增片段</el-button>
			</div>
		</div>

		<div class="ks-toolbar">
			<el-input
				v-model="keyword"
				placeholder="搜索标题、说明或标签"
				clearable
				class="snippet-search"
				@keyup.enter="load"
				@clear="load">
				<template #append>
					<el-button :icon="Search" @click="load" />
				</template>
			</el-input>
			<div class="lang-chips">
				<button
					v-for="item in languages"
					:key="item || 'all'"
					type="button"
					class="lang-chip"
					:class="{ on: language === item }"
					@click="pick(item)">
					{{ item || '全部' }}
				</button>
			</div>
		</div>

		<el-skeleton v-if="loading && !items.length" :rows="4" animated />

		<el-empty v-else-if="!items.length" description="没有匹配的片段" />

		<div v-else class="snippet-list">
			<article v-for="item in items" :key="item.id" class="snippet" @click="openDetail(item)">
				<header class="snippet-head">
					<span class="snippet-title">{{ item.title }}</span>
					<span class="ks-chip ks-chip--gold">{{ item.language }}</span>
				</header>
				<p class="snippet-desc">{{ item.description || '没有写说明。' }}</p>
				<footer class="snippet-foot">
					<span class="snippet-tags">
						<span v-for="tag in item.tags" :key="tag" class="ks-chip">{{ tag }}</span>
					</span>
					<span class="snippet-meta ks-muted">{{ item.lineCount }} 行 · {{ item.createTime?.slice(0, 10) }}</span>
				</footer>
			</article>
		</div>

		<el-drawer v-model="drawerVisible" size="640px" :with-header="false">
			<el-skeleton v-if="drawerLoading" :rows="6" animated />
			<div v-else-if="detail" class="drawer-body">
				<div class="ks-eyebrow">{{ detail.language }}</div>
				<h3 class="drawer-title">{{ detail.title }}</h3>
				<p class="ks-sub">{{ detail.description }}</p>
				<div class="drawer-tags">
					<span v-for="tag in detail.tags" :key="tag" class="ks-chip">{{ tag }}</span>
					<span class="ks-muted">{{ detail.lineCount }} 行</span>
				</div>
				<pre class="code-block"><code>{{ detail.codeContent }}</code></pre>
				<div class="drawer-actions">
					<el-button type="primary" :icon="DocumentCopy" @click="copyCode">复制代码</el-button>
					<el-button @click="editFromDrawer">编辑</el-button>
					<el-button type="danger" plain @click="removeFromDrawer">删除</el-button>
					<el-button @click="drawerVisible = false">关闭</el-button>
				</div>
			</div>
		</el-drawer>

		<el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" top="6vh">
			<el-form label-position="top">
				<div class="form-row">
					<el-form-item label="标题">
						<el-input v-model="form.title" maxlength="100" placeholder="例如：MySQL 幂等 ALTER 模板" />
					</el-form-item>
					<el-form-item label="语言 / 分类">
						<el-input v-model="form.language" maxlength="20" placeholder="Java / Vue / AI / SQL / 工具" />
					</el-form-item>
				</div>
				<el-form-item label="说明：什么场景下用">
					<el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
				</el-form-item>
				<el-form-item label="代码内容">
					<el-input v-model="form.codeContent" type="textarea" :rows="10" placeholder="粘贴代码" class="code-input" />
				</el-form-item>
				<el-form-item label="标签（英文逗号分隔）">
					<el-input v-model="form.tags" placeholder="MySQL,迁移,幂等" />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="dialogVisible = false">取消</el-button>
				<el-button type="primary" :loading="saving" @click="submit">保存</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<style scoped>
.snippet-search {
	max-width: 340px;
}

.lang-chips {
	display: flex;
	flex-wrap: wrap;
	gap: 6px;
}

.lang-chip {
	border: 1px solid var(--ks-line);
	background: var(--ks-surface);
	color: var(--ks-ink-2);
	border-radius: 999px;
	padding: 5px 13px;
	font-size: 12.5px;
	cursor: pointer;
}

.lang-chip:hover {
	border-color: var(--ks-gold-soft);
	color: var(--ks-gold-deep);
}

.lang-chip.on {
	background: #fdf8ec;
	border-color: var(--ks-gold);
	color: var(--ks-gold-deep);
	font-weight: 600;
}

.snippet-list {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
	gap: 14px;
}

.snippet {
	display: flex;
	flex-direction: column;
	gap: 8px;
	background: var(--ks-surface);
	border: 1px solid var(--ks-line);
	border-radius: 14px;
	padding: 15px;
	cursor: pointer;
	transition: box-shadow 0.18s ease, transform 0.18s ease;
}

.snippet:hover {
	box-shadow: 0 8px 24px rgba(36, 31, 25, 0.07);
	transform: translateY(-1px);
}

.snippet-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 10px;
}

.snippet-title {
	font-size: 15px;
	font-weight: 600;
}

.snippet-desc {
	margin: 0;
	color: var(--ks-ink-2);
	font-size: 13px;
	line-height: 1.7;
}

.snippet-foot {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	margin-top: auto;
	padding-top: 6px;
	border-top: 1px solid var(--ks-line-soft);
}

.snippet-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 5px;
}

.snippet-meta {
	white-space: nowrap;
}

.drawer-body {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.drawer-title {
	margin: 0;
	font-size: 20px;
}

.drawer-tags {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: 6px;
}

.code-block {
	margin: 0;
	background: #f7f4ee;
	border: 1px solid var(--ks-line);
	border-radius: 12px;
	padding: 14px;
	overflow-x: auto;
	font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
	font-size: 12.5px;
	line-height: 1.7;
	color: var(--ks-ink);
	white-space: pre;
}

.drawer-actions {
	display: flex;
	gap: 8px;
}

.form-row {
	display: grid;
	grid-template-columns: 2fr 1fr;
	gap: 12px;
}

.code-input :deep(textarea) {
	font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
	font-size: 12.5px;
}
</style>
