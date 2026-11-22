# 水果交易智能管理系统（fruit-trade-smart-system）

面向生鲜电商的软件工程课程大作业：覆盖「货源上架 → 客户下单 → 库存管理 → 物流配送 → 售后服务」全链路的三端（用户端、商家端、平台管理端）智能交易系统，并完整呈现需求分析 → 系统设计 → 编码实现 → 测试 → 部署的软件工程流程。

---

## 一、核心功能

| 端 | 功能 |
|---|---|
| 用户端 | 分类浏览、搜索、商品详情、购物车、地址管理、下单与模拟支付、订单状态跟踪（待支付/待发货/运输中/已完成/已取消/售后中）、售后申请与申诉、投诉、个性化推荐 |
| 商家端 | 商品上下架、库存调整与不足预警、临期商品预警、订单发货与物流节点、售后审核、销售报表（ECharts） |
| 平台管理端 | 商家资质审核、用户账号管理、投诉处理、平台数据总览（ECharts） |

**智能特性**
- 个性化推荐：`score = 0.5×购买频次 + 0.3×User-Based CF + 0.2×近 30 天热销`，冷启动走热销榜并附推荐理由，缓存 30 分钟。
- 临期与损耗预警：临期阈值 30/7/3 天、损耗风险阈值 0.2/0.4/0.7，综合等级取最大值，支持每日 02:00 定时扫描。

---

## 二、技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5 + Java 17 + Spring Security + JWT + MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0（`utf8mb4`），脚本 `sql/init.sql` |
| 前端 | Vue 3 + Vite 5 + Vue Router 4 + Pinia 2 + Axios + ECharts 5 |
| 建模 | PlantUML（11 张图，源文件与 SVG 图片均在 `docs/uml/`） |
| 构建 | Maven 3.9+ / npm 10+ |

---

## 三、目录结构

```
fruit-trade-smart-system/
├── README.md                    # 本文件
├── sql/init.sql                 # 建库建表 + 种子数据（17 张表）
├── backend/                     # Spring Boot 后端工程
├── frontend/                    # Vue 3 前端工程
└── docs/
    ├── 00-项目管理/             # 项目计划、周报与周会纪要、阶段确认记录
    ├── 01-需求分析/             # 需求规格说明书、用户故事
    ├── 02-系统设计/             # 架构/数据库/详细/部署/API 契约
    ├── 03-测试/                 # 测试用例、测试报告
    ├── 04-交付文档/             # 软件使用说明书、软件部署说明书
    └── uml/                     # PlantUML 源文件 + images/ 下的 SVG
```

---

## 四、快速启动

### 1. 初始化数据库（MySQL 8）
```bash
mysql -u root -p < sql/init.sql
```
脚本将创建数据库 `fruit_trade_db` 并写入 17 张表与完整种子数据。若本机账号密码不是 `root/root`，请修改 `backend/src/main/resources/application.yml` 中的 datasource 配置。

### 2. 启动后端
```bash
cd backend
mvn -DskipTests compile
mvn spring-boot:run
```
后端监听 `http://localhost:8080`，接口前缀 `/api`。

> 项目在 `.tools/` 中准备了便携 Maven 3.9.9（本地辅助工具，未纳入版本库），也可使用系统 Maven：
> `../.tools/apache-maven-3.9.9/bin/mvn spring-boot:run`

### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```
浏览器打开 Vite 输出的地址（默认 `http://localhost:5173`），开发代理自动把 `/api` 转发到 `http://localhost:8080`。

### 4. 生产构建
```bash
cd frontend && npm run build     # 产物在 frontend/dist/
cd backend && mvn clean package  # 产物在 backend/target/
```

---

## 五、演示账号

以 `sql/init.sql` 种子数据为准，密码统一为 `123456`：

| 角色 | 账号 | 说明 |
|---|---|---|
| 平台管理员 | `admin` | 审核商家、管理用户与投诉 |
| 商家 | `merchant01`、`merchant02` | `merchant01` 已审核通过，`merchant02` 可用于审核演示（待审） |
| 普通用户 | `user01`、`user02`、`user03` | 浏览商品、下单、售后 |

登录页提供一键填充入口。若页面预填账号与此处不一致，请以此表为准（前端填充值已按种子数据校正）。

---

## 六、验证方式

```bash
# 后端单元测试（18 个用例，不依赖 MySQL）
cd backend && mvn test

# 前端构建验证
cd frontend && npm run build

# UML 图重新渲染（可选）
java -jar .tools/plantuml.jar -tsvg -o docs/uml/images docs/uml/*.puml
```

---

## 七、过程文档索引

| 阶段 | 文档 | 撰写成员 |
|---|---|---|
| 项目管理 | 项目计划.md、周报与周会纪要.md、阶段确认记录.md | 主管智能体 |
| 需求分析 | 需求规格说明书.md、用户故事.md | 需求分析智能体 |
| 系统设计 | 系统架构设计.md、数据库设计.md、详细设计.md、部署设计.md、API接口契约.md | 架构设计智能体 |
| 编码实现 | backend/、frontend/、sql/ | 后端开发智能体、前端开发智能体 |
| 测试与交付 | 测试用例.md、测试报告.md、软件使用说明书.md、软件部署说明书.md | 测试与文档智能体 |
| 最终汇总 | 大作业报告.md | 主管智能体 |

完整交付说明见 `docs/04-交付文档/软件部署说明书.md`，操作指引见 `docs/04-交付文档/软件使用说明书.md`。

---

## 八、版本与追溯

- 文档版本：v1.0；需求基线 `SRS-FTS-v1.0`，设计基线 `DESIGN-FTS-v1.0`。
- 所有过程文档与代码同仓库管理，API 契约是前后端对接的唯一依据，变更须同步更新文档。
- 本仓库为课程大作业交付物，系统角色与功能以《需求规格说明书》为准。
