-- =====================================================================
-- Kingdom Studio · Motion Lab 2.0 社区精选 + 候选池（自动生成，请勿手工编辑）
--
-- 生成器：_gh/mc_build.py（发现数据 _gh/mc_candidates.json，Pattern 数据 _gh/mc_data1-3.py）
-- 内容：社区精选模板 30 个 + 候选池 118 条
-- 纪律：候选池只存元数据；进库的模板代码全部是 Kingdom Studio 原创实现，
--       来源地址与许可只用于标注灵感出处。
-- 幂等：模板按 template_key upsert，候选按 candidate_key upsert，可重复执行。
-- =====================================================================

USE `kingdom_studio`;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 社区精选模板（Community Collection）
-- ---------------------------------------------------------------------
INSERT INTO `motion_template`
	(`template_key`, `name`, `name_en`, `description`, `category`, `scene`, `style`, `technology`,
	 `difficulty`, `best_for`, `trigger_type`, `score_visual`, `score_code`, `score_reuse`, `score_perf`,
	 `score`, `runtime_tier`, `runtime_note`, `params`, `preview_url`, `preview_html`, `preview_js`,
	 `css_code`, `vue_code`, `react_code`, `three_code`, `prompt`, `tags`, `source`, `source_url`,
	 `source_license`, `status`)
VALUES
	('split-char-rise', '逐字上升', 'Split Char Rise', '把标题拆成单个字，从下方逐个升起并虚化到实——比整段淡入更有节奏，适合首屏大标题。', '文字动画', 'Landing Page', 'Luxury', 'CSS', 2, '官网首页,产品发布页,个人作品集', 'load', 91, 88, 86, 94, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.4, "max": 1.6, "step": 0.1, "default": 0.8}, {"key": "--m-stagger", "label": "逐字间隔", "unit": "s", "min": 0.03, "max": 0.2, "step": 0.01, "default": 0.06}]', '', '<div class="motion-root">
  <h2 class="m-split" aria-label="Kingdom Studio">
    <span>K</span><span>i</span><span>n</span><span>g</span><span>d</span><span>o</span><span>m</span>
  </h2>
  <p class="m-sub">逐字上升 · Split Char Rise</p>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-split {
  margin: 0;
  display: flex;
  gap: 0.04em;
  font: 700 54px/1 system-ui, "PingFang SC", sans-serif;
  color: #f4f6f8;
}
.m-split span {
  display: inline-block;
  opacity: 0;
  filter: blur(8px);
  transform: translateY(0.42em);
  animation: m-char-rise var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
.m-split span:nth-child(1) { animation-delay: calc(var(--m-stagger) * 0); }
.m-split span:nth-child(2) { animation-delay: calc(var(--m-stagger) * 1); }
.m-split span:nth-child(3) { animation-delay: calc(var(--m-stagger) * 2); }
.m-split span:nth-child(4) { animation-delay: calc(var(--m-stagger) * 3); }
.m-split span:nth-child(5) { animation-delay: calc(var(--m-stagger) * 4); }
.m-split span:nth-child(6) { animation-delay: calc(var(--m-stagger) * 5); }
.m-split span:nth-child(7) { animation-delay: calc(var(--m-stagger) * 6); }
.m-sub {
  margin: 14px 0 0;
  font: 400 13px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.16em;
  color: rgba(244, 246, 248, 0.42);
}
@keyframes m-char-rise {
  to { opacity: 1; filter: blur(0); transform: translateY(0); }
}', '<template>
    <div class="motion-root">
      <h2 class="m-split" aria-label="Kingdom Studio">
        <span>K</span><span>i</span><span>n</span><span>g</span><span>d</span><span>o</span><span>m</span>
      </h2>
      <p class="m-sub">逐字上升 · Split Char Rise</p>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-split {
  margin: 0;
  display: flex;
  gap: 0.04em;
  font: 700 54px/1 system-ui, "PingFang SC", sans-serif;
  color: #f4f6f8;
}
.m-split span {
  display: inline-block;
  opacity: 0;
  filter: blur(8px);
  transform: translateY(0.42em);
  animation: m-char-rise var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
.m-split span:nth-child(1) { animation-delay: calc(var(--m-stagger) * 0); }
.m-split span:nth-child(2) { animation-delay: calc(var(--m-stagger) * 1); }
.m-split span:nth-child(3) { animation-delay: calc(var(--m-stagger) * 2); }
.m-split span:nth-child(4) { animation-delay: calc(var(--m-stagger) * 3); }
.m-split span:nth-child(5) { animation-delay: calc(var(--m-stagger) * 4); }
.m-split span:nth-child(6) { animation-delay: calc(var(--m-stagger) * 5); }
.m-split span:nth-child(7) { animation-delay: calc(var(--m-stagger) * 6); }
.m-sub {
  margin: 14px 0 0;
  font: 400 13px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.16em;
  color: rgba(244, 246, 248, 0.42);
}
@keyframes m-char-rise {
  to { opacity: 1; filter: blur(0); transform: translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <h2 className="m-split" aria-label="Kingdom Studio">
          <span>K</span><span>i</span><span>n</span><span>g</span><span>d</span><span>o</span><span>m</span>
        </h2>
        <p className="m-sub">逐字上升 · Split Char Rise</p>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-split {
  margin: 0;
  display: flex;
  gap: 0.04em;
  font: 700 54px/1 system-ui, "PingFang SC", sans-serif;
  color: #f4f6f8;
}
.m-split span {
  display: inline-block;
  opacity: 0;
  filter: blur(8px);
  transform: translateY(0.42em);
  animation: m-char-rise var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
.m-split span:nth-child(1) { animation-delay: calc(var(--m-stagger) * 0); }
.m-split span:nth-child(2) { animation-delay: calc(var(--m-stagger) * 1); }
.m-split span:nth-child(3) { animation-delay: calc(var(--m-stagger) * 2); }
.m-split span:nth-child(4) { animation-delay: calc(var(--m-stagger) * 3); }
.m-split span:nth-child(5) { animation-delay: calc(var(--m-stagger) * 4); }
.m-split span:nth-child(6) { animation-delay: calc(var(--m-stagger) * 5); }
.m-split span:nth-child(7) { animation-delay: calc(var(--m-stagger) * 6); }
.m-sub {
  margin: 14px 0 0;
  font: 400 13px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.16em;
  color: rgba(244, 246, 248, 0.42);
}
@keyframes m-char-rise {
  to { opacity: 1; filter: blur(0); transform: translateY(0); }
}
*/', '', '做一段逐字上升的标题动画：把标题拆成单字（每个字一个 span），初始 opacity:0、filter:blur(8px)、translateY(0.42em)，用 cubic-bezier(0.22,0.61,0.36,1) 逐个升起到位；每个字的 animation-delay 按索引递增（0.06s 一档），整体时长 0.8s。', 'split text,stagger,headline,community,pomber__git-history', 'COMMUNITY', 'https://github.com/pomber/git-history', 'MIT', 'READY'),
	('blur-focus-in', '模糊聚焦', 'Blur Focus In', '文字从重模糊逐渐聚焦，像镜头对焦——适合需要「安静但高级」的段落或副标题。', '文字动画', 'Portfolio', 'Minimal', 'CSS', 1, '个人作品集,官网首页,登录页面', 'load', 85, 93, 90, 95, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.5, "max": 2.4, "step": 0.1, "default": 1.2}, {"key": "--m-blur", "label": "起始模糊", "unit": "px", "min": 4, "max": 24, "step": 1, "default": 14}]', '', '<div class="motion-root">
  <p class="m-focus">把注意力<br />交给内容本身</p>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-focus {
  margin: 0;
  text-align: center;
  font: 600 34px/1.35 system-ui, "PingFang SC", sans-serif;
  color: #f4f6f8;
  animation: m-focus-in var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
@keyframes m-focus-in {
  from { filter: blur(var(--m-blur)); opacity: 0.15; letter-spacing: 0.08em; }
  to { filter: blur(0); opacity: 1; letter-spacing: 0; }
}', '<template>
    <div class="motion-root">
      <p class="m-focus">把注意力<br />交给内容本身</p>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-focus {
  margin: 0;
  text-align: center;
  font: 600 34px/1.35 system-ui, "PingFang SC", sans-serif;
  color: #f4f6f8;
  animation: m-focus-in var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
@keyframes m-focus-in {
  from { filter: blur(var(--m-blur)); opacity: 0.15; letter-spacing: 0.08em; }
  to { filter: blur(0); opacity: 1; letter-spacing: 0; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <p className="m-focus">把注意力<br />交给内容本身</p>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-focus {
  margin: 0;
  text-align: center;
  font: 600 34px/1.35 system-ui, "PingFang SC", sans-serif;
  color: #f4f6f8;
  animation: m-focus-in var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
@keyframes m-focus-in {
  from { filter: blur(var(--m-blur)); opacity: 0.15; letter-spacing: 0.08em; }
  to { filter: blur(0); opacity: 1; letter-spacing: 0; }
}
*/', '', '做一段模糊聚焦的文字入场：从 filter:blur(14px)、opacity:0.15、字距 0.08em 过渡到完全清晰，时长 1.2s，缓动 cubic-bezier(0.22,0.61,0.36,1)；模糊半径与时长都做成可调参数。', 'blur,text,focus,community,denvercoder1__readme-typing-svg', 'COMMUNITY', 'https://github.com/DenverCoder1/readme-typing-svg', 'MIT', 'READY'),
	('gradient-text-flow', '渐变文字流动', 'Flowing Gradient Text', '文字用 background-clip 做渐变填充，并让渐变的色带缓慢横向流动，静态标题也有呼吸感。', '文字动画', 'AI SaaS', 'Luxury', 'CSS', 2, 'AI 产品页,官网首页,个人作品集', 'load', 92, 90, 88, 88, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-speed", "label": "流动周期", "unit": "s", "min": 3, "max": 16, "step": 0.5, "default": 8}, {"key": "--m-size", "label": "色带宽度", "unit": "%", "min": 120, "max": 340, "step": 10, "default": 220}]', '', '<div class="motion-root">
  <h2 class="m-flow">Premium Motion</h2>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-flow {
  margin: 0;
  font: 800 46px/1.1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.01em;
  background: linear-gradient(100deg, #f0cd72 0%, #ff8f6b 26%, #a78bfa 52%, #5eead4 76%, #f0cd72 100%);
  background-size: var(--m-size) 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: m-flow var(--m-speed) linear infinite;
}
@keyframes m-flow {
  from { background-position: 0% 50%; }
  to { background-position: 100% 50%; }
}', '<template>
    <div class="motion-root">
      <h2 class="m-flow">Premium Motion</h2>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-flow {
  margin: 0;
  font: 800 46px/1.1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.01em;
  background: linear-gradient(100deg, #f0cd72 0%, #ff8f6b 26%, #a78bfa 52%, #5eead4 76%, #f0cd72 100%);
  background-size: var(--m-size) 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: m-flow var(--m-speed) linear infinite;
}
@keyframes m-flow {
  from { background-position: 0% 50%; }
  to { background-position: 100% 50%; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <h2 className="m-flow">Premium Motion</h2>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-flow {
  margin: 0;
  font: 800 46px/1.1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.01em;
  background: linear-gradient(100deg, #f0cd72 0%, #ff8f6b 26%, #a78bfa 52%, #5eead4 76%, #f0cd72 100%);
  background-size: var(--m-size) 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: m-flow var(--m-speed) linear infinite;
}
@keyframes m-flow {
  from { background-position: 0% 50%; }
  to { background-position: 100% 50%; }
}
*/', '', '做一段渐变文字流动：用 linear-gradient(100deg, 金→橙→紫→青) 做 background-clip:text 填充，background-size 设到 220%，动画只改 background-position 从 0% 到 100% 循环，周期 8 秒线性播放。', 'gradient,text,shimmer,community,cgoldsby__logincritter', 'COMMUNITY', 'https://github.com/cgoldsby/LoginCritter', 'MIT', 'READY'),
	('typewriter-caret', '打字机光标', 'Typewriter Caret', '用 steps() 让文本按字符逐格出现（不写一行 JS），配一个常亮闪烁的光标，适合宣言式短句。', '文字动画', 'Login', 'Cyber', 'CSS', 1, '登录页面,AI 产品页,个人作品集', 'load', 84, 94, 92, 96, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "打字时长", "unit": "s", "min": 0.8, "max": 4, "step": 0.1, "default": 2.2}, {"key": "--m-caret", "label": "光标闪烁", "unit": "s", "min": 0.4, "max": 1.4, "step": 0.1, "default": 0.8}]', '', '<div class="motion-root">
  <div class="m-type"><span class="m-type-text">欢迎回来，继续建造</span></div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-type {
  display: flex;
  align-items: center;
  font: 600 30px/1.2 ui-monospace, "Cascadia Mono", Consolas, monospace;
  color: #e8f7f2;
}
.m-type-text {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  /* 9 个字：步数必须等于字符数，steps 才能每步露出一个字符 */
  width: 9ch;
  animation: m-typing var(--m-duration) steps(9, end) forwards;
}
.m-type::after {
  content: "";
  width: 2px;
  height: 1.1em;
  margin-left: 3px;
  background: #5eead4;
  animation: m-caret var(--m-caret) steps(1, end) infinite;
}
@keyframes m-typing {
  from { width: 0; }
  to { width: 9ch; }
}
@keyframes m-caret {
  0%, 50% { opacity: 1; }
  50.01%, 100% { opacity: 0; }
}', '<template>
    <div class="motion-root">
      <div class="m-type"><span class="m-type-text">欢迎回来，继续建造</span></div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-type {
  display: flex;
  align-items: center;
  font: 600 30px/1.2 ui-monospace, "Cascadia Mono", Consolas, monospace;
  color: #e8f7f2;
}
.m-type-text {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  /* 9 个字：步数必须等于字符数，steps 才能每步露出一个字符 */
  width: 9ch;
  animation: m-typing var(--m-duration) steps(9, end) forwards;
}
.m-type::after {
  content: "";
  width: 2px;
  height: 1.1em;
  margin-left: 3px;
  background: #5eead4;
  animation: m-caret var(--m-caret) steps(1, end) infinite;
}
@keyframes m-typing {
  from { width: 0; }
  to { width: 9ch; }
}
@keyframes m-caret {
  0%, 50% { opacity: 1; }
  50.01%, 100% { opacity: 0; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-type"><span className="m-type-text">欢迎回来，继续建造</span></div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-type {
  display: flex;
  align-items: center;
  font: 600 30px/1.2 ui-monospace, "Cascadia Mono", Consolas, monospace;
  color: #e8f7f2;
}
.m-type-text {
  display: inline-block;
  overflow: hidden;
  white-space: nowrap;
  /* 9 个字：步数必须等于字符数，steps 才能每步露出一个字符 */
  width: 9ch;
  animation: m-typing var(--m-duration) steps(9, end) forwards;
}
.m-type::after {
  content: "";
  width: 2px;
  height: 1.1em;
  margin-left: 3px;
  background: #5eead4;
  animation: m-caret var(--m-caret) steps(1, end) infinite;
}
@keyframes m-typing {
  from { width: 0; }
  to { width: 9ch; }
}
@keyframes m-caret {
  0%, 50% { opacity: 1; }
  50.01%, 100% { opacity: 0; }
}
*/', '', '做一段纯 CSS 打字机效果：文本容器定宽并用 overflow:hidden，宽度从 0 动画到 9ch，animation-timing-function 用 steps(9, end) 让字符逐格出现；再用伪元素做一根 2px 光标，用 steps(1) 在 0.8s 周期内闪烁。', 'typewriter,mono,caret,community,alexmacarthur__typeit', 'COMMUNITY', 'https://github.com/alexmacarthur/typeit', 'GPL-3.0', 'READY'),
	('digit-roll-up', '数字位翻滚', 'Digit Roll Up', '数据后台里的数字盘：每一位数字像转轮一样翻滚到目标值，比直接改数字更有「正在计算」的感觉。', '文字动画', 'Dashboard', 'Cyber', 'CSS', 2, '数据后台,AI 产品页,产品发布页', 'load', 87, 90, 88, 92, 89, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "翻滚时长", "unit": "s", "min": 0.6, "max": 3, "step": 0.1, "default": 1.6}, {"key": "--m-stagger", "label": "逐位延迟", "unit": "s", "min": 0, "max": 0.3, "step": 0.02, "default": 0.08}]', '', '<div class="motion-root">
  <div class="m-digits">
    <span class="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
    <span class="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
    <span class="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
    <span class="m-dot">%</span>
  </div>
  <p class="m-cap">本季度动效复用率</p>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-digits {
  display: flex;
  align-items: baseline;
  gap: 2px;
  font: 700 62px/1 ui-monospace, "Cascadia Mono", Consolas, monospace;
  color: #f0cd72;
}
.m-digit {
  display: block;
  height: 1em;
  overflow: hidden;
}
.m-digit i {
  display: block;
  font-style: normal;
  line-height: 1;
  animation: m-roll var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
.m-digit:nth-child(1) i { animation-delay: calc(var(--m-stagger) * 0); }
.m-digit:nth-child(2) i { animation-delay: calc(var(--m-stagger) * 1); }
.m-digit:nth-child(3) i { animation-delay: calc(var(--m-stagger) * 2); }
.m-dot { font-size: 34px; color: rgba(240, 205, 114, 0.7); }
.m-cap {
  margin: 14px 0 0;
  font: 400 13px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.14em;
  color: rgba(244, 246, 248, 0.42);
}
/* 每一位都从 0 滚到目标位：用 transform 位移整列数字，位移量决定最终停在哪个数字上 */
@keyframes m-roll {
  from { transform: translateY(0); }
  to { transform: translateY(-62%); }
}', '<template>
    <div class="motion-root">
      <div class="m-digits">
        <span class="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
        <span class="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
        <span class="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
        <span class="m-dot">%</span>
      </div>
      <p class="m-cap">本季度动效复用率</p>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-digits {
  display: flex;
  align-items: baseline;
  gap: 2px;
  font: 700 62px/1 ui-monospace, "Cascadia Mono", Consolas, monospace;
  color: #f0cd72;
}
.m-digit {
  display: block;
  height: 1em;
  overflow: hidden;
}
.m-digit i {
  display: block;
  font-style: normal;
  line-height: 1;
  animation: m-roll var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
.m-digit:nth-child(1) i { animation-delay: calc(var(--m-stagger) * 0); }
.m-digit:nth-child(2) i { animation-delay: calc(var(--m-stagger) * 1); }
.m-digit:nth-child(3) i { animation-delay: calc(var(--m-stagger) * 2); }
.m-dot { font-size: 34px; color: rgba(240, 205, 114, 0.7); }
.m-cap {
  margin: 14px 0 0;
  font: 400 13px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.14em;
  color: rgba(244, 246, 248, 0.42);
}
/* 每一位都从 0 滚到目标位：用 transform 位移整列数字，位移量决定最终停在哪个数字上 */
@keyframes m-roll {
  from { transform: translateY(0); }
  to { transform: translateY(-62%); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-digits">
          <span className="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
          <span className="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
          <span className="m-digit"><i>0<br />1<br />2<br />3<br />4<br />5<br />6<br />7<br />8<br />9</i></span>
          <span className="m-dot">%</span>
        </div>
        <p className="m-cap">本季度动效复用率</p>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-digits {
  display: flex;
  align-items: baseline;
  gap: 2px;
  font: 700 62px/1 ui-monospace, "Cascadia Mono", Consolas, monospace;
  color: #f0cd72;
}
.m-digit {
  display: block;
  height: 1em;
  overflow: hidden;
}
.m-digit i {
  display: block;
  font-style: normal;
  line-height: 1;
  animation: m-roll var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}
.m-digit:nth-child(1) i { animation-delay: calc(var(--m-stagger) * 0); }
.m-digit:nth-child(2) i { animation-delay: calc(var(--m-stagger) * 1); }
.m-digit:nth-child(3) i { animation-delay: calc(var(--m-stagger) * 2); }
.m-dot { font-size: 34px; color: rgba(240, 205, 114, 0.7); }
.m-cap {
  margin: 14px 0 0;
  font: 400 13px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.14em;
  color: rgba(244, 246, 248, 0.42);
}
/* 每一位都从 0 滚到目标位：用 transform 位移整列数字，位移量决定最终停在哪个数字上 */
@keyframes m-roll {
  from { transform: translateY(0); }
  to { transform: translateY(-62%); }
}
*/', '', '做一个数字翻滚计数器：每一位数字是一个高度 1em 的 overflow:hidden 容器，里面竖排 0-9（每行 line-height:1）；动画只改内部列的 translateY，从 0 到 -62%（即停在数字 6），每位延迟 0.08s 递增，时长 1.6s。', 'counter,dashboard,digits,community,raulriera__textfieldeffects', 'COMMUNITY', 'https://github.com/raulriera/TextFieldEffects', 'MIT', 'READY'),
	('tilt-glare-card', '3D 倾斜反光卡', 'Tilt Glare Card', '鼠标在卡片上移动时卡片做透视倾斜，并有一道高光跟着指针走——最像「实体卡片」的一种 hover。', '卡片交互', 'Portfolio', 'Glass', 'CSS', 3, '个人作品集,产品发布页,官网首页', 'hover', 93, 86, 84, 88, 88, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-tilt", "label": "最大倾角", "unit": "deg", "min": 4, "max": 22, "step": 1, "default": 12}, {"key": "--m-duration", "label": "回正时长", "unit": "s", "min": 0.2, "max": 1.2, "step": 0.05, "default": 0.5}]', '', '<div class="motion-root">
  <div class="m-card" id="tiltCard">
    <div class="m-glare"></div>
    <div class="m-card-body">
      <span class="m-tag">PREMIUM</span>
      <h3>倾斜反光卡</h3>
      <p>移动鼠标看透视与高光</p>
    </div>
  </div>
</div>', '(function () {
  var card = document.getElementById(''tiltCard'');
  if (!card) { return; }
  var maxTilt = 12;
  var style = getComputedStyle(document.documentElement);
  var configured = parseFloat(style.getPropertyValue(''--m-tilt''));
  if (!isNaN(configured)) { maxTilt = configured; }
  function move(event) {
    var box = card.getBoundingClientRect();
    var px = (event.clientX - box.left) / box.width;
    var py = (event.clientY - box.top) / box.height;
    card.style.transform = ''rotateY('' + ((px - 0.5) * maxTilt * 2).toFixed(2) + ''deg) rotateX('' +
      ((0.5 - py) * maxTilt * 2).toFixed(2) + ''deg) translateZ(6px)'';
    card.style.setProperty(''--m-gx'', (px * 100).toFixed(1) + ''%'');
    card.style.setProperty(''--m-gy'', (py * 100).toFixed(1) + ''%'');
  }
  card.addEventListener(''pointermove'', move);
  card.addEventListener(''pointerleave'', function () { card.style.transform = ''rotateY(0deg) rotateX(0deg)''; });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { perspective: 900px; }
.m-card {
  position: relative;
  width: 260px;
  padding: 22px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(150deg, rgba(240, 205, 114, 0.16), rgba(20, 22, 28, 0.9));
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
  transform-style: preserve-3d;
  transition: transform var(--m-duration) ease-out;
  overflow: hidden;
}
.m-glare {
  position: absolute;
  inset: 0;
  background: radial-gradient(240px circle at var(--m-gx, 50%) var(--m-gy, 0%), rgba(255, 255, 255, 0.28), transparent 62%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.m-card:hover .m-glare { opacity: 1; }
.m-card-body { position: relative; color: #f4f6f8; }
.m-tag {
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.18em;
  color: #f0cd72;
}
.m-card-body h3 { margin: 10px 0 6px; font: 600 20px/1.2 system-ui, sans-serif; }
.m-card-body p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }', '<template>
    <div class="motion-root">
      <div class="m-card" id="tiltCard">
        <div class="m-glare"></div>
        <div class="m-card-body">
          <span class="m-tag">PREMIUM</span>
          <h3>倾斜反光卡</h3>
          <p>移动鼠标看透视与高光</p>
        </div>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var card = document.getElementById(''tiltCard'');
    if (!card) { return; }
    var maxTilt = 12;
    var style = getComputedStyle(document.documentElement);
    var configured = parseFloat(style.getPropertyValue(''--m-tilt''));
    if (!isNaN(configured)) { maxTilt = configured; }
    function move(event) {
      var box = card.getBoundingClientRect();
      var px = (event.clientX - box.left) / box.width;
      var py = (event.clientY - box.top) / box.height;
      card.style.transform = ''rotateY('' + ((px - 0.5) * maxTilt * 2).toFixed(2) + ''deg) rotateX('' +
        ((0.5 - py) * maxTilt * 2).toFixed(2) + ''deg) translateZ(6px)'';
      card.style.setProperty(''--m-gx'', (px * 100).toFixed(1) + ''%'');
      card.style.setProperty(''--m-gy'', (py * 100).toFixed(1) + ''%'');
    }
    card.addEventListener(''pointermove'', move);
    card.addEventListener(''pointerleave'', function () { card.style.transform = ''rotateY(0deg) rotateX(0deg)''; });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { perspective: 900px; }
.m-card {
  position: relative;
  width: 260px;
  padding: 22px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(150deg, rgba(240, 205, 114, 0.16), rgba(20, 22, 28, 0.9));
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
  transform-style: preserve-3d;
  transition: transform var(--m-duration) ease-out;
  overflow: hidden;
}
.m-glare {
  position: absolute;
  inset: 0;
  background: radial-gradient(240px circle at var(--m-gx, 50%) var(--m-gy, 0%), rgba(255, 255, 255, 0.28), transparent 62%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.m-card:hover .m-glare { opacity: 1; }
.m-card-body { position: relative; color: #f4f6f8; }
.m-tag {
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.18em;
  color: #f0cd72;
}
.m-card-body h3 { margin: 10px 0 6px; font: 600 20px/1.2 system-ui, sans-serif; }
.m-card-body p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var card = document.getElementById(''tiltCard'');
      if (!card) { return; }
      var maxTilt = 12;
      var style = getComputedStyle(document.documentElement);
      var configured = parseFloat(style.getPropertyValue(''--m-tilt''));
      if (!isNaN(configured)) { maxTilt = configured; }
      function move(event) {
        var box = card.getBoundingClientRect();
        var px = (event.clientX - box.left) / box.width;
        var py = (event.clientY - box.top) / box.height;
        card.style.transform = ''rotateY('' + ((px - 0.5) * maxTilt * 2).toFixed(2) + ''deg) rotateX('' +
          ((0.5 - py) * maxTilt * 2).toFixed(2) + ''deg) translateZ(6px)'';
        card.style.setProperty(''--m-gx'', (px * 100).toFixed(1) + ''%'');
        card.style.setProperty(''--m-gy'', (py * 100).toFixed(1) + ''%'');
      }
      card.addEventListener(''pointermove'', move);
      card.addEventListener(''pointerleave'', function () { card.style.transform = ''rotateY(0deg) rotateX(0deg)''; });
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-card" id="tiltCard">
          <div className="m-glare"></div>
          <div className="m-card-body">
            <span className="m-tag">PREMIUM</span>
            <h3>倾斜反光卡</h3>
            <p>移动鼠标看透视与高光</p>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { perspective: 900px; }
.m-card {
  position: relative;
  width: 260px;
  padding: 22px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(150deg, rgba(240, 205, 114, 0.16), rgba(20, 22, 28, 0.9));
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
  transform-style: preserve-3d;
  transition: transform var(--m-duration) ease-out;
  overflow: hidden;
}
.m-glare {
  position: absolute;
  inset: 0;
  background: radial-gradient(240px circle at var(--m-gx, 50%) var(--m-gy, 0%), rgba(255, 255, 255, 0.28), transparent 62%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}
.m-card:hover .m-glare { opacity: 1; }
.m-card-body { position: relative; color: #f4f6f8; }
.m-tag {
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.18em;
  color: #f0cd72;
}
.m-card-body h3 { margin: 10px 0 6px; font: 600 20px/1.2 system-ui, sans-serif; }
.m-card-body p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
*/', '', '做一个 3D 倾斜反光卡：容器加 perspective:900px，卡片 transform-style:preserve-3d；pointermove 时按指针在卡片内的相对位置算 rotateX/rotateY（最大 ±12deg），并用径向渐变把高光位置同步到 --m-gx/--m-gy；指针离开时回正。', 'tilt,3d,hover,glare,community,ramotion__expanding-collection', 'COMMUNITY', 'https://github.com/Ramotion/expanding-collection', 'MIT', 'READY'),
	('spotlight-card', '光斑跟随卡', 'Spotlight Card', '指针在哪，卡片就在哪亮——一层聚光跟着指针走，边框同时被点亮，暗色界面里非常提神。', '卡片交互', 'AI SaaS', 'Cyber', 'CSS', 2, 'AI 产品页,数据后台,官网首页', 'hover', 91, 88, 90, 90, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-spot", "label": "光斑半径", "unit": "px", "min": 90, "max": 320, "step": 10, "default": 180}]', '', '<div class="motion-root">
  <div class="m-spot" id="spotCard">
    <div class="m-spot-inner">
      <h3>Spotlight</h3>
      <p>指针移到卡片上，看光斑跟随</p>
    </div>
  </div>
</div>', '(function () {
  var card = document.getElementById(''spotCard'');
  if (!card) { return; }
  card.addEventListener(''pointermove'', function (event) {
    var box = card.getBoundingClientRect();
    card.style.setProperty(''--m-x'', (((event.clientX - box.left) / box.width) * 100).toFixed(1) + ''%'');
    card.style.setProperty(''--m-y'', (((event.clientY - box.top) / box.height) * 100).toFixed(1) + ''%'');
  });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-spot {
  position: relative;
  width: 280px;
  padding: 1px;
  border-radius: 18px;
  background: radial-gradient(var(--m-spot) circle at var(--m-x, 50%) var(--m-y, 50%), rgba(94, 234, 212, 0.85), rgba(255, 255, 255, 0.07) 70%);
}
.m-spot-inner {
  position: relative;
  border-radius: 17px;
  padding: 22px;
  background: linear-gradient(160deg, rgba(16, 18, 24, 0.96), rgba(10, 11, 15, 0.99));
  color: #f4f6f8;
}
.m-spot-inner::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 17px;
  background: radial-gradient(var(--m-spot) circle at var(--m-x, 50%) var(--m-y, 50%), rgba(94, 234, 212, 0.16), transparent 68%);
  opacity: 0;
  transition: opacity 0.25s ease;
}
.m-spot:hover .m-spot-inner::before { opacity: 1; }
.m-spot-inner h3 { margin: 0 0 6px; font: 600 19px/1.2 system-ui, sans-serif; }
.m-spot-inner p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }', '<template>
    <div class="motion-root">
      <div class="m-spot" id="spotCard">
        <div class="m-spot-inner">
          <h3>Spotlight</h3>
          <p>指针移到卡片上，看光斑跟随</p>
        </div>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var card = document.getElementById(''spotCard'');
    if (!card) { return; }
    card.addEventListener(''pointermove'', function (event) {
      var box = card.getBoundingClientRect();
      card.style.setProperty(''--m-x'', (((event.clientX - box.left) / box.width) * 100).toFixed(1) + ''%'');
      card.style.setProperty(''--m-y'', (((event.clientY - box.top) / box.height) * 100).toFixed(1) + ''%'');
    });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-spot {
  position: relative;
  width: 280px;
  padding: 1px;
  border-radius: 18px;
  background: radial-gradient(var(--m-spot) circle at var(--m-x, 50%) var(--m-y, 50%), rgba(94, 234, 212, 0.85), rgba(255, 255, 255, 0.07) 70%);
}
.m-spot-inner {
  position: relative;
  border-radius: 17px;
  padding: 22px;
  background: linear-gradient(160deg, rgba(16, 18, 24, 0.96), rgba(10, 11, 15, 0.99));
  color: #f4f6f8;
}
.m-spot-inner::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 17px;
  background: radial-gradient(var(--m-spot) circle at var(--m-x, 50%) var(--m-y, 50%), rgba(94, 234, 212, 0.16), transparent 68%);
  opacity: 0;
  transition: opacity 0.25s ease;
}
.m-spot:hover .m-spot-inner::before { opacity: 1; }
.m-spot-inner h3 { margin: 0 0 6px; font: 600 19px/1.2 system-ui, sans-serif; }
.m-spot-inner p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var card = document.getElementById(''spotCard'');
      if (!card) { return; }
      card.addEventListener(''pointermove'', function (event) {
        var box = card.getBoundingClientRect();
        card.style.setProperty(''--m-x'', (((event.clientX - box.left) / box.width) * 100).toFixed(1) + ''%'');
        card.style.setProperty(''--m-y'', (((event.clientY - box.top) / box.height) * 100).toFixed(1) + ''%'');
      });
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-spot" id="spotCard">
          <div className="m-spot-inner">
            <h3>Spotlight</h3>
            <p>指针移到卡片上，看光斑跟随</p>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-spot {
  position: relative;
  width: 280px;
  padding: 1px;
  border-radius: 18px;
  background: radial-gradient(var(--m-spot) circle at var(--m-x, 50%) var(--m-y, 50%), rgba(94, 234, 212, 0.85), rgba(255, 255, 255, 0.07) 70%);
}
.m-spot-inner {
  position: relative;
  border-radius: 17px;
  padding: 22px;
  background: linear-gradient(160deg, rgba(16, 18, 24, 0.96), rgba(10, 11, 15, 0.99));
  color: #f4f6f8;
}
.m-spot-inner::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 17px;
  background: radial-gradient(var(--m-spot) circle at var(--m-x, 50%) var(--m-y, 50%), rgba(94, 234, 212, 0.16), transparent 68%);
  opacity: 0;
  transition: opacity 0.25s ease;
}
.m-spot:hover .m-spot-inner::before { opacity: 1; }
.m-spot-inner h3 { margin: 0 0 6px; font: 600 19px/1.2 system-ui, sans-serif; }
.m-spot-inner p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
*/', '', '做一个光斑跟随卡：外层用 radial-gradient 画一圈亮边（位置由 --m-x/--m-y 控制），内层是深色圆角面板；pointermove 时把指针在卡片内的百分比写进 CSS 变量，内层再用一个同位置的径向渐变做柔光。', 'spotlight,pointer,glow,community,pkissling__clock-weather-card', 'COMMUNITY', 'https://github.com/pkissling/clock-weather-card', 'MIT', 'READY'),
	('glass-lift-hover', '玻璃浮起', 'Glass Lift Hover', '毛玻璃卡片在悬停时轻轻抬起、上缘透出一道光，静态列表也能有层次。', '卡片交互', 'Portfolio', 'Glass', 'CSS', 1, '个人作品集,官网首页,数据后台', 'hover', 88, 94, 92, 90, 91, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "抬起时长", "unit": "s", "min": 0.16, "max": 0.8, "step": 0.02, "default": 0.34}, {"key": "--m-distance", "label": "抬起高度", "unit": "px", "min": 2, "max": 22, "step": 1, "default": 8}]', '', '<div class="motion-root">
  <article class="m-glass">
    <span class="m-glass-k">CASE 07</span>
    <h3>玻璃浮起</h3>
    <p>hover 抬起 · 上缘流光</p>
  </article>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-glass {
  position: relative;
  width: 250px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.03));
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 38px rgba(0, 0, 0, 0.42);
  color: #f4f6f8;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow var(--m-duration) ease, border-color var(--m-duration) ease;
}
.m-glass::before {
  content: "";
  position: absolute;
  left: 12%;
  right: 12%;
  top: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(240, 205, 114, 0.9), transparent);
  opacity: 0;
  transition: opacity var(--m-duration) ease;
}
.m-glass:hover {
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.4);
  box-shadow: 0 26px 54px rgba(0, 0, 0, 0.55);
}
.m-glass:hover::before { opacity: 1; }
.m-glass-k {
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.2em;
  color: rgba(240, 205, 114, 0.85);
}
.m-glass h3 { margin: 10px 0 6px; font: 600 19px/1.2 system-ui, sans-serif; }
.m-glass p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }', '<template>
    <div class="motion-root">
      <article class="m-glass">
        <span class="m-glass-k">CASE 07</span>
        <h3>玻璃浮起</h3>
        <p>hover 抬起 · 上缘流光</p>
      </article>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-glass {
  position: relative;
  width: 250px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.03));
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 38px rgba(0, 0, 0, 0.42);
  color: #f4f6f8;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow var(--m-duration) ease, border-color var(--m-duration) ease;
}
.m-glass::before {
  content: "";
  position: absolute;
  left: 12%;
  right: 12%;
  top: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(240, 205, 114, 0.9), transparent);
  opacity: 0;
  transition: opacity var(--m-duration) ease;
}
.m-glass:hover {
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.4);
  box-shadow: 0 26px 54px rgba(0, 0, 0, 0.55);
}
.m-glass:hover::before { opacity: 1; }
.m-glass-k {
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.2em;
  color: rgba(240, 205, 114, 0.85);
}
.m-glass h3 { margin: 10px 0 6px; font: 600 19px/1.2 system-ui, sans-serif; }
.m-glass p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <article className="m-glass">
          <span className="m-glass-k">CASE 07</span>
          <h3>玻璃浮起</h3>
          <p>hover 抬起 · 上缘流光</p>
        </article>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-glass {
  position: relative;
  width: 250px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.03));
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 38px rgba(0, 0, 0, 0.42);
  color: #f4f6f8;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1),
              box-shadow var(--m-duration) ease, border-color var(--m-duration) ease;
}
.m-glass::before {
  content: "";
  position: absolute;
  left: 12%;
  right: 12%;
  top: -1px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(240, 205, 114, 0.9), transparent);
  opacity: 0;
  transition: opacity var(--m-duration) ease;
}
.m-glass:hover {
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.4);
  box-shadow: 0 26px 54px rgba(0, 0, 0, 0.55);
}
.m-glass:hover::before { opacity: 1; }
.m-glass-k {
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.2em;
  color: rgba(240, 205, 114, 0.85);
}
.m-glass h3 { margin: 10px 0 6px; font: 600 19px/1.2 system-ui, sans-serif; }
.m-glass p { margin: 0; font: 400 12.5px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
*/', '', '做一个玻璃卡片悬停效果：backdrop-filter:blur(14px) + 半透明白底；hover 时 translateY(-8px)、阴影加深、边框变成金色半透明，并用伪元素在卡片上缘画一道 1px 的横向流光（两端透明、中间金色）。', 'glass,hover,lift,community,ianlunn__hover', 'COMMUNITY', 'https://github.com/IanLunn/Hover', 'NOASSERTION', 'READY'),
	('flip-reveal-card', '翻面揭示', 'Flip Reveal Card', '悬停时卡片绕 Y 轴翻到背面，正面是封面、背面是说明——一张卡装两层信息。', '卡片交互', 'Portfolio', 'Minimal', 'CSS', 2, '个人作品集,产品发布页,官网首页', 'hover', 89, 92, 88, 92, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "翻面时长", "unit": "s", "min": 0.3, "max": 1.4, "step": 0.05, "default": 0.7}]', '', '<div class="motion-root">
  <div class="m-flip">
    <div class="m-flip-face m-flip-front">
      <h3>Front</h3>
      <p>悬停翻面</p>
    </div>
    <div class="m-flip-face m-flip-back">
      <h3>Back</h3>
      <p>背面放说明、指标或链接</p>
    </div>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { perspective: 1000px; }
.m-flip {
  position: relative;
  width: 240px;
  height: 150px;
  transform-style: preserve-3d;
  transition: transform var(--m-duration) cubic-bezier(0.4, 0.02, 0.2, 1);
}
.m-flip:hover { transform: rotateY(180deg); }
.m-flip-face {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: 16px;
  padding: 18px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.12);
}
.m-flip-front { background: linear-gradient(150deg, rgba(240, 205, 114, 0.9), rgba(125, 20, 24, 0.85)); color: #12060a; }
.m-flip-back { background: linear-gradient(150deg, rgba(20, 22, 28, 0.98), rgba(12, 13, 17, 0.98)); color: #f4f6f8; transform: rotateY(180deg); }
.m-flip-face h3 { margin: 0 0 6px; font: 700 20px/1.2 system-ui, sans-serif; }
.m-flip-face p { margin: 0; font: 400 12px/1.6 system-ui, sans-serif; opacity: 0.72; }', '<template>
    <div class="motion-root">
      <div class="m-flip">
        <div class="m-flip-face m-flip-front">
          <h3>Front</h3>
          <p>悬停翻面</p>
        </div>
        <div class="m-flip-face m-flip-back">
          <h3>Back</h3>
          <p>背面放说明、指标或链接</p>
        </div>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { perspective: 1000px; }
.m-flip {
  position: relative;
  width: 240px;
  height: 150px;
  transform-style: preserve-3d;
  transition: transform var(--m-duration) cubic-bezier(0.4, 0.02, 0.2, 1);
}
.m-flip:hover { transform: rotateY(180deg); }
.m-flip-face {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: 16px;
  padding: 18px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.12);
}
.m-flip-front { background: linear-gradient(150deg, rgba(240, 205, 114, 0.9), rgba(125, 20, 24, 0.85)); color: #12060a; }
.m-flip-back { background: linear-gradient(150deg, rgba(20, 22, 28, 0.98), rgba(12, 13, 17, 0.98)); color: #f4f6f8; transform: rotateY(180deg); }
.m-flip-face h3 { margin: 0 0 6px; font: 700 20px/1.2 system-ui, sans-serif; }
.m-flip-face p { margin: 0; font: 400 12px/1.6 system-ui, sans-serif; opacity: 0.72; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-flip">
          <div className="m-flip-face m-flip-front">
            <h3>Front</h3>
            <p>悬停翻面</p>
          </div>
          <div className="m-flip-face m-flip-back">
            <h3>Back</h3>
            <p>背面放说明、指标或链接</p>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { perspective: 1000px; }
.m-flip {
  position: relative;
  width: 240px;
  height: 150px;
  transform-style: preserve-3d;
  transition: transform var(--m-duration) cubic-bezier(0.4, 0.02, 0.2, 1);
}
.m-flip:hover { transform: rotateY(180deg); }
.m-flip-face {
  position: absolute;
  inset: 0;
  backface-visibility: hidden;
  border-radius: 16px;
  padding: 18px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.12);
}
.m-flip-front { background: linear-gradient(150deg, rgba(240, 205, 114, 0.9), rgba(125, 20, 24, 0.85)); color: #12060a; }
.m-flip-back { background: linear-gradient(150deg, rgba(20, 22, 28, 0.98), rgba(12, 13, 17, 0.98)); color: #f4f6f8; transform: rotateY(180deg); }
.m-flip-face h3 { margin: 0 0 6px; font: 700 20px/1.2 system-ui, sans-serif; }
.m-flip-face p { margin: 0; font: 400 12px/1.6 system-ui, sans-serif; opacity: 0.72; }
*/', '', '做一张悬停翻面的卡片：容器 perspective:1000px，卡片 transform-style:preserve-3d 并在 hover 时 rotateY(180deg)；正反两面都用 backface-visibility:hidden 叠在一起，背面预先 rotateY(180deg)；时长 0.7s。', 'flip,3d,card,community,chenyilong__cyltabbarcontroller', 'COMMUNITY', 'https://github.com/ChenYilong/CYLTabBarController', 'MIT', 'READY'),
	('press-depth-card', '按压回弹', 'Press Depth', '按下时卡片变矮、阴影收紧，松手回弹——像一个真的被按下去的键帽，适合列表项与工具卡。', '卡片交互', 'Game UI', 'Cyber', 'CSS', 1, '游戏界面,数据后台,AI 产品页', 'click', 86, 93, 91, 95, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-depth", "label": "按下深度", "unit": "px", "min": 2, "max": 16, "step": 1, "default": 6}, {"key": "--m-duration", "label": "回弹时长", "unit": "s", "min": 0.1, "max": 0.6, "step": 0.02, "default": 0.24}]', '', '<div class="motion-root">
  <div class="m-key" id="pressCard">
    <span class="m-key-label">PRESS</span>
    <span class="m-key-hint">按住看看</span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-key {
  width: 200px;
  padding: 20px 22px;
  border-radius: 16px;
  border: 1px solid rgba(94, 234, 212, 0.35);
  background: linear-gradient(170deg, rgba(20, 24, 30, 0.98), rgba(12, 14, 18, 0.98));
  color: #e8f7f2;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 var(--m-depth) 0 rgba(94, 234, 212, 0.22), 0 18px 34px rgba(0, 0, 0, 0.5);
  transition: transform var(--m-duration) ease-out, box-shadow var(--m-duration) ease-out;
}
.m-key:active {
  transform: translateY(var(--m-depth));
  box-shadow: 0 0 0 rgba(94, 234, 212, 0.2), 0 6px 14px rgba(0, 0, 0, 0.45);
}
.m-key-label { display: block; font: 700 15px/1 ui-monospace, monospace; letter-spacing: 0.22em; }
.m-key-hint { display: block; margin-top: 8px; font: 400 12px/1.6 system-ui, sans-serif; color: rgba(232, 247, 242, 0.5); }', '<template>
    <div class="motion-root">
      <div class="m-key" id="pressCard">
        <span class="m-key-label">PRESS</span>
        <span class="m-key-hint">按住看看</span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-key {
  width: 200px;
  padding: 20px 22px;
  border-radius: 16px;
  border: 1px solid rgba(94, 234, 212, 0.35);
  background: linear-gradient(170deg, rgba(20, 24, 30, 0.98), rgba(12, 14, 18, 0.98));
  color: #e8f7f2;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 var(--m-depth) 0 rgba(94, 234, 212, 0.22), 0 18px 34px rgba(0, 0, 0, 0.5);
  transition: transform var(--m-duration) ease-out, box-shadow var(--m-duration) ease-out;
}
.m-key:active {
  transform: translateY(var(--m-depth));
  box-shadow: 0 0 0 rgba(94, 234, 212, 0.2), 0 6px 14px rgba(0, 0, 0, 0.45);
}
.m-key-label { display: block; font: 700 15px/1 ui-monospace, monospace; letter-spacing: 0.22em; }
.m-key-hint { display: block; margin-top: 8px; font: 400 12px/1.6 system-ui, sans-serif; color: rgba(232, 247, 242, 0.5); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-key" id="pressCard">
          <span className="m-key-label">PRESS</span>
          <span className="m-key-hint">按住看看</span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-key {
  width: 200px;
  padding: 20px 22px;
  border-radius: 16px;
  border: 1px solid rgba(94, 234, 212, 0.35);
  background: linear-gradient(170deg, rgba(20, 24, 30, 0.98), rgba(12, 14, 18, 0.98));
  color: #e8f7f2;
  cursor: pointer;
  user-select: none;
  box-shadow: 0 var(--m-depth) 0 rgba(94, 234, 212, 0.22), 0 18px 34px rgba(0, 0, 0, 0.5);
  transition: transform var(--m-duration) ease-out, box-shadow var(--m-duration) ease-out;
}
.m-key:active {
  transform: translateY(var(--m-depth));
  box-shadow: 0 0 0 rgba(94, 234, 212, 0.2), 0 6px 14px rgba(0, 0, 0, 0.45);
}
.m-key-label { display: block; font: 700 15px/1 ui-monospace, monospace; letter-spacing: 0.22em; }
.m-key-hint { display: block; margin-top: 8px; font: 400 12px/1.6 system-ui, sans-serif; color: rgba(232, 247, 242, 0.5); }
*/', '', '做一个按压回弹的卡片：常态用 box-shadow 画出一条 6px 的「厚度」，:active 时 translateY(6px) 并把厚度收到 0、阴影收紧，transition 0.24s ease-out；松手自动回弹。', 'press,keycap,active,community,nolimits4web__atropos', 'COMMUNITY', 'https://github.com/nolimits4web/atropos', 'MIT', 'READY'),
	('magnetic-pull-btn', '磁吸吸附按钮', 'Magnetic Pull Button', '指针靠近时按钮被「吸」过去一小段距离，离开后弹回——页面上的按钮因此有了物理感。', '按钮交互', 'Landing Page', 'Luxury', 'CSS', 2, '官网首页,产品发布页,AI 产品页', 'hover', 92, 88, 90, 92, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-pull", "label": "吸附强度", "unit": "", "min": 0.1, "max": 0.6, "step": 0.05, "default": 0.32}, {"key": "--m-duration", "label": "回弹时长", "unit": "s", "min": 0.2, "max": 0.9, "step": 0.05, "default": 0.45}]', '', '<div class="motion-root">
  <div class="m-zone" id="magZone">
    <button class="m-magnet" type="button">开始创作</button>
  </div>
</div>', '(function () {
  var zone = document.getElementById(''magZone'');
  var button = zone ? zone.querySelector(''.m-magnet'') : null;
  if (!zone || !button) { return; }
  var strength = 0.32;
  var configured = parseFloat(getComputedStyle(document.documentElement).getPropertyValue(''--m-pull''));
  if (!isNaN(configured)) { strength = configured; }
  zone.addEventListener(''pointermove'', function (event) {
    var box = button.getBoundingClientRect();
    var dx = event.clientX - (box.left + box.width / 2);
    var dy = event.clientY - (box.top + box.height / 2);
    button.style.transform = ''translate('' + (dx * strength).toFixed(1) + ''px,'' + (dy * strength).toFixed(1) + ''px)'';
  });
  zone.addEventListener(''pointerleave'', function () { button.style.transform = ''translate(0,0)''; });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-zone { display: grid; place-items: center; width: 320px; height: 190px; }
.m-magnet {
  padding: 14px 30px;
  border: 0;
  border-radius: 999px;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.08em;
  color: #12060a;
  background: linear-gradient(120deg, #f0cd72, #ffb37a);
  box-shadow: 0 14px 30px rgba(240, 205, 114, 0.28);
  cursor: pointer;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1);
}
.m-magnet:hover { box-shadow: 0 18px 40px rgba(240, 205, 114, 0.4); }', '<template>
    <div class="motion-root">
      <div class="m-zone" id="magZone">
        <button class="m-magnet" type="button">开始创作</button>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var zone = document.getElementById(''magZone'');
    var button = zone ? zone.querySelector(''.m-magnet'') : null;
    if (!zone || !button) { return; }
    var strength = 0.32;
    var configured = parseFloat(getComputedStyle(document.documentElement).getPropertyValue(''--m-pull''));
    if (!isNaN(configured)) { strength = configured; }
    zone.addEventListener(''pointermove'', function (event) {
      var box = button.getBoundingClientRect();
      var dx = event.clientX - (box.left + box.width / 2);
      var dy = event.clientY - (box.top + box.height / 2);
      button.style.transform = ''translate('' + (dx * strength).toFixed(1) + ''px,'' + (dy * strength).toFixed(1) + ''px)'';
    });
    zone.addEventListener(''pointerleave'', function () { button.style.transform = ''translate(0,0)''; });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-zone { display: grid; place-items: center; width: 320px; height: 190px; }
.m-magnet {
  padding: 14px 30px;
  border: 0;
  border-radius: 999px;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.08em;
  color: #12060a;
  background: linear-gradient(120deg, #f0cd72, #ffb37a);
  box-shadow: 0 14px 30px rgba(240, 205, 114, 0.28);
  cursor: pointer;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1);
}
.m-magnet:hover { box-shadow: 0 18px 40px rgba(240, 205, 114, 0.4); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var zone = document.getElementById(''magZone'');
      var button = zone ? zone.querySelector(''.m-magnet'') : null;
      if (!zone || !button) { return; }
      var strength = 0.32;
      var configured = parseFloat(getComputedStyle(document.documentElement).getPropertyValue(''--m-pull''));
      if (!isNaN(configured)) { strength = configured; }
      zone.addEventListener(''pointermove'', function (event) {
        var box = button.getBoundingClientRect();
        var dx = event.clientX - (box.left + box.width / 2);
        var dy = event.clientY - (box.top + box.height / 2);
        button.style.transform = ''translate('' + (dx * strength).toFixed(1) + ''px,'' + (dy * strength).toFixed(1) + ''px)'';
      });
      zone.addEventListener(''pointerleave'', function () { button.style.transform = ''translate(0,0)''; });
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-zone" id="magZone">
          <button className="m-magnet" type="button">开始创作</button>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-zone { display: grid; place-items: center; width: 320px; height: 190px; }
.m-magnet {
  padding: 14px 30px;
  border: 0;
  border-radius: 999px;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.08em;
  color: #12060a;
  background: linear-gradient(120deg, #f0cd72, #ffb37a);
  box-shadow: 0 14px 30px rgba(240, 205, 114, 0.28);
  cursor: pointer;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1);
}
.m-magnet:hover { box-shadow: 0 18px 40px rgba(240, 205, 114, 0.4); }
*/', '', '做一个磁吸按钮：外层放一个比按钮大的感应区，pointermove 时算指针相对按钮中心的位移，按钮按 0.32 的系数跟随平移；指针离开感应区时回正，transition 0.45s 带一点弹性。', 'magnetic,cursor,cta,community,codrops__magneticbuttons', 'COMMUNITY', 'https://github.com/codrops/MagneticButtons', 'MIT', 'READY'),
	('ripple-click-btn', '水波点击按钮', 'Ripple Click Button', '点击位置扩散出一圈水波——把「点到了」这件事画出来，最基础的触感反馈。', '按钮交互', 'Dashboard', 'Cyber', 'CSS', 2, '数据后台,AI 产品页,登录页面', 'click', 85, 90, 92, 93, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "扩散时长", "unit": "s", "min": 0.3, "max": 1.4, "step": 0.05, "default": 0.62}]', '', '<div class="motion-root">
  <button class="m-ripple" id="rippleBtn" type="button">点击试试</button>
</div>', '(function () {
  var button = document.getElementById(''rippleBtn'');
  if (!button) { return; }
  button.addEventListener(''pointerdown'', function (event) {
    var box = button.getBoundingClientRect();
    var size = Math.max(box.width, box.height) * 2;
    var wave = document.createElement(''span'');
    wave.className = ''m-wave'';
    wave.style.width = size + ''px'';
    wave.style.height = size + ''px'';
    wave.style.left = (event.clientX - box.left - size / 2) + ''px'';
    wave.style.top = (event.clientY - box.top - size / 2) + ''px'';
    button.appendChild(wave);
    // 动画结束后自己摘掉，避免 DOM 越堆越多
    wave.addEventListener(''animationend'', function () { wave.remove(); });
  });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-ripple {
  position: relative;
  overflow: hidden;
  padding: 14px 32px;
  border: 1px solid rgba(94, 234, 212, 0.5);
  border-radius: 12px;
  background: rgba(94, 234, 212, 0.1);
  color: #e8f7f2;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  cursor: pointer;
}
.m-ripple .m-wave {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(94, 234, 212, 0.75), rgba(94, 234, 212, 0));
  transform: scale(0);
  animation: m-wave var(--m-duration) ease-out forwards;
  pointer-events: none;
}
@keyframes m-wave {
  to { transform: scale(1); opacity: 0; }
}', '<template>
    <div class="motion-root">
      <button class="m-ripple" id="rippleBtn" type="button">点击试试</button>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var button = document.getElementById(''rippleBtn'');
    if (!button) { return; }
    button.addEventListener(''pointerdown'', function (event) {
      var box = button.getBoundingClientRect();
      var size = Math.max(box.width, box.height) * 2;
      var wave = document.createElement(''span'');
      wave.className = ''m-wave'';
      wave.style.width = size + ''px'';
      wave.style.height = size + ''px'';
      wave.style.left = (event.clientX - box.left - size / 2) + ''px'';
      wave.style.top = (event.clientY - box.top - size / 2) + ''px'';
      button.appendChild(wave);
      // 动画结束后自己摘掉，避免 DOM 越堆越多
      wave.addEventListener(''animationend'', function () { wave.remove(); });
    });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-ripple {
  position: relative;
  overflow: hidden;
  padding: 14px 32px;
  border: 1px solid rgba(94, 234, 212, 0.5);
  border-radius: 12px;
  background: rgba(94, 234, 212, 0.1);
  color: #e8f7f2;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  cursor: pointer;
}
.m-ripple .m-wave {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(94, 234, 212, 0.75), rgba(94, 234, 212, 0));
  transform: scale(0);
  animation: m-wave var(--m-duration) ease-out forwards;
  pointer-events: none;
}
@keyframes m-wave {
  to { transform: scale(1); opacity: 0; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var button = document.getElementById(''rippleBtn'');
      if (!button) { return; }
      button.addEventListener(''pointerdown'', function (event) {
        var box = button.getBoundingClientRect();
        var size = Math.max(box.width, box.height) * 2;
        var wave = document.createElement(''span'');
        wave.className = ''m-wave'';
        wave.style.width = size + ''px'';
        wave.style.height = size + ''px'';
        wave.style.left = (event.clientX - box.left - size / 2) + ''px'';
        wave.style.top = (event.clientY - box.top - size / 2) + ''px'';
        button.appendChild(wave);
        // 动画结束后自己摘掉，避免 DOM 越堆越多
        wave.addEventListener(''animationend'', function () { wave.remove(); });
      });
    })();
  }, [])

  return (
      <div className="motion-root">
        <button className="m-ripple" id="rippleBtn" type="button">点击试试</button>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-ripple {
  position: relative;
  overflow: hidden;
  padding: 14px 32px;
  border: 1px solid rgba(94, 234, 212, 0.5);
  border-radius: 12px;
  background: rgba(94, 234, 212, 0.1);
  color: #e8f7f2;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  cursor: pointer;
}
.m-ripple .m-wave {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(94, 234, 212, 0.75), rgba(94, 234, 212, 0));
  transform: scale(0);
  animation: m-wave var(--m-duration) ease-out forwards;
  pointer-events: none;
}
@keyframes m-wave {
  to { transform: scale(1); opacity: 0; }
}
*/', '', '做点击水波：按钮 overflow:hidden，pointerdown 时在点击位置插入一个圆形 span（直径=按钮长边的两倍），从 scale(0) 动画到 scale(1) 同时 opacity 归零，0.62s ease-out，animationend 后移除节点。', 'ripple,click,feedback,community,ganapativs__bttn.css', 'COMMUNITY', 'https://github.com/ganapativs/bttn.css', 'MIT', 'READY'),
	('glow-pulse-cta', '呼吸发光 CTA', 'Glow Pulse CTA', '主按钮外圈有一层缓慢呼吸的光晕，视线很难不停在它上面——用于「只有一个主按钮」的页面。', '按钮交互', 'AI SaaS', 'Cyber', 'CSS', 1, 'AI 产品页,官网首页,产品发布页', 'load', 88, 93, 91, 92, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-speed", "label": "呼吸周期", "unit": "s", "min": 1.2, "max": 5, "step": 0.1, "default": 2.6}, {"key": "--m-glow", "label": "光晕大小", "unit": "px", "min": 6, "max": 40, "step": 2, "default": 22}]', '', '<div class="motion-root">
  <button class="m-cta" type="button">免费开始</button>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-cta {
  position: relative;
  padding: 15px 34px;
  border: 0;
  border-radius: 999px;
  font: 600 14.5px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  color: #12060a;
  background: linear-gradient(120deg, #f0cd72, #ffb37a);
  cursor: pointer;
}
.m-cta::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  box-shadow: 0 0 0 0 rgba(240, 205, 114, 0.55);
  animation: m-glow var(--m-speed) ease-in-out infinite;
  pointer-events: none;
}
@keyframes m-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(240, 205, 114, 0.5), 0 0 var(--m-glow) rgba(240, 205, 114, 0.28); }
  50% { box-shadow: 0 0 0 10px rgba(240, 205, 114, 0), 0 0 calc(var(--m-glow) * 1.8) rgba(240, 205, 114, 0.5); }
}', '<template>
    <div class="motion-root">
      <button class="m-cta" type="button">免费开始</button>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-cta {
  position: relative;
  padding: 15px 34px;
  border: 0;
  border-radius: 999px;
  font: 600 14.5px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  color: #12060a;
  background: linear-gradient(120deg, #f0cd72, #ffb37a);
  cursor: pointer;
}
.m-cta::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  box-shadow: 0 0 0 0 rgba(240, 205, 114, 0.55);
  animation: m-glow var(--m-speed) ease-in-out infinite;
  pointer-events: none;
}
@keyframes m-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(240, 205, 114, 0.5), 0 0 var(--m-glow) rgba(240, 205, 114, 0.28); }
  50% { box-shadow: 0 0 0 10px rgba(240, 205, 114, 0), 0 0 calc(var(--m-glow) * 1.8) rgba(240, 205, 114, 0.5); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <button className="m-cta" type="button">免费开始</button>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-cta {
  position: relative;
  padding: 15px 34px;
  border: 0;
  border-radius: 999px;
  font: 600 14.5px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  color: #12060a;
  background: linear-gradient(120deg, #f0cd72, #ffb37a);
  cursor: pointer;
}
.m-cta::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: inherit;
  box-shadow: 0 0 0 0 rgba(240, 205, 114, 0.55);
  animation: m-glow var(--m-speed) ease-in-out infinite;
  pointer-events: none;
}
@keyframes m-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(240, 205, 114, 0.5), 0 0 var(--m-glow) rgba(240, 205, 114, 0.28); }
  50% { box-shadow: 0 0 0 10px rgba(240, 205, 114, 0), 0 0 calc(var(--m-glow) * 1.8) rgba(240, 205, 114, 0.5); }
}
*/', '', '做呼吸发光的 CTA 按钮：用伪元素做光晕，keyframes 在 0%/50%/100% 之间交替两层 box-shadow（一层向外扩 10px 并淡出、一层模糊半径放大到 1.8 倍），2.6s ease-in-out 无限循环。', 'glow,pulse,cta,community,sahaj-b__ghostty-cursor-shaders', 'COMMUNITY', 'https://github.com/sahaj-b/ghostty-cursor-shaders', 'MIT', 'READY'),
	('arrow-swipe-btn', '箭头滑出按钮', 'Arrow Swipe Button', '悬停时箭头从按钮左侧滑入、文字右移让位——经典但不油腻的「去看看」按钮。', '按钮交互', 'Landing Page', 'Minimal', 'CSS', 1, '官网首页,产品发布页,个人作品集', 'hover', 84, 94, 93, 95, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "滑动时长", "unit": "s", "min": 0.18, "max": 0.8, "step": 0.02, "default": 0.36}, {"key": "--m-gap", "label": "让位距离", "unit": "px", "min": 4, "max": 24, "step": 1, "default": 12}]', '', '<div class="motion-root">
  <button class="m-arrow" type="button">
    <span class="m-arrow-icon">→</span>
    <span class="m-arrow-text">查看案例</span>
  </button>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-arrow {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 13px 28px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: transparent;
  color: #f4f6f8;
  font: 500 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.04em;
  cursor: pointer;
  overflow: hidden;
  transition: border-color var(--m-duration) ease, background var(--m-duration) ease;
}
.m-arrow:hover { border-color: rgba(240, 205, 114, 0.6); background: rgba(240, 205, 114, 0.08); }
.m-arrow-icon {
  display: inline-block;
  transform: translateX(-10px);
  opacity: 0;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1), opacity var(--m-duration) ease;
}
.m-arrow:hover .m-arrow-icon { transform: translateX(0); opacity: 1; }
.m-arrow-text { transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1); }
.m-arrow:hover .m-arrow-text { transform: translateX(var(--m-gap)); }', '<template>
    <div class="motion-root">
      <button class="m-arrow" type="button">
        <span class="m-arrow-icon">→</span>
        <span class="m-arrow-text">查看案例</span>
      </button>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-arrow {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 13px 28px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: transparent;
  color: #f4f6f8;
  font: 500 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.04em;
  cursor: pointer;
  overflow: hidden;
  transition: border-color var(--m-duration) ease, background var(--m-duration) ease;
}
.m-arrow:hover { border-color: rgba(240, 205, 114, 0.6); background: rgba(240, 205, 114, 0.08); }
.m-arrow-icon {
  display: inline-block;
  transform: translateX(-10px);
  opacity: 0;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1), opacity var(--m-duration) ease;
}
.m-arrow:hover .m-arrow-icon { transform: translateX(0); opacity: 1; }
.m-arrow-text { transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1); }
.m-arrow:hover .m-arrow-text { transform: translateX(var(--m-gap)); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <button className="m-arrow" type="button">
          <span className="m-arrow-icon">→</span>
          <span className="m-arrow-text">查看案例</span>
        </button>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-arrow {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 13px 28px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  background: transparent;
  color: #f4f6f8;
  font: 500 14px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.04em;
  cursor: pointer;
  overflow: hidden;
  transition: border-color var(--m-duration) ease, background var(--m-duration) ease;
}
.m-arrow:hover { border-color: rgba(240, 205, 114, 0.6); background: rgba(240, 205, 114, 0.08); }
.m-arrow-icon {
  display: inline-block;
  transform: translateX(-10px);
  opacity: 0;
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1), opacity var(--m-duration) ease;
}
.m-arrow:hover .m-arrow-icon { transform: translateX(0); opacity: 1; }
.m-arrow-text { transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1); }
.m-arrow:hover .m-arrow-text { transform: translateX(var(--m-gap)); }
*/', '', '做箭头滑出的按钮：箭头初始 translateX(-10px) 且 opacity:0，hover 时回到 0 并显现；文字同时向右平移 12px 让位；两者都是 0.36s cubic-bezier(0.22,0.61,0.36,1)。', 'arrow,hover,cta,community,alexwolfe__buttons', 'COMMUNITY', 'https://github.com/alexwolfe/Buttons', 'NOASSERTION', 'READY'),
	('scroll-progress-bar', '顶部阅读进度条', 'Scroll Progress Bar', '页面顶端的细进度条随滚动增长——长文与文档站最实用的一条动效。', '滚动动画', 'Dashboard', 'Minimal', 'CSS', 1, '数据后台,个人作品集,官网首页', 'scroll', 82, 92, 94, 96, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-thickness", "label": "条高", "unit": "px", "min": 2, "max": 8, "step": 1, "default": 3}]', '', '<div class="motion-root">
  <div class="m-track" id="progressTrack">
    <div class="m-bar" id="progressBar"></div>
  </div>
  <div class="m-panel">
    <p>向下滚动看看进度条</p>
    <span class="m-filler"></span>
  </div>
</div>', '(function () {
  var panel = document.querySelector(''.m-panel'');
  var bar = document.getElementById(''progressBar'');
  if (!panel || !bar) { return; }
  panel.addEventListener(''scroll'', function () {
    var max = panel.scrollHeight - panel.clientHeight;
    var ratio = max > 0 ? (panel.scrollTop / max) * 100 : 0;
    bar.style.width = ratio.toFixed(1) + ''%'';
  });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { display: flex; flex-direction: column; align-items: stretch; gap: 12px; padding: 16px; }
.m-track {
  width: 100%;
  height: var(--m-thickness);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  overflow: hidden;
}
.m-bar {
  width: 0%;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f0cd72, #5eead4);
  transition: width 0.1s linear;
}
.m-panel {
  flex: 1;
  overflow-y: auto;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 14px;
  color: rgba(244, 246, 248, 0.65);
  font: 400 13px/1.9 system-ui, "PingFang SC", sans-serif;
}
.m-filler { display: block; height: 420px; }', '<template>
    <div class="motion-root">
      <div class="m-track" id="progressTrack">
        <div class="m-bar" id="progressBar"></div>
      </div>
      <div class="m-panel">
        <p>向下滚动看看进度条</p>
        <span class="m-filler"></span>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var panel = document.querySelector(''.m-panel'');
    var bar = document.getElementById(''progressBar'');
    if (!panel || !bar) { return; }
    panel.addEventListener(''scroll'', function () {
      var max = panel.scrollHeight - panel.clientHeight;
      var ratio = max > 0 ? (panel.scrollTop / max) * 100 : 0;
      bar.style.width = ratio.toFixed(1) + ''%'';
    });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { display: flex; flex-direction: column; align-items: stretch; gap: 12px; padding: 16px; }
.m-track {
  width: 100%;
  height: var(--m-thickness);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  overflow: hidden;
}
.m-bar {
  width: 0%;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f0cd72, #5eead4);
  transition: width 0.1s linear;
}
.m-panel {
  flex: 1;
  overflow-y: auto;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 14px;
  color: rgba(244, 246, 248, 0.65);
  font: 400 13px/1.9 system-ui, "PingFang SC", sans-serif;
}
.m-filler { display: block; height: 420px; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var panel = document.querySelector(''.m-panel'');
      var bar = document.getElementById(''progressBar'');
      if (!panel || !bar) { return; }
      panel.addEventListener(''scroll'', function () {
        var max = panel.scrollHeight - panel.clientHeight;
        var ratio = max > 0 ? (panel.scrollTop / max) * 100 : 0;
        bar.style.width = ratio.toFixed(1) + ''%'';
      });
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-track" id="progressTrack">
          <div className="m-bar" id="progressBar"></div>
        </div>
        <div className="m-panel">
          <p>向下滚动看看进度条</p>
          <span className="m-filler"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { display: flex; flex-direction: column; align-items: stretch; gap: 12px; padding: 16px; }
.m-track {
  width: 100%;
  height: var(--m-thickness);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  overflow: hidden;
}
.m-bar {
  width: 0%;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #f0cd72, #5eead4);
  transition: width 0.1s linear;
}
.m-panel {
  flex: 1;
  overflow-y: auto;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 14px;
  color: rgba(244, 246, 248, 0.65);
  font: 400 13px/1.9 system-ui, "PingFang SC", sans-serif;
}
.m-filler { display: block; height: 420px; }
*/', '', '做顶部阅读进度条：外层是高度 3px 的圆角轨道，内层宽度百分比随滚动容器 scrollTop/(scrollHeight-clientHeight) 更新，width 用 0.1s linear 过渡（滚动时更顺），渐变从金色到青色。', 'progress,scroll,reading,community,meliorence__react-native-snap-carousel', 'COMMUNITY', 'https://github.com/meliorence/react-native-snap-carousel', 'BSD-3-Clause', 'READY'),
	('sticky-pin-panel', '钉住分屏', 'Sticky Pin Panel', '左栏说明文字随滚动切换，右侧面板钉在原地——讲「三步走」这类结构时非常好用。', '滚动动画', 'Landing Page', 'Minimal', 'CSS', 2, '官网首页,产品发布页,数据后台', 'scroll', 89, 90, 90, 94, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-gap", "label": "段落间距", "unit": "px", "min": 40, "max": 220, "step": 10, "default": 140}]', '', '<div class="motion-root">
  <div class="m-pin-wrap">
    <div class="m-pin-left">
      <section class="m-pin-item"><b>01</b><p>描述需求，助手给出方案</p></section>
      <section class="m-pin-item"><b>02</b><p>实时预览，参数随手可调</p></section>
      <section class="m-pin-item"><b>03</b><p>导出 Vue / React / CSS</p></section>
    </div>
    <div class="m-pin-right">
      <div class="m-pin-stage">STAGE</div>
    </div>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-pin-wrap {
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 18px;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 18px;
  box-sizing: border-box;
}
.m-pin-left { display: flex; flex-direction: column; }
.m-pin-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 0;
  margin-bottom: var(--m-gap);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(244, 246, 248, 0.72);
  font: 400 13px/1.8 system-ui, "PingFang SC", sans-serif;
}
.m-pin-item b { color: #f0cd72; font: 700 13px/1.8 ui-monospace, monospace; }
.m-pin-item p { margin: 0; }
.m-pin-right { position: relative; }
/* 关键：右侧面板 position:sticky 钉在可视区，滚动时它不动，左侧内容继续走 */
.m-pin-stage {
  position: sticky;
  top: 0;
  height: 190px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  border: 1px solid rgba(240, 205, 114, 0.28);
  background: linear-gradient(150deg, rgba(240, 205, 114, 0.14), rgba(12, 13, 17, 0.7));
  color: rgba(240, 205, 114, 0.85);
  font: 700 13px/1 ui-monospace, monospace;
  letter-spacing: 0.3em;
}', '<template>
    <div class="motion-root">
      <div class="m-pin-wrap">
        <div class="m-pin-left">
          <section class="m-pin-item"><b>01</b><p>描述需求，助手给出方案</p></section>
          <section class="m-pin-item"><b>02</b><p>实时预览，参数随手可调</p></section>
          <section class="m-pin-item"><b>03</b><p>导出 Vue / React / CSS</p></section>
        </div>
        <div class="m-pin-right">
          <div class="m-pin-stage">STAGE</div>
        </div>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-pin-wrap {
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 18px;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 18px;
  box-sizing: border-box;
}
.m-pin-left { display: flex; flex-direction: column; }
.m-pin-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 0;
  margin-bottom: var(--m-gap);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(244, 246, 248, 0.72);
  font: 400 13px/1.8 system-ui, "PingFang SC", sans-serif;
}
.m-pin-item b { color: #f0cd72; font: 700 13px/1.8 ui-monospace, monospace; }
.m-pin-item p { margin: 0; }
.m-pin-right { position: relative; }
/* 关键：右侧面板 position:sticky 钉在可视区，滚动时它不动，左侧内容继续走 */
.m-pin-stage {
  position: sticky;
  top: 0;
  height: 190px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  border: 1px solid rgba(240, 205, 114, 0.28);
  background: linear-gradient(150deg, rgba(240, 205, 114, 0.14), rgba(12, 13, 17, 0.7));
  color: rgba(240, 205, 114, 0.85);
  font: 700 13px/1 ui-monospace, monospace;
  letter-spacing: 0.3em;
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-pin-wrap">
          <div className="m-pin-left">
            <section className="m-pin-item"><b>01</b><p>描述需求，助手给出方案</p></section>
            <section className="m-pin-item"><b>02</b><p>实时预览，参数随手可调</p></section>
            <section className="m-pin-item"><b>03</b><p>导出 Vue / React / CSS</p></section>
          </div>
          <div className="m-pin-right">
            <div className="m-pin-stage">STAGE</div>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-pin-wrap {
  display: grid;
  grid-template-columns: 1fr 0.9fr;
  gap: 18px;
  width: 100%;
  height: 100%;
  overflow-y: auto;
  padding: 18px;
  box-sizing: border-box;
}
.m-pin-left { display: flex; flex-direction: column; }
.m-pin-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 0;
  margin-bottom: var(--m-gap);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(244, 246, 248, 0.72);
  font: 400 13px/1.8 system-ui, "PingFang SC", sans-serif;
}
.m-pin-item b { color: #f0cd72; font: 700 13px/1.8 ui-monospace, monospace; }
.m-pin-item p { margin: 0; }
.m-pin-right { position: relative; }
/* 关键：右侧面板 position:sticky 钉在可视区，滚动时它不动，左侧内容继续走 */
.m-pin-stage {
  position: sticky;
  top: 0;
  height: 190px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  border: 1px solid rgba(240, 205, 114, 0.28);
  background: linear-gradient(150deg, rgba(240, 205, 114, 0.14), rgba(12, 13, 17, 0.7));
  color: rgba(240, 205, 114, 0.85);
  font: 700 13px/1 ui-monospace, monospace;
  letter-spacing: 0.3em;
}
*/', '', '做钉住分屏：外层是 grid 两栏 + overflow-y:auto 的滚动容器；右栏面板用 position:sticky; top:0 钉住，左栏放三段带大间距的说明文字，滚动时右栏不动、左栏依次经过。', 'sticky,scroll,storytelling,community,prinzhorn__skrollr', 'COMMUNITY', 'https://github.com/Prinzhorn/skrollr', 'MIT', 'READY'),
	('stagger-grid-reveal', '网格错落揭示', 'Stagger Grid Reveal', '卡片进入视口时按网格顺序逐个浮起，越靠右下越晚——滚动到哪亮到哪，不需要用户做任何事。', '滚动动画', 'Dashboard', 'Cyber', 'CSS', 2, '数据后台,官网首页,AI 产品页', 'scroll', 90, 88, 90, 92, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "单卡时长", "unit": "s", "min": 0.3, "max": 1.2, "step": 0.05, "default": 0.6}, {"key": "--m-stagger", "label": "错落间隔", "unit": "ms", "min": 30, "max": 200, "step": 10, "default": 80}]', '', '<div class="motion-root">
  <div class="m-grid" id="revealGrid"></div>
</div>', '(function () {
  var grid = document.getElementById(''revealGrid'');
  if (!grid) { return; }
  var stagger = parseInt(getComputedStyle(document.documentElement).getPropertyValue(''--m-stagger''), 10);
  if (isNaN(stagger)) { stagger = 80; }
  for (var i = 0; i < 12; i++) {
    var cell = document.createElement(''div'');
    cell.className = ''m-cell'';
    grid.appendChild(cell);
  }
  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (!entry.isIntersecting) { return; }
      var index = Array.prototype.indexOf.call(grid.children, entry.target);
      var row = Math.floor(index / 3);
      var column = index % 3;
      setTimeout(function () { entry.target.classList.add(''on''); }, (row + column) * stagger);
      observer.unobserve(entry.target);
    });
  }, { rootMargin: ''0px 0px -12% 0px'', threshold: 0.2 });
  Array.prototype.forEach.call(grid.children, function (cell) { observer.observe(cell); });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { align-items: start; overflow-y: auto; }
.m-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}
.m-cell {
  height: 84px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(150deg, rgba(94, 234, 212, 0.16), rgba(12, 13, 17, 0.9));
  opacity: 0;
  transform: translateY(18px) scale(0.98);
  transition: opacity var(--m-duration) ease, transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1);
}
.m-cell.on { opacity: 1; transform: translateY(0) scale(1); }', '<template>
    <div class="motion-root">
      <div class="m-grid" id="revealGrid"></div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var grid = document.getElementById(''revealGrid'');
    if (!grid) { return; }
    var stagger = parseInt(getComputedStyle(document.documentElement).getPropertyValue(''--m-stagger''), 10);
    if (isNaN(stagger)) { stagger = 80; }
    for (var i = 0; i < 12; i++) {
      var cell = document.createElement(''div'');
      cell.className = ''m-cell'';
      grid.appendChild(cell);
    }
    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) { return; }
        var index = Array.prototype.indexOf.call(grid.children, entry.target);
        var row = Math.floor(index / 3);
        var column = index % 3;
        setTimeout(function () { entry.target.classList.add(''on''); }, (row + column) * stagger);
        observer.unobserve(entry.target);
      });
    }, { rootMargin: ''0px 0px -12% 0px'', threshold: 0.2 });
    Array.prototype.forEach.call(grid.children, function (cell) { observer.observe(cell); });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { align-items: start; overflow-y: auto; }
.m-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}
.m-cell {
  height: 84px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(150deg, rgba(94, 234, 212, 0.16), rgba(12, 13, 17, 0.9));
  opacity: 0;
  transform: translateY(18px) scale(0.98);
  transition: opacity var(--m-duration) ease, transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1);
}
.m-cell.on { opacity: 1; transform: translateY(0) scale(1); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var grid = document.getElementById(''revealGrid'');
      if (!grid) { return; }
      var stagger = parseInt(getComputedStyle(document.documentElement).getPropertyValue(''--m-stagger''), 10);
      if (isNaN(stagger)) { stagger = 80; }
      for (var i = 0; i < 12; i++) {
        var cell = document.createElement(''div'');
        cell.className = ''m-cell'';
        grid.appendChild(cell);
      }
      var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
          if (!entry.isIntersecting) { return; }
          var index = Array.prototype.indexOf.call(grid.children, entry.target);
          var row = Math.floor(index / 3);
          var column = index % 3;
          setTimeout(function () { entry.target.classList.add(''on''); }, (row + column) * stagger);
          observer.unobserve(entry.target);
        });
      }, { rootMargin: ''0px 0px -12% 0px'', threshold: 0.2 });
      Array.prototype.forEach.call(grid.children, function (cell) { observer.observe(cell); });
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-grid" id="revealGrid"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { align-items: start; overflow-y: auto; }
.m-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}
.m-cell {
  height: 84px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(150deg, rgba(94, 234, 212, 0.16), rgba(12, 13, 17, 0.9));
  opacity: 0;
  transform: translateY(18px) scale(0.98);
  transition: opacity var(--m-duration) ease, transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1);
}
.m-cell.on { opacity: 1; transform: translateY(0) scale(1); }
*/', '', '做网格错落揭示：卡片初始 opacity:0、translateY(18px) scale(0.98)；用 IntersectionObserver 观察每张卡，进入视口后按 (行号+列号)×80ms 递增延迟加上 .on 类，过渡 0.6s；触发一次后 unobserve。', 'grid,reveal,stagger,observer,community,jlmakes__scrollreveal', 'COMMUNITY', 'https://github.com/jlmakes/scrollreveal', 'NO-LICENSE', 'READY'),
	('auto-track-marquee', '横向滚动轨道', 'Auto Track Marquee', '一排卡片沿 x 轴匀速走马，鼠标悬停暂停——展示「很多客户 / 很多案例」时最省地方。', '滚动动画', 'Landing Page', 'Glass', 'CSS', 1, '官网首页,产品发布页,AI 产品页', 'load', 86, 92, 93, 93, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-speed", "label": "一圈时长", "unit": "s", "min": 8, "max": 40, "step": 1, "default": 18}, {"key": "--m-gap", "label": "卡片间距", "unit": "px", "min": 8, "max": 40, "step": 2, "default": 16}]', '', '<div class="motion-root">
  <div class="m-track-wrap">
    <div class="m-track-row">
      <span class="m-chip">Aurora</span><span class="m-chip">Glass</span><span class="m-chip">Shimmer</span>
      <span class="m-chip">Parallax</span><span class="m-chip">Ripple</span><span class="m-chip">Spotlight</span>
    </div>
    <div class="m-track-row m-track-reverse">
      <span class="m-chip">Tilt</span><span class="m-chip">Marquee</span><span class="m-chip">Reveal</span>
      <span class="m-chip">Magnetic</span><span class="m-chip">Flow</span><span class="m-chip">Caret</span>
    </div>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-track-wrap {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
}
.m-track-row {
  display: flex;
  gap: var(--m-gap);
  width: max-content;
  animation: m-marquee var(--m-speed) linear infinite;
}
.m-track-reverse { animation-direction: reverse; }
.m-track-wrap:hover .m-track-row { animation-play-state: paused; }
.m-chip {
  padding: 12px 22px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.02));
  color: rgba(244, 246, 248, 0.8);
  font: 500 13px/1 system-ui, "PingFang SC", sans-serif;
  white-space: nowrap;
}
@keyframes m-marquee {
  from { transform: translateX(0); }
  to { transform: translateX(-50%); }
}', '<template>
    <div class="motion-root">
      <div class="m-track-wrap">
        <div class="m-track-row">
          <span class="m-chip">Aurora</span><span class="m-chip">Glass</span><span class="m-chip">Shimmer</span>
          <span class="m-chip">Parallax</span><span class="m-chip">Ripple</span><span class="m-chip">Spotlight</span>
        </div>
        <div class="m-track-row m-track-reverse">
          <span class="m-chip">Tilt</span><span class="m-chip">Marquee</span><span class="m-chip">Reveal</span>
          <span class="m-chip">Magnetic</span><span class="m-chip">Flow</span><span class="m-chip">Caret</span>
        </div>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-track-wrap {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
}
.m-track-row {
  display: flex;
  gap: var(--m-gap);
  width: max-content;
  animation: m-marquee var(--m-speed) linear infinite;
}
.m-track-reverse { animation-direction: reverse; }
.m-track-wrap:hover .m-track-row { animation-play-state: paused; }
.m-chip {
  padding: 12px 22px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.02));
  color: rgba(244, 246, 248, 0.8);
  font: 500 13px/1 system-ui, "PingFang SC", sans-serif;
  white-space: nowrap;
}
@keyframes m-marquee {
  from { transform: translateX(0); }
  to { transform: translateX(-50%); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-track-wrap">
          <div className="m-track-row">
            <span className="m-chip">Aurora</span><span className="m-chip">Glass</span><span className="m-chip">Shimmer</span>
            <span className="m-chip">Parallax</span><span className="m-chip">Ripple</span><span className="m-chip">Spotlight</span>
          </div>
          <div className="m-track-row m-track-reverse">
            <span className="m-chip">Tilt</span><span className="m-chip">Marquee</span><span className="m-chip">Reveal</span>
            <span className="m-chip">Magnetic</span><span className="m-chip">Flow</span><span className="m-chip">Caret</span>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-track-wrap {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
}
.m-track-row {
  display: flex;
  gap: var(--m-gap);
  width: max-content;
  animation: m-marquee var(--m-speed) linear infinite;
}
.m-track-reverse { animation-direction: reverse; }
.m-track-wrap:hover .m-track-row { animation-play-state: paused; }
.m-chip {
  padding: 12px 22px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.02));
  color: rgba(244, 246, 248, 0.8);
  font: 500 13px/1 system-ui, "PingFang SC", sans-serif;
  white-space: nowrap;
}
@keyframes m-marquee {
  from { transform: translateX(0); }
  to { transform: translateX(-50%); }
}
*/', '', '做横向走马轨道：轨道宽度 max-content，把内容复制一份接在后面，动画从 translateX(0) 到 translateX(-50%) 就能无缝循环；两行方向相反，悬停时 animation-play-state:paused。', 'marquee,track,hover-pause,community,alexfoxy__lax.js', 'COMMUNITY', 'https://github.com/alexfoxy/lax.js', 'MIT', 'READY'),
	('parallax-depth-3', '三层视差', 'Three Layer Parallax', '背景、中景、前景三层以不同速度跟随滚动，画面一下有了纵深——只用位移，不掉帧。', '滚动动画', 'Portfolio', 'Luxury', 'CSS', 2, '个人作品集,官网首页,产品发布页', 'scroll', 91, 89, 88, 90, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-depth", "label": "视差强度", "unit": "", "min": 0.1, "max": 1.2, "step": 0.05, "default": 0.5}]', '', '<div class="motion-root">
  <div class="m-scene" id="parallaxScene">
    <div class="m-layer m-layer-back"></div>
    <div class="m-layer m-layer-mid"></div>
    <div class="m-layer m-layer-front">
      <p>滚动看三层速度差</p>
    </div>
    <span class="m-scene-filler"></span>
  </div>
</div>', '(function () {
  var scene = document.getElementById(''parallaxScene'');
  if (!scene) { return; }
  var depth = 0.5;
  var configured = parseFloat(getComputedStyle(document.documentElement).getPropertyValue(''--m-depth''));
  if (!isNaN(configured)) { depth = configured; }
  var layers = [
    { node: scene.querySelector(''.m-layer-back''), factor: 0.18 },
    { node: scene.querySelector(''.m-layer-mid''), factor: 0.36 },
    { node: scene.querySelector(''.m-layer-front''), factor: 0.62 }
  ];
  scene.addEventListener(''scroll'', function () {
    var top = scene.scrollTop;
    layers.forEach(function (layer) {
      if (!layer.node) { return; }
      layer.node.style.transform = ''translateY('' + (-top * layer.factor * depth * 2).toFixed(1) + ''px)'';
    });
  });
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-scene {
  position: relative;
  width: 100%;
  height: 100%;
  overflow-y: auto;
}
.m-layer {
  position: absolute;
  left: 0;
  right: 0;
  border-radius: 0;
  will-change: transform;
}
.m-layer-back { top: 0; height: 420px; background: linear-gradient(160deg, rgba(94, 234, 212, 0.18), rgba(12, 13, 17, 0.2)); }
.m-layer-mid { top: 90px; height: 320px; background: linear-gradient(160deg, rgba(167, 139, 250, 0.2), rgba(12, 13, 17, 0.1)); }
.m-layer-front {
  top: 260px;
  display: grid;
  place-items: center;
  color: #f4f6f8;
  font: 500 14px/1.6 system-ui, "PingFang SC", sans-serif;
}
.m-layer-front p {
  margin: 0;
  padding: 14px 22px;
  border-radius: 14px;
  border: 1px solid rgba(240, 205, 114, 0.4);
  background: rgba(10, 11, 15, 0.86);
}
.m-scene-filler { display: block; height: 900px; }', '<template>
    <div class="motion-root">
      <div class="m-scene" id="parallaxScene">
        <div class="m-layer m-layer-back"></div>
        <div class="m-layer m-layer-mid"></div>
        <div class="m-layer m-layer-front">
          <p>滚动看三层速度差</p>
        </div>
        <span class="m-scene-filler"></span>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var scene = document.getElementById(''parallaxScene'');
    if (!scene) { return; }
    var depth = 0.5;
    var configured = parseFloat(getComputedStyle(document.documentElement).getPropertyValue(''--m-depth''));
    if (!isNaN(configured)) { depth = configured; }
    var layers = [
      { node: scene.querySelector(''.m-layer-back''), factor: 0.18 },
      { node: scene.querySelector(''.m-layer-mid''), factor: 0.36 },
      { node: scene.querySelector(''.m-layer-front''), factor: 0.62 }
    ];
    scene.addEventListener(''scroll'', function () {
      var top = scene.scrollTop;
      layers.forEach(function (layer) {
        if (!layer.node) { return; }
        layer.node.style.transform = ''translateY('' + (-top * layer.factor * depth * 2).toFixed(1) + ''px)'';
      });
    });
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-scene {
  position: relative;
  width: 100%;
  height: 100%;
  overflow-y: auto;
}
.m-layer {
  position: absolute;
  left: 0;
  right: 0;
  border-radius: 0;
  will-change: transform;
}
.m-layer-back { top: 0; height: 420px; background: linear-gradient(160deg, rgba(94, 234, 212, 0.18), rgba(12, 13, 17, 0.2)); }
.m-layer-mid { top: 90px; height: 320px; background: linear-gradient(160deg, rgba(167, 139, 250, 0.2), rgba(12, 13, 17, 0.1)); }
.m-layer-front {
  top: 260px;
  display: grid;
  place-items: center;
  color: #f4f6f8;
  font: 500 14px/1.6 system-ui, "PingFang SC", sans-serif;
}
.m-layer-front p {
  margin: 0;
  padding: 14px 22px;
  border-radius: 14px;
  border: 1px solid rgba(240, 205, 114, 0.4);
  background: rgba(10, 11, 15, 0.86);
}
.m-scene-filler { display: block; height: 900px; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var scene = document.getElementById(''parallaxScene'');
      if (!scene) { return; }
      var depth = 0.5;
      var configured = parseFloat(getComputedStyle(document.documentElement).getPropertyValue(''--m-depth''));
      if (!isNaN(configured)) { depth = configured; }
      var layers = [
        { node: scene.querySelector(''.m-layer-back''), factor: 0.18 },
        { node: scene.querySelector(''.m-layer-mid''), factor: 0.36 },
        { node: scene.querySelector(''.m-layer-front''), factor: 0.62 }
      ];
      scene.addEventListener(''scroll'', function () {
        var top = scene.scrollTop;
        layers.forEach(function (layer) {
          if (!layer.node) { return; }
          layer.node.style.transform = ''translateY('' + (-top * layer.factor * depth * 2).toFixed(1) + ''px)'';
        });
      });
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-scene" id="parallaxScene">
          <div className="m-layer m-layer-back"></div>
          <div className="m-layer m-layer-mid"></div>
          <div className="m-layer m-layer-front">
            <p>滚动看三层速度差</p>
          </div>
          <span className="m-scene-filler"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}.motion-root { padding: 0; }
.m-scene {
  position: relative;
  width: 100%;
  height: 100%;
  overflow-y: auto;
}
.m-layer {
  position: absolute;
  left: 0;
  right: 0;
  border-radius: 0;
  will-change: transform;
}
.m-layer-back { top: 0; height: 420px; background: linear-gradient(160deg, rgba(94, 234, 212, 0.18), rgba(12, 13, 17, 0.2)); }
.m-layer-mid { top: 90px; height: 320px; background: linear-gradient(160deg, rgba(167, 139, 250, 0.2), rgba(12, 13, 17, 0.1)); }
.m-layer-front {
  top: 260px;
  display: grid;
  place-items: center;
  color: #f4f6f8;
  font: 500 14px/1.6 system-ui, "PingFang SC", sans-serif;
}
.m-layer-front p {
  margin: 0;
  padding: 14px 22px;
  border-radius: 14px;
  border: 1px solid rgba(240, 205, 114, 0.4);
  background: rgba(10, 11, 15, 0.86);
}
.m-scene-filler { display: block; height: 900px; }
*/', '', '做三层视差：滚动容器里放三层绝对定位的图层，滚动时分别以 0.18 / 0.36 / 0.62 的系数反向平移（乘一个统一的强度参数），用 transform:translateY 而不是改 top，保证走合成层不掉帧。', 'parallax,layers,scroll,community,locomotivemtl__locomotive-scroll', 'COMMUNITY', 'https://github.com/locomotivemtl/locomotive-scroll', 'MIT', 'READY'),
	('hero-split-curtain', '幕布分屏入场', 'Hero Split Curtain', '首屏像两扇幕布一样向两侧拉开，露出后面的标题与内容——发布会与产品页开场很吃这套。', '首屏动画', 'Landing Page', 'Luxury', 'CSS', 2, '官网首页,产品发布页,AI 产品页', 'load', 93, 89, 86, 92, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "拉开时长", "unit": "s", "min": 0.6, "max": 2.4, "step": 0.1, "default": 1.1}, {"key": "--m-delay", "label": "内容延迟", "unit": "s", "min": 0, "max": 0.9, "step": 0.05, "default": 0.35}]', '', '<div class="motion-root">
  <div class="m-hero-content">
    <h2>KINGDOM</h2>
    <p>幕布拉开 · Split Curtain</p>
  </div>
  <span class="m-curtain m-curtain-left"></span>
  <span class="m-curtain m-curtain-right"></span>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-hero-content {
  text-align: center;
  animation: m-content-in 0.9s cubic-bezier(0.22, 0.61, 0.36, 1) var(--m-delay) both;
}
.m-hero-content h2 {
  margin: 0;
  font: 800 52px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  color: #f4f6f8;
}
.m-hero-content p {
  margin: 12px 0 0;
  font: 400 12.5px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.24em;
  color: rgba(240, 205, 114, 0.8);
}
.m-curtain {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 50.5%;
  background: linear-gradient(120deg, #14161d, #0a0b10);
  animation: m-curtain var(--m-duration) cubic-bezier(0.76, 0, 0.24, 1) forwards;
}
.m-curtain-left { left: 0; --m-to: -100%; }
.m-curtain-right { right: 0; --m-to: 100%; }
@keyframes m-curtain {
  to { transform: translateX(var(--m-to)); }
}
@keyframes m-content-in {
  from { opacity: 0; transform: translateY(14px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}', '<template>
    <div class="motion-root">
      <div class="m-hero-content">
        <h2>KINGDOM</h2>
        <p>幕布拉开 · Split Curtain</p>
      </div>
      <span class="m-curtain m-curtain-left"></span>
      <span class="m-curtain m-curtain-right"></span>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-hero-content {
  text-align: center;
  animation: m-content-in 0.9s cubic-bezier(0.22, 0.61, 0.36, 1) var(--m-delay) both;
}
.m-hero-content h2 {
  margin: 0;
  font: 800 52px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  color: #f4f6f8;
}
.m-hero-content p {
  margin: 12px 0 0;
  font: 400 12.5px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.24em;
  color: rgba(240, 205, 114, 0.8);
}
.m-curtain {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 50.5%;
  background: linear-gradient(120deg, #14161d, #0a0b10);
  animation: m-curtain var(--m-duration) cubic-bezier(0.76, 0, 0.24, 1) forwards;
}
.m-curtain-left { left: 0; --m-to: -100%; }
.m-curtain-right { right: 0; --m-to: 100%; }
@keyframes m-curtain {
  to { transform: translateX(var(--m-to)); }
}
@keyframes m-content-in {
  from { opacity: 0; transform: translateY(14px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-hero-content">
          <h2>KINGDOM</h2>
          <p>幕布拉开 · Split Curtain</p>
        </div>
        <span className="m-curtain m-curtain-left"></span>
        <span className="m-curtain m-curtain-right"></span>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-hero-content {
  text-align: center;
  animation: m-content-in 0.9s cubic-bezier(0.22, 0.61, 0.36, 1) var(--m-delay) both;
}
.m-hero-content h2 {
  margin: 0;
  font: 800 52px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.06em;
  color: #f4f6f8;
}
.m-hero-content p {
  margin: 12px 0 0;
  font: 400 12.5px/1.6 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.24em;
  color: rgba(240, 205, 114, 0.8);
}
.m-curtain {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 50.5%;
  background: linear-gradient(120deg, #14161d, #0a0b10);
  animation: m-curtain var(--m-duration) cubic-bezier(0.76, 0, 0.24, 1) forwards;
}
.m-curtain-left { left: 0; --m-to: -100%; }
.m-curtain-right { right: 0; --m-to: 100%; }
@keyframes m-curtain {
  to { transform: translateX(var(--m-to)); }
}
@keyframes m-content-in {
  from { opacity: 0; transform: translateY(14px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
*/', '', '做幕布分屏入场：两片各占 50% 宽的深色幕布盖在首屏上，分别向左/右 translateX(±100%) 拉开（1.1s，cubic-bezier(0.76,0,0.24,1)）；内容在幕布拉开 0.35s 后淡入上移 14px。', 'curtain,hero,open,community,herotransitions__hero', 'COMMUNITY', 'https://github.com/HeroTransitions/Hero', 'MIT', 'READY'),
	('product-float-in', '产品浮现', 'Product Float In', '产品图从下方缓慢浮起并落定，阴影由散到聚——比直接淡入更像「东西被放上来」。', '首屏动画', 'Landing Page', 'Luxury', 'CSS', 2, '产品发布页,官网首页,AI 产品页', 'load', 92, 90, 88, 92, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "浮起时长", "unit": "s", "min": 0.6, "max": 2.6, "step": 0.1, "default": 1.4}, {"key": "--m-distance", "label": "起始高度", "unit": "px", "min": 20, "max": 140, "step": 5, "default": 64}]', '', '<div class="motion-root">
  <div class="m-product">
    <div class="m-product-inner">
      <span class="m-product-badge">NEW</span>
      <strong>Kingdom Studio</strong>
      <span class="m-product-sub">AI Creator Workbench</span>
    </div>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-product {
  animation: m-float-in var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) both;
}
.m-product-inner {
  width: 230px;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(240, 205, 114, 0.35);
  background: linear-gradient(155deg, rgba(240, 205, 114, 0.16), rgba(12, 13, 17, 0.94));
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #f4f6f8;
}
.m-product-badge {
  align-self: flex-start;
  font: 700 9.5px/1 ui-monospace, monospace;
  letter-spacing: 0.22em;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(240, 205, 114, 0.18);
  color: #f0cd72;
}
.m-product-inner strong { font: 700 20px/1.2 system-ui, sans-serif; }
.m-product-sub { font: 400 12px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
@keyframes m-float-in {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.94); filter: blur(6px); }
  to { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}', '<template>
    <div class="motion-root">
      <div class="m-product">
        <div class="m-product-inner">
          <span class="m-product-badge">NEW</span>
          <strong>Kingdom Studio</strong>
          <span class="m-product-sub">AI Creator Workbench</span>
        </div>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-product {
  animation: m-float-in var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) both;
}
.m-product-inner {
  width: 230px;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(240, 205, 114, 0.35);
  background: linear-gradient(155deg, rgba(240, 205, 114, 0.16), rgba(12, 13, 17, 0.94));
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #f4f6f8;
}
.m-product-badge {
  align-self: flex-start;
  font: 700 9.5px/1 ui-monospace, monospace;
  letter-spacing: 0.22em;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(240, 205, 114, 0.18);
  color: #f0cd72;
}
.m-product-inner strong { font: 700 20px/1.2 system-ui, sans-serif; }
.m-product-sub { font: 400 12px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
@keyframes m-float-in {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.94); filter: blur(6px); }
  to { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-product">
          <div className="m-product-inner">
            <span className="m-product-badge">NEW</span>
            <strong>Kingdom Studio</strong>
            <span className="m-product-sub">AI Creator Workbench</span>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-product {
  animation: m-float-in var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1) both;
}
.m-product-inner {
  width: 230px;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(240, 205, 114, 0.35);
  background: linear-gradient(155deg, rgba(240, 205, 114, 0.16), rgba(12, 13, 17, 0.94));
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #f4f6f8;
}
.m-product-badge {
  align-self: flex-start;
  font: 700 9.5px/1 ui-monospace, monospace;
  letter-spacing: 0.22em;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(240, 205, 114, 0.18);
  color: #f0cd72;
}
.m-product-inner strong { font: 700 20px/1.2 system-ui, sans-serif; }
.m-product-sub { font: 400 12px/1.6 system-ui, sans-serif; color: rgba(244, 246, 248, 0.5); }
@keyframes m-float-in {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.94); filter: blur(6px); }
  to { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}
*/', '', '做产品浮现入场：产品卡从 translateY(64px) scale(0.94) 且带 6px 模糊的状态浮到原位，1.4s cubic-bezier(0.22,0.61,0.36,1)，阴影同步从散到聚。', 'product,hero,float,community,snail-z__dawntransition', 'COMMUNITY', 'https://github.com/snail-z/DawnTransition', 'MIT', 'READY'),
	('headline-clip-wipe', '标题擦除入场', 'Headline Clip Wipe', '一道亮色条从左到右扫过，扫过的地方标题才显现——把「逐段出现」做成一个可视的动作。', '首屏动画', 'AI SaaS', 'Cyber', 'CSS', 2, 'AI 产品页,官网首页,产品发布页', 'load', 90, 91, 89, 94, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "擦除时长", "unit": "s", "min": 0.5, "max": 2.2, "step": 0.1, "default": 1.1}, {"key": "--m-offset", "label": "色条偏移", "unit": "px", "min": 0, "max": 24, "step": 2, "default": 8}]', '', '<div class="motion-root">
  <div class="m-wipe">
    <h2>MOTION INTELLIGENCE</h2>
    <span class="m-wipe-bar"></span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-wipe { position: relative; }
.m-wipe h2 {
  margin: 0;
  font: 800 30px/1.2 ui-monospace, "Cascadia Mono", Consolas, monospace;
  letter-spacing: 0.1em;
  color: #e8f7f2;
  clip-path: inset(0 100% 0 0);
  animation: m-wipe-text var(--m-duration) cubic-bezier(0.65, 0, 0.35, 1) forwards;
}
.m-wipe-bar {
  position: absolute;
  top: -6px;
  bottom: -6px;
  width: 2px;
  background: linear-gradient(180deg, transparent, #5eead4, transparent);
  box-shadow: 0 0 18px rgba(94, 234, 212, 0.8);
  animation: m-wipe-bar var(--m-duration) cubic-bezier(0.65, 0, 0.35, 1) forwards;
}
@keyframes m-wipe-text {
  to { clip-path: inset(0 0 0 0); }
}
@keyframes m-wipe-bar {
  from { left: 0; opacity: 1; }
  85% { opacity: 1; }
  to { left: calc(100% + var(--m-offset)); opacity: 0; }
}', '<template>
    <div class="motion-root">
      <div class="m-wipe">
        <h2>MOTION INTELLIGENCE</h2>
        <span class="m-wipe-bar"></span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-wipe { position: relative; }
.m-wipe h2 {
  margin: 0;
  font: 800 30px/1.2 ui-monospace, "Cascadia Mono", Consolas, monospace;
  letter-spacing: 0.1em;
  color: #e8f7f2;
  clip-path: inset(0 100% 0 0);
  animation: m-wipe-text var(--m-duration) cubic-bezier(0.65, 0, 0.35, 1) forwards;
}
.m-wipe-bar {
  position: absolute;
  top: -6px;
  bottom: -6px;
  width: 2px;
  background: linear-gradient(180deg, transparent, #5eead4, transparent);
  box-shadow: 0 0 18px rgba(94, 234, 212, 0.8);
  animation: m-wipe-bar var(--m-duration) cubic-bezier(0.65, 0, 0.35, 1) forwards;
}
@keyframes m-wipe-text {
  to { clip-path: inset(0 0 0 0); }
}
@keyframes m-wipe-bar {
  from { left: 0; opacity: 1; }
  85% { opacity: 1; }
  to { left: calc(100% + var(--m-offset)); opacity: 0; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-wipe">
          <h2>MOTION INTELLIGENCE</h2>
          <span className="m-wipe-bar"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-wipe { position: relative; }
.m-wipe h2 {
  margin: 0;
  font: 800 30px/1.2 ui-monospace, "Cascadia Mono", Consolas, monospace;
  letter-spacing: 0.1em;
  color: #e8f7f2;
  clip-path: inset(0 100% 0 0);
  animation: m-wipe-text var(--m-duration) cubic-bezier(0.65, 0, 0.35, 1) forwards;
}
.m-wipe-bar {
  position: absolute;
  top: -6px;
  bottom: -6px;
  width: 2px;
  background: linear-gradient(180deg, transparent, #5eead4, transparent);
  box-shadow: 0 0 18px rgba(94, 234, 212, 0.8);
  animation: m-wipe-bar var(--m-duration) cubic-bezier(0.65, 0, 0.35, 1) forwards;
}
@keyframes m-wipe-text {
  to { clip-path: inset(0 0 0 0); }
}
@keyframes m-wipe-bar {
  from { left: 0; opacity: 1; }
  85% { opacity: 1; }
  to { left: calc(100% + var(--m-offset)); opacity: 0; }
}
*/', '', '做标题擦除入场：标题 clip-path 从 inset(0 100% 0 0) 动画到 inset(0 0 0 0)；同时一条 2px 的青色发光竖条从左侧扫到右侧并淡出，两者同为 1.1s cubic-bezier(0.65,0,0.35,1)。', 'clip,wipe,headline,community,jeffersonlicet__react-motion-layout', 'COMMUNITY', 'https://github.com/jeffersonlicet/react-motion-layout', 'MIT', 'READY'),
	('scroll-hint-bounce', '滚动提示', 'Scroll Hint', '首屏底部的向下指示器反复轻跳，配一条竖向渐隐轨迹，无声地告诉用户「下面还有」。', '首屏动画', 'Landing Page', 'Minimal', 'CSS', 1, '官网首页,产品发布页,个人作品集', 'load', 81, 95, 93, 97, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-speed", "label": "跳动周期", "unit": "s", "min": 0.8, "max": 3, "step": 0.1, "default": 1.7}, {"key": "--m-distance", "label": "跳动距离", "unit": "px", "min": 3, "max": 18, "step": 1, "default": 8}]', '', '<div class="motion-root">
  <div class="m-hint">
    <span class="m-hint-text">向下滚动</span>
    <span class="m-hint-dot"></span>
    <span class="m-hint-line"></span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.m-hint-text {
  font: 400 11px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.3em;
  color: rgba(244, 246, 248, 0.45);
}
.m-hint-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f0cd72;
  animation: m-hint-dot var(--m-speed) cubic-bezier(0.4, 0, 0.2, 1) infinite;
}
.m-hint-line {
  width: 1px;
  height: 42px;
  background: linear-gradient(180deg, rgba(240, 205, 114, 0.7), rgba(240, 205, 114, 0));
}
@keyframes m-hint-dot {
  0%, 100% { transform: translateY(0); opacity: 0.5; }
  50% { transform: translateY(var(--m-distance)); opacity: 1; }
}', '<template>
    <div class="motion-root">
      <div class="m-hint">
        <span class="m-hint-text">向下滚动</span>
        <span class="m-hint-dot"></span>
        <span class="m-hint-line"></span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.m-hint-text {
  font: 400 11px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.3em;
  color: rgba(244, 246, 248, 0.45);
}
.m-hint-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f0cd72;
  animation: m-hint-dot var(--m-speed) cubic-bezier(0.4, 0, 0.2, 1) infinite;
}
.m-hint-line {
  width: 1px;
  height: 42px;
  background: linear-gradient(180deg, rgba(240, 205, 114, 0.7), rgba(240, 205, 114, 0));
}
@keyframes m-hint-dot {
  0%, 100% { transform: translateY(0); opacity: 0.5; }
  50% { transform: translateY(var(--m-distance)); opacity: 1; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-hint">
          <span className="m-hint-text">向下滚动</span>
          <span className="m-hint-dot"></span>
          <span className="m-hint-line"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
}
.m-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.m-hint-text {
  font: 400 11px/1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.3em;
  color: rgba(244, 246, 248, 0.45);
}
.m-hint-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #f0cd72;
  animation: m-hint-dot var(--m-speed) cubic-bezier(0.4, 0, 0.2, 1) infinite;
}
.m-hint-line {
  width: 1px;
  height: 42px;
  background: linear-gradient(180deg, rgba(240, 205, 114, 0.7), rgba(240, 205, 114, 0));
}
@keyframes m-hint-dot {
  0%, 100% { transform: translateY(0); opacity: 0.5; }
  50% { transform: translateY(var(--m-distance)); opacity: 1; }
}
*/', '', '做滚动提示：一个 5px 的金色圆点沿 y 轴上下跳 8px（1.7s cubic-bezier(0.4,0,0.2,1) 无限循环，同时透明度 0.5↔1），下方接一条 42px 高、向下渐隐的竖线。', 'hint,scroll,hero,community,letsar__flutter_sidekick', 'COMMUNITY', 'https://github.com/letsar/flutter_sidekick', 'MIT', 'READY'),
	('mesh-drift-bg', '网格渐变漂移', 'Mesh Gradient Drift', '多层径向渐变缓慢漂移叠加，形成会呼吸的网格渐变——比静态渐变高级，又不吃 GPU。', '背景效果', 'AI SaaS', 'Luxury', 'CSS', 2, 'AI 产品页,官网首页,产品发布页', 'load', 92, 91, 89, 90, 91, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-speed", "label": "漂移周期", "unit": "s", "min": 8, "max": 40, "step": 1, "default": 22}, {"key": "--m-blur", "label": "融合模糊", "unit": "px", "min": 20, "max": 90, "step": 5, "default": 60}]', '', '<div class="motion-root">
  <div class="m-mesh">
    <span class="m-blob m-blob-a"></span>
    <span class="m-blob m-blob-b"></span>
    <span class="m-blob m-blob-c"></span>
    <span class="m-mesh-title">AURORA MESH</span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-mesh {
  position: relative;
  width: 100%;
  height: 100%;
  background: #06070a;
  overflow: hidden;
  display: grid;
  place-items: center;
}
.m-blob {
  position: absolute;
  width: 62%;
  aspect-ratio: 1;
  border-radius: 50%;
  filter: blur(var(--m-blur));
  opacity: 0.75;
  will-change: transform;
}
.m-blob-a { background: rgba(240, 205, 114, 0.55); top: -12%; left: -6%; animation: m-drift-a var(--m-speed) ease-in-out infinite; }
.m-blob-b { background: rgba(167, 139, 250, 0.5); bottom: -16%; right: -8%; animation: m-drift-b var(--m-speed) ease-in-out infinite; }
.m-blob-c { background: rgba(94, 234, 212, 0.42); top: 24%; right: 18%; width: 44%; animation: m-drift-c var(--m-speed) ease-in-out infinite; }
.m-mesh-title {
  position: relative;
  font: 800 15px/1 ui-monospace, monospace;
  letter-spacing: 0.42em;
  color: rgba(255, 255, 255, 0.86);
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.6);
}
@keyframes m-drift-a { 0%,100% { transform: translate3d(0,0,0) scale(1); } 50% { transform: translate3d(10%,8%,0) scale(1.12); } }
@keyframes m-drift-b { 0%,100% { transform: translate3d(0,0,0) scale(1.06); } 50% { transform: translate3d(-12%,-6%,0) scale(1); } }
@keyframes m-drift-c { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-8%,10%,0); } }', '<template>
    <div class="motion-root">
      <div class="m-mesh">
        <span class="m-blob m-blob-a"></span>
        <span class="m-blob m-blob-b"></span>
        <span class="m-blob m-blob-c"></span>
        <span class="m-mesh-title">AURORA MESH</span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-mesh {
  position: relative;
  width: 100%;
  height: 100%;
  background: #06070a;
  overflow: hidden;
  display: grid;
  place-items: center;
}
.m-blob {
  position: absolute;
  width: 62%;
  aspect-ratio: 1;
  border-radius: 50%;
  filter: blur(var(--m-blur));
  opacity: 0.75;
  will-change: transform;
}
.m-blob-a { background: rgba(240, 205, 114, 0.55); top: -12%; left: -6%; animation: m-drift-a var(--m-speed) ease-in-out infinite; }
.m-blob-b { background: rgba(167, 139, 250, 0.5); bottom: -16%; right: -8%; animation: m-drift-b var(--m-speed) ease-in-out infinite; }
.m-blob-c { background: rgba(94, 234, 212, 0.42); top: 24%; right: 18%; width: 44%; animation: m-drift-c var(--m-speed) ease-in-out infinite; }
.m-mesh-title {
  position: relative;
  font: 800 15px/1 ui-monospace, monospace;
  letter-spacing: 0.42em;
  color: rgba(255, 255, 255, 0.86);
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.6);
}
@keyframes m-drift-a { 0%,100% { transform: translate3d(0,0,0) scale(1); } 50% { transform: translate3d(10%,8%,0) scale(1.12); } }
@keyframes m-drift-b { 0%,100% { transform: translate3d(0,0,0) scale(1.06); } 50% { transform: translate3d(-12%,-6%,0) scale(1); } }
@keyframes m-drift-c { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-8%,10%,0); } }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-mesh">
          <span className="m-blob m-blob-a"></span>
          <span className="m-blob m-blob-b"></span>
          <span className="m-blob m-blob-c"></span>
          <span className="m-mesh-title">AURORA MESH</span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-mesh {
  position: relative;
  width: 100%;
  height: 100%;
  background: #06070a;
  overflow: hidden;
  display: grid;
  place-items: center;
}
.m-blob {
  position: absolute;
  width: 62%;
  aspect-ratio: 1;
  border-radius: 50%;
  filter: blur(var(--m-blur));
  opacity: 0.75;
  will-change: transform;
}
.m-blob-a { background: rgba(240, 205, 114, 0.55); top: -12%; left: -6%; animation: m-drift-a var(--m-speed) ease-in-out infinite; }
.m-blob-b { background: rgba(167, 139, 250, 0.5); bottom: -16%; right: -8%; animation: m-drift-b var(--m-speed) ease-in-out infinite; }
.m-blob-c { background: rgba(94, 234, 212, 0.42); top: 24%; right: 18%; width: 44%; animation: m-drift-c var(--m-speed) ease-in-out infinite; }
.m-mesh-title {
  position: relative;
  font: 800 15px/1 ui-monospace, monospace;
  letter-spacing: 0.42em;
  color: rgba(255, 255, 255, 0.86);
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.6);
}
@keyframes m-drift-a { 0%,100% { transform: translate3d(0,0,0) scale(1); } 50% { transform: translate3d(10%,8%,0) scale(1.12); } }
@keyframes m-drift-b { 0%,100% { transform: translate3d(0,0,0) scale(1.06); } 50% { transform: translate3d(-12%,-6%,0) scale(1); } }
@keyframes m-drift-c { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-8%,10%,0); } }
*/', '', '做网格渐变漂移背景：三个不同颜色的大圆（金/紫/青）绝对定位并加 blur(60px) 融合，各自用 translate3d + scale 做 22 秒的错位漂移，中心压一层带字距的标题；只用 transform 保证走合成层。', 'mesh,gradient,background,blur,community,sarcadass__granim.js', 'COMMUNITY', 'https://github.com/sarcadass/granim.js', 'MIT', 'READY'),
	('grain-overlay-bg', '颗粒噪点叠加', 'Grain Overlay', '用 SVG 噪点贴图叠一层会轻微跳动的颗粒，数字画面立刻有了胶片质感——一层就够，别加第二层。', '背景效果', 'Portfolio', 'Luxury', 'CSS', 2, '个人作品集,产品发布页,官网首页', 'load', 87, 92, 92, 93, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-opacity", "label": "颗粒强度", "unit": "", "min": 0.04, "max": 0.34, "step": 0.02, "default": 0.14}, {"key": "--m-speed", "label": "跳动周期", "unit": "s", "min": 0.2, "max": 1.4, "step": 0.1, "default": 0.6}]', '', '<div class="motion-root">
  <div class="m-grain-stage">
    <span class="m-grain-title">FILM GRAIN</span>
    <span class="m-grain"></span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-grain-stage {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(150deg, #171a22, #0a0b10 60%);
  overflow: hidden;
}
.m-grain-title {
  font: 700 18px/1 ui-monospace, monospace;
  letter-spacing: 0.34em;
  color: rgba(244, 246, 248, 0.9);
}
/* 噪点用内联 SVG 生成，不依赖任何外部图片 */
.m-grain {
  position: absolute;
  inset: -60%;
  opacity: var(--m-opacity);
  pointer-events: none;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns=''http://www.w3.org/2000/svg'' width=''160'' height=''160''><filter id=''n''><feTurbulence type=''fractalNoise'' baseFrequency=''0.85'' numOctaves=''3''/></filter><rect width=''160'' height=''160'' filter=''url(%23n)''/></svg>");
  animation: m-grain var(--m-speed) steps(4, end) infinite;
}
@keyframes m-grain {
  0% { transform: translate3d(0, 0, 0); }
  25% { transform: translate3d(-6%, 3%, 0); }
  50% { transform: translate3d(4%, -5%, 0); }
  75% { transform: translate3d(-3%, -3%, 0); }
  100% { transform: translate3d(0, 0, 0); }
}', '<template>
    <div class="motion-root">
      <div class="m-grain-stage">
        <span class="m-grain-title">FILM GRAIN</span>
        <span class="m-grain"></span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-grain-stage {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(150deg, #171a22, #0a0b10 60%);
  overflow: hidden;
}
.m-grain-title {
  font: 700 18px/1 ui-monospace, monospace;
  letter-spacing: 0.34em;
  color: rgba(244, 246, 248, 0.9);
}
/* 噪点用内联 SVG 生成，不依赖任何外部图片 */
.m-grain {
  position: absolute;
  inset: -60%;
  opacity: var(--m-opacity);
  pointer-events: none;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns=''http://www.w3.org/2000/svg'' width=''160'' height=''160''><filter id=''n''><feTurbulence type=''fractalNoise'' baseFrequency=''0.85'' numOctaves=''3''/></filter><rect width=''160'' height=''160'' filter=''url(%23n)''/></svg>");
  animation: m-grain var(--m-speed) steps(4, end) infinite;
}
@keyframes m-grain {
  0% { transform: translate3d(0, 0, 0); }
  25% { transform: translate3d(-6%, 3%, 0); }
  50% { transform: translate3d(4%, -5%, 0); }
  75% { transform: translate3d(-3%, -3%, 0); }
  100% { transform: translate3d(0, 0, 0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-grain-stage">
          <span className="m-grain-title">FILM GRAIN</span>
          <span className="m-grain"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-grain-stage {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: linear-gradient(150deg, #171a22, #0a0b10 60%);
  overflow: hidden;
}
.m-grain-title {
  font: 700 18px/1 ui-monospace, monospace;
  letter-spacing: 0.34em;
  color: rgba(244, 246, 248, 0.9);
}
/* 噪点用内联 SVG 生成，不依赖任何外部图片 */
.m-grain {
  position: absolute;
  inset: -60%;
  opacity: var(--m-opacity);
  pointer-events: none;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns=''http://www.w3.org/2000/svg'' width=''160'' height=''160''><filter id=''n''><feTurbulence type=''fractalNoise'' baseFrequency=''0.85'' numOctaves=''3''/></filter><rect width=''160'' height=''160'' filter=''url(%23n)''/></svg>");
  animation: m-grain var(--m-speed) steps(4, end) infinite;
}
@keyframes m-grain {
  0% { transform: translate3d(0, 0, 0); }
  25% { transform: translate3d(-6%, 3%, 0); }
  50% { transform: translate3d(4%, -5%, 0); }
  75% { transform: translate3d(-3%, -3%, 0); }
  100% { transform: translate3d(0, 0, 0); }
}
*/', '', '做颗粒噪点叠加：用内联 SVG 的 feTurbulence（baseFrequency 0.85）当噪点贴图，铺满一层 opacity 0.14 的覆盖层；用 steps(4) 让它在 0.6 秒内跳四个位移，形成胶片颗粒感。', 'grain,noise,texture,community,tsparticles__tsparticles', 'COMMUNITY', 'https://github.com/tsparticles/tsparticles', 'MIT', 'READY'),
	('multi-orb-float', '多光球漂浮', 'Multi Orb Float', '几个大小不一的柔光球各自漂浮，营造「AI 正在思考」的氛围——首页与空状态都能用。', '背景效果', 'AI SaaS', 'Cyber', 'CSS', 1, 'AI 产品页,登录页面,数据后台', 'load', 88, 92, 91, 92, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-speed", "label": "漂浮周期", "unit": "s", "min": 4, "max": 20, "step": 0.5, "default": 11}, {"key": "--m-blur", "label": "柔光半径", "unit": "px", "min": 6, "max": 40, "step": 2, "default": 18}]', '', '<div class="motion-root">
  <div class="m-orb-stage">
    <span class="m-orb m-orb-1"></span>
    <span class="m-orb m-orb-2"></span>
    <span class="m-orb m-orb-3"></span>
    <span class="m-orb m-orb-4"></span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-orb-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 120%, #101528, #05060a 70%);
  overflow: hidden;
}
.m-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(var(--m-blur));
  mix-blend-mode: screen;
  will-change: transform;
}
.m-orb-1 { width: 90px; height: 90px; top: 22%; left: 18%; background: radial-gradient(circle at 35% 35%, rgba(240, 205, 114, 0.95), rgba(240, 205, 114, 0.05)); animation: m-orb-a var(--m-speed) ease-in-out infinite; }
.m-orb-2 { width: 64px; height: 64px; top: 54%; left: 62%; background: radial-gradient(circle at 40% 40%, rgba(94, 234, 212, 0.9), rgba(94, 234, 212, 0.04)); animation: m-orb-b calc(var(--m-speed) * 0.8) ease-in-out infinite; }
.m-orb-3 { width: 42px; height: 42px; top: 32%; left: 68%; background: radial-gradient(circle at 40% 40%, rgba(167, 139, 250, 0.9), rgba(167, 139, 250, 0.04)); animation: m-orb-c calc(var(--m-speed) * 1.2) ease-in-out infinite; }
.m-orb-4 { width: 26px; height: 26px; top: 68%; left: 34%; background: radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.04)); animation: m-orb-d calc(var(--m-speed) * 0.9) ease-in-out infinite; }
@keyframes m-orb-a { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(26px,-22px,0); } }
@keyframes m-orb-b { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-30px,18px,0); } }
@keyframes m-orb-c { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(18px,26px,0); } }
@keyframes m-orb-d { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-22px,-14px,0); } }', '<template>
    <div class="motion-root">
      <div class="m-orb-stage">
        <span class="m-orb m-orb-1"></span>
        <span class="m-orb m-orb-2"></span>
        <span class="m-orb m-orb-3"></span>
        <span class="m-orb m-orb-4"></span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-orb-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 120%, #101528, #05060a 70%);
  overflow: hidden;
}
.m-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(var(--m-blur));
  mix-blend-mode: screen;
  will-change: transform;
}
.m-orb-1 { width: 90px; height: 90px; top: 22%; left: 18%; background: radial-gradient(circle at 35% 35%, rgba(240, 205, 114, 0.95), rgba(240, 205, 114, 0.05)); animation: m-orb-a var(--m-speed) ease-in-out infinite; }
.m-orb-2 { width: 64px; height: 64px; top: 54%; left: 62%; background: radial-gradient(circle at 40% 40%, rgba(94, 234, 212, 0.9), rgba(94, 234, 212, 0.04)); animation: m-orb-b calc(var(--m-speed) * 0.8) ease-in-out infinite; }
.m-orb-3 { width: 42px; height: 42px; top: 32%; left: 68%; background: radial-gradient(circle at 40% 40%, rgba(167, 139, 250, 0.9), rgba(167, 139, 250, 0.04)); animation: m-orb-c calc(var(--m-speed) * 1.2) ease-in-out infinite; }
.m-orb-4 { width: 26px; height: 26px; top: 68%; left: 34%; background: radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.04)); animation: m-orb-d calc(var(--m-speed) * 0.9) ease-in-out infinite; }
@keyframes m-orb-a { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(26px,-22px,0); } }
@keyframes m-orb-b { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-30px,18px,0); } }
@keyframes m-orb-c { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(18px,26px,0); } }
@keyframes m-orb-d { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-22px,-14px,0); } }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-orb-stage">
          <span className="m-orb m-orb-1"></span>
          <span className="m-orb m-orb-2"></span>
          <span className="m-orb m-orb-3"></span>
          <span className="m-orb m-orb-4"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-orb-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 120%, #101528, #05060a 70%);
  overflow: hidden;
}
.m-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(var(--m-blur));
  mix-blend-mode: screen;
  will-change: transform;
}
.m-orb-1 { width: 90px; height: 90px; top: 22%; left: 18%; background: radial-gradient(circle at 35% 35%, rgba(240, 205, 114, 0.95), rgba(240, 205, 114, 0.05)); animation: m-orb-a var(--m-speed) ease-in-out infinite; }
.m-orb-2 { width: 64px; height: 64px; top: 54%; left: 62%; background: radial-gradient(circle at 40% 40%, rgba(94, 234, 212, 0.9), rgba(94, 234, 212, 0.04)); animation: m-orb-b calc(var(--m-speed) * 0.8) ease-in-out infinite; }
.m-orb-3 { width: 42px; height: 42px; top: 32%; left: 68%; background: radial-gradient(circle at 40% 40%, rgba(167, 139, 250, 0.9), rgba(167, 139, 250, 0.04)); animation: m-orb-c calc(var(--m-speed) * 1.2) ease-in-out infinite; }
.m-orb-4 { width: 26px; height: 26px; top: 68%; left: 34%; background: radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.04)); animation: m-orb-d calc(var(--m-speed) * 0.9) ease-in-out infinite; }
@keyframes m-orb-a { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(26px,-22px,0); } }
@keyframes m-orb-b { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-30px,18px,0); } }
@keyframes m-orb-c { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(18px,26px,0); } }
@keyframes m-orb-d { 0%,100% { transform: translate3d(0,0,0); } 50% { transform: translate3d(-22px,-14px,0); } }
*/', '', '做多光球漂浮：四个尺寸递减的圆，各自用 radial-gradient 画高光（外圈透明），加 blur(18px) 与 mix-blend-mode:screen；用不同的周期与相位做 translate3d 漂浮，营造柔光氛围。', 'orb,glow,ambient,community,cruisediary__pastel', 'COMMUNITY', 'https://github.com/cruisediary/Pastel', 'MIT', 'READY'),
	('starfield-drift-bg', '星空漂移', 'Starfield Drift', '两层星点以不同速度横向漂移，做出视差星空——纯 CSS 实现，不需要 canvas。', '背景效果', 'Game UI', 'Cyber', 'CSS', 1, '游戏界面,登录页面,AI 产品页', 'load', 86, 93, 92, 95, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-speed", "label": "漂移周期", "unit": "s", "min": 6, "max": 40, "step": 1, "default": 20}, {"key": "--m-count", "label": "星点密度", "unit": "", "min": 0.4, "max": 1.6, "step": 0.1, "default": 1}]', '', '<div class="motion-root">
  <div class="m-star-stage">
    <span class="m-stars m-stars-far"></span>
    <span class="m-stars m-stars-near"></span>
  </div>
</div>', '', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-star-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(130% 100% at 50% 0%, #0d1220, #04050a 70%);
  overflow: hidden;
}
.m-stars {
  position: absolute;
  top: 0;
  bottom: 0;
  /* 宽度是容器的两倍，才能无缝循环 */
  width: 200%;
  background-repeat: repeat;
  will-change: transform;
}
.m-stars-far {
  opacity: calc(0.4 + var(--m-count) * 0.3);
  background-image: radial-gradient(1px 1px at 20px 30px, rgba(255,255,255,0.85), transparent),
                    radial-gradient(1px 1px at 120px 80px, rgba(255,255,255,0.6), transparent),
                    radial-gradient(1px 1px at 210px 150px, rgba(200,225,255,0.8), transparent);
  background-size: 260px 220px;
  animation: m-star-drift var(--m-speed) linear infinite;
}
.m-stars-near {
  opacity: calc(0.5 + var(--m-count) * 0.35);
  background-image: radial-gradient(1.6px 1.6px at 60px 120px, rgba(255,255,255,0.95), transparent),
                    radial-gradient(1.4px 1.4px at 190px 40px, rgba(240,205,114,0.9), transparent);
  background-size: 340px 260px;
  animation: m-star-drift calc(var(--m-speed) * 0.55) linear infinite;
}
@keyframes m-star-drift {
  from { transform: translate3d(0, 0, 0); }
  to { transform: translate3d(-50%, 0, 0); }
}', '<template>
    <div class="motion-root">
      <div class="m-star-stage">
        <span class="m-stars m-stars-far"></span>
        <span class="m-stars m-stars-near"></span>
      </div>
    </div>
</template>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-star-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(130% 100% at 50% 0%, #0d1220, #04050a 70%);
  overflow: hidden;
}
.m-stars {
  position: absolute;
  top: 0;
  bottom: 0;
  /* 宽度是容器的两倍，才能无缝循环 */
  width: 200%;
  background-repeat: repeat;
  will-change: transform;
}
.m-stars-far {
  opacity: calc(0.4 + var(--m-count) * 0.3);
  background-image: radial-gradient(1px 1px at 20px 30px, rgba(255,255,255,0.85), transparent),
                    radial-gradient(1px 1px at 120px 80px, rgba(255,255,255,0.6), transparent),
                    radial-gradient(1px 1px at 210px 150px, rgba(200,225,255,0.8), transparent);
  background-size: 260px 220px;
  animation: m-star-drift var(--m-speed) linear infinite;
}
.m-stars-near {
  opacity: calc(0.5 + var(--m-count) * 0.35);
  background-image: radial-gradient(1.6px 1.6px at 60px 120px, rgba(255,255,255,0.95), transparent),
                    radial-gradient(1.4px 1.4px at 190px 40px, rgba(240,205,114,0.9), transparent);
  background-size: 340px 260px;
  animation: m-star-drift calc(var(--m-speed) * 0.55) linear infinite;
}
@keyframes m-star-drift {
  from { transform: translate3d(0, 0, 0); }
  to { transform: translate3d(-50%, 0, 0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-star-stage">
          <span className="m-stars m-stars-far"></span>
          <span className="m-stars m-stars-near"></span>
        </div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.m-star-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(130% 100% at 50% 0%, #0d1220, #04050a 70%);
  overflow: hidden;
}
.m-stars {
  position: absolute;
  top: 0;
  bottom: 0;
  /* 宽度是容器的两倍，才能无缝循环 */
  width: 200%;
  background-repeat: repeat;
  will-change: transform;
}
.m-stars-far {
  opacity: calc(0.4 + var(--m-count) * 0.3);
  background-image: radial-gradient(1px 1px at 20px 30px, rgba(255,255,255,0.85), transparent),
                    radial-gradient(1px 1px at 120px 80px, rgba(255,255,255,0.6), transparent),
                    radial-gradient(1px 1px at 210px 150px, rgba(200,225,255,0.8), transparent);
  background-size: 260px 220px;
  animation: m-star-drift var(--m-speed) linear infinite;
}
.m-stars-near {
  opacity: calc(0.5 + var(--m-count) * 0.35);
  background-image: radial-gradient(1.6px 1.6px at 60px 120px, rgba(255,255,255,0.95), transparent),
                    radial-gradient(1.4px 1.4px at 190px 40px, rgba(240,205,114,0.9), transparent);
  background-size: 340px 260px;
  animation: m-star-drift calc(var(--m-speed) * 0.55) linear infinite;
}
@keyframes m-star-drift {
  from { transform: translate3d(0, 0, 0); }
  to { transform: translate3d(-50%, 0, 0); }
}
*/', '', '做纯 CSS 星空漂移：两层用多组 radial-gradient 点出星点并 background-repeat，图层宽度设为容器两倍，动画从 translateX(0) 到 translateX(-50%) 无缝循环；近层速度是远层的 1.8 倍形成视差。', 'stars,space,parallax,community,jnicol__particleground', 'COMMUNITY', 'https://github.com/jnicol/particleground', 'MIT', 'READY'),
	('canvas-flow-field', '粒子流场', 'Particle Flow Field', '上千个粒子沿着一层隐形的流场游动，留下淡淡轨迹——把「数据在流动」这种抽象感画出来。', '三维 WebGL', 'AI SaaS', 'Cyber', 'Canvas', 3, 'AI 产品页,数据后台,官网首页', 'load', 94, 86, 84, 84, 88, 'GPU_ENHANCED', '高级视觉效果：依赖 GPU 加速，推荐在桌面设备上查看与使用；移动端启用时可适当减少粒子数量或降低分辨率。', '[{"key": "--m-count", "label": "粒子数", "unit": "", "min": 300, "max": 1600, "step": 100, "default": 800}, {"key": "--m-speed", "label": "流速", "unit": "", "min": 0.3, "max": 2, "step": 0.1, "default": 0.9}]', '', '<div class="motion-root motion-root--bleed">
  <canvas class="m-flow"></canvas>
</div>', '(function () {
  var canvas = document.querySelector(''.m-flow'');
  if (!canvas) { return; }
  var ctx = canvas.getContext(''2d'');
  var root = getComputedStyle(document.documentElement);
  var count = parseInt(root.getPropertyValue(''--m-count''), 10);
  if (isNaN(count)) { count = 800; }
  var speed = parseFloat(root.getPropertyValue(''--m-speed''));
  if (isNaN(speed)) { speed = 0.9; }
  var particles = [];
  var time = 0;

  function resize() {
    var rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    particles = [];
    for (var i = 0; i < count; i++) {
      particles.push({ x: Math.random() * canvas.width, y: Math.random() * canvas.height,
                       life: Math.random() * 160 });
    }
  }

  // 流场：用两层正弦叠加当方向场，避免引入噪声库
  function angleAt(x, y) {
    return Math.sin(x * 0.006 + time * 0.4) * 1.6 + Math.cos(y * 0.005 - time * 0.3) * 1.6;
  }

  function frame() {
    time += 0.006 * speed;
    ctx.fillStyle = ''rgba(4, 5, 10, 0.075)'';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    for (var i = 0; i < particles.length; i++) {
      var p = particles[i];
      var angle = angleAt(p.x, p.y);
      var nx = p.x + Math.cos(angle) * 1.5 * speed;
      var ny = p.y + Math.sin(angle) * 1.5 * speed;
      ctx.strokeStyle = ''rgba(120, 220, 220, '' + (0.05 + (p.life % 40) / 400) + '')'';
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(p.x, p.y);
      ctx.lineTo(nx, ny);
      ctx.stroke();
      p.x = nx; p.y = ny; p.life += 1;
      if (p.life > 160 || nx < 0 || ny < 0 || nx > canvas.width || ny > canvas.height) {
        p.x = Math.random() * canvas.width;
        p.y = Math.random() * canvas.height;
        p.life = 0;
      }
    }
    requestAnimationFrame(frame);
  }

  window.addEventListener(''resize'', resize);
  resize();
  frame();
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-flow {
  display: block;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 0%, #0b0f1a, #04050a 72%);
}', '<template>
    <div class="motion-root motion-root--bleed">
      <canvas class="m-flow"></canvas>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var canvas = document.querySelector(''.m-flow'');
    if (!canvas) { return; }
    var ctx = canvas.getContext(''2d'');
    var root = getComputedStyle(document.documentElement);
    var count = parseInt(root.getPropertyValue(''--m-count''), 10);
    if (isNaN(count)) { count = 800; }
    var speed = parseFloat(root.getPropertyValue(''--m-speed''));
    if (isNaN(speed)) { speed = 0.9; }
    var particles = [];
    var time = 0;

    function resize() {
      var rect = canvas.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
      particles = [];
      for (var i = 0; i < count; i++) {
        particles.push({ x: Math.random() * canvas.width, y: Math.random() * canvas.height,
                         life: Math.random() * 160 });
      }
    }

    // 流场：用两层正弦叠加当方向场，避免引入噪声库
    function angleAt(x, y) {
      return Math.sin(x * 0.006 + time * 0.4) * 1.6 + Math.cos(y * 0.005 - time * 0.3) * 1.6;
    }

    function frame() {
      time += 0.006 * speed;
      ctx.fillStyle = ''rgba(4, 5, 10, 0.075)'';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      for (var i = 0; i < particles.length; i++) {
        var p = particles[i];
        var angle = angleAt(p.x, p.y);
        var nx = p.x + Math.cos(angle) * 1.5 * speed;
        var ny = p.y + Math.sin(angle) * 1.5 * speed;
        ctx.strokeStyle = ''rgba(120, 220, 220, '' + (0.05 + (p.life % 40) / 400) + '')'';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(p.x, p.y);
        ctx.lineTo(nx, ny);
        ctx.stroke();
        p.x = nx; p.y = ny; p.life += 1;
        if (p.life > 160 || nx < 0 || ny < 0 || nx > canvas.width || ny > canvas.height) {
          p.x = Math.random() * canvas.width;
          p.y = Math.random() * canvas.height;
          p.life = 0;
        }
      }
      requestAnimationFrame(frame);
    }

    window.addEventListener(''resize'', resize);
    resize();
    frame();
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-flow {
  display: block;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 0%, #0b0f1a, #04050a 72%);
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var canvas = document.querySelector(''.m-flow'');
      if (!canvas) { return; }
      var ctx = canvas.getContext(''2d'');
      var root = getComputedStyle(document.documentElement);
      var count = parseInt(root.getPropertyValue(''--m-count''), 10);
      if (isNaN(count)) { count = 800; }
      var speed = parseFloat(root.getPropertyValue(''--m-speed''));
      if (isNaN(speed)) { speed = 0.9; }
      var particles = [];
      var time = 0;

      function resize() {
        var rect = canvas.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
        particles = [];
        for (var i = 0; i < count; i++) {
          particles.push({ x: Math.random() * canvas.width, y: Math.random() * canvas.height,
                           life: Math.random() * 160 });
        }
      }

      // 流场：用两层正弦叠加当方向场，避免引入噪声库
      function angleAt(x, y) {
        return Math.sin(x * 0.006 + time * 0.4) * 1.6 + Math.cos(y * 0.005 - time * 0.3) * 1.6;
      }

      function frame() {
        time += 0.006 * speed;
        ctx.fillStyle = ''rgba(4, 5, 10, 0.075)'';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        for (var i = 0; i < particles.length; i++) {
          var p = particles[i];
          var angle = angleAt(p.x, p.y);
          var nx = p.x + Math.cos(angle) * 1.5 * speed;
          var ny = p.y + Math.sin(angle) * 1.5 * speed;
          ctx.strokeStyle = ''rgba(120, 220, 220, '' + (0.05 + (p.life % 40) / 400) + '')'';
          ctx.lineWidth = 1;
          ctx.beginPath();
          ctx.moveTo(p.x, p.y);
          ctx.lineTo(nx, ny);
          ctx.stroke();
          p.x = nx; p.y = ny; p.life += 1;
          if (p.life > 160 || nx < 0 || ny < 0 || nx > canvas.width || ny > canvas.height) {
            p.x = Math.random() * canvas.width;
            p.y = Math.random() * canvas.height;
            p.life = 0;
          }
        }
        requestAnimationFrame(frame);
      }

      window.addEventListener(''resize'', resize);
      resize();
      frame();
    })();
  }, [])

  return (
      <div className="motion-root motion-root--bleed">
        <canvas className="m-flow"></canvas>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-flow {
  display: block;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 0%, #0b0f1a, #04050a 72%);
}
*/', '', '做 Canvas 粒子流场：用两层正弦函数叠出一个方向场 angleAt(x,y)，每帧让每个粒子沿该角度前进 1.5px 并画一条极淡的线段，整帧盖一层 rgba(4,5,10,0.075) 形成拖尾；粒子出界或寿命到 160 帧就重生；粒子数与流速做成可调参数。', 'canvas,particles,flow,generative,community,lettier__3d-game-shaders-for-beginners', 'COMMUNITY', 'https://github.com/lettier/3d-game-shaders-for-beginners', 'NO-LICENSE', 'READY'),
	('three-orbit-cubes', '轨道方块', 'Orbit Cubes', '一组方块在不同倾角的轨道上绕中心运行，金属高光随角度变化——三维页面的标准开场。', '三维 WebGL', 'Game UI', 'Cyber', 'Three.js', 3, '游戏界面,产品发布页,AI 产品页', 'load', 93, 84, 82, 80, 85, 'GPU_ENHANCED', '高级视觉效果：依赖 GPU 加速，推荐在桌面设备上查看与使用；移动端启用时可适当减少粒子数量或降低分辨率。', '[{"key": "--m-speed", "label": "运行速度", "unit": "", "min": 0.2, "max": 2.4, "step": 0.1, "default": 1}, {"key": "--m-count", "label": "方块数量", "unit": "", "min": 4, "max": 12, "step": 1, "default": 7}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-orbit"><span class="m-orbit-hint">Three.js</span></div>
</div>', '// 预览用轻量等效实现：无 CDN 依赖，用 Canvas 画一组绕轨道运行的立方体线框；
// 「代码」页签里给的是真正的 Three.js 版本
(function () {
  var host = document.querySelector(''.m-orbit'');
  if (!host) { return; }
  var canvas = document.createElement(''canvas'');
  host.appendChild(canvas);
  var ctx = canvas.getContext(''2d'');
  var root = getComputedStyle(document.documentElement);
  var speed = parseFloat(root.getPropertyValue(''--m-speed''));
  if (isNaN(speed)) { speed = 1; }
  var count = parseInt(root.getPropertyValue(''--m-count''), 10);
  if (isNaN(count)) { count = 7; }
  var angle = 0;

  function resize() {
    var rect = host.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
  }

  function cube(x, y, size, rot) {
    var cos = Math.cos(rot), sin = Math.sin(rot);
    var half = size / 2;
    var points = [[-half, -half], [half, -half], [half, half], [-half, half]].map(function (p) {
      return [x + p[0] * cos - p[1] * sin, y + p[0] * sin + p[1] * cos];
    });
    ctx.beginPath();
    ctx.moveTo(points[0][0], points[0][1]);
    for (var i = 1; i < points.length; i++) { ctx.lineTo(points[i][0], points[i][1]); }
    ctx.closePath();
    ctx.stroke();
  }

  function frame() {
    angle += 0.012 * speed;
    ctx.fillStyle = ''rgba(4, 5, 10, 0.22)'';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    var cx = canvas.width / 2, cy = canvas.height / 2;
    var base = Math.min(canvas.width, canvas.height) * 0.3;
    for (var i = 0; i < count; i++) {
      var phase = angle * (1 + i * 0.12) + (i * Math.PI * 2) / count;
      var radius = base * (0.55 + (i % 3) * 0.22);
      var x = cx + Math.cos(phase) * radius;
      var y = cy + Math.sin(phase * 0.7) * radius * 0.45;
      var size = 16 + (i % 4) * 7;
      ctx.strokeStyle = i % 2 === 0 ? ''rgba(240, 205, 114, 0.85)'' : ''rgba(94, 234, 212, 0.75)'';
      ctx.lineWidth = 1.4;
      cube(x, y, size, phase);
    }
    ctx.fillStyle = ''rgba(240, 205, 114, 0.9)'';
    ctx.beginPath();
    ctx.arc(cx, cy, 3.4, 0, Math.PI * 2);
    ctx.fill();
    requestAnimationFrame(frame);
  }

  window.addEventListener(''resize'', resize);
  resize();
  frame();
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-orbit {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: radial-gradient(120% 100% at 50% 0%, #0f1424, #04050a 72%);
}
.m-orbit canvas { display: block; width: 100%; height: 100%; }
.m-orbit-hint {
  position: absolute;
  bottom: 12px;
  right: 14px;
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.24em;
  color: rgba(240, 205, 114, 0.5);
}', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-orbit"><span class="m-orbit-hint">Three.js</span></div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  // 预览用轻量等效实现：无 CDN 依赖，用 Canvas 画一组绕轨道运行的立方体线框；
  // 「代码」页签里给的是真正的 Three.js 版本
  (function () {
    var host = document.querySelector(''.m-orbit'');
    if (!host) { return; }
    var canvas = document.createElement(''canvas'');
    host.appendChild(canvas);
    var ctx = canvas.getContext(''2d'');
    var root = getComputedStyle(document.documentElement);
    var speed = parseFloat(root.getPropertyValue(''--m-speed''));
    if (isNaN(speed)) { speed = 1; }
    var count = parseInt(root.getPropertyValue(''--m-count''), 10);
    if (isNaN(count)) { count = 7; }
    var angle = 0;

    function resize() {
      var rect = host.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
    }

    function cube(x, y, size, rot) {
      var cos = Math.cos(rot), sin = Math.sin(rot);
      var half = size / 2;
      var points = [[-half, -half], [half, -half], [half, half], [-half, half]].map(function (p) {
        return [x + p[0] * cos - p[1] * sin, y + p[0] * sin + p[1] * cos];
      });
      ctx.beginPath();
      ctx.moveTo(points[0][0], points[0][1]);
      for (var i = 1; i < points.length; i++) { ctx.lineTo(points[i][0], points[i][1]); }
      ctx.closePath();
      ctx.stroke();
    }

    function frame() {
      angle += 0.012 * speed;
      ctx.fillStyle = ''rgba(4, 5, 10, 0.22)'';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      var cx = canvas.width / 2, cy = canvas.height / 2;
      var base = Math.min(canvas.width, canvas.height) * 0.3;
      for (var i = 0; i < count; i++) {
        var phase = angle * (1 + i * 0.12) + (i * Math.PI * 2) / count;
        var radius = base * (0.55 + (i % 3) * 0.22);
        var x = cx + Math.cos(phase) * radius;
        var y = cy + Math.sin(phase * 0.7) * radius * 0.45;
        var size = 16 + (i % 4) * 7;
        ctx.strokeStyle = i % 2 === 0 ? ''rgba(240, 205, 114, 0.85)'' : ''rgba(94, 234, 212, 0.75)'';
        ctx.lineWidth = 1.4;
        cube(x, y, size, phase);
      }
      ctx.fillStyle = ''rgba(240, 205, 114, 0.9)'';
      ctx.beginPath();
      ctx.arc(cx, cy, 3.4, 0, Math.PI * 2);
      ctx.fill();
      requestAnimationFrame(frame);
    }

    window.addEventListener(''resize'', resize);
    resize();
    frame();
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-orbit {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: radial-gradient(120% 100% at 50% 0%, #0f1424, #04050a 72%);
}
.m-orbit canvas { display: block; width: 100%; height: 100%; }
.m-orbit-hint {
  position: absolute;
  bottom: 12px;
  right: 14px;
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.24em;
  color: rgba(240, 205, 114, 0.5);
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    // 预览用轻量等效实现：无 CDN 依赖，用 Canvas 画一组绕轨道运行的立方体线框；
    // 「代码」页签里给的是真正的 Three.js 版本
    (function () {
      var host = document.querySelector(''.m-orbit'');
      if (!host) { return; }
      var canvas = document.createElement(''canvas'');
      host.appendChild(canvas);
      var ctx = canvas.getContext(''2d'');
      var root = getComputedStyle(document.documentElement);
      var speed = parseFloat(root.getPropertyValue(''--m-speed''));
      if (isNaN(speed)) { speed = 1; }
      var count = parseInt(root.getPropertyValue(''--m-count''), 10);
      if (isNaN(count)) { count = 7; }
      var angle = 0;

      function resize() {
        var rect = host.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
      }

      function cube(x, y, size, rot) {
        var cos = Math.cos(rot), sin = Math.sin(rot);
        var half = size / 2;
        var points = [[-half, -half], [half, -half], [half, half], [-half, half]].map(function (p) {
          return [x + p[0] * cos - p[1] * sin, y + p[0] * sin + p[1] * cos];
        });
        ctx.beginPath();
        ctx.moveTo(points[0][0], points[0][1]);
        for (var i = 1; i < points.length; i++) { ctx.lineTo(points[i][0], points[i][1]); }
        ctx.closePath();
        ctx.stroke();
      }

      function frame() {
        angle += 0.012 * speed;
        ctx.fillStyle = ''rgba(4, 5, 10, 0.22)'';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        var cx = canvas.width / 2, cy = canvas.height / 2;
        var base = Math.min(canvas.width, canvas.height) * 0.3;
        for (var i = 0; i < count; i++) {
          var phase = angle * (1 + i * 0.12) + (i * Math.PI * 2) / count;
          var radius = base * (0.55 + (i % 3) * 0.22);
          var x = cx + Math.cos(phase) * radius;
          var y = cy + Math.sin(phase * 0.7) * radius * 0.45;
          var size = 16 + (i % 4) * 7;
          ctx.strokeStyle = i % 2 === 0 ? ''rgba(240, 205, 114, 0.85)'' : ''rgba(94, 234, 212, 0.75)'';
          ctx.lineWidth = 1.4;
          cube(x, y, size, phase);
        }
        ctx.fillStyle = ''rgba(240, 205, 114, 0.9)'';
        ctx.beginPath();
        ctx.arc(cx, cy, 3.4, 0, Math.PI * 2);
        ctx.fill();
        requestAnimationFrame(frame);
      }

      window.addEventListener(''resize'', resize);
      resize();
      frame();
    })();
  }, [])

  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-orbit"><span className="m-orbit-hint">Three.js</span></div>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-orbit {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  background: radial-gradient(120% 100% at 50% 0%, #0f1424, #04050a 72%);
}
.m-orbit canvas { display: block; width: 100%; height: 100%; }
.m-orbit-hint {
  position: absolute;
  bottom: 12px;
  right: 14px;
  font: 600 10px/1 ui-monospace, monospace;
  letter-spacing: 0.24em;
  color: rgba(240, 205, 114, 0.5);
}
*/', 'import * as THREE from ''three''

// 场景 / 相机 / 渲染器
const scene = new THREE.Scene()
const camera = new THREE.PerspectiveCamera(42, 1, 0.1, 100)
camera.position.set(0, 1.6, 7)

const renderer = new THREE.WebGLRenderer({ antialias: true })
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
renderer.setClearColor(0x04050a, 1)
document.getElementById(''scene'').appendChild(renderer.domElement)

scene.add(new THREE.AmbientLight(0xffffff, 0.4))
const key = new THREE.DirectionalLight(0xf0cd72, 1.6)
key.position.set(3, 5, 4)
scene.add(key)
const rim = new THREE.DirectionalLight(0x5eead4, 0.9)
rim.position.set(-4, -2, -3)
scene.add(rim)

// 中心球 + 一圈方块，方块各有自己的轨道倾角与速度
const core = new THREE.Mesh(
  new THREE.SphereGeometry(0.34, 32, 32),
  new THREE.MeshStandardMaterial({ color: 0xf0cd72, metalness: 0.8, roughness: 0.22 })
)
scene.add(core)

const cubeCount = 7
const orbiters = []
for (let index = 0; index < cubeCount; index += 1) {
  const mesh = new THREE.Mesh(
    new THREE.BoxGeometry(0.26, 0.26, 0.26),
    new THREE.MeshStandardMaterial({
      color: index % 2 === 0 ? 0xf0cd72 : 0x5eead4,
      metalness: 0.7,
      roughness: 0.3,
    })
  )
  mesh.userData.radius = 1.5 + (index % 3) * 0.7
  mesh.userData.speed = 0.6 + index * 0.08
  mesh.userData.tilt = (index / cubeCount) * Math.PI
  scene.add(mesh)
  orbiters.push(mesh)
}

// 尺寸自适应：容器多大，渲染区就多大
function resize() {
  const container = renderer.domElement.parentElement
  const width = container.clientWidth
  const height = container.clientHeight
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}
window.addEventListener(''resize'', resize)
resize()

const clock = new THREE.Clock()
renderer.setAnimationLoop(() => {
  const time = clock.getElapsedTime()
  core.rotation.y = time * 0.4
  orbiters.forEach((mesh) => {
    const { radius, speed, tilt } = mesh.userData
    const angle = time * speed
    mesh.position.set(Math.cos(angle) * radius, Math.sin(angle * 0.7) * 0.6, Math.sin(angle) * radius * 0.5)
    mesh.rotation.set(angle * 0.8, angle, tilt)
  })
  renderer.render(scene, camera)
})', '用 Three.js 做轨道方块：中心一个金属球，周围 7 个方块各自在有倾角的轨道上运行（半径与速度按索引递增）；两盏方向光（金 + 青）做双色高光；每帧更新方块位置与自转，page resize 时同步 renderer 与相机 aspect。', 'three,orbit,3d,cubes,community,pmndrs__react-three-fiber', 'COMMUNITY', 'https://github.com/pmndrs/react-three-fiber', 'MIT', 'READY'),
	('canvas-metaball', '融合球', 'Metaball', '几个圆靠近时像液体一样粘在一起再分开——用模糊 + 阈值滤镜做的「假融合」，Canvas 里几行就够。', '三维 WebGL', 'AI SaaS', 'Organic', 'Canvas', 3, 'AI 产品页,登录页面,产品发布页', 'load', 92, 85, 83, 86, 87, 'GPU_ENHANCED', '高级视觉效果：依赖 GPU 加速，推荐在桌面设备上查看与使用；移动端启用时可适当减少粒子数量或降低分辨率。', '[{"key": "--m-count", "label": "球体数量", "unit": "", "min": 3, "max": 9, "step": 1, "default": 5}, {"key": "--m-speed", "label": "运动速度", "unit": "", "min": 0.2, "max": 2, "step": 0.1, "default": 0.8}]', '', '<div class="motion-root motion-root--bleed">
  <canvas class="m-metaball"></canvas>
</div>', '(function () {
  var canvas = document.querySelector(''.m-metaball'');
  if (!canvas) { return; }
  var ctx = canvas.getContext(''2d'');
  var root = getComputedStyle(document.documentElement);
  var count = parseInt(root.getPropertyValue(''--m-count''), 10);
  if (isNaN(count)) { count = 5; }
  var speed = parseFloat(root.getPropertyValue(''--m-speed''));
  if (isNaN(speed)) { speed = 0.8; }
  var balls = [];
  var time = 0;

  function resize() {
    var rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    balls = [];
    for (var i = 0; i < count; i++) {
      balls.push({
        cx: 0.2 + (i / Math.max(1, count)) * 0.6,
        cy: 0.35 + (i % 3) * 0.16,
        radius: 46 + (i % 4) * 16,
        phase: i * 1.3
      });
    }
  }

  function frame() {
    time += 0.01 * speed;
    var width = canvas.width, height = canvas.height;
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = ''rgba(5, 6, 10, 1)'';
    ctx.fillRect(0, 0, width, height);

    // 关键：把球画进一个带模糊的离屏层，再用 globalCompositeOperation 叠出「融合」的假象
    ctx.save();
    ctx.filter = ''blur(18px)'';
    for (var i = 0; i < balls.length; i++) {
      var ball = balls[i];
      var x = ball.cx * width + Math.cos(time + ball.phase) * width * 0.09;
      var y = ball.cy * height + Math.sin(time * 1.2 + ball.phase) * height * 0.09;
      var gradient = ctx.createRadialGradient(x, y, 0, x, y, ball.radius);
      gradient.addColorStop(0, ''rgba(240, 205, 114, 0.95)'');
      gradient.addColorStop(1, ''rgba(240, 205, 114, 0)'');
      ctx.fillStyle = gradient;
      ctx.beginPath();
      ctx.arc(x, y, ball.radius, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.restore();

    // 对比度拉高，模糊边缘就会被「切」成硬边，看起来像液体融合
    ctx.globalCompositeOperation = ''color-dodge'';
    ctx.fillStyle = ''rgba(30, 30, 40, 0.28)'';
    ctx.fillRect(0, 0, width, height);
    ctx.globalCompositeOperation = ''source-over'';
    requestAnimationFrame(frame);
  }

  window.addEventListener(''resize'', resize);
  resize();
  frame();
})();', '.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-metaball {
  display: block;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 100%, #14101c, #05060a 70%);
}', '<template>
    <div class="motion-root motion-root--bleed">
      <canvas class="m-metaball"></canvas>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  (function () {
    var canvas = document.querySelector(''.m-metaball'');
    if (!canvas) { return; }
    var ctx = canvas.getContext(''2d'');
    var root = getComputedStyle(document.documentElement);
    var count = parseInt(root.getPropertyValue(''--m-count''), 10);
    if (isNaN(count)) { count = 5; }
    var speed = parseFloat(root.getPropertyValue(''--m-speed''));
    if (isNaN(speed)) { speed = 0.8; }
    var balls = [];
    var time = 0;

    function resize() {
      var rect = canvas.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
      balls = [];
      for (var i = 0; i < count; i++) {
        balls.push({
          cx: 0.2 + (i / Math.max(1, count)) * 0.6,
          cy: 0.35 + (i % 3) * 0.16,
          radius: 46 + (i % 4) * 16,
          phase: i * 1.3
        });
      }
    }

    function frame() {
      time += 0.01 * speed;
      var width = canvas.width, height = canvas.height;
      ctx.clearRect(0, 0, width, height);
      ctx.fillStyle = ''rgba(5, 6, 10, 1)'';
      ctx.fillRect(0, 0, width, height);

      // 关键：把球画进一个带模糊的离屏层，再用 globalCompositeOperation 叠出「融合」的假象
      ctx.save();
      ctx.filter = ''blur(18px)'';
      for (var i = 0; i < balls.length; i++) {
        var ball = balls[i];
        var x = ball.cx * width + Math.cos(time + ball.phase) * width * 0.09;
        var y = ball.cy * height + Math.sin(time * 1.2 + ball.phase) * height * 0.09;
        var gradient = ctx.createRadialGradient(x, y, 0, x, y, ball.radius);
        gradient.addColorStop(0, ''rgba(240, 205, 114, 0.95)'');
        gradient.addColorStop(1, ''rgba(240, 205, 114, 0)'');
        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(x, y, ball.radius, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.restore();

      // 对比度拉高，模糊边缘就会被「切」成硬边，看起来像液体融合
      ctx.globalCompositeOperation = ''color-dodge'';
      ctx.fillStyle = ''rgba(30, 30, 40, 0.28)'';
      ctx.fillRect(0, 0, width, height);
      ctx.globalCompositeOperation = ''source-over'';
      requestAnimationFrame(frame);
    }

    window.addEventListener(''resize'', resize);
    resize();
    frame();
  })();
})
</script>

<style scoped>
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-metaball {
  display: block;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 100%, #14101c, #05060a 70%);
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    (function () {
      var canvas = document.querySelector(''.m-metaball'');
      if (!canvas) { return; }
      var ctx = canvas.getContext(''2d'');
      var root = getComputedStyle(document.documentElement);
      var count = parseInt(root.getPropertyValue(''--m-count''), 10);
      if (isNaN(count)) { count = 5; }
      var speed = parseFloat(root.getPropertyValue(''--m-speed''));
      if (isNaN(speed)) { speed = 0.8; }
      var balls = [];
      var time = 0;

      function resize() {
        var rect = canvas.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;
        balls = [];
        for (var i = 0; i < count; i++) {
          balls.push({
            cx: 0.2 + (i / Math.max(1, count)) * 0.6,
            cy: 0.35 + (i % 3) * 0.16,
            radius: 46 + (i % 4) * 16,
            phase: i * 1.3
          });
        }
      }

      function frame() {
        time += 0.01 * speed;
        var width = canvas.width, height = canvas.height;
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = ''rgba(5, 6, 10, 1)'';
        ctx.fillRect(0, 0, width, height);

        // 关键：把球画进一个带模糊的离屏层，再用 globalCompositeOperation 叠出「融合」的假象
        ctx.save();
        ctx.filter = ''blur(18px)'';
        for (var i = 0; i < balls.length; i++) {
          var ball = balls[i];
          var x = ball.cx * width + Math.cos(time + ball.phase) * width * 0.09;
          var y = ball.cy * height + Math.sin(time * 1.2 + ball.phase) * height * 0.09;
          var gradient = ctx.createRadialGradient(x, y, 0, x, y, ball.radius);
          gradient.addColorStop(0, ''rgba(240, 205, 114, 0.95)'');
          gradient.addColorStop(1, ''rgba(240, 205, 114, 0)'');
          ctx.fillStyle = gradient;
          ctx.beginPath();
          ctx.arc(x, y, ball.radius, 0, Math.PI * 2);
          ctx.fill();
        }
        ctx.restore();

        // 对比度拉高，模糊边缘就会被「切」成硬边，看起来像液体融合
        ctx.globalCompositeOperation = ''color-dodge'';
        ctx.fillStyle = ''rgba(30, 30, 40, 0.28)'';
        ctx.fillRect(0, 0, width, height);
        ctx.globalCompositeOperation = ''source-over'';
        requestAnimationFrame(frame);
      }

      window.addEventListener(''resize'', resize);
      resize();
      frame();
    })();
  }, [])

  return (
      <div className="motion-root motion-root--bleed">
        <canvas className="m-metaball"></canvas>
      </div>
  )
}

/* styles.css
.motion-root {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}
.motion-root--bleed { padding: 0; }
.m-metaball {
  display: block;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 100%, #14101c, #05060a 70%);
}
*/', '', '做 Canvas 融合球：把若干个径向渐变的球画在 ctx.filter=''blur(18px)'' 的图层里，再用 globalCompositeOperation=''color-dodge'' 叠一层半透明底色把模糊边缘切成硬边，形成液体融合感；每个球用 cos/sin 做相位不同的漂移，球数与速度可调。', 'metaball,canvas,organic,blur,community,paper-design__shaders', 'COMMUNITY', 'https://github.com/paper-design/shaders', 'Apache-2.0', 'READY')
ON DUPLICATE KEY UPDATE
	`name` = VALUES(`name`), `name_en` = VALUES(`name_en`), `description` = VALUES(`description`),
	`category` = VALUES(`category`), `scene` = VALUES(`scene`), `style` = VALUES(`style`),
	`technology` = VALUES(`technology`), `difficulty` = VALUES(`difficulty`), `best_for` = VALUES(`best_for`),
	`trigger_type` = VALUES(`trigger_type`),
	`score_visual` = VALUES(`score_visual`), `score_code` = VALUES(`score_code`),
	`score_reuse` = VALUES(`score_reuse`), `score_perf` = VALUES(`score_perf`), `score` = VALUES(`score`),
	`runtime_tier` = VALUES(`runtime_tier`), `runtime_note` = VALUES(`runtime_note`),
	`params` = VALUES(`params`), `preview_html` = VALUES(`preview_html`), `preview_js` = VALUES(`preview_js`),
	`css_code` = VALUES(`css_code`), `vue_code` = VALUES(`vue_code`), `react_code` = VALUES(`react_code`),
	`three_code` = VALUES(`three_code`), `prompt` = VALUES(`prompt`), `tags` = VALUES(`tags`),
	`source` = VALUES(`source`), `source_url` = VALUES(`source_url`), `source_license` = VALUES(`source_license`);

-- 官方模板补上触发方式（分类与触发方式在 2.0 一起成为检索维度）
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'smooth-fade';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'slide-up';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'scale-reveal';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'text-reveal';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'blur-reveal';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'hero-entrance';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'login-animation';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'page-transition';
UPDATE `motion_template` SET `trigger_type` = 'hover' WHERE `template_key` = 'magnetic-button';
UPDATE `motion_template` SET `trigger_type` = 'hover' WHERE `template_key` = 'cursor-follow';
UPDATE `motion_template` SET `trigger_type` = 'hover' WHERE `template_key` = 'glow-border';
UPDATE `motion_template` SET `trigger_type` = 'hover' WHERE `template_key` = 'glass-card-hover';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'floating-card';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'card-stagger';
UPDATE `motion_template` SET `trigger_type` = 'hover' WHERE `template_key` = 'pricing-card-hover';
UPDATE `motion_template` SET `trigger_type` = 'click' WHERE `template_key` = 'notification-popup';
UPDATE `motion_template` SET `trigger_type` = 'click' WHERE `template_key` = 'modal-morph';
UPDATE `motion_template` SET `trigger_type` = 'hover' WHERE `template_key` = 'tilt-card-3d';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'dashboard-counter';
UPDATE `motion_template` SET `trigger_type` = 'scroll' WHERE `template_key` = 'image-parallax';
UPDATE `motion_template` SET `trigger_type` = 'scroll' WHERE `template_key` = 'scroll-reveal';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'aurora-background';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'mesh-gradient';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'liquid-gradient';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'galaxy-background';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'noise-texture';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'floating-orb';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'particle-network';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'shader-background';
UPDATE `motion_template` SET `trigger_type` = 'load' WHERE `template_key` = 'three-scene';

-- ---------------------------------------------------------------------
-- 2. 候选池（GitHub 发现 → 规则分析 → 自动初审）
-- ---------------------------------------------------------------------
INSERT INTO `motion_candidate`
	(`candidate_key`, `name`, `full_name`, `source_url`, `description`, `category`, `technology`,
	 `trigger_type`, `difficulty`, `performance_level`, `visual_score`, `license`, `stars`, `language`,
	 `topics`, `matched_hints`, `prompt`, `status`, `review_note`, `pattern_key`, `promoted_template_key`)
VALUES
	('pomber__git-history', 'git-history', 'pomber/git-history', 'https://github.com/pomber/git-history', 'Quickly browse the history of a file from any git repository', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 13685, 'JavaScript', 'animation,blame,cli,commit,git,github,history,log', 'text', '做一段逐字上升的标题动画：把标题拆成单字（每个字一个 span），初始 opacity:0、filter:blur(8px)、translateY(0.42em)，用 cubic-bezier(0.22,0.61,0.36,1) 逐个升起到位；每个字的 animation-delay 按索引递增（0.06s 一档），整体时长 0.8s。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'split-char-rise', 'split-char-rise'),
	('denvercoder1__readme-typing-svg', 'readme-typing-svg', 'DenverCoder1/readme-typing-svg', 'https://github.com/DenverCoder1/readme-typing-svg', '⚡ Dynamically generated, customizable SVG that gives the appearance of typing and deleting text for use on your profile page, repositories, or website.', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 9364, 'PHP', 'animation,dynamic,github,hacktoberfest,php,profile-readme,readme,svg', 'text,typing', '做一段模糊聚焦的文字入场：从 filter:blur(14px)、opacity:0.15、字距 0.08em 过渡到完全清晰，时长 1.2s，缓动 cubic-bezier(0.22,0.61,0.36,1)；模糊半径与时长都做成可调参数。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'blur-focus-in', 'blur-focus-in'),
	('raulriera__textfieldeffects', 'TextFieldEffects', 'raulriera/TextFieldEffects', 'https://github.com/raulriera/TextFieldEffects', 'Custom UITextFields effects inspired by Codrops, built using Swift', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 6014, 'Swift', 'animation,effects,swift,textfield', 'text', '做一个数字翻滚计数器：每一位数字是一个高度 1em 的 overflow:hidden 容器，里面竖排 0-9（每行 line-height:1）；动画只改内部列的 translateY，从 0 到 -62%（即停在数字 6），每位延迟 0.08s 递增，时长 1.6s。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'digit-roll-up', 'digit-roll-up'),
	('cgoldsby__logincritter', 'LoginCritter', 'cgoldsby/LoginCritter', 'https://github.com/cgoldsby/LoginCritter', 'An animated avatar that responds to text field interactions', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 5669, 'Swift', 'animation,swift,uipropertyanimator', 'text', '做一段渐变文字流动：用 linear-gradient(100deg, 金→橙→紫→青) 做 background-clip:text 填充，background-size 设到 220%，动画只改 background-position 从 0% 到 100% 循环，周期 8 秒线性播放。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'gradient-text-flow', 'gradient-text-flow'),
	('hanks-zyh__htextview', 'HTextView', 'hanks-zyh/HTextView', 'https://github.com/hanks-zyh/HTextView', 'Animation effects to text, not really textview', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 5636, 'Java', 'android,animation-effects,animations,textview', 'text', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('pterm__pterm', 'pterm', 'pterm/pterm', 'https://github.com/pterm/pterm', '✨ PTerm is a modern Go module to easily beautify console output. Featuring charts, progressbars, tables, trees, text input, select menus and much more 🚀 It''s completely configurable and 100% cross-platform compatible.', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 5543, 'Go', 'animation,ansi-colors,beautify,console,console-framework,go,golang,golang-library', 'text', '', 'REJECTED', '主要语言是 Go，不是前端动效可直接复用的实现', '', ''),
	('robinhood__ticker', 'ticker', 'robinhood/ticker', 'https://github.com/robinhood/ticker', 'An Android text view with scrolling text change animation', '文字动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 4379, 'Java', 'android,android-animation,android-ui', 'text,scroll', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('peterbrittain__asciimatics', 'asciimatics', 'peterbrittain/asciimatics', 'https://github.com/peterbrittain/asciimatics', 'A cross platform package to do curses-like operations, plus higher level APIs and widgets to create text UIs and ASCII art animations', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 4302, 'Python', 'ascii-art,console,cross-platform,curses,python,terminal,tui', 'text', '若要把它做成 Pattern：先确认它解决的是「A cross platform package to do curses-like operations, plus 」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('alexmacarthur__typeit', 'typeit', 'alexmacarthur/typeit', 'https://github.com/alexmacarthur/typeit', 'The most versatile JavaScript typewriter effect library on the planet.', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'GPL-3.0', 3178, 'JavaScript', 'animation,javascript,javascript-library,text-animation,text-effects,typewriter,typewriter-effect,typography', 'text,typewriter', '做一段纯 CSS 打字机效果：文本容器定宽并用 overflow:hidden，宽度从 0 动画到 9ch，animation-timing-function 用 steps(9, end) 让字符逐格出现；再用伪元素做一根 2px 光标，用 steps(1) 在 0.8s 周期内闪烁。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'typewriter-caret', 'typewriter-caret'),
	('tameemsafi__typewriterjs', 'typewriterjs', 'tameemsafi/typewriterjs', 'https://github.com/tameemsafi/typewriterjs', 'A simple yet powerful native javascript plugin for a cool typewriter effect.', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 2672, 'JavaScript', 'javascript,native-javascript-plugin,typewriter,typewriterjs', 'typewriter', '若要把它做成 Pattern：先确认它解决的是「A simple yet powerful native javascript plugin for a cool ty」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('orhun__daktilo', 'daktilo', 'orhun/daktilo', 'https://github.com/orhun/daktilo', 'Turn your keyboard into a typewriter! 📇', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 1253, 'Rust', 'daktilo,hacktoberfest,keyboard,rust,sound,typewriter,typewriter-animation,typewriter-effect', 'typewriter', '', 'REJECTED', '主要语言是 Rust，不是前端动效可直接复用的实现', '', ''),
	('sgwilym__windups', 'windups', 'sgwilym/windups', 'https://github.com/sgwilym/windups', 'A unique typewriter effect library for React.', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 860, 'TypeScript', 'animation,javascript,react,reactjs,typewriter,typewriter-effect,windups', 'typewriter', '若要把它做成 Pattern：先确认它解决的是「A unique typewriter effect library for React.」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('luca3317__tmpeffects', 'TMPEffects', 'Luca3317/TMPEffects', 'https://github.com/Luca3317/TMPEffects', 'Easily animate Unity text and apply other effects with custom tags', '文字动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 797, 'C#', 'animation,custom-tags,effects,tags,text,text-effects,textmeshpro,tmp', 'text,typewriter,mesh', '', 'REJECTED', '主要语言是 C#，不是前端动效可直接复用的实现', '', ''),
	('cngu__vue-typer', 'vue-typer', 'cngu/vue-typer', 'https://github.com/cngu/vue-typer', 'Vue component that simulates a user typing, selecting, and erasing text.', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 788, 'Vue', 'component,typewriter,vue,vue-component,vue-typer,vue2,vuetyper', 'text,typewriter,typing', '若要把它做成 Pattern：先确认它解决的是「Vue component that simulates a user typing, selecting, and e」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('antfu__retypewriter', 'retypewriter', 'antfu/retypewriter', 'https://github.com/antfu/retypewriter', 'Replay the steps of your changes at ease.', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 755, 'TypeScript', 'diff,typewriter,vscode-extension', 'typewriter', '若要把它做成 Pattern：先确认它解决的是「Replay the steps of your changes at ease.」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('internet-development__www-server-mono', 'www-server-mono', 'internet-development/www-server-mono', 'https://github.com/internet-development/www-server-mono', 'Server Mono is a typeface inspired by typewriters, Apple''s San Francisco Mono, ASCII art, command-line interfaces, and programming tools.', '文字动画', 'CSS', 'load', 2, 'BALANCED', 97, 'OFL-1.1', 751, 'TypeScript', 'font,fonts,monospace,typeface,typeface-design,typefaces', 'typewriter,font', '若要把它做成 Pattern：先确认它解决的是「Server Mono is a typeface inspired by typewriters, Apple''s S」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('mauricenino__dashdot', 'dashdot', 'MauriceNino/dashdot', 'https://github.com/MauriceNino/dashdot', 'A simple, modern server dashboard, primarily used by smaller private servers', '卡片交互', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 3546, 'TypeScript', 'dashboard,docker,glassmorphism,monitoring,nodejs,react,server', 'glass', '若要把它做成 Pattern：先确认它解决的是「A simple, modern server dashboard, primarily used by smaller」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('justadumbprsn__zen-nebula', 'Zen-Nebula', 'JustAdumbPrsn/Zen-Nebula', 'https://github.com/JustAdumbPrsn/Zen-Nebula', 'A minimalist Glassmorphism based theme to elevate the UI of Zen browser', '卡片交互', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'GPL-3.0', 1418, 'CSS', '', 'glass', '若要把它做成 Pattern：先确认它解决的是「A minimalist Glassmorphism based theme to elevate the UI of 」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('naughtyduk__liquidgl', 'liquidGL', 'naughtyduk/liquidGL', 'https://github.com/naughtyduk/liquidGL', 'liquidGL – Liquid Glass - Powered by WebGPU/WebGL', '卡片交互', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'NO-LICENSE', 899, 'JavaScript', 'apple,glass,glassmorphism,interactive,liquid,liquidglass,magnifying-glass,webgl', 'glass,webgl', '若要把它做成 Pattern：先确认它解决的是「liquidGL – Liquid Glass - Powered by WebGPU/WebGL」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('hamza417__peristyle', 'Peristyle', 'Hamza417/Peristyle', 'https://github.com/Hamza417/Peristyle', 'Advance wallpaper manager app for Android with cool glassmorphic UI, tags, auto wallpaper, custom effects and multiple folder support and a native live wallpaper picker.', '卡片交互', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 722, 'Kotlin', 'android,android-app,compose,gallery,glassmorphism,jetpack-compose,jetpackcompose,kotlin', 'glass', '', 'REJECTED', '主要语言是 Kotlin，不是前端动效可直接复用的实现', '', ''),
	('heiehiehi__xinghuisamablogs', 'XinghuisamaBlogs', 'heiehiehi/XinghuisamaBlogs', 'https://github.com/heiehiehi/XinghuisamaBlogs', '这是一个采用 Next.js 构建的高颜值、毛玻璃（Glassmorphism）风格个人博客系统。本项目自带完善的前端展示与独立的本地后台控制台，支持 Markdown 沉浸式写作、草稿管理以及便捷的图床配置。（新增对移动端适配））', '卡片交互', 'CSS', 'load', 2, 'BALANCED', 97, 'NOASSERTION', 692, 'TypeScript', '', 'glass', '若要把它做成 Pattern：先确认它解决的是「这是一个采用 Next.js 构建的高颜值、毛玻璃（Glassmorphism）风格个人博客系统。本项目自带完善的前端展」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('sdegenaar__liquid_glass_widgets', 'liquid_glass_widgets', 'sdegenaar/liquid_glass_widgets', 'https://github.com/sdegenaar/liquid_glass_widgets', 'Flutter UI kit implementing Apple''s iOS 26 Liquid Glass design language - a comprehensive glass widget library with real shader-based blur, physics-driven jelly animations, and dynamic lighting. Works on every platform out of the box.', '卡片交互', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'MIT', 681, 'Dart', 'apple-design,dart,flutter,flutter-widgets,fragment-shader,glassmorphism,impeller,ios26', 'glass,shader', '', 'REJECTED', '主要语言是 Dart，不是前端动效可直接复用的实现', '', ''),
	('muwmx__yumaplayer', 'YumaPlayer', 'MuwMx/YumaPlayer', 'https://github.com/MuwMx/YumaPlayer', '🎵 Hybrid music client for Android: Spotify discovery & UI + YouTube Music library + Hi-Res Lossless (FLAC) streaming with fluid Glassmorphism💫', '卡片交互', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'GPL-3.0', 551, 'Kotlin', 'android,android-ui,audiophile,flac,glassmorphism,jetpack-compose,kotlin,lossless-audio', 'glass', '', 'REJECTED', '主要语言是 Kotlin，不是前端动效可直接复用的实现', '', ''),
	('ybouane__liquidglass', 'liquidglass', 'ybouane/liquidglass', 'https://github.com/ybouane/liquidglass', 'A liquid glass effect library for the web. Apply realistic glass refraction, blur, chromatic aberration, and lighting effects to any HTML element using WebGL shaders.', '三维 WebGL', 'CSS', 'load', 3, 'BALANCED', 96, 'NO-LICENSE', 509, 'TypeScript', 'chromatic-aberration,effect,filter,glass,glassmorphism,ios26-liquid-glass,liquid,refraction', 'glass,webgl,shader', '若要把它做成 Pattern：先确认它解决的是「A liquid glass effect library for the web. Apply realistic g」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('ianlunn__hover', 'Hover', 'IanLunn/Hover', 'https://github.com/IanLunn/Hover', 'A collection of CSS3 powered hover effects to be applied to links, buttons, logos, SVG, featured images and so on. Easily apply to your own elements, modify or just use for inspiration. Available in CSS, Sass, and LESS.', '卡片交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 97, 'NOASSERTION', 29403, 'SCSS', 'css,css-effects,sass', 'hover,button', '做一个玻璃卡片悬停效果：backdrop-filter:blur(14px) + 半透明白底；hover 时 translateY(-8px)、阴影加深、边框变成金色半透明，并用伪元素在卡片上缘画一道 1px 的横向流光（两端透明、中间金色）。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'glass-lift-hover', 'glass-lift-hover'),
	('nolimits4web__atropos', 'atropos', 'nolimits4web/atropos', 'https://github.com/nolimits4web/atropos', 'Stunning touch-friendly 3D parallax hover effects', '卡片交互', 'CSS', 'hover', 2, 'BALANCED', 97, 'MIT', 3605, 'JavaScript', 'cards,effect,gallery,hover,javascript,parallax,portfolio,react', 'card,hover,parallax', '做一个按压回弹的卡片：常态用 box-shadow 画出一条 6px 的「厚度」，:active 时 translateY(6px) 并把厚度收到 0、阴影收紧，transition 0.24s ease-out；松手自动回弹。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'press-depth-card', 'press-depth-card'),
	('gudh__ihover', 'ihover', 'gudh/ihover', 'https://github.com/gudh/ihover', 'iHover is a collection of hover effects using pure CSS, inspired by codrops article, powered by Sass.', '卡片交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 97, 'NO-LICENSE', 3460, 'Pug', '', 'hover', '', 'REJECTED', '是清单 / 合集类仓库，没有可提炼的单一动效模式', '', ''),
	('codrops__hovereffectideas', 'HoverEffectIdeas', 'codrops/HoverEffectIdeas', 'https://github.com/codrops/HoverEffectIdeas', 'Some inspiration and modern ideas for subtle hover effects.', '卡片交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 97, 'NO-LICENSE', 1641, 'CSS', '', 'hover', '若要把它做成 Pattern：先确认它解决的是「Some inspiration and modern ideas for subtle hover effects.」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('miketricking__bootstrap-image-hover', 'bootstrap-image-hover', 'miketricking/bootstrap-image-hover', 'https://github.com/miketricking/bootstrap-image-hover', 'Image hover effects that work with or without bootstrap', '卡片交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 97, 'MIT', 853, 'HTML', 'bootstrap,css,css-hover-effects,hover,hovers,image,imagehover', 'hover', '若要把它做成 Pattern：先确认它解决的是「Image hover effects that work with or without bootstrap」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('sqmw__mfcmouseeffect', 'MFCMouseEffect', 'sqmw/MFCMouseEffect', 'https://github.com/sqmw/MFCMouseEffect', '跨平台鼠标特效与输入可视化引擎：点击/轨迹/滚轮/悬停等效果，输入指示器叠加层，手势→快捷键自动化映射，可扩展 WASM 插件。 Cross-platform mouse effects & input visualization engine: click/trail/scroll/hover effects, indicator overlays, gesture→hotkey automation, extensible WASM plugins.', '卡片交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 97, 'MIT', 839, 'C++', 'automation,cross-platform,gesture-recognition,hotkeys,input-visualization,linux,macos,mouse', 'hover,scroll', '', 'REJECTED', '主要语言是 C++，不是前端动效可直接复用的实现', '', ''),
	('codrops__magneticbuttons', 'MagneticButtons', 'codrops/MagneticButtons', 'https://github.com/codrops/MagneticButtons', 'A set of buttons with a magnetic interaction and a hover effect.', '按钮交互', 'CSS', 'hover', 2, 'BALANCED', 96, 'MIT', 486, 'JavaScript', 'button-animation', 'hover,button,magnetic', '做一个磁吸按钮：外层放一个比按钮大的感应区，pointermove 时算指针相对按钮中心的位移，按钮按 0.32 的系数跟随平移；指针离开感应区时回正，transition 0.45s 带一点弹性。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'magnetic-pull-btn', 'magnetic-pull-btn'),
	('alexwolfe__buttons', 'Buttons', 'alexwolfe/Buttons', 'https://github.com/alexwolfe/Buttons', 'A CSS button library built using Sass and Compass', '按钮交互', 'CSS', 'click', 2, 'BALANCED', 97, 'NOASSERTION', 4995, 'JavaScript', '', 'button', '做箭头滑出的按钮：箭头初始 translateX(-10px) 且 opacity:0，hover 时回到 0 并显现；文字同时向右平移 12px 让位；两者都是 0.36s cubic-bezier(0.22,0.61,0.36,1)。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'arrow-swipe-btn', 'arrow-swipe-btn'),
	('youneslaaroussi__ui-buttons', 'ui-buttons', 'youneslaaroussi/ui-buttons', 'https://github.com/youneslaaroussi/ui-buttons', '100 Modern CSS Buttons. Every style that you can imagine.', '按钮交互', 'CSS', 'click', 1, 'LIGHTWEIGHT', 97, 'MIT', 3844, 'CSS', 'angular,awesome,awesome-list,css,design,design-system,first-timer-friendly,framework', 'button', '若要把它做成 Pattern：先确认它解决的是「100 Modern CSS Buttons. Every style that you can imagine.」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('lipis__bootstrap-social', 'bootstrap-social', 'lipis/bootstrap-social', 'https://github.com/lipis/bootstrap-social', ':couple: Social Sign-In Buttons for Bootstrap', '按钮交互', 'CSS', 'click', 1, 'LIGHTWEIGHT', 97, 'MIT', 2885, 'HTML', 'bootstrap,css,social-buttons', 'button', '若要把它做成 Pattern：先确认它解决的是「:couple: Social Sign-In Buttons for Bootstrap」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('themesberg__flowbite-svelte', 'flowbite-svelte', 'themesberg/flowbite-svelte', 'https://github.com/themesberg/flowbite-svelte', 'Official Svelte components built for Flowbite and Tailwind CSS', '卡片交互', 'CSS', 'click', 1, 'LIGHTWEIGHT', 97, 'MIT', 2784, 'CSS', 'accordion,buttons,cards,components,dark-mode,dropdown,flowbite,forms', 'card,button', '若要把它做成 Pattern：先确认它解决的是「Official Svelte components built for Flowbite and Tailwind C」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('ganapativs__bttn.css', 'bttn.css', 'ganapativs/bttn.css', 'https://github.com/ganapativs/bttn.css', 'Awesome buttons for awesome projects!', '按钮交互', 'CSS', 'click', 1, 'LIGHTWEIGHT', 97, 'MIT', 2051, 'CSS', 'button,css,frontend,interaction-design,library,stylus,ui-design', 'button', '做点击水波：按钮 overflow:hidden，pointerdown 时在点击位置插入一个圆形 span（直径=按钮长边的两倍），从 scale(0) 动画到 scale(1) 同时 opacity 归零，0.62s ease-out，animationend 后移除节点。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'ripple-click-btn', 'ripple-click-btn'),
	('lokesh-coder__pretty-checkbox', 'pretty-checkbox', 'lokesh-coder/pretty-checkbox', 'https://github.com/lokesh-coder/pretty-checkbox', 'A pure CSS library to beautify checkbox and radio buttons.', '按钮交互', 'CSS', 'click', 1, 'LIGHTWEIGHT', 97, 'MIT', 1803, 'CSS', 'animation,bootstrap,checkbox,css,html,icons,radio-buttons,react', 'button', '若要把它做成 Pattern：先确认它解决的是「A pure CSS library to beautify checkbox and radio buttons.」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('saadeghi__theme-change', 'theme-change', 'saadeghi/theme-change', 'https://github.com/saadeghi/theme-change', 'Change CSS theme with toggle, buttons or select using CSS custom properties and localStorage', '按钮交互', 'CSS', 'click', 2, 'BALANCED', 97, 'MIT', 1717, 'JavaScript', 'css-custom-properties,css-theme,css-variables,javascript,localstorage,theme,theming', 'button', '若要把它做成 Pattern：先确认它解决的是「Change CSS theme with toggle, buttons or select using CSS cu」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('jlmakes__scrollreveal', 'scrollreveal', 'jlmakes/scrollreveal', 'https://github.com/jlmakes/scrollreveal', 'Animate elements as they scroll into view.', '滚动动画', 'CSS', 'load', 2, 'BALANCED', 97, 'NO-LICENSE', 22477, 'JavaScript', 'animation,css,javascript,reveal,scroll,scrollreveal,transform,transition', 'scroll,reveal', '做网格错落揭示：卡片初始 opacity:0、translateY(18px) scale(0.98)；用 IntersectionObserver 观察每张卡，进入视口后按 (行号+列号)×80ms 递增延迟加上 .on 类，过渡 0.6s；触发一次后 unobserve。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'stagger-grid-reveal', 'stagger-grid-reveal'),
	('mattdelacdev__wow', 'WOW', 'mattdelacdev/WOW', 'https://github.com/mattdelacdev/WOW', 'Reveal CSS animation as you scroll down a page', '滚动动画', 'CSS', 'load', 2, 'BALANCED', 97, 'NO-LICENSE', 9895, 'JavaScript', '', 'scroll,reveal', '若要把它做成 Pattern：先确认它解决的是「Reveal CSS animation as you scroll down a page」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('rnosov__react-reveal', 'react-reveal', 'rnosov/react-reveal', 'https://github.com/rnosov/react-reveal', 'Easily add reveal on scroll animations to your React app', '滚动动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 2728, 'JavaScript', 'animation,react-components,reveal,scroll-animations', 'scroll,reveal', '若要把它做成 Pattern：先确认它解决的是「Easily add reveal on scroll animations to your React app」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('xtianmiller__emergence.js', 'emergence.js', 'xtianmiller/emergence.js', 'https://github.com/xtianmiller/emergence.js', 'Detect element visibility in the browser', '滚动动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 1864, 'JavaScript', 'animation,element,javascript,reveal,scroll,viewport,visibility', 'scroll,reveal', '若要把它做成 Pattern：先确认它解决的是「Detect element visibility in the browser」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('scroll-out__scroll-out', 'scroll-out', 'scroll-out/scroll-out', 'https://github.com/scroll-out/scroll-out', 'ScrollOut detects changes in scroll for reveal, parallax, and CSS Variable effects!', '滚动动画', 'CSS', 'load', 2, 'BALANCED', 97, 'NO-LICENSE', 1218, 'TypeScript', 'animation,css,css-classes,javascript,scroll-events,scrolling,transition', 'scroll,parallax,reveal', '若要把它做成 Pattern：先确认它解决的是「ScrollOut detects changes in scroll for reveal, parallax, an」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('crazychicken__t-scroll', 't-scroll', 'crazychicken/t-scroll', 'https://github.com/crazychicken/t-scroll', 'A modern reveal-on-scroll library with useful options and animations. (Animate Elements On Reveal)', '滚动动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 631, 'HTML', 'animated,animation,bounce,clgt,css,fade,flip,html', 'flip,scroll,reveal', '若要把它做成 Pattern：先确认它解决的是「A modern reveal-on-scroll library with useful options and an」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('prinzhorn__skrollr', 'skrollr', 'Prinzhorn/skrollr', 'https://github.com/Prinzhorn/skrollr', 'Stand-alone parallax scrolling library for mobile (Android + iOS) and desktop. No jQuery. Just plain JavaScript (and some love).', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'MIT', 18404, 'HTML', '', 'scroll,parallax', '做钉住分屏：外层是 grid 两栏 + overflow-y:auto 的滚动容器；右栏面板用 position:sticky; top:0 钉住，左栏放三段带大间距的说明文字，滚动时右栏不动、左栏依次经过。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'sticky-pin-panel', 'sticky-pin-panel'),
	('wagerfield__parallax', 'parallax', 'wagerfield/parallax', 'https://github.com/wagerfield/parallax', 'Parallax Engine that reacts to the orientation of a smart device', '滚动动画', 'CSS', 'scroll', 2, 'BALANCED', 97, 'NOASSERTION', 16572, 'JavaScript', '', 'parallax', '若要把它做成 Pattern：先确认它解决的是「Parallax Engine that reacts to the orientation of a smart de」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('meliorence__react-native-snap-carousel', 'react-native-snap-carousel', 'meliorence/react-native-snap-carousel', 'https://github.com/meliorence/react-native-snap-carousel', 'Swiper/carousel component for React Native featuring previews, multiple layouts, parallax images, performant handling of huge numbers of items, and more. Compatible with Android & iOS.', '滚动动画', 'CSS', 'scroll', 2, 'BALANCED', 97, 'BSD-3-Clause', 10506, 'JavaScript', 'advanced-effects,carousel,flatlist-based,infinite-scroll,parallax-effect,swiper', 'scroll,parallax', '做顶部阅读进度条：外层是高度 3px 的圆角轨道，内层宽度百分比随滚动容器 scrollTop/(scrollHeight-clientHeight) 更新，width 用 0.1s linear 过渡（滚动时更顺），渐变从金色到青色。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'scroll-progress-bar', 'scroll-progress-bar'),
	('alexfoxy__lax.js', 'lax.js', 'alexfoxy/lax.js', 'https://github.com/alexfoxy/lax.js', 'Simple & lightweight (<4kb gzipped) vanilla JavaScript library to create smooth & beautiful animations when you scroll.', '滚动动画', 'CSS', 'scroll', 2, 'BALANCED', 97, 'MIT', 10472, 'JavaScript', 'animation,css,effects,parallax,scroll,transitions', 'scroll,parallax', '做横向走马轨道：轨道宽度 max-content，把内容复制一份接在后面，动画从 translateX(0) 到 translateX(-50%) 就能无缝循环；两行方向相反，悬停时 animation-play-state:paused。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'auto-track-marquee', 'auto-track-marquee'),
	('locomotivemtl__locomotive-scroll', 'locomotive-scroll', 'locomotivemtl/locomotive-scroll', 'https://github.com/locomotivemtl/locomotive-scroll', '🛤 Detection of elements in viewport & smooth scrolling with parallax.', '滚动动画', 'CSS', 'scroll', 2, 'BALANCED', 97, 'MIT', 8865, 'JavaScript', 'in-view,javascript,parallax,smooth-scrolling', 'scroll,parallax,locomotive', '做三层视差：滚动容器里放三层绝对定位的图层，滚动时分别以 0.18 / 0.36 / 0.62 的系数反向平移（乘一个统一的强度参数），用 transform:translateY 而不是改 top，保证走合成层不掉帧。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'parallax-depth-3', 'parallax-depth-3'),
	('florent37__materialviewpager', 'MaterialViewPager', 'florent37/MaterialViewPager', 'https://github.com/florent37/MaterialViewPager', 'A Material Design ViewPager easy to use library', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 8068, 'Java', 'android,java,kenburnsview,material,materialviewpager,parallax,scroll,toolbar', 'scroll,parallax', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('dixonandmoe__rellax', 'rellax', 'dixonandmoe/rellax', 'https://github.com/dixonandmoe/rellax', 'Lightweight, vanilla javascript parallax library', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'MIT', 7129, 'HTML', '', 'parallax', '若要把它做成 Pattern：先确认它解决的是「Lightweight, vanilla javascript parallax library」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('romaonthego__residemenu', 'RESideMenu', 'romaonthego/RESideMenu', 'https://github.com/romaonthego/RESideMenu', 'iOS 7/8 style side menu with parallax effect.', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'MIT', 7035, 'Objective-C', '', 'parallax', '', 'REJECTED', '主要语言是 Objective-C，不是前端动效可直接复用的实现', '', ''),
	('lecho__hellocharts-android', 'hellocharts-android', 'lecho/hellocharts-android', 'https://github.com/lecho/hellocharts-android', 'Charts library for Android compatible with API 8+, several chart types with scaling, scrolling and animations 📊', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 7567, 'Java', 'android,chart', 'scroll', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('ifttt__jazzhands', 'JazzHands', 'IFTTT/JazzHands', 'https://github.com/IFTTT/JazzHands', 'A simple keyframe-based animation framework for UIKit. Perfect for scrolling app intros.', '滚动动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 6353, 'Objective-C', '', 'scroll,intro', '', 'REJECTED', '主要语言是 Objective-C，不是前端动效可直接复用的实现', '', ''),
	('yarolegovich__discretescrollview', 'DiscreteScrollView', 'yarolegovich/DiscreteScrollView', 'https://github.com/yarolegovich/DiscreteScrollView', 'A scrollable list of items that centers the current element and provides easy-to-use APIs for cool item animations.', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'NO-LICENSE', 5768, 'Java', 'android,android-development,android-library,android-ui,carousel,discrete-scroll,item-picker,layoutmanager', 'scroll', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('mciastek__sal', 'sal', 'mciastek/sal', 'https://github.com/mciastek/sal', '🚀 Performance focused, lightweight scroll animation library 🚀', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'MIT', 3693, 'HTML', '', 'scroll', '若要把它做成 Pattern：先确认它解决的是「🚀 Performance focused, lightweight scroll animation library 」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('setchi__fancyscrollview', 'FancyScrollView', 'setchi/FancyScrollView', 'https://github.com/setchi/FancyScrollView', 'A versatile Unity scroll view component that enables highly flexible animations.', '滚动动画', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'MIT', 3532, 'C#', 'csharp,infinite-scroll,scroller,scrollview,ugui,unity,unity-asset,unity-scripts', 'scroll', '', 'REJECTED', '主要语言是 C#，不是前端动效可直接复用的实现', '', ''),
	('pulkitxm__claude-directory', 'claude-directory', 'pulkitxm/claude-directory', 'https://github.com/pulkitxm/claude-directory', 'Open-source AI interfaces built with Claude (Fable 5) including hero sections, GLSL shaders, design systems, animations, 3D components  and and landing pages,  in React, Tailwind, and Three.js. Plug them into your AI agent and ship faster.', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'MIT', 563, 'HTML', 'ai-generated,anthropic,claude,claude-ai,design-system,frontend,generative-ui,glsl-shaders', 'hero,landing,three,threejs,webgl,glsl', '若要把它做成 Pattern：先确认它解决的是「Open-source AI interfaces built with Claude (Fable 5) includ」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('codebucks27__the-weirdos-nft-website-starter-code', 'The-Weirdos-NFT-Website-Starter-Code', 'codebucks27/The-Weirdos-NFT-Website-Starter-Code', 'https://github.com/codebucks27/The-Weirdos-NFT-Website-Starter-Code', 'Build a cool NFT Collection website landing page with React JS . This website is created using Gsap for cool scrolling and animation. If you want to learn how to create this website then you can follow below tutorial link in the ReadMe.', '滚动动画', 'CSS', 'scroll', 2, 'BALANCED', 96, 'NO-LICENSE', 431, 'JavaScript', 'beginner-project,gsap,gsap-scrolltrigger,gsap3,landing-page,nft,nft-gallery,react', 'scroll,landing', '若要把它做成 Pattern：先确认它解决的是「Build a cool NFT Collection website landing page with React 」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('herotransitions__hero', 'Hero', 'HeroTransitions/Hero', 'https://github.com/HeroTransitions/Hero', 'Elegant transition library for iOS & tvOS', '首屏动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 22490, 'Swift', 'animation,animations,carthage,custom-transitions,hero,ios,ios-animation,ios-framework', 'hero', '做幕布分屏入场：两片各占 50% 宽的深色幕布盖在首屏上，分别向左/右 translateX(±100%) 拉开（1.1s，cubic-bezier(0.76,0,0.24,1)）；内容在幕布拉开 0.35s 后淡入上移 14px。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'hero-split-curtain', 'hero-split-curtain'),
	('snail-z__dawntransition', 'DawnTransition', 'snail-z/DawnTransition', 'https://github.com/snail-z/DawnTransition', 'DawnTransition is a lightweight iOS transition framework for smooth, customizable animations and native-like interactive swipe-back gestures. It solves common gesture conflicts in custom transitions and is proven in real projects.', '首屏动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 1502, 'Swift', 'animation,custom-transitions,gesture-back,hero,interactive-transition,swipe-back,ui-animation,view-controller-transition', 'hero', '做产品浮现入场：产品卡从 translateY(64px) scale(0.94) 且带 6px 模糊的状态浮到原位，1.4s cubic-bezier(0.22,0.61,0.36,1)，阴影同步从散到聚。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'product-float-in', 'product-float-in'),
	('jeffersonlicet__react-motion-layout', 'react-motion-layout', 'jeffersonlicet/react-motion-layout', 'https://github.com/jeffersonlicet/react-motion-layout', '🦸 Beautiful immersive React hero animations.', '首屏动画', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 586, 'JavaScript', 'animate,animations,react,react-router,reactjs,transitions', 'hero', '做标题擦除入场：标题 clip-path 从 inset(0 100% 0 0) 动画到 inset(0 0 0 0)；同时一条 2px 的青色发光竖条从左侧扫到右侧并淡出，两者同为 1.1s cubic-bezier(0.65,0,0.35,1)。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'headline-clip-wipe', 'headline-clip-wipe'),
	('putraxor__flutter-login-ui', 'flutter-login-ui', 'putraxor/flutter-login-ui', 'https://github.com/putraxor/flutter-login-ui', 'Create a clean and simple login UI screen with a basic hero animation in Flutter', '首屏动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 95, 'NO-LICENSE', 356, 'Dart', 'dart,flutter,hero-animation', 'hero', '', 'REJECTED', '主要语言是 Dart，不是前端动效可直接复用的实现', '', ''),
	('letsar__flutter_sidekick', 'flutter_sidekick', 'letsar/flutter_sidekick', 'https://github.com/letsar/flutter_sidekick', 'Widgets for creating Hero-like animations between two widgets within the same screen.', '首屏动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 95, 'MIT', 296, 'Dart', 'animation,dart,flutter,flutter-widget,hero', 'hero', '做滚动提示：一个 5px 的金色圆点沿 y 轴上下跳 8px（1.7s cubic-bezier(0.4,0,0.2,1) 无限循环，同时透明度 0.5↔1），下方接一条 42px 高、向下渐隐的竖线。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'scroll-hint-bounce', 'scroll-hint-bounce'),
	('swiftui-lab__swiftui-hero-animations', 'swiftui-hero-animations', 'swiftui-lab/swiftui-hero-animations', 'https://github.com/swiftui-lab/swiftui-hero-animations', 'An Example on how to create SwiftUI hero animations, using matchedGeometryEffect.', '首屏动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 94, 'MIT', 249, 'Swift', '', 'hero', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', ''),
	('kaomei__kid-papercraft', 'kid-papercraft', 'kaomei/kid-papercraft', 'https://github.com/kaomei/kid-papercraft', 'Turn any child''s birthday into a magical 30-second stop-motion origami video with popular animation heroes and Gemini Omni Flash.', '首屏动画', 'CSS', 'load', 1, 'LIGHTWEIGHT', 94, 'MIT', 225, '', '', 'hero', '若要把它做成 Pattern：先确认它解决的是「Turn any child''s birthday into a magical 30-second stop-moti」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，星数一般，人工筛选时优先看前几条', '', ''),
	('lunarlogic__auroral', 'auroral', 'LunarLogic/auroral', 'https://github.com/LunarLogic/auroral', 'Animated background gradients with pure CSS', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 95, 'MIT', 287, 'CSS', '', 'background,gradient,aurora', '若要把它做成 Pattern：先确认它解决的是「Animated background gradients with pure CSS」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，星数一般，人工筛选时优先看前几条', '', ''),
	('tg-tg-tg-tg-tg-tg__aurora-for-chatgpt', 'Aurora-for-ChatGPT', 'TG-TG-TG-TG-TG-TG/Aurora-for-ChatGPT', 'https://github.com/TG-TG-TG-TG-TG-TG/Aurora-for-ChatGPT', 'Ambient blurred background under the ChatGPT UI with a chat visibility toggle and legacy composer option. Not affiliated with OpenAI.', '背景效果', 'CSS', 'click', 2, 'BALANCED', 92, 'MIT', 116, 'JavaScript', '', 'background,aurora', '', 'REJECTED', '星数偏低（116），案例参考价值不足', '', ''),
	('tsparticles__tsparticles', 'tsparticles', 'tsparticles/tsparticles', 'https://github.com/tsparticles/tsparticles', 'tsParticles - Easily create highly customizable JavaScript particles effects, confetti explosions and fireworks animations and use them as animated backgrounds for your website. Ready to use components available for React.js, Vue.js (2.x and 3.x), Angular, Svelte, jQuery, Preact, Inferno, Solid, Rio', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 8984, 'TypeScript', '2d,angular,animations,bg,canvas,confetti,fireworks,hacktoberfest', 'background,particle', '做颗粒噪点叠加：用内联 SVG 的 feTurbulence（baseFrequency 0.85）当噪点贴图，铺满一层 opacity 0.14 的覆盖层；用 steps(4) 让它在 0.6 秒内跳四个位移，形成胶片颗粒感。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'grain-overlay-bg', 'grain-overlay-bg'),
	('jnicol__particleground', 'particleground', 'jnicol/particleground', 'https://github.com/jnicol/particleground', 'A jQuery plugin for snazzy background particle systems', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 2151, 'JavaScript', '', 'background,particle', '做纯 CSS 星空漂移：两层用多组 radial-gradient 点出星点并 background-repeat，图层宽度设为容器两倍，动画从 translateX(0) 到 translateX(-50%) 无缝循环；近层速度是远层的 1.8 倍形成视差。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'starfield-drift-bg', 'starfield-drift-bg'),
	('marcbruederlin__particles.js', 'particles.js', 'marcbruederlin/particles.js', 'https://github.com/marcbruederlin/particles.js', 'A lightweight, dependency-free and responsive javascript plugin for particle backgrounds.', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 1668, 'JavaScript', 'animated,javascript,particle-backgrounds,particles,responsive,vanilla-javascript', 'background,particle', '若要把它做成 Pattern：先确认它解决的是「A lightweight, dependency-free and responsive javascript plu」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('creotip__vue-particles', 'vue-particles', 'creotip/vue-particles', 'https://github.com/creotip/vue-particles', 'Vue.js component for particles backgrounds ✨', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'NO-LICENSE', 1435, 'JavaScript', 'particles-backgrounds,vue,vue-component,vue-components,vue-particles,vue2,vuejs,vuejs2', 'background,particle', '若要把它做成 Pattern：先确认它解决的是「Vue.js component for particles backgrounds ✨」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('lindelof__particles-bg', 'particles-bg', 'lindelof/particles-bg', 'https://github.com/lindelof/particles-bg', 'React particles animation background component', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'NO-LICENSE', 670, 'JavaScript', 'backgrounds,login-template,react-background,react-background-component,react-bg,react-component,react-particle,react-particles', 'background,particle', '若要把它做成 Pattern：先确认它解决的是「React particles animation background component」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('jgagneastro__coffeegrindsize', 'coffeegrindsize', 'jgagneastro/coffeegrindsize', 'https://github.com/jgagneastro/coffeegrindsize', 'Detects the individual coffee grounds in a white-background picture to determine particle size distribution', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 551, 'Python', '', 'background,particle', '若要把它做成 Pattern：先确认它解决的是「Detects the individual coffee grounds in a white-background 」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('lettier__3d-game-shaders-for-beginners', '3d-game-shaders-for-beginners', 'lettier/3d-game-shaders-for-beginners', 'https://github.com/lettier/3d-game-shaders-for-beginners', '🎮 A step-by-step guide to implementing SSAO, depth of field, lighting, normal mapping, and more for your 3D game.', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'NO-LICENSE', 19921, 'C++', '3d,3d-graphics,game-development,gamedev,glsl,glsl-shader,glsl-shaders,godot', 'webgl,glsl,shader', '做 Canvas 粒子流场：用两层正弦函数叠出一个方向场 angleAt(x,y)，每帧让每个粒子沿该角度前进 1.5px 并画一条极淡的线段，整帧盖一层 rgba(4,5,10,0.075) 形成拖尾；粒子出界或寿命到 160 帧就重生；粒子数与流速做成可调参数。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'canvas-flow-field', 'canvas-flow-field'),
	('terkelg__awesome-creative-coding', 'awesome-creative-coding', 'terkelg/awesome-creative-coding', 'https://github.com/terkelg/awesome-creative-coding', 'Creative Coding: Generative Art, Data visualization, Interaction Design, Resources.', '三维 WebGL', 'CSS', 'load', 3, 'BALANCED', 97, 'NO-LICENSE', 15368, 'HTML', '3d-graphics,art,awesome,awesome-list,computer-graphics,creative-coding,data-visualization,design', 'webgl,shader', '', 'REJECTED', '是清单 / 合集类仓库，没有可提炼的单一动效模式', '', ''),
	('mengto__threeui', 'threeui', 'MengTo/threeui', 'https://github.com/MengTo/threeui', 'Open-source ThreeUI Community catalog with live interactive components and complete Community source.', '三维 WebGL', 'Three.js', 'load', 3, 'GPU_ENHANCED', 97, 'MIT', 6227, 'HTML', 'react,shaders,threejs,ui-components,webgl', 'three,threejs,webgl,shader', '若要把它做成 Pattern：先确认它解决的是「Open-source ThreeUI Community catalog with live interactive 」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('patriciogonzalezvivo__glslviewer', 'glslViewer', 'patriciogonzalezvivo/glslViewer', 'https://github.com/patriciogonzalezvivo/glslViewer', 'Console-based GLSL Sandbox for 2D/3D shaders', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'BSD-3-Clause', 5334, 'C++', 'c,c-plus-plus,console,fragment-shader,geometry,glfw,glsl,glslviewer', 'text,webgl,glsl,shader', '', 'REJECTED', '主要语言是 C++，不是前端动效可直接复用的实现', '', ''),
	('jagenjo__webglstudio.js', 'webglstudio.js', 'jagenjo/webglstudio.js', 'https://github.com/jagenjo/webglstudio.js', 'A full open source 3D graphics editor in the browser, with scene editor, coding pad, graph editor, virtual file system, and many features more.', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'MIT', 5325, 'JavaScript', '3d,editor,graph-editor,rendering,scene-graph,shaders,webapp,webgl', 'webgl,shader', '若要把它做成 Pattern：先确认它解决的是「A full open source 3D graphics editor in the browser, with s」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('gfxfundamentals__webgl-fundamentals', 'webgl-fundamentals', 'gfxfundamentals/webgl-fundamentals', 'https://github.com/gfxfundamentals/webgl-fundamentals', 'WebGL lessons that start with the basics', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'NOASSERTION', 5022, 'HTML', '3d,3d-math,glsl,glsl-shaders,math,shaders,webgl', 'webgl,glsl,shader', '若要把它做成 Pattern：先确认它解决的是「WebGL lessons that start with the basics」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('davidhdev__canvas-ui', 'canvas-ui', 'DavidHDev/canvas-ui', 'https://github.com/DavidHDev/canvas-ui', 'A library of creative canvas components. Real HTML with WebGL effects running over it. React, Vue, Svelte, vanilla.', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'NOASSERTION', 4707, 'TypeScript', 'animation,canvas,component-library,components,creative-coding,design-engineering,frontend,glsl', 'three,threejs,webgl,glsl,shader', '若要把它做成 Pattern：先确认它解决的是「A library of creative canvas components. Real HTML with WebG」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('paper-design__shaders', 'shaders', 'paper-design/shaders', 'https://github.com/paper-design/shaders', 'Zero-dependency canvas shaders that can be installed from npm or designed in Paper', '三维 WebGL', 'Canvas', 'load', 3, 'GPU_ENHANCED', 97, 'Apache-2.0', 3487, 'TypeScript', 'react,shaders,webgl', 'webgl,shader', '做 Canvas 融合球：把若干个径向渐变的球画在 ctx.filter=''blur(18px)'' 的图层里，再用 globalCompositeOperation=''color-dodge'' 叠一层半透明底色把模糊边缘切成硬边，形成液体融合感；每个球用 cos/sin 做相位不同的漂移，球数与速度可调。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'canvas-metaball', 'canvas-metaball'),
	('pmndrs__react-three-fiber', 'react-three-fiber', 'pmndrs/react-three-fiber', 'https://github.com/pmndrs/react-three-fiber', '🇨🇭 A React renderer for Three.js', '三维 WebGL', 'Three.js', 'load', 2, 'GPU_ENHANCED', 97, 'MIT', 32538, 'TypeScript', '3d,animation,fiber,react,renderer,threejs', 'three,threejs', '用 Three.js 做轨道方块：中心一个金属球，周围 7 个方块各自在有倾角的轨道上运行（半径与速度按索引递增）；两盏方向光（金 + 青）做双色高光；每帧更新方块位置与自转，page resize 时同步 renderer 与相机 aspect。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'three-orbit-cubes', 'three-orbit-cubes'),
	('img2threejs__img2threejs', 'img2threejs', 'img2threejs/img2threejs', 'https://github.com/img2threejs/img2threejs', 'Rebuild the object in a reference image as a code-only, procedural, quality-gated, animation-ready Three.js model. Token-efficient image-to-3D.', '三维 WebGL', 'Three.js', 'load', 3, 'GPU_ENHANCED', 97, 'Apache-2.0', 16935, 'Python', '3d,ai-agents,claude-code,computer-graphics,generative,image-to-3d,procedural-generation,threejs', 'three,threejs,webgl', '若要把它做成 Pattern：先确认它解决的是「Rebuild the object in a reference image as a code-only, proc」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('theatre-js__theatre', 'theatre', 'theatre-js/theatre', 'https://github.com/theatre-js/theatre', 'Motion design editor for the web', '三维 WebGL', 'Three.js', 'load', 2, 'GPU_ENHANCED', 97, 'Apache-2.0', 12703, 'TypeScript', 'animation,devtools,generative-art,motion-design,r3f,threejs', 'three,threejs', '若要把它做成 Pattern：先确认它解决的是「Motion design editor for the web」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('adrianhajdin__project_3d_developer_portfolio', 'project_3D_developer_portfolio', 'adrianhajdin/project_3D_developer_portfolio', 'https://github.com/adrianhajdin/project_3D_developer_portfolio', 'The most impressive websites in the world use 3D graphics and animations to bring their content to life. Learn how to build your own ThreeJS 3D Developer Portfolio today!', '三维 WebGL', 'Three.js', 'load', 2, 'GPU_ENHANCED', 97, 'NO-LICENSE', 7128, 'JavaScript', '3d,reactjs,threejs', 'three,threejs', '若要把它做成 Pattern：先确认它解决的是「The most impressive websites in the world use 3D graphics an」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('tengbao__vanta', 'vanta', 'tengbao/vanta', 'https://github.com/tengbao/vanta', 'Animated 3D backgrounds for your website', '三维 WebGL', 'Three.js', 'load', 2, 'GPU_ENHANCED', 97, 'MIT', 7077, 'JavaScript', '3d,animation,animations,background,three-js,threejs', 'background,three,threejs', '若要把它做成 Pattern：先确认它解决的是「Animated 3D backgrounds for your website」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('wasasquatch__was-node-suite-comfyui', 'was-node-suite-comfyui', 'WASasquatch/was-node-suite-comfyui', 'https://github.com/WASasquatch/was-node-suite-comfyui', 'WAS-NS Reborn; Tools for image processing, filters, masking, text, logic, numbers, latents, files, 3D scenes, and animation.', '三维 WebGL', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 1854, 'Python', 'animation,comfyui,document-processing,filter,hdr-image,image-manipulation,image-processing,layers', 'text,noise,three,threejs', '若要把它做成 Pattern：先确认它解决的是「WAS-NS Reborn; Tools for image processing, filters, masking,」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('fireship-io__threejs-scroll-animation-demo', 'threejs-scroll-animation-demo', 'fireship-io/threejs-scroll-animation-demo', 'https://github.com/fireship-io/threejs-scroll-animation-demo', '3D Scrolling Portfolio Website with Three.js', '三维 WebGL', 'CSS', 'scroll', 1, 'LIGHTWEIGHT', 97, 'NO-LICENSE', 1668, 'HTML', '', 'scroll,three,threejs', '若要把它做成 Pattern：先确认它解决的是「3D Scrolling Portfolio Website with Three.js」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('tholman__cursor-effects', 'cursor-effects', 'tholman/cursor-effects', 'https://github.com/tholman/cursor-effects', 'Old-school cursor effects for your browser built with modern JavaScript', '按钮交互', 'CSS', 'hover', 2, 'BALANCED', 97, 'NO-LICENSE', 4064, 'JavaScript', '90s,canvas,javascript,library', 'cursor', '若要把它做成 Pattern：先确认它解决的是「Old-school cursor effects for your browser built with modern」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('sahaj-b__ghostty-cursor-shaders', 'ghostty-cursor-shaders', 'sahaj-b/ghostty-cursor-shaders', 'https://github.com/sahaj-b/ghostty-cursor-shaders', 'Custom cursor shaders for ghostty (trails and ripple/pulse effects)', '按钮交互', 'Canvas', 'hover', 1, 'GPU_ENHANCED', 97, 'MIT', 1134, 'GLSL', '', 'ripple,cursor,shader', '做呼吸发光的 CTA 按钮：用伪元素做光晕，keyframes 在 0%/50%/100% 之间交替两层 box-shadow（一层向外扩 10px 并淡出、一层模糊半径放大到 1.8 倍），2.6s ease-in-out 无限循环。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'glow-pulse-cta', 'glow-pulse-cta'),
	('cuberto__mouse-follower', 'mouse-follower', 'Cuberto/mouse-follower', 'https://github.com/Cuberto/mouse-follower', 'A powerful javascript library to create amazing and smooth effects for the mouse cursor on your website.', '按钮交互', 'CSS', 'hover', 2, 'BALANCED', 97, 'MIT', 818, 'JavaScript', '', 'cursor', '若要把它做成 Pattern：先确认它解决的是「A powerful javascript library to create amazing and smooth e」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('syi0808__screenize', 'screenize', 'syi0808/screenize', 'https://github.com/syi0808/screenize', '(Development of Screenize is currently paused) Open-source macOS screen recording app with auto-zoom, cursor effects, and timeline editing — Screen Studio alternative.', '按钮交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 604, 'Swift', '', 'cursor', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', ''),
	('dshongphuc__magic-mouse-js', 'magic-mouse-js', 'dshongphuc/magic-mouse-js', 'https://github.com/dshongphuc/magic-mouse-js', 'A lightweight javascript library to create some amazing effects for the mouse (cursor) on your website - MagicMouse.js : https://magicmousejs.web.app/', '按钮交互', 'CSS', 'hover', 2, 'BALANCED', 96, 'MIT', 424, 'JavaScript', 'css,javascript', 'cursor', '若要把它做成 Pattern：先确认它解决的是「A lightweight javascript library to create some amazing effe」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('chaos-xxl__zelda-hyrule-ui', 'zelda-hyrule-ui', 'chaos-xxl/zelda-hyrule-ui', 'https://github.com/chaos-xxl/zelda-hyrule-ui', '🗡️ Zelda BOTW style React UI component library — 84 components with Sheikah glow effects, dark theme, and AI-consumable design specs (Cursor/v0 ready)', '按钮交互', 'CSS', 'hover', 1, 'LIGHTWEIGHT', 95, 'NOASSERTION', 337, 'HTML', '', 'cursor', '若要把它做成 Pattern：先确认它解决的是「🗡️ Zelda BOTW style React UI component library — 84 componen」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，星数一般，人工筛选时优先看前几条', '', ''),
	('sarcadass__granim.js', 'granim.js', 'sarcadass/granim.js', 'https://github.com/sarcadass/granim.js', 'Create fluid and interactive gradient animations with this small javascript library.', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 5304, 'JavaScript', 'animation,fluid,gradient', 'gradient', '做网格渐变漂移背景：三个不同颜色的大圆（金/紫/青）绝对定位并加 blur(60px) 融合，各自用 translate3d + scale 做 22 秒的错位漂移，中心压一层带字距的标题；只用 transform 保证走合成层。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'mesh-drift-bg', 'mesh-drift-bg'),
	('cruisediary__pastel', 'Pastel', 'cruisediary/Pastel', 'https://github.com/cruisediary/Pastel', '🎨 Gradient animation effect like Instagram', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 3516, 'Swift', 'animation,gradient,instagram,pastel,swift', 'gradient', '做多光球漂浮：四个尺寸递减的圆，各自用 radial-gradient 画高光（外圈透明），加 blur(18px) 与 mix-blend-mode:screen；用不同的周期与相位做 translate3d 漂浮，营造柔光氛围。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'multi-orb-float', 'multi-orb-float'),
	('abhinandan-kushwaha__react-native-gifted-charts', 'react-native-gifted-charts', 'Abhinandan-Kushwaha/react-native-gifted-charts', 'https://github.com/Abhinandan-Kushwaha/react-native-gifted-charts', 'The most loved library for Bar, Line, Area, Pie, Donut, Stacked Bar, Population Pyramid, Radar, Bubble, Scatter and Candle Stick charts in React Native. Allows 2D, 3D, gradient, animations and live data updates.', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 1373, 'TypeScript', 'area-chart,bar,bar-chart,barchart,bubble,candlestick-chart,charts,data-visualization', 'gradient', '若要把它做成 Pattern：先确认它解决的是「The most loved library for Bar, Line, Area, Pie, Donut, Stac」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('csolanam__skeletonui', 'SkeletonUI', 'CSolanaM/SkeletonUI', 'https://github.com/CSolanaM/SkeletonUI', '☠️ Elegant skeleton loading animation in lightweight SwiftUI', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 965, 'Swift', 'animation,cocoapods,combine,gradient,ios,loading,loading-animation,loading-indicator', 'gradient', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', ''),
	('cctanfujun__progressroundbutton', 'ProgressRoundButton', 'cctanfujun/ProgressRoundButton', 'https://github.com/cctanfujun/ProgressRoundButton', 'A DownloadProgressButton with Animation for Android', '按钮交互', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'Apache-2.0', 876, 'Java', 'android,gradient', 'button,gradient', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('pkissling__clock-weather-card', 'clock-weather-card', 'pkissling/clock-weather-card', 'https://github.com/pkissling/clock-weather-card', 'A Home Assistant Card indicating today''s date/time, along with an iOS inspired weather forecast for the next days with animated icons', '卡片交互', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 875, 'TypeScript', 'animated,animation,bar,clock,date,forecast,gradient,icons', 'card,gradient', '做一个光斑跟随卡：外层用 radial-gradient 画一圈亮边（位置由 --m-x/--m-y 控制），内层是深色圆角面板；pointermove 时把指针在卡片内的百分比写进 CSS 变量，内层再用一个同位置的径向渐变做柔光。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'spotlight-card', 'spotlight-card'),
	('tonnyl__spark', 'Spark', 'TonnyL/Spark', 'https://github.com/TonnyL/Spark', '🎨 An Android library to create gradient animation like Instagram&Spotify', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 666, 'Kotlin', 'android,android-library,animation,gradient,gradient-animation,instagram,kotlin,spotify', 'gradient', '', 'REJECTED', '主要语言是 Kotlin，不是前端动效可直接复用的实现', '', ''),
	('taishi-y__instagramlikecolortransitionandroid', 'InstagramLikeColorTransitionAndroid', 'Taishi-Y/InstagramLikeColorTransitionAndroid', 'https://github.com/Taishi-Y/InstagramLikeColorTransitionAndroid', 'How to create instagram like Gradient color transition in android.📸', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 591, 'Java', 'android,animation,gradient-color-transition,instagram,transition', 'gradient', '', 'REJECTED', '主要语言是 Java，不是前端动效可直接复用的实现', '', ''),
	('connoratherton__loaders.css', 'loaders.css', 'ConnorAtherton/loaders.css', 'https://github.com/ConnorAtherton/loaders.css', 'Delightful, performance-focused pure css loading animations.', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'NO-LICENSE', 10225, 'CSS', '', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('jh3y__whirl', 'whirl', 'jh3y/whirl', 'https://github.com/jh3y/whirl', 'CSS loading animations with minimal effort!', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 1836, 'SCSS', 'animation,css,css-loading-animations,hacktoberfest,hacktoberfest2020,loader,loading,loading-animation', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('loadingio__css-spinner', 'css-spinner', 'loadingio/css-spinner', 'https://github.com/loadingio/css-spinner', 'small, elegant pure css spinner for ajax or loading animation', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'NO-LICENSE', 1766, 'JavaScript', 'ajax,css,html,icon,loader,loading,preloader,spinner', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('vineethtrv__css-loader', 'css-loader', 'vineethtrv/css-loader', 'https://github.com/vineethtrv/css-loader', 'This is a library having a collection of different types of CSS loaders, spinners', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 1735, 'CSS', 'animation,css3,loading-spinner,spinners', '', '', 'REJECTED', '是清单 / 合集类仓库，没有可提炼的单一动效模式', '', ''),
	('zalog__placeholder-loading', 'placeholder-loading', 'zalog/placeholder-loading', 'https://github.com/zalog/placeholder-loading', 'Simple and flexible, css only, content placeholder loading animation. https://zalog.github.io/placeholder-loading/', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 1480, 'HTML', 'animation,content-loading,loading,placeholder', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('nzbin__three-dots', 'three-dots', 'nzbin/three-dots', 'https://github.com/nzbin/three-dots', '🔮 CSS loading animations made with single element.', '三维 WebGL', 'Three.js', 'load', 1, 'GPU_ENHANCED', 97, 'MIT', 1343, 'SCSS', 'less,loading-animations,sass,single-element-css-spinners,three-dots', 'three', '若要把它做成 Pattern：先确认它解决的是「🔮 CSS loading animations made with single element.」这类需求，再用 CSS/Canvas 自己实现一遍，不要复制源码。', 'ANALYZED', '规则分析已完成，等待人工筛选', '', ''),
	('alexjoverm__v-lazy-image', 'v-lazy-image', 'alexjoverm/v-lazy-image', 'https://github.com/alexjoverm/v-lazy-image', 'Lazy load images using Intersection Observer, apply progressive rendering and css animations.', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 987, 'JavaScript', 'javascript,lazy-loading,vue,web-performance', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('codrops__gridloadingeffects', 'GridLoadingEffects', 'codrops/GridLoadingEffects', 'https://github.com/codrops/GridLoadingEffects', 'Some inspiration for loading effects of grid items using CSS animations.', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 96, 'NO-LICENSE', 513, 'HTML', '', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('ramotion__animated-tab-bar', 'animated-tab-bar', 'Ramotion/animated-tab-bar', 'https://github.com/Ramotion/animated-tab-bar', ':octocat: RAMAnimatedTabBarController is a Swift UI module library for adding animation to iOS tabbar items and icons. iOS library made by @Ramotion', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 11082, 'Swift', 'animation,ios,library,material-design,swift,ui', '', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', ''),
	('ramotion__folding-cell', 'folding-cell', 'Ramotion/folding-cell', 'https://github.com/Ramotion/folding-cell', ':octocat: 📃 FoldingCell is an expanding content cell with animation made by @Ramotion', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 10174, 'Swift', 'animation,ios,library,material-design,swift,ui', '', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', ''),
	('chenyilong__cyltabbarcontroller', 'CYLTabBarController', 'ChenYilong/CYLTabBarController', 'https://github.com/ChenYilong/CYLTabBarController', '[EN]It is an iOS UI module library for adding animation to iOS tabbar items and icons with Lottie, Liquid Glass and adding a bigger center UITabBar Item.  [CN]【中国特色 TabBar】一行代码实现 Lottie 动画TabBar，支持中间带+号的TabBar样式，自带红点角标，支持动态刷新。【iOS27 & Dark Mode & iPhone 17 tested】', '卡片交互', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 7035, 'Swift', 'animation,cocoapods,darkmode,estabbar,estabbarcontroller,estabbarcontroller-swift,ios,ios26-liquid-glass', 'glass', '做一张悬停翻面的卡片：容器 perspective:1000px，卡片 transform-style:preserve-3d 并在 hover 时 rotateY(180deg)；正反两面都用 backface-visibility:hidden 叠在一起，背面预先 rotateY(180deg)；时长 0.7s。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'flip-reveal-card', 'flip-reveal-card'),
	('ibelick__motion-primitives', 'motion-primitives', 'ibelick/motion-primitives', 'https://github.com/ibelick/motion-primitives', 'UI kit to make beautiful, animated interfaces, faster. Customizable. Open Source.', '背景效果', 'CSS', 'load', 2, 'BALANCED', 97, 'MIT', 6387, 'TypeScript', 'animate,animated,animation,components,framer-motion,library,motion,tailwindcss', '', '', 'REJECTED', '关键词分析没命中任何一类，无法归档', '', ''),
	('exyte__macaw', 'Macaw', 'exyte/Macaw', 'https://github.com/exyte/Macaw', 'Powerful and easy-to-use vector graphics Swift library with SVG support', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 6045, 'Swift', 'animation,drawing,graphics,ios,ios-animation,svg,swift,transition', '', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', ''),
	('ramotion__expanding-collection', 'expanding-collection', 'Ramotion/expanding-collection', 'https://github.com/Ramotion/expanding-collection', ':octocat: ExpandingCollection is an animated material design UI card peek/pop controller. iOS library made by @Ramotion', '卡片交互', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 5508, 'Swift', 'animation,component,library,material-design,swift,ui', 'card', '做一个 3D 倾斜反光卡：容器加 perspective:900px，卡片 transform-style:preserve-3d；pointermove 时按指针在卡片内的相对位置算 rotateX/rotateY（最大 ±12deg），并用径向渐变把高光位置同步到 --m-gx/--m-gy；指针离开时回正。', 'PROMOTED', '已转为 Motion Pattern 并进入资源库：实现为 Kingdom Studio 原创，来源仅作标注', 'tilt-glare-card', 'tilt-glare-card'),
	('ramotion__swift-ui-animation-components-and-libraries', 'swift-ui-animation-components-and-libraries', 'Ramotion/swift-ui-animation-components-and-libraries', 'https://github.com/Ramotion/swift-ui-animation-components-and-libraries', 'Swift UI libraries, iOS components and animations by @Ramotion', '背景效果', 'CSS', 'load', 1, 'LIGHTWEIGHT', 97, 'MIT', 3719, 'Swift', 'ios,libraries,library,swift', '', '', 'REJECTED', '主要语言是 Swift，不是前端动效可直接复用的实现', '', '')
ON DUPLICATE KEY UPDATE
	`name` = VALUES(`name`), `full_name` = VALUES(`full_name`), `source_url` = VALUES(`source_url`),
	`description` = VALUES(`description`), `license` = VALUES(`license`), `stars` = VALUES(`stars`),
	`language` = VALUES(`language`), `topics` = VALUES(`topics`);
