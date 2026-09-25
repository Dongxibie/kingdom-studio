<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { MOTION_CATEGORIES, MOTION_TECHNOLOGIES } from '@/extensions/motion-lab/types/motion'
import type { MotionDetail, MotionSavePayload } from '@/extensions/motion-lab/types/motion'

interface Props {
	modelValue: boolean
	detail: MotionDetail | null
	saving?: boolean
}

const props = withDefaults(defineProps<Props>(), { saving: false })
const emit = defineEmits<{
	'update:modelValue': [value: boolean]
	submit: [payload: MotionSavePayload]
}>()

const formRef = ref<FormInstance>()

const blank = (): MotionSavePayload => ({
	name: '',
	description: '',
	category: 'Entrance',
	technology: 'CSS',
	sourceUrl: '',
	repoUrl: '',
	previewUrl: '',
	tags: '',
	license: '',
	codePath: '',
	status: 'READY',
})

const form = reactive<MotionSavePayload>(blank())

const rules: FormRules = {
	name: [{ required: true, message: '请填写动效名称', trigger: 'blur' }],
	category: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

// 打开弹窗时把当前资源灌进表单（新增则是空表单）
watch(
	() => props.modelValue,
	(open) => {
		if (!open) {
			return
		}
		const detail = props.detail
		Object.assign(form, blank(), detail ? {
			name: detail.name,
			description: detail.description,
			category: detail.category,
			technology: detail.technology,
			sourceUrl: detail.sourceUrl,
			repoUrl: detail.repoUrl,
			previewUrl: detail.previewUrl,
			tags: detail.tags.join(','),
			license: detail.license,
			codePath: detail.codePath,
			status: detail.status,
		} : {})
	},
)

async function submit() {
	const valid = await formRef.value?.validate().catch(() => false)
	if (!valid) {
		return
	}
	emit('submit', { ...form })
}
</script>

<template>
	<el-dialog
		:model-value="modelValue"
		:title="detail ? '编辑动效' : '新增动效'"
		width="620px"
		append-to-body
		@update:model-value="(value: boolean) => emit('update:modelValue', value)">
		<el-form ref="formRef" :model="form" :rules="rules" label-width="82px" size="default">
			<el-form-item label="名称" prop="name">
				<el-input v-model="form.name" placeholder="如：玻璃卡片错位入场" />
			</el-form-item>
			<el-form-item label="分类" prop="category">
				<el-select v-model="form.category" style="width: 100%">
					<el-option v-for="item in MOTION_CATEGORIES" :key="item.key" :label="item.label + ' · ' + item.key" :value="item.key" />
				</el-select>
			</el-form-item>
			<el-form-item label="技术栈">
				<el-select v-model="form.technology" filterable allow-create style="width: 100%">
					<el-option v-for="item in MOTION_TECHNOLOGIES" :key="item" :label="item" :value="item" />
				</el-select>
			</el-form-item>
			<el-form-item label="说明">
				<el-input v-model="form.description" type="textarea" :rows="2" placeholder="一句话说明这个动效做了什么" />
			</el-form-item>
			<el-form-item label="标签">
				<el-input v-model="form.tags" placeholder="英文逗号分隔，如 glass,premium" />
			</el-form-item>
			<el-form-item label="来源地址">
				<el-input v-model="form.sourceUrl" placeholder="https://..." />
			</el-form-item>
			<el-form-item label="仓库地址">
				<el-input v-model="form.repoUrl" placeholder="可留空" />
			</el-form-item>
			<el-form-item label="许可">
				<el-input v-model="form.license" placeholder="如 MIT；展示时必须标注来源" />
			</el-form-item>
			<el-form-item label="状态">
				<el-radio-group v-model="form.status">
					<el-radio-button value="DRAFT">草稿</el-radio-button>
					<el-radio-button value="READY">可用</el-radio-button>
					<el-radio-button value="ARCHIVED">归档</el-radio-button>
				</el-radio-group>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="emit('update:modelValue', false)">取消</el-button>
			<el-button type="primary" :loading="saving" @click="submit">保存</el-button>
		</template>
	</el-dialog>
</template>
