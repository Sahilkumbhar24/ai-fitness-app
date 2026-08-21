const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8181';
const REALM = import.meta.env.VITE_KEYCLOAK_REALM || 'fitness-app';
const CLIENT_ID = import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'oauth2-pkce-client';
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI || (typeof window !== 'undefined' ? window.location.origin : 'http://localhost:5173');

export const authConfig = {
    clientId: CLIENT_ID,
    authorizationEndpoint: `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/auth`,
    tokenEndpoint: `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`,
    redirectUri: REDIRECT_URI,
    scope: 'openid profile email offline_access',
    onRefreshTokenExpire: (event) => event.logIn(),
};