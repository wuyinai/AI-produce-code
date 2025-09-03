<template>
  <div id="userRegisterPage">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shapes">
        <div class="shape shape1"></div>
        <div class="shape shape2"></div>
        <div class="shape shape3"></div>
        <div class="shape shape4"></div>
      </div>
    </div>
    
    <!-- 注册表单容器 -->
    <div class="register-container">
      <div class="register-card">
        <!-- 头部区域 -->
        <div class="register-header">
          <div class="logo-section">
            <div class="logo-icon">🤖</div>
            <h1 class="title">AI 应用生成</h1>
          </div>
          <p class="subtitle">创建您的账号</p>
          <p class="desc">加入我们，开始您的AI之旅</p>
        </div>
        
        <!-- 表单区域 -->
        <div class="register-form">
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
            <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
              <div class="input-wrapper">
                <div class="input-icon">👤</div>
                <a-input 
                  v-model:value="formState.userAccount" 
                  placeholder="请输入账号" 
                  size="large"
                  class="custom-input"
                />
              </div>
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[
                { required: true, message: '请输入密码' },
                { min: 7, message: '密码不能小于 7 位' },
              ]"
            >
              <div class="input-wrapper">
                <div class="input-icon">🔒</div>
                <a-input-password 
                  v-model:value="formState.userPassword" 
                  placeholder="请输入密码" 
                  size="large"
                  class="custom-input"
                />
              </div>
            </a-form-item>
            <a-form-item
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { min: 7, message: '密码不能小于 7 位' },
                { validator: validateCheckPassword },
              ]"
            >
              <div class="input-wrapper">
                <div class="input-icon">✓</div>
                <a-input-password 
                  v-model:value="formState.checkPassword" 
                  placeholder="请确认密码" 
                  size="large"
                  class="custom-input"
                />
              </div>
            </a-form-item>
            
            <div class="form-footer">
              <div class="tips">
                已有账号？
                <RouterLink to="/user/login" class="link">立即登录</RouterLink>
              </div>
              
              <a-form-item class="submit-item">
                <a-button 
                  type="primary" 
                  html-type="submit" 
                  size="large"
                  class="register-btn"
                >
                  注册
                </a-button>
              </a-form-item>
            </div>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  min-height: 100vh;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.floating-shapes {
  position: relative;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 8s ease-in-out infinite;
}

.shape1 {
  width: 100px;
  height: 100px;
  top: 15%;
  left: 15%;
  animation-delay: 0s;
}

.shape2 {
  width: 80px;
  height: 80px;
  top: 60%;
  right: 25%;
  animation-delay: 2s;
}

.shape3 {
  width: 60px;
  height: 60px;
  top: 30%;
  right: 15%;
  animation-delay: 4s;
}

.shape4 {
  width: 90px;
  height: 90px;
  bottom: 20%;
  left: 20%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 1;
  }
}

/* 注册容器 */
.register-container {
  width: 100%;
  max-width: 420px;
  z-index: 10;
  position: relative;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.register-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #764ba2, #667eea, #f093fb);
}

/* 头部区域 */
.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 32px;
  background: linear-gradient(135deg, #764ba2, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #764ba2, #667eea);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 8px 0;
}

.desc {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

/* 表单区域 */
.register-form {
  width: 100%;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 16px;
  z-index: 2;
  color: #a0aec0;
}

.custom-input {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  padding-left: 48px !important;
  height: 50px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.8);
}

.custom-input:focus,
.custom-input:hover {
  border-color: #764ba2;
  box-shadow: 0 0 0 3px rgba(118, 75, 162, 0.1);
  background: rgba(255, 255, 255, 1);
}

.form-footer {
  margin-top: 24px;
}

.tips {
  text-align: center;
  color: #718096;
  font-size: 14px;
  margin-bottom: 24px;
}

.link {
  color: #764ba2;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.link:hover {
  color: #667eea;
}

.submit-item {
  margin-bottom: 0;
}

.register-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.register-btn:hover::before {
  left: 100%;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(118, 75, 162, 0.3);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-container {
    max-width: 100%;
    padding: 0 16px;
  }
  
  .register-card {
    padding: 32px 24px;
    border-radius: 16px;
  }
  
  .title {
    font-size: 24px;
  }
  
  .subtitle {
    font-size: 18px;
  }
}
</style>
