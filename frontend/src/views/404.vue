<template>
  <main class="not-found">
    <section class="not-found__card">
      <div class="not-found__mark" aria-hidden="true">
        <span class="not-found__steam" />
        <i class="el-icon-coffee-cup" />
      </div>
      <p class="not-found__code">
        404 · NOT FOUND
      </p>
      <h1>这杯暂时找不到</h1>
      <p class="not-found__copy">
        可能是地址写错了，也可能这页刚好去歇了一会儿。
      </p>
      <el-button type="primary" @click="$router.push('/')">
        回到今日店况
      </el-button>
    </section>
  </main>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'

@Component({ name: 'Page404' })
export default class extends Vue {}
</script>

<style lang="scss" scoped>
.not-found {
  display: grid;
  min-height: 100%;
  padding: 40px;
  background:
    radial-gradient(circle at 70% 18%, rgba(184, 101, 59, 0.09), transparent 24%),
    $color-page-bg;
  place-items: center;

  &__card {
    width: min(520px, 100%);
    padding: 64px 54px;
    text-align: center;
    background: $color-card-bg;
    border: 1px solid $color-border-light;
    border-radius: $radius-xl;
    box-shadow: $shadow-md;
  }

  &__mark {
    position: relative;
    display: grid;
    width: 92px;
    height: 92px;
    margin: 0 auto 28px;
    color: #fffaf0;
    background: $color-primary;
    border-radius: 28px 28px 28px 9px;
    place-items: center;

    i {
      font-size: 38px;
    }
  }

  &__steam {
    position: absolute;
    top: -14px;
    left: 44px;
    width: 3px;
    height: 21px;
    background: $color-accent;
    border-radius: 99px;
    box-shadow: 11px 4px 0 rgba(184, 101, 59, 0.55);
    transform: rotate(-8deg);
  }

  &__code {
    margin: 0 0 10px;
    color: $color-accent;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.18em;
  }

  h1 {
    margin: 0;
    color: $color-text-primary;
    font-family: 'Songti SC', STSong, SimSun, serif;
    font-size: 30px;
    letter-spacing: 0.05em;
  }

  &__copy {
    margin: 14px 0 30px;
    color: $color-text-secondary;
    line-height: 1.8;
  }
}
</style>
