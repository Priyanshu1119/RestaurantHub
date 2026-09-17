import axiosClient from './axiosClient'

export const getCart = () => axiosClient.get('/cart')
export const addToCart = (menuItemId, quantity = 1) => axiosClient.post('/cart/items', { menuItemId, quantity })
export const updateCartItem = (itemId, quantity) => axiosClient.put(`/cart/items/${itemId}`, { quantity })
export const removeCartItem = (itemId) => axiosClient.delete(`/cart/items/${itemId}`)
export const clearCart = () => axiosClient.delete('/cart')
