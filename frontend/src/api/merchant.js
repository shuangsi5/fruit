import request from './index'

// 商家端接口（MERCHANT）

// 27. 商家商品列表
export function getMerchantProducts(params) {
  return request.get('/merchant/products', { params })
}

// 28. 上架商品
export function createProduct(data) {
  return request.post('/merchant/products', data)
}

// 29. 修改商品
export function updateProduct(id, data) {
  return request.put('/merchant/products/' + id, data)
}

// 30. 下架商品
export function offShelfProduct(id) {
  return request.post('/merchant/products/' + id + '/off-shelf')
}

// 31. 查询库存列表
export function getInventory(params) {
  return request.get('/merchant/inventory', { params })
}

// 32. 更新库存
export function updateInventory(productId, data) {
  return request.put('/merchant/inventory/' + productId, data)
}

// 33. 库存与临期预警查询
export function getInventoryWarnings(params) {
  return request.get('/merchant/inventory/warnings', { params })
}

// 34. 商家订单列表
export function getMerchantOrders(params) {
  return request.get('/merchant/orders', { params })
}

// 35. 订单发货
export function shipOrder(id, data) {
  return request.post('/merchant/orders/' + id + '/ship', data)
}

// 36. 商家售后列表
export function getMerchantAfterSales(params) {
  return request.get('/merchant/after-sales', { params })
}

// 37. 处理售后
export function handleAfterSale(id, data) {
  return request.post('/merchant/after-sales/' + id + '/handle', data)
}

// 38. 销售报表
export function getSalesReport(params) {
  return request.get('/merchant/reports/sales', { params })
}

// 39. 临期商品查询
export function getExpiringProducts(params) {
  return request.get('/merchant/products/expiring', { params })
}
