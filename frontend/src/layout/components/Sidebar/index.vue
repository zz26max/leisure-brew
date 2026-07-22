<template>
  <aside class="sidebar-panel">
    <div :class="['sidebar-brand', { 'is-collapsed': isCollapse }]">
      <span class="sidebar-brand__mark">闲</span>
      <span v-if="!isCollapse" class="sidebar-brand__name">
        <strong>闲里茶咖</strong>
        <small>LEISURE BREW</small>
      </span>
    </div>

    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
          :is-collapse="isCollapse"
        />
      </el-menu>
    </el-scrollbar>

    <div v-if="!isCollapse" class="sidebar-note">
      <span />
      门店运营台
    </div>
  </aside>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import SidebarItem from './SidebarItem.vue'
import variables from '@/styles/_variables.scss'

@Component({
  name: 'SideBar',
  components: { SidebarItem },
})
export default class extends Vue {
  get sidebar() {
    return AppModule.sidebar
  }

  get routes() {
    const rootRoute = (this.$router as any).options.routes.find(
      (route) => route.path === '/'
    )
    return rootRoute && rootRoute.children ? rootRoute.children : []
  }

  get variables() {
    return variables
  }

  get isCollapse() {
    return !this.sidebar.opened
  }
}
</script>

<style lang="scss" scoped>
.sidebar-panel {
  position: relative;
  height: 100%;
  background: $color-sidebar-bg;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 72px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 250, 240, 0.1);

  &.is-collapsed {
    justify-content: center;
    padding: 0;
  }

  &__mark {
    display: grid;
    flex: 0 0 38px;
    width: 38px;
    height: 38px;
    color: #fffaf0;
    background: $color-accent;
    border-radius: 11px 11px 11px 4px;
    place-items: center;
    font-family: 'Songti SC', STSong, SimSun, serif;
    font-size: 22px;
    font-weight: 700;
  }

  &__name {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: 2px;
    color: #fffaf0;

    strong {
      font-family: 'Songti SC', STSong, SimSun, serif;
      font-size: 17px;
      letter-spacing: 0.1em;
      white-space: nowrap;
    }

    small {
      color: rgba(255, 250, 240, 0.58);
      font-size: 8px;
      font-weight: 600;
      letter-spacing: 0.2em;
      white-space: nowrap;
    }
  }
}

.el-scrollbar {
  height: calc(100% - 116px);
  background: $menuBg;
}

.el-menu {
  width: 100% !important;
  padding: 22px 12px 0;
  border: 0;
}

.sidebar-note {
  position: absolute;
  right: 20px;
  bottom: 18px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 250, 240, 0.48);
  font-size: 11px;
  letter-spacing: 0.12em;

  span {
    width: 18px;
    height: 1px;
    background: $color-accent;
  }
}
</style>
