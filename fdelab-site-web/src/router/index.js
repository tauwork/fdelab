import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import CaseListView from '../views/CaseListView.vue'
import CaseDetailView from '../views/CaseDetailView.vue'
import TalentListView from '../views/TalentListView.vue'
import TalentListViewLegacy from '../views/TalentListViewLegacy.vue'
import TalentPage from '../views/TalentPage.vue'
import TalentProfileView from '../views/TalentProfileView.vue'
import ProductServiceView from '../views/ProductServiceView.vue'
import ProductItemView from '../views/ProductItemView.vue'
import ProductFormView from '../views/ProductFormView.vue'
import UnderConstruction from '../views/UnderConstruction.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/articles', name: 'cases', component: CaseListView, meta: { title: '案例库' } },
    { path: '/products', name: 'products', component: ProductServiceView, meta: { title: '产品与服务' } },
    { path: '/products/item/:code', name: 'productItem', component: ProductItemView, meta: { title: '产品与服务' } },
    { path: '/products/form/:code', name: 'productForm', component: ProductFormView, meta: { title: '产品与服务' } },
    { path: '/sites', name: 'sites', component: UnderConstruction, meta: { title: '网点' } },
    { path: '/vendors', name: 'vendors', component: UnderConstruction, meta: { title: '供应商' } },
    { path: '/caliber', name: 'caliber', component: UnderConstruction, meta: { title: '数据口径说明' } },
    { path: '/articles/:id', name: 'article', component: CaseDetailView },
    { path: '/talents', name: 'talents', component: TalentListView },
    { path: '/talents/:slug', name: 'talentDetail', component: TalentProfileView },
    // 仅用于新旧列表页并排对比，不进导航、不对外暴露入口
    { path: '/talents-legacy', name: 'talentsLegacy', component: TalentListViewLegacy },
    { path: '/t/:slug', name: 'talent', component: TalentPage },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0 }
  },
})

export default router
