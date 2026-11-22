# 水果交易智能管理系统 - 前端

基于 Vue 3 + Vite 5 的响应式三端（用户端 / 商家端 / 平台端）前端工程。

## 技术栈

- Vue 3（`<script setup>` 组合式 API）
- Vite 5
- Vue Router 4（角色路由守卫）
- Pinia 2（状态管理）
- Axios（统一请求/响应拦截）
- ECharts 5（数据可视化）

## 启动方式

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

默认访问 `http://localhost:5173`。

### 3. 生产构建

```bash
npm run build
```

构建产物输出到 `frontend/dist/`。

### 4. 预览构建产物

```bash
npm run preview
```

## 代理配置

开发服务器在 `vite.config.js` 中配置了代理：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

所有以 `/api` 开头的请求会被转发到后端 `http://localhost:8080`。Axios 的 `baseURL` 统一设置为 `/api`。**启动前端前请确保后端服务已在 8080 端口运行。**

## 目录说明

```
frontend/
├── index.html                 # 入口 HTML
├── package.json               # 依赖与脚本
├── vite.config.js             # Vite 配置（含 /api 代理）
├── README.md
└── src/
    ├── main.js                # 应用入口
    ├── App.vue                # 全局布局（头部 + 侧边栏 + 路由出口）
    ├── router/index.js        # 路由表与角色守卫
    ├── store/index.js         # Pinia：用户会话、购物车数量、预警数
    ├── api/
    │   ├── index.js           # Axios 实例 + 拦截器（解包/鉴权/错误提示）
    │   ├── auth.js            # 认证接口（4）
    │   ├── user.js            # 公共 + 用户端接口（22）
    │   ├── merchant.js        # 商家端接口（13）
    │   └── admin.js           # 平台端接口（7）
    ├── utils/
    │   ├── enums.js           # 枚举文案映射（订单/售后/审核/预警状态）
    │   ├── format.js          # 金额、日期格式化
    │   └── request.js         # 错误码提示与消息组件
    ├── styles/global.css      # 全局样式（浅色主题、响应式）
    ├── components/            # 通用组件
    │   ├── AppHeader.vue / AppSidebar.vue
    │   ├── ProductCard.vue / CartItem.vue
    │   ├── OrderStatusTag.vue / WarningTag.vue / StatusTag.vue
    │   ├── EmptyState.vue / Pagination.vue / ConfirmDialog.vue
    └── views/
        ├── NotFound.vue
        ├── auth/               # LoginView / RegisterView
        ├── user/               # 首页、分类、详情、购物车、结算、订单、售后、个人中心（10）
        ├── merchant/           # 工作台、商品、库存、订单、售后、报表（6）
        └── admin/              # 工作台、商家审核、用户、投诉、总览（5）
```

## 接口封装

共封装 **46 个接口**，与 `docs/02-系统设计/API接口契约.md` 一一对应：

| 模块 | 文件 | 数量 |
|------|------|------|
| 认证 | api/auth.js | 4 |
| 公共 + 用户端 | api/user.js | 22 |
| 商家端 | api/merchant.js | 13 |
| 平台端 | api/admin.js | 7 |

Axios 拦截器行为：

- 请求拦截器注入 `Authorization: Bearer <token>`（Token 来自 localStorage）。
- 响应拦截器解包 `{ code, message, data, timestamp }`，`code === 0` 返回 `data`。
- `code ≠ 0` 按错误码表提示；收到 `40101 / 40102` 时清除本地 Token 并跳转 `/login`。

## 三端演示账号

> 演示账号为 `user01` / `merchant01` / `admin`，密码统一为 `123456`，**具体以后端种子数据为准**。登录页提供一键填充按钮。

| 角色 | 账号 | 密码 | 登录后首页 |
|------|------|------|-----------|
| 普通用户 USER | user01 | 123456 | `/home` |
| 入驻商家 MERCHANT | merchant01 | 123456 | `/merchant/dashboard` |
| 平台管理员 ADMIN | admin | 123456 | `/admin/dashboard` |

说明：商家账号需平台审核通过后（`audit_status=1`）方可正常经营；若后端种子数据未审核通过，请先在平台端「商家审核」中通过。

## 角色与权限

- 路由 `meta.role` 标识允许访问的角色，`router.beforeEach` 进行守卫：
  - 未登录访问受保护页面 → 跳转 `/login` 并记录 `redirect`。
  - 角色不匹配 → 重定向到对应角色首页。
- 数据隔离由后端保证：用户仅本人数据、商家仅本商家数据、管理员可访问全部。

## 状态文案约定

| 类型 | 映射 |
|------|------|
| 订单状态 | 0 待支付 / 1 待发货 / 2 运输中 / 3 已完成 / 4 已取消 / 5 售后中 |
| 售后状态 | 0 待审核 / 1 审核通过 / 2 处理中 / 3 已完成 / 4 已驳回 |
| 商家审核 | 0 待审核 / 1 审核通过 / 2 审核驳回 |
| 预警等级 | 0 正常 / 1 注意 / 2 警告 / 3 严重 |

金额统一以 `¥` 符号展示并保留两位小数；日期时间统一展示 `yyyy-MM-dd HH:mm:ss`。
