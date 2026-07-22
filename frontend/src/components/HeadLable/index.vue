<template>
  <header class="page-heading">
    <button v-if="goback" type="button" class="page-heading__back" @click="goBack">
      <i class="el-icon-back" />
      返回
    </button>
    <div v-if="!butList" class="page-heading__title">
      <small>LEISURE BREW</small>
      <h2>{{ title }}</h2>
    </div>
    <div v-else class="page-heading__content">
      <slot />
    </div>
  </header>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({ name: 'PageHeading' })
export default class extends Vue {
  @Prop({ default: false }) private goback!: boolean
  @Prop({ default: false }) private butList!: boolean
  @Prop({ default: '' }) private title!: string

  private goBack() {
    this.$router.go(-1)
  }
}
</script>

<style lang="scss" scoped>
.page-heading {
  display: flex;
  align-items: center;
  gap: 18px;
  min-height: 78px;
  margin-bottom: 18px;
  padding: 14px 24px;
  background: $color-card-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;

  &__back {
    display: inline-flex;
    align-items: center;
    gap: 7px;
    padding: 8px 14px 8px 0;
    color: $color-text-secondary;
    background: transparent;
    border: 0;
    border-right: 1px solid $color-border;
    cursor: pointer;
    font: inherit;

    &:hover {
      color: $color-primary;
    }
  }

  &__title {
    small {
      display: block;
      margin-bottom: 3px;
      color: $color-accent;
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.18em;
    }

    h2 {
      margin: 0;
      color: $color-text-primary;
      font-family: 'Songti SC', STSong, SimSun, serif;
      font-size: 20px;
      letter-spacing: 0.04em;
    }
  }

  &__content {
    width: 100%;
  }
}
</style>
