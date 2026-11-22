# API 接口契约

> 文档名称：水果交易智能管理系统 —— API 接口契约
> 所属阶段：P2 系统设计与数据建模
> 撰写成员：架构设计智能体
> 对应版本：v1.0
>
> 本文档是后端与前端对接的**唯一依据**，前后端必须严格按此实现，任何变更需同步更新本文档并经双方确认。

---

## 1. 统一约定

### 1.1 基础信息

| 项目 | 约定 |
|------|------|
| Base URL | `/api` |
| 协议 | HTTP/HTTPS，UTF-8 |
| 请求格式 | `Content-Type: application/json`（除文件上传另约定） |
| 响应格式 | `application/json` |
| 时间格式 | 日期时间 `yyyy-MM-dd HH:mm:ss`；时间戳为毫秒级 Long |
| 金额格式 | 数字，保留两位小数，如 `19.90` |
| ID 类型 | `Long`（对应数据库 BIGINT UNSIGNED） |

### 1.2 统一响应包装

所有接口（包括错误）均返回以下结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1732166400000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，0 表示成功，非 0 见错误码表 |
| message | string | 提示信息 |
| data | object/array/null | 业务数据，无数据时为 `null` 或 `{}` |
| timestamp | long | 服务端响应时间戳（毫秒） |

### 1.3 分页约定

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码，从 1 开始 |
| pageSize | int | 否 | 10 | 每页条数，最大 100 |

**响应 data 结构**：

```json
{
  "list": [],
  "total": 128,
  "pageNum": 1,
  "pageSize": 10,
  "pages": 13
}
```

### 1.4 鉴权约定

- 除标注「公开」的接口外，所有接口需在请求头携带：

```http
Authorization: Bearer <token>
```

- Token 为登录返回的 JWT，含 `userId`、`role`（`USER`/`MERCHANT`/`ADMIN`）、`merchantId`。
- 角色权限：
  - `USER`：用户端接口，且仅能访问本人数据。
  - `MERCHANT`：商家端接口，且仅能访问本商家（`merchantId`）数据。
  - `ADMIN`：平台端接口，可访问全部数据。
- 前端 Axios 拦截器统一注入 Token，并在收到 `40101/40102` 时跳转登录。

### 1.5 错误码表

| code | 含义 | 处理建议 |
|------|------|----------|
| 0 | 成功 | - |
| 40001 | 参数校验失败 | 检查请求参数 |
| 40002 | 请求体 JSON 格式错误 | 检查请求体 |
| 40003 | 缺少必填参数 | 补齐必填项 |
| 40101 | 未登录或 Token 缺失 | 跳转登录 |
| 40102 | Token 无效或已过期 | 重新登录 |
| 40103 | 账号或密码错误 | 检查凭据 |
| 40301 | 权限不足 | 联系管理员 |
| 40302 | 账号已被禁用 | 联系平台 |
| 40401 | 资源不存在 | 检查 ID |
| 40402 | 接口不存在 | 检查路径 |
| 40501 | 请求方法不支持 | 检查 HTTP 方法 |
| 40901 | 数据已存在（重复） | 更换唯一字段 |
| 40902 | 并发冲突（乐观锁失败） | 重试操作 |
| 42201 | 业务规则校验失败 | 查看 message |
| 42901 | 请求过于频繁 | 稍后重试 |
| 50001 | 系统内部错误 | 联系技术人员 |
| 50002 | 数据库异常 | 联系技术人员 |
| 50003 | 文件操作异常 | 检查文件 |
| 10001 | 库存不足 | 减少购买数量 |
| 10002 | 商品已下架 | 选择其他商品 |
| 10003 | 订单状态不允许此操作 | 刷新订单状态 |
| 10004 | 支付失败 | 重新支付 |
| 10005 | 售后申请超出时限 | 超时不可申请 |
| 10006 | 商家未通过审核 | 等待审核结果 |

---

## 2. 认证模块

### 2.1 用户注册
- **POST `/api/auth/register`** ｜ 权限：公开
- 请求体字段：`username`(string,必填)、`password`(string,必填,长度≥6)、`role`(string,必填,`USER`/`MERCHANT`)、`phone`(string,可选)、`realName`(string,可选)、`merchantName`(string,注册商家时必填)
- 请求示例：
```json
{ "username": "merchant01", "password": "123456", "role": "MERCHANT", "phone": "13800000000", "merchantName": "鲜果优选" }
```
- 响应 data：
```json
{ "userId": 12, "username": "merchant01", "role": "MERCHANT", "merchantId": 3, "auditStatus": 0 }
```
- 规则：`username`/`phone` 唯一；密码 BCrypt 加密；`role=MERCHANT` 时自动创建待审核商家（`audit_status=0`），审核通过前不可上架商品。

### 2.2 用户登录
- **POST `/api/auth/login`** ｜ 权限：公开
- 请求体字段：`username`(string,必填)、`password`(string,必填)
- 请求示例：
```json
{ "username": "admin", "password": "123456" }
```
- 响应 data：
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "tokenType": "Bearer", "expiresIn": 86400, "userId": 1, "username": "admin", "role": "ADMIN", "merchantId": null, "realName": "系统管理员" }
```
- 规则：校验密码，账号被禁用返回 `40302`；签发 JWT；`expiresIn` 单位秒。

### 2.3 获取当前用户
- **GET `/api/auth/me`** ｜ 权限：登录用户
- 请求参数：无（从 Token 解析）
- 响应 data：
```json
{ "userId": 12, "username": "merchant01", "role": "MERCHANT", "merchantId": 3, "realName": "张三", "avatar": null, "status": 1 }
```
- 规则：Token 无效返回 `40102`。

### 2.4 退出登录
- **POST `/api/auth/logout`** ｜ 权限：登录用户
- 请求参数：无
- 响应 data：`null`
- 规则：前端清除本地 Token；服务端可选将 Token 加入黑名单。

---

## 3. 公共接口

### 3.1 分类列表
- **GET `/api/categories`** ｜ 权限：公开
- 请求参数：`parentId`(int,可选,默认 0，查一级分类)、`status`(int,可选)
- 响应 data：
```json
[
  { "id": 1, "name": "应季水果", "parentId": 0, "sort": 1, "icon": null,
    "children": [ { "id": 11, "name": "柑橘类", "parentId": 1, "sort": 1, "icon": null } ] }
]
```
- 规则：仅返回启用分类；支持树形结构（一级带 `children`）。

### 3.2 商品列表
- **GET `/api/products`** ｜ 权限：公开
- 请求参数：`categoryId`(long,可选)、`keyword`(string,可选,名称模糊)、`merchantId`(long,可选)、`minPrice`/`maxPrice`(decimal,可选)、`sortBy`(string,可选,`sales`/`priceAsc`/`priceDesc`/`newest`,默认 `newest`)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{
  "list": [ { "id": 101, "name": "赣南脐橙", "subtitle": "酸甜多汁", "mainImage": "/img/orange.jpg",
    "price": 19.90, "originalPrice": 25.00, "unit": "斤", "origin": "江西赣州",
    "salesCount": 1200, "merchantId": 3, "merchantName": "鲜果优选", "status": 1 } ],
  "total": 128, "pageNum": 1, "pageSize": 10, "pages": 13
}
```
- 规则：仅返回 `status=1`（上架）且 `deleted=0` 的商品。

### 3.3 商品详情
- **GET `/api/products/{id}`** ｜ 权限：公开
- 路径参数：`id`(long,必填)
- 响应 data：
```json
{
  "id": 101, "name": "赣南脐橙", "subtitle": "酸甜多汁", "description": "产地直发…",
  "price": 19.90, "originalPrice": 25.00, "unit": "斤", "spec": "5斤装", "origin": "江西赣州",
  "shelfLifeDays": 15, "productionDate": "2026-11-18", "salesCount": 1200, "viewCount": 3200,
  "status": 1, "merchantId": 3, "merchantName": "鲜果优选",
  "categoryId": 11, "categoryName": "柑橘类",
  "images": [ { "url": "/img/orange1.jpg", "sort": 0, "isMain": 1 } ],
  "stock": 500
}
```
- 规则：浏览时 `view_count + 1`；商品不存在返回 `40401`。

---

## 4. 用户端接口

### 4.1 查询购物车
- **GET `/api/cart`** ｜ 权限：USER
- 请求参数：无
- 响应 data：
```json
{ "items": [ { "id": 5, "productId": 101, "productName": "赣南脐橙", "mainImage": "/img/orange.jpg",
  "price": 19.90, "quantity": 2, "selected": 1, "stock": 500, "subtotal": 39.80 } ],
  "totalQuantity": 2, "selectedSubtotal": 39.80 }
```
- 规则：仅当前用户数据；价格以商品实时价为准（展示用，下单服务端重算）。

### 4.2 加入购物车
- **POST `/api/cart`** ｜ 权限：USER
- 请求体字段：`productId`(long,必填)、`quantity`(int,必填,≥1)
- 请求示例：`{ "productId": 101, "quantity": 2 }`
- 响应 data：`{ "cartId": 5, "quantity": 2 }`
- 规则：已存在则累加数量；商品下架返回 `10002`；数量不得超过可用库存，超出返回 `10001`。

### 4.3 修改购物车数量
- **PUT `/api/cart/{id}`** ｜ 权限：USER
- 路径参数：`id`(long,必填)；请求体：`quantity`(int,必填,≥1)、`selected`(int,可选,0/1)
- 请求示例：`{ "quantity": 3, "selected": 1 }`
- 响应 data：`{ "cartId": 5, "quantity": 3, "subtotal": 59.70 }`
- 规则：非本人购物车项返回 `40301`；超出库存返回 `10001`。

### 4.4 删除购物车项
- **DELETE `/api/cart/{id}`** ｜ 权限：USER
- 路径参数：`id`(long,必填)
- 响应 data：`null`
- 规则：仅本人可删除。

### 4.5 查询地址列表
- **GET `/api/addresses`** ｜ 权限：USER
- 响应 data：
```json
[ { "id": 8, "receiver": "李四", "phone": "13900000000", "province": "广东省", "city": "深圳市",
    "district": "南山区", "detail": "科技园路1号", "isDefault": 1 } ]
```
- 规则：按默认地址优先排序。

### 4.6 新增地址
- **POST `/api/addresses`** ｜ 权限：USER
- 请求体字段：`receiver`(string,必填)、`phone`(string,必填)、`province`/`city`/`district`(string,可选)、`detail`(string,必填)、`isDefault`(int,可选,默认 0)
- 请求示例：
```json
{ "receiver": "李四", "phone": "13900000000", "province": "广东省", "city": "深圳市", "district": "南山区", "detail": "科技园路1号", "isDefault": 1 }
```
- 响应 data：`{ "addressId": 8 }`
- 规则：`isDefault=1` 时清除该用户其他默认地址。

### 4.7 删除地址
- **DELETE `/api/addresses/{id}`** ｜ 权限：USER
- 路径参数：`id`(long,必填)
- 响应 data：`null`
- 规则：仅本人可删除；逻辑删除。

### 4.8 创建订单
- **POST `/api/orders`** ｜ 权限：USER
- 请求体字段：`addressId`(long,必填)、`items`(array,必填,至少 1 项，每项含 `productId`、`quantity`)、`remark`(string,可选)、`payMethod`(int,可选,默认 1)
- 请求示例：
```json
{ "addressId": 8, "payMethod": 1, "remark": "尽快发货",
  "items": [ { "productId": 101, "quantity": 2 }, { "productId": 105, "quantity": 1 } ] }
```
- 响应 data：
```json
{ "orderId": 9001, "orderNo": "2026112100010001", "payAmount": 59.70, "status": 0, "expireTime": "2026-11-21 12:30:00" }
```
- 规则：服务端重新计算价格，不信任前端金额；校验库存并预占（`stock -= qty; locked_stock += qty`），库存不足返回 `10001`；商品下架返回 `10002`；状态置 `0`（待支付）；`expireTime` 为支付超时时间（如 30 分钟）。

### 4.9 支付订单
- **POST `/api/orders/{id}/pay`** ｜ 权限：USER（订单所属人）
- 路径参数：`id`(long,必填)；请求体字段：`payMethod`(int,必填,1 模拟微信 2 模拟支付宝)
- 请求示例：`{ "payMethod": 1 }`
- 响应 data：
```json
{ "paymentNo": "PAY202611210001", "orderNo": "2026112100010001", "amount": 59.70, "status": 1, "paidTime": "2026-11-21 10:05:00" }
```
- 规则：仅 `status=0` 可支付，否则 `10003`；模拟支付直接成功；生成 `payment_record`；订单状态流转 `0 → 1`（待发货），释放锁定库存并扣减实际库存；支付失败返回 `10004`。

### 4.10 查询订单列表
- **GET `/api/orders`** ｜ 权限：USER
- 请求参数：`status`(int,可选,订单状态)、`keyword`(string,可选,商品名)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "id": 9001, "orderNo": "2026112100010001", "status": 1, "statusText": "待发货",
  "payAmount": 59.70, "createTime": "2026-11-21 10:00:00",
  "itemPreview": [ { "productName": "赣南脐橙", "mainImage": "/img/orange.jpg", "quantity": 2 } ] }
```
- 规则：仅本人订单；按创建时间倒序。

### 4.11 查询订单详情
- **GET `/api/orders/{id}`** ｜ 权限：USER（或该订单商家、ADMIN）
- 路径参数：`id`(long,必填)
- 响应 data：
```json
{
  "id": 9001, "orderNo": "2026112100010001", "status": 2, "statusText": "运输中",
  "receiver": "李四", "receiverPhone": "13900000000", "receiverAddress": "广东省深圳市南山区科技园路1号",
  "totalAmount": 59.70, "freight": 0.00, "payAmount": 59.70, "payMethod": 1,
  "payTime": "2026-11-21 10:05:00", "shipTime": "2026-11-21 14:00:00", "createTime": "2026-11-21 10:00:00",
  "items": [ { "productId": 101, "productName": "赣南脐橙", "productImage": "/img/orange.jpg", "price": 19.90, "quantity": 2, "subtotal": 39.80 } ],
  "logistics": { "logisticsNo": "SF1234567890", "carrier": "顺丰速运", "status": 2, "traces": [ { "time": "2026-11-21 14:00:00", "desc": "已揽收" } ] }
}
```
- 规则：非相关用户返回 `40301`。

### 4.12 取消订单
- **POST `/api/orders/{id}/cancel`** ｜ 权限：USER（订单所属人）
- 路径参数：`id`(long,必填)
- 响应 data：`{ "orderId": 9001, "status": 4, "statusText": "已取消" }`
- 规则：仅 `status=0`（待支付）或 `status=1`（待发货，需商家同意，教学可简化）可取消；释放锁定库存回补 `stock`；已发货不可取消返回 `10003`。

### 4.13 确认收货
- **POST `/api/orders/{id}/confirm`** ｜ 权限：USER（订单所属人）
- 路径参数：`id`(long,必填)
- 响应 data：`{ "orderId": 9001, "status": 3, "statusText": "已完成" }`
- 规则：仅 `status=2`（运输中）可确认；流转 `2 → 3`；记录 `finish_time`；确认后 7 天内可申请售后。

### 4.14 查询物流轨迹
- **GET `/api/orders/{id}/logistics`** ｜ 权限：USER（订单所属人）
- 路径参数：`id`(long,必填)
- 响应 data：
```json
{ "logisticsNo": "SF1234567890", "carrier": "顺丰速运", "status": 2, "statusText": "运输中",
  "shipTime": "2026-11-21 14:00:00", "deliverTime": null,
  "traces": [ { "time": "2026-11-21 14:00:00", "desc": "快件已揽收" }, { "time": "2026-11-21 18:00:00", "desc": "到达转运中心" } ] }
```
- 规则：未发货订单返回 `null` 数据或空轨迹。

### 4.15 申请售后
- **POST `/api/after-sales`** ｜ 权限：USER
- 请求体字段：`orderId`(long,必填)、`orderItemId`(long,可选)、`type`(int,必填,1 仅退款 2 退货退款 3 换货)、`reason`(string,必填)、`description`(string,可选)、`images`(array,可选)、`amount`(decimal,必填)
- 请求示例：
```json
{ "orderId": 9001, "orderItemId": 7001, "type": 2, "reason": "商品腐烂", "description": "收到部分变质", "images": ["/img/proof.jpg"], "amount": 19.90 }
```
- 响应 data：`{ "afterSaleId": 3001, "afterSaleNo": "AS202611210001", "status": 0, "statusText": "待审核" }`
- 规则：订单需为 `status=3`（已完成）且在完成后 7 天内，超时返回 `10005`；生成售后单；订单状态置 `5`（售后中）。

### 4.16 查询我的售后列表
- **GET `/api/after-sales`** ｜ 权限：USER
- 请求参数：`status`(int,可选)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "id": 3001, "afterSaleNo": "AS202611210001", "orderId": 9001, "type": 2, "reason": "商品腐烂", "amount": 19.90, "status": 0, "statusText": "待审核", "createTime": "2026-11-22 09:00:00" }
```
- 规则：仅本人售后单。

### 4.17 提交投诉
- **POST `/api/complaints`** ｜ 权限：USER
- 请求体字段：`merchantId`(long,可选)、`orderId`(long,可选)、`type`(int,必填,1 商品质量 2 服务态度 3 物流 4 其他)、`content`(string,必填)、`images`(array,可选)
- 请求示例：
```json
{ "merchantId": 3, "orderId": 9001, "type": 1, "content": "水果不新鲜，与描述不符", "images": ["/img/c1.jpg"] }
```
- 响应 data：`{ "complaintId": 5001, "complaintNo": "CP202611220001", "status": 0, "statusText": "待处理" }`
- 规则：投诉进入平台待处理队列，状态 `0`。

### 4.18 个性化推荐商品
- **GET `/api/recommendations`** ｜ 权限：USER
- 请求参数：`scene`(string,可选,`HOME`/`DETAIL`/`CART`,默认 `HOME`)、`limit`(int,可选,默认 10,最大 50)
- 响应 data：
```json
{ "scene": "HOME", "algorithm": "MIXED", "list": [
    { "productId": 101, "name": "赣南脐橙", "mainImage": "/img/orange.jpg", "price": 19.90, "salesCount": 1200,
      "score": 0.8723, "reason": "基于购买频次" } ] }
```
- 规则：调用推荐引擎（频次 + 协同过滤 + 热销兜底），详见详细设计；结果缓存，冷启动用户返回热销榜；`reason` 可取值「基于购买频次/猜你喜欢/热销推荐」。

### 4.19 相关商品推荐
- **GET `/api/products/{id}/related`** ｜ 权限：公开
- 路径参数：`id`(long,必填)；请求参数：`limit`(int,可选,默认 6)
- 响应 data：
```json
{ "list": [ { "productId": 102, "name": "冰糖脐橙", "mainImage": "/img/orange2.jpg", "price": 22.50, "score": 0.61 } ] }
```
- 规则：基于同类目 + 协同过滤，排除当前商品，仅上架商品。

---

## 5. 商家端接口

> 商家端所有接口校验当前用户 `role=MERCHANT` 且商家 `audit_status=1`（审核通过）、`status=1`（营业中），否则返回 `40301` 或 `10006`。

### 5.1 商家商品列表
- **GET `/api/merchant/products`** ｜ 权限：MERCHANT
- 请求参数：`status`(int,可选,0 下架 1 上架)、`keyword`(string,可选)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "id": 101, "name": "赣南脐橙", "mainImage": "/img/orange.jpg", "price": 19.90, "categoryId": 11,
  "status": 1, "salesCount": 1200, "stock": 500, "warningLevel": 0, "createTime": "2026-11-01 10:00:00" }
```
- 规则：仅本商家商品。

### 5.2 上架商品
- **POST `/api/merchant/products`** ｜ 权限：MERCHANT
- 请求体字段：`name`(string,必填)、`categoryId`(long,必填)、`price`(decimal,必填)、`originalPrice`(decimal,可选)、`unit`(string,可选,默认 斤)、`spec`(string,可选)、`origin`(string,可选)、`description`(string,可选)、`shelfLifeDays`(int,必填)、`productionDate`(date,必填)、`stock`(int,必填,≥0)、`safetyStock`(int,可选,默认 10)、`images`(array,必填,至少 1 项 URL)
- 请求示例：
```json
{ "name": "赣南脐橙", "categoryId": 11, "price": 19.90, "originalPrice": 25.00, "unit": "斤",
  "spec": "5斤装", "origin": "江西赣州", "description": "产地直发", "shelfLifeDays": 15,
  "productionDate": "2026-11-18", "stock": 500, "safetyStock": 50, "images": ["/img/orange.jpg"] }
```
- 响应 data：`{ "productId": 101, "status": 1 }`
- 规则：创建商品同时创建 `inventory` 记录并写 `inventory_log`（入库）；状态置 `1`（上架）。

### 5.3 修改商品
- **PUT `/api/merchant/products/{id}`** ｜ 权限：MERCHANT
- 路径参数：`id`(long,必填)；请求体字段同 5.2（可选字段按需传）
- 响应 data：`{ "productId": 101 }`
- 规则：仅本商家商品可改；不得修改 `merchantId`。

### 5.4 下架商品
- **POST `/api/merchant/products/{id}/off-shelf`** ｜ 权限：MERCHANT
- 路径参数：`id`(long,必填)
- 响应 data：`{ "productId": 101, "status": 0 }`
- 规则：仅本商家商品；置 `status=0`；已下单未支付订单不受影响。

### 5.5 查询库存列表
- **GET `/api/merchant/inventory`** ｜ 权限：MERCHANT
- 请求参数：`warningLevel`(int,可选,0-3)、`keyword`(string,可选)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "productId": 101, "productName": "赣南脐橙", "stock": 500, "lockedStock": 20, "safetyStock": 50,
  "warningLevel": 0, "remainingDays": 12, "productionDate": "2026-11-18", "shelfLifeDays": 15 }
```
- 规则：仅本商家库存；`remainingDays = shelfLifeDays - (今日 - productionDate)`。

### 5.6 更新库存
- **PUT `/api/merchant/inventory/{productId}`** ｜ 权限：MERCHANT
- 路径参数：`productId`(long,必填)；请求体字段：`delta`(int,必填,变动量，正为入库负为出库)、`changeType`(int,必填,1 入库 4 盘点 等)、`remark`(string,可选)、`safetyStock`(int,可选)
- 请求示例：`{ "delta": 100, "changeType": 1, "remark": "补货", "safetyStock": 60 }`
- 响应 data：`{ "productId": 101, "beforeStock": 500, "afterStock": 600, "warningLevel": 0 }`
- 规则：使用乐观锁 `version` 防并发；写 `inventory_log`；重算预警等级。

### 5.7 库存与临期预警查询
- **GET `/api/merchant/inventory/warnings`** ｜ 权限：MERCHANT
- 请求参数：`level`(int,可选,最低预警等级,默认 1)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "productId": 101, "productName": "赣南脐橙", "warningType": "EXPIRING", "warningTypeText": "临期",
  "level": 2, "levelText": "警告", "remainingDays": 5, "stock": 120,
  "avgDailySales": 30, "lossRisk": 0.65, "suggestion": "建议5天内促销清仓" }
```
- 规则：`warningType` 取 `EXPIRING`（临期）/`LOSS`（损耗）/`LOW_STOCK`（库存不足）；由预警引擎计算，见详细设计。

### 5.8 商家订单列表
- **GET `/api/merchant/orders`** ｜ 权限：MERCHANT
- 请求参数：`status`(int,可选)、`keyword`(string,可选,订单号/商品名)、分页参数
- 响应 data：分页对象，`list` 元素同 4.10 并含 `receiver`（脱敏）。
- 规则：仅本商家订单；按创建时间倒序。

### 5.9 订单发货
- **POST `/api/merchant/orders/{id}/ship`** ｜ 权限：MERCHANT
- 路径参数：`id`(long,必填)；请求体字段：`logisticsNo`(string,必填)、`carrier`(string,必填)
- 请求示例：`{ "logisticsNo": "SF1234567890", "carrier": "顺丰速运" }`
- 响应 data：`{ "orderId": 9001, "status": 2, "statusText": "运输中", "shipTime": "2026-11-21 14:00:00" }`
- 规则：仅 `status=1`（待发货）可发货，否则 `10003`；创建 `logistics` 记录；扣减 `locked_stock`；订单状态 `1 → 2`。

### 5.10 商家售后列表
- **GET `/api/merchant/after-sales`** ｜ 权限：MERCHANT
- 请求参数：`status`(int,可选)、分页参数
- 响应 data：分页对象，`list` 元素同 4.16 并含 `orderNo`、`buyerName`。
- 规则：仅本商家售后单。

### 5.11 处理售后
- **POST `/api/merchant/after-sales/{id}/handle`** ｜ 权限：MERCHANT
- 路径参数：`id`(long,必填)；请求体字段：`action`(string,必填,`APPROVE`/`REJECT`)、`handleRemark`(string,可选)
- 请求示例：`{ "action": "APPROVE", "handleRemark": "同意退款" }`
- 响应 data：`{ "afterSaleId": 3001, "status": 2, "statusText": "处理中" }`
- 规则：仅 `status=0`（待审核）可处理；`APPROVE` 置 `status=2`（处理中），`REJECT` 置 `status=4`（已驳回）；写 `audit_record`；同意退款时回补库存并生成退款流水（可选）。

### 5.12 销售报表
- **GET `/api/merchant/reports/sales`** ｜ 权限：MERCHANT
- 请求参数：`startDate`(date,必填)、`endDate`(date,必填)、`granularity`(string,可选,`DAY`/`WEEK`/`MONTH`,默认 `DAY`)
- 响应 data：
```json
{
  "summary": { "totalSales": 15860.50, "totalOrders": 230, "totalUnits": 1450, "avgOrderAmount": 68.96 },
  "trend": [ { "date": "2026-11-01", "sales": 820.00, "orders": 12, "units": 75 } ],
  "topProducts": [ { "productId": 101, "productName": "赣南脐橙", "units": 520, "sales": 10348.00 } ]
}
```
- 规则：按支付成功且未退款的订单统计；前端用 ECharts 渲染趋势图。

### 5.13 临期商品查询
- **GET `/api/merchant/products/expiring`** ｜ 权限：MERCHANT
- 请求参数：`days`(int,可选,查询未来 N 天内到期,默认 7)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "productId": 101, "productName": "赣南脐橙", "productionDate": "2026-11-18", "shelfLifeDays": 15,
  "expireDate": "2026-12-03", "remainingDays": 5, "stock": 120, "warningLevel": 2 }
```
- 规则：`remainingDays <= days` 且 `stock > 0` 的在售商品，按剩余天数升序。

---

## 6. 平台管理端接口

### 6.1 商家列表（含审核状态）
- **GET `/api/admin/merchants`** ｜ 权限：ADMIN
- 请求参数：`auditStatus`(int,可选,0 待审 1 通过 2 驳回)、`keyword`(string,可选)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "id": 3, "merchantName": "鲜果优选", "userName": "merchant01", "contactPerson": "张三",
  "contactPhone": "13800000000", "licenseNo": "91360700MA...", "licenseImage": "/img/license.jpg",
  "auditStatus": 0, "auditStatusText": "待审核", "status": 1, "createTime": "2026-11-20 10:00:00" }
```
- 规则：可按审核状态筛选待审商家。

### 6.2 审核商家
- **POST `/api/admin/merchants/{id}/audit`** ｜ 权限：ADMIN
- 路径参数：`id`(long,必填)；请求体字段：`action`(string,必填,`APPROVE`/`REJECT`)、`remark`(string,可选)
- 请求示例：`{ "action": "APPROVE", "remark": "资质齐全" }`
- 响应 data：`{ "merchantId": 3, "auditStatus": 1, "auditStatusText": "审核通过" }`
- 规则：仅 `auditStatus=0` 可审核；`APPROVE` 置 `1`，`REJECT` 置 `2`；记录 `audit_time`、`audit_remark`；写 `audit_record`（`biz_type=MERCHANT`）。

### 6.3 用户管理列表
- **GET `/api/admin/users`** ｜ 权限：ADMIN
- 请求参数：`role`(string,可选)、`status`(int,可选)、`keyword`(string,可选,用户名/手机号)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "id": 12, "username": "merchant01", "realName": "张三", "phone": "13800000000",
  "role": "MERCHANT", "status": 1, "createTime": "2026-11-20 10:00:00" }
```
- 规则：不返回密码字段。

### 6.4 启用/禁用用户
- **PUT `/api/admin/users/{id}/status`** ｜ 权限：ADMIN
- 路径参数：`id`(long,必填)；请求体字段：`status`(int,必填,1 启用 0 禁用)
- 请求示例：`{ "status": 0 }`
- 响应 data：`{ "userId": 12, "status": 0 }`
- 规则：禁用后该用户 Token 失效（鉴权时校验 `status`），不可登录；不可禁用自身 ADMIN 账号。

### 6.5 投诉列表
- **GET `/api/admin/complaints`** ｜ 权限：ADMIN
- 请求参数：`status`(int,可选)、`type`(int,可选)、分页参数
- 响应 data：分页对象，`list` 元素：
```json
{ "id": 5001, "complaintNo": "CP202611220001", "userName": "lisi", "merchantName": "鲜果优选",
  "type": 1, "typeText": "商品质量", "content": "水果不新鲜", "status": 0, "statusText": "待处理",
  "createTime": "2026-11-22 09:00:00" }
```
- 规则：按创建时间倒序。

### 6.6 处理投诉
- **POST `/api/admin/complaints/{id}/handle`** ｜ 权限：ADMIN
- 路径参数：`id`(long,必填)；请求体字段：`action`(string,必填,`PROCESS`/`CLOSE`)、`handleRemark`(string,必填)
- 请求示例：`{ "action": "CLOSE", "handleRemark": "已联系商家补发，用户满意" }`
- 响应 data：`{ "complaintId": 5001, "status": 2, "statusText": "已处理" }`
- 规则：`PROCESS` 置 `status=1`（处理中），`CLOSE` 置 `status=2`（已处理）；写 `audit_record`（`biz_type=COMPLAINT`）。

### 6.7 数据总览统计
- **GET `/api/admin/overview`** ｜ 权限：ADMIN
- 请求参数：`days`(int,可选,统计近 N 天,默认 30)
- 响应 data：
```json
{
  "users": { "total": 1200, "newUsers": 85 },
  "merchants": { "total": 46, "pendingAudit": 5, "active": 41 },
  "orders": { "total": 8900, "today": 120, "totalSales": 568000.00 },
  "afterSales": { "total": 210, "pending": 12 },
  "complaints": { "total": 36, "pending": 4 },
  "trend": [ { "date": "2026-11-01", "orders": 110, "sales": 7200.00 } ]
}
```
- 规则：聚合统计，建议缓存；前端用 ECharts 渲染总览看板。

---

## 7. 接口汇总清单

| 序号 | 方法 | 路径 | 权限 | 模块 |
|------|------|------|------|------|
| 1 | POST | /api/auth/register | 公开 | 认证 |
| 2 | POST | /api/auth/login | 公开 | 认证 |
| 3 | GET | /api/auth/me | 登录 | 认证 |
| 4 | POST | /api/auth/logout | 登录 | 认证 |
| 5 | GET | /api/categories | 公开 | 公共 |
| 6 | GET | /api/products | 公开 | 公共 |
| 7 | GET | /api/products/{id} | 公开 | 公共 |
| 8 | GET | /api/cart | USER | 用户端 |
| 9 | POST | /api/cart | USER | 用户端 |
| 10 | PUT | /api/cart/{id} | USER | 用户端 |
| 11 | DELETE | /api/cart/{id} | USER | 用户端 |
| 12 | GET | /api/addresses | USER | 用户端 |
| 13 | POST | /api/addresses | USER | 用户端 |
| 14 | DELETE | /api/addresses/{id} | USER | 用户端 |
| 15 | POST | /api/orders | USER | 用户端 |
| 16 | POST | /api/orders/{id}/pay | USER | 用户端 |
| 17 | GET | /api/orders | USER | 用户端 |
| 18 | GET | /api/orders/{id} | USER/商家/ADMIN | 用户端 |
| 19 | POST | /api/orders/{id}/cancel | USER | 用户端 |
| 20 | POST | /api/orders/{id}/confirm | USER | 用户端 |
| 21 | GET | /api/orders/{id}/logistics | USER | 用户端 |
| 22 | POST | /api/after-sales | USER | 用户端 |
| 23 | GET | /api/after-sales | USER | 用户端 |
| 24 | POST | /api/complaints | USER | 用户端 |
| 25 | GET | /api/recommendations | USER | 用户端 |
| 26 | GET | /api/products/{id}/related | 公开 | 用户端 |
| 27 | GET | /api/merchant/products | MERCHANT | 商家端 |
| 28 | POST | /api/merchant/products | MERCHANT | 商家端 |
| 29 | PUT | /api/merchant/products/{id} | MERCHANT | 商家端 |
| 30 | POST | /api/merchant/products/{id}/off-shelf | MERCHANT | 商家端 |
| 31 | GET | /api/merchant/inventory | MERCHANT | 商家端 |
| 32 | PUT | /api/merchant/inventory/{productId} | MERCHANT | 商家端 |
| 33 | GET | /api/merchant/inventory/warnings | MERCHANT | 商家端 |
| 34 | GET | /api/merchant/orders | MERCHANT | 商家端 |
| 35 | POST | /api/merchant/orders/{id}/ship | MERCHANT | 商家端 |
| 36 | GET | /api/merchant/after-sales | MERCHANT | 商家端 |
| 37 | POST | /api/merchant/after-sales/{id}/handle | MERCHANT | 商家端 |
| 38 | GET | /api/merchant/reports/sales | MERCHANT | 商家端 |
| 39 | GET | /api/merchant/products/expiring | MERCHANT | 商家端 |
| 40 | GET | /api/admin/merchants | ADMIN | 平台端 |
| 41 | POST | /api/admin/merchants/{id}/audit | ADMIN | 平台端 |
| 42 | GET | /api/admin/users | ADMIN | 平台端 |
| 43 | PUT | /api/admin/users/{id}/status | ADMIN | 平台端 |
| 44 | GET | /api/admin/complaints | ADMIN | 平台端 |
| 45 | POST | /api/admin/complaints/{id}/handle | ADMIN | 平台端 |
| 46 | GET | /api/admin/overview | ADMIN | 平台端 |

> 合计 **46 个接口**，覆盖认证、公共、用户端、商家端、平台端全部业务场景。

---

撰写成员：架构设计智能体
对应版本：v1.0
