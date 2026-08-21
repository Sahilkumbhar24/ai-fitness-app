import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.request.use((config) => {
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    if (userId) {
        config.headers['X-User-Id'] = userId;
    }
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
});

export const getActivities = () => api.get('/activities');

export const addActivity = (activity) => api.post('/activities', activity);

export const getActivityDetail = (id) => api.get(`/recommendations/activity/${id}`);

export default api;
