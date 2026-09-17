import axiosClient from './axiosClient'

export const sendChatMessage = (restaurantId, message, sessionId) =>
  axiosClient.post('/customer-care/chat', { restaurantId, message, sessionId })
