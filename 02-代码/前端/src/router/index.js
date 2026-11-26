import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store'

const routes = [
  {
    path: '/',
    redirect: () => {
      const user = useUserStore()
      if (!user.token) return '/login'
      if (user.role === 'MERCHANT') return '/merchant/dashboard'
      if (user.role === 'ADMIN') return '/admin/dashboard'
      return '/home'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/RegisterView.vue'),
    meta: { public: true, title: '注册' }
  },

  // ============ 用户端 ============
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/user/HomeView.vue'),
    meta: { role: 'USER', title: '首页' }
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('../views/user/CategoryView.vue'),
    meta: { role: 'USER', title: '商品分类' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/user/ProductDetailView.vue'),
    meta: { role: 'USER', title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/user/CartView.vue'),
    meta: { role: 'USER', title: '购物车' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('../views/user/CheckoutView.vue'),
    meta: { role: 'USER', title: '确认下单' }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/user/OrderListView.vue'),
    meta: { role: 'USER', title: '我的订单' }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('../views/user/OrderDetailView.vue'),
    meta: { role: 'USER', title: '订单详情' }
  },
  {
    path: '/after-sale/apply/:orderId',
    name: 'AfterSaleApply',
    component: () => import('../views/user/AfterSaleApplyView.vue'),
    meta: { role: 'USER', title: '申请售后' }
  },
  {
    path: '/after-sales',
    name: 'AfterSaleList',
    component: () => import('../views/user/AfterSaleListView.vue'),
    meta: { role: 'USER', title: '我的售后' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/user/UserProfileView.vue'),
    meta: { role: 'USER', title: '个人中心' }
  },

  // ============ 商家端 ============
  {
    path: '/merchant/dashboard',
    name: 'MerchantDashboard',
    component: () => import('../views/merchant/MerchantDashboard.vue'),
    meta: { role: 'MERCHANT', title: '商家工作台' }
  },
  {
    path: '/merchant/products',
    name: 'MerchantProducts',
    component: () => import('../views/merchant/MerchantProductsView.vue'),
    meta: { role: 'MERCHANT', title: '商品管理' }
  },
  {
    path: '/merchant/stock',
    name: 'MerchantStock',
    component: () => import('../views/merchant/MerchantStockView.vue'),
    meta: { role: 'MERCHANT', title: '库存与预警' }
  },
  {
    path: '/merchant/orders',
    name: 'MerchantOrders',
    component: () => import('../views/merchant/MerchantOrdersView.vue'),
    meta: { role: 'MERCHANT', title: '订单管理' }
  },
  {
    path: '/merchant/after-sales',
    name: 'MerchantAfterSales',
    component: () => import('../views/merchant/MerchantAfterSaleView.vue'),
    meta: { role: 'MERCHANT', title: '售后处理' }
  },
  {
    path: '/merchant/reports',
    name: 'MerchantReports',
    component: () => import('../views/merchant/MerchantReportsView.vue'),
    meta: { role: 'MERCHANT', title: '销售报表' }
  },

  // ============ 平台端 ============
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { role: 'ADMIN', title: '平台工作台' }
  },
  {
    path: '/admin/merchants',
    name: 'MerchantAudit',
    component: () => import('../views/admin/MerchantAuditView.vue'),
    meta: { role: 'ADMIN', title: '商家审核' }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/admin/AdminUserView.vue'),
    meta: { role: 'ADMIN', title: '用户管理' }
  },
  {
    path: '/admin/complaints',
    name: 'Complaints',
    component: () => import('../views/admin/ComplaintView.vue'),
    meta: { role: 'ADMIN', title: '投诉处理' }
  },
  {
    path: '/admin/overview',
    name: 'AdminOverview',
    component: () => import('../views/admin/AdminOverviewView.vue'),
    meta: { role: 'ADMIN', title: '数据总览' }
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 登录与角色守卫
router.beforeEach((to, from, next) => {
  const user = useUserStore()
  if (!user.token) user.restore()

  document.title = to.meta.title ? to.meta.title + ' - 水果交易智能管理系统' : '水果交易智能管理系统'

  if (to.meta.public) {
    next()
    return
  }
  if (!user.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  const needRole = to.meta.role
  if (needRole && user.role !== needRole) {
    // 角色不符，回到各自首页
    if (user.role === 'MERCHANT') next('/merchant/dashboard')
    else if (user.role === 'ADMIN') next('/admin/dashboard')
    else next('/home')
    return
  }
  next()
})

export default router
