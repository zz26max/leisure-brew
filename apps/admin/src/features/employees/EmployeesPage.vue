<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Employee } from '@leisure-brew/api-contract'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import { adminApi } from '@/services/admin-api'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const employees = ref<Employee[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, name: '' })
const form = reactive({
  id: undefined as number | undefined,
  username: '',
  name: '',
  phone: '',
  sex: '1' as '0' | '1',
  idNumber: '',
})
const rules: FormRules = {
  username: [
    {
      required: true,
      pattern: /^[A-Za-z0-9_-]{3,20}$/,
      message: '使用 3–20 位字母、数字、下划线或短横线',
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请填写姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请填写正确的手机号', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const response = await adminApi.employees({ ...query, name: query.name || undefined })
    employees.value = response.data.data.records
    total.value = response.data.data.total
  } finally {
    loading.value = false
  }
}
function search() {
  query.page = 1
  load()
}
function openCreate() {
  Object.assign(form, { id: undefined, username: '', name: '', phone: '', sex: '1', idNumber: '' })
  dialogVisible.value = true
}
function openEdit(employee: Employee) {
  Object.assign(form, employee)
  dialogVisible.value = true
}
async function submit() {
  if (!(await formRef.value?.validate().catch(() => false))) return
  if (form.id) {
    await adminApi.updateEmployee({ ...form, id: form.id })
    dialogVisible.value = false
    ElMessage.success('伙伴信息已更新')
    await load()
    return
  }
  const response = await adminApi.addEmployee(form)
  dialogVisible.value = false
  await ElMessageBox.alert(
    `<p class="password-note">这是仅展示一次的初始口令，请当面交给员工：</p><strong class="one-time-password">${response.data.data}</strong>`,
    '新伙伴已加入',
    { dangerouslyUseHTMLString: true, confirmButtonText: '我已记下' },
  )
  load()
}
async function toggle(employee: Employee) {
  const next = employee.status ? 0 : 1
  await adminApi.setEmployeeStatus(employee.id, next)
  ElMessage.success(next ? '账号已启用' : '账号已停用')
  load()
}
onMounted(load)
</script>

<template>
  <div>
    <PageHeader eyebrow="OUR PEOPLE" title="门店伙伴" description="清楚管理账号，也保留人与人之间的温度。"
      ><el-button type="primary" @click="openCreate">邀请新伙伴</el-button></PageHeader
    >
    <section class="paper-card table-toolbar">
      <el-input v-model.trim="query.name" clearable placeholder="按姓名查找" @keyup.enter="search" />
      <el-button @click="search">查询</el-button>
    </section>
    <section v-loading="loading" class="paper-card table-card">
      <el-table v-if="employees.length" :data="employees"
        ><el-table-column label="伙伴"
          ><template #default="scope"
            ><div class="partner-cell">
              <span>{{ scope.row.name.slice(0, 1) }}</span>
              <div>
                <strong>{{ scope.row.name }}</strong
                ><small>@{{ scope.row.username }}</small>
              </div>
            </div></template
          ></el-table-column
        ><el-table-column prop="phone" label="手机号" /><el-table-column
          prop="updateTime"
          label="最近更新"
        /><el-table-column label="状态" width="100"
          ><template #default="scope"
            ><el-tag :type="scope.row.status ? 'success' : 'info'">{{
              scope.row.status ? '正常' : '停用'
            }}</el-tag></template
          ></el-table-column
        ><el-table-column label="操作" width="170" align="right"
          ><template #default="scope"
            ><el-button text @click="openEdit(scope.row)">编辑</el-button
            ><el-button text :disabled="scope.row.username === 'admin'" @click="toggle(scope.row)">{{
              scope.row.status ? '停用' : '启用'
            }}</el-button></template
          ></el-table-column
        ></el-table
      >
      <EmptyState v-else title="没有找到伙伴" description="换个姓名试试，或邀请一位新伙伴。" />
      <el-pagination
        v-if="total"
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </section>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑伙伴资料' : '邀请新伙伴'" width="560px"
      ><el-form ref="formRef" :model="form" :rules="rules" label-position="top"
        ><div class="form-grid">
          <el-form-item label="姓名" prop="name"><el-input v-model.trim="form.name" /></el-form-item
          ><el-form-item label="登录账号" prop="username"
            ><el-input v-model.trim="form.username" /></el-form-item
          ><el-form-item label="手机号" prop="phone"><el-input v-model.trim="form.phone" /></el-form-item
          ><el-form-item label="性别"
            ><el-radio-group v-model="form.sex"
              ><el-radio value="1">男</el-radio><el-radio value="0">女</el-radio></el-radio-group
            ></el-form-item
          ><el-form-item label="身份证号"
            ><el-input v-model.trim="form.idNumber"
          /></el-form-item></div></el-form
      ><template #footer
        ><el-button @click="dialogVisible = false">取消</el-button
        ><el-button type="primary" @click="submit">{{
          form.id ? '保存修改' : '生成账号'
        }}</el-button></template
      ></el-dialog
    >
  </div>
</template>
