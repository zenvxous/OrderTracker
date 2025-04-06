import apiClient from "./client";

export const fetchCustomers = async () => {
  const response = await apiClient.get("/customers");
  return response.data;
};

export const fetchCustomerById = async (id) => {
  const response = await apiClient.get(`/customers/${id}`);
  return response.data;
};

export const fetchCustomerByName = async (name) => {
  const response = await apiClient.get(`/customers/name/${name}`);
  return response.data;
};

export const fetchCustomerByPhone = async (phone) => {
  const response = await apiClient.get(`/customers/phone/${phone}`);
  return response.data;
};

export const createCustomer = async (customer) => {
  const response = await apiClient.post("/customers", customer);
  return response.data;
};

export const updateCustomer = async (id, customer) => {
  const response = await apiClient.put(`/customers/${id}`, customer);
  return response.data;
};

export const deleteCustomer = async (id) => {
  await apiClient.delete(`/customers/${id}`);
};

export const fetchCustomerOrders = async (customerId) => {
  const response = await apiClient.get(`/customers/${customerId}/orders`);
  return response.data;
};

export const createOrderForCustomer = async (customerId, mealIds) => {
  const response = await apiClient.post(`/customers/${customerId}/orders`, {
    mealIds,
  });
  return response.data;
};