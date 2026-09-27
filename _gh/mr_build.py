# -*- coding: utf-8 -*-
"""组合方案种子生成器。

做三件事：
  1. 校验数据里的每个 templateKey 都真实存在（宁可生成失败，也不要写一份带悬空引用的种子）；
  2. 按服务端同一套权重算每个方案的推荐指数缓存值（视觉 30 / 代码 25 / 复用 25 / 性能 20，成员取均值）；
  3. 生成 db/extensions_motion_recipe_seed.sql（幂等 upsert，按 recipe_key）。

用法：python _gh/mr_build.py
"""
import io
import json
import subprocess
import sys

sys.path.insert(0, '_gh')
from mr_data import RECIPES  # noqa: E402

DB = 'kingdom_studio'
OUT_SEED = 'db/extensions_motion_recipe_seed.sql'


def query(sql):
	"""用 mysql 客户端取数据：-N 去掉表头，-B 去掉边框，字段用 Tab 分隔。"""
	out = subprocess.run(
		['mysql', '-uroot', '-proot', '-N', '-B', '--default-character-set=utf8mb4', DB, '-e', sql],
		capture_output=True)
	if out.returncode != 0:
		raise SystemExit('查询失败：' + out.stderr.decode('utf-8', 'replace'))
	rows = []
	for line in out.stdout.decode('utf-8').splitlines():
		if line.strip():
			rows.append(line.split('\t'))
	return rows


def esc(value):
	"""SQL 字符串转义：只处理单引号与反斜杠，其余原样。"""
	return str(value).replace('\\', '\\\\').replace("'", "''")


def main():
	rows = query('SELECT template_key, score_visual, score_code, score_reuse, score_perf FROM motion_template;')
	scores, missing = {}, []
	for key, visual, code, reuse, perf in rows:
		scores[key] = (int(visual) * 0.30 + int(code) * 0.25 + int(reuse) * 0.25 + int(perf) * 0.20)

	used = [step[0] for recipe in RECIPES for step in recipe['steps']]
	for key in sorted(set(used)):
		if key not in scores:
			missing.append(key)
	if missing:
		raise SystemExit('数据里引用了不存在的模板：' + ', '.join(missing))

	lines = []
	lines.append('-- =====================================================================')
	lines.append('-- 组合方案（Motion Recipe）种子：25 套新方案 + 5 套既有方案（补上步骤）')
	lines.append('--')
	lines.append('-- 生成器：_gh/mr_build.py（数据源 _gh/mr_data.py）—— 不要手改本文件，改数据后重跑生成器。')
	lines.append('-- 幂等：按 recipe_key upsert，重跑只刷新方案本身，不动人工评分（motion_rating 单独一张表）。')
	lines.append('-- 依赖：motion_recipe.steps 列（见 db/extensions_motion_recipe.sql）。')
	lines.append('-- =====================================================================')
	lines.append('')
	lines.append('USE `kingdom_studio`;')
	lines.append('')
	lines.append('SET NAMES utf8mb4;')
	lines.append('')
	lines.append('-- ---------------------------------------------------------------------')
	lines.append('-- 组合方案：每个方案是一串按应用顺序排列的步骤，每一步带')
	lines.append('--   templateKey 用哪个模板 / stage 负责哪一层 / role 这一步的作用')
	lines.append('-- score 是按成员实时算出来的缓存值，服务端每次都重算（与模板列表口径一致）。')
	lines.append('-- ---------------------------------------------------------------------')
	lines.append('INSERT INTO `motion_recipe`')
	lines.append('\t(`recipe_key`, `name`, `description`, `scene`, `style`, `best_for`, `score`,')
	lines.append('\t `template_keys`, `steps`, `prompt`, `status`)')
	lines.append('VALUES')

	values = []
	for recipe in RECIPES:
		steps = [dict(templateKey=key, stage=stage, role=role) for key, stage, role in recipe['steps']]
		keys = [step['templateKey'] for step in steps]
		score = round(sum(scores[key] for key in keys) / len(keys))
		values.append('\t(' + ', '.join([
			"'" + esc(recipe['key']) + "'",
			"'" + esc(recipe['name']) + "'",
			"'" + esc(recipe['description']) + "'",
			"'" + esc(recipe['scene']) + "'",
			"'" + esc(recipe['style']) + "'",
			"'" + esc(recipe['best_for']) + "'",
			str(score),
			"'" + esc(json.dumps(keys, ensure_ascii=False)) + "'",
			"'" + esc(json.dumps(steps, ensure_ascii=False)) + "'",
			"'" + esc(recipe['prompt']) + "'",
			"'READY'",
		]) + ')')
	lines.append(',\n'.join(values))
	lines.append('ON DUPLICATE KEY UPDATE')
	lines.append('\t`name` = VALUES(`name`), `description` = VALUES(`description`), `scene` = VALUES(`scene`),')
	lines.append('\t`style` = VALUES(`style`), `best_for` = VALUES(`best_for`), `score` = VALUES(`score`),')
	lines.append('\t`template_keys` = VALUES(`template_keys`), `steps` = VALUES(`steps`), `prompt` = VALUES(`prompt`);')
	lines.append('')

	io.open(OUT_SEED, 'w', encoding='utf-8', newline='\n').write('\n'.join(lines))

	scenes, tiers = {}, {}
	for recipe in RECIPES:
		scenes[recipe['scene']] = scenes.get(recipe['scene'], 0) + 1
	print('方案数 =', len(RECIPES), '步骤数 =', len(used))
	print('场景分布 =', scenes)
	print('写出', OUT_SEED)


if __name__ == '__main__':
	main()
