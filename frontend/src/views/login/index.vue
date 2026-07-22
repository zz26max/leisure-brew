<template>
  <main class="login-page">
    <section class="login-shell" aria-label="闲里茶咖门店运营台登录">
      <div class="brand-panel">
        <div class="brand-panel__art" aria-hidden="true">
          <span class="brand-panel__sun" />
          <span class="brand-panel__leaf brand-panel__leaf--one" />
          <span class="brand-panel__leaf brand-panel__leaf--two" />
          <span class="brand-panel__steam brand-panel__steam--one" />
          <span class="brand-panel__steam brand-panel__steam--two" />
          <span class="brand-panel__cup" />
          <span class="brand-panel__saucer" />
        </div>
        <div class="brand-panel__shade" />
        <div class="brand-panel__content">
          <div class="brand-lockup brand-lockup--light">
            <span class="brand-lockup__mark">闲</span>
            <span class="brand-lockup__name">
              <strong>闲里茶咖</strong>
              <small>LEISURE BREW</small>
            </span>
          </div>
          <div class="brand-panel__message">
            <p>忙里偷闲，杯中有序。</p>
            <span>把每一杯认真做好，也把每一天从容打理。</span>
          </div>
        </div>
      </div>

      <div class="login-panel">
        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.native.prevent
        >
          <div class="login-form__eyebrow">
            门店运营台
          </div>
          <h1>欢迎回来</h1>
          <p class="login-form__intro">
            登录后查看今日店况与待处理订单。
          </p>

          <el-form-item prop="username">
            <label class="field-label" for="username">账号</label>
            <el-input
              id="username"
              v-model.trim="loginForm.username"
              type="text"
              autocomplete="username"
              placeholder="请输入账号"
              prefix-icon="iconfont icon-user"
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="field-label" for="password">密码</label>
            <el-input
              id="password"
              v-model="loginForm.password"
              type="password"
              autocomplete="current-password"
              placeholder="请输入密码"
              prefix-icon="iconfont icon-lock"
              show-password
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>

          <el-button
            :loading="loading"
            class="login-button"
            type="primary"
            native-type="submit"
            @click="handleLogin"
          >
            {{ loading ? '正在进入…' : '进入门店' }}
          </el-button>

          <p class="login-form__note">
            仅限已授权的门店伙伴使用
          </p>
        </el-form>
      </div>
    </section>
  </main>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { Form as ElForm } from 'element-ui'
import { UserModule } from '@/store/modules/user'

@Component({
  name: 'Login',
})
export default class extends Vue {
  private loginForm = {
    username: '',
    password: '',
  }

  private loading = false

  private validateUsername = (
    rule: unknown,
    value: string,
    callback: (error?: Error) => void
  ) => {
    callback(value ? undefined : new Error('请输入账号'))
  }

  private validatePassword = (
    rule: unknown,
    value: string,
    callback: (error?: Error) => void
  ) => {
    if (!value) {
      callback(new Error('请输入密码'))
      return
    }
    callback(value.length >= 6 ? undefined : new Error('密码至少需要 6 位'))
  }

  private loginRules = {
    username: [{ validator: this.validateUsername, trigger: 'blur' }],
    password: [{ validator: this.validatePassword, trigger: 'blur' }],
  }

  private handleLogin() {
    const form = this.$refs.loginForm as ElForm
    form.validate(async (valid: boolean) => {
      if (!valid) return

      this.loading = true
      try {
        const result: any = await UserModule.Login(this.loginForm)
        if (String(result.code) === '1') {
          await this.$router.push('/')
        }
      } finally {
        this.loading = false
      }
    })
  }
}
</script>

<style lang="scss">
.login-page {
  display: grid;
  min-height: 100%;
  place-items: center;
  padding: 48px;
  background:
    radial-gradient(circle at 12% 16%, rgba(184, 101, 59, 0.1), transparent 28%),
    linear-gradient(145deg, #f8f4eb 0%, $color-page-bg 62%, #eee5d5 100%);
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(480px, 1.25fr) minmax(390px, 0.75fr);
  width: min(1080px, calc(100vw - 96px));
  min-height: 620px;
  overflow: hidden;
  background: $color-card-bg;
  border: 1px solid rgba(35, 72, 63, 0.12);
  border-radius: 28px;
  box-shadow: 0 30px 90px rgba(35, 48, 41, 0.16);
}

.brand-panel {
  position: relative;
  min-height: 620px;
  overflow: hidden;
  background: $color-primary-dark;

  &__art,
  &__shade {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  &__art {
    background:
      linear-gradient(145deg, transparent 58%, rgba(246, 241, 231, 0.05) 58%),
      radial-gradient(circle at 18% 24%, rgba(246, 241, 231, 0.09), transparent 26%),
      $color-primary-dark;
  }

  &__sun {
    position: absolute;
    top: 118px;
    right: 82px;
    width: 220px;
    height: 220px;
    background: rgba(184, 101, 59, 0.72);
    border-radius: 50%;
  }

  &__cup {
    position: absolute;
    right: 108px;
    bottom: 122px;
    width: 210px;
    height: 126px;
    background: #e8deca;
    border-radius: 12px 12px 88px 88px;
    box-shadow: inset 0 12px rgba(255, 252, 247, 0.5);

    &::after {
      position: absolute;
      top: 18px;
      right: -56px;
      width: 64px;
      height: 70px;
      border: 18px solid #e8deca;
      border-left: 0;
      border-radius: 0 44px 44px 0;
      content: '';
    }
  }

  &__saucer {
    position: absolute;
    right: 70px;
    bottom: 100px;
    width: 300px;
    height: 32px;
    background: rgba(232, 222, 202, 0.76);
    border-radius: 50%;
  }

  &__steam {
    position: absolute;
    right: 188px;
    bottom: 270px;
    width: 24px;
    height: 90px;
    border-left: 3px solid rgba(246, 241, 231, 0.6);
    border-radius: 50%;
    transform: rotate(12deg);

    &--two {
      right: 244px;
      bottom: 250px;
      height: 116px;
      transform: rotate(-10deg);
    }
  }

  &__leaf {
    position: absolute;
    width: 92px;
    height: 42px;
    background: #768f78;
    border-radius: 100% 0 100% 0;
    transform: rotate(32deg);

    &--one { top: 92px; left: 112px; }
    &--two { top: 148px; left: 174px; transform: rotate(112deg) scale(0.72); }
  }

  &__shade {
    background:
      linear-gradient(180deg, rgba(18, 48, 41, 0.28), rgba(18, 48, 41, 0.78)),
      linear-gradient(90deg, rgba(18, 48, 41, 0.3), transparent 70%);
  }

  &__content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
    padding: 44px 48px 52px;
  }

  &__message {
    max-width: 430px;
    color: #fdf7eb;

    p {
      margin: 0 0 14px;
      font-family: 'Songti SC', STSong, SimSun, serif;
      font-size: 38px;
      line-height: 1.25;
      letter-spacing: 0.08em;
    }

    span {
      color: rgba(253, 247, 235, 0.76);
      font-size: 14px;
      line-height: 1.8;
    }
  }
}

.brand-lockup {
  display: inline-flex;
  align-items: center;
  gap: 12px;

  &__mark {
    display: grid;
    width: 42px;
    height: 42px;
    color: #fffaf0;
    background: $color-accent;
    border-radius: 12px 12px 12px 4px;
    place-items: center;
    font-family: 'Songti SC', STSong, SimSun, serif;
    font-size: 24px;
    font-weight: 700;
  }

  &__name {
    display: flex;
    flex-direction: column;
    gap: 3px;

    strong {
      font-family: 'Songti SC', STSong, SimSun, serif;
      font-size: 20px;
      letter-spacing: 0.12em;
    }

    small {
      font-size: 9px;
      font-weight: 600;
      letter-spacing: 0.24em;
    }
  }

  &--light {
    color: #fffaf0;
  }
}

.login-panel {
  display: grid;
  padding: 58px 64px;
  background: $color-card-bg;
  place-items: center;
}

.login-form {
  width: 100%;
  max-width: 330px;

  &__eyebrow {
    margin-bottom: 14px;
    color: $color-accent;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.18em;
  }

  h1 {
    margin: 0;
    color: $color-text-primary;
    font-family: 'Songti SC', STSong, SimSun, serif;
    font-size: 34px;
    line-height: 1.3;
    letter-spacing: 0.04em;
  }

  &__intro {
    margin: 10px 0 36px;
    color: $color-text-secondary;
    line-height: 1.7;
  }

  .el-form-item {
    margin-bottom: 24px;
  }

  .el-form-item__content {
    line-height: normal;
  }

  .field-label {
    display: block;
    margin-bottom: 9px;
    color: $color-text-primary;
    font-size: 13px;
    font-weight: 600;
  }

  .el-input__inner {
    height: 48px;
    padding-left: 42px;
    line-height: 48px;
    background: #fffefa;
    border-radius: $radius-md;
  }

  .el-input__prefix {
    left: 13px;
    color: $color-text-muted;
  }

  .el-input__icon {
    line-height: 48px;
  }

  .login-button {
    width: 100%;
    height: 48px;
    margin-top: 4px;
    font-size: 15px;
    letter-spacing: 0.08em;
  }

  &__note {
    margin: 20px 0 0;
    color: $color-text-muted;
    font-size: 12px;
    text-align: center;
  }
}

@media (max-width: 1100px) {
  .login-page {
    padding: 32px;
  }

  .login-shell {
    grid-template-columns: 1fr 420px;
    width: calc(100vw - 64px);
  }

  .brand-panel__content {
    padding-right: 36px;
    padding-left: 36px;
  }

  .login-panel {
    padding-right: 44px;
    padding-left: 44px;
  }
}
</style>
