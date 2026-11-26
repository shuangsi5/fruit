# 《软件工程与 UML》大作业 —— 个人交付包

**负责人：朱国昱**
**负责数据流：DF-1 用户交易主数据流**
**系统：水果交易智能管理系统（三端：用户端 / 商家端 / 平台管理端）**

---

## 一、本交付包包含什么

本目录是本人（朱国昱）在大作业中承担任务的完整产出，共两部分：

1. **`01-UML图/`**：DF-1 的五类 UML 图，均为 **draw.io 原生 `.drawio` 格式**，可直接用 draw.io 打开、编辑、导出。
2. **`02-代码/`**：与上述五张图对应的可执行代码（后端 Spring Boot + 前端 Vue 3 + 数据库脚本）。

本人负责的 **DF-1 用户交易主数据流** 覆盖的业务链路为：

> 用户登录 → 浏览商品/分类/详情 → 加入购物车 → 提交订单 → 支付 → 查看订单状态与物流 → 申请售后
> 涉及的智能特性接口：相关商品推荐（下单/详情页）、库存校验与预占。

---

## 二、目录结构

```
大作业/
├── 01-UML图/
│   ├── DF1-用例图-用户交易.drawio          ← 功能模型
│   ├── DF1-活动图-下单支付.drawio          ← 功能模型（流程分支）
│   ├── DF1-顺序图-下单支付.drawio          ← 行为模型（对象交互）
│   ├── DF1-状态图-订单.drawio              ← 行为模型（状态流转）
│   ├── DF1-数据流图-用户交易.drawio        ← 数据模型（数据流向）
│   └── 预览PNG/                            ← 五张图的 PNG 预览，便于快速查看
│       ├── DF1-用例图-用户交易.png
│       ├── DF1-活动图-下单支付.png
│       ├── DF1-顺序图-下单支付.png
│       ├── DF1-状态图-订单.png
│       └── DF1-数据流图-用户交易.png
│
├── 02-代码/
│   ├── 后端/                               ← Spring Boot 工程（pom.xml + src/）
│   │   ├── pom.xml
│   │   └── src/main/java/com/fruittrade/...
│   ├── 前端/                               ← Vue 3 + Vite 工程
│   │   ├── package.json
│   │   ├── vite.config.js
│   │   ├── index.html
│   │   └── src/...
│   └── 数据库/
│       └── init.sql                        ← 17 张表建表语句 + 种子数据
│
└── README.md                               ← 本文件
```

> 说明：`02-代码/` 中放入的是**完整可运行的工程源码**，而不是仅裁剪 DF-1 的零星文件。原因是后端各层（Controller → Service → Mapper → Entity）与公共模块（安全鉴权、统一返回、异常处理、JWT）之间存在编译依赖，单独抽取部分文件无法直接编译运行。DF-1 本人负责的核心文件已在 **第五章** 逐一标出。

---

## 三、五类 UML 图说明与打开方式

| 序号 | 图名 | 文件 | 建模内容 |
|---|---|---|---|
| 1 | 用例图 | `DF1-用例图-用户交易.drawio` | 参与者（用户、商家）、系统边界、登录/浏览/购物车/下单支付/订单查询/售后等用例，以及 `include` / `extend` 关系 |
| 2 | 活动图 | `DF1-活动图-下单支付.drawio` | 从提交订单到支付完成的主流程，含库存不足、支付失败、超时取消、释放库存等分支与合流 |
| 3 | 顺序图 | `DF1-顺序图-下单支付.drawio` | 用户、前端页面、认证网关、订单服务、库存服务、支付模拟、数据库 7 个对象间 17 条消息的时间顺序 |
| 4 | 状态图 | `DF1-状态图-订单.drawio` | 订单 6 个状态（待支付/待发货/运输中/已完成/已取消/售后中）及创建、支付、发货、确认收货、售后等转换 |
| 5 | 数据流图 | `DF1-数据流图-用户交易.drawio` | 外部实体（用户/商家/支付系统）、6 个处理过程（P1~P6）、7 个数据存储之间的数据流向 |

### 用 draw.io 打开 `.drawio` 文件

- **方式一（推荐 · 桌面版）**：安装 draw.io Desktop，菜单 `文件(File)` → `打开(Open from)` → `设备(Device)` → 选择对应 `.drawio` 文件。打开后可直接双击图形修改文字、拖动调整布局。
- **方式二（VS Code 插件）**：安装 VS Code 扩展 `Draw.io Integration`，右键 `.drawio` 文件 → `Open with Draw.io`。
- **方式三（网页版，无需安装）**：访问 `app.diagrams.net` → `File` → `Open from` → `Device` → 选择文件；或直接从本目录拖拽文件到网页中。
- **只看效果**：直接打开 `预览PNG/` 下对应的 PNG 图片。

> 所有图均为标准 `mxGraphModel` XML，draw.io 全版本兼容。PNG 预览由 draw.io 自身导出，证明文件可正常解析。

---

## 四、五张图 ↔ 代码 对应关系（重点）

这张表可直接用于课程汇报，说明每张图在代码中落在哪些文件上：

| 图 | 图中关键元素 | 对应代码文件（`02-代码/` 下的相对路径） |
|---|---|---|
| **用例图** | 登录、浏览商品、购物车、下单支付、订单查询、申请售后 | 后端 `后端/src/main/java/com/fruittrade/controller/`：`AuthController.java`、`ProductController.java`、`CartController.java`、`OrderController.java`、`UserFeatureController.java`；前端 `前端/src/views/user/`、`前端/src/views/auth/` 下各页面 |
| **活动图** | 提交订单 → 校验库存 → 锁库存 → 支付 → 发货/取消分支 | `后端/src/main/java/com/fruittrade/service/impl/OrderServiceImpl.java` 的 `create()`、`pay()`、`cancel()`、`lockStock()`、`deductLockedStock()`、`releaseStock()` 方法 |
| **顺序图** | 7 个对象、17 条请求/返回消息 | `后端/.../controller/OrderController.java`（create/pay）、`.../security/JwtAuthenticationFilter.java`（鉴权）、`.../service/impl/OrderServiceImpl.java`（业务）、`.../service/impl/CartServiceImpl.java`、`.../domain/entity/Inventory.java`（库存） |
| **状态图** | 0~5 六个状态及合法流转 | `后端/src/main/java/com/fruittrade/engine/OrderStateMachine.java`（`canTransition()` / `requireTransition()` / `statusText()`）；状态变更在 `OrderServiceImpl.java` 中执行 |
| **数据流图** | P1 认证浏览、P2 购物车、P3 订单、P4 支付、P5 库存、P6 售后；7 个数据存储 | 处理过程对应上述各 Controller/Service；数据存储对应 `数据库/init.sql` 中的 `sys_user`、`product`、`cart`、`orders`、`order_item`、`payment_record`、`inventory`、`after_sale` 等表 |

### 状态图的状态编码（与代码常量一致）

| 状态 | 编码 | 代码常量 |
|---|---|---|
| 待支付 | 0 | `OrderStateMachine.PENDING_PAYMENT` |
| 待发货 | 1 | `OrderStateMachine.PENDING_SHIPMENT` |
| 运输中 | 2 | `OrderStateMachine.SHIPPING` |
| 已完成 | 3 | `OrderStateMachine.COMPLETED` |
| 已取消 | 4 | `OrderStateMachine.CANCELLED` |
| 售后中 | 5 | `OrderStateMachine.AFTER_SALE` |

---

## 五、DF-1 本人负责的核心代码文件清单

### 5.1 后端（`02-代码/后端/src/main/java/com/fruittrade/`）

| 层次 | 文件 | 作用 |
|---|---|---|
| Controller | `controller/AuthController.java` | 注册、登录、当前用户、登出 |
| Controller | `controller/ProductController.java` | 分类列表、商品列表/详情、相关推荐 |
| Controller | `controller/CartController.java` | 购物车增删查改 |
| Controller | `controller/OrderController.java` | 下单、支付、订单列表/详情、取消、确认收货、物流 |
| Controller | `controller/AddressController.java` | 收货地址管理 |
| Controller | `controller/UserFeatureController.java` | 售后申请、投诉提交、推荐结果 |
| Service | `service/impl/AuthServiceImpl.java` | 认证业务逻辑、JWT 签发 |
| Service | `service/impl/CartServiceImpl.java` | 购物车业务逻辑 |
| Service | `service/impl/OrderServiceImpl.java` | **订单核心**：价格重算、库存预占、状态流转、模拟支付 |
| Service | `service/impl/AfterSaleServiceImpl.java` | 售后申请与处理 |
| Service | `service/impl/ProductServiceImpl.java` | 商品浏览与详情 |
| Service | `service/impl/AddressServiceImpl.java` | 地址管理 |
| Service | `service/impl/RecommendationServiceImpl.java` | 历史购买推荐（智能特性） |
| 引擎 | `engine/OrderStateMachine.java` | 订单状态机（状态图的直接实现） |
| 引擎 | `engine/HybridRecommendationEngine.java` | 混合推荐算法（频次 + 协同过滤 + 热销） |
| 安全 | `security/JwtAuthenticationFilter.java` 等 | Token 校验（顺序图“认证网关”） |
| 公共 | `common/Result.java` 等 | 统一响应、错误码、异常处理 |
| 领域 | `domain/entity/`、`domain/dto/`、`domain/vo/`、`mapper/` | 实体、传输对象、视图对象、数据访问 |

### 5.2 前端（`02-代码/前端/src/`）

| 目录 | 文件 | 作用 |
|---|---|---|
| `api/` | `auth.js`、`user.js`、`index.js` | 认证与用户端接口封装 |
| `views/auth/` | `LoginView.vue`、`RegisterView.vue` | 登录、注册页 |
| `views/user/` | `HomeView.vue`、`CategoryView.vue`、`ProductDetailView.vue` | 首页、分类、商品详情（浏览） |
| `views/user/` | `CartView.vue`、`CheckoutView.vue` | 购物车、订单结算（下单） |
| `views/user/` | `OrderListView.vue`、`OrderDetailView.vue` | 订单列表、订单详情与物流（状态查看） |
| `views/user/` | `AfterSaleApplyView.vue`、`AfterSaleListView.vue` | 售后申请与列表 |
| `views/user/` | `UserProfileView.vue` | 个人中心 |
| `components/` | `ProductCard.vue`、`CartItem.vue`、`OrderStatusTag.vue` 等 | 通用组件 |
| `router/`、`store/`、`utils/` | `index.js`、`request.js`、`enums.js` 等 | 路由、状态管理、请求封装、枚举 |

### 5.3 数据库（`02-代码/数据库/init.sql`）

DF-1 直接读写的表：`sys_user`、`address`、`product`、`category`、`cart`、`orders`、`order_item`、`payment_record`、`inventory`、`inventory_log`、`after_sale`、`logistics`、`recommendation_log`。

---

## 六、如何运行代码

### 6.1 环境要求
- JDK 17+、Maven 3.6+、MySQL 8.0+、Node.js 18+

### 6.2 初始化数据库
```bash
# 登录 MySQL 后执行
CREATE DATABASE IF NOT EXISTS fruit_trade_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SOURCE C:/Users/朱国昱/Desktop/软件工程与UML/大作业/02-代码/数据库/init.sql;
```
数据库连接配置在 `02-代码/后端/src/main/resources/application.yml`，默认 `root/root`、`localhost:3306`，请按本机情况修改。

### 6.3 启动后端
```bash
cd 02-代码/后端
mvn spring-boot:run
# 后端端口 8080，接口统一前缀 /api
```

### 6.4 启动前端
```bash
cd 02-代码/前端
npm install
npm run dev
# 浏览器访问终端提示的地址（默认 http://localhost:5173）
```

### 6.5 演示账号（`02-代码/数据库/init.sql` 种子数据为准）

| 角色 | 账号 | 密码 |
|---|---|---|
| 普通用户 USER | `user01`（另有 user02、user03） | `123456` |
| 入驻商家 MERCHANT | `merchant01`（另有 merchant02） | `123456` |
| 平台管理员 ADMIN | `admin` | `123456` |

> 密码统一为 `123456`，数据库中以 BCrypt 加密存储（以 `init.sql` 种子数据为准）。

---

## 七、可直接写入个人报告的内容说明

> 本人（朱国昱）负责 **DF-1 用户交易主数据流** 的分析、建模与实现。在需求分析阶段，绘制了**用例图**梳理用户与商家在交易流程中的功能边界，绘制**活动图**描述下单支付的主流程与库存不足、支付失败等异常分支；在系统设计阶段，绘制**顺序图**刻画前端页面、认证网关、订单服务、库存服务与数据库之间的消息交互，绘制**状态图**定义订单从待支付到已完成/已取消/售后中的六态流转约束，绘制**数据流图**明确外部实体、处理过程与数据存储之间的数据流向。在编码实现阶段，本人承担了认证、商品浏览、购物车、订单、支付、售后相关的后端 Controller/Service 与前端用户端页面，并实现了订单状态机（`OrderStateMachine`）与混合推荐算法。上述五类图均使用 draw.io 绘制并以 `.drawio` 原生格式保存，可直接打开编辑；图与代码文件的对应关系已在第四章列表中说明。

---

*本 README 同时作为个人交付清单与汇报索引使用。*
