/**
 * 演示歌曲：点一下就能导入的简谱。
 *
 * 用的是后端已有的简谱解析（POST /music/tasks/jianpu），和手工粘贴走的是同一条路，
 * 所以这里只放谱子、不放任何解析逻辑。
 */
export interface DemoSong {
	name: string
	hint: string
	jianpu: string
}

export const DEMO_SONGS: DemoSong[] = [
	{
		name: '小星星',
		hint: '14 个音 · 96 BPM · 入门首选',
		jianpu: `1=C 4/4 BPM=96
1 1 5 5 | 6 6 5 - | 4 4 3 3 | 2 2 1 -`,
	},
	{
		name: '两只老虎',
		hint: '32 个音 · 108 BPM · 有重复段落',
		jianpu: `1=C 4/4 BPM=108
1 2 3 1 | 1 2 3 1 | 3 4 5 - | 3 4 5 - | 5 6 5 4 3 1 | 5 6 5 4 3 1 | 1 5 1 - | 1 5 1 -`,
	},
	{
		name: '音阶练习',
		hint: '8 个音 · 120 BPM · 拿来校准键位',
		jianpu: `1=C 4/4 BPM=120
1 2 3 4 | 5 6 7 1'`,
	},
]
