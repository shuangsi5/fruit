# 付磊杰 — 个人贡献说明

> 水果交易智能管理系统（软件工程课程大作业）
> 负责数据流：**DF-3 智能推荐数据流**
> 成员目录：`members/fu/`

## 1. 我负责的内容

按课程「每人负责一条完整端到端数据流」的分工，我负责 **DF-3 智能推荐数据流**，覆盖从
「历史订单采集 → 购买频次统计 → 协同过滤计算 → 热销度加权 → 推荐结果生成 → 用户端"猜你喜欢"展示」的完整链路。

本目录集中存放我个人的建模与文档产物，目录结构如下：

```
members/fu/
├── README.md                            ← 本说明
├── README-个人交付包-付磊杰.md          ← 个人交付清单（详细）
├── 01-UML图/                             ← 五类 UML 图（PlantUML 源文件 + SVG 预览）
│   ├── 用例图-推荐.puml / .svg
│   ├── 活动图-推荐计算.puml / .svg
│   ├── 时序图-推荐计算.puml / .svg
│   ├── 状态图-推荐.puml / .svg
│   └── DFD-3-智能推荐.puml / .svg
├── 需求分析/
│   └── 推荐功能用户故事.md
├── 系统设计/
│   └── 推荐引擎设计说明.md
├── 测试/
│   └── 推荐测试用例与执行记录.md
└── 文档/
    ├── SRS-第3.6章-智能特性说明.md
    └── 推荐算法设计说明.md
```

## 2. 五类 UML 图说明

| 图 | 文件 | 主要内容 |
|---|---|---|
| 用例图 | `用例图-推荐.puml` | 用户/系统（推荐引擎）/平台管理员三类参与者，8 个推荐用例及 include/extend 关系 |
| 活动图 | `活动图-推荐计算.puml` | 从请求推荐到缓存返回的主流程，含冷启动兜底、候选不足等分支 |
| 顺序图 | `时序图-推荐计算.puml` | 用户/前端/推荐控制器/推荐服务/推荐引擎/订单库/推荐日志/缓存 8 个对象的消息时序 |
| 状态图 | `状态图-推荐.puml` | 推荐生命周期「待计算 → 已生成 → 已展示 → 缓存失效 → 重新计算」 |
| 数据流图 | `DFD-3-智能推荐.puml` | 外部实体、P3.1~P3.5 处理过程、4 个数据存储之间的数据流 |

- 五类图均为 **PlantUML 文本化建模**，源文件为 `.puml`，SVG 为导出预览图。

## 3. 图 ↔ 代码对应关系

DF-3 的建模产物与仓库中的实际代码一一对应（代码位于仓库标准目录，非本目录副本）：

| 图 | 对应代码（仓库路径） |
|---|---|
| 用例图 | `backend/src/main/java/com/fruittrade/controller/UserFeatureController.java`（`GET /api/recommendations`）；`frontend/src/views/user/HomeView.vue` |
| 活动图 | `backend/src/main/java/com/fruittrade/engine/HybridRecommendationEngine.java`（`recommend`/`hotFallback`）；`service/impl/RecommendationServiceImpl.java`（`loadData`） |
| 顺序图 | `UserFeatureController` → `RecommendationServiceImpl` → `HybridRecommendationEngine` → `OrdersMapper`/`OrderItemMapper` |
| 状态图 | `RecommendationServiceImpl`（`ConcurrentHashMap` 缓存 + 30 分钟 TTL + `evictUser`） |
| 数据流图 | `RecommendationServiceImpl.loadData()` + `recommend()`；`sql/init.sql`（`order_item`、`recommendation_log`、`product` 表） |

前端对应：`frontend/src/views/user/HomeView.vue`（「为您推荐」展示位）、`frontend/src/components/ProductCard.vue`（推荐理由标签）、`frontend/src/api/user.js`（`getRecommendations` 接口封装）。

## 4. 运行方式（完整工程见仓库根目录 README.md）

```bash
# 数据库：先执行建库与种子数据
mysql -u root -p < sql/init.sql

# 后端（Java 17 + Maven），推荐引擎单测随构建执行
cd backend && mvn test && mvn spring-boot:run   # 默认 http://localhost:8080

# 前端（Node 18+）
cd frontend && npm install && npm run dev        # 默认 http://localhost:5173
```

演示账号（以 `sql/init.sql` 为准）：管理员 `admin`、商家 `merchant01`、用户 `user01`；密码统一 `123456`。

## 5. 说明

- 前后端代码已整理进仓库根目录的 `backend/`、`frontend/`、`sql/`（团队共用工程），本目录不再保留代码副本，避免版本分叉。
- 本目录保留我个人负责的 DF-3 建模与文档产物，便于按成员查阅与评分。
- 推荐算法权重、Top-K、缓存时长等参数集中在 `backend/src/main/resources/application.yml` 的 `recommendation` 段配置。
