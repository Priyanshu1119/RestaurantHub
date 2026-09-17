import axiosClient from './axiosClient'

export const createPaymentOrder = (orderId) => axiosClient.post('/payments/create', { orderId })
export const verifyPayment = (payload) => axiosClient.post('/payments/verify', payload)
