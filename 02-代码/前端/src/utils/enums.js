// 全局枚举与文案映射（数字枚举 -> 中文文案）
// 严格遵循 API 接口契约与需求规格说明书的状态定义

export const ROLE = {
  USER: 'USER',
  MERCHANT: 'MERCHANT',
  ADMIN: 'ADMIN'
}

export const ROLE_TEXT = {
  USER: '普通用户',
  MERCHANT: '入驻商家',
  ADMIN: '平台管理员'
}

// 订单状态：0待支付 1待发货 2运输中 3已完成 4已取消 5售后中
export const ORDER_STATUS = {
  0: '待支付',
  1: '待发货',
  2: '运输中',
  3: '已完成',
  4: '已取消',
  5: '售后中'
}

export const ORDER_STATUS_TYPE = {
  0: 'warning',
  1: 'primary',
  2: 'primary',
  3: 'success',
  4: 'info',
  5: 'danger'
}

// 售后状态：0待审核 1审核通过 2处理中 3已完成 4已驳回
export const AFTERSALE_STATUS = {
  0: '待审核',
  1: '审核通过',
  2: '处理中',
  3: '已完成',
  4: '已驳回'
}

export const AFTERSALE_STATUS_TYPE = {
  0: 'warning',
  1: 'primary',
  2: 'primary',
  3: 'success',
  4: 'danger'
}

// 商家审核状态：0待审 1通过 2驳回
export const MERCHANT_AUDIT_STATUS = {
  0: '待审核',
  1: '审核通过',
  2: '审核驳回'
}

export const MERCHANT_AUDIT_TYPE = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

// 预警等级：0正常 1注意 2警告 3严重
export const WARNING_LEVEL = {
  0: '正常',
  1: '注意',
  2: '警告',
  3: '严重'
}

export const WARNING_LEVEL_TYPE = {
  0: 'success',
  1: 'primary',
  2: 'warning',
  3: 'danger'
}

// 售后类型：1仅退款 2退货退款
export const AFTERSALE_TYPE = {
  1: '仅退款',
  2: '退货退款'
}

// 投诉类型：1商品质量 2服务态度 3物流 4其他
export const COMPLAINT_TYPE = {
  1: '商品质量',
  2: '服务态度',
  3: '物流问题',
  4: '其他'
}

// 投诉状态：0待处理 1处理中 2已处理（3已关闭为后端兼容映射）
export const COMPLAINT_STATUS = {
  0: '待处理',
  1: '处理中',
  2: '已处理',
  3: '已关闭'
}

export const COMPLAINT_STATUS_TYPE = {
  0: 'warning',
  1: 'primary',
  2: 'success',
  3: 'success'
}

// 支付方式：1模拟微信 2模拟支付宝
export const PAY_METHOD = {
  1: '模拟微信支付',
  2: '模拟支付宝'
}

// 库存变动类型
export const INVENTORY_CHANGE_TYPE = {
  1: '采购入库',
  2: '销售出库',
  3: '损耗出库',
  4: '盘点调整'
}

// 预警类型
export const WARNING_TYPE = {
  LOW_STOCK: '库存不足',
  EXPIRING: '临期',
  EXPIRED: '过期'
}

// 推荐理由
export const RECOMMEND_REASON = {
  FREQUENCY: '基于购买频次',
  COLLABORATIVE: '猜你喜欢',
  HOT: '热销推荐'
}

// 通用：取文案
export function textOf(map, code) {
  if (map == null || code == null) return '-'
  return map[code] !== undefined ? map[code] : String(code)
}

// 通用：取标签类型
export function typeOf(map, code, defaultType = 'info') {
  if (map == null || code == null) return defaultType
  return map[code] !== undefined ? map[code] : defaultType
}

export const orderStatusText = (code) => textOf(ORDER_STATUS, code)
export const orderStatusType = (code) => typeOf(ORDER_STATUS_TYPE, code)
export const afterSaleStatusText = (code) => textOf(AFTERSALE_STATUS, code)
export const afterSaleStatusType = (code) => typeOf(AFTERSALE_STATUS_TYPE, code)
export const warningLevelText = (code) => textOf(WARNING_LEVEL, code)
export const warningLevelType = (code) => typeOf(WARNING_LEVEL_TYPE, code)
