import { onBeforeUnmount, onMounted, ref } from 'vue'

/** 窄屏断点：侧边栏折叠、工作台信息表改单列都以此为准 */
export const NARROW_MAX_WIDTH = 900

/**
 * 窄屏检测。
 *
 * <p>用同一个断点驱动多处布局（侧边栏宽度、信息表列数），避免各写一套宽度判断，
 * 出现「侧边栏收了但表格还是三列」这类对不齐的情况。
 */
export function useNarrowScreen() {
	const isNarrow = ref(window.innerWidth <= NARROW_MAX_WIDTH)

	function update() {
		isNarrow.value = window.innerWidth <= NARROW_MAX_WIDTH
	}

	onMounted(() => window.addEventListener('resize', update))
	onBeforeUnmount(() => window.removeEventListener('resize', update))

	return { isNarrow }
}
