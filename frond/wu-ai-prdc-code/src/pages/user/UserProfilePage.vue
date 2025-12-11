<template>
  <div class="user-profile-page">
    <div class="profile-container">
      <!-- 用户基本信息卡片 -->
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-section">
            <a-avatar
              :size="120"
              :src="userInfo.userAvatar || defaultAvatar"
              class="user-avatar"
            >
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <a-button
              type="primary"
              ghost
              class="edit-avatar-btn"
              @click="showAvatarModal"
            >
              <UploadOutlined /> 更换头像
            </a-button>
          </div>

          <div class="user-info">
            <h1 class="user-name">{{ userInfo.userName || '未设置昵称' }}</h1>
            <div class="user-account">
              <span class="label">账号：</span>
              <span class="value">{{ userInfo.userAccount }}</span>
            </div>
            <div class="user-role">
              <span class="label">角色：</span>
              <a-tag :color="userInfo.userRole === 'admin' ? 'red' : 'blue'">
                {{ userInfo.userRole === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </div>
            <div class="join-time">
              <span class="label">注册时间：</span>
              <span class="value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
            <div class="last-update">
              <span class="label">最后编辑：</span>
              <span class="value">{{ formatDate(userInfo.updateTime) }}</span>
            </div>
          </div>
        </div>

        <div class="profile-actions">
          <a-button type="primary" size="large" @click="showEditModal">
            <EditOutlined /> 编辑资料
          </a-button>
        </div>
      </div>

      <!-- 数据统计卡片 -->
      <div class="stats-cards">
        <a-row :gutter="[24, 24]">
          <a-col :xs="24" :sm="12" :md="8">
            <div class="stat-card">
              <div class="stat-icon apps-icon">
                <AppstoreOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.appNumber || 0 }}</div>
                <div class="stat-label">创建应用</div>
              </div>
            </div>
          </a-col>

          <a-col :xs="24" :sm="12" :md="8">
            <div class="stat-card">
              <div class="stat-icon featured-icon">
                <StarOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.selectedAppNumber || 0 }}</div>
                <div class="stat-label">应用被精选</div>
              </div>
            </div>
          </a-col>

          <a-col :xs="24" :sm="12" :md="8">
            <div class="stat-card">
              <div class="stat-icon chat-icon">
                <CommentOutlined />
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ userStats.chatNumber || 0 }}</div>
                <div class="stat-label">对话次数</div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 用户应用列表 -->
      <div class="apps-section">
        <div class="section-header">
          <h2>我的应用</h2>
          <a-button type="primary" @click="goToCreateApp">
            <PlusOutlined /> 创建新应用
          </a-button>
        </div>

        <div v-if="userApps.length > 0" class="apps-grid">
          <AppCard
            v-for="app in userApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>

        <a-empty v-else description="暂无应用，快来创建第一个吧！" class="empty-state">
          <a-button type="primary" @click="goToCreateApp">创建应用</a-button>
        </a-empty>
      </div>
    </div>

    <!-- 编辑资料模态框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑个人资料"
      @ok="handleEditOk"
      @cancel="handleEditCancel"
      :confirm-loading="editLoading"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="用户昵称">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户昵称" />
        </a-form-item>

        <a-form-item label="用户简介">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="请输入用户简介"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 更换头像模态框 -->
    <a-modal
      v-model:open="avatarModalVisible"
      title="更换头像"
      @ok="handleAvatarOk"
      @cancel="handleAvatarCancel"
      :confirm-loading="avatarLoading"
    >
      <div class="avatar-upload">
        <a-upload
          :before-upload="beforeUpload"
          :show-upload-list="false"
          :custom-request="handleUpload"
        >
          <div class="upload-area">
            <a-avatar :size="120" :src="tempAvatar || userInfo.userAvatar || defaultAvatar" />
            <div class="upload-text">
              <UploadOutlined />
              <div>点击上传头像</div>
            </div>
          </div>
        </a-upload>
      </div>
    </a-modal>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  EditOutlined,
  UploadOutlined,
  AppstoreOutlined,
  StarOutlined,
  CommentOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { getAppChatNumber, updateUser } from '@/api/userController'
import { listMyAppVoByPage } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'
import defaultAvatar from '@/assets/logo.png'
import { formatDate } from '@/utils/time'

// 路由和状态管理
const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户信息
const userInfo = ref<API.User>({ ...loginUserStore.loginUser })

// 用户统计数据
const userStats = ref({
  appNumber: 0,
  selectedAppNumber: 0,
  chatNumber: 0
})

// 用户应用列表
const userApps = ref<API.AppVO[]>([])

// 编辑模态框状态
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  userName: '',
  userProfile: ''
})

// 头像模态框状态
const avatarModalVisible = ref(false)
const avatarLoading = ref(false)
const tempAvatar = ref('')

// 显示编辑模态框
const showEditModal = () => {
  editForm.userName = userInfo.value.userName || ''
  editForm.userProfile = userInfo.value.userProfile || ''
  editModalVisible.value = true
}

// 处理编辑确认
const handleEditOk = async () => {
  editLoading.value = true
  try {
    const res = await updateUser({
      id: userInfo.value.id,
      userName: editForm.userName,
      userProfile: editForm.userProfile
    })

    if (res.data.code === 0) {
      message.success('资料更新成功')
      // 更新本地状态
      userInfo.value.userName = editForm.userName
      userInfo.value.userProfile = editForm.userProfile
      loginUserStore.setLoginUser({ ...userInfo.value })
      handleEditCancel()
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    message.error('更新失败，请重试'+error)
  } finally {
    editLoading.value = false
  }
}

// 处理编辑取消
const handleEditCancel = () => {
  editModalVisible.value = false
}

// 显示头像模态框
const showAvatarModal = () => {
  tempAvatar.value = ''
  avatarModalVisible.value = true
}

// 处理头像确认
const handleAvatarOk = async () => {
  if (!tempAvatar.value) {
    message.warning('请先上传头像')
    return
  }

  avatarLoading.value = true
  try {
    // 实际项目中这里应该调用更新头像的API
    message.success('头像更新成功')
    userInfo.value.userAvatar = tempAvatar.value
    loginUserStore.setLoginUser({ ...userInfo.value })
    handleAvatarCancel()
  } catch (error) {
    message.error('头像更新失败，请重试'+error)
  } finally {
    avatarLoading.value = false
  }
}

// 处理头像取消
const handleAvatarCancel = () => {
  avatarModalVisible.value = false
}

// 上传前检查
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }

  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }

  return true
}

// 处理上传
const handleUpload = async (options: any) => {
  const { file, onSuccess, onError } = options

  try {
    // 这里应该是实际的上传API调用
    // 模拟上传过程
    setTimeout(() => {
      // 模拟上传成功，设置临时头像预览
      tempAvatar.value = URL.createObjectURL(file)
      onSuccess()
    }, 1000)
  } catch (error) {
    console.error('上传出错:', error);
    onError()
  }
}

// 加载用户应用
const loadUserApps = async () => {
  try {
    const res = await listMyAppVoByPage({
      pageNum: 1,
      pageSize: 6,
      sortField: 'createTime',
      sortOrder: 'desc'
    })

    if (res.data.code === 0 && res.data.data) {
      userApps.value = res.data.data.records || []
    }
  } catch (error) {
    console.error('加载用户应用失败：', error)
  }
}

// 跳转到创建应用页面
const goToCreateApp = () => {
  router.push('/')
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
    // 这里应该使用实际的部署URL生成方法
    const url = `/deploy/${app.deployKey}`
    window.open(url, '_blank')
  }
}

// 初始化数据
onMounted(async () => {
  // 加载统计数据
  try {
    const res = await getAppChatNumber()
    if (res.data.code === 0 && res.data.data) {
      userStats.value = {
        appNumber: res.data.data.appNumber || 0,
        selectedAppNumber: res.data.data.selectedAppNumber || 0,
        chatNumber: res.data.data.chatNumber || 0
      }
    }
  } catch (error) {
    console.error('加载用户应用失败：', error)
  }
  // 加载用户应用
  await loadUserApps()
})
</script>

<style scoped>
.user-profile-page {
  min-height: calc(100vh - 120px);
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  align-items: center;
}

.avatar-section {
  position: relative;
  text-align: center;
}

.user-avatar {
  border: 4px solid #f0f2f5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.edit-avatar-btn {
  margin-top: 12px;
}

.user-info {
  flex: 1;
  min-width: 300px;
}

.user-name {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #1d2129;
}

.user-account,
.user-role,
.join-time,
.last-update {
  display: flex;
  margin-bottom: 12px;
  font-size: 16px;
}

.label {
  font-weight: 500;
  color: #646a73;
  width: 100px;
}

.value {
  color: #1d2129;
}

.profile-actions {
  margin-top: 24px;
  text-align: center;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.apps-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.featured-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.chat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #646a73;
}

.apps-section {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1d2129;
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.empty-state {
  padding: 48px 0;
}

.upload-area {
  text-align: center;
  cursor: pointer;
}

.upload-text {
  margin-top: 16px;
  color: #646a73;
}

.upload-text div {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .user-info {
    min-width: unset;
  }

  .user-account,
  .user-role,
  .join-time,
  .last-update {
    justify-content: center;
  }

  .section-header {
    flex-direction: column;
    gap: 16px;
  }

  .apps-section {
    padding: 24px 16px;
  }
}
</style>
