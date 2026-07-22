<template>
  <div class="empty-state" role="status">
    <div class="empty-state__icon">
      <i :class="isSearch ? 'el-icon-search' : 'el-icon-coffee-cup'" />
      <span v-if="!isSearch" />
    </div>
    <h3>{{ isSearch ? '没有找到匹配结果' : '这里还没有内容' }}</h3>
    <p>
      {{
        isSearch
          ? '换个关键词，或清除筛选后再试。'
          : '从第一杯开始，把这里慢慢添满。'
      }}
    </p>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({ name: 'EmptyState' })
export default class extends Vue {
  @Prop({ default: false }) private isSearch!: boolean
}
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  min-height: 330px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  padding: 54px 20px;
  text-align: center;

  &__icon {
    position: relative;
    display: grid;
    width: 78px;
    height: 78px;
    margin-bottom: 22px;
    color: $color-primary;
    background: rgba(35, 72, 63, 0.08);
    border: 1px solid rgba(35, 72, 63, 0.12);
    border-radius: 24px 24px 24px 8px;
    place-items: center;

    i {
      font-size: 32px;
    }

    span {
      position: absolute;
      top: -10px;
      width: 2px;
      height: 15px;
      background: $color-accent;
      border-radius: 99px;
      box-shadow: 8px 4px 0 rgba(184, 101, 59, 0.5);
      transform: rotate(-8deg);
    }
  }

  h3 {
    margin: 0;
    color: $color-text-primary;
    font-family: 'Songti SC', STSong, SimSun, serif;
    font-size: 18px;
  }

  p {
    margin: 9px 0 0;
    color: $color-text-muted;
    font-size: 13px;
  }
}
</style>
