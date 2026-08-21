import React, { useEffect, useContext } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { AuthContext } from 'react-oauth2-code-pkce';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Box from '@mui/material/Box';
import { SpeedInsights } from '@vercel/speed-insights/react';
import { theme } from './theme';
import { logout, setCredentials } from './store/authSlice';
import Navbar from './components/Navbar';
import LandingHero from './components/LandingHero';
import ActivityPage from './components/ActivityPage';
import ActivityDetail from './components/ActivityDetail';
import './App.css';

function App() {
  const { token, tokenData, logIn, logOut } = useContext(AuthContext);
  const dispatch = useDispatch();

  useEffect(() => {
    if (token && tokenData) {
      dispatch(
        setCredentials({
          user: tokenData,
          token: token,
          userId: tokenData.sub,
        })
      );
    }
  }, [token, tokenData, dispatch]);

  const handleLogout = () => {
    dispatch(logout());
    logOut();
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        {!token ? (
          <LandingHero onLogin={() => logIn()} />
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f8fafc' }}>
            <Navbar user={tokenData} onLogout={handleLogout} />
            <Box component="main" sx={{ flexGrow: 1 }}>
              <Routes>
                <Route path="/activities" element={<ActivityPage />} />
                <Route path="/activities/:id" element={<ActivityDetail />} />
                <Route path="/" element={<Navigate to="/activities" replace />} />
                <Route path="*" element={<Navigate to="/activities" replace />} />
              </Routes>
            </Box>
          </Box>
        )}
      </Router>
      <SpeedInsights />
    </ThemeProvider>
  );
}

export default App;
