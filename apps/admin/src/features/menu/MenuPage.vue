<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Category, Combo, MenuItem } from '@leisure-brew/api-contract'
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import { adminApi } from '@/services/admin-api'

const activeTab = ref('drinks')
const loading = ref(false)
const drinks = ref<MenuItem[]>([])
const combos = ref<Combo[]>([])
const categories = ref<Category[]>([])
const query = reactive({ page: 1, pageSize: 50, name: '' })
const drinkDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const saving = ref(false)
const drinkForm = reactive({
  id: undefined as number | undefined,
  name: '',
  categoryId: undefined as number | undefined,
  price: 0,
  image: '',
  description: '',
  status: 0 as MenuItem['status'],
  specsText: '',
})
const categoryForm = reactive({
  id: undefined as number | undefined,
  type: 1 as Category['type'],
  name: '',
  sort: 1,
})
const comboDialogVisible = ref(false)
const comboForm = reactive({
  name: '',
  categoryId: undefined as number | undefined,
  price: 0,
  image: '',
  description: '',
  drinkIds: [] as number[],
})

async function load() {
  loading.value = true
  try {
    const [drinkResponse, comboResponse, categoryResponse] = await Promise.all([
      adminApi.drinks(query),
      adminApi.combos(query),
      adminApi.categories(query),
    ])
    drinks.value = drinkResponse.data.data.records
    combos.value = comboResponse.data.data.records
    categories.value = categoryResponse.data.data.records
  } finally {
    loading.value = false
  }
}

async function toggleDrink(item: MenuItem) {
  await adminApi.setDrinkStatus(item.id, item.status ? 0 : 1)
  ElMessage.success(item.status ? '饮品已下架' : '饮品已上架')
  load()
}
async function toggleCombo(item: Combo) {
  await adminApi.setComboStatus(item.id, item.status ? 0 : 1)
  ElMessage.success(item.status ? '套餐已下架' : '套餐已上架')
  load()
}
function toggleMenuItem(item: MenuItem | Combo) {
  return activeTab.value === 'drinks' ? toggleDrink(item as MenuItem) : toggleCombo(item as Combo)
}
function categoryName(id: number) {
  return categories.value.find((item) => item.id === id)?.name || '未分组'
}
function openCreate() {
  if (activeTab.value === 'categories') {
    Object.assign(categoryForm, { id: undefined, type: 1, name: '', sort: categories.value.length + 1 })
    categoryDialogVisible.value = true
    return
  }
  if (activeTab.value === 'drinks') {
    Object.assign(drinkForm, {
      id: undefined,
      name: '',
      categoryId: categories.value.find((item) => item.type === 1)?.id,
      price: 0,
      image: '',
      description: '',
      status: 0,
      specsText: '',
    })
    drinkDialogVisible.value = true
    return
  }
  Object.assign(comboForm, {
    name: '',
    categoryId: categories.value.find((item) => item.type === 2)?.id,
    price: 0,
    image: '',
    description: '',
    drinkIds: [],
  })
  comboDialogVisible.value = true
}
async function editDrink(item: MenuItem) {
  const response = await adminApi.drink(item.id)
  const drink = response.data.data
  Object.assign(drinkForm, {
    ...drink,
    specsText: (drink.specs || [])
      .map((spec) => `${spec.name}：${readSpecOptions(spec.value).join('、')}`)
      .join('\n'),
  })
  drinkDialogVisible.value = true
}
function readSpecOptions(value: string) {
  try {
    const options = JSON.parse(value) as unknown
    return Array.isArray(options) ? options.map(String) : []
  } catch {
    return []
  }
}
function parseSpecs(value: string) {
  return value
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [name, options = ''] = line.split(/[：:]/, 2)
      return {
        name: name.trim(),
        value: JSON.stringify(
          options
            .split(/[、,，]/)
            .map((item) => item.trim())
            .filter(Boolean),
        ),
      }
    })
    .filter((spec) => spec.name && spec.value !== '[]')
}
async function saveDrink() {
  if (!drinkForm.name || !drinkForm.categoryId || drinkForm.price <= 0) {
    ElMessage.warning('请填写饮品名称、分组和正确价格')
    return
  }
  saving.value = true
  try {
    await adminApi.saveDrink({ ...drinkForm, specs: parseSpecs(drinkForm.specsText) })
    drinkDialogVisible.value = false
    ElMessage.success(drinkForm.id ? '饮品信息已更新' : '新饮品已保存，默认保持下架')
    await load()
  } finally {
    saving.value = false
  }
}
function editMenuItem(item: MenuItem | Combo) {
  if (activeTab.value === 'drinks') return editDrink(item as MenuItem)
}
async function uploadImage(options: UploadRequestOptions) {
  const response = await adminApi.uploadImage(options.file)
  drinkForm.image = response.data.data
  options.onSuccess(response.data)
}
async function uploadComboImage(options: UploadRequestOptions) {
  const response = await adminApi.uploadImage(options.file)
  comboForm.image = response.data.data
  options.onSuccess(response.data)
}
async function removeDrink(item: MenuItem) {
  await ElMessageBox.confirm(`确定删除“${item.name}”吗？`, '删除饮品', { type: 'warning' })
  await adminApi.deleteDrink(item.id)
  ElMessage.success('饮品已删除')
  await load()
}
function removeMenuItem(item: MenuItem | Combo) {
  return activeTab.value === 'drinks' ? removeDrink(item as MenuItem) : removeCombo(item as Combo)
}
async function saveCombo() {
  if (!comboForm.name || !comboForm.categoryId || comboForm.price <= 0 || !comboForm.drinkIds.length) {
    ElMessage.warning('请填写套餐名称、分组、价格，并选择至少一杯饮品')
    return
  }
  saving.value = true
  try {
    await adminApi.saveCombo({
      ...comboForm,
      status: 0,
      comboDrinks: comboForm.drinkIds.map((drinkId) => ({ drinkId, copies: 1 })),
    })
    comboDialogVisible.value = false
    ElMessage.success('新套餐已保存，默认保持下架')
    await load()
  } finally {
    saving.value = false
  }
}
async function removeCombo(item: Combo) {
  await ElMessageBox.confirm(`确定删除“${item.name}”吗？`, '删除套餐', { type: 'warning' })
  await adminApi.deleteCombo(item.id)
  ElMessage.success('套餐已删除')
  await load()
}
function editCategory(category: Category) {
  Object.assign(categoryForm, category)
  categoryDialogVisible.value = true
}
async function saveCategory() {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('请填写分组名称')
    return
  }
  saving.value = true
  try {
    await adminApi.saveCategory(categoryForm)
    categoryDialogVisible.value = false
    ElMessage.success(categoryForm.id ? '分组已更新' : '分组已创建')
    await load()
  } finally {
    saving.value = false
  }
}
async function toggleCategory(category: Category) {
  await adminApi.setCategoryStatus(category.id, category.status ? 0 : 1)
  await load()
}
async function removeCategory(category: Category) {
  await ElMessageBox.confirm('只有未关联饮品或套餐的分组可以删除。', `删除“${category.name}”？`, {
    type: 'warning',
  })
  await adminApi.deleteCategory(category.id)
  ElMessage.success('分组已删除')
  await load()
}
onMounted(load)
</script>

<template>
  <div>
    <PageHeader
      eyebrow="MENU & SEASONS"
      title="菜单管理"
      description="按茶香和时令整理菜单，不让上新淹没日常好喝。"
    >
      <el-button type="primary" @click="openCreate">
        {{ activeTab === 'categories' ? '新建分组' : activeTab === 'combos' ? '新建套餐' : '上新饮品' }}
      </el-button>
    </PageHeader>
    <section class="paper-card menu-toolbar">
      <el-tabs v-model="activeTab"
        ><el-tab-pane label="饮品" name="drinks" /><el-tab-pane label="搭配套餐" name="combos" /><el-tab-pane
          label="菜单分组"
          name="categories" /></el-tabs
      ><el-input v-model.trim="query.name" clearable placeholder="查找名称" @keyup.enter="load" /><el-button
        @click="load"
        >查询</el-button
      >
    </section>
    <section v-loading="loading" class="menu-grid" v-if="activeTab !== 'categories'">
      <article v-for="item in activeTab === 'drinks' ? drinks : combos" :key="item.id" class="menu-card">
        <div class="menu-card__image">
          <img v-if="item.image" :src="item.image" :alt="item.name" /><span v-else>茶</span
          ><i :class="{ off: !item.status }">{{ item.status ? '售卖中' : '已下架' }}</i>
        </div>
        <div class="menu-card__body">
          <small>{{ categoryName(item.categoryId) }}</small>
          <h3>{{ item.name }}</h3>
          <p>{{ item.description || '一杯认真做好的日常茶饮。' }}</p>
          <div>
            <strong>¥{{ Number(item.price).toFixed(2) }}</strong
            ><el-switch :model-value="Boolean(item.status)" @change="toggleMenuItem(item)" />
          </div>
          <div v-if="activeTab === 'drinks'" class="menu-card__actions">
            <el-button text @click="editMenuItem(item)">编辑</el-button>
            <el-button text type="danger" :disabled="Boolean(item.status)" @click="removeMenuItem(item)">
              删除
            </el-button>
          </div>
          <div v-else class="menu-card__actions">
            <el-button text type="danger" :disabled="Boolean(item.status)" @click="removeMenuItem(item)">
              删除
            </el-button>
          </div>
        </div>
      </article>
      <EmptyState
        v-if="!(activeTab === 'drinks' ? drinks : combos).length"
        title="菜单里还没有这一项"
        description="新的季节风味可以从这里开始。"
      />
    </section>
    <section v-else class="paper-card category-list">
      <div v-for="category in categories" :key="category.id">
        <span>{{ String(category.sort).padStart(2, '0') }}</span
        ><strong>{{ category.name }}</strong
        ><small>{{ category.type === 1 ? '饮品分组' : '套餐分组' }}</small
        ><el-switch :model-value="Boolean(category.status)" @change="toggleCategory(category)" />
        <div class="category-list__actions">
          <el-button text @click="editCategory(category)">编辑</el-button>
          <el-button
            text
            type="danger"
            :disabled="Boolean(category.status)"
            @click="removeCategory(category)"
          >
            删除
          </el-button>
        </div>
      </div>
    </section>
    <el-dialog v-model="drinkDialogVisible" :title="drinkForm.id ? '编辑饮品' : '上新饮品'" width="620">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="饮品名称"
            ><el-input v-model.trim="drinkForm.name" maxlength="32"
          /></el-form-item>
          <el-form-item label="菜单分组">
            <el-select v-model="drinkForm.categoryId" placeholder="选择饮品分组">
              <el-option
                v-for="item in categories.filter((entry) => entry.type === 1)"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="售价"
            ><el-input-number v-model="drinkForm.price" :min="0.01" :precision="2"
          /></el-form-item>
          <el-form-item label="图片">
            <el-upload
              :show-file-list="false"
              accept="image/png,image/jpeg,image/webp"
              :http-request="uploadImage"
            >
              <el-button>{{ drinkForm.image ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </el-form-item>
        </div>
        <el-form-item label="图片地址"
          ><el-input v-model.trim="drinkForm.image" placeholder="也可以粘贴图片 URL"
        /></el-form-item>
        <el-form-item label="一句介绍"
          ><el-input
            v-model.trim="drinkForm.description"
            type="textarea"
            :rows="2"
            maxlength="120"
            show-word-limit
        /></el-form-item>
        <el-form-item label="可选规格">
          <el-input
            v-model="drinkForm.specsText"
            type="textarea"
            :rows="4"
            placeholder="每行一组，例如：\n温度：少冰、正常冰\n甜度：三分糖、五分糖"
          />
        </el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="drinkDialogVisible = false">取消</el-button
        ><el-button type="primary" :loading="saving" @click="saveDrink">保存</el-button></template
      >
    </el-dialog>
    <el-dialog
      v-model="categoryDialogVisible"
      :title="categoryForm.id ? '编辑菜单分组' : '新建菜单分组'"
      width="480"
    >
      <el-form label-position="top">
        <el-form-item label="分组类型"
          ><el-radio-group v-model="categoryForm.type" :disabled="Boolean(categoryForm.id)"
            ><el-radio :value="1">饮品</el-radio><el-radio :value="2">套餐</el-radio></el-radio-group
          ></el-form-item
        >
        <el-form-item label="分组名称"
          ><el-input v-model.trim="categoryForm.name" maxlength="20"
        /></el-form-item>
        <el-form-item label="排序"
          ><el-input-number v-model="categoryForm.sort" :min="1" :max="999"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="categoryDialogVisible = false">取消</el-button
        ><el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button></template
      >
    </el-dialog>
    <el-dialog v-model="comboDialogVisible" title="新建搭配套餐" width="680">
      <el-form label-position="top">
        <div class="form-grid">
          <el-form-item label="套餐名称"
            ><el-input v-model.trim="comboForm.name" maxlength="32"
          /></el-form-item>
          <el-form-item label="套餐分组">
            <el-select v-model="comboForm.categoryId" placeholder="选择套餐分组">
              <el-option
                v-for="item in categories.filter((entry) => entry.type === 2)"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="套餐售价"
            ><el-input-number v-model="comboForm.price" :min="0.01" :precision="2"
          /></el-form-item>
          <el-form-item label="图片">
            <el-upload
              :show-file-list="false"
              accept="image/png,image/jpeg,image/webp"
              :http-request="uploadComboImage"
              ><el-button>{{ comboForm.image ? '更换图片' : '上传图片' }}</el-button></el-upload
            >
          </el-form-item>
        </div>
        <el-form-item label="图片地址"
          ><el-input v-model.trim="comboForm.image" placeholder="也可以粘贴图片 URL"
        /></el-form-item>
        <el-form-item label="一句介绍"
          ><el-input
            v-model.trim="comboForm.description"
            type="textarea"
            :rows="2"
            maxlength="120"
            show-word-limit
        /></el-form-item>
        <el-form-item label="包含饮品">
          <el-checkbox-group v-model="comboForm.drinkIds" class="combo-options">
            <el-checkbox v-for="drink in drinks" :key="drink.id" :value="drink.id" :disabled="!drink.status"
              >{{ drink.name }} · ¥{{ Number(drink.price).toFixed(2) }}</el-checkbox
            >
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="comboDialogVisible = false">取消</el-button
        ><el-button type="primary" :loading="saving" @click="saveCombo">保存套餐</el-button></template
      >
    </el-dialog>
  </div>
</template>
