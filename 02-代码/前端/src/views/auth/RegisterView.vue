<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <span class="brand-mark">果</span>
        <h1>注册账号</h1>
        <p>选择您的角色，开启水果交易之旅</p>
      </div>

      <form @submit.prevent="onRegister">
        <div class="form-group">
          <label class="form-label">注册角色</label>
          <div class="role-select">
            <button
              type="button"
              v-for="r in roles"
              :key="r.value"
              class="role-option"
              :class="{ active: form.role === r.value }"
              @click="form.role = r.value"
            >
              {{ r.label }}
            </button>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input v-model="form.username" class="form-control" placeholder="请输入用户名" />
          </div>
          <div class="form-group">
            <label class="form-label">真实姓名</label>
            <input v-model="form.realName" class="form-control" placeholder="请输入真实姓名" />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">手机号</label>
          <input v-model="form.phone" class="form-control" placeholder="请输入 11 位手机号" maxlength="11" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">密码</label>
            <input v-model="form.password" type="password" class="form-control" placeholder="请输入密码" />
          </div>
          <div class="form-group">
            <label class="form-label">确认密码</label>
            <input v-model="form.confirmPassword" type="password" class="form-control" placeholder="请再次输入密码" />
          </div>
        </div>

        <div v-if="form.role === 'MERCHANT'" class="merchant-extra">
          <div class="form-group">
            <label class="form-label">店铺名称</label>
            <input v-model="form.merchantName" class="form-control" placeholder="请输入店铺/商家名称" />
          </div>
          <div class="form-group">
            <label class="form-label">营业执照号（选填）</label>
            <input v-model="form.licenseNo" class="form-control" placeholder="入驻后可在商家中心补充" />
          </div>
        </div>

        <div class="form-error" v-if="errorMsg">{{ errorMsg }}</div>
        <button type="submit" class="btn btn-primary btn-login btn-lg" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>

      <div class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
    <div class="auth-aside">
      <div class="aside-content">
        <h2>加入我们</h2>
        <ul>
          <li>普通用户：浏览水果、下单支付、跟踪订单、申请售后</li>
          <li>入驻商家：上架货源、管理库存、处理订单、查看报表</li>
          <li>平台管理员：审核商家、管理用户、处理投诉、查看总览</li>
        </ul>
        <p class="aside-note">商家账号需通过平台资质审核后方可经营。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../../api/auth'
import { notifySuccess } from '../../utils/request'

const router = useRouter()
const roles = [
  { value: 'USER', label: '普通用户' },
  { value: 'MERCHANT', label: '入驻商家' },
  { value: 'ADMIN', label: '平台管理员' }
]

const form = reactive({
  role: 'USER',
  username: '',
  realName: '',
  phone: '',
  password: '',
  confirmPassword: '',
  merchantName: '',
  licenseNo: ''
})
const loading = ref(false)
const errorMsg = ref('')

async function onRegister() {
  errorMsg.value = ''
  if (!form.username || !form.phone || !form.password) {
    errorMsg.value = '请填写用户名、手机号和密码'
    return
  }
  if (!/^1\d{10}$/.test(form.phone)) {
    errorMsg.value = '请输入正确的 11 位手机号'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  if (form.role === 'MERCHANT' && !form.merchantName) {
    errorMsg.value = '请填写店铺名称'
    return
  }
  loading.value = true
  try {
    const payload = {
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      password: form.password,
      role: form.role
    }
    if (form.role === 'MERCHANT') {
      payload.merchantName = form.merchantName
      if (form.licenseNo) payload.licenseNo = form.licenseNo
    }
    await register(payload)
    notifySuccess('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    errorMsg.value = e.message || '注册失败，请稍后重试'
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
  width: 520px;
  max-width: 100%;
  padding: 36px 48px;
  background: #fff;
}
.auth-brand {
  text-align: center;
  margin-bottom: 24px;
}
.auth-brand .brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  font-weight: 700;
}
.auth-brand h1 {
  font-size: 22px;
  margin-top: 12px;
}
.auth-brand p {
  color: var(--text-light);
  font-size: 13px;
  margin-top: 4px;
}
.role-select {
  display: flex;
  gap: 8px;
}
.role-option {
  flex: 1;
  padding: 10px;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: 8px;
  color: var(--text-secondary);
  font-weight: 500;
}
.role-option.active {
  border-color: var(--primary);
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 600;
}
.merchant-extra {
  background: #f9fafb;
  border-radius: 8px;
  padding: 14px 14px 0;
  margin-bottom: 16px;
}
.btn-login {
  width: 100%;
  margin-top: 8px;
}
.auth-footer {
  text-align: center;
  margin-top: 18px;
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
.aside-note {
  margin-top: 20px;
  font-size: 13px;
  opacity: 0.85;
}
@media (max-width: 900px) {
  .auth-aside {
    display: none;
  }
  .auth-card {
    width: 100%;
    padding: 32px 20px;
  }
}
</style>
