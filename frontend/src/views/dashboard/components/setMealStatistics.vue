<template>
  <section class="dashboard-card menu-card">
    <header class="menu-card__header">
      <div class="menu-card__icon is-clay">
        <i class="el-icon-collection-tag" />
      </div>
      <div>
        <p>PAIRINGS</p>
        <h2>搭配套餐</h2>
      </div>
      <router-link to="/combo">
        查看搭配
      </router-link>
    </header>
    <div class="menu-card__counts">
      <span><i class="is-live" /> 已上架 <strong>{{ count(setMealData.sold) }}</strong></span>
      <span><i /> 已下架 <strong>{{ count(setMealData.discontinued) }}</strong></span>
    </div>
    <router-link class="menu-card__action" to="/combo/add">
      <i class="el-icon-plus" /> 新建搭配
    </router-link>
  </section>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({ name: 'SetMealSummary' })
export default class extends Vue {
  @Prop({ default: () => ({}) }) private setMealData!: any

  private count(value: unknown) {
    return Number(value || 0)
  }
}
</script>

<style lang="scss" scoped src="./menu-summary.scss"></style>
