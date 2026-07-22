<template>
  <div class="page-shell">
    <section class="content-card">
      <div class="table-toolbar">
        <el-input v-model.trim="keyword" placeholder="按姓名查找员工" clearable @clear="search" @keyup.enter.native="search" />
        <el-button @click="search">
          查询
        </el-button>
        <el-button class="add-button" type="primary" @click="$router.push('/employee/add')">
          添加员工
        </el-button>
      </div>

      <el-table v-if="employees.length" :data="employees" stripe>
        <el-table-column prop="name" label="员工姓名" />
        <el-table-column prop="username" label="登录账号" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="账号状态" width="120">
          <template slot-scope="scope">
            <span :class="['status-pill', { muted: scope.row.status === 0 }]">
              {{ scope.row.status === 1 ? '正常' : '已停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最近更新" />
        <el-table-column label="操作" width="150" align="right">
          <template slot-scope="scope">
            <el-button type="text" :disabled="scope.row.username === 'admin'" @click="edit(scope.row)">
              编辑
            </el-button>
            <el-button type="text" :disabled="scope.row.username === 'admin'" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else :is-search="Boolean(keyword)" />

      <el-pagination
        :current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="changePage"
      />
    </section>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import Empty from '@/components/Empty/index.vue'
import { enableOrDisableEmployee, getEmployeeList } from '@/api/employee'

@Component({ name: 'Employee', components: { Empty } })
export default class extends Vue {
  private keyword = ''
  private page = 1
  private pageSize = 10
  private total = 0
  private employees: any[] = []

  created() { this.loadEmployees() }

  private async loadEmployees() {
    const response = await getEmployeeList({
      page: this.page,
      pageSize: this.pageSize,
      name: this.keyword || undefined,
    })
    if (Number(response.data.code) === 1) {
      this.employees = response.data.data.records || []
      this.total = response.data.data.total || 0
    }
  }

  private search() { this.page = 1; this.loadEmployees() }
  private changePage(page: number) { this.page = page; this.loadEmployees() }
  private edit(employee: any) { this.$router.push({ path: '/employee/add', query: { id: employee.id } }) }

  private async toggleStatus(employee: any) {
    const nextStatus = employee.status === 1 ? 0 : 1
    await this.$confirm(
      nextStatus === 1 ? `确认重新启用 ${employee.name} 的账号？` : `确认停用 ${employee.name} 的账号？`,
      '调整账号状态',
      { type: 'warning' }
    )
    const response = await enableOrDisableEmployee(employee.id, nextStatus)
    if (Number(response.data.code) === 1) {
      this.$message.success(nextStatus === 1 ? '账号已启用' : '账号已停用')
      this.loadEmployees()
    }
  }
}
</script>

<style lang="scss" scoped>
.page-shell { padding: 24px; }
.content-card { padding: 24px; background: $color-card-bg; border: 1px solid $color-border-light; border-radius: $radius-lg; box-shadow: $shadow-sm; }
.table-toolbar { display: flex; gap: 10px; margin-bottom: 20px; .el-input { width: 260px; } .add-button { margin-left: auto; } }
.status-pill { display: inline-block; padding: 4px 10px; color: $color-success; background: rgba(63, 122, 91, .1); border-radius: 999px; &.muted { color: $color-text-muted; background: #eeeae1; } }
.el-pagination { margin-top: 22px; text-align: right; }
</style>
