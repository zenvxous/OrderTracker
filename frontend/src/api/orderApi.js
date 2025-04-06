import apiClient from "./client";

export const fetchOrders = async () => {
  const response = await apiClient.get("/orders");
  return response.data;
};

export const fetchOrderById = async (id) => {
  const response = await apiClient.get(`/orders/${id}`);
  return response.data;
};

export const createOrder = async (customerId) => {
  const response = await apiClient.post("/orders?customerId=" + customerId);
  return response.data;
};

export const updateOrderStatus = async (id, status) => {
  const response = await apiClient.put(`/orders/${id}/status?status=${status}`);
  return response.data;
};

export const addMealToOrder = async (orderId, mealId) => {
  const response = await apiClient.put(
    `/orders/${orderId}/meals?mealId=${mealId}`
  );
  return response.data;
};

export const deleteOrder = async (id) => {
  await apiClient.delete(`/orders/${id}`);
};  

export const removeMealFromOrder = async (orderId, mealId) => {
  await apiClient.delete(`/orders/${orderId}/meals?mealId=${mealId}`);
};

export const addMultipleMealsToOrder = async (orderId, mealIds) => {
  const response = await apiClient.put(
    `/orders/${orderId}/meals/bulk`,
    { mealIds },
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  return response.data;
};