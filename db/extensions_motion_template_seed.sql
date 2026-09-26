-- =====================================================================
-- Kingdom Studio · Motion Lab 官方模板集合（自动生成，请勿手工编辑）
--
-- 生成器：_gh/mt_build.py（数据源 _gh/mt_data1.py + mt_data2.py）
-- 内容：30 个官方模板（基础交互 10 / 产品页面 10 / 高级效果 10）+ 5 个组合方案 + 评分记录
-- 幂等：按 template_key / recipe_key / target_key upsert，可重复执行
-- =====================================================================

USE `kingdom_studio`;

-- 连接字符集按 utf8mb4 显式设置：Windows 下 mysql 客户端默认可能是 GBK，
-- 那样导入含中文的脚本会报 Incorrect string value，这里先把它定死，脚本换台机器也能直接跑。
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- 1. 官方模板
-- ---------------------------------------------------------------------
INSERT INTO `motion_template`
	(`template_key`, `name`, `name_en`, `description`, `category`, `scene`, `style`, `technology`,
	 `difficulty`, `best_for`, `score_visual`, `score_code`, `score_reuse`, `score_perf`, `score`,
	 `runtime_tier`, `runtime_note`,	 `params`, `preview_url`, `preview_html`, `preview_js`, `css_code`, `vue_code`, `react_code`,
	 `three_code`, `prompt`, `tags`, `source`, `status`)
VALUES
	('smooth-fade', '柔和淡入', 'Smooth Fade', '最基础的入场：透明度 0 到 1，只配一条舒缓的缓动，不加位移与缩放，适合不想抢戏的内容区。', '基础交互', 'Landing Page', 'Minimal', 'CSS', 1, '官网首页,后台系统,个人作品集', 78, 96, 95, 98, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.3, "max": 3, "step": 0.1, "default": 0.9}, {"key": "--m-delay", "label": "延迟", "unit": "s", "min": 0, "max": 1.5, "step": 0.05, "default": 0}]', '', '<div class="motion-root">
  <div class="m-el"></div>
  <div class="m-el"></div>
  <div class="m-el"></div>
</div>', '', '.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-fade var(--m-duration) var(--m-easing) var(--m-delay) both;
}
.m-el:nth-child(2) { animation-delay: calc(var(--m-delay) + 0.12s); }
.m-el:nth-child(3) { animation-delay: calc(var(--m-delay) + 0.24s); }

@keyframes m-fade {
  from { opacity: 0; }
  to { opacity: 1; }
}', '<template>
    <div class="motion-root">
      <div class="m-el"></div>
      <div class="m-el"></div>
      <div class="m-el"></div>
    </div>
</template>

<style scoped>
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-fade var(--m-duration) var(--m-easing) var(--m-delay) both;
}
.m-el:nth-child(2) { animation-delay: calc(var(--m-delay) + 0.12s); }
.m-el:nth-child(3) { animation-delay: calc(var(--m-delay) + 0.24s); }

@keyframes m-fade {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-el"></div>
        <div className="m-el"></div>
        <div className="m-el"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-fade var(--m-duration) var(--m-easing) var(--m-delay) both;
}
.m-el:nth-child(2) { animation-delay: calc(var(--m-delay) + 0.12s); }
.m-el:nth-child(3) { animation-delay: calc(var(--m-delay) + 0.24s); }

@keyframes m-fade {
  from { opacity: 0; }
  to { opacity: 1; }
}
*/', '', '用纯 CSS 实现一组元素的柔和淡入：元素从 opacity 0 过渡到 1，时长 0.9 秒、缓动 cubic-bezier(0.22,0.61,0.36,1)，三个元素依次延迟 0.12 秒出现。时长与延迟用 CSS 变量暴露，便于调参。', 'smooth fade', 'OFFICIAL', 'READY'),
	('scale-reveal', '缩放揭示', 'Scale Reveal', '从 0.92 放大到 1 并同时淡入，比纯淡入更有「出现」的实感，适合卡片与产品图。', '基础交互', 'Portfolio', 'Luxury', 'CSS', 1, '官网首页,个人作品集', 84, 94, 92, 96, 91, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.3, "max": 2, "step": 0.1, "default": 0.8}, {"key": "--m-scale", "label": "起始缩放", "unit": "", "min": 0.7, "max": 1, "step": 0.01, "default": 0.92}]', '', '<div class="motion-root">
  <div class="m-el"></div>
  <div class="m-el"></div>
  <div class="m-el"></div>
</div>', '', '.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-scale var(--m-duration) var(--m-easing) both;
  transform-origin: 50% 60%;
}
.m-el:nth-child(2) { animation-delay: 0.1s; }
.m-el:nth-child(3) { animation-delay: 0.2s; }

@keyframes m-scale {
  from { opacity: 0; transform: scale(var(--m-scale)) translateY(8px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}', '<template>
    <div class="motion-root">
      <div class="m-el"></div>
      <div class="m-el"></div>
      <div class="m-el"></div>
    </div>
</template>

<style scoped>
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-scale var(--m-duration) var(--m-easing) both;
  transform-origin: 50% 60%;
}
.m-el:nth-child(2) { animation-delay: 0.1s; }
.m-el:nth-child(3) { animation-delay: 0.2s; }

@keyframes m-scale {
  from { opacity: 0; transform: scale(var(--m-scale)) translateY(8px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-el"></div>
        <div className="m-el"></div>
        <div className="m-el"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-scale var(--m-duration) var(--m-easing) both;
  transform-origin: 50% 60%;
}
.m-el:nth-child(2) { animation-delay: 0.1s; }
.m-el:nth-child(3) { animation-delay: 0.2s; }

@keyframes m-scale {
  from { opacity: 0; transform: scale(var(--m-scale)) translateY(8px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
*/', '', '用纯 CSS 做缩放揭示：元素从 scale 0.92、透明度 0、下移 8px，过渡到原尺寸与原位置；时长 0.8 秒、缓动 ease-out 风格，三个元素依次延迟 0.1 秒。', 'scale reveal', 'OFFICIAL', 'READY'),
	('slide-up', '向上滑入', 'Slide Up', '从下方 28px 滑入并淡入，最通用的入场方式；位移距离用变量控制，移动端可调小。', '基础交互', 'Landing Page', 'Minimal', 'CSS', 1, '官网首页,后台系统,个人作品集', 80, 96, 96, 98, 92, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.3, "max": 2, "step": 0.1, "default": 0.85}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 8, "max": 80, "step": 2, "default": 28}]', '', '<div class="motion-root">
  <div class="m-el"></div>
  <div class="m-el"></div>
  <div class="m-el"></div>
</div>', '', '.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-slide var(--m-duration) var(--m-easing) both;
}
.m-el:nth-child(2) { animation-delay: 0.1s; }
.m-el:nth-child(3) { animation-delay: 0.2s; }

@keyframes m-slide {
  from { opacity: 0; transform: translateY(var(--m-distance)); }
  to { opacity: 1; transform: translateY(0); }
}', '<template>
    <div class="motion-root">
      <div class="m-el"></div>
      <div class="m-el"></div>
      <div class="m-el"></div>
    </div>
</template>

<style scoped>
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-slide var(--m-duration) var(--m-easing) both;
}
.m-el:nth-child(2) { animation-delay: 0.1s; }
.m-el:nth-child(3) { animation-delay: 0.2s; }

@keyframes m-slide {
  from { opacity: 0; transform: translateY(var(--m-distance)); }
  to { opacity: 1; transform: translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-el"></div>
        <div className="m-el"></div>
        <div className="m-el"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-slide var(--m-duration) var(--m-easing) both;
}
.m-el:nth-child(2) { animation-delay: 0.1s; }
.m-el:nth-child(3) { animation-delay: 0.2s; }

@keyframes m-slide {
  from { opacity: 0; transform: translateY(var(--m-distance)); }
  to { opacity: 1; transform: translateY(0); }
}
*/', '', '用纯 CSS 做向上滑入：元素从 translateY(28px)、opacity 0 进入，时长 0.85 秒，缓动使用带轻微回弹的 ease-out，多个元素依次延迟 0.1 秒。', 'slide up', 'OFFICIAL', 'READY'),
	('blur-reveal', '模糊恢复', 'Blur Reveal', '从模糊 14px 逐渐对焦到清晰，配合淡入。看起来「更贵」，但 blur 开销较高，元素多时要节制。', '基础交互', 'AI SaaS', 'Glass', 'CSS', 2, '官网首页,AI 产品页', 88, 90, 88, 82, 87, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.4, "max": 2.5, "step": 0.1, "default": 1.1}]', '', '<div class="motion-root">
  <div class="m-el"></div>
  <div class="m-el"></div>
  <div class="m-el"></div>
</div>', '', '.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-blur var(--m-duration) var(--m-easing) both;
  will-change: filter, opacity;
}
.m-el:nth-child(2) { animation-delay: 0.14s; }
.m-el:nth-child(3) { animation-delay: 0.28s; }

@keyframes m-blur {
  from { opacity: 0; filter: blur(14px); transform: translateY(10px); }
  to { opacity: 1; filter: blur(0); transform: translateY(0); }
}', '<template>
    <div class="motion-root">
      <div class="m-el"></div>
      <div class="m-el"></div>
      <div class="m-el"></div>
    </div>
</template>

<style scoped>
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-blur var(--m-duration) var(--m-easing) both;
  will-change: filter, opacity;
}
.m-el:nth-child(2) { animation-delay: 0.14s; }
.m-el:nth-child(3) { animation-delay: 0.28s; }

@keyframes m-blur {
  from { opacity: 0; filter: blur(14px); transform: translateY(10px); }
  to { opacity: 1; filter: blur(0); transform: translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-el"></div>
        <div className="m-el"></div>
        <div className="m-el"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-blur var(--m-duration) var(--m-easing) both;
  will-change: filter, opacity;
}
.m-el:nth-child(2) { animation-delay: 0.14s; }
.m-el:nth-child(3) { animation-delay: 0.28s; }

@keyframes m-blur {
  from { opacity: 0; filter: blur(14px); transform: translateY(10px); }
  to { opacity: 1; filter: blur(0); transform: translateY(0); }
}
*/', '', '用纯 CSS 做模糊恢复入场：从 filter blur(14px)、opacity 0、下移 10px，过渡到完全清晰；时长 1.1 秒，缓动 ease-out，元素之间错开 0.14 秒。注意只在少量元素上使用，避免性能问题。', 'blur reveal', 'OFFICIAL', 'READY'),
	('magnetic-button', '磁吸按钮', 'Magnetic Button', '指针靠近时按钮向指针方向轻微位移并在离开时弹回，手感来自 JS 计算偏移 + CSS 过渡。', '基础交互', 'Landing Page', 'Cyber', 'CSS', 2, '官网首页,AI 产品页,个人作品集', 90, 88, 90, 90, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-distance", "label": "吸力距离", "unit": "px", "min": 4, "max": 40, "step": 1, "default": 16}, {"key": "--m-duration", "label": "回弹时长", "unit": "s", "min": 0.1, "max": 1, "step": 0.02, "default": 0.35}]', '', '<div class="motion-root">
  <button class="m-magnet" type="button">Hover me</button>
</div>', '.motion-root.addEventListener(''pointermove'', function (event) {
  var button = event.target.closest(''.m-magnet'');
  if (!button) { return; }
  var rect = button.getBoundingClientRect();
  var dx = event.clientX - (rect.left + rect.width / 2);
  var dy = event.clientY - (rect.top + rect.height / 2);
  var distance = 16;
  button.style.setProperty(''--mx'', Math.max(-distance, Math.min(distance, dx * 0.35)) + ''px'');
  button.style.setProperty(''--my'', Math.max(-distance, Math.min(distance, dy * 0.35)) + ''px'');
  button.classList.add(''is-active'');
});
.motion-root.addEventListener(''pointerleave'', function () {
  document.querySelectorAll(''.m-magnet'').forEach(function (button) {
    button.style.setProperty(''--mx'', ''0px'');
    button.style.setProperty(''--my'', ''0px'');
    button.classList.remove(''is-active'');
  });
});', '.m-magnet {
  --mx: 0px;
  --my: 0px;
  padding: 16px 34px;
  border: 0;
  border-radius: 999px;
  font: 600 15px/1 system-ui, "PingFang SC", sans-serif;
  color: #0b0b0f;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  box-shadow: 0 14px 32px rgba(240, 205, 114, 0.28);
  transform: translate3d(var(--mx), var(--my), 0);
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1), box-shadow var(--m-duration) ease;
}
.m-magnet.is-active { box-shadow: 0 20px 44px rgba(240, 205, 114, 0.45); }', '<template>
    <div class="motion-root">
      <button class="m-magnet" type="button">Hover me</button>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  .motion-root.addEventListener(''pointermove'', function (event) {
    var button = event.target.closest(''.m-magnet'');
    if (!button) { return; }
    var rect = button.getBoundingClientRect();
    var dx = event.clientX - (rect.left + rect.width / 2);
    var dy = event.clientY - (rect.top + rect.height / 2);
    var distance = 16;
    button.style.setProperty(''--mx'', Math.max(-distance, Math.min(distance, dx * 0.35)) + ''px'');
    button.style.setProperty(''--my'', Math.max(-distance, Math.min(distance, dy * 0.35)) + ''px'');
    button.classList.add(''is-active'');
  });
  .motion-root.addEventListener(''pointerleave'', function () {
    document.querySelectorAll(''.m-magnet'').forEach(function (button) {
      button.style.setProperty(''--mx'', ''0px'');
      button.style.setProperty(''--my'', ''0px'');
      button.classList.remove(''is-active'');
    });
  });
})
</script>

<style scoped>
.m-magnet {
  --mx: 0px;
  --my: 0px;
  padding: 16px 34px;
  border: 0;
  border-radius: 999px;
  font: 600 15px/1 system-ui, "PingFang SC", sans-serif;
  color: #0b0b0f;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  box-shadow: 0 14px 32px rgba(240, 205, 114, 0.28);
  transform: translate3d(var(--mx), var(--my), 0);
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1), box-shadow var(--m-duration) ease;
}
.m-magnet.is-active { box-shadow: 0 20px 44px rgba(240, 205, 114, 0.45); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    .motion-root.addEventListener(''pointermove'', function (event) {
      var button = event.target.closest(''.m-magnet'');
      if (!button) { return; }
      var rect = button.getBoundingClientRect();
      var dx = event.clientX - (rect.left + rect.width / 2);
      var dy = event.clientY - (rect.top + rect.height / 2);
      var distance = 16;
      button.style.setProperty(''--mx'', Math.max(-distance, Math.min(distance, dx * 0.35)) + ''px'');
      button.style.setProperty(''--my'', Math.max(-distance, Math.min(distance, dy * 0.35)) + ''px'');
      button.classList.add(''is-active'');
    });
    .motion-root.addEventListener(''pointerleave'', function () {
      document.querySelectorAll(''.m-magnet'').forEach(function (button) {
        button.style.setProperty(''--mx'', ''0px'');
        button.style.setProperty(''--my'', ''0px'');
        button.classList.remove(''is-active'');
      });
    });
  }, [])

  return (
      <div className="motion-root">
        <button className="m-magnet" type="button">Hover me</button>
      </div>
  )
}

/* styles.css
.m-magnet {
  --mx: 0px;
  --my: 0px;
  padding: 16px 34px;
  border: 0;
  border-radius: 999px;
  font: 600 15px/1 system-ui, "PingFang SC", sans-serif;
  color: #0b0b0f;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  box-shadow: 0 14px 32px rgba(240, 205, 114, 0.28);
  transform: translate3d(var(--mx), var(--my), 0);
  transition: transform var(--m-duration) cubic-bezier(0.22, 0.61, 0.36, 1), box-shadow var(--m-duration) ease;
}
.m-magnet.is-active { box-shadow: 0 20px 44px rgba(240, 205, 114, 0.45); }
*/', '', '做一个磁吸按钮：指针在按钮周围移动时，按钮朝指针方向最多偏移 16px（系数 0.35），离开后弹回原位；位移动画 0.35 秒、ease-out 缓动，激活时阴影加深。用 CSS 变量 --mx/--my 承载偏移。', 'magnetic button', 'OFFICIAL', 'READY'),
	('glow-border', '流光描边', 'Glow Border', '用锥形渐变沿边框旋转形成流动光带，靠 mask 把渐变裁成 1px 描边，不额外增加 DOM。', '基础交互', 'AI SaaS', 'Cyber', 'CSS', 2, 'AI 产品页,后台系统', 92, 86, 88, 86, 88, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "旋转周期", "unit": "s", "min": 1, "max": 8, "step": 0.5, "default": 3.6}, {"key": "--m-color", "label": "光带颜色", "unit": "", "min": 0, "max": 1, "step": 0.01, "default": 0.5}]', '', '<div class="motion-root">
  <div class="m-ring"><span class="m-ring-label">AI SaaS</span></div>
</div>', '', '.m-ring {
  position: relative;
  width: 220px;
  height: 120px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: rgba(12, 13, 18, 0.9);
  color: #e8eaed;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  isolation: isolate;
}
.m-ring::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  padding: 1px;
  background: conic-gradient(from 0deg, transparent 0 55%, rgba(240, 205, 114, 0.95) 75%, transparent 85% 100%);
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: m-spin var(--m-duration) linear infinite;
  z-index: -1;
}

@keyframes m-spin {
  to { transform: rotate(1turn); }
}', '<template>
    <div class="motion-root">
      <div class="m-ring"><span class="m-ring-label">AI SaaS</span></div>
    </div>
</template>

<style scoped>
.m-ring {
  position: relative;
  width: 220px;
  height: 120px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: rgba(12, 13, 18, 0.9);
  color: #e8eaed;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  isolation: isolate;
}
.m-ring::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  padding: 1px;
  background: conic-gradient(from 0deg, transparent 0 55%, rgba(240, 205, 114, 0.95) 75%, transparent 85% 100%);
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: m-spin var(--m-duration) linear infinite;
  z-index: -1;
}

@keyframes m-spin {
  to { transform: rotate(1turn); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-ring"><span className="m-ring-label">AI SaaS</span></div>
      </div>
  )
}

/* styles.css
.m-ring {
  position: relative;
  width: 220px;
  height: 120px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: rgba(12, 13, 18, 0.9);
  color: #e8eaed;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  isolation: isolate;
}
.m-ring::before {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  padding: 1px;
  background: conic-gradient(from 0deg, transparent 0 55%, rgba(240, 205, 114, 0.95) 75%, transparent 85% 100%);
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: m-spin var(--m-duration) linear infinite;
  z-index: -1;
}

@keyframes m-spin {
  to { transform: rotate(1turn); }
}
*/', '', '给卡片加流动的描边光带：用 conic-gradient 做一段高亮弧，再用 mask 的 xor 合成把渐变裁成 1px 边框，整体以 3.6 秒一圈旋转。不增加额外 DOM，边框随卡片圆角。', 'glow border', 'OFFICIAL', 'READY'),
	('glass-card-hover', '玻璃卡片悬停', 'Glass Card Hover', '毛玻璃面板悬停时上浮、边框变亮、内部高光扫过；blur 只作用在卡片自身，代价可控。', '基础交互', 'Portfolio', 'Glass', 'CSS', 2, '个人作品集,官网首页,AI 产品页', 89, 92, 94, 84, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.2, "max": 1.2, "step": 0.05, "default": 0.45}, {"key": "--m-distance", "label": "上浮", "unit": "px", "min": 2, "max": 24, "step": 1, "default": 8}]', '', '<div class="motion-root">
  <div class="m-glass"><span>Glass</span></div>
</div>', '', '.m-glass {
  position: relative;
  width: 240px;
  height: 150px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  color: #eef1f5;
  font: 600 15px/1 system-ui, "PingFang SC", sans-serif;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.04));
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  overflow: hidden;
  transition: transform var(--m-duration) var(--m-easing), border-color var(--m-duration) ease, box-shadow var(--m-duration) ease;
}
.m-glass::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(115deg, transparent 35%, rgba(255, 255, 255, 0.34) 50%, transparent 65%);
  transform: translateX(-120%);
  transition: transform calc(var(--m-duration) * 1.7) ease;
}
.m-glass:hover {
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.55);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.5);
}
.m-glass:hover::after { transform: translateX(120%); }', '<template>
    <div class="motion-root">
      <div class="m-glass"><span>Glass</span></div>
    </div>
</template>

<style scoped>
.m-glass {
  position: relative;
  width: 240px;
  height: 150px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  color: #eef1f5;
  font: 600 15px/1 system-ui, "PingFang SC", sans-serif;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.04));
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  overflow: hidden;
  transition: transform var(--m-duration) var(--m-easing), border-color var(--m-duration) ease, box-shadow var(--m-duration) ease;
}
.m-glass::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(115deg, transparent 35%, rgba(255, 255, 255, 0.34) 50%, transparent 65%);
  transform: translateX(-120%);
  transition: transform calc(var(--m-duration) * 1.7) ease;
}
.m-glass:hover {
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.55);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.5);
}
.m-glass:hover::after { transform: translateX(120%); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-glass"><span>Glass</span></div>
      </div>
  )
}

/* styles.css
.m-glass {
  position: relative;
  width: 240px;
  height: 150px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  color: #eef1f5;
  font: 600 15px/1 system-ui, "PingFang SC", sans-serif;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.04));
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  overflow: hidden;
  transition: transform var(--m-duration) var(--m-easing), border-color var(--m-duration) ease, box-shadow var(--m-duration) ease;
}
.m-glass::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(115deg, transparent 35%, rgba(255, 255, 255, 0.34) 50%, transparent 65%);
  transform: translateX(-120%);
  transition: transform calc(var(--m-duration) * 1.7) ease;
}
.m-glass:hover {
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.55);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.5);
}
.m-glass:hover::after { transform: translateX(120%); }
*/', '', '做一张玻璃拟态卡片：半透明背景 + backdrop-filter blur(14px) + 1px 亮边框；悬停时上浮 8px、边框变为金色，并有一道高光从左到右扫过。过渡 0.45 秒。', 'glass card hover', 'OFFICIAL', 'READY'),
	('floating-card', '悬浮卡片', 'Floating Card', '极慢的上下浮动（6 秒一轮），给静态卡片一点呼吸感；位移很小，不影响阅读。', '基础交互', 'Portfolio', 'Organic', 'CSS', 1, '个人作品集,官网首页', 82, 94, 90, 94, 89, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "浮动周期", "unit": "s", "min": 2, "max": 12, "step": 0.5, "default": 6}, {"key": "--m-distance", "label": "幅度", "unit": "px", "min": 2, "max": 24, "step": 1, "default": 10}]', '', '<div class="motion-root">
  <div class="m-el"></div>
  <div class="m-el"></div>
  <div class="m-el"></div>
</div>', '', '.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-float var(--m-duration) ease-in-out infinite;
}
.m-el:nth-child(2) { animation-delay: calc(var(--m-duration) / -3); }
.m-el:nth-child(3) { animation-delay: calc(var(--m-duration) / -1.5); }

@keyframes m-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(calc(var(--m-distance) * -1)); }
}', '<template>
    <div class="motion-root">
      <div class="m-el"></div>
      <div class="m-el"></div>
      <div class="m-el"></div>
    </div>
</template>

<style scoped>
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-float var(--m-duration) ease-in-out infinite;
}
.m-el:nth-child(2) { animation-delay: calc(var(--m-duration) / -3); }
.m-el:nth-child(3) { animation-delay: calc(var(--m-duration) / -1.5); }

@keyframes m-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(calc(var(--m-distance) * -1)); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-el"></div>
        <div className="m-el"></div>
        <div className="m-el"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.m-el {
  animation: m-float var(--m-duration) ease-in-out infinite;
}
.m-el:nth-child(2) { animation-delay: calc(var(--m-duration) / -3); }
.m-el:nth-child(3) { animation-delay: calc(var(--m-duration) / -1.5); }

@keyframes m-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(calc(var(--m-distance) * -1)); }
}
*/', '', '让几张卡片轻微悬浮：上下位移 10px、周期 6 秒、ease-in-out 循环，三个卡片用负延迟错开相位，看起来像各自在呼吸而不是整齐地一起动。', 'floating card', 'OFFICIAL', 'READY'),
	('text-reveal', '文字揭示', 'Text Reveal', '遮罩从下往上抽出，文字像被「掀开」而不是淡入；用 clip-path 实现，无需逐字切分 DOM。', '基础交互', 'Landing Page', 'Minimal', 'CSS', 2, '官网首页,个人作品集', 86, 92, 88, 92, 89, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.3, "max": 2, "step": 0.1, "default": 1}, {"key": "--m-delay", "label": "延迟", "unit": "s", "min": 0, "max": 1.5, "step": 0.05, "default": 0.1}]', '', '<div class="motion-root">
  <h2 class="m-title">Motion Lab</h2>
</div>', '', '.m-title {
  margin: 0;
  font: 700 40px/1.1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.02em;
  color: #f4f6f8;
  clip-path: inset(0 0 100% 0);
  transform: translateY(6px);
  animation: m-reveal var(--m-duration) var(--m-easing) var(--m-delay) forwards;
}

@keyframes m-reveal {
  from { clip-path: inset(0 0 100% 0); transform: translateY(12px); opacity: 0.2; }
  to { clip-path: inset(0 0 -10% 0); transform: translateY(0); opacity: 1; }
}', '<template>
    <div class="motion-root">
      <h2 class="m-title">Motion Lab</h2>
    </div>
</template>

<style scoped>
.m-title {
  margin: 0;
  font: 700 40px/1.1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.02em;
  color: #f4f6f8;
  clip-path: inset(0 0 100% 0);
  transform: translateY(6px);
  animation: m-reveal var(--m-duration) var(--m-easing) var(--m-delay) forwards;
}

@keyframes m-reveal {
  from { clip-path: inset(0 0 100% 0); transform: translateY(12px); opacity: 0.2; }
  to { clip-path: inset(0 0 -10% 0); transform: translateY(0); opacity: 1; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <h2 className="m-title">Motion Lab</h2>
      </div>
  )
}

/* styles.css
.m-title {
  margin: 0;
  font: 700 40px/1.1 system-ui, "PingFang SC", sans-serif;
  letter-spacing: 0.02em;
  color: #f4f6f8;
  clip-path: inset(0 0 100% 0);
  transform: translateY(6px);
  animation: m-reveal var(--m-duration) var(--m-easing) var(--m-delay) forwards;
}

@keyframes m-reveal {
  from { clip-path: inset(0 0 100% 0); transform: translateY(12px); opacity: 0.2; }
  to { clip-path: inset(0 0 -10% 0); transform: translateY(0); opacity: 1; }
}
*/', '', '做一段标题揭示：用 clip-path 从底部向上展开（inset 的 bottom 从 100% 到 0），同时轻微上移 12px 并提升不透明度；时长 1 秒，延迟 0.1 秒，只需一个元素。', 'text reveal', 'OFFICIAL', 'READY'),
	('cursor-follow', '光标跟随光斑', 'Cursor Follow', '一团柔光带阻尼地跟随指针（插值 0.12），停下即停，不硬跟；用 transform 与 will-change 控制开销。', '基础交互', 'Portfolio', 'Cyber', 'CSS', 2, '个人作品集,AI 产品页', 90, 86, 88, 80, 86, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-distance", "label": "光斑半径", "unit": "px", "min": 40, "max": 240, "step": 10, "default": 120}, {"key": "--m-duration", "label": "阻尼", "unit": "", "min": 0.04, "max": 0.4, "step": 0.01, "default": 0.12}]', '', '<div class="motion-root">
  <div class="m-glow"></div>
  <p class="m-hint">移动鼠标试试</p>
</div>', 'var glow = document.querySelector(''.m-glow'');
var target = { x: window.innerWidth / 2, y: window.innerHeight / 2 };
var current = { x: target.x, y: target.y };
var ease = 0.12;

document.addEventListener(''pointermove'', function (event) {
  var rect = document.querySelector(''.motion-root'').getBoundingClientRect();
  target.x = event.clientX - rect.left;
  target.y = event.clientY - rect.top;
});

(function loop() {
  current.x += (target.x - current.x) * ease;
  current.y += (target.y - current.y) * ease;
  glow.style.left = current.x + ''px'';
  glow.style.top = current.y + ''px'';
  requestAnimationFrame(loop);
})();', '.motion-root { cursor: crosshair; }
.m-glow {
  position: absolute;
  width: calc(var(--m-distance) * 2);
  height: calc(var(--m-distance) * 2);
  border-radius: 50%;
  pointer-events: none;
  background: radial-gradient(circle, rgba(240, 205, 114, 0.42), rgba(240, 205, 114, 0) 68%);
  transform: translate3d(-50%, -50%, 0);
  will-change: left, top;
}
.m-hint { color: rgba(232, 234, 237, 0.5); font: 500 13px/1 system-ui, "PingFang SC", sans-serif; }', '<template>
    <div class="motion-root">
      <div class="m-glow"></div>
      <p class="m-hint">移动鼠标试试</p>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var glow = document.querySelector(''.m-glow'');
  var target = { x: window.innerWidth / 2, y: window.innerHeight / 2 };
  var current = { x: target.x, y: target.y };
  var ease = 0.12;

  document.addEventListener(''pointermove'', function (event) {
    var rect = document.querySelector(''.motion-root'').getBoundingClientRect();
    target.x = event.clientX - rect.left;
    target.y = event.clientY - rect.top;
  });

  (function loop() {
    current.x += (target.x - current.x) * ease;
    current.y += (target.y - current.y) * ease;
    glow.style.left = current.x + ''px'';
    glow.style.top = current.y + ''px'';
    requestAnimationFrame(loop);
  })();
})
</script>

<style scoped>
.motion-root { cursor: crosshair; }
.m-glow {
  position: absolute;
  width: calc(var(--m-distance) * 2);
  height: calc(var(--m-distance) * 2);
  border-radius: 50%;
  pointer-events: none;
  background: radial-gradient(circle, rgba(240, 205, 114, 0.42), rgba(240, 205, 114, 0) 68%);
  transform: translate3d(-50%, -50%, 0);
  will-change: left, top;
}
.m-hint { color: rgba(232, 234, 237, 0.5); font: 500 13px/1 system-ui, "PingFang SC", sans-serif; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var glow = document.querySelector(''.m-glow'');
    var target = { x: window.innerWidth / 2, y: window.innerHeight / 2 };
    var current = { x: target.x, y: target.y };
    var ease = 0.12;

    document.addEventListener(''pointermove'', function (event) {
      var rect = document.querySelector(''.motion-root'').getBoundingClientRect();
      target.x = event.clientX - rect.left;
      target.y = event.clientY - rect.top;
    });

    (function loop() {
      current.x += (target.x - current.x) * ease;
      current.y += (target.y - current.y) * ease;
      glow.style.left = current.x + ''px'';
      glow.style.top = current.y + ''px'';
      requestAnimationFrame(loop);
    })();
  }, [])

  return (
      <div className="motion-root">
        <div className="m-glow"></div>
        <p className="m-hint">移动鼠标试试</p>
      </div>
  )
}

/* styles.css
.motion-root { cursor: crosshair; }
.m-glow {
  position: absolute;
  width: calc(var(--m-distance) * 2);
  height: calc(var(--m-distance) * 2);
  border-radius: 50%;
  pointer-events: none;
  background: radial-gradient(circle, rgba(240, 205, 114, 0.42), rgba(240, 205, 114, 0) 68%);
  transform: translate3d(-50%, -50%, 0);
  will-change: left, top;
}
.m-hint { color: rgba(232, 234, 237, 0.5); font: 500 13px/1 system-ui, "PingFang SC", sans-serif; }
*/', '', '做一团跟随鼠标的柔光：用 requestAnimationFrame 做插值跟随（系数 0.12），半径 120px 的径向渐变光斑，中心亮、边缘透明，指针停下后自然停住，不要硬贴。', 'cursor follow', 'OFFICIAL', 'READY'),
	('hero-entrance', '首屏入场', 'Hero Entrance', '标题上移淡入 + 副标题延迟 + 按钮缩放出现，三段式错开；这是「高级感」最省力的来源。', '产品页面', 'Landing Page', 'Luxury', 'CSS', 2, '官网首页,AI 产品页', 94, 90, 92, 88, 91, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.4, "max": 2, "step": 0.1, "default": 0.9}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 8, "max": 60, "step": 2, "default": 26}]', '', '<div class="motion-root motion-root--hero">
  <h1 class="m-hero-title">Build in motion</h1>
  <p class="m-hero-sub">动效不是装饰，是节奏。</p>
  <button class="m-hero-cta" type="button">开始体验</button>
</div>', '', '.motion-root--hero { grid-auto-flow: row; text-align: center; }
.m-hero-title, .m-hero-sub, .m-hero-cta { animation: m-hero var(--m-duration) var(--m-easing) both; }
.m-hero-title {
  margin: 0;
  font: 800 38px/1.15 system-ui, "PingFang SC", sans-serif;
  color: #f6f7f9;
  letter-spacing: -0.01em;
}
.m-hero-sub { margin: 0; color: rgba(232, 234, 237, 0.66); font: 400 15px/1.6 system-ui, "PingFang SC", sans-serif; }
.m-hero-cta {
  padding: 14px 30px;
  border: 0;
  border-radius: 999px;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  color: #0b0b0f;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  box-shadow: 0 16px 34px rgba(240, 205, 114, 0.3);
}
.m-hero-sub { animation-delay: 0.14s; }
.m-hero-cta { animation-delay: 0.28s; }

@keyframes m-hero {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}', '<template>
    <div class="motion-root motion-root--hero">
      <h1 class="m-hero-title">Build in motion</h1>
      <p class="m-hero-sub">动效不是装饰，是节奏。</p>
      <button class="m-hero-cta" type="button">开始体验</button>
    </div>
</template>

<style scoped>
.motion-root--hero { grid-auto-flow: row; text-align: center; }
.m-hero-title, .m-hero-sub, .m-hero-cta { animation: m-hero var(--m-duration) var(--m-easing) both; }
.m-hero-title {
  margin: 0;
  font: 800 38px/1.15 system-ui, "PingFang SC", sans-serif;
  color: #f6f7f9;
  letter-spacing: -0.01em;
}
.m-hero-sub { margin: 0; color: rgba(232, 234, 237, 0.66); font: 400 15px/1.6 system-ui, "PingFang SC", sans-serif; }
.m-hero-cta {
  padding: 14px 30px;
  border: 0;
  border-radius: 999px;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  color: #0b0b0f;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  box-shadow: 0 16px 34px rgba(240, 205, 114, 0.3);
}
.m-hero-sub { animation-delay: 0.14s; }
.m-hero-cta { animation-delay: 0.28s; }

@keyframes m-hero {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--hero">
        <h1 className="m-hero-title">Build in motion</h1>
        <p className="m-hero-sub">动效不是装饰，是节奏。</p>
        <button className="m-hero-cta" type="button">开始体验</button>
      </div>
  )
}

/* styles.css
.motion-root--hero { grid-auto-flow: row; text-align: center; }
.m-hero-title, .m-hero-sub, .m-hero-cta { animation: m-hero var(--m-duration) var(--m-easing) both; }
.m-hero-title {
  margin: 0;
  font: 800 38px/1.15 system-ui, "PingFang SC", sans-serif;
  color: #f6f7f9;
  letter-spacing: -0.01em;
}
.m-hero-sub { margin: 0; color: rgba(232, 234, 237, 0.66); font: 400 15px/1.6 system-ui, "PingFang SC", sans-serif; }
.m-hero-cta {
  padding: 14px 30px;
  border: 0;
  border-radius: 999px;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  color: #0b0b0f;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  box-shadow: 0 16px 34px rgba(240, 205, 114, 0.3);
}
.m-hero-sub { animation-delay: 0.14s; }
.m-hero-cta { animation-delay: 0.28s; }

@keyframes m-hero {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
*/', '', '做首屏入场动画：标题从下方 26px 上移淡入，副标题延迟 0.14 秒，按钮再延迟 0.14 秒并用 scale(0.98)→1 出现；整体 0.9 秒、缓动 cubic-bezier(0.22,0.61,0.36,1)，三段节奏递进而不是同时出现。', 'hero entrance', 'OFFICIAL', 'READY'),
	('dashboard-counter', '数字滚动', 'Dashboard Counter', '数字从 0 递增到目标值，带千分位；用 JS 插值而不是 CSS counter，才能控制缓动与格式。', '产品页面', 'Dashboard', 'Minimal', 'CSS', 2, '后台系统,数据看板', 76, 92, 90, 96, 88, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.4, "max": 4, "step": 0.1, "default": 1.6}]', '', '<div class="motion-root">
  <div class="m-stat">
    <div class="m-stat-value" data-target="128450">0</div>
    <div class="m-stat-label">本月访问</div>
  </div>
</div>', 'document.querySelectorAll(''.m-stat-value'').forEach(function (node) {
  var target = Number(node.dataset.target || 0);
  var duration = 1600;
  var start = performance.now();
  function step(now) {
    var progress = Math.min(1, (now - start) / duration);
    var eased = 1 - Math.pow(1 - progress, 3);
    node.textContent = Math.round(target * eased).toLocaleString(''zh-CN'');
    if (progress < 1) { requestAnimationFrame(step); }
  }
  requestAnimationFrame(step);
});', '.m-stat { text-align: center; }
.m-stat-value {
  font: 700 42px/1 ui-monospace, "SF Mono", Consolas, monospace;
  color: #f0cd72;
  letter-spacing: 0.01em;
}
.m-stat-label { margin-top: 8px; color: rgba(232, 234, 237, 0.55); font: 500 12px/1 system-ui, "PingFang SC", sans-serif; }', '<template>
    <div class="motion-root">
      <div class="m-stat">
        <div class="m-stat-value" data-target="128450">0</div>
        <div class="m-stat-label">本月访问</div>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  document.querySelectorAll(''.m-stat-value'').forEach(function (node) {
    var target = Number(node.dataset.target || 0);
    var duration = 1600;
    var start = performance.now();
    function step(now) {
      var progress = Math.min(1, (now - start) / duration);
      var eased = 1 - Math.pow(1 - progress, 3);
      node.textContent = Math.round(target * eased).toLocaleString(''zh-CN'');
      if (progress < 1) { requestAnimationFrame(step); }
    }
    requestAnimationFrame(step);
  });
})
</script>

<style scoped>
.m-stat { text-align: center; }
.m-stat-value {
  font: 700 42px/1 ui-monospace, "SF Mono", Consolas, monospace;
  color: #f0cd72;
  letter-spacing: 0.01em;
}
.m-stat-label { margin-top: 8px; color: rgba(232, 234, 237, 0.55); font: 500 12px/1 system-ui, "PingFang SC", sans-serif; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    document.querySelectorAll(''.m-stat-value'').forEach(function (node) {
      var target = Number(node.dataset.target || 0);
      var duration = 1600;
      var start = performance.now();
      function step(now) {
        var progress = Math.min(1, (now - start) / duration);
        var eased = 1 - Math.pow(1 - progress, 3);
        node.textContent = Math.round(target * eased).toLocaleString(''zh-CN'');
        if (progress < 1) { requestAnimationFrame(step); }
      }
      requestAnimationFrame(step);
    });
  }, [])

  return (
      <div className="motion-root">
        <div className="m-stat">
          <div className="m-stat-value" data-target="128450">0</div>
          <div className="m-stat-label">本月访问</div>
        </div>
      </div>
  )
}

/* styles.css
.m-stat { text-align: center; }
.m-stat-value {
  font: 700 42px/1 ui-monospace, "SF Mono", Consolas, monospace;
  color: #f0cd72;
  letter-spacing: 0.01em;
}
.m-stat-label { margin-top: 8px; color: rgba(232, 234, 237, 0.55); font: 500 12px/1 system-ui, "PingFang SC", sans-serif; }
*/', '', '做一个数字滚动组件：从 0 递增到 128450，1.6 秒内完成，缓动为 easeOutCubic，过程中用千分位格式化，结束后停在最终值。用 requestAnimationFrame 而不是 setInterval 累加。', 'dashboard counter', 'OFFICIAL', 'READY'),
	('card-stagger', '卡片错落入场', 'Card Stagger', '一排卡片依次浮现，延迟按序号递增；关键是「依次」而不是「同时」，节奏差就是质感差。', '产品页面', 'Landing Page', 'Minimal', 'CSS', 1, '官网首页,后台系统,个人作品集', 84, 96, 94, 96, 92, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.2, "max": 1.5, "step": 0.05, "default": 0.6}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 4, "max": 40, "step": 1, "default": 16}]', '', '<div class="motion-root motion-root--row">
  <div class="m-el"></div>
  <div class="m-el"></div>
  <div class="m-el"></div>
</div>', '', '.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.motion-root--row { grid-auto-flow: column; }
.m-el {
  animation: m-stagger var(--m-duration) var(--m-easing) both;
}
.m-el:nth-child(1) { animation-delay: 0s; }
.m-el:nth-child(2) { animation-delay: 0.09s; }
.m-el:nth-child(3) { animation-delay: 0.18s; }

@keyframes m-stagger {
  from { opacity: 0; transform: translateY(var(--m-distance)); }
  to { opacity: 1; transform: translateY(0); }
}', '<template>
    <div class="motion-root motion-root--row">
      <div class="m-el"></div>
      <div class="m-el"></div>
      <div class="m-el"></div>
    </div>
</template>

<style scoped>
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.motion-root--row { grid-auto-flow: column; }
.m-el {
  animation: m-stagger var(--m-duration) var(--m-easing) both;
}
.m-el:nth-child(1) { animation-delay: 0s; }
.m-el:nth-child(2) { animation-delay: 0.09s; }
.m-el:nth-child(3) { animation-delay: 0.18s; }

@keyframes m-stagger {
  from { opacity: 0; transform: translateY(var(--m-distance)); }
  to { opacity: 1; transform: translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--row">
        <div className="m-el"></div>
        <div className="m-el"></div>
        <div className="m-el"></div>
      </div>
  )
}

/* styles.css
.motion-root {
  --m-duration: 1s;
  --m-delay: 0s;
  --m-easing: cubic-bezier(0.22, 0.61, 0.36, 1);
  --m-scale: 1.06;
  --m-distance: 28px;
  --m-color: #f0cd72;
  --m-surface: linear-gradient(150deg, rgba(240, 205, 114, 0.92), rgba(125, 20, 24, 0.6));
  display: grid;
  place-items: center;
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 24px;
  box-sizing: border-box;
}
.m-el {
  width: 132px;
  height: 74px;
  border-radius: 14px;
  background: var(--m-surface);
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.45);
}
.motion-root--row { grid-auto-flow: column; }
.m-el {
  animation: m-stagger var(--m-duration) var(--m-easing) both;
}
.m-el:nth-child(1) { animation-delay: 0s; }
.m-el:nth-child(2) { animation-delay: 0.09s; }
.m-el:nth-child(3) { animation-delay: 0.18s; }

@keyframes m-stagger {
  from { opacity: 0; transform: translateY(var(--m-distance)); }
  to { opacity: 1; transform: translateY(0); }
}
*/', '', '做卡片错落入场：三张卡片依次浮现，每张延迟 0.09 秒，单张时长 0.6 秒，从下方 16px 上移并淡入；延迟用 nth-child 写，避免为每个卡片加行内样式。', 'card stagger', 'OFFICIAL', 'READY'),
	('page-transition', '页面转场', 'Page Transition', '新页面从右侧滑入并淡入，旧页面轻微左移淡出；让路由切换有方向感，而不是硬切。', '产品页面', 'Dashboard', 'Minimal', 'CSS', 2, '后台系统,官网首页', 78, 90, 88, 92, 86, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.2, "max": 1.2, "step": 0.05, "default": 0.45}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 8, "max": 80, "step": 2, "default": 32}]', '', '<div class="motion-root">
  <div class="m-panel">页面内容</div>
</div>', '', '.m-panel {
  width: min(320px, 80%);
  padding: 28px;
  border-radius: 16px;
  background: rgba(20, 22, 28, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e8eaed;
  font: 500 14px/1.6 system-ui, "PingFang SC", sans-serif;
  animation: m-page-in var(--m-duration) var(--m-easing) both;
}

@keyframes m-page-in {
  from { opacity: 0; transform: translateX(var(--m-distance)); filter: blur(2px); }
  to { opacity: 1; transform: translateX(0); filter: blur(0); }
}', '<template>
    <div class="motion-root">
      <div class="m-panel">页面内容</div>
    </div>
</template>

<style scoped>
.m-panel {
  width: min(320px, 80%);
  padding: 28px;
  border-radius: 16px;
  background: rgba(20, 22, 28, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e8eaed;
  font: 500 14px/1.6 system-ui, "PingFang SC", sans-serif;
  animation: m-page-in var(--m-duration) var(--m-easing) both;
}

@keyframes m-page-in {
  from { opacity: 0; transform: translateX(var(--m-distance)); filter: blur(2px); }
  to { opacity: 1; transform: translateX(0); filter: blur(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-panel">页面内容</div>
      </div>
  )
}

/* styles.css
.m-panel {
  width: min(320px, 80%);
  padding: 28px;
  border-radius: 16px;
  background: rgba(20, 22, 28, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e8eaed;
  font: 500 14px/1.6 system-ui, "PingFang SC", sans-serif;
  animation: m-page-in var(--m-duration) var(--m-easing) both;
}

@keyframes m-page-in {
  from { opacity: 0; transform: translateX(var(--m-distance)); filter: blur(2px); }
  to { opacity: 1; transform: translateX(0); filter: blur(0); }
}
*/', '', '做页面转场：新页面面板从右侧 32px 滑入并淡入，同时从 blur(2px) 变清晰，时长 0.45 秒；旧页面用反向的 translateX(-16px) + opacity 0 淡出，两者时间重叠形成交叠转场。', 'page transition', 'OFFICIAL', 'READY'),
	('modal-morph', '弹窗形变', 'Modal Morph', '弹窗从触发按钮的位置放大出现（transform-origin 对准按钮），关闭时缩回，空间关系清楚。', '产品页面', 'Dashboard', 'Glass', 'CSS', 3, '后台系统,AI 产品页', 88, 84, 86, 88, 86, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.2, "max": 1, "step": 0.05, "default": 0.36}, {"key": "--m-scale", "label": "起始缩放", "unit": "", "min": 0.6, "max": 1, "step": 0.01, "default": 0.86}]', '', '<div class="motion-root">
  <button class="m-trigger" type="button">打开弹窗</button>
  <div class="m-modal" hidden>
    <div class="m-modal-body">
      <strong>Modal Morph</strong>
      <p>从触发点放大出现</p>
    </div>
  </div>
</div>', 'var trigger = document.querySelector(''.m-trigger'');
var modal = document.querySelector(''.m-modal'');
trigger.addEventListener(''click'', function () { modal.hidden = false; });
modal.addEventListener(''click'', function () { modal.hidden = true; });', '.motion-root { position: relative; }
.m-trigger {
  padding: 13px 26px;
  border: 1px solid rgba(240, 205, 114, 0.5);
  border-radius: 12px;
  background: transparent;
  color: #f0cd72;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  cursor: pointer;
}
.m-modal {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(6, 7, 10, 0.6);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}
.m-modal-body {
  width: min(280px, 76%);
  padding: 22px;
  border-radius: 18px;
  background: rgba(28, 30, 38, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #eef1f5;
  font: 500 13px/1.7 system-ui, "PingFang SC", sans-serif;
  transform-origin: 20% 80%;
  animation: m-morph var(--m-duration) var(--m-easing) both;
}
.m-modal-body strong { display: block; margin-bottom: 6px; font-size: 15px; }

@keyframes m-morph {
  from { opacity: 0; transform: scale(var(--m-scale)) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}', '<template>
    <div class="motion-root">
      <button class="m-trigger" type="button">打开弹窗</button>
      <div class="m-modal" hidden>
        <div class="m-modal-body">
          <strong>Modal Morph</strong>
          <p>从触发点放大出现</p>
        </div>
      </div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var trigger = document.querySelector(''.m-trigger'');
  var modal = document.querySelector(''.m-modal'');
  trigger.addEventListener(''click'', function () { modal.hidden = false; });
  modal.addEventListener(''click'', function () { modal.hidden = true; });
})
</script>

<style scoped>
.motion-root { position: relative; }
.m-trigger {
  padding: 13px 26px;
  border: 1px solid rgba(240, 205, 114, 0.5);
  border-radius: 12px;
  background: transparent;
  color: #f0cd72;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  cursor: pointer;
}
.m-modal {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(6, 7, 10, 0.6);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}
.m-modal-body {
  width: min(280px, 76%);
  padding: 22px;
  border-radius: 18px;
  background: rgba(28, 30, 38, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #eef1f5;
  font: 500 13px/1.7 system-ui, "PingFang SC", sans-serif;
  transform-origin: 20% 80%;
  animation: m-morph var(--m-duration) var(--m-easing) both;
}
.m-modal-body strong { display: block; margin-bottom: 6px; font-size: 15px; }

@keyframes m-morph {
  from { opacity: 0; transform: scale(var(--m-scale)) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var trigger = document.querySelector(''.m-trigger'');
    var modal = document.querySelector(''.m-modal'');
    trigger.addEventListener(''click'', function () { modal.hidden = false; });
    modal.addEventListener(''click'', function () { modal.hidden = true; });
  }, [])

  return (
      <div className="motion-root">
        <button className="m-trigger" type="button">打开弹窗</button>
        <div className="m-modal" hidden>
          <div className="m-modal-body">
            <strong>Modal Morph</strong>
            <p>从触发点放大出现</p>
          </div>
        </div>
      </div>
  )
}

/* styles.css
.motion-root { position: relative; }
.m-trigger {
  padding: 13px 26px;
  border: 1px solid rgba(240, 205, 114, 0.5);
  border-radius: 12px;
  background: transparent;
  color: #f0cd72;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  cursor: pointer;
}
.m-modal {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  background: rgba(6, 7, 10, 0.6);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}
.m-modal-body {
  width: min(280px, 76%);
  padding: 22px;
  border-radius: 18px;
  background: rgba(28, 30, 38, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #eef1f5;
  font: 500 13px/1.7 system-ui, "PingFang SC", sans-serif;
  transform-origin: 20% 80%;
  animation: m-morph var(--m-duration) var(--m-easing) both;
}
.m-modal-body strong { display: block; margin-bottom: 6px; font-size: 15px; }

@keyframes m-morph {
  from { opacity: 0; transform: scale(var(--m-scale)) translateY(12px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
*/', '', '做弹窗形变：弹窗从触发按钮方向放大出现（transform-origin 放在左下 20%/80%），从 scale 0.86、下移 12px、透明到原尺寸；时长 0.36 秒，关闭时反向缩回。遮罩用半透明 + blur(6px)。', 'modal morph', 'OFFICIAL', 'READY'),
	('notification-popup', '通知弹入', 'Notification Popup', '右上角通知从右侧滑入并轻微回弹，自动消失前有一次「呼吸」提示；不打断操作。', '产品页面', 'Dashboard', 'Minimal', 'CSS', 2, '后台系统,AI 产品页', 80, 92, 90, 94, 88, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.2, "max": 1, "step": 0.05, "default": 0.5}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 12, "max": 80, "step": 2, "default": 40}]', '', '<div class="motion-root">
  <div class="m-toast">
    <span class="m-dot"></span>
    <div><strong>构建完成</strong><p>3 个动效模板已更新</p></div>
  </div>
</div>', '', '.m-toast {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: min(320px, 86%);
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(22, 24, 30, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.09);
  box-shadow: 0 20px 44px rgba(0, 0, 0, 0.5);
  color: #e8eaed;
  font: 500 12.5px/1.6 system-ui, "PingFang SC", sans-serif;
  animation: m-toast-in var(--m-duration) cubic-bezier(0.16, 1, 0.3, 1) both;
}
.m-toast strong { display: block; font-size: 13.5px; }
.m-toast p { margin: 2px 0 0; color: rgba(232, 234, 237, 0.6); }
.m-dot {
  flex: none;
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #7ee0a2;
  box-shadow: 0 0 0 4px rgba(126, 224, 162, 0.16);
  animation: m-pulse 2s ease-in-out infinite;
}

@keyframes m-toast-in {
  from { opacity: 0; transform: translateX(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateX(0) scale(1); }
}
@keyframes m-pulse {
  50% { box-shadow: 0 0 0 8px rgba(126, 224, 162, 0.08); }
}', '<template>
    <div class="motion-root">
      <div class="m-toast">
        <span class="m-dot"></span>
        <div><strong>构建完成</strong><p>3 个动效模板已更新</p></div>
      </div>
    </div>
</template>

<style scoped>
.m-toast {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: min(320px, 86%);
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(22, 24, 30, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.09);
  box-shadow: 0 20px 44px rgba(0, 0, 0, 0.5);
  color: #e8eaed;
  font: 500 12.5px/1.6 system-ui, "PingFang SC", sans-serif;
  animation: m-toast-in var(--m-duration) cubic-bezier(0.16, 1, 0.3, 1) both;
}
.m-toast strong { display: block; font-size: 13.5px; }
.m-toast p { margin: 2px 0 0; color: rgba(232, 234, 237, 0.6); }
.m-dot {
  flex: none;
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #7ee0a2;
  box-shadow: 0 0 0 4px rgba(126, 224, 162, 0.16);
  animation: m-pulse 2s ease-in-out infinite;
}

@keyframes m-toast-in {
  from { opacity: 0; transform: translateX(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateX(0) scale(1); }
}
@keyframes m-pulse {
  50% { box-shadow: 0 0 0 8px rgba(126, 224, 162, 0.08); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-toast">
          <span className="m-dot"></span>
          <div><strong>构建完成</strong><p>3 个动效模板已更新</p></div>
        </div>
      </div>
  )
}

/* styles.css
.m-toast {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: min(320px, 86%);
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(22, 24, 30, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.09);
  box-shadow: 0 20px 44px rgba(0, 0, 0, 0.5);
  color: #e8eaed;
  font: 500 12.5px/1.6 system-ui, "PingFang SC", sans-serif;
  animation: m-toast-in var(--m-duration) cubic-bezier(0.16, 1, 0.3, 1) both;
}
.m-toast strong { display: block; font-size: 13.5px; }
.m-toast p { margin: 2px 0 0; color: rgba(232, 234, 237, 0.6); }
.m-dot {
  flex: none;
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #7ee0a2;
  box-shadow: 0 0 0 4px rgba(126, 224, 162, 0.16);
  animation: m-pulse 2s ease-in-out infinite;
}

@keyframes m-toast-in {
  from { opacity: 0; transform: translateX(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateX(0) scale(1); }
}
@keyframes m-pulse {
  50% { box-shadow: 0 0 0 8px rgba(126, 224, 162, 0.08); }
}
*/', '', '做通知弹入：通知卡从右侧 40px 滑入，缓动用能产生轻微回弹的 cubic-bezier(0.16,1,0.3,1)，时长 0.5 秒；左侧状态点用呼吸光环循环提示，整体不遮挡主操作区。', 'notification popup', 'OFFICIAL', 'READY'),
	('pricing-card-hover', '价格卡对比', 'Pricing Card Hover', '被选中的价格卡抬起、描边点亮，未选中的轻微变暗；用兄弟选择器做对比，突出推荐档位。', '产品页面', 'Landing Page', 'Minimal', 'CSS', 2, '官网首页,AI 产品页', 86, 92, 92, 92, 90, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-distance", "label": "抬起", "unit": "px", "min": 2, "max": 24, "step": 1, "default": 10}, {"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.2, "max": 1, "step": 0.05, "default": 0.4}]', '', '<div class="motion-root motion-root--row">
  <div class="m-plan"><b>基础</b><span>¥0</span></div>
  <div class="m-plan m-plan--hot"><b>专业</b><span>¥39</span></div>
  <div class="m-plan"><b>团队</b><span>¥99</span></div>
</div>', '', '.motion-root--row { grid-auto-flow: column; }
.m-plan {
  width: 128px;
  padding: 18px 14px;
  border-radius: 16px;
  text-align: center;
  background: rgba(20, 22, 28, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e8eaed;
  font: 500 12px/1.5 system-ui, "PingFang SC", sans-serif;
  transition: transform var(--m-duration) var(--m-easing), box-shadow var(--m-duration) ease,
    border-color var(--m-duration) ease, opacity var(--m-duration) ease;
}
.m-plan b { display: block; font-size: 13px; margin-bottom: 6px; }
.m-plan span { font: 700 20px/1 ui-monospace, Consolas, monospace; color: #f0cd72; }
.m-plan--hot { border-color: rgba(240, 205, 114, 0.4); transform: translateY(-6px); }
.motion-root--row:hover .m-plan { opacity: 0.62; }
.motion-root--row .m-plan:hover {
  opacity: 1;
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.8);
  box-shadow: 0 22px 46px rgba(0, 0, 0, 0.5);
}', '<template>
    <div class="motion-root motion-root--row">
      <div class="m-plan"><b>基础</b><span>¥0</span></div>
      <div class="m-plan m-plan--hot"><b>专业</b><span>¥39</span></div>
      <div class="m-plan"><b>团队</b><span>¥99</span></div>
    </div>
</template>

<style scoped>
.motion-root--row { grid-auto-flow: column; }
.m-plan {
  width: 128px;
  padding: 18px 14px;
  border-radius: 16px;
  text-align: center;
  background: rgba(20, 22, 28, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e8eaed;
  font: 500 12px/1.5 system-ui, "PingFang SC", sans-serif;
  transition: transform var(--m-duration) var(--m-easing), box-shadow var(--m-duration) ease,
    border-color var(--m-duration) ease, opacity var(--m-duration) ease;
}
.m-plan b { display: block; font-size: 13px; margin-bottom: 6px; }
.m-plan span { font: 700 20px/1 ui-monospace, Consolas, monospace; color: #f0cd72; }
.m-plan--hot { border-color: rgba(240, 205, 114, 0.4); transform: translateY(-6px); }
.motion-root--row:hover .m-plan { opacity: 0.62; }
.motion-root--row .m-plan:hover {
  opacity: 1;
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.8);
  box-shadow: 0 22px 46px rgba(0, 0, 0, 0.5);
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--row">
        <div className="m-plan"><b>基础</b><span>¥0</span></div>
        <div className="m-plan m-plan--hot"><b>专业</b><span>¥39</span></div>
        <div className="m-plan"><b>团队</b><span>¥99</span></div>
      </div>
  )
}

/* styles.css
.motion-root--row { grid-auto-flow: column; }
.m-plan {
  width: 128px;
  padding: 18px 14px;
  border-radius: 16px;
  text-align: center;
  background: rgba(20, 22, 28, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #e8eaed;
  font: 500 12px/1.5 system-ui, "PingFang SC", sans-serif;
  transition: transform var(--m-duration) var(--m-easing), box-shadow var(--m-duration) ease,
    border-color var(--m-duration) ease, opacity var(--m-duration) ease;
}
.m-plan b { display: block; font-size: 13px; margin-bottom: 6px; }
.m-plan span { font: 700 20px/1 ui-monospace, Consolas, monospace; color: #f0cd72; }
.m-plan--hot { border-color: rgba(240, 205, 114, 0.4); transform: translateY(-6px); }
.motion-root--row:hover .m-plan { opacity: 0.62; }
.motion-root--row .m-plan:hover {
  opacity: 1;
  transform: translateY(calc(var(--m-distance) * -1));
  border-color: rgba(240, 205, 114, 0.8);
  box-shadow: 0 22px 46px rgba(0, 0, 0, 0.5);
}
*/', '', '做价格卡对比交互：鼠标进入某张卡时它抬起 10px、描边点亮、阴影加深，同时其余卡片降到 0.62 不透明度；过渡 0.4 秒，推荐档位默认就抬高 6px。', 'pricing card hover', 'OFFICIAL', 'READY'),
	('login-animation', '登录页入场', 'Login Animation', '登录卡片整体上浮淡入，输入框自上而下依次出现；顺序比动效本身更重要，视线跟着字段走。', '产品页面', 'Login', 'Glass', 'CSS', 2, '登录页,后台系统', 84, 90, 90, 90, 88, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.3, "max": 1.6, "step": 0.1, "default": 0.7}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 8, "max": 48, "step": 2, "default": 20}]', '', '<div class="motion-root">
  <form class="m-login">
    <div class="m-login-title">Welcome back</div>
    <div class="m-field"></div>
    <div class="m-field"></div>
    <div class="m-submit"></div>
  </form>
</div>', '', '.m-login {
  width: 236px;
  padding: 22px 20px;
  border-radius: 20px;
  display: grid;
  gap: 12px;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.03));
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  animation: m-login-in var(--m-duration) var(--m-easing) both;
}
.m-login-title { color: #f2f4f7; font: 600 15px/1 system-ui, "PingFang SC", sans-serif; }
.m-field {
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  animation: m-field-in var(--m-duration) var(--m-easing) both;
}
.m-field:nth-of-type(2) { animation-delay: 0.1s; }
.m-field:nth-of-type(3) { animation-delay: 0.2s; }
.m-submit {
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  animation: m-field-in var(--m-duration) var(--m-easing) 0.3s both;
}

@keyframes m-login-in {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
@keyframes m-field-in {
  from { opacity: 0; transform: translateY(calc(var(--m-distance) / 2)); }
  to { opacity: 1; transform: translateY(0); }
}', '<template>
    <div class="motion-root">
      <form class="m-login">
        <div class="m-login-title">Welcome back</div>
        <div class="m-field"></div>
        <div class="m-field"></div>
        <div class="m-submit"></div>
      </form>
    </div>
</template>

<style scoped>
.m-login {
  width: 236px;
  padding: 22px 20px;
  border-radius: 20px;
  display: grid;
  gap: 12px;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.03));
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  animation: m-login-in var(--m-duration) var(--m-easing) both;
}
.m-login-title { color: #f2f4f7; font: 600 15px/1 system-ui, "PingFang SC", sans-serif; }
.m-field {
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  animation: m-field-in var(--m-duration) var(--m-easing) both;
}
.m-field:nth-of-type(2) { animation-delay: 0.1s; }
.m-field:nth-of-type(3) { animation-delay: 0.2s; }
.m-submit {
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  animation: m-field-in var(--m-duration) var(--m-easing) 0.3s both;
}

@keyframes m-login-in {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
@keyframes m-field-in {
  from { opacity: 0; transform: translateY(calc(var(--m-distance) / 2)); }
  to { opacity: 1; transform: translateY(0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <form className="m-login">
          <div className="m-login-title">Welcome back</div>
          <div className="m-field"></div>
          <div className="m-field"></div>
          <div className="m-submit"></div>
        </form>
      </div>
  )
}

/* styles.css
.m-login {
  width: 236px;
  padding: 22px 20px;
  border-radius: 20px;
  display: grid;
  gap: 12px;
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.03));
  border: 1px solid rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  animation: m-login-in var(--m-duration) var(--m-easing) both;
}
.m-login-title { color: #f2f4f7; font: 600 15px/1 system-ui, "PingFang SC", sans-serif; }
.m-field {
  height: 34px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  animation: m-field-in var(--m-duration) var(--m-easing) both;
}
.m-field:nth-of-type(2) { animation-delay: 0.1s; }
.m-field:nth-of-type(3) { animation-delay: 0.2s; }
.m-submit {
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f0cd72, #d8a94a);
  animation: m-field-in var(--m-duration) var(--m-easing) 0.3s both;
}

@keyframes m-login-in {
  from { opacity: 0; transform: translateY(var(--m-distance)) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
@keyframes m-field-in {
  from { opacity: 0; transform: translateY(calc(var(--m-distance) / 2)); }
  to { opacity: 1; transform: translateY(0); }
}
*/', '', '做登录卡片入场：卡片整体从下方 20px 上浮淡入（0.7 秒），卡片内两个输入框与提交按钮依次延迟 0.1 秒出现，形成自上而下的视线引导；卡片用玻璃拟态。', 'login animation', 'OFFICIAL', 'READY'),
	('scroll-reveal', '滚动逐级点亮', 'Scroll Reveal', '元素进入视口时点亮（IntersectionObserver 触发一次），离开不重置；滚动叙事的基础件。', '产品页面', 'Landing Page', 'Minimal', 'CSS', 2, '官网首页,个人作品集', 88, 92, 94, 90, 91, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "时长", "unit": "s", "min": 0.3, "max": 1.6, "step": 0.1, "default": 0.8}, {"key": "--m-distance", "label": "位移", "unit": "px", "min": 8, "max": 60, "step": 2, "default": 24}]', '', '<div class="motion-root motion-root--scroll">
  <section class="m-section">第一屏</section>
  <section class="m-section m-section--next">滚动到这里</section>
  <section class="m-section">第三屏</section>
</div>', 'var io = new IntersectionObserver(function (entries) {
  entries.forEach(function (entry) {
    if (entry.isIntersecting) {
      entry.target.classList.add(''is-in'');
      io.unobserve(entry.target);
    }
  });
}, { threshold: 0.25 });
document.querySelectorAll(''.m-section'').forEach(function (node) { io.observe(node); });', '.motion-root--scroll { display: block; overflow-y: auto; padding: 0; }
.m-section {
  display: grid;
  place-items: center;
  height: 150px;
  margin: 12px 0;
  border-radius: 14px;
  background: rgba(20, 22, 28, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.07);
  color: #e8eaed;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  opacity: 0;
  transform: translateY(var(--m-distance));
  transition: opacity var(--m-duration) var(--m-easing), transform var(--m-duration) var(--m-easing);
}
.m-section.is-in { opacity: 1; transform: translateY(0); }', '<template>
    <div class="motion-root motion-root--scroll">
      <section class="m-section">第一屏</section>
      <section class="m-section m-section--next">滚动到这里</section>
      <section class="m-section">第三屏</section>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var io = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        entry.target.classList.add(''is-in'');
        io.unobserve(entry.target);
      }
    });
  }, { threshold: 0.25 });
  document.querySelectorAll(''.m-section'').forEach(function (node) { io.observe(node); });
})
</script>

<style scoped>
.motion-root--scroll { display: block; overflow-y: auto; padding: 0; }
.m-section {
  display: grid;
  place-items: center;
  height: 150px;
  margin: 12px 0;
  border-radius: 14px;
  background: rgba(20, 22, 28, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.07);
  color: #e8eaed;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  opacity: 0;
  transform: translateY(var(--m-distance));
  transition: opacity var(--m-duration) var(--m-easing), transform var(--m-duration) var(--m-easing);
}
.m-section.is-in { opacity: 1; transform: translateY(0); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add(''is-in'');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.25 });
    document.querySelectorAll(''.m-section'').forEach(function (node) { io.observe(node); });
  }, [])

  return (
      <div className="motion-root motion-root--scroll">
        <section className="m-section">第一屏</section>
        <section className="m-section m-section--next">滚动到这里</section>
        <section className="m-section">第三屏</section>
      </div>
  )
}

/* styles.css
.motion-root--scroll { display: block; overflow-y: auto; padding: 0; }
.m-section {
  display: grid;
  place-items: center;
  height: 150px;
  margin: 12px 0;
  border-radius: 14px;
  background: rgba(20, 22, 28, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.07);
  color: #e8eaed;
  font: 600 14px/1 system-ui, "PingFang SC", sans-serif;
  opacity: 0;
  transform: translateY(var(--m-distance));
  transition: opacity var(--m-duration) var(--m-easing), transform var(--m-duration) var(--m-easing);
}
.m-section.is-in { opacity: 1; transform: translateY(0); }
*/', '', '做滚动逐级点亮：用 IntersectionObserver（阈值 0.25）监听区块，进入视口后加 is-in 类，从下方 24px 上移淡入 0.8 秒，触发后即取消监听，滚回去不重置。', 'scroll reveal', 'OFFICIAL', 'READY'),
	('image-parallax', '视差图层', 'Image Parallax', '鼠标移动时前后图层以不同幅度位移，形成纵深感；只用 transform，不触发布局。', '产品页面', 'Portfolio', 'Luxury', 'CSS', 2, '个人作品集,官网首页', 87, 88, 90, 84, 87, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-distance", "label": "位移幅度", "unit": "px", "min": 6, "max": 60, "step": 2, "default": 22}, {"key": "--m-duration", "label": "跟随时长", "unit": "s", "min": 0.1, "max": 1, "step": 0.05, "default": 0.4}]', '', '<div class="motion-root">
  <div class="m-layer m-layer--back"></div>
  <div class="m-layer m-layer--mid"></div>
  <div class="m-layer m-layer--front"></div>
</div>', 'var root = document.querySelector(''.motion-root'');
root.addEventListener(''pointermove'', function (event) {
  var rect = root.getBoundingClientRect();
  var rx = (event.clientX - rect.left) / rect.width - 0.5;
  var ry = (event.clientY - rect.top) / rect.height - 0.5;
  var amount = 22;
  root.querySelector(''.m-layer--back'').style.transform = ''translate('' + (rx * amount * 0.4) + ''px,'' + (ry * amount * 0.4) + ''px)'';
  root.querySelector(''.m-layer--mid'').style.transform = ''translate('' + (rx * amount * 0.8) + ''px,'' + (ry * amount * 0.8) + ''px)'';
  root.querySelector(''.m-layer--front'').style.transform = ''translate('' + (rx * amount * 1.4) + ''px,'' + (ry * amount * 1.4) + ''px)'';
});', '.motion-root { perspective: 700px; }
.m-layer {
  position: absolute;
  border-radius: 18px;
  transition: transform var(--m-duration) var(--m-easing);
  will-change: transform;
}
.m-layer--back { width: 230px; height: 140px; background: rgba(240, 205, 114, 0.14); }
.m-layer--mid { width: 180px; height: 110px; background: rgba(240, 205, 114, 0.28); }
.m-layer--front {
  width: 130px;
  height: 80px;
  background: linear-gradient(140deg, rgba(240, 205, 114, 0.95), rgba(125, 20, 24, 0.6));
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.45);
}', '<template>
    <div class="motion-root">
      <div class="m-layer m-layer--back"></div>
      <div class="m-layer m-layer--mid"></div>
      <div class="m-layer m-layer--front"></div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var root = document.querySelector(''.motion-root'');
  root.addEventListener(''pointermove'', function (event) {
    var rect = root.getBoundingClientRect();
    var rx = (event.clientX - rect.left) / rect.width - 0.5;
    var ry = (event.clientY - rect.top) / rect.height - 0.5;
    var amount = 22;
    root.querySelector(''.m-layer--back'').style.transform = ''translate('' + (rx * amount * 0.4) + ''px,'' + (ry * amount * 0.4) + ''px)'';
    root.querySelector(''.m-layer--mid'').style.transform = ''translate('' + (rx * amount * 0.8) + ''px,'' + (ry * amount * 0.8) + ''px)'';
    root.querySelector(''.m-layer--front'').style.transform = ''translate('' + (rx * amount * 1.4) + ''px,'' + (ry * amount * 1.4) + ''px)'';
  });
})
</script>

<style scoped>
.motion-root { perspective: 700px; }
.m-layer {
  position: absolute;
  border-radius: 18px;
  transition: transform var(--m-duration) var(--m-easing);
  will-change: transform;
}
.m-layer--back { width: 230px; height: 140px; background: rgba(240, 205, 114, 0.14); }
.m-layer--mid { width: 180px; height: 110px; background: rgba(240, 205, 114, 0.28); }
.m-layer--front {
  width: 130px;
  height: 80px;
  background: linear-gradient(140deg, rgba(240, 205, 114, 0.95), rgba(125, 20, 24, 0.6));
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.45);
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var root = document.querySelector(''.motion-root'');
    root.addEventListener(''pointermove'', function (event) {
      var rect = root.getBoundingClientRect();
      var rx = (event.clientX - rect.left) / rect.width - 0.5;
      var ry = (event.clientY - rect.top) / rect.height - 0.5;
      var amount = 22;
      root.querySelector(''.m-layer--back'').style.transform = ''translate('' + (rx * amount * 0.4) + ''px,'' + (ry * amount * 0.4) + ''px)'';
      root.querySelector(''.m-layer--mid'').style.transform = ''translate('' + (rx * amount * 0.8) + ''px,'' + (ry * amount * 0.8) + ''px)'';
      root.querySelector(''.m-layer--front'').style.transform = ''translate('' + (rx * amount * 1.4) + ''px,'' + (ry * amount * 1.4) + ''px)'';
    });
  }, [])

  return (
      <div className="motion-root">
        <div className="m-layer m-layer--back"></div>
        <div className="m-layer m-layer--mid"></div>
        <div className="m-layer m-layer--front"></div>
      </div>
  )
}

/* styles.css
.motion-root { perspective: 700px; }
.m-layer {
  position: absolute;
  border-radius: 18px;
  transition: transform var(--m-duration) var(--m-easing);
  will-change: transform;
}
.m-layer--back { width: 230px; height: 140px; background: rgba(240, 205, 114, 0.14); }
.m-layer--mid { width: 180px; height: 110px; background: rgba(240, 205, 114, 0.28); }
.m-layer--front {
  width: 130px;
  height: 80px;
  background: linear-gradient(140deg, rgba(240, 205, 114, 0.95), rgba(125, 20, 24, 0.6));
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.45);
}
*/', '', '做视差图层：三个图层随鼠标移动以 0.4 / 0.8 / 1.4 倍的不同幅度位移（最多 22px），用 transform 保证不触发布局重排，过渡 0.4 秒让跟随带一点滞后感。', 'image parallax', 'OFFICIAL', 'READY'),
	('aurora-background', '极光背景', 'Aurora Background', '多层径向渐变以不同速度和方向缓慢漂移，形成极光流动；纯 CSS，无需 WebGL。', '高级效果', 'AI SaaS', 'Cyber', 'CSS', 2, 'AI 产品页,官网首页', 93, 92, 90, 82, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "漂移周期", "unit": "s", "min": 4, "max": 30, "step": 1, "default": 14}, {"key": "--m-distance", "label": "漂移幅度", "unit": "%", "min": 2, "max": 30, "step": 1, "default": 12}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-aurora"></div>
</div>', '', '.motion-root--bleed { padding: 0; }
.m-aurora {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 90% at 50% 110%, #10131c, #06070a 70%);
}
.m-aurora::before,
.m-aurora::after {
  content: "";
  position: absolute;
  inset: -20%;
  background:
    radial-gradient(38% 42% at 24% 32%, rgba(94, 234, 212, 0.32), transparent 62%),
    radial-gradient(42% 46% at 74% 40%, rgba(167, 139, 250, 0.34), transparent 64%),
    radial-gradient(36% 40% at 52% 76%, rgba(240, 205, 114, 0.28), transparent 66%);
  filter: blur(24px);
  animation: m-aurora-a var(--m-duration) ease-in-out infinite alternate;
}
.m-aurora::after {
  background:
    radial-gradient(34% 40% at 66% 66%, rgba(56, 189, 248, 0.28), transparent 64%),
    radial-gradient(30% 36% at 30% 60%, rgba(244, 114, 182, 0.22), transparent 62%);
  animation-duration: calc(var(--m-duration) * 1.6);
  animation-direction: alternate-reverse;
}

@keyframes m-aurora-a {
  from { transform: translate3d(calc(var(--m-distance) * -1), 2%, 0) scale(1); }
  to { transform: translate3d(var(--m-distance), -3%, 0) scale(1.08); }
}', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-aurora"></div>
    </div>
</template>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-aurora {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 90% at 50% 110%, #10131c, #06070a 70%);
}
.m-aurora::before,
.m-aurora::after {
  content: "";
  position: absolute;
  inset: -20%;
  background:
    radial-gradient(38% 42% at 24% 32%, rgba(94, 234, 212, 0.32), transparent 62%),
    radial-gradient(42% 46% at 74% 40%, rgba(167, 139, 250, 0.34), transparent 64%),
    radial-gradient(36% 40% at 52% 76%, rgba(240, 205, 114, 0.28), transparent 66%);
  filter: blur(24px);
  animation: m-aurora-a var(--m-duration) ease-in-out infinite alternate;
}
.m-aurora::after {
  background:
    radial-gradient(34% 40% at 66% 66%, rgba(56, 189, 248, 0.28), transparent 64%),
    radial-gradient(30% 36% at 30% 60%, rgba(244, 114, 182, 0.22), transparent 62%);
  animation-duration: calc(var(--m-duration) * 1.6);
  animation-direction: alternate-reverse;
}

@keyframes m-aurora-a {
  from { transform: translate3d(calc(var(--m-distance) * -1), 2%, 0) scale(1); }
  to { transform: translate3d(var(--m-distance), -3%, 0) scale(1.08); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-aurora"></div>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-aurora {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 90% at 50% 110%, #10131c, #06070a 70%);
}
.m-aurora::before,
.m-aurora::after {
  content: "";
  position: absolute;
  inset: -20%;
  background:
    radial-gradient(38% 42% at 24% 32%, rgba(94, 234, 212, 0.32), transparent 62%),
    radial-gradient(42% 46% at 74% 40%, rgba(167, 139, 250, 0.34), transparent 64%),
    radial-gradient(36% 40% at 52% 76%, rgba(240, 205, 114, 0.28), transparent 66%);
  filter: blur(24px);
  animation: m-aurora-a var(--m-duration) ease-in-out infinite alternate;
}
.m-aurora::after {
  background:
    radial-gradient(34% 40% at 66% 66%, rgba(56, 189, 248, 0.28), transparent 64%),
    radial-gradient(30% 36% at 30% 60%, rgba(244, 114, 182, 0.22), transparent 62%);
  animation-duration: calc(var(--m-duration) * 1.6);
  animation-direction: alternate-reverse;
}

@keyframes m-aurora-a {
  from { transform: translate3d(calc(var(--m-distance) * -1), 2%, 0) scale(1); }
  to { transform: translate3d(var(--m-distance), -3%, 0) scale(1.08); }
}
*/', '', '做极光背景：两层径向渐变（青绿、紫、金 + 蓝、粉）叠加并 blur(24px)，两层以 14 秒与 22 秒不同周期、相反方向来回漂移与微缩放，形成缓慢流动的极光感，完全用 CSS。', 'aurora background', 'OFFICIAL', 'READY'),
	('particle-network', '粒子星网', 'Particle Network', '粒子缓慢漂移，距离小于阈值时自动连线，指针附近粒子被点亮；节点数与连线阈值都可调。', '高级效果', 'AI SaaS', 'Cyber', 'Canvas', 3, 'AI 产品页,官网首页', 94, 86, 88, 80, 88, 'GPU_ENHANCED', '依赖 GPU 或逐像素计算：观感最好，但在低端设备/集显上可能掉帧。建议开启硬件加速；移动端可减少粒子数、降低分辨率或按需启用。', '[{"key": "--m-distance", "label": "连线距离", "unit": "px", "min": 60, "max": 200, "step": 10, "default": 120}, {"key": "--m-duration", "label": "粒子数", "unit": "", "min": 20, "max": 140, "step": 10, "default": 64}]', '', '<div class="motion-root motion-root--bleed">
  <canvas class="m-canvas"></canvas>
</div>', 'var canvas = document.querySelector(''.m-canvas'');
var ctx = canvas.getContext(''2d'');
var count = 64;
var linkDistance = 120;
var pointer = { x: -999, y: -999 };
var dots = [];

function resize() {
  var rect = canvas.getBoundingClientRect();
  canvas.width = rect.width;
  canvas.height = rect.height;
  dots = Array.from({ length: count }, function () {
    return { x: Math.random() * canvas.width, y: Math.random() * canvas.height,
             vx: (Math.random() - 0.5) * 0.35, vy: (Math.random() - 0.5) * 0.35 };
  });
}

canvas.addEventListener(''pointermove'', function (event) {
  var rect = canvas.getBoundingClientRect();
  pointer.x = event.clientX - rect.left;
  pointer.y = event.clientY - rect.top;
});

function frame() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  dots.forEach(function (dot) {
    dot.x += dot.vx; dot.y += dot.vy;
    if (dot.x < 0 || dot.x > canvas.width) { dot.vx *= -1; }
    if (dot.y < 0 || dot.y > canvas.height) { dot.vy *= -1; }
  });
  for (var i = 0; i < dots.length; i++) {
    for (var j = i + 1; j < dots.length; j++) {
      var dx = dots[i].x - dots[j].x;
      var dy = dots[i].y - dots[j].y;
      var distance = Math.hypot(dx, dy);
      if (distance < linkDistance) {
        ctx.strokeStyle = ''rgba(148, 163, 184,'' + (0.18 * (1 - distance / linkDistance)).toFixed(3) + '')'';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(dots[i].x, dots[i].y);
        ctx.lineTo(dots[j].x, dots[j].y);
        ctx.stroke();
      }
    }
  }
  dots.forEach(function (dot) {
    var near = Math.hypot(dot.x - pointer.x, dot.y - pointer.y) < 140;
    ctx.fillStyle = near ? ''rgba(240, 205, 114, 0.95)'' : ''rgba(203, 213, 225, 0.75)'';
    ctx.beginPath();
    ctx.arc(dot.x, dot.y, near ? 2.4 : 1.5, 0, Math.PI * 2);
    ctx.fill();
  });
  requestAnimationFrame(frame);
}

resize();
window.addEventListener(''resize'', resize);
frame();', '.motion-root--bleed { padding: 0; }
.m-canvas { display: block; width: 100%; height: 100%; background: radial-gradient(120% 100% at 50% 0%, #0b0d14, #05060a 70%); }', '<template>
    <div class="motion-root motion-root--bleed">
      <canvas class="m-canvas"></canvas>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var canvas = document.querySelector(''.m-canvas'');
  var ctx = canvas.getContext(''2d'');
  var count = 64;
  var linkDistance = 120;
  var pointer = { x: -999, y: -999 };
  var dots = [];

  function resize() {
    var rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    dots = Array.from({ length: count }, function () {
      return { x: Math.random() * canvas.width, y: Math.random() * canvas.height,
               vx: (Math.random() - 0.5) * 0.35, vy: (Math.random() - 0.5) * 0.35 };
    });
  }

  canvas.addEventListener(''pointermove'', function (event) {
    var rect = canvas.getBoundingClientRect();
    pointer.x = event.clientX - rect.left;
    pointer.y = event.clientY - rect.top;
  });

  function frame() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    dots.forEach(function (dot) {
      dot.x += dot.vx; dot.y += dot.vy;
      if (dot.x < 0 || dot.x > canvas.width) { dot.vx *= -1; }
      if (dot.y < 0 || dot.y > canvas.height) { dot.vy *= -1; }
    });
    for (var i = 0; i < dots.length; i++) {
      for (var j = i + 1; j < dots.length; j++) {
        var dx = dots[i].x - dots[j].x;
        var dy = dots[i].y - dots[j].y;
        var distance = Math.hypot(dx, dy);
        if (distance < linkDistance) {
          ctx.strokeStyle = ''rgba(148, 163, 184,'' + (0.18 * (1 - distance / linkDistance)).toFixed(3) + '')'';
          ctx.lineWidth = 1;
          ctx.beginPath();
          ctx.moveTo(dots[i].x, dots[i].y);
          ctx.lineTo(dots[j].x, dots[j].y);
          ctx.stroke();
        }
      }
    }
    dots.forEach(function (dot) {
      var near = Math.hypot(dot.x - pointer.x, dot.y - pointer.y) < 140;
      ctx.fillStyle = near ? ''rgba(240, 205, 114, 0.95)'' : ''rgba(203, 213, 225, 0.75)'';
      ctx.beginPath();
      ctx.arc(dot.x, dot.y, near ? 2.4 : 1.5, 0, Math.PI * 2);
      ctx.fill();
    });
    requestAnimationFrame(frame);
  }

  resize();
  window.addEventListener(''resize'', resize);
  frame();
})
</script>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-canvas { display: block; width: 100%; height: 100%; background: radial-gradient(120% 100% at 50% 0%, #0b0d14, #05060a 70%); }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var canvas = document.querySelector(''.m-canvas'');
    var ctx = canvas.getContext(''2d'');
    var count = 64;
    var linkDistance = 120;
    var pointer = { x: -999, y: -999 };
    var dots = [];

    function resize() {
      var rect = canvas.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
      dots = Array.from({ length: count }, function () {
        return { x: Math.random() * canvas.width, y: Math.random() * canvas.height,
                 vx: (Math.random() - 0.5) * 0.35, vy: (Math.random() - 0.5) * 0.35 };
      });
    }

    canvas.addEventListener(''pointermove'', function (event) {
      var rect = canvas.getBoundingClientRect();
      pointer.x = event.clientX - rect.left;
      pointer.y = event.clientY - rect.top;
    });

    function frame() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      dots.forEach(function (dot) {
        dot.x += dot.vx; dot.y += dot.vy;
        if (dot.x < 0 || dot.x > canvas.width) { dot.vx *= -1; }
        if (dot.y < 0 || dot.y > canvas.height) { dot.vy *= -1; }
      });
      for (var i = 0; i < dots.length; i++) {
        for (var j = i + 1; j < dots.length; j++) {
          var dx = dots[i].x - dots[j].x;
          var dy = dots[i].y - dots[j].y;
          var distance = Math.hypot(dx, dy);
          if (distance < linkDistance) {
            ctx.strokeStyle = ''rgba(148, 163, 184,'' + (0.18 * (1 - distance / linkDistance)).toFixed(3) + '')'';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(dots[i].x, dots[i].y);
            ctx.lineTo(dots[j].x, dots[j].y);
            ctx.stroke();
          }
        }
      }
      dots.forEach(function (dot) {
        var near = Math.hypot(dot.x - pointer.x, dot.y - pointer.y) < 140;
        ctx.fillStyle = near ? ''rgba(240, 205, 114, 0.95)'' : ''rgba(203, 213, 225, 0.75)'';
        ctx.beginPath();
        ctx.arc(dot.x, dot.y, near ? 2.4 : 1.5, 0, Math.PI * 2);
        ctx.fill();
      });
      requestAnimationFrame(frame);
    }

    resize();
    window.addEventListener(''resize'', resize);
    frame();
  }, [])

  return (
      <div className="motion-root motion-root--bleed">
        <canvas className="m-canvas"></canvas>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-canvas { display: block; width: 100%; height: 100%; background: radial-gradient(120% 100% at 50% 0%, #0b0d14, #05060a 70%); }
*/', '', '用 Canvas 做粒子星网：64 个粒子以 0.35 的初速度漂移并碰壁反弹，距离小于 120px 的粒子间画一条随距离衰减的细线；指针 140px 内的粒子放大并变为金色。用 requestAnimationFrame 绘制，尺寸变化时重建。', 'particle network', 'OFFICIAL', 'READY'),
	('tilt-card-3d', '三维倾斜卡', '3D Tilt Card', '卡片随指针做透视倾斜，并有高光跟随；倾斜角度与透视距离可调，离开时回正。', '高级效果', 'Portfolio', 'Luxury', 'CSS', 2, '个人作品集,官网首页', 92, 90, 92, 86, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-distance", "label": "倾斜角度", "unit": "deg", "min": 4, "max": 24, "step": 1, "default": 12}, {"key": "--m-duration", "label": "回正时长", "unit": "s", "min": 0.1, "max": 1, "step": 0.05, "default": 0.4}]', '', '<div class="motion-root">
  <div class="m-tilt"><span class="m-tilt-shine"></span><strong>3D Tilt</strong></div>
</div>', 'var card = document.querySelector(''.m-tilt'');
var shine = document.querySelector(''.m-tilt-shine'');
var maxTilt = 12;

card.addEventListener(''pointermove'', function (event) {
  var rect = card.getBoundingClientRect();
  var px = (event.clientX - rect.left) / rect.width;
  var py = (event.clientY - rect.top) / rect.height;
  card.style.transform = ''rotateY('' + ((px - 0.5) * maxTilt * 2) + ''deg) rotateX('' + ((0.5 - py) * maxTilt * 2) + ''deg)'';
  shine.style.background = ''radial-gradient(45% 45% at '' + (px * 100) + ''% '' + (py * 100) + ''%, rgba(255,255,255,.6), transparent 70%)'';
  shine.style.opacity = ''1'';
});
card.addEventListener(''pointerleave'', function () {
  card.style.transform = ''rotateY(0) rotateX(0)'';
  shine.style.opacity = ''0'';
});', '.motion-root { perspective: 900px; }
.m-tilt {
  position: relative;
  width: 240px;
  height: 150px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  overflow: hidden;
  color: #0b0b0f;
  font: 700 15px/1 system-ui, "PingFang SC", sans-serif;
  background: linear-gradient(150deg, #f0cd72, #c9963a);
  box-shadow: 0 24px 52px rgba(0, 0, 0, 0.5);
  transform-style: preserve-3d;
  transition: transform var(--m-duration) var(--m-easing);
}
.m-tilt-shine {
  position: absolute;
  inset: 0;
  background: radial-gradient(40% 40% at 50% 50%, rgba(255, 255, 255, 0.55), transparent 70%);
  opacity: 0;
  transition: opacity var(--m-duration) ease;
}
.m-tilt strong { position: relative; z-index: 1; }', '<template>
    <div class="motion-root">
      <div class="m-tilt"><span class="m-tilt-shine"></span><strong>3D Tilt</strong></div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var card = document.querySelector(''.m-tilt'');
  var shine = document.querySelector(''.m-tilt-shine'');
  var maxTilt = 12;

  card.addEventListener(''pointermove'', function (event) {
    var rect = card.getBoundingClientRect();
    var px = (event.clientX - rect.left) / rect.width;
    var py = (event.clientY - rect.top) / rect.height;
    card.style.transform = ''rotateY('' + ((px - 0.5) * maxTilt * 2) + ''deg) rotateX('' + ((0.5 - py) * maxTilt * 2) + ''deg)'';
    shine.style.background = ''radial-gradient(45% 45% at '' + (px * 100) + ''% '' + (py * 100) + ''%, rgba(255,255,255,.6), transparent 70%)'';
    shine.style.opacity = ''1'';
  });
  card.addEventListener(''pointerleave'', function () {
    card.style.transform = ''rotateY(0) rotateX(0)'';
    shine.style.opacity = ''0'';
  });
})
</script>

<style scoped>
.motion-root { perspective: 900px; }
.m-tilt {
  position: relative;
  width: 240px;
  height: 150px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  overflow: hidden;
  color: #0b0b0f;
  font: 700 15px/1 system-ui, "PingFang SC", sans-serif;
  background: linear-gradient(150deg, #f0cd72, #c9963a);
  box-shadow: 0 24px 52px rgba(0, 0, 0, 0.5);
  transform-style: preserve-3d;
  transition: transform var(--m-duration) var(--m-easing);
}
.m-tilt-shine {
  position: absolute;
  inset: 0;
  background: radial-gradient(40% 40% at 50% 50%, rgba(255, 255, 255, 0.55), transparent 70%);
  opacity: 0;
  transition: opacity var(--m-duration) ease;
}
.m-tilt strong { position: relative; z-index: 1; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var card = document.querySelector(''.m-tilt'');
    var shine = document.querySelector(''.m-tilt-shine'');
    var maxTilt = 12;

    card.addEventListener(''pointermove'', function (event) {
      var rect = card.getBoundingClientRect();
      var px = (event.clientX - rect.left) / rect.width;
      var py = (event.clientY - rect.top) / rect.height;
      card.style.transform = ''rotateY('' + ((px - 0.5) * maxTilt * 2) + ''deg) rotateX('' + ((0.5 - py) * maxTilt * 2) + ''deg)'';
      shine.style.background = ''radial-gradient(45% 45% at '' + (px * 100) + ''% '' + (py * 100) + ''%, rgba(255,255,255,.6), transparent 70%)'';
      shine.style.opacity = ''1'';
    });
    card.addEventListener(''pointerleave'', function () {
      card.style.transform = ''rotateY(0) rotateX(0)'';
      shine.style.opacity = ''0'';
    });
  }, [])

  return (
      <div className="motion-root">
        <div className="m-tilt"><span className="m-tilt-shine"></span><strong>3D Tilt</strong></div>
      </div>
  )
}

/* styles.css
.motion-root { perspective: 900px; }
.m-tilt {
  position: relative;
  width: 240px;
  height: 150px;
  border-radius: 20px;
  display: grid;
  place-items: center;
  overflow: hidden;
  color: #0b0b0f;
  font: 700 15px/1 system-ui, "PingFang SC", sans-serif;
  background: linear-gradient(150deg, #f0cd72, #c9963a);
  box-shadow: 0 24px 52px rgba(0, 0, 0, 0.5);
  transform-style: preserve-3d;
  transition: transform var(--m-duration) var(--m-easing);
}
.m-tilt-shine {
  position: absolute;
  inset: 0;
  background: radial-gradient(40% 40% at 50% 50%, rgba(255, 255, 255, 0.55), transparent 70%);
  opacity: 0;
  transition: opacity var(--m-duration) ease;
}
.m-tilt strong { position: relative; z-index: 1; }
*/', '', '做三维倾斜卡：指针在卡片上移动时按归一化坐标计算 rotateX/rotateY（最大 12 度），父容器 perspective 900px；同时有一团高光跟指针走，离开时 0.4 秒回正。', 'tilt card 3d', 'OFFICIAL', 'READY'),
	('liquid-gradient', '流体质感渐变', 'Liquid Gradient', '背景渐变的色标位置缓慢游走，像液体在流动；用 background-position 动画，几乎零开销。', '高级效果', 'Landing Page', 'Organic', 'CSS', 2, '官网首页,个人作品集', 89, 92, 88, 88, 89, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "周期", "unit": "s", "min": 2, "max": 20, "step": 1, "default": 9}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-liquid"></div>
</div>', '', '.motion-root--bleed { padding: 0; }
.m-liquid {
  width: 100%;
  height: 100%;
  background: linear-gradient(115deg, #1b1030, #12303a, #3a1f12, #14203a, #1b1030);
  background-size: 300% 300%;
  animation: m-liquid var(--m-duration) ease-in-out infinite;
  position: relative;
  overflow: hidden;
}
.m-liquid::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(60% 60% at 50% 50%, transparent 40%, rgba(6, 7, 10, 0.55) 100%);
}

@keyframes m-liquid {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-liquid"></div>
    </div>
</template>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-liquid {
  width: 100%;
  height: 100%;
  background: linear-gradient(115deg, #1b1030, #12303a, #3a1f12, #14203a, #1b1030);
  background-size: 300% 300%;
  animation: m-liquid var(--m-duration) ease-in-out infinite;
  position: relative;
  overflow: hidden;
}
.m-liquid::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(60% 60% at 50% 50%, transparent 40%, rgba(6, 7, 10, 0.55) 100%);
}

@keyframes m-liquid {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-liquid"></div>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-liquid {
  width: 100%;
  height: 100%;
  background: linear-gradient(115deg, #1b1030, #12303a, #3a1f12, #14203a, #1b1030);
  background-size: 300% 300%;
  animation: m-liquid var(--m-duration) ease-in-out infinite;
  position: relative;
  overflow: hidden;
}
.m-liquid::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(60% 60% at 50% 50%, transparent 40%, rgba(6, 7, 10, 0.55) 100%);
}

@keyframes m-liquid {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
*/', '', '做流体渐变背景：五段深色渐变（紫、墨绿、棕、蓝）以 300% 的尺寸铺开，background-position 在 9 秒内 0%→100%→0% 来回移动，边缘再加一层径向暗角压住视线。', 'liquid gradient', 'OFFICIAL', 'READY'),
	('noise-texture', '噪点质感', 'Noise Texture', '用内联 SVG 的 feTurbulence 做细颗粒噪点，叠在纯色上消除塑料感；静态零开销。', '高级效果', 'Portfolio', 'Organic', 'CSS', 1, '个人作品集,官网首页', 78, 96, 92, 94, 89, 'LIGHTWEIGHT', '纯合成属性动画（transform / opacity）：几乎无性能代价，可放心大面积使用。', '[{"key": "--m-distance", "label": "颗粒强度", "unit": "%", "min": 4, "max": 40, "step": 2, "default": 14}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-noise"></div>
</div>', '', '.motion-root--bleed { padding: 0; }
.m-noise {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(140deg, #171a22, #0a0b10);
}
.m-noise::after {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.16;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg xmlns=''http://www.w3.org/2000/svg'' width=''160'' height=''160''%3E%3Cfilter id=''n''%3E%3CfeTurbulence type=''fractalNoise'' baseFrequency=''0.85'' numOctaves=''3''/%3E%3C/filter%3E%3Crect width=''160'' height=''160'' filter=''url(%23n)''/%3E%3C/svg%3E");
}', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-noise"></div>
    </div>
</template>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-noise {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(140deg, #171a22, #0a0b10);
}
.m-noise::after {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.16;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg xmlns=''http://www.w3.org/2000/svg'' width=''160'' height=''160''%3E%3Cfilter id=''n''%3E%3CfeTurbulence type=''fractalNoise'' baseFrequency=''0.85'' numOctaves=''3''/%3E%3C/filter%3E%3Crect width=''160'' height=''160'' filter=''url(%23n)''/%3E%3C/svg%3E");
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-noise"></div>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-noise {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(140deg, #171a22, #0a0b10);
}
.m-noise::after {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.16;
  mix-blend-mode: overlay;
  background-image: url("data:image/svg+xml,%3Csvg xmlns=''http://www.w3.org/2000/svg'' width=''160'' height=''160''%3E%3Cfilter id=''n''%3E%3CfeTurbulence type=''fractalNoise'' baseFrequency=''0.85'' numOctaves=''3''/%3E%3C/filter%3E%3Crect width=''160'' height=''160'' filter=''url(%23n)''/%3E%3C/svg%3E");
}
*/', '', '给深色背景加细颗粒噪点：用内联 SVG 的 feTurbulence（baseFrequency 0.85、3 个八度）生成噪点，以 0.16 不透明度和 overlay 混合模式叠加，消除纯色背景的塑料感。', 'noise texture', 'OFFICIAL', 'READY'),
	('mesh-gradient', '网格渐变', 'Mesh Gradient', '多个色块在网格上错位缩放，形成类似 Figma Mesh 的多点渐变；比单点径向渐变更有层次。', '高级效果', 'AI SaaS', 'Cyber', 'CSS', 2, 'AI 产品页,官网首页', 90, 92, 90, 86, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "周期", "unit": "s", "min": 4, "max": 30, "step": 1, "default": 16}, {"key": "--m-scale", "label": "色块缩放", "unit": "", "min": 1, "max": 1.8, "step": 0.05, "default": 1.35}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-mesh"></div>
</div>', '', '.motion-root--bleed { padding: 0; }
.m-mesh {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #08090d;
}
.m-mesh::before,
.m-mesh::after {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(30% 38% at 18% 24%, rgba(96, 165, 250, 0.5), transparent 60%),
    radial-gradient(34% 40% at 82% 28%, rgba(240, 205, 114, 0.45), transparent 62%),
    radial-gradient(30% 36% at 62% 82%, rgba(168, 85, 247, 0.42), transparent 60%);
  animation: m-mesh var(--m-duration) ease-in-out infinite alternate;
}
.m-mesh::after {
  background-image:
    radial-gradient(28% 34% at 40% 66%, rgba(45, 212, 191, 0.4), transparent 58%),
    radial-gradient(26% 32% at 88% 74%, rgba(244, 63, 94, 0.3), transparent 58%);
  animation-duration: calc(var(--m-duration) * 1.3);
  animation-direction: alternate-reverse;
}

@keyframes m-mesh {
  from { transform: scale(1) translate3d(-2%, -1%, 0); }
  to { transform: scale(var(--m-scale)) translate3d(2%, 2%, 0); }
}', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-mesh"></div>
    </div>
</template>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-mesh {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #08090d;
}
.m-mesh::before,
.m-mesh::after {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(30% 38% at 18% 24%, rgba(96, 165, 250, 0.5), transparent 60%),
    radial-gradient(34% 40% at 82% 28%, rgba(240, 205, 114, 0.45), transparent 62%),
    radial-gradient(30% 36% at 62% 82%, rgba(168, 85, 247, 0.42), transparent 60%);
  animation: m-mesh var(--m-duration) ease-in-out infinite alternate;
}
.m-mesh::after {
  background-image:
    radial-gradient(28% 34% at 40% 66%, rgba(45, 212, 191, 0.4), transparent 58%),
    radial-gradient(26% 32% at 88% 74%, rgba(244, 63, 94, 0.3), transparent 58%);
  animation-duration: calc(var(--m-duration) * 1.3);
  animation-direction: alternate-reverse;
}

@keyframes m-mesh {
  from { transform: scale(1) translate3d(-2%, -1%, 0); }
  to { transform: scale(var(--m-scale)) translate3d(2%, 2%, 0); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-mesh"></div>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-mesh {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #08090d;
}
.m-mesh::before,
.m-mesh::after {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(30% 38% at 18% 24%, rgba(96, 165, 250, 0.5), transparent 60%),
    radial-gradient(34% 40% at 82% 28%, rgba(240, 205, 114, 0.45), transparent 62%),
    radial-gradient(30% 36% at 62% 82%, rgba(168, 85, 247, 0.42), transparent 60%);
  animation: m-mesh var(--m-duration) ease-in-out infinite alternate;
}
.m-mesh::after {
  background-image:
    radial-gradient(28% 34% at 40% 66%, rgba(45, 212, 191, 0.4), transparent 58%),
    radial-gradient(26% 32% at 88% 74%, rgba(244, 63, 94, 0.3), transparent 58%);
  animation-duration: calc(var(--m-duration) * 1.3);
  animation-direction: alternate-reverse;
}

@keyframes m-mesh {
  from { transform: scale(1) translate3d(-2%, -1%, 0); }
  to { transform: scale(var(--m-scale)) translate3d(2%, 2%, 0); }
}
*/', '', '做网格渐变背景：两层各含 3-5 个径向色斑（蓝、金、紫、青、玫红），两层以 16 秒与 21 秒相反方向缓慢缩放（最大 1.35 倍）与位移，做出 Figma Mesh 那种多点流动感。', 'mesh gradient', 'OFFICIAL', 'READY'),
	('floating-orb', '浮动光球', 'Floating Orb', '一颗带内发光与投影的球体在三维空间里缓慢漂浮旋转，适合做视觉焦点或加载态。', '高级效果', 'AI SaaS', 'Luxury', 'CSS', 1, 'AI 产品页,个人作品集', 85, 94, 90, 90, 90, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-distance", "label": "漂移幅度", "unit": "px", "min": 6, "max": 40, "step": 2, "default": 18}, {"key": "--m-duration", "label": "周期", "unit": "s", "min": 2, "max": 14, "step": 1, "default": 8}]', '', '<div class="motion-root">
  <div class="m-orb"></div>
</div>', '', '.m-orb {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background:
    radial-gradient(38% 34% at 32% 28%, rgba(255, 255, 255, 0.95), transparent 62%),
    radial-gradient(70% 70% at 62% 68%, rgba(125, 20, 24, 0.85), transparent 70%),
    linear-gradient(160deg, #f0cd72, #b8763a 62%, #4a1220);
  box-shadow:
    inset 0 -18px 32px rgba(0, 0, 0, 0.5),
    0 28px 60px rgba(240, 205, 114, 0.18),
    0 0 90px rgba(240, 205, 114, 0.22);
  animation: m-orb var(--m-duration) ease-in-out infinite alternate;
}

@keyframes m-orb {
  from { transform: translate3d(calc(var(--m-distance) * -1), calc(var(--m-distance) * -0.5), 0) rotate(-8deg); }
  to { transform: translate3d(var(--m-distance), calc(var(--m-distance) * 0.5), 0) rotate(8deg); }
}', '<template>
    <div class="motion-root">
      <div class="m-orb"></div>
    </div>
</template>

<style scoped>
.m-orb {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background:
    radial-gradient(38% 34% at 32% 28%, rgba(255, 255, 255, 0.95), transparent 62%),
    radial-gradient(70% 70% at 62% 68%, rgba(125, 20, 24, 0.85), transparent 70%),
    linear-gradient(160deg, #f0cd72, #b8763a 62%, #4a1220);
  box-shadow:
    inset 0 -18px 32px rgba(0, 0, 0, 0.5),
    0 28px 60px rgba(240, 205, 114, 0.18),
    0 0 90px rgba(240, 205, 114, 0.22);
  animation: m-orb var(--m-duration) ease-in-out infinite alternate;
}

@keyframes m-orb {
  from { transform: translate3d(calc(var(--m-distance) * -1), calc(var(--m-distance) * -0.5), 0) rotate(-8deg); }
  to { transform: translate3d(var(--m-distance), calc(var(--m-distance) * 0.5), 0) rotate(8deg); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root">
        <div className="m-orb"></div>
      </div>
  )
}

/* styles.css
.m-orb {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background:
    radial-gradient(38% 34% at 32% 28%, rgba(255, 255, 255, 0.95), transparent 62%),
    radial-gradient(70% 70% at 62% 68%, rgba(125, 20, 24, 0.85), transparent 70%),
    linear-gradient(160deg, #f0cd72, #b8763a 62%, #4a1220);
  box-shadow:
    inset 0 -18px 32px rgba(0, 0, 0, 0.5),
    0 28px 60px rgba(240, 205, 114, 0.18),
    0 0 90px rgba(240, 205, 114, 0.22);
  animation: m-orb var(--m-duration) ease-in-out infinite alternate;
}

@keyframes m-orb {
  from { transform: translate3d(calc(var(--m-distance) * -1), calc(var(--m-distance) * -0.5), 0) rotate(-8deg); }
  to { transform: translate3d(var(--m-distance), calc(var(--m-distance) * 0.5), 0) rotate(8deg); }
}
*/', '', '做一颗浮动光球：用三层 radial-gradient 叠出球体的高光、暗部与反光，外加金色外发光；以 8 秒周期左右漂移 ±18px 并轻微自转，缓动 ease-in-out 往返。', 'floating orb', 'OFFICIAL', 'READY'),
	('galaxy-background', '星系背景', 'Galaxy Background', '多层星点以不同速度横向掠过，形成星际穿越感；用 repeating-radial-gradient 生成星场，无需图片。', '高级效果', 'Landing Page', 'Cyber', 'CSS', 2, '官网首页,Game UI', 88, 90, 86, 88, 88, 'BALANCED', '用到模糊、离屏合成或较大面积动画：桌面端无压力，低端移动端建议减少同时播放的元素数量。', '[{"key": "--m-duration", "label": "穿越周期", "unit": "s", "min": 6, "max": 40, "step": 2, "default": 18}, {"key": "--m-distance", "label": "星密度", "unit": "%", "min": 6, "max": 30, "step": 2, "default": 14}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-galaxy"></div>
</div>', '', '.motion-root--bleed { padding: 0; }
.m-galaxy {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 100% at 50% 20%, #0d1024, #04050a 72%);
}
.m-galaxy::before,
.m-galaxy::after {
  content: "";
  position: absolute;
  inset: -50% -50% -50% -50%;
  background-image:
    radial-gradient(1.4px 1.4px at 20% 30%, #fff, transparent),
    radial-gradient(1.2px 1.2px at 68% 62%, #cbd5e1, transparent),
    radial-gradient(1.6px 1.6px at 44% 82%, #f0cd72, transparent),
    radial-gradient(1px 1px at 84% 22%, #fff, transparent),
    radial-gradient(1.3px 1.3px at 12% 74%, #94a3b8, transparent);
  background-size: 260px 260px;
  animation: m-warp var(--m-duration) linear infinite;
  opacity: 0.85;
}
.m-galaxy::after {
  background-size: 140px 140px;
  animation-duration: calc(var(--m-duration) * 0.6);
  opacity: 0.5;
}

@keyframes m-warp {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to { transform: translate3d(-160px, 90px, 0) scale(1.06); }
}', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-galaxy"></div>
    </div>
</template>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-galaxy {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 100% at 50% 20%, #0d1024, #04050a 72%);
}
.m-galaxy::before,
.m-galaxy::after {
  content: "";
  position: absolute;
  inset: -50% -50% -50% -50%;
  background-image:
    radial-gradient(1.4px 1.4px at 20% 30%, #fff, transparent),
    radial-gradient(1.2px 1.2px at 68% 62%, #cbd5e1, transparent),
    radial-gradient(1.6px 1.6px at 44% 82%, #f0cd72, transparent),
    radial-gradient(1px 1px at 84% 22%, #fff, transparent),
    radial-gradient(1.3px 1.3px at 12% 74%, #94a3b8, transparent);
  background-size: 260px 260px;
  animation: m-warp var(--m-duration) linear infinite;
  opacity: 0.85;
}
.m-galaxy::after {
  background-size: 140px 140px;
  animation-duration: calc(var(--m-duration) * 0.6);
  opacity: 0.5;
}

@keyframes m-warp {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to { transform: translate3d(-160px, 90px, 0) scale(1.06); }
}
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-galaxy"></div>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-galaxy {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: radial-gradient(120% 100% at 50% 20%, #0d1024, #04050a 72%);
}
.m-galaxy::before,
.m-galaxy::after {
  content: "";
  position: absolute;
  inset: -50% -50% -50% -50%;
  background-image:
    radial-gradient(1.4px 1.4px at 20% 30%, #fff, transparent),
    radial-gradient(1.2px 1.2px at 68% 62%, #cbd5e1, transparent),
    radial-gradient(1.6px 1.6px at 44% 82%, #f0cd72, transparent),
    radial-gradient(1px 1px at 84% 22%, #fff, transparent),
    radial-gradient(1.3px 1.3px at 12% 74%, #94a3b8, transparent);
  background-size: 260px 260px;
  animation: m-warp var(--m-duration) linear infinite;
  opacity: 0.85;
}
.m-galaxy::after {
  background-size: 140px 140px;
  animation-duration: calc(var(--m-duration) * 0.6);
  opacity: 0.5;
}

@keyframes m-warp {
  from { transform: translate3d(0, 0, 0) scale(1); }
  to { transform: translate3d(-160px, 90px, 0) scale(1.06); }
}
*/', '', '做星系穿越背景：用五个不同大小与颜色的 radial-gradient 星点以 260px 平铺成星场，两层星场分别以 18 秒与 10.8 秒向对角缓慢平移并微缩放，形成视差穿越感，不使用任何图片。', 'galaxy background', 'OFFICIAL', 'READY'),
	('shader-background', '着色器波纹', 'Shader Background', '用 Canvas 逐像素算正弦函数生成流动波纹（GLSL 着色器的等效 JS 版），可调频率与速度。', '高级效果', 'Game UI', 'Cyber', 'Canvas', 3, 'Game UI,AI 产品页', 95, 82, 84, 76, 85, 'GPU_ENHANCED', '依赖 GPU 或逐像素计算：观感最好，但在低端设备/集显上可能掉帧。建议开启硬件加速；移动端可减少粒子数、降低分辨率或按需启用。', '[{"key": "--m-duration", "label": "流动速度", "unit": "", "min": 0.2, "max": 3, "step": 0.1, "default": 1}, {"key": "--m-distance", "label": "波纹频率", "unit": "", "min": 2, "max": 16, "step": 1, "default": 7}]', '', '<div class="motion-root motion-root--bleed">
  <canvas class="m-shader"></canvas>
</div>', 'var canvas = document.querySelector(''.m-shader'');
var ctx = canvas.getContext(''2d'');
var scale = 6;
var width = 0;
var height = 0;

function resize() {
  var rect = canvas.getBoundingClientRect();
  canvas.width = Math.max(1, Math.floor(rect.width / scale));
  canvas.height = Math.max(1, Math.floor(rect.height / scale));
  width = canvas.width;
  height = canvas.height;
}

var time = 0;
function frame() {
  time += 0.02;
  var image = ctx.createImageData(width, height);
  for (var y = 0; y < height; y++) {
    for (var x = 0; x < width; x++) {
      var u = x / width - 0.5;
      var v = y / height - 0.5;
      var value = Math.sin(u * 7 + time) * 0.5 + Math.sin(v * 7 - time * 0.8) * 0.5 + Math.sin((u + v) * 5 + time * 0.6) * 0.5;
      var index = (y * width + x) * 4;
      image.data[index] = 20 + value * 90;
      image.data[index + 1] = 18 + value * 60;
      image.data[index + 2] = 34 + value * 120;
      image.data[index + 3] = 255;
    }
  }
  ctx.putImageData(image, 0, 0);
  requestAnimationFrame(frame);
}

resize();
window.addEventListener(''resize'', resize);
frame();', '.motion-root--bleed { padding: 0; }
.m-shader { display: block; width: 100%; height: 100%; background: #05060a; }', '<template>
    <div class="motion-root motion-root--bleed">
      <canvas class="m-shader"></canvas>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  var canvas = document.querySelector(''.m-shader'');
  var ctx = canvas.getContext(''2d'');
  var scale = 6;
  var width = 0;
  var height = 0;

  function resize() {
    var rect = canvas.getBoundingClientRect();
    canvas.width = Math.max(1, Math.floor(rect.width / scale));
    canvas.height = Math.max(1, Math.floor(rect.height / scale));
    width = canvas.width;
    height = canvas.height;
  }

  var time = 0;
  function frame() {
    time += 0.02;
    var image = ctx.createImageData(width, height);
    for (var y = 0; y < height; y++) {
      for (var x = 0; x < width; x++) {
        var u = x / width - 0.5;
        var v = y / height - 0.5;
        var value = Math.sin(u * 7 + time) * 0.5 + Math.sin(v * 7 - time * 0.8) * 0.5 + Math.sin((u + v) * 5 + time * 0.6) * 0.5;
        var index = (y * width + x) * 4;
        image.data[index] = 20 + value * 90;
        image.data[index + 1] = 18 + value * 60;
        image.data[index + 2] = 34 + value * 120;
        image.data[index + 3] = 255;
      }
    }
    ctx.putImageData(image, 0, 0);
    requestAnimationFrame(frame);
  }

  resize();
  window.addEventListener(''resize'', resize);
  frame();
})
</script>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-shader { display: block; width: 100%; height: 100%; background: #05060a; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    var canvas = document.querySelector(''.m-shader'');
    var ctx = canvas.getContext(''2d'');
    var scale = 6;
    var width = 0;
    var height = 0;

    function resize() {
      var rect = canvas.getBoundingClientRect();
      canvas.width = Math.max(1, Math.floor(rect.width / scale));
      canvas.height = Math.max(1, Math.floor(rect.height / scale));
      width = canvas.width;
      height = canvas.height;
    }

    var time = 0;
    function frame() {
      time += 0.02;
      var image = ctx.createImageData(width, height);
      for (var y = 0; y < height; y++) {
        for (var x = 0; x < width; x++) {
          var u = x / width - 0.5;
          var v = y / height - 0.5;
          var value = Math.sin(u * 7 + time) * 0.5 + Math.sin(v * 7 - time * 0.8) * 0.5 + Math.sin((u + v) * 5 + time * 0.6) * 0.5;
          var index = (y * width + x) * 4;
          image.data[index] = 20 + value * 90;
          image.data[index + 1] = 18 + value * 60;
          image.data[index + 2] = 34 + value * 120;
          image.data[index + 3] = 255;
        }
      }
      ctx.putImageData(image, 0, 0);
      requestAnimationFrame(frame);
    }

    resize();
    window.addEventListener(''resize'', resize);
    frame();
  }, [])

  return (
      <div className="motion-root motion-root--bleed">
        <canvas className="m-shader"></canvas>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-shader { display: block; width: 100%; height: 100%; background: #05060a; }
*/', '', '用 Canvas 做流动波纹背景：按 1/6 分辨率逐像素计算三个正弦波叠加的值（频率 7、速度 0.02），映射到深蓝紫的 RGB 上再用 putImageData 放大绘制；用 requestAnimationFrame 驱动时间。', 'shader background', 'OFFICIAL', 'READY'),
	('three-scene', '三维场景', 'Three.js Scene', '一组几何体在场景中自转与公转，带方向光与雾；这是 Three.js 类模板的骨架，可替换模型。', '高级效果', 'Game UI', 'Cyber', 'Three.js', 3, 'Game UI,官网首页', 96, 80, 84, 74, 85, 'GPU_ENHANCED', '依赖 GPU 或逐像素计算：观感最好，但在低端设备/集显上可能掉帧。建议开启硬件加速；移动端可减少粒子数、降低分辨率或按需启用。', '[{"key": "--m-duration", "label": "自转速度", "unit": "", "min": 0.2, "max": 3, "step": 0.1, "default": 1}]', '', '<div class="motion-root motion-root--bleed">
  <div class="m-three"><span class="m-three-hint">Three.js</span></div>
</div>', '// 预览用轻量等效实现：无 CDN 依赖，用 Canvas 画一组旋转的多面体线框，
// 导出代码里给的是真正的 Three.js 版本（见「代码」页签）
var host = document.querySelector(''.m-three'');
var canvas = document.createElement(''canvas'');
host.appendChild(canvas);
var ctx = canvas.getContext(''2d'');
var angle = 0;
var shapes = [
  { r: 46, orbit: 0, speed: 1 },
  { r: 26, orbit: 1, speed: -1.4 },
  { r: 14, orbit: 2, speed: 2.1 }
];

function resize() {
  var rect = host.getBoundingClientRect();
  canvas.width = rect.width;
  canvas.height = rect.height;
}

function polyhedron(cx, cy, radius, rotation) {
  var points = [];
  for (var i = 0; i < 6; i++) {
    var a = rotation + (i / 6) * Math.PI * 2;
    points.push([cx + Math.cos(a) * radius, cy + Math.sin(a) * radius * 0.62]);
  }
  ctx.beginPath();
  points.forEach(function (point, index) {
    index === 0 ? ctx.moveTo(point[0], point[1]) : ctx.lineTo(point[0], point[1]);
  });
  ctx.closePath();
  ctx.stroke();
}

function frame() {
  angle += 0.012;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  var cx = canvas.width / 2;
  var cy = canvas.height / 2;
  shapes.forEach(function (shape, index) {
    ctx.strokeStyle = index === 0 ? ''rgba(240, 205, 114, 0.9)'' : ''rgba(148, 163, 184, 0.7)'';
    ctx.lineWidth = index === 0 ? 1.6 : 1;
    var orbitX = Math.cos(angle * shape.speed + shape.orbit) * 54;
    var orbitY = Math.sin(angle * shape.speed + shape.orbit) * 28;
    polyhedron(cx + orbitX, cy + orbitY, shape.r, angle * shape.speed);
  });
  requestAnimationFrame(frame);
}

resize();
window.addEventListener(''resize'', resize);
frame();', '.motion-root--bleed { padding: 0; }
.m-three {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 0%, #101528, #05060a 70%);
  display: grid;
  place-items: center;
  overflow: hidden;
}
.m-three canvas { position: absolute; inset: 0; }
.m-three-hint { position: relative; color: rgba(232, 234, 237, 0.35); font: 600 12px/1 system-ui, sans-serif; letter-spacing: 0.16em; text-transform: uppercase; }', '<template>
    <div class="motion-root motion-root--bleed">
      <div class="m-three"><span class="m-three-hint">Three.js</span></div>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from ''vue''

// 组件挂载后再取 DOM：模板里的元素此时已就绪
onMounted(() => {
  // 预览用轻量等效实现：无 CDN 依赖，用 Canvas 画一组旋转的多面体线框，
  // 导出代码里给的是真正的 Three.js 版本（见「代码」页签）
  var host = document.querySelector(''.m-three'');
  var canvas = document.createElement(''canvas'');
  host.appendChild(canvas);
  var ctx = canvas.getContext(''2d'');
  var angle = 0;
  var shapes = [
    { r: 46, orbit: 0, speed: 1 },
    { r: 26, orbit: 1, speed: -1.4 },
    { r: 14, orbit: 2, speed: 2.1 }
  ];

  function resize() {
    var rect = host.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
  }

  function polyhedron(cx, cy, radius, rotation) {
    var points = [];
    for (var i = 0; i < 6; i++) {
      var a = rotation + (i / 6) * Math.PI * 2;
      points.push([cx + Math.cos(a) * radius, cy + Math.sin(a) * radius * 0.62]);
    }
    ctx.beginPath();
    points.forEach(function (point, index) {
      index === 0 ? ctx.moveTo(point[0], point[1]) : ctx.lineTo(point[0], point[1]);
    });
    ctx.closePath();
    ctx.stroke();
  }

  function frame() {
    angle += 0.012;
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    var cx = canvas.width / 2;
    var cy = canvas.height / 2;
    shapes.forEach(function (shape, index) {
      ctx.strokeStyle = index === 0 ? ''rgba(240, 205, 114, 0.9)'' : ''rgba(148, 163, 184, 0.7)'';
      ctx.lineWidth = index === 0 ? 1.6 : 1;
      var orbitX = Math.cos(angle * shape.speed + shape.orbit) * 54;
      var orbitY = Math.sin(angle * shape.speed + shape.orbit) * 28;
      polyhedron(cx + orbitX, cy + orbitY, shape.r, angle * shape.speed);
    });
    requestAnimationFrame(frame);
  }

  resize();
  window.addEventListener(''resize'', resize);
  frame();
})
</script>

<style scoped>
.motion-root--bleed { padding: 0; }
.m-three {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 0%, #101528, #05060a 70%);
  display: grid;
  place-items: center;
  overflow: hidden;
}
.m-three canvas { position: absolute; inset: 0; }
.m-three-hint { position: relative; color: rgba(232, 234, 237, 0.35); font: 600 12px/1 system-ui, sans-serif; letter-spacing: 0.16em; text-transform: uppercase; }
</style>', 'import { useEffect } from ''react''
import ''./styles.css''

export default function MotionDemo() {
  useEffect(() => {
    // 预览用轻量等效实现：无 CDN 依赖，用 Canvas 画一组旋转的多面体线框，
    // 导出代码里给的是真正的 Three.js 版本（见「代码」页签）
    var host = document.querySelector(''.m-three'');
    var canvas = document.createElement(''canvas'');
    host.appendChild(canvas);
    var ctx = canvas.getContext(''2d'');
    var angle = 0;
    var shapes = [
      { r: 46, orbit: 0, speed: 1 },
      { r: 26, orbit: 1, speed: -1.4 },
      { r: 14, orbit: 2, speed: 2.1 }
    ];

    function resize() {
      var rect = host.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
    }

    function polyhedron(cx, cy, radius, rotation) {
      var points = [];
      for (var i = 0; i < 6; i++) {
        var a = rotation + (i / 6) * Math.PI * 2;
        points.push([cx + Math.cos(a) * radius, cy + Math.sin(a) * radius * 0.62]);
      }
      ctx.beginPath();
      points.forEach(function (point, index) {
        index === 0 ? ctx.moveTo(point[0], point[1]) : ctx.lineTo(point[0], point[1]);
      });
      ctx.closePath();
      ctx.stroke();
    }

    function frame() {
      angle += 0.012;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      var cx = canvas.width / 2;
      var cy = canvas.height / 2;
      shapes.forEach(function (shape, index) {
        ctx.strokeStyle = index === 0 ? ''rgba(240, 205, 114, 0.9)'' : ''rgba(148, 163, 184, 0.7)'';
        ctx.lineWidth = index === 0 ? 1.6 : 1;
        var orbitX = Math.cos(angle * shape.speed + shape.orbit) * 54;
        var orbitY = Math.sin(angle * shape.speed + shape.orbit) * 28;
        polyhedron(cx + orbitX, cy + orbitY, shape.r, angle * shape.speed);
      });
      requestAnimationFrame(frame);
    }

    resize();
    window.addEventListener(''resize'', resize);
    frame();
  }, [])

  return (
      <div className="motion-root motion-root--bleed">
        <div className="m-three"><span className="m-three-hint">Three.js</span></div>
      </div>
  )
}

/* styles.css
.motion-root--bleed { padding: 0; }
.m-three {
  position: relative;
  width: 100%;
  height: 100%;
  background: radial-gradient(120% 100% at 50% 0%, #101528, #05060a 70%);
  display: grid;
  place-items: center;
  overflow: hidden;
}
.m-three canvas { position: absolute; inset: 0; }
.m-three-hint { position: relative; color: rgba(232, 234, 237, 0.35); font: 600 12px/1 system-ui, sans-serif; letter-spacing: 0.16em; text-transform: uppercase; }
*/', 'import * as THREE from ''three''

// 场景 / 相机 / 渲染器
const scene = new THREE.Scene()
scene.fog = new THREE.Fog(0x05060a, 4, 12)

const camera = new THREE.PerspectiveCamera(45, 1, 0.1, 100)
camera.position.set(0, 0, 6)

const renderer = new THREE.WebGLRenderer({ antialias: true })
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
renderer.setClearColor(0x05060a, 1)

const container = document.getElementById(''scene'')
container.appendChild(renderer.domElement)

// 灯光：一盏主光 + 环境光，避免暗面全黑
scene.add(new THREE.AmbientLight(0xffffff, 0.45))
const key = new THREE.DirectionalLight(0xf0cd72, 1.4)
key.position.set(3, 4, 5)
scene.add(key)

// 三个几何体：中心多面体 + 两个轨道上的小球
const core = new THREE.Mesh(
  new THREE.IcosahedronGeometry(1.1, 1),
  new THREE.MeshStandardMaterial({ color: 0xf0cd72, metalness: 0.6, roughness: 0.28 })
)
scene.add(core)

const orbiters = [0, 1].map((index) => {
  const mesh = new THREE.Mesh(
    new THREE.SphereGeometry(0.22, 24, 24),
    new THREE.MeshStandardMaterial({ color: 0x94a3b8, metalness: 0.4, roughness: 0.4 })
  )
  mesh.userData.phase = index * Math.PI
  scene.add(mesh)
  return mesh
})

function resize() {
  const { clientWidth: width, clientHeight: height } = container
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}

const clock = new THREE.Clock()
function animate() {
  const elapsed = clock.getElapsedTime()
  core.rotation.y = elapsed * 0.6
  core.rotation.x = elapsed * 0.25
  orbiters.forEach((mesh, index) => {
    const angle = elapsed * (0.9 + index * 0.4) + mesh.userData.phase
    const radius = 2 + index * 0.5
    mesh.position.set(Math.cos(angle) * radius, Math.sin(angle * 1.3) * 0.5, Math.sin(angle) * radius)
  })
  renderer.render(scene, camera)
  requestAnimationFrame(animate)
}

resize()
window.addEventListener(''resize'', resize)
animate()', '用 Three.js 做一个三维场景：中心一个二十面体（金属质感金色）、两个小球在半径 2-2.5 的轨道上公转，一盏平行光加环境光，加雾营造纵深；相机 45 度位于 z=6，窗口尺寸变化时同步 renderer 与相机比例。', 'three scene', 'OFFICIAL', 'READY')
ON DUPLICATE KEY UPDATE
	`name` = VALUES(`name`), `name_en` = VALUES(`name_en`), `description` = VALUES(`description`),
	`category` = VALUES(`category`), `scene` = VALUES(`scene`), `style` = VALUES(`style`),
	`technology` = VALUES(`technology`), `difficulty` = VALUES(`difficulty`), `best_for` = VALUES(`best_for`),
	`score_visual` = VALUES(`score_visual`), `score_code` = VALUES(`score_code`),
	`score_reuse` = VALUES(`score_reuse`), `score_perf` = VALUES(`score_perf`), `score` = VALUES(`score`),
	`runtime_tier` = VALUES(`runtime_tier`), `runtime_note` = VALUES(`runtime_note`), 	`params` = VALUES(`params`), `preview_html` = VALUES(`preview_html`), `preview_js` = VALUES(`preview_js`),
	`css_code` = VALUES(`css_code`), `vue_code` = VALUES(`vue_code`), `react_code` = VALUES(`react_code`),
	`three_code` = VALUES(`three_code`), `prompt` = VALUES(`prompt`), `tags` = VALUES(`tags`);

-- ---------------------------------------------------------------------
-- 2. 组合方案（Motion Recipe）
-- ---------------------------------------------------------------------
INSERT INTO `motion_recipe`
	(`recipe_key`, `name`, `description`, `scene`, `style`, `best_for`, `score`, `template_keys`, `prompt`, `status`)
VALUES
	('premium-hero', 'Premium Hero', '首屏三件套：极光背景铺氛围、粒子星网给纵深、标题揭示出现，最后用磁吸按钮收住视线。', 'Landing Page', 'Luxury', '官网首页,AI 产品页', 89, '["aurora-background", "particle-network", "text-reveal", "magnetic-button"]', '做一个高级感首屏：背景是缓慢漂移的极光渐变，上面叠一层粒子星网；主标题用 clip-path 从下向上揭示，主按钮带磁吸效果。整体节奏 1.2 秒内完成入场，不要同时出现。', 'READY'),
	('luxury-product-card', 'Luxury Product Card', '产品卡三件套：三维倾斜给触感、玻璃悬停给反馈、轻微浮动让静止状态也有呼吸。', 'Portfolio', 'Luxury', '个人作品集,官网首页', 90, '["tilt-card-3d", "glass-card-hover", "floating-card"]', '做一个产品卡片：静止时轻微上下浮动；鼠标移入时随指针做三维倾斜并点亮高光；卡片本身用玻璃拟态 + 1px 亮边框，悬停时上浮 8px。', 'READY'),
	('ai-saas-landing', 'AI SaaS Landing', 'AI 产品首页的完整配方：网格渐变底、浮动光球做视觉锚点、内容柔和淡入、边框流光提示可交互。', 'AI SaaS', 'Cyber', 'AI 产品页,官网首页', 90, '["mesh-gradient", "floating-orb", "smooth-fade", "glow-border", "magnetic-button"]', '做 AI 产品首页：背景是网格渐变，中央一颗浮动光球作为视觉锚点；下方三个特性卡带流光描边，文案依次淡入，主按钮磁吸；整体冷静、有科技感，不要花哨。', 'READY'),
	('portfolio-opening', 'Portfolio Opening', '作品集开场：标题揭示 → 光斑跟随光标 → 作品卡缩放浮现，三段式把注意力从名字带到作品。', 'Portfolio', 'Minimal', '个人作品集', 89, '["text-reveal", "cursor-follow", "scale-reveal"]', '做个人作品集开场：先揭示名字标题，然后一团柔光跟随鼠标，最后作品卡从 0.92 缩放浮现；整体克制，只保留必要动效，让别人记住作品而不是特效。', 'READY'),
	('dashboard-boot', 'Dashboard Boot', '后台开屏：面板从右侧切入、卡片错落出现、关键数字滚动到目标值，最后弹一条完成通知。', 'Dashboard', 'Minimal', '后台系统,数据看板', 88, '["dashboard-counter", "card-stagger", "page-transition", "notification-popup"]', '做后台开屏：内容面板从右滑入淡入，统计卡片错落出现（每张差 0.09 秒），核心数字从 0 滚动到目标值，全部完成后右上角弹一条通知。整体不超过 2 秒。', 'READY')
ON DUPLICATE KEY UPDATE
	`name` = VALUES(`name`), `description` = VALUES(`description`), `scene` = VALUES(`scene`),
	`style` = VALUES(`style`), `best_for` = VALUES(`best_for`), `score` = VALUES(`score`),
	`template_keys` = VALUES(`template_keys`), `prompt` = VALUES(`prompt`);

-- ---------------------------------------------------------------------
-- 3. 人工评分记录（评分 + 理由，与算法的推荐指数分开存）
-- ---------------------------------------------------------------------
INSERT INTO `motion_rating` (`target_type`, `target_key`, `score`, `reason`)
VALUES
	('TEMPLATE', 'hero-entrance', 5, '首屏三段式节奏最省力：改标题与按钮文案就能直接用，几乎不需要调参。'),
	('TEMPLATE', 'glass-card-hover', 5, '玻璃卡片悬停是这类风格里最稳的一个：blur 只作用在卡片自身，性能可控。'),
	('TEMPLATE', 'particle-network', 4, '效果好但粒子数与连线距离需要按屏幕调；移动端建议降到 30 个粒子。'),
	('TEMPLATE', 'three-scene', 4, '作为 Three.js 骨架很完整，但真实项目要替换模型与光照，属于起点而非成品。'),
	('TEMPLATE', 'noise-texture', 5, '一行内联 SVG 就消掉了纯色背景的塑料感，性价比最高的质感手段。'),
	('RECIPE', 'premium-hero', 5, '四个模板叠出来的效果明显强于任何单个动效，是「组合优于模板」的直观例子。'),
	('RECIPE', 'ai-saas-landing', 4, '配方完整，但网格渐变 + 流光 + 光球同时上会比较满，实际项目建议去掉其中一个。')
ON DUPLICATE KEY UPDATE `score` = VALUES(`score`), `reason` = VALUES(`reason`);
