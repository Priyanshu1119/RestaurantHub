import axiosClient from './axiosClient'

export const fetchEvents = (restaurantId) => axiosClient.get('/events', { params: { restaurantId, size: 20 } })
export const purchaseTicket = (eventId, quantity) => axiosClient.post(`/events/${eventId}/tickets`, { quantity })
