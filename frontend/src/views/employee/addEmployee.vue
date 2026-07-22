<template>
  <div class="page-shell">
    <section class="form-card">
      <div class="form-card__heading">
        <p>TEAM MEMBER</p>
        <h2>{{ isEditing ? '编辑员工资料' : '邀请一位新伙伴' }}</h2>
        <span>{{ isEditing ? '登录账号不可修改，其余资料可随时更新。' : '保存后会生成一次性初始口令，请当面交给员工。' }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-position="top">
        <div class="form-grid">
          <el-form-item label="姓名" prop="name">
            <el-input v-model.trim="form.name" />
          </el-form-item>
          <el-form-item label="登录账号" prop="username">
            <el-input v-model.trim="form.username" :disabled="isEditing" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model.trim="form.phone" maxlength="11" />
          </el-form-item>
          <el-form-item label="身份证号" prop="idNumber">
            <el-input v-model.trim="form.idNumber" maxlength="18" />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="form.sex">
              <el-radio label="1">
                男
              </el-radio><el-radio label="0">
                女
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div class="form-actions">
          <el-button @click="$router.back()">
            返回
          </el-button>
          <el-button type="primary" :loading="saving" @click="save">
            {{ isEditing ? '保存修改' : '添加员工' }}
          </el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { Form as ElForm } from 'element-ui'
import { addEmployee, editEmployee, queryEmployeeById } from '@/api/employee'

@Component({ name: 'EmployeeEditor' })
export default class extends Vue {
  private saving = false
  private form = { id: undefined as number | undefined, name: '', username: '', phone: '', idNumber: '', sex: '1' }
  private rules = {
    name: [{ required: true, message: '请填写员工姓名', trigger: 'blur' }],
    username: [
      { required: true, message: '请填写登录账号', trigger: 'blur' },
      { pattern: /^[A-Za-z0-9_-]{3,20}$/, message: '使用 3 到 20 位字母、数字、下划线或短横线', trigger: 'blur' },
    ],
    phone: [{ pattern: /^1\d{10}$/, message: '请填写正确的手机号', trigger: 'blur' }],
    idNumber: [{ pattern: /(^\d{15}$)|(^\d{17}[0-9Xx]$)/, message: '请填写正确的身份证号', trigger: 'blur' }],
  }

  get isEditing() { return Boolean(this.$route.query.id) }
  created() { if (this.isEditing) this.loadEmployee() }

  private async loadEmployee() {
    const response = await queryEmployeeById(this.$route.query.id as string)
    if (Number(response.data.code) === 1) this.form = response.data.data
  }

  private save() {
    (this.$refs.form as ElForm).validate(async (valid: boolean) => {
      if (!valid) return
      this.saving = true
      try {
        const response = this.isEditing ? await editEmployee(this.form) : await addEmployee(this.form)
        if (Number(response.data.code) !== 1) return
        if (this.isEditing) {
          this.$message.success('员工资料已更新')
        } else {
          await this.$alert(
            `<p style="margin:0 0 10px;color:#6e746d">这是仅展示一次的初始口令，请妥善交给员工：</p><strong style="font:700 20px monospace;letter-spacing:.08em;color:#23483f">${response.data.data}</strong>`,
            '新伙伴已加入',
            { dangerouslyUseHTMLString: true, confirmButtonText: '我已记下' }
          )
        }
        await this.$router.push('/employee')
      } finally {
        this.saving = false
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.page-shell { padding: 24px; }
.form-card { max-width: 860px; padding: 34px 40px; background: $color-card-bg; border: 1px solid $color-border-light; border-radius: $radius-lg; box-shadow: $shadow-sm; }
.form-card__heading { margin-bottom: 28px; p { margin: 0; color: $color-accent; font-size: 10px; font-weight: 700; letter-spacing: .2em; } h2 { margin: 7px 0; font-family: 'Songti SC', STSong, SimSun, serif; font-size: 25px; } span { color: $color-text-secondary; font-size: 13px; } }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 4px 28px; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; padding-top: 22px; border-top: 1px solid $color-border-light; }
</style>
