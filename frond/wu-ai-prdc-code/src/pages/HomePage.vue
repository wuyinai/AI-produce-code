<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-icon">✨</span>
            <span class="badge-text">AI驱动的零代码平台</span>
          </div>
          <h1 class="hero-title">AI代码生成平台</h1>
          <p class="hero-description">一句话轻松创建完整网站应用，让创意瞬间变为现实</p>
          <div class="hero-stats">
            <div class="stat-item">
              <div class="stat-number">{{ myAppsPage.total || 0 }}</div>
              <div class="stat-label">我的应用</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">{{ featuredAppsPage.total || 0 }}</div>
              <div class="stat-label">精选案例</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">100%</div>
              <div class="stat-label">零代码</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <div class="input-container">
          <div class="input-header">
            <h2 class="input-title">描述您的想法</h2>
            <p class="input-subtitle">详细描述您想要的应用，AI将为您生成完整的网站</p>
          </div>
          
          <div class="input-wrapper">
            <a-textarea
              v-model:value="userPrompt"
              placeholder="例如：创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签和搜索功能..."
              :rows="4"
              :maxlength="1000"
              class="prompt-textarea"
              :show-count="true"
            />
            
            <div class="input-actions">
              <div class="input-meta">
                <span class="char-count">{{ userPrompt.length }}/1000</span>
                <div class="quick-tips">
                  <span class="tips-icon">💡</span>
                  <span class="tips-text">详细描述可获得更好的生成效果</span>
                </div>
              </div>
              
              <a-button 
                type="primary" 
                size="large" 
                @click="createApp" 
                :loading="creating"
                class="create-btn"
                :disabled="!userPrompt.trim()"
              >
                <template #icon>
                  <span class="btn-icon">⚡</span>
                </template>
                {{ creating ? '正在生成...' : '开始创建' }}
              </a-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <div class="quick-actions-header">
          <h3 class="quick-title">快速开始</h3>
          <p class="quick-subtitle">选择一个模板快速创建您的应用</p>
        </div>
        
        <div class="template-grid">
          <div 
            class="template-card"
            @click="setPrompt('创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。')"
          >
            <div class="template-icon blog-icon">📝</div>
            <h4 class="template-title">个人博客</h4>
            <p class="template-desc">现代化博客系统，支持Markdown</p>
            <div class="template-tags">
              <span class="tag">响应式</span>
              <span class="tag">评论系统</span>
            </div>
          </div>
          
          <div 
            class="template-card"
            @click="setPrompt('设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。')"
          >
            <div class="template-icon corporate-icon">🏢</div>
            <h4 class="template-title">企业官网</h4>
            <p class="template-desc">专业商务风格，多语言支持</p>
            <div class="template-tags">
              <span class="tag">商务风格</span>
              <span class="tag">在线客服</span>
            </div>
          </div>
          
          <div 
            class="template-card"
            @click="setPrompt('构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。')"
          >
            <div class="template-icon ecommerce-icon">🛍️</div>
            <h4 class="template-title">在线商城</h4>
            <p class="template-desc">完整电商功能，支付系统</p>
            <div class="template-tags">
              <span class="tag">电商功能</span>
              <span class="tag">会员系统</span>
            </div>
          </div>
          
          <div 
            class="template-card"
            @click="setPrompt('制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。')"
          >
            <div class="template-icon portfolio-icon">🎨</div>
            <h4 class="template-title">作品展示</h4>
            <p class="template-desc">精美瀑布流布局，适合创作者</p>
            <div class="template-tags">
              <span class="tag">瀑布流</span>
              <span class="tag">图片预览</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>
<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background:
    linear-gradient(180deg, #f8fafc 0%, #f1f5f9 8%, #e2e8f0 20%, #cbd5e1 100%),
    radial-gradient(circle at 20% 80%, rgba(59, 130, 246, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(139, 92, 246, 0.12) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(16, 185, 129, 0.08) 0%, transparent 50%);
  position: relative;
  overflow: hidden;
}

/* 科技感网格背景 */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(139, 92, 246, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(139, 92, 246, 0.04) 1px, transparent 1px);
  background-size:
    100px 100px,
    100px 100px,
    20px 20px,
    20px 20px;
  pointer-events: none;
  animation: gridFloat 20s ease-in-out infinite;
}

/* 动态光效 */
#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(
      600px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
      rgba(59, 130, 246, 0.08) 0%,
      rgba(139, 92, 246, 0.06) 40%,
      transparent 80%
    ),
    linear-gradient(45deg, transparent 30%, rgba(59, 130, 246, 0.04) 50%, transparent 70%),
    linear-gradient(-45deg, transparent 30%, rgba(139, 92, 246, 0.04) 50%, transparent 70%);
  pointer-events: none;
  animation: lightPulse 8s ease-in-out infinite alternate;
}

@keyframes gridFloat {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(5px, 5px);
  }
}

@keyframes lightPulse {
  0% {
    opacity: 0.3;
  }
  100% {
    opacity: 0.7;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* 移除居中光束效果 */

/* 英雄区域 */
.hero-section {
  text-align: center;
  padding: 100px 0 80px;
  margin-bottom: 40px;
  color: #1e293b;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse 1000px 500px at center, rgba(59, 130, 246, 0.12) 0%, transparent 70%),
    linear-gradient(45deg, transparent 30%, rgba(139, 92, 246, 0.08) 50%, transparent 70%),
    linear-gradient(-45deg, transparent 30%, rgba(16, 185, 129, 0.06) 50%, transparent 70%);
  animation: heroGlow 12s ease-in-out infinite alternate;
}

@keyframes heroGlow {
  0% {
    opacity: 0.7;
    transform: scale(1);
  }
  100% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 50px;
  padding: 8px 20px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  animation: badgeFloat 3s ease-in-out infinite;
}

@keyframes badgeFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-5px);
  }
}

.badge-icon {
  font-size: 16px;
  color: #667eea;
}

.badge-text {
  font-size: 14px;
  font-weight: 500;
  color: #667eea;
}

.hero-title {
  font-size: 64px;
  font-weight: 800;
  margin: 0 0 24px;
  line-height: 1.1;
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #10b981 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -2px;
  position: relative;
  animation: titleShimmer 4s ease-in-out infinite;
}

@keyframes titleShimmer {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.hero-description {
  font-size: 22px;
  margin: 0 0 40px;
  opacity: 0.85;
  color: #64748b;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  margin-top: 32px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  max-width: 480px;
  margin-left: auto;
  margin-right: auto;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(to bottom, transparent, rgba(102, 126, 234, 0.3), transparent);
}

/* 输入区域 */
.input-section {
  position: relative;
  margin: 0 auto 60px;
  max-width: 900px;
}

.input-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(102, 126, 234, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.input-header {
  text-align: center;
  margin-bottom: 32px;
}

.input-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #1e293b;
}

.input-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  line-height: 1.6;
}

.input-wrapper {
  position: relative;
}

.prompt-textarea {
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  font-size: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  resize: none;
  line-height: 1.6;
}

.prompt-textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  background: rgba(255, 255, 255, 1);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 20px;
  gap: 20px;
}

.input-meta {
  flex: 1;
}

.char-count {
  font-size: 14px;
  color: #9ca3af;
  margin-bottom: 8px;
  display: block;
}

.quick-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.tips-icon {
  font-size: 14px;
}

.tips-text {
  font-weight: 500;
}

.create-btn {
  height: 56px;
  padding: 0 32px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-width: 160px;
}

.create-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.create-btn:hover::before {
  left: 100%;
}

.create-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.4);
}

.create-btn:disabled {
  background: #e2e8f0;
  color: #9ca3af;
  transform: none;
  box-shadow: none;
}

.btn-icon {
  margin-right: 8px;
  font-size: 18px;
}

/* 快捷按钮 */
.quick-actions {
  margin-bottom: 80px;
}

.quick-actions-header {
  text-align: center;
  margin-bottom: 40px;
}

.quick-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #1e293b;
}

.quick-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.template-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  padding: 32px 24px;
  border: 2px solid transparent;
  background-clip: padding-box;
  position: relative;
  cursor: pointer;
  transition: all 0.4s ease;
  text-align: center;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.06);
}

.template-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 20px;
  padding: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.template-card:hover::before {
  opacity: 1;
}

.template-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
}

.template-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 0 auto 20px;
  position: relative;
  overflow: hidden;
}

.blog-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.corporate-icon {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: white;
}

.ecommerce-icon {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
}

.portfolio-icon {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
  color: #667eea;
}

.template-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 12px;
  color: #1e293b;
}

.template-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 20px;
  line-height: 1.5;
}

.template-tags {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.tag {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

/* 区域标题 */
.section {
  margin-bottom: 60px;
}

.section-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 32px;
  color: #1e293b;
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-description {
    font-size: 16px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    justify-content: center;
  }
}
</style>
