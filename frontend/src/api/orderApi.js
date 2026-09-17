import axiosClient from './axiosClient'

export const getAddresses = () => axiosClient.get('/addresses')
export const createAddress = (payload) => axiosClient.post('/addresses', payload)
export const createOrder = (addressId, couponCode) => axiosClient.post('/orders', { addressId, couponCode })
export const getMyOrders = () => axiosClient.get('/orders')
export const getOrder = (id) => axiosClient.get(`/orders/${id}`)
