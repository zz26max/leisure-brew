<template>
  <div class="navbar">
    <div class="statusBox">
      <hamburger id="hamburger-container"
                 :is-active="sidebar.opened"
                 class="hamburger-container"
                 @toggleClick="toggleSideBar" />
      <span v-if="status===1"
            class="businessBtn">营业中</span>
      <span v-else
            class="businessBtn closing">打烊中</span>
    </div>

    <div :key="restKey"
         class="right-menu">
      <div class="rightStatus">
        <audio ref="audioVo"
               hidden>
          <source src="./../../../assets/preview.mp3" type="audio/mp3" />
        </audio>
        <audio ref="audioVo2"
               hidden>
          <source src="./../../../assets/reminder.mp3" type="audio/mp3" />
        </audio>
        <span class="navicon operatingState" @click="handleStatus"><i />营业状态设置</span>
      </div>
      <div class="avatar-wrapper">
        <div :class="shopShow?'userInfo':''"
             @mouseenter="toggleShow"
             @mouseleave="mouseLeaves">
          <el-button type="primary"
                     :class="shopShow?'active':''">
            {{ name }}<i class="el-icon-arrow-down" />
          </el-button>
          <div v-if="shopShow"
               class="userList">
            <p class="amendPwdIcon"
               @click="handlePwd">
              修改密码<i />
            </p>
            <p class="outLogin"
               @click="logout">
              退出登录<i />
            </p>
          </div>
        </div>
      </div>
    </div>
    <!-- 营业状态弹层 -->
    <el-dialog title="营业状态设置"
               :visible.sync="dialogVisible"
               width="25%"
               :show-close="false">
      <el-radio-group v-model="setStatus">
        <el-radio :label="1">
          营业中
          <span>当前店铺处于营业状态，自动接收任何订单，可点击打烊进入休息状态。</span>
        </el-radio>
        <el-radio :label="0">
          打烊中
          <span>当前店铺处于休息状态，仅接受营业时间内的预定订单，可点击营业中手动恢复营业状态。</span>
        </el-radio>
      </el-radio-group>
      <span slot="footer"
            class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary"
                   @click="handleSave">确 定</el-button>
      </span>
    </el-dialog>
    <!-- end -->
    <!-- 修改密码 -->
    <Password :dialog-form-visible="dialogFormVisible"
              @handleclose="handlePwdClose" />
    <!-- end -->
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import { UserModule } from '@/store/modules/user'
import Breadcrumb from '@/components/Breadcrumb/index.vue'
import Hamburger from '@/components/Hamburger/index.vue'
import { getStatus, setStatus } from '@/api/users'
import Cookies from 'js-cookie'
import { debounce, throttle } from '@/utils/common'
import { setNewData, getNewData } from '@/utils/cookies'

// 接口
import { getCountUnread } from '@/api/inform'
// 修改密码弹层
import Password from '../components/password.vue'

@Component({
  name: 'Navbar',
  components: {
    Breadcrumb,
    Hamburger,
    Password,
  },
})
export default class extends Vue {
  private storeId = this.getStoreId
  private restKey: number = 0
  private websocket: WebSocket | null = null
  private reconnectTimer: number | null = null
  private newOrder = ''
  private message = ''
  private audioIsPlaying = false
  private audioPaused = false
  private statusValue = true
  private audioUrl: './../../../assets/preview.mp3'
  private shopShow = false
  private dialogVisible = false
  private status = 1
  private setStatus = 1
  private dialogFormVisible = false
  private ountUnread = 0
  // get ountUnread() {
  //   return Number(getNewData())
  // }
  get sidebar() {
    return AppModule.sidebar
  }

  get device() {
    return AppModule.device.toString()
  }

  getuserInfo() {
    return UserModule.userInfo
  }

  get name() {
    return (UserModule.userInfo as any).name
      ? (UserModule.userInfo as any).name
      : JSON.parse(Cookies.get('user_info') as any).name
  }

  get getStoreId() {
    let storeId = ''
    if (UserModule.storeId) {
      storeId = UserModule.storeId
    } else if ((UserModule.userInfo as any).stores != null) {
      storeId = (UserModule.userInfo as any).stores[0].storeId
    }
    return storeId
  }
  mounted() {
    document.addEventListener('click', this.handleClose)
    //console.log(this.$store.state.app.statusNumber)
    // const msg = {
    //   data: {
    //     type: 2,
    //     content: '订单1653904906519客户催单，已下单23分钟，仍未接单。',
    //     details: '434'
    //   }
    // }
    this.getStatus()
  }
  created() {
    this.webSocket()
  }
  onload() {
  }
  destroyed() {
    if (this.websocket) {
      this.websocket.close() //离开路由之后断开websocket连接
    }
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  // 添加新订单提示弹窗
  webSocket() {
    const that = this as any
    if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
      return
    }
    const currentUser = (UserModule.userInfo as any) || {}
    const clientId =
      currentUser.id || Math.random().toString(36).substr(2)
    const socketUrl = this.buildSocketUrl(clientId)
    console.log(socketUrl, 'socketUrl')
    if (typeof WebSocket == 'undefined') {
      that.$notify({
        title: '提示',
        message: '当前浏览器无法接收实时报警信息，请使用谷歌浏览器！',
        type: 'warning',
        duration: 0,
      })
    } else {
      this.websocket = new WebSocket(socketUrl)
      // 监听socket打开
      this.websocket.onopen = function () {
        console.log('浏览器WebSocket已打开')
        if (that.reconnectTimer) {
          window.clearTimeout(that.reconnectTimer)
          that.reconnectTimer = null
        }
      }
      // 监听socket消息接收
      this.websocket.onmessage = function (msg) {
        // 转换为json对象
        that.$refs.audioVo.currentTime = 0
        that.$refs.audioVo2.currentTime = 0

        console.log(msg, JSON.parse(msg.data), 'msg')
        // const h = this.$createElement
        const jsonMsg = JSON.parse(msg.data)
        if (jsonMsg.type === 1) {
          that.$refs.audioVo.play()
        } else if (jsonMsg.type === 2) {
          that.$refs.audioVo2.play()
        }
        that.$notify({
          title: jsonMsg.type === 1 ? '待接单' : '催单',
          duration: 0,
          dangerouslyUseHTMLString: true,
          onClick: () => {
            that.$router
              .push(`/order?orderId=${jsonMsg.orderId}`)
              .catch((err) => {
                console.log(err)
              })
            setTimeout(() => {
              location.reload()
            }, 100)
          },
          // 这里也可以把返回信息加入到message中显示
          message: `${
            jsonMsg.type === 1
              ? `<span>您有1个<span style=color:#419EFF>订单待处理</span>,${jsonMsg.content},请及时接单</span>`
              : `${jsonMsg.content}<span style='color:#419EFF;cursor: pointer'>去处理</span>`
          }`,
        })
      }
      // 监听socket错误
      this.websocket.onerror = function (event) {
        console.log('WebSocket错误', event)
        that.$notify({
          title: '错误',
          message: '服务器错误，无法接收实时报警信息',
          type: 'error',
          duration: 0,
        })
        that.reconnectWebSocket()
      }
      // 监听socket关闭
      this.websocket.onclose = function (event) {
        console.log('WebSocket已关闭', event.code, event.reason)
        that.reconnectWebSocket()
      }
    }
  }

  buildSocketUrl(clientId: string | number) {
    const configured = String(process.env.VUE_APP_SOCKET_URL || '')
      .replace(/^['"]|['"]$/g, '')
      .trim()
    // !未配置或配置异常时，按当前页面地址自动拼接ws端点
    const base =
      configured ||
      `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/`
    return `${base.replace(/\/+$/, '')}/${clientId}`
  }

  reconnectWebSocket() {
    if (this.reconnectTimer) {
      return
    }
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null
      this.webSocket()
    }, 3000)
  }

  private toggleSideBar() {
    AppModule.ToggleSideBar(false)
  }
  // 退出
  private async logout() {
    this.$store.dispatch('LogOut').then(() => {
      // location.href = '/'
      this.$router.replace({ path: '/login' })
    })
    // this.$router.push(`/login?redirect=${this.$route.fullPath}`)
  }
  // 获取未读消息
  async getCountUnread() {
    const { data } = await getCountUnread()
    if (data.code === 1) {
      // this.ountUnread = data.data
      AppModule.StatusNumber(data.data)
      // setNewData(data.data)
      // this.$message.success('操作成功！')
    } else {
      this.$message.error(data.msg)
    }
  }
  // 营业状态
  async getStatus() {
    const { data } = await getStatus()
    this.status = data.data
    this.setStatus = this.status
  }
  // 下拉菜单显示
  toggleShow() {
    this.shopShow = true
  }
  // 下拉菜单隐藏
  mouseLeaves() {
    this.shopShow = false
  }
  // 触发空白处下来菜单关闭
  handleClose() {
    // clearTimeout(this.leave)
    // this.shopShow = false
  }
  // 设置营业状态
  handleStatus() {
    this.dialogVisible = true
  }
  // 营业状态设置
  async handleSave() {
    const { data } = await setStatus(this.setStatus)
    if (data.code === 1) {
      this.dialogVisible = false
      this.getStatus()
    }
  }
  // 修改密码
  handlePwd() {
    this.dialogFormVisible = true
  }
  // 关闭密码编辑弹层
  handlePwdClose() {
    this.dialogFormVisible = false
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 60px;
  position: relative;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-bottom: 1px solid $color-border;
  .statusBox {
    float: left;
    height: 100%;
    align-items: center;
    display: flex;
  }
  .hamburger-container {
    // line-height: 54px;

    padding: 0 12px 0 20px;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }
  .right-menu {
    float: right;

    margin-right: 20px;

    color: #333333;
    font-size: 14px;

    span {
      padding: 0 10px;
      width: 130px;
      display: inline-block;
      cursor: pointer;
      &:hover {
        background: rgba(255, 255, 255, 0.52);
      }
    }
    .amendPwdIcon {
      i {
        width: 18px;
        height: 18px;
        background: url(./../../../assets/icons/btn_gaimi@2x.png) no-repeat;
        background-size: contain;
        margin-top: 8px;
      }
    }
    .outLogin {
      i {
        width: 18px;
        height: 18px;
        background: url(./../../../assets/icons/btn_close@2x.png) no-repeat 100%
          100%;
        background-size: contain;
        margin-top: 8px;
      }
    }
    .outLogin {
      cursor: pointer;
    }

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    // .avatar-container {
    // margin-right: 30px;

    // }
  }
  .rightStatus {
    height: 100%;
    line-height: 60px;
    display: flex;
    align-items: center;
    float: left;
  }
  .avatar-wrapper {
    margin-top: 14px;
    margin-left: 18px;
    position: relative;
    // vertical-align: middle;
    float: right;
    width: 120px;
    text-align: left;
    .user-avatar {
      cursor: pointer;
      width: 40px;
      height: 40px;
      border-radius: 10px;
    }

    .el-icon-caret-bottom {
      cursor: pointer;
      position: absolute;
      right: -20px;
      top: 25px;
      font-size: 12px;
    }

    .el-button--primary {
      background: rgba(255, 122, 0, 0.08);
      border-radius: $radius-md;
      padding-top: 0px;
      padding-bottom: 0px;
      position: relative;
      width: 120px;
      padding-left: 12px;
      text-align: left;
      border: 0 none;
      height: 32px;
      line-height: 32px;
      color: $color-text-primary;
      &.active {
        background: rgba(255, 122, 0, 0.05);
        border: 0 none;
        .el-icon-arrow-down {
          transform: rotate(-180deg);
        }
      }
    }
  }
  .businessBtn {
    height: 22px;
    line-height: 20px;
    background: #fd3333;
    border: 1px solid #ffffff;
    border-radius: 4px;
    display: inline-block;
    padding: 0 6px;
    color: #fff;
  }
  .closing {
    background: #6a6a6a;
  }
  .navicon {
    i {
      display: inline-block;
      width: 18px;
      height: 18px;
      vertical-align: sub;
      margin: 0 4px 0 0;
    }
  }
  .operatingState {
    i {
      background: url('./../../../assets/icons/time.png') no-repeat;
      background-size: contain;
    }
  }
  .mesCenter {
    i {
      background: url('./../../../assets/icons/msg.png') no-repeat;
      background-size: contain;
    }
  }
  // .el-badge__content.is-fixed {
  //   top: 20px;
  //   right: 6px;
  // }
}
</style>
<style lang="scss">
.el-notification {
  // background: rgba(255, 255, 255, 0.71);
  width: 419px !important;
  .el-notification__title {
    margin-bottom: 14px;
    color: #333;
    .el-notification__content {
      color: #333;
    }
  }
}
.navbar {
  .el-dialog {
    min-width: auto !important;
  }
  .el-dialog__header {
    height: 61px;
    line-height: 60px;
    background: #fbfbfa;
    padding: 0 30px;
    font-size: 16px;
    color: #333;
    border: 0 none;
  }
  .el-dialog__body {
    padding: 10px 30px 30px;
    .el-radio,
    .el-radio__input {
      white-space: normal;
    }
    .el-radio__label {
      padding-left: 5px;
      color: #333;
      font-weight: 700;
      span {
        display: block;
        line-height: 20px;
        padding-top: 12px;
        color: #666;
        font-weight: normal;
      }
    }
    .el-radio__input.is-checked .el-radio__inner {
      &::after {
        background: #333;
      }
    }
    .el-radio-group {
      & > .is-checked {
        border: 1px solid $color-primary;
      }
    }
    .el-radio {
      width: 100%;
      background: #fbfbfa;
      border: 1px solid #e5e4e4;
      border-radius: 4px;
      padding: 14px 22px;
      margin-top: 20px;
    }
    .el-radio__input.is-checked + .el-radio__label {
      span {
      }
    }
  }
  .el-badge__content.is-fixed {
    top: 24px;
    right: 2px;
    width: 18px;
    height: 18px;
    font-size: 10px;
    line-height: 16px;
    font-size: 10px;
    border-radius: 50%;
    padding: 0;
  }
  .badgeW {
    .el-badge__content.is-fixed {
      width: 30px;
      border-radius: 20px;
    }
  }
}
.el-icon-arrow-down {
  background: url('./../../../assets/icons/up.png') no-repeat 50% 50%;
  background-size: contain;
  width: 8px;
  height: 8px;
  transform: rotate(0eg);
  margin-left: 16px;
  position: absolute;
  right: 16px;
  top: 12px;
  &:before {
    content: '';
  }
}

.userInfo {
  background: #fff;
  position: absolute;
  top: 0px;
  left: 0;
  z-index: 99;
  box-shadow: $shadow-lg;
  width: 100%;
  border-radius: $radius-md;
  line-height: 32px;
  padding: 0 0 5px;
  height: 105px;
  border: 1px solid $color-border;
  .userList {
    width: 95%;
    padding-left: 5px;
  }
  p {
    cursor: pointer;
    height: 32px;
    line-height: 32px;
    padding: 0 5px 0 7px;
    border-radius: 4px;
    i {
      margin-left: 10px;
      vertical-align: middle;
      margin-top: 4px;
      float: right;
    }
    &:hover {
      background: #fef7f0;
    }
  }
}
.msgTip {
  color: #419eff;
  padding: 0 5px;
}
// .el-dropdown{
//   .el-button--primary{
//     height: 32px;
//     background: rgba(255,255,255,0.52);
//     border-radius: 4px;
//     padding-top: 0px;
//     padding-bottom: 0px;
//   }
//   margin-top: 2px;
// }
// .el-popper{
//   top: 45px !important;
//   padding-top: 50px !important;
//   border-radius: 0 0 4px 4px;
// }
// .el-popper[x-placement^=bottom] .popper__arrow::after,.popper__arrow{
//   display: none !important;
// }
</style>
