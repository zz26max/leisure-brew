<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import BrandMark from '@/components/BrandMark.vue'
import { adminApi } from '@/services/admin-api'
import { useAuthStore } from '@/stores/auth'

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules: FormRules = {
  username: [{ required: true, message: '请填写登录账号', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '请填写至少 6 位密码', trigger: 'blur' }],
}
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

async function submit() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  loading.value = true
  try {
    const response = await adminApi.login(form)
    auth.save(response.data.data)
    ElMessage.success(`晚上好，${response.data.data.name}`)
    await router.replace(String(route.query.redirect || '/dashboard'))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-story">
      <div class="login-story__brand">
        <BrandMark />
        <div><strong>闲里茶咖</strong><span>LEISURE BREW</span></div>
      </div>
      <div class="tea-scene" aria-hidden="true">
        <i class="tea-scene__sun" /><i class="tea-scene__leaf one" /><i class="tea-scene__leaf two" /><i
          class="tea-scene__steam one"
        /><i class="tea-scene__steam two" /><i class="tea-scene__cup" /><i class="tea-scene__plate" />
      </div>
      <div class="login-story__copy">
        <span>一杯茶的从容时间</span>
        <h1>忙里偷闲，<br />杯中有序。</h1>
        <p>用清爽的节奏，打理今天的每一杯。</p>
      </div>
    </section>
    <section class="login-form-panel">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <p class="eyebrow">门店运营台</p>
        <h2>欢迎回来</h2>
        <p class="muted">查看今天的店况与待处理订单。</p>
        <el-form-item label="账号" prop="username"
          ><el-input
            v-model.trim="form.username"
            size="large"
            autocomplete="username"
            placeholder="请输入账号"
        /></el-form-item>
        <el-form-item label="密码" prop="password"
          ><el-input
            v-model="form.password"
            size="large"
            type="password"
            show-password
            autocomplete="current-password"
            placeholder="请输入密码"
        /></el-form-item>
        <el-button type="primary" size="large" :loading="loading" @click="submit">进入门店</el-button>
        <small>仅限已授权的门店伙伴使用</small>
      </el-form>
    </section>
  </main>
</template>
