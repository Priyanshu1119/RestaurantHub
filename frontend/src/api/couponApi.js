import axiosClient from './axiosClient'

export const validateCoupon = (code, restaurantId, subtotal) =>
  axiosClient.post('/coupons/validate', { code, restaurantId, subtotal })
