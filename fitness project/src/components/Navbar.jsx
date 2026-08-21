import React from 'react';
import { useNavigate } from 'react-router-dom';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Avatar from '@mui/material/Avatar';
import Chip from '@mui/material/Chip';
import Container from '@mui/material/Container';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import LogoutIcon from '@mui/icons-material/Logout';

const Navbar = ({ user, onLogout }) => {
  const navigate = useNavigate();
  const userName = user?.name || user?.preferred_username || (user?.sub ? `User ${user.sub.slice(0, 5)}` : 'Athlete');

  return (
    <AppBar 
      position="sticky" 
      elevation={0}
      sx={{ 
        backgroundColor: '#ffffff', 
        borderBottom: '1px solid #e2e8f0',
        color: '#0f172a'
      }}
    >
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ justifyContent: 'space-between', minHeight: 68 }}>
          {/* Brand Logo */}
          <Box 
            onClick={() => navigate('/activities')} 
            sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 1.5, 
              cursor: 'pointer',
              userSelect: 'none'
            }}
          >
            <Box 
              sx={{ 
                width: 38, 
                height: 38, 
                borderRadius: '10px', 
                backgroundColor: '#2563eb',
                color: '#ffffff',
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                boxShadow: '0 2px 6px rgba(37, 99, 235, 0.3)'
              }}
            >
              <FitnessCenterIcon sx={{ fontSize: 22 }} />
            </Box>
            <Box>
              <Typography 
                variant="h6" 
                sx={{ 
                  fontWeight: 800, 
                  letterSpacing: '-0.02em',
                  lineHeight: 1.1,
                  color: '#0f172a'
                }}
              >
                FitAI
              </Typography>
              <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600, fontSize: '11px', letterSpacing: '0.04em' }}>
                ACTIVITY & COACHING
              </Typography>
            </Box>
          </Box>

          {/* User Profile & Actions */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Chip
              avatar={<Avatar sx={{ bgcolor: '#eff6ff', color: '#2563eb', fontWeight: 700 }}>{userName.charAt(0).toUpperCase()}</Avatar>}
              label={userName}
              variant="outlined"
              sx={{ 
                borderColor: '#e2e8f0',
                color: '#334155',
                fontWeight: 600,
                py: 2,
                px: 0.5,
                bgcolor: '#f8fafc'
              }}
            />
            <Button 
              variant="outlined" 
              color="inherit"
              size="small"
              startIcon={<LogoutIcon sx={{ fontSize: 18 }} />}
              onClick={onLogout}
              sx={{ 
                borderColor: '#e2e8f0',
                color: '#64748b',
                fontWeight: 600,
                '&:hover': {
                  borderColor: '#ef4444',
                  color: '#ef4444',
                  backgroundColor: '#fef2f2'
                }
              }}
            >
              Log Out
            </Button>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default Navbar;
