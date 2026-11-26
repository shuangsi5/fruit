<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <span class="brand-mark">果</span>
        <h1>水果交易智能管理系统</h1>
        <p>让新鲜水果从果园直达餐桌</p>
      </div>

      <div class="quick-accounts">
        <div class="quick-title">演示账号（点击自动填充）</div>
        <div class="quick-list">
          <button class="quick-btn" @click="fillAccount('user01')">用户端 user01</button>
          <button class="quick-btn" @click="fillAccount('merchant01')">商家端 merchant01</button>
          <button class="quick-btn" @click="fillAccount('admin')">平台端 admin</button>
        </div>
        <div class="quick-hint">密码统一为 123456（以实际种子数据为准）</div>
      </div>

      <form @submit.prevent="onLogin">
        <div class="form-group">
          <label class="form-label">用户名 / 手机号</label>
          <input v-model="form.username" class="form-control" placeholder="请输入用户名或手机号" autocomplete="username" />
        </div>
        <div class="form-group">
          <label class="form-label">密码</label>
          <input v-model="form.password" type="password" class="form-control" placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <div class="form-error" v-if="errorMsg">{{ errorMsg }}</div>
        <button type="submit" class="btn btn-primary btn-login btn-lg" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="auth-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
    <div class="auth-aside">
      <div class="aside-content">
        <h2>新鲜 · 智能 · 高效</h2>
        <ul>
          <li>三端协同：用户选购、商家经营、平台监管</li>
          <li>个性化推荐：买的称心，卖得省心</li>
          <li>临期与库存预警：减少损耗，守住利润</li>
          <li>全流程跟踪：下单、支付、物流、售后一手掌握</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, getMe } from '../../api/auth'
import { useUserStore } from '../../store'
import { notifySuccess, notifyError } from '../../utils/request'

const router = useRouter()
const route = useRoute()
const user = useUserStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const errorMsg = ref('')

function fillAccount(username) {
  form.username = username
  form.password = '123456'
  errorMsg.value = ''
}

async function onLogin() {
  errorMsg.value = ''
  if (!form.username || !form.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  loading.value = true
  try {
    const data = await login({ username: form.username, password: form.password })
    // 登录返回 token/userId/role/merchantId，补充 username
    user.setLogin({
      token: data.token,
      userId: data.userId,
      role: data.role,
      merchantId: data.merchantId,
      username: form.username
    })
    // 尝试拉取用户信息补全显示名
    try {
      const me = await getMe()
      if (me && (me.realName || me.username)) {
        user.userInfo = me
        user.realName = me.realName || ''
      }
    } catch (e) {
      // getMe 失败不影响登录主流程
    }
    notifySuccess('登录成功')
    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
    } else if (user.role === 'MERCHANT') {
      router.push('/merchant/dashboard')
    } else if (user.role === 'ADMIN') {
      router.push('/admin/dashboard')
    } else {
      router.push('/home')
    }
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 64px);
  display: flex;
}
.auth-card {
  width: 480px;
  max-width: 100%;
  padding: 48px 56px;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.auth-brand {
  text-align: center;
  margin-bottom: 28px;
}
.auth-brand .brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--primary);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
}
.auth-brand h1 {
  font-size: 22px;
  margin-top: 14px;
}
.auth-brand p {
  color: var(--text-light);
  font-size: 13px;
  margin-top: 4px;
}
.quick-accounts {
  background: var(--primary-light);
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 22px;
}
.quick-title {
  font-size: 12px;
  color: var(--primary-dark);
  font-weight: 600;
  margin-bottom: 10px;
}
.quick-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.quick-btn {
  border: 1px solid var(--primary);
  background: #fff;
  color: var(--primary);
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 12px;
}
.quick-btn:hover {
  background: var(--primary);
  color: #fff;
}
.quick-hint {
  font-size: 11px;
  color: var(--text-light);
  margin-top: 8px;
}
.btn-login {
  width: 100%;
  margin-top: 10px;
}
.auth-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}
.auth-footer a {
  color: var(--primary);
  font-weight: 600;
}
.auth-aside {
  flex: 1;
  background: linear-gradient(135deg, #2e9e5b, #1f7a42);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.aside-content {
  max-width: 420px;
  padding: 40px;
}
.aside-content h2 {
  font-size: 32px;
  margin-bottom: 28px;
}
.aside-content ul {
  list-style: none;
  line-height: 2.4;
  font-size: 15px;
}
.aside-content li::before {
  content: '✓ ';
  font-weight: 700;
  margin-right: 8px;
}
@media (max-width: 900px) {
  .auth-aside {
    display: none;
  }
  .auth-card {
    width: 100%;
    padding: 40px 24px;
  }
}
</style>
