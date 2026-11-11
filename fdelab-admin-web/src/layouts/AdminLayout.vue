<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">FDELab</div>
      <el-menu router :default-active="activeMenu" background-color="#17241e" text-color="#d5e0d8" active-text-color="#9ddec0">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-menu-item index="/talents">
          <el-icon><Avatar /></el-icon>
          <span>人才</span>
        </el-menu-item>
        <el-menu-item index="/cases">
          <el-icon><Files /></el-icon>
          <span>案例</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>产品与服务</span>
        </el-menu-item>
        <el-menu-item index="/system/users">
          <el-icon><User /></el-icon>
          <span>用户</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <strong>{{ route.meta.title || 'FDELab' }}</strong>
        <el-dropdown @command="onCommand">
          <span class="user">{{ store.profile?.nickname || '账号' }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Avatar, Files, Goods, Odometer, User } from '@element-plus/icons-vue'
import { logout } from '../api/auth'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const activeMenu = computed(() => (route.path.startsWith('/cases') ? '/cases' : route.path))

async function onCommand(command) {
  if (command !== 'logout') return
  try {
    await logout()
  } catch {
    // 本地会话一并清掉，避免卡在失效 token 上。
  }
  store.clear()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: #17241e;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 22px;
  color: #f4f1ea;
  font-size: 20px;
  letter-spacing: 0.04em;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fffdf8;
  border-bottom: 1px solid var(--line);
}

.user {
  cursor: pointer;
  color: #3d4a43;
}

.main {
  background: #f4f1ea;
}
</style>
