<template>
  <section class="dashboard-card menu-card">
    <header class="menu-card__header">
      <div class="menu-card__icon">
        <i class="el-icon-coffee-cup" />
      </div>
      <div>
        <p>DRINKS</p>
        <h2>饮品菜单</h2>
      </div>
      <router-link to="/drink">
        查看菜单
      </router-link>
    </header>
    <div class="menu-card__counts">
      <span><i class="is-live" /> 已上架 <strong>{{ count(dishesData.sold) }}</strong></span>
      <span><i /> 已下架 <strong>{{ count(dishesData.discontinued) }}</strong></span>
    </div>
    <router-link class="menu-card__action" to="/drink/add">
      <i class="el-icon-plus" /> 上新饮品
    </router-link>
  </section>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({ name: 'DrinkSummary' })
export default class extends Vue {
  @Prop({ default: () => ({}) }) private dishesData!: any

  private count(value: unknown) {
    return Number(value || 0)
  }
}
</script>

<style lang="scss" scoped src="./menu-summary.scss"></style>
