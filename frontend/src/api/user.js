import request from './index'

// 公共接口 + 用户端接口（USER）

// 5. 分类列表（树形）
export function getCategories(params) {
  return request.get('/categories', { params })
}

// 6. 商品列表
export function getProducts(params) {
  return request.get('/products', { params })
}

// 7. 商品详情
export function getProductDetail(id) {
  return request.get('/products/' + id)
}

// 26. 相关商品推荐（公开）
export function getRelatedProducts(id, params) {
  return request.get('/products/' + id + '/related', { params })
}

// 8. 查询购物车
export function getCart() {
  return request.get('/cart')
}

// 9. 加入购物车
export function addToCart(data) {
  return request.post('/cart', data)
}

// 10. 修改购物车数量
export function updateCartItem(id, data) {
  return request.put('/cart/' + id, data)
}

// 11. 删除购物车项
export function deleteCartItem(id) {
  return request.delete('/cart/' + id)
}

// 12. 查询地址列表
export function getAddresses() {
  return request.get('/addresses')
}

// 13. 新增地址
export function addAddress(data) {
  return request.post('/addresses', data)
}

// 14. 删除地址
export function deleteAddress(id) {
  return request.delete('/addresses/' + id)
}

// 15. 创建订单
export function createOrder(data) {
  return request.post('/orders', data)
}

// 16. 支付订单
export function payOrder(id, data) {
  return request.post('/orders/' + id + '/pay', data)
}

// 17. 查询订单列表
export function getOrders(params) {
  return request.get('/orders', { params })
}

// 18. 查询订单详情
export function getOrderDetail(id) {
  return request.get('/orders/' + id)
}

// 19. 取消订单
export function cancelOrder(id) {
  return request.post('/orders/' + id + '/cancel')
}

// 20. 确认收货
export function confirmOrder(id) {
  return request.post('/orders/' + id + '/confirm')
}

// 21. 查询物流
export function getLogistics(id) {
  return request.get('/orders/' + id + '/logistics')
}

// 22. 提交售后申请
export function applyAfterSale(data) {
  return request.post('/after-sales', data)
}

// 23. 查询售后列表
export function getAfterSales(params) {
  return request.get('/after-sales', { params })
}

// 24. 提交投诉
export function submitComplaint(data) {
  return request.post('/complaints', data)
}

// 25. 个性化推荐商品
export function getRecommendations(params) {
  return request.get('/recommendations', { params })
}
