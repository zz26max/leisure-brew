<template>
  <div class="container">
    <h2 class="homeTitle">
      套餐总览<span><router-link to="combo">套餐管理</router-link></span>
    </h2>
    <div class="orderviewBox">
      <ul>
        <li>
          <span class="status"><i class="iconfont icon-open"></i>已上架</span>
          <span class="num">{{ setMealData.sold }}</span>
        </li>
        <li>
          <span class="status"><i class="iconfont icon-stop"></i>已下架</span>
          <span class="num">{{ setMealData.discontinued }}</span>
        </li>
        <li class="add">
          <router-link to="/combo/add">
            <i></i>
            <p>新增套餐</p>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator'
@Component({
  name: 'SetMeal',
})
export default class extends Vue {
  @Prop() private setMealData!: any
}
</script>
