<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { adminApi } from '@/services/admin-api'

const formRef = ref<FormInstance>()
const saving = ref(false)
const password = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules: FormRules = {
  oldPassword: [{ required: true, message: '请填写当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 8, max: 64, message: '新密码需为 8–64 位', trigger: 'blur' }],
  confirmPassword: [
    {
      validator: (_rule, value, callback) =>
        value === password.newPassword ? callback() : callback(new Error('两次输入的密码不一致')),
      trigger: 'blur',
    },
  ],
}
async function savePassword() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  saving.value = true
  try {
    await adminApi.changePassword(password)
    ElMessage.success('密码已更新')
    formRef.value?.resetFields()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader
      eyebrow="STORE SETTINGS"
      title="门店设置"
      description="把高频操作留在前台，把敏感设置收在这里。"
    />
    <section class="settings-grid">
      <article class="paper-card setting-card">
        <p class="eyebrow">ACCOUNT</p>
        <h3>修改登录密码</h3>
        <p class="muted">建议使用字母、数字与符号组合，不要与其他网站共用。</p>
        <el-form ref="formRef" :model="password" :rules="rules" label-position="top"
          ><el-form-item label="当前密码" prop="oldPassword"
            ><el-input v-model="password.oldPassword" type="password" show-password /></el-form-item
          ><el-form-item label="新密码" prop="newPassword"
            ><el-input v-model="password.newPassword" type="password" show-password /></el-form-item
          ><el-form-item label="再次输入" prop="confirmPassword"
            ><el-input v-model="password.confirmPassword" type="password" show-password /></el-form-item
          ><el-button type="primary" :loading="saving" @click="savePassword">保存新密码</el-button></el-form
        >
      </article>
      <article class="paper-card setting-card store-profile">
        <p class="eyebrow">BRAND</p>
        <h3>闲里茶咖</h3>
        <div class="store-profile__seal">闲</div>
        <dl>
          <div>
            <dt>品牌气质</dt>
            <dd>东方鲜活 · 日常茶感</dd>
          </div>
          <div>
            <dt>主色</dt>
            <dd><i class="color-dot" />茶叶绿 #286454</dd>
          </div>
          <div>
            <dt>服务理念</dt>
            <dd>每一步都让人安心</dd>
          </div>
        </dl>
        <p class="muted">门店名称、电话与配送范围将在新版后端设置接口完成后开放编辑。</p>
      </article>
    </section>
  </div>
</template>
