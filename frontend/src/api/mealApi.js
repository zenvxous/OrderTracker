import apiClient from "./client";

export const fetchMeals = async () => {
  const response = await apiClient.get("/meals");
  return response.data;
};

export const fetchMealById = async (id) => {
  const response = await apiClient.get(`/meals/${id}`);
  return response.data;
};

export const fetchMealByName = async (name) => {
  const response = await apiClient.get(`/meals/name?name=${name}`);
  return response.data;
};

export const createMeal = async (meal) => {
  const response = await apiClient.post("/meals", meal);
  return response.data;
};

export const updateMeal = async (id, meal) => {
  const response = await apiClient.put(`/meals/${id}`, meal);
  return response.data;
};

export const deleteMeal = async (id) => {
  await apiClient.delete(`/meals/${id}`);
};
