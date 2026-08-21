import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import DirectionsRunIcon from '@mui/icons-material/DirectionsRun';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import HealthAndSafetyIcon from '@mui/icons-material/HealthAndSafety';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';

const LandingHero = ({ onLogin }) => {
  const features = [
    {
      icon: <DirectionsRunIcon sx={{ fontSize: 28, color: '#2563eb' }} />,
      bg: '#eff6ff',
      title: 'Precision Activity Tracking',
      desc: 'Seamlessly track duration, energy expenditure, and key metrics across running, cycling, swimming, and strength.'
    },
    {
      icon: <AutoAwesomeIcon sx={{ fontSize: 28, color: '#7c3aed' }} />,
      bg: '#f5f3ff',
      title: 'Automated AI Coaching',
      desc: 'Receive tailored workout feedback, training pacing analysis, and performance recommendations for every session.'
    },
    {
      icon: <HealthAndSafetyIcon sx={{ fontSize: 28, color: '#10b981' }} />,
      bg: '#ecfdf5',
      title: 'Recovery & Safety First',
      desc: 'Smart recovery guidelines and injury-prevention cues tailored to your workout intensity and volume.'
    }
  ];

  return (
    <Box 
      sx={{ 
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        background: 'linear-gradient(180deg, #f8fafc 0%, #edf2f7 100%)',
        py: { xs: 8, md: 12 }
      }}
    >
      <Container maxWidth="lg">
        {/* Hero Section */}
        <Box sx={{ textAlign: 'center', maxWidth: 760, mx: 'auto', mb: 8 }}>
          <Box 
            sx={{ 
              display: 'inline-flex', 
              alignItems: 'center', 
              gap: 1, 
              px: 2, 
              py: 0.8, 
              borderRadius: '20px', 
              backgroundColor: '#ffffff', 
              border: '1px solid #e2e8f0',
              boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
              mb: 3
            }}
          >
            <FitnessCenterIcon sx={{ fontSize: 16, color: '#2563eb' }} />
            <Typography variant="caption" sx={{ color: '#475569', fontWeight: 700, letterSpacing: '0.04em' }}>
              INTELLIGENT FITNESS PLATFORM
            </Typography>
          </Box>

          <Typography 
            variant="h2" 
            component="h1" 
            sx={{ 
              fontSize: { xs: '34px', sm: '48px', md: '56px' },
              fontWeight: 800,
              lineHeight: 1.15,
              mb: 2.5,
              color: '#0f172a'
            }}
          >
            Elevate Your Training with <span style={{ color: '#2563eb' }}>AI-Driven Insights</span>
          </Typography>

          <Typography 
            variant="body1" 
            sx={{ 
              color: '#64748b', 
              fontWeight: 400, 
              lineHeight: 1.6, 
              mb: 4.5,
              fontSize: { xs: '16px', md: '18px' },
              maxWidth: 620,
              mx: 'auto'
            }}
          >
            Log workouts in seconds, analyze progress across all sports, and receive personalized coaching feedback to optimize performance and recovery.
          </Typography>

          <Button 
            variant="contained" 
            size="large"
            endIcon={<ArrowForwardIcon />}
            onClick={onLogin}
            sx={{ 
              py: 1.6, 
              px: 4.5, 
              fontSize: '17px',
              fontWeight: 700,
              borderRadius: '12px',
              backgroundColor: '#2563eb',
              boxShadow: '0 4px 14px rgba(37, 99, 235, 0.3)',
              '&:hover': {
                backgroundColor: '#1d4ed8',
                boxShadow: '0 6px 20px rgba(37, 99, 235, 0.4)',
              }
            }}
          >
            Get Started & Sign In
          </Button>
        </Box>

        {/* Feature Cards Grid */}
        <Grid container spacing={3}>
          {features.map((feat, i) => (
            <Grid key={i} size={{ xs: 12, md: 4 }}>
              <Card 
                sx={{ 
                  height: '100%', 
                  p: 1.5,
                  border: '1px solid #e2e8f0',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
                  transition: 'all 0.2s',
                  '&:hover': { 
                    transform: 'translateY(-2px)',
                    boxShadow: '0 8px 20px -4px rgba(0,0,0,0.08)'
                  }
                }}
              >
                <CardContent>
                  <Box 
                    sx={{ 
                      width: 52, 
                      height: 52, 
                      borderRadius: '12px', 
                      bgcolor: feat.bg, 
                      display: 'flex', 
                      alignItems: 'center', 
                      justifyContent: 'center',
                      mb: 2.5
                    }}
                  >
                    {feat.icon}
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 700, mb: 1, color: '#0f172a' }}>
                    {feat.title}
                  </Typography>
                  <Typography variant="body2" sx={{ color: '#64748b', lineHeight: 1.6 }}>
                    {feat.desc}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Container>
    </Box>
  );
};

export default LandingHero;
