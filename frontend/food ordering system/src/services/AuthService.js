import api from "../api/axios";

export const register = (userData) => {
    return api.post("/auth/register", userData);
};

export const login = (credentials) => {
    return api.post("/auth/login", credentials);
};