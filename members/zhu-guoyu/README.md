# 朱国昱 — 个人贡献说明

> 水果交易智能管理系统（软件工程课程大作业）
> 负责数据流：**DF-1 用户交易主数据流**
> 成员目录：`members/zhu-guoyu/`

## 1. 我负责的内容

按课程"每人负责一条完整端到端数据流"的分工，我负责 **DF-1 用户交易主数据流**，覆盖从
「浏览商品 → 加入购物车 → 下单 → 支付 → 查看订单状态 → 申请售后」的完整链路。

本目录集中存放我个人的建模产物：

```
members/zhu-guoyu/
├── README.md                         ← 本说明
└── 01-UML图/
    ├── DF1-用例图-用户交易.drawio
    ├── DF1-活动图-下单支付.drawio
    ├── DF1-顺序图-下单支付.drawio
    ├── DF1-状态图-订单.drawio
    ├── DF1-数据流图-用户交易.drawio
    └── 预览PNG/                       ← 由 draw.io 导出的 5 张 PNG 预览
```

## 2. 五类 UML 图说明

| 图 | 文件 | 主要内容 |
|---|---|---|
| 用例图 | `DF1-用例图-用户交易.drawio` | 用户/商家参与者、系统边界、浏览商品/购物车/下单支付/查订单/售后等 11 个用例及 include/extend 关系 |
| 活动图 | `DF1-活动图-下单支付.drawio` | 下单主流程，含库存不足、支付失败、取消订单、释放库存等分支 |
| 顺序图 | `DF1-顺序图-下单支付.drawio` | 用户/前端/认证网关/订单服务/库存服务/支付模拟/数据库 7 个对象，17 条消息 |
| 状态图 | `DF1-状态图-订单.drawio` | 订单 6 个状态及创建/支付/超时取消/发货/确认收货/售后等转换 |
| 数据流图 | `DF1-数据流图-用户交易.drawio` | 外部实体、P1~P6 处理过程、7 个数据存储之间的数据流 |

- 订单状态编码：`0` 待支付 / `1` 待发货 / `2` 运输中 / `3` 已完成 / `4` 已取消 / `5` 售后中。
- 五类图均为 **draw.io 原生 `.drawio` 格式**，可直接打开编辑。

### 如何打开 `.drawio` 图

1. draw.io Desktop：菜单「文件 → 打开 → 设备」，选择 `.drawio` 文件；
2. VS Code：安装 Draw.io Integration 插件后直接双击打开；
3. 在线：访问 <https://app.diagrams.net>，选择「打开已有的图」。

`预览PNG/` 下是与五张图一一对应的 PNG，便于在报告/文档中直接引用。

## 3. 图 ↔ 代码对应关系

DF-1 的建模产物与仓库中的实际代码一一对应（代码位于仓库标准目录，非本目录副本）：

| 图 | 对应代码（仓库路径） |
|---|---|
| 用例图 | `backend/src/main/java/com/fruittrade/controller/` 下的 `AuthController`、`CartController`、`OrderController`、`ProductController`、`UserFeatureController`、`AddressController`；`frontend/src/views/user/` 用户端页面 |
| 活动图 | `backend/src/main/java/com/fruittrade/service/impl/OrderServiceImpl.java`（`create` / `pay` / `cancel` / 库存预占与释放） |
| 顺序图 | `OrderController` + `backend/src/main/java/com/fruittrade/security/JwtAuthenticationFilter.java` + `OrderServiceImpl` |
| 状态图 | `backend/src/main/java/com/fruittrade/engine/OrderStateMachine.java` |
| 数据流图 | 各 `Service` / `Mapper` + `sql/init.sql`（用户表、商品表、购物车表、订单表、支付流水表、售后单表等） |

前端对应页面（`frontend/src/views/user/`）：
`HomeView.vue`、`CategoryView.vue`、`ProductDetailView.vue`、`CartView.vue`、`CheckoutView.vue`、
`OrderListView.vue`、`OrderDetailView.vue`、`AfterSaleApplyView.vue`、`AfterSaleListView.vue`、`UserProfileView.vue`；
接口封装见 `frontend/src/api/user.js`。

## 4. 运行方式（完整工程见仓库根目录 README.md）

```bash
# 数据库：先执行建库与种子数据
mysql -u root -p < sql/init.sql

# 后端（Java 17 + Maven）
cd backend && mvn spring-boot:run        # 默认 http://localhost:8080

# 前端（Node 18+）
cd frontend && npm install && npm run dev  # 默认 http://localhost:5173
```

演示账号（以 `sql/init.sql` 为准）：管理员 `admin`、商家 `merchant01`、用户 `user01`；密码统一 `123456`（BCrypt 加密存储）。

## 5. 说明

- 前后端代码已整理进仓库根目录的 `backend/`、`frontend/`、`sql/`（团队共用工程），本目录不再保留代码副本，避免版本分叉。
- 本目录仅保留我个人负责的 UML 建模产物，便于按成员查阅与评分。
