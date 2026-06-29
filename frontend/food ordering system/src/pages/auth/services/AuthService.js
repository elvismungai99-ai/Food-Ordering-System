// src/services/AuthService.js

import api from "../api/axios";

export const login = async (loginData) => {

    const response = await api.post(
        "/auth/login",
        loginData
    );

    const data = response.data;

    // Save authentication data
    localStorage.setItem("token", data.token);
    localStorage.setItem("userId", data.userId);
    localStorage.setItem("role", data.role);
    localStorage.setItem("firstName", data.firstName);

    return data;
};

export const register = async (registerData) => {

    const response = await api.post(
        "/auth/register",
        registerData
    );

    return response.data;
};

export const logout = async () => {

    // Remove stored authentication data
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("firstName");

    const response = await api.post("/auth/logout");

    return response.data;
};

export const getAuthRole = async () => {

    const response = await api.get("/auth/role");

    return response.data;
};