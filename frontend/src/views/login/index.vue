<template>
  <div class="login">
    <div class="login-box">
      <img src="@/assets/login/login-l.png" alt="" />
      <div class="login-form">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
          <div class="login-form-title">
            <img
              src="@/assets/login/icon_logo.png"
              style="width: 149px; height: 38px"
              alt=""
            />
          </div>
          <div class="login-form-subtitle">
            <p class="brand-name">闲里茶咖</p>
            <p class="brand-desc">Leisure Brew Admin</p>
          </div>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="账号"
              prefix-icon="iconfont icon-user"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="iconfont icon-lock"
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>
          <el-form-item style="width: 100%">
            <el-button
              :loading="loading"
              class="login-btn"
              size="medium"
              type="primary"
              style="width: 100%"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import { Route } from 'vue-router'
import { Form as ElForm, Input } from 'element-ui'
import { UserModule } from '@/store/modules/user'
import { isValidUsername } from '@/utils/validate'

@Component({
  name: 'Login',
})
export default class extends Vue {
  private validateUsername = (rule: any, value: string, callback: Function) => {
    if (!value) {
      callback(new Error('请输入用户名'))
    } else {
      callback()
    }
  }
  private validatePassword = (rule: any, value: string, callback: Function) => {
    if (value.length < 6) {
      callback(new Error('密码必须在6位以上'))
    } else {
      callback()
    }
  }
  private loginForm = {
    username: 'admin',
    password: '123456',
  } as {
    username: String
    password: String
  }

  loginRules = {
    username: [{ validator: this.validateUsername, trigger: 'blur' }],
    password: [{ validator: this.validatePassword, trigger: 'blur' }],
  }
  private loading = false
  private redirect?: string

  @Watch('$route', { immediate: true })
  private onRouteChange(route: Route) {}

  // 登录
  private handleLogin() {
    ;(this.$refs.loginForm as ElForm).validate(async (valid: boolean) => {
      if (valid) {
        this.loading = true
        await UserModule.Login(this.loginForm as any)
          .then((res: any) => {
            if (String(res.code) === '1') {
              this.$router.push('/')
            } else {
              // this.$message.error(res.msg)
              this.loading = false
            }
          })
          .catch(() => {
            // this.$message.error('用户名或密码错误！')
            this.loading = false
          })
      } else {
        return false
      }
    })
  }
}
</script>

<style lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: linear-gradient(135deg, #FF7A00 0%, #FF6B00 30%, #E06900 70%, #CC5500 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 600px;
    height: 600px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 50%;
    pointer-events: none;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 400px;
    height: 400px;
    background: rgba(255, 255, 255, 0.04);
    border-radius: 50%;
    pointer-events: none;
  }
}

.login-box {
  width: 1000px;
  height: 474.38px;
  border-radius: 16px;
  display: flex;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;

  > img {
    width: 60%;
    height: auto;
    object-fit: cover;
  }
}

.title {
  margin: 0px auto 10px auto;
  text-align: left;
  color: #707070;
}

.login-form {
  background: #ffffff;
  width: 40%;
  border-radius: 0px 16px 16px 0px;
  display: flex;
  justify-content: center;
  align-items: center;
  .el-form {
    width: 240px;
  }
  .el-form-item {
    margin-bottom: 24px;
  }
  .el-form-item.is-error .el-input__inner {
    border: 1px solid #fd7065 !important;
    background: #fff !important;
  }
  .input-icon {
    height: 32px;
    width: 18px;
    margin-left: -2px;
  }
  .el-input__inner {
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 400;
    color: #1F2937;
    height: 42px;
    line-height: 42px;
    padding-left: 36px;
    transition: all 0.2s ease;
    &:focus {
      border-color: #FF7A00;
      box-shadow: 0 0 0 3px rgba(255, 122, 0, 0.1);
    }
  }
  .el-input__prefix {
    left: 12px;
    color: #9CA3AF;
  }
  .el-input--prefix .el-input__inner {
    padding-left: 36px;
  }
  .el-input__inner::placeholder {
    color: #9CA3AF;
  }
  .el-form-item--medium .el-form-item__content {
    line-height: 42px;
  }
  .el-input--medium .el-input__icon {
    line-height: 42px;
  }
}

.login-btn {
  border-radius: 8px;
  padding: 12px 20px !important;
  margin-top: 6px;
  font-weight: 600;
  font-size: 15px;
  border: 0;
  color: #FFFFFF;
  background: linear-gradient(135deg, #FF7A00, #E06900);
  transition: all 0.3s ease;
  letter-spacing: 2px;
  &:hover,
  &:focus {
    background: linear-gradient(135deg, #FF9F43, #FF7A00);
    color: #FFFFFF;
    box-shadow: 0 4px 15px rgba(255, 122, 0, 0.4);
    transform: translateY(-1px);
  }
}
.login-form-title {
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 12px;
  .title-label {
    font-weight: 500;
    font-size: 20px;
    color: #1F2937;
    margin-left: 10px;
  }
}
.login-form-subtitle {
  text-align: center;
  margin-bottom: 32px;
  .brand-name {
    font-size: 18px;
    font-weight: 700;
    color: #1F2937;
    margin: 0 0 4px;
    letter-spacing: 1px;
  }
  .brand-desc {
    font-size: 12px;
    color: #9CA3AF;
    margin: 0;
    letter-spacing: 0.5px;
  }
}
</style>
