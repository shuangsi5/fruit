# 《软件工程与 UML》大作业 —— 个人交付包

**负责人：付磊杰**
**负责数据流：DF-3 智能推荐数据流**
**系统：水果交易智能管理系统（三端：用户端 / 商家端 / 平台管理端）**

---

## 一、本交付包包含什么

本目录是本人（付磊杰）在大作业中承担任务的完整产出，共五部分：

1. **`01-UML图/`**：DF-3 的五类 UML 图，均为 **PlantUML 原生 `.puml` 格式**（可直接用 PlantUML 打开、编辑、导出），并附导出 SVG 预览。
2. **`需求分析/`**：推荐功能用户故事（6 条）。
3. **`系统设计/`**：推荐引擎模块设计、评分公式与阈值配置、推荐日志表设计。
4. **`测试/`**：推荐相关性、冷启动兜底、评分边界、缓存失效四类测试用例与执行记录。
5. **`文档/`**：SRS 第 3.6 章智能特性说明、推荐算法设计说明。

本人负责的 **DF-3 智能推荐数据流** 覆盖的业务链路为：

> 历史订单采集 → 购买频次统计 → User-Based 协同过滤计算 → 近 30 天热销度加权 → 推荐结果生成 → 用户端「猜你喜欢」展示
> 涉及的智能特性：个性化推荐（FR-6.1~FR-6.3），冷启动走热销榜兜底并附推荐理由，结果缓存 30 分钟。

---

## 二、目录结构

```
members/fu/
├── 01-UML图/
│   ├── 用例图-推荐.puml              ← 功能模型
│   ├── 活动图-推荐计算.puml          ← 功能模型（流程分支）
│   ├── 时序图-推荐计算.puml          ← 行为模型（对象交互）
│   ├── 状态图-推荐.puml              ← 行为模型（状态流转）
│   ├── DFD-3-智能推荐.puml           ← 数据模型（数据流向）
│   └── *.svg                         ← 五张图 SVG 预览
├── 需求分析/
│   └── 推荐功能用户故事.md
├── 系统设计/
│   └── 推荐引擎设计说明.md
├── 测试/
│   └── 推荐测试用例与执行记录.md
├── 文档/
│   ├── SRS-第3.6章-智能特性说明.md
│   └── 推荐算法设计说明.md
├── README.md                         ← 个人贡献说明
└── README-个人交付包-付磊杰.md       ← 本文件
```

> 说明：DF-3 对应的代码（推荐引擎、推荐接口、推荐展示组件）已整理进仓库根目录的 `backend/`、`frontend/`、`sql/` 团队共用工程，本目录不再保留代码副本。

---

## 三、五类 UML 图说明与打开方式

| 序号 | 图名 | 文件 | 建模内容 |
|---|---|---|---|
| 1 | 用例图 | `用例图-推荐.puml` | 参与者（用户、系统/推荐引擎、平台管理员）、系统边界、查看猜你喜欢/查看推荐理由/冷启动兜底/计算评分/采集订单/记录日志/缓存结果/配置权重 8 个用例及 include/extend 关系 |
| 2 | 活动图 | `活动图-推荐计算.puml` | 从请求推荐到缓存返回的主流程，含缓存命中、冷启动、候选不足等分支与合流 |
| 3 | 顺序图 | `时序图-推荐计算.puml` | 用户、前端、GuessYouLike 组件、推荐控制器、推荐服务、推荐引擎、订单库、推荐日志、结果缓存 9 个对象间消息的时间顺序 |
| 4 | 状态图 | `状态图-推荐.puml` | 推荐生命周期「待计算→已生成→已展示→缓存失效→重新计算」四态及转换 |
| 5 | 数据流图 | `DFD-3-智能推荐.puml` | 外部实体（用户/订单历史/推荐结果）、5 个处理过程（P3.1~P3.5）、4 个数据存储之间的数据流向 |

### 打开 `.puml` 文件

- **方式一（命令行批量出图）**：`java -jar plantuml.jar -tsvg 01-UML图/*.puml`
- **方式二（VS Code 插件）**：安装「PlantUML」扩展，打开 `.puml` 按 `Alt+D` 预览。
- **方式三（IntelliJ IDEA）**：安装 PlantUML Integration 插件后右键渲染。
- **只看效果**：直接打开 `01-UML图/` 下同名 `.svg` 图片。

> 所有图均符合 PlantUML 标准语法，SVG 预览由 PlantUML 导出，证明文件可正常解析。

---

## 四、五张图 ↔ 代码 对应关系（重点）

| 图 | 图中关键元素 | 对应代码文件（仓库根目录下相对路径） |
|---|---|---|
| **用例图** | 查看猜你喜欢、推荐理由、冷启动兜底、配置权重 | 后端 `backend/src/main/java/com/fruittrade/controller/UserFeatureController.java`（推荐接口）；前端 `frontend/src/views/user/HomeView.vue` |
| **活动图** | 采集订单 → 频次/CF/热销 → 冷启动兜底 → 写日志 → 写缓存 | `backend/src/main/java/com/fruittrade/engine/HybridRecommendationEngine.java`（`recommend`/`hotFallback`）；`service/impl/RecommendationServiceImpl.java`（`loadData`/`recommend`） |
| **顺序图** | 9 个对象、缓存命中/未命中分支 | `UserFeatureController` → `RecommendationServiceImpl` → `HybridRecommendationEngine` → `OrdersMapper`/`OrderItemMapper`/`RecommendationLogMapper` |
| **状态图** | 待计算/已生成/已展示/缓存失效 四态 | `RecommendationServiceImpl`（`ConcurrentHashMap` 缓存、`cache-ttl-minutes=30`、`evictUser()`） |
| **数据流图** | P3.1~P3.5 五个处理、4 个数据存储 | `RecommendationServiceImpl.loadData()`/`recommend()`；数据存储对应 `sql/init.sql` 中的 `order_item`、`recommendation_log`、`product` 表与本地缓存 |

### 推荐算法的评分公式（与代码一致）

```
score(u, i) = 0.5 × freq(u,i) + 0.3 × cf_norm(u,i) + 0.2 × hot(i)
```

- `freq(u,i)`：购买频次归一化（`c(u,i) / max`）
- `cf_norm(u,i)`：User-Based 协同过滤（余弦相似度，Top-20 邻居）极差归一化后
- `hot(i)`：近 30 天热销度归一化（`s(i) / max`）
- 冷启动（无历史记录）直接走热销榜，`algorithm=HOT`，理由「近30天热销商品，适合新用户参考」

---

## 五、DF-3 本人负责的核心代码文件清单

### 5.1 后端（`backend/src/main/java/com/fruittrade/`）

| 层次 | 文件 | 作用 |
|---|---|---|
| 引擎 | `engine/HybridRecommendationEngine.java` | **混合推荐算法核心**：频次、协同过滤、热销加权、冷启动兜底、推荐理由 |
| 引擎 | `engine/RecommendData.java` | 算法输入数据模型 |
| 引擎 | `engine/RecommendItem.java` | 算法输出结果项 |
| Service | `service/RecommendationService.java` | 推荐服务接口 |
| Service | `service/impl/RecommendationServiceImpl.java` | **数据加载、缓存 30 分钟、推荐日志写入** |
| Controller | `controller/UserFeatureController.java` | `GET /api/recommendations` 推荐接口 |
| 领域 | `domain/entity/RecommendationLog.java`、`domain/vo/RecommendationVO.java` | 推荐日志实体、推荐结果 VO |
| 数据访问 | `mapper/RecommendationLogMapper.java` | 推荐日志表访问 |
| 配置 | `resources/application.yml`（`recommendation` 段） | 权重、Top-K、缓存时长等配置 |

### 5.2 前端（`frontend/src/`）

| 文件 | 作用 |
|---|---|
| `views/user/HomeView.vue` | 首页「为您推荐」展示位 |
| `components/ProductCard.vue` | 商品卡片（含推荐理由标签） |
| `api/user.js` | `getRecommendations` 接口封装 |

### 5.3 数据库（`sql/init.sql`）

DF-3 直接读写的表：`orders`、`order_item`（购买/热销数据来源）、`recommendation_log`（推荐日志）、`product`（候选商品）、`inventory`（库存过滤）。

---

## 六、如何运行代码

### 6.1 环境要求
- JDK 17+、Maven 3.6+、MySQL 8.0+、Node.js 18+

### 6.2 初始化数据库
```bash
mysql -u root -p < sql/init.sql
```
数据库连接配置在 `backend/src/main/resources/application.yml`，默认 `root/root`、`localhost:3306`。

### 6.3 启动后端
```bash
cd backend
mvn test              # 推荐引擎 5 条单元测试
mvn spring-boot:run   # 后端端口 8080，接口前缀 /api
```

### 6.4 启动前端
```bash
cd frontend
npm install
npm run dev           # 浏览器访问终端提示的地址（默认 http://localhost:5173）
```

### 6.5 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 普通用户 USER | `user01`（另有 user02、user03） | `123456` |
| 入驻商家 MERCHANT | `merchant01`（另有 merchant02） | `123456` |
| 平台管理员 ADMIN | `admin` | `123456` |

> 密码统一为 `123456`（BCrypt 加密存储，以 `sql/init.sql` 种子数据为准）。登录 `user01` 后首页「为您推荐」区即可看到带推荐理由标签的个性化推荐。

---

## 七、可直接写入个人报告的内容说明

> 本人（付磊杰）负责 **DF-3 智能推荐数据流** 的分析、建模与实现。在需求分析阶段，绘制了**用例图**梳理用户、系统（推荐引擎）与平台管理员在推荐功能中的功能边界，绘制**活动图**描述推荐计算的主流程与缓存命中、冷启动兜底、候选不足等分支；在系统设计阶段，绘制**顺序图**刻画前端、推荐控制器、推荐服务、推荐引擎与订单库/推荐日志/缓存之间的消息交互，绘制**状态图**定义推荐从「待计算」到「已生成/已展示/缓存失效」的生命周期流转，绘制**数据流图**明确外部实体、处理过程与数据存储之间的数据流向。在编码实现阶段，本人承担了混合推荐引擎（`HybridRecommendationEngine`，含购买频次、User-Based 协同过滤、近 30 天热销度加权与冷启动兜底）、推荐接口与用户端「猜你喜欢」展示组件及推荐理由标签，并实现了 30 分钟结果缓存与推荐日志留痕。上述五类图均使用 PlantUML 绘制并以 `.puml` 原生格式保存，可直接打开编辑；图与代码文件的对应关系已在第四章列表中说明。

---

*本 README 同时作为个人交付清单与汇报索引使用。*
