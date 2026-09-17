import axiosClient from './axiosClient'

export const fetchMenu = (params) => axiosClient.get('/menu', { params })
export const fetchCategories = (restaurantId) => axiosClient.get('/categories', { params: { restaurantId } })
export const fetchRestaurant = (restaurantId) => axiosClient.get(`/restaurants/${restaurantId}`)
