import axios from 'axios';

// Resolve API URL dynamically:
// 1. If running on Render (fitness-frontend-*.onrender.com), route to the active production backend
// 2. Correct any domain typo (pqga -> pqge)
// 3. Fallback to env or local dev
let API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

if (typeof window !== 'undefined' && window.location.hostname.includes('onrender.com')) {
    if (!API_URL || API_URL.includes('localhost') || API_URL.includes('fitness-backend-pqga')) {
        API_URL = 'https://fitness-backend-pqge.onrender.com/api';
    }
}

if (API_URL.includes('fitness-backend-pqga')) {
    API_URL = API_URL.replace('fitness-backend-pqga', 'fitness-backend-pqge');
}

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
