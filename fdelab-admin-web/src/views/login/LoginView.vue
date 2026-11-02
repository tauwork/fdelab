<template>
  <div class="login">
    <section class="panel">
      <p class="mark">FDELab</p>
      <h1>管理后台</h1>
      <p class="lead">用户、内容和后续业务模块从这里进入。</p>
    </section>
    <section class="form-wrap">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="submit">登录</el-button>
        <p class="hint">本地初始账号 admin / admin123</p>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123',
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.login(form)
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
}

.panel {
  padding: 72px 64px;
  background:
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.08), transparent 32%),
    #17241e;
  color: #f4f1ea;
}

.mark {
  margin: 0 0 48px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #c4a574;
}

h1 {
  margin: 0;
  font-family: "Iowan Old Style", Palatino, "Songti SC", serif;
  font-size: 56px;
  font-weight: 500;
}

.lead {
  max-width: 360px;
  margin-top: 18px;
  line-height: 1.7;
  color: #d5e0d8;
}

.form-wrap {
  display: flex;
  align-items: center;
  padding: 48px;
  background: #f4f1ea;
}

.el-form {
  width: min(380px, 100%);
}

.submit {
  width: 100%;
}

.hint {
  margin: 16px 0 0;
  color: #6d7872;
  font-size: 13px;
}

@media (max-width: 860px) {
  .login {
    grid-template-columns: 1fr;
  }

  .panel,
  .form-wrap {
    padding: 32px 24px;
  }

  h1 {
    font-size: 40px;
  }
}
</style>
