# -*- coding: utf-8 -*-
"""组合方案（Motion Recipe）数据源。

一个方案 = 若干模板按应用顺序叠出来的效果，每一步带：
  templateKey  用哪个已入库的模板（必须是真实存在的 key）
  stage        这一步在页面里负责哪一层（背景 / 内容 / 滚动 / 交互 / 收尾）
  role         作用：为什么在这一步用它（界面上要显示这一列）

评分不在数据里写死：由 mr_build.py 按服务端同一套权重（视觉 30 / 代码 25 / 复用 25 / 性能 20）
从库里取子分算出来，避免「方案写着 92、成员平均只有 85」这种对不上的情况。
"""

RECIPES = [
	# ---------------------------------------------------------------- Landing Page
	dict(
		key='apple-product-page',
		name='Apple Product Page',
		scene='Landing Page', style='Minimal', best_for='官网首页,产品页',
		description='苹果式产品页：先让页面安静下来，再用一句标题、一次滚动、一张卡片把注意力收到产品上。',
		steps=[
			('smooth-fade', '背景', '整页容器先柔和淡入，避免首帧闪一下'),
			('slide-up', '内容', '主标题与副标题上滑淡入，一次只动一个层级'),
			('scroll-reveal', '滚动', '向下滚动时逐块点亮，讲解顺序由滚动控制'),
			('pricing-card-hover', '交互', '规格或版本对比卡带上浮悬停，把差异讲清楚'),
		],
		prompt='做一个苹果风格的产品页：整页柔和淡入，主标题上滑出现；向下滚动时各内容块逐级点亮，规格卡悬停时轻微上浮并加深投影。全程只使用 transform / opacity，滚动动画只在元素进入视口时触发一次，不重复播放。',
	),
	dict(
		key='luxury-hotel',
		name='Luxury Hotel',
		scene='Landing Page', style='Luxury', best_for='高级酒店官网,品牌官网',
		description='高级酒店官网：颗粒底纹压住廉价感，幕布式开场拉出空间，再用视差与磁吸按钮收尾。',
		steps=[
			('grain-overlay-bg', '背景', '叠一层极细颗粒，避免大面积渐变显得发灰'),
			('hero-split-curtain', '内容', '首屏像幕布一样从中间分开，先给空间再给内容'),
			('split-char-rise', '内容', '品牌名逐字上升，节奏比整块淡入更慢更贵'),
			('parallax-depth-3', '滚动', '三层视差让画面在滚动时有纵深'),
			('magnetic-pull-btn', '收尾', '预订按钮带磁吸，提示这是主行动点'),
		],
		prompt='做一个高级酒店官网首屏：背景铺一层细腻颗粒纹理，首屏用幕布分屏方式打开；品牌名逐字上升出现；滚动时背景 / 中景 / 前景三层以不同速度位移；主按钮带磁吸效果。整体节奏要慢，单步动效不低于 0.8 秒。',
	),
	dict(
		key='cyber-launch',
		name='Cyber Launch',
		scene='Landing Page', style='Cyber', best_for='科技感首页,发布会页',
		description='科技感发布页：星系底 + 星空漂移铺满屏幕，标题擦除进入，按钮在悬停时给出金属触感。',
		steps=[
			('galaxy-background', '背景', '星系渐变打底，给整页定下冷色调'),
			('starfield-drift-bg', '背景', '星空缓慢漂移，让静止画面也不死板'),
			('headline-clip-wipe', '内容', '标题从左到右擦除出现，比淡入更有发布感'),
			('magnetic-button', '收尾', '主按钮磁吸跟随指针，引导点击'),
		],
		prompt='做一个科技感发布页：背景是缓慢旋转的星系渐变，上面叠一层缓慢漂移的星空；主标题用 clip-path 从左侧擦除出现；主按钮磁吸。整体冷色、克制，不要发光过曝。',
	),
	dict(
		key='scroll-story',
		name='Scroll Story',
		scene='Landing Page', style='Minimal', best_for='长页叙事,品牌故事页',
		description='长页叙事：进度条告诉用户走到哪，分屏钉住讲一段，剩下的交给滚动逐级点亮。',
		steps=[
			('scroll-progress-bar', '滚动', '顶部进度条让长页有「还有多少」的预期'),
			('sticky-pin-panel', '内容', '关键一段钉住不动，让文案自己走完'),
			('scroll-reveal', '滚动', '其余内容进入视口时逐级点亮'),
			('text-reveal', '收尾', '结尾用一句揭示式文案收住'),
		],
		prompt='做一个长页叙事页面：顶部有滚动进度条；中间用 sticky 钉住一个分屏，让文字在固定画面里逐段替换；其余内容块进入视口时逐级点亮；结尾一句话用揭示方式出现。滚动动效一律使用 IntersectionObserver，只触发一次。',
	),
	dict(
		key='organic-brand',
		name='Organic Brand',
		scene='Landing Page', style='Organic', best_for='品牌官网,生活方式页',
		description='治愈系品牌页：流体质感渐变打底，内容用模糊恢复的方式慢慢显影，卡片保持轻微浮动。',
		steps=[
			('liquid-gradient', '背景', '流体质感渐变让画面像一直在轻轻呼吸'),
			('blur-focus-in', '内容', '文案从模糊恢复到清晰，比淡入更柔和'),
			('floating-card', '内容', '内容卡静止时轻微上下浮动，去掉生硬边界'),
			('arrow-swipe-btn', '交互', '按钮用横向滑动给反馈，克制但明确'),
		],
		prompt='做一个治愈系品牌页：背景是缓慢流动的流体渐变；标题与段落从 blur(10px) 恢复到清晰；内容卡静止时以 6 秒周期轻微上下浮动 6px；按钮悬停时箭头横向滑出。整体节奏舒缓，不要出现硬切。',
	),

	# ---------------------------------------------------------------- AI SaaS
	dict(
		key='ai-model-launch',
		name='AI Model Launch',
		scene='AI SaaS', style='Cyber', best_for='AI 产品页,模型发布页',
		description='模型发布页：网格渐变底 + 粒子星网给纵深，标题擦除进入，能力卡用流光描边提示可交互。',
		steps=[
			('mesh-gradient', '背景', '网格渐变打底，科技感来自结构而不是特效'),
			('particle-network', '背景', '粒子星网给画面纵深，暗示「网络 / 连接」'),
			('headline-clip-wipe', '内容', '模型名擦除出现，配合一次轻微上移'),
			('glow-border', '交互', '能力卡悬停时边框流光，提示可以点开'),
			('glow-pulse-cta', '收尾', '主 CTA 呼吸发光，把转化点放在最后一屏'),
		],
		prompt='做一个 AI 模型发布页：背景用网格渐变 + 粒子星网两层叠加；模型名用 clip-path 横向擦除出现；三项能力卡悬停时边框流光扫过；主 CTA 按钮以 2.6 秒周期呼吸发光。粒子数量不超过 60，移动端降到 30。',
	),
	dict(
		key='ai-analytics-product',
		name='AI Analytics Product',
		scene='AI SaaS', style='Glass', best_for='AI 产品页,数据产品页',
		description='数据智能产品页：模糊恢复的文案 + 浮动光球当视觉锚点，卡片用光斑跟随强调「智能」。',
		steps=[
			('blur-reveal', '内容', '说明文案从模糊恢复，像镜头对焦'),
			('floating-orb', '背景', '一颗浮动光球当整页的视觉锚点'),
			('spotlight-card', '交互', '指针靠近卡片时亮起光斑，强调可探索'),
			('modal-morph', '收尾', '点开示例时用形变弹窗，不打断上下文'),
		],
		prompt='做一个 AI 数据分析产品页：顶部说明文案从 blur 恢复到清晰；画面中有一颗缓慢上下浮动的光球作为视觉锚点；能力卡在指针靠近时出现跟随光斑；点击「看示例」时用形变方式打开弹窗。整体透明度层次要清楚，不要堆叠过多模糊。',
	),
	dict(
		key='ai-wellness',
		name='AI Wellness',
		scene='AI SaaS', style='Organic', best_for='AI 产品页,健康类产品',
		description='AI 健康产品页：融合球与漂移网格给「活着的」感觉，渐变文字承担品牌语气。',
		steps=[
			('canvas-metaball', '背景', '融合球缓慢变形，暗示数据在流动'),
			('mesh-drift-bg', '背景', '网格渐变漂移，和融合球形成两层纵深'),
			('gradient-text-flow', '内容', '品牌主张用渐变流动，替代普通标题'),
			('glow-pulse-cta', '收尾', 'CTA 呼吸发光，给页面一个温和的落点'),
		],
		prompt='做一个 AI 健康产品页：背景用 Canvas 融合球 + 漂移网格两层；品牌主张用渐变文字缓慢流动；主 CTA 以 3 秒周期呼吸发光。整体柔和不刺眼，动画帧率优先保证 60fps，粒子与模糊半径按屏幕缩放。',
	),

	# ---------------------------------------------------------------- Portfolio
	dict(
		key='designer-portfolio',
		name='Designer Portfolio',
		scene='Portfolio', style='Luxury', best_for='个人作品集,设计师主页',
		description='设计师作品集：颗粒底 + 缩放揭示，滚动时作品以不同速度掠过，卡片悬停浮起。',
		steps=[
			('grain-overlay-bg', '背景', '颗粒质感让整页更像印刷品而不是网页'),
			('scale-reveal', '内容', '首个作品从 0.92 放大浮现，先给一个「作品」信号'),
			('parallax-depth-3', '滚动', '作品图分批以不同速度移动，制造层次'),
			('glass-lift-hover', '交互', '作品卡悬停时上浮并加深投影'),
		],
		prompt='做一个设计师作品集：整页叠一层细颗粒；首个作品从 0.92 缩放浮现；滚动时作品图分三层以 0.85 / 1.0 / 1.15 的速度位移；作品卡悬停上浮 8px。整体留白要大，动效只为作品服务。',
	),
	dict(
		key='photographer-gallery',
		name='Photographer Gallery',
		scene='Portfolio', style='Minimal', best_for='摄影作品集,画廊',
		description='摄影画廊：照片从模糊对焦进来，滚动视差让画面连续，翻面卡用来放拍摄信息。',
		steps=[
			('blur-focus-in', '内容', '照片从失焦到清晰，像镜头对焦'),
			('image-parallax', '滚动', '滚动时照片与说明以不同速度移动'),
			('flip-reveal-card', '交互', '翻面卡放拍摄参数，不打断浏览节奏'),
			('cursor-follow', '收尾', '光标跟随一团柔光，代替传统指针'),
		],
		prompt='做一个摄影作品集：每张照片进入视口时从 blur(14px) 恢复到清晰；滚动时照片与说明文字以不同速度位移；点击照片翻面显示拍摄参数；鼠标移动时有一团柔光跟随。整体以图片为主，动效克制。',
	),
	dict(
		key='developer-homepage',
		name='Developer Homepage',
		scene='Portfolio', style='Cyber', best_for='个人主页,开发者主页',
		description='开发者主页：星空底 + 打字机自我介绍，网格错落揭示技术栈，卡片光斑跟随指针。',
		steps=[
			('starfield-drift-bg', '背景', '星空缓慢漂移，给深色页面一点呼吸'),
			('typewriter-caret', '内容', '自我介绍逐字打出来，像终端在说话'),
			('stagger-grid-reveal', '滚动', '技术栈网格错落点亮，每格差 0.06 秒'),
			('spotlight-card', '交互', '项目卡在指针靠近时亮起光斑'),
		],
		prompt='做一个开发者主页：背景是缓慢漂移的星空；顶部用打字机效果逐字输出一句自我介绍并带光标闪烁；滚动到技术栈时网格逐格错落点亮（每格延迟 0.06 秒）；项目卡悬停出现跟随光斑。整体深色、等宽字体。',
	),
	dict(
		key='studio-showcase',
		name='Studio Showcase',
		scene='Portfolio', style='Glass', best_for='工作室官网,团队主页',
		description='工作室展示页：玻璃卡打底，倾斜反光给触感，团队与案例用模糊聚焦分批出现。',
		steps=[
			('glass-card-hover', '内容', '玻璃卡打底，一眼看出是「工作室」的气质'),
			('tilt-glare-card', '交互', '案例卡随指针倾斜并点亮反光'),
			('glass-lift-hover', '交互', '团队卡悬停浮起，与案例卡形成两种层级'),
			('blur-focus-in', '收尾', '结尾的联系方式用模糊聚焦出现'),
		],
		prompt='做一个工作室展示页：内容用玻璃拟态卡片承载；案例卡随鼠标做三维倾斜并点亮高光；团队卡悬停上浮 8px；结尾的联系方式从模糊恢复到清晰。模糊只作用在卡片自身，避免整页 backdrop-filter 拖慢滚动。',
	),

	# ---------------------------------------------------------------- Dashboard
	dict(
		key='analytics-console',
		name='Analytics Console',
		scene='Dashboard', style='Cyber', best_for='数据看板,分析后台',
		description='数据分析台：数字位翻滚呈现指标，网格错落铺开面板，点击有明确水波反馈。',
		steps=[
			('digit-roll-up', '内容', '核心指标逐位翻滚到目标值，先看数再看图'),
			('stagger-grid-reveal', '内容', '分析面板错落铺开，避免整屏同时闪'),
			('scroll-progress-bar', '滚动', '长报表用顶部进度条标出位置'),
			('ripple-click-btn', '交互', '筛选按钮带水波反馈，点击后立即有回应'),
		],
		prompt='做一个数据分析台：顶部三个核心指标用数字位翻滚到目标值；下方面板错落出现（每块差 0.07 秒）；长报表顶部有滚动进度条；筛选按钮点击时从点击点扩散水波。数字滚动要在 0.9 秒内收敛并停住。',
	),
	dict(
		key='ops-console',
		name='Ops Console',
		scene='Dashboard', style='Glass', best_for='运维后台,监控台',
		description='运维控制台：面板转场切入，关键数字滚动，异常用弹入提示，弹窗用形变减少跳变感。',
		steps=[
			('page-transition', '内容', '切换视图时面板整体切入，保持方位感'),
			('dashboard-counter', '内容', '关键指标滚动到目标值，一眼看到变化'),
			('notification-popup', '交互', '告警从右上角弹入，带一条进度指示'),
			('modal-morph', '收尾', '查看详情时用形变弹窗，不切断当前上下文'),
		],
		prompt='做一个运维控制台：切换视图时面板从右侧整体切入淡入；关键指标在 0.8 秒内滚动到目标值；告警以右上角弹入方式出现并在 4 秒后自动收起；点击「详情」时弹窗由被点击的卡片形变展开。整体深色玻璃质感。',
	),
	dict(
		key='minimal-report',
		name='Minimal Report',
		scene='Dashboard', style='Minimal', best_for='报表页,周报看板',
		description='极简报表页：只保留三件事——数字滚动、进度提示、一条完成通知。',
		steps=[
			('dashboard-counter', '内容', '数字滚动是这一页唯一的「主角动效」'),
			('scroll-progress-bar', '滚动', '长报表给一个阅读进度，减少翻找'),
			('notification-popup', '收尾', '加载完成只弹一条通知，不打断阅读'),
		],
		prompt='做一个极简报表页：核心数字在 0.8 秒内滚动到目标值；页面顶部有滚动进度条；数据加载完成后从右上角弹一条通知，4 秒后自动消失。除这三处外不要添加任何动画。',
	),

	# ---------------------------------------------------------------- Login
	dict(
		key='game-login',
		name='Game Login',
		scene='Login', style='Cyber', best_for='游戏登录页,电竞入口',
		description='游戏登录页：星空底 + 打字机标语，输入框流光待命，登录按钮按下有回弹。',
		steps=[
			('starfield-drift-bg', '背景', '星空漂移撑起「开场画面」的感觉'),
			('typewriter-caret', '内容', '版本标语逐字打出，像游戏启动时的读取行'),
			('glow-border', '交互', '输入框聚焦时边框流光，明确当前焦点'),
			('press-depth-card', '收尾', '登录按钮按下时下沉再回弹，手感扎实'),
		],
		prompt='做一个游戏登录页：背景是缓慢漂移的星空；上方标语逐字打出并保留光标闪烁；输入框聚焦时边框流光扫过；登录按钮按下时下沉 2px 并在 250ms 内回弹。整体深色，霓虹只用在边框与按钮上。',
	),
	dict(
		key='minimal-login',
		name='Minimal Login',
		scene='Login', style='Glass', best_for='后台登录页,产品登录',
		description='极简登录页：卡片整体浮入，输入框从模糊恢复，其余一切安静。',
		steps=[
			('login-animation', '内容', '登录卡整体浮入，一次把结构交代清楚'),
			('blur-reveal', '内容', '表单项从模糊恢复，视线自然落到第一个输入框'),
			('glow-border', '交互', '提交按钮悬停时边框亮起，给一个轻反馈'),
		],
		prompt='做一个极简登录页：登录卡从下方 24px 上浮淡入；表单元素依次从 blur(8px) 恢复到清晰（每项延迟 0.08 秒）；提交按钮悬停时边框流光。整页只保留玻璃卡与一处渐变背景，不要其它装饰。',
	),

	# ---------------------------------------------------------------- Game UI
	dict(
		key='esports-site',
		name='Esports Site',
		scene='Game UI', style='Cyber', best_for='电竞官网,战队主页',
		description='电竞官网：三维场景开场 + 着色器波纹，赛程卡片按压回弹，点击有水波反馈。',
		steps=[
			('three-scene', '背景', '三维场景做开场，直接立住「大制作」的印象'),
			('shader-background', '背景', '着色器波纹接住三维场景，滚动时不过于割裂'),
			('press-depth-card', '交互', '赛程卡片按压回弹，模拟点按物理感'),
			('ripple-click-btn', '收尾', '报名按钮点击扩散水波，反馈立刻可见'),
		],
		prompt='做一个电竞官网：首屏用 Three.js 场景开场，滚动后由着色器波纹背景接续；赛程卡片按下时下沉 2px、松开回弹；报名按钮点击时从点击点扩散水波。三维场景需提供降级方案，低端设备只保留静态首帧。',
	),
	dict(
		key='game-loading',
		name='Game Loading Screen',
		scene='Game UI', style='Cyber', best_for='加载界面,启动页',
		description='游戏加载界面：星空底上转着轨道方块，进度数字翻滚，底部一句话在上浮。',
		steps=[
			('starfield-drift-bg', '背景', '星空漂移让等待过程不显得像卡住'),
			('three-orbit-cubes', '内容', '轨道方块持续旋转，是最直接的「加载中」信号'),
			('digit-roll-up', '内容', '进度数字翻滚，把等待量化'),
			('slide-up', '收尾', '底部提示文案上浮淡入'),
		],
		prompt='做一个游戏加载界面：背景是缓慢漂移的星空；画面中央一组方块沿轨道持续旋转；进度以数字翻滚方式从 0 走到 100；底部提示文案上浮淡入。加载完成后所有动画同时淡出 200ms，不要硬切。',
	),
	dict(
		key='match-lobby',
		name='Match Lobby',
		scene='Game UI', style='Cyber', best_for='对战大厅,组队页',
		description='对战大厅：着色器波纹撑底，玩家卡错落铺开，就位提示弹入，卡片按下有回弹。',
		steps=[
			('shader-background', '背景', '着色器波纹给大厅一点动态，但不抢信息'),
			('stagger-grid-reveal', '内容', '玩家卡错落出现，每张差 0.06 秒'),
			('press-depth-card', '交互', '准备按钮按下回弹，手感明确'),
			('notification-popup', '收尾', '队友就位时右上角弹一条提示'),
		],
		prompt='做一个对战大厅：背景是低幅度的着色器波纹；玩家卡错落出现（每张延迟 0.06 秒）；准备按钮按下下沉并回弹；队友就位时右上角弹入提示并在 3 秒后收起。整体信息密度高，动效必须短（≤300ms）。',
	),

	# ---------------------------------------------------------------- 其余场景
	dict(
		key='jewelry-brand',
		name='Jewelry Brand',
		scene='Landing Page', style='Luxury', best_for='珠宝品牌,高端电商',
		description='珠宝品牌页：颗粒底压住反光，产品浮现交代形态，品牌名逐字上升，最后磁吸到购买。',
		steps=[
			('grain-overlay-bg', '背景', '细颗粒底让金属反光不至于刺眼'),
			('product-float-in', '内容', '产品以浮现方式出现，先把形态讲清楚'),
			('split-char-rise', '内容', '品牌名逐字上升，节奏慢下来'),
			('magnetic-pull-btn', '收尾', '购买按钮磁吸，明确唯一的行动点'),
		],
		prompt='做一个珠宝品牌页：背景铺细颗粒纹理；产品图从下方浮入并轻微放大；品牌名逐字上升；购买按钮带磁吸效果。整页只允许一处金属高光，节奏控制在 1.4 秒以上，避免廉价感。',
	),
	dict(
		key='saas-pricing',
		name='SaaS Pricing',
		scene='AI SaaS', style='Minimal', best_for='定价页,套餐对比',
		description='定价页：段落柔和淡入，三张价格卡依次上浮悬停，主按钮滑出箭头，滚动提示可见。',
		steps=[
			('smooth-fade', '内容', '顶部说明柔和淡入，不抢价格卡的注意力'),
			('pricing-card-hover', '交互', '三张套餐卡悬停上浮，对比差异一眼可见'),
			('arrow-swipe-btn', '交互', '主按钮箭头滑出，指明「下一步」'),
			('scroll-hint-bounce', '收尾', '底部提示继续滚动，长页不留死角'),
		],
		prompt='做一个定价页：顶部说明文字柔和淡入；三张套餐卡悬停上浮 8px 并加深投影，被选中的那张边框高亮；主按钮悬停时箭头横向滑出；页面底部有一个轻微上下跳动的滚动提示。除选中态外不要改变卡片尺寸，避免布局抖动。',
	),
	dict(
		key='startup-one-pager',
		name='Startup One-Pager',
		scene='Landing Page', style='Minimal', best_for='官网首页,创业一页纸',
		description='创业一页纸：淡入 + 上滑两段式开场，特性卡错落铺开，底部给一个滚动提示收尾。',
		steps=[
			('smooth-fade', '背景', '整页淡入，先把首帧的闪烁抹平'),
			('slide-up', '内容', '标题与主按钮上滑出现，顺序明确'),
			('card-stagger', '内容', '特性卡错落出现，每张差 0.09 秒'),
			('scroll-hint-bounce', '收尾', '底部提示继续滚动，引导读完一页'),
		],
		prompt='做一个创业一页纸：整页淡入；主标题与按钮依次上滑（差 0.12 秒）；三到四张特性卡错落出现（每张差 0.09 秒）；底部有轻微跳动的滚动提示。全部动效在 1.2 秒内完成，只用 transform / opacity。',
	),
	dict(
		key='hotel-booking',
		name='Hotel Booking',
		scene='Landing Page', style='Glass', best_for='预订页,订单流程',
		description='预订流程页：玻璃卡承载表单，弹窗形变给出确认，通知弹入回报结果，点击有水波。',
		steps=[
			('glass-card-hover', '交互', '房型卡用玻璃悬停，反馈轻且不打断选择'),
			('modal-morph', '内容', '确认弹窗由被点击的卡片形变展开'),
			('ripple-click-btn', '交互', '提交按钮点击扩散水波，明确「已提交」'),
			('notification-popup', '收尾', '预订成功右上角弹入一条通知'),
		],
		prompt='做一个预订流程页：房型卡悬停上浮并微微提亮；点击「预订」时弹窗由该卡片形变展开；提交按钮点击时扩散水波；成功后右上角弹入通知并在 4 秒后收起。表单校验错误就地显示，不要用弹窗打断。',
	),
	dict(
		key='creative-agency',
		name='Creative Agency',
		scene='Landing Page', style='Cyber', best_for='创意机构,品牌官网',
		description='创意机构首页：星系底撑场，标题擦除进入，光标跟随改成柔光，按钮磁吸收尾。',
		steps=[
			('galaxy-background', '背景', '星系渐变定调，给创意机构一点未来感'),
			('headline-clip-wipe', '内容', '主标题擦除出现，比淡入更有主张'),
			('cursor-follow', '交互', '光标跟随一团柔光，替代传统指针'),
			('magnetic-button', '收尾', '「联系我们」磁吸，把转化点收住'),
		],
		prompt='做一个创意机构首页：背景是缓慢旋转的星系渐变；主标题从左向右擦除出现；鼠标移动时有一团 240px 的柔光跟随（指针设备才启用）；联系按钮带磁吸效果。整页只有背景一处高饱和，其它保持低饱和。',
	),

	# ---------------------------------------------------------------- 既有五套（补上步骤，文案保持原样）
	dict(
		key='premium-hero',
		name='Premium Hero',
		scene='Landing Page', style='Luxury', best_for='官网首页,AI 产品页',
		description='首屏三件套：极光背景铺氛围、粒子星网给纵深、标题揭示出现，最后用磁吸按钮收住视线。',
		steps=[
			('aurora-background', '背景', '极光渐变铺氛围，先把画面质感定下来'),
			('particle-network', '背景', '粒子星网给首屏纵深，避免背景是一张平面'),
			('text-reveal', '内容', '主标题揭示出现，把视线带到内容'),
			('magnetic-button', '收尾', '主按钮磁吸跟随，收住视线并引导点击'),
		],
		prompt='做一个高级感首屏：背景是缓慢漂移的极光渐变，上面叠一层粒子星网；主标题用 clip-path 从下向上揭示，主按钮带磁吸效果。整体节奏 1.2 秒内完成入场，不要同时出现。',
	),
	dict(
		key='luxury-product-card',
		name='Luxury Product Card',
		scene='Portfolio', style='Luxury', best_for='个人作品集,官网首页',
		description='产品卡三件套：三维倾斜给触感、玻璃悬停给反馈、轻微浮动让静止状态也有呼吸。',
		steps=[
			('floating-card', '背景', '静止状态轻微浮动，让卡片一直「活着」'),
			('tilt-card-3d', '交互', '随指针做三维倾斜，给出实体触感'),
			('glass-card-hover', '交互', '玻璃悬停 + 亮边框，明确「可交互」'),
		],
		prompt='做一个产品卡片：静止时轻微上下浮动；鼠标移入时随指针做三维倾斜并点亮高光；卡片本身用玻璃拟态 + 1px 亮边框，悬停时上浮 8px。',
	),
	dict(
		key='ai-saas-landing',
		name='AI SaaS Landing',
		scene='AI SaaS', style='Cyber', best_for='AI 产品页,官网首页',
		description='AI 产品首页的完整配方：网格渐变底、浮动光球做视觉锚点、内容柔和淡入、边框流光提示可交互。',
		steps=[
			('mesh-gradient', '背景', '网格渐变打底，科技感来自结构'),
			('floating-orb', '背景', '浮动光球当视觉锚点，让视线有落点'),
			('smooth-fade', '内容', '文案柔和淡入，一次只动一个层级'),
			('glow-border', '交互', '特性卡边框流光，提示可交互'),
			('magnetic-button', '收尾', '主按钮磁吸，明确唯一的行动点'),
		],
		prompt='做 AI 产品首页：背景是网格渐变，中央一颗浮动光球作为视觉锚点；下方三个特性卡带流光描边，文案依次淡入，主按钮磁吸；整体冷静、有科技感，不要花哨。',
	),
	dict(
		key='portfolio-opening',
		name='Portfolio Opening',
		scene='Portfolio', style='Minimal', best_for='个人作品集',
		description='作品集开场：标题揭示 → 光斑跟随光标 → 作品卡缩放浮现，三段式把注意力从名字带到作品。',
		steps=[
			('text-reveal', '内容', '先揭示名字标题，交代「这是谁」'),
			('cursor-follow', '交互', '柔光跟随光标，把浏览变成一种手感'),
			('scale-reveal', '收尾', '作品卡缩放浮现，把注意力交到作品上'),
		],
		prompt='做个人作品集开场：先揭示名字标题，然后一团柔光跟随鼠标，最后作品卡从 0.92 缩放浮现；整体克制，只保留必要动效，让别人记住作品而不是特效。',
	),
	dict(
		key='dashboard-boot',
		name='Dashboard Boot',
		scene='Dashboard', style='Minimal', best_for='后台系统,数据看板',
		description='后台开屏：面板从右侧切入、卡片错落出现、关键数字滚动到目标值，最后弹一条完成通知。',
		steps=[
			('page-transition', '内容', '面板从右侧切入淡入，先建立方位感'),
			('card-stagger', '内容', '统计卡片错落出现，每张差 0.09 秒'),
			('dashboard-counter', '内容', '核心数字滚动到目标值，一眼看到变化'),
			('notification-popup', '收尾', '全部完成后弹一条通知，收束整个过程'),
		],
		prompt='做后台开屏：内容面板从右滑入淡入，统计卡片错落出现（每张差 0.09 秒），核心数字从 0 滚动到目标值，全部完成后右上角弹一条通知。整体不超过 2 秒。',
	),
]
