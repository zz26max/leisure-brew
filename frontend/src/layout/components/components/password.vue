<template>
  <el-dialog
    title="修改密码"
    :visible="dialogFormVisible"
    width="480px"
    custom-class="password-dialog"
    @close="close"
  >
    <el-form ref="form" :model="form" :rules="rules" label-position="top">
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password autocomplete="new-password" />
        <p class="password-hint">
          建议使用 8 位以上的字母、数字和符号组合。
        </p>
      </el-form-item>
      <el-form-item label="再输入一次" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
    </el-form>
    <span slot="footer">
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存新密码</el-button>
    </span>
  </el-dialog>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { Form as ElForm } from 'element-ui'
import { editPassword } from '@/api/users'

@Component({ name: 'PasswordDialog' })
export default class extends Vue {
  @Prop({ default: false }) private dialogFormVisible!: boolean

  private saving = false
  private form = { oldPassword: '', newPassword: '', confirmPassword: '' }

  private validatePassword = (_: any, value: string, callback: Function) => {
    if (!value) callback(new Error('请填写密码'))
    else if (value.length < 8 || value.length > 64) callback(new Error('密码需为 8 到 64 位'))
    else callback()
  }

  private validateConfirmation = (_: any, value: string, callback: Function) => {
    if (value !== this.form.newPassword) callback(new Error('两次输入的密码不一致'))
    else callback()
  }

  private rules = {
    oldPassword: [{ required: true, message: '请填写当前密码', trigger: 'blur' }],
    newPassword: [{ validator: this.validatePassword, trigger: 'blur' }],
    confirmPassword: [{ validator: this.validateConfirmation, trigger: 'blur' }],
  }

  private save() {
    (this.$refs.form as ElForm).validate(async (valid: boolean) => {
      if (!valid) return
      this.saving = true
      try {
        const response = await editPassword({
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword,
        })
        if (Number(response.data.code) === 1) {
          this.$message.success('密码已更新，下次登录请使用新密码')
          this.close()
        }
      } finally {
        this.saving = false
      }
    })
  }

  private close() {
    const form = this.$refs.form as ElForm
    if (form) form.resetFields()
    this.$emit('handleclose')
  }
}
</script>

<style lang="scss">
.password-dialog {
  .el-dialog__body { padding: 18px 32px 8px; }
  .password-hint { margin: 7px 0 0; color: $color-text-muted; font-size: 12px; line-height: 1.5; }
}
</style>
