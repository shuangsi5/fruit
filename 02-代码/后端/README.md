# 水果交易智能管理系统 - 后端

基于 Spring Boot 3.2 + Java 17 + MyBatis-Plus 3.5 的后端服务，实现用户端、商家端、平台管理端共 46 个 REST 接口，含混合推荐引擎与规则预警引擎。

## 一、技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.5 |
| MySQL Connector/J | 由 Spring Boot 管理 |
| JJWT | 0.11.5 |
| Lombok / Validation | 由 Spring Boot 管理 |
| Maven | 3.9.9（项目自带 `.tools/apache-maven-3.9.9`） |

## 二、启动方式

### 1. 初始化数据库

安装 MySQL 8，执行初始化脚本：

```bash
mysql -u root -p < sql/init.sql
```

脚本会创建 `fruit_trade_db`、17 张表并插入充足种子数据（3+ 用户、2 个已审核商家、6 个一级分类、20 个商品、多状态订单等）。

### 2. 修改配置

编辑 `src/main/resources/application.yml` 中的数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fruit_trade_db?...
    username: root
    password: root
```

### 3. 编译与运行

```bash
cd backend
../.tools/apache-maven-3.9.9/bin/mvn -q -DskipTests package
java -jar target/fruit-trade-backend-1.0.0.jar
```

服务启动后监听 `http://localhost:8080`。

### 4. 测试账号

密码统一为 `123456`（BCrypt 加密存储）：

| 账号 | 角色 | 说明 |
|------|------|------|
| admin | ADMIN | 平台管理员 |
| merchant01 | MERCHANT | 鲜果优选（已审核） |
| merchant02 | MERCHANT | 绿园鲜果（已审核） |
| user01 / user02 / user03 | USER | 普通用户 |

登录：`POST /api/auth/login`，请求体 `{"username":"user01","password":"123456"}`，返回 `data.token`。

## 三、接口概览

统一响应：`{ "code":0, "message":"success", "data":{}, "timestamp":1732166400000 }`，鉴权头 `Authorization: Bearer <token>`。

| 分组 | 接口 |
|------|------|
| 认证（4） | 注册、登录、当前用户、退出 |
| 公共（3） | 分类列表（树形）、商品列表（分页/筛选/排序）、商品详情 |
| 用户端（19） | 购物车 CRUD、地址 CRUD、下单/支付/取消/确认/物流、售后申请与列表、投诉、推荐、相关商品 |
| 商家端（13） | 商品管理、库存管理与预警、订单发货、售后处理、销售报表、临期商品 |
| 管理端（7） | 商家审核、用户管理、投诉处理、平台总览 |

完整参数与响应见 `docs/02-系统设计/API接口契约.md`。

## 四、配置项说明

| 配置 | 说明 | 默认 |
|------|------|------|
| `server.port` | 服务端口 | 8080 |
| `jwt.secret` | JWT 签名密钥 | 见 yml |
| `jwt.expiration` | Token 有效期（毫秒） | 86400000 |
| `cors.enabled` | 跨域开关 | true |
| `recommendation.weight-*` | 推荐混合权重（频次/CF/热销） | 0.5/0.3/0.2 |
| `recommendation.cache-ttl-minutes` | 推荐缓存时间 | 30 |
| `warning.expiring-thresholds` | 临期阈值（天） | 30,7,3 |
| `warning.loss-risk-thresholds` | 损耗风险阈值 | 0.2,0.4,0.7 |
| `warning.scan-cron` | 预警定时扫描表达式 | 每日 02:00 |
| `business.order-timeout-minutes` | 支付超时（分钟） | 30 |

## 五、核心业务规则

1. **下单**：服务端按 `price × qty` 重算金额；锁定库存 `stock -= qty, locked_stock += qty`，用 `version` 乐观锁防超卖；库存不足返回 `10001`，下架返回 `10002`。
2. **支付**：模拟支付直接成功，写 `payment_record`，扣 `locked_stock`，状态 `0→1`，生成待发货物流。
3. **状态机**：非法流转返回 `10003`；取消回补库存；发货生成物流节点。
4. **推荐**：`score = 0.5×频次 + 0.3×User-CF + 0.2×近30天热销`，冷启动走热销榜，带推荐理由，缓存 30 分钟。
5. **预警**：`综合等级 = max(临期, 损耗, 库存不足)`，每日定时扫描 + 商家端实时计算。

## 六、目录结构

```
backend/src/main/java/com/fruittrade/
  common/       统一响应、错误码、异常、常量
  config/       Security/CORS/MybatisPlus/定时任务/Jackson 配置
  security/     JWT 工具、过滤器、登录主体
  domain/       entity / dto / vo
  mapper/       MyBatis-Plus Mapper
  service/      业务接口 + impl
  engine/       推荐引擎、预警引擎
  job/          定时预警扫描
  controller/   用户端/商家端/管理端接口
```
