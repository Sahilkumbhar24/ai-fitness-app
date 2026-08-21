import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Container from '@mui/material/Container';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Chip from '@mui/material/Chip';
import Grid from '@mui/material/Grid';
import Alert from '@mui/material/Alert';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import LocalFireDepartmentOutlinedIcon from '@mui/icons-material/LocalFireDepartmentOutlined';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TipsAndUpdatesOutlinedIcon from '@mui/icons-material/TipsAndUpdatesOutlined';
import HealthAndSafetyOutlinedIcon from '@mui/icons-material/HealthAndSafetyOutlined';
import DirectionsRunIcon from '@mui/icons-material/DirectionsRun';
import DirectionsWalkIcon from '@mui/icons-material/DirectionsWalk';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import PoolIcon from '@mui/icons-material/Pool';
import HikingIcon from '@mui/icons-material/Hiking';
import SelfImprovementIcon from '@mui/icons-material/SelfImprovement';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import RefreshIcon from '@mui/icons-material/Refresh';
import { getActivityDetail } from '../services/api';

const SPORT_CONFIG = {
  RUNNING: { label: 'Running', icon: <DirectionsRunIcon sx={{ fontSize: 26 }} />, color: '#2563eb', bg: '#eff6ff' },
  WALKING: { label: 'Walking', icon: <DirectionsWalkIcon sx={{ fontSize: 26 }} />, color: '#059669', bg: '#ecfdf5' },
  CYCLING: { label: 'Cycling', icon: <DirectionsBikeIcon sx={{ fontSize: 26 }} />, color: '#7c3aed', bg: '#f5f3ff' },
  SWIMMING: { label: 'Swimming', icon: <PoolIcon sx={{ fontSize: 26 }} />, color: '#0284c7', bg: '#f0f9ff' },
  HIKING: { label: 'Hiking', icon: <HikingIcon sx={{ fontSize: 26 }} />, color: '#d97706', bg: '#fffbeb' },
  YOGA: { label: 'Yoga', icon: <SelfImprovementIcon sx={{ fontSize: 26 }} />, color: '#db2777', bg: '#fdf2f8' },
  OTHER: { label: 'Other', icon: <FitnessCenterIcon sx={{ fontSize: 26 }} />, color: '#475569', bg: '#f8fafc' },
};

const ActivityDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [retryCount, setRetryCount] = useState(0);

  const fetchActivityDetail = useCallback(async (isRetry = false) => {
    try {
      setLoading(true);
      setError(null);
      const response = await getActivityDetail(id);
      if (response.data) {
        setDetail(response.data);
      } else {
        throw new Error('Empty response');
      }
    } catch (err) {
      console.error('Error fetching activity detail:', err);
      // Auto-retry once after 2.5 seconds if newly created session is still processing
      if (!isRetry && retryCount < 2) {
        setRetryCount((prev) => prev + 1);
        setTimeout(() => fetchActivityDetail(true), 2500);
        return;
      }
      setError('Could not load AI recommendations. Ensure the AI service is active.');
    } finally {
      setLoading(false);
    }
  }, [id, retryCount]);

  useEffect(() => {
    if (id) {
      fetchActivityDetail();
    }
  }, [id, fetchActivityDetail]);

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 12, textAlign: 'center' }}>
        <CircularProgress size={44} sx={{ color: '#2563eb' }} />
        <Typography variant="h6" sx={{ mt: 3, fontWeight: 700, color: '#0f172a' }}>
          Generating AI Coaching Insights...
        </Typography>
        <Typography variant="body2" sx={{ mt: 0.5, color: '#64748b' }}>
          Analyzing your workout metrics and preparing recommendations.
        </Typography>
      </Container>
    );
  }

  if (error || !detail) {
    return (
      <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
        <Card sx={{ p: 4, border: '1px solid #e2e8f0', boxShadow: '0 4px 16px rgba(0,0,0,0.06)' }}>
          <Alert severity="warning" sx={{ mb: 3, borderRadius: 2, textAlign: 'left' }}>
            {error || 'Could not load AI recommendations. Ensure the AI service is active.'}
          </Alert>
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2 }}>
            <Button 
              variant="contained" 
              startIcon={<RefreshIcon />}
              onClick={() => fetchActivityDetail(true)}
              sx={{ backgroundColor: '#2563eb' }}
            >
              Retry
            </Button>
            <Button 
              variant="outlined" 
              startIcon={<ArrowBackIcon />}
              onClick={() => navigate('/activities')}
            >
              Back to Dashboard
            </Button>
          </Box>
        </Card>
      </Container>
    );
  }

  const config = SPORT_CONFIG[detail.type] || SPORT_CONFIG.OTHER;

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      {/* Back button */}
      <Button 
        variant="text" 
        startIcon={<ArrowBackIcon />}
        sx={{ mb: 3, color: '#64748b', fontWeight: 600, '&:hover': { color: '#2563eb' } }} 
        onClick={() => navigate('/activities')}
      >
        Back to Dashboard
      </Button>

      {/* Main Header / Overview Card */}
      <Card sx={{ mb: 3.5, p: 0.5, border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2, mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box 
                sx={{ 
                  width: 52, 
                  height: 52, 
                  borderRadius: '12px', 
                  bgcolor: config.bg,
                  color: config.color,
                  border: `1px solid ${config.color}30`,
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'center'
                }}
              >
                {config.icon}
              </Box>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 800, color: '#0f172a' }}>
                  {config.label} Session
                </Typography>
                {detail.createdAt && (
                  <Typography variant="caption" color="text.secondary">
                    Logged on {new Date(detail.createdAt).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })}
                  </Typography>
                )}
              </Box>
            </Box>

            <Chip 
              icon={<AutoAwesomeIcon sx={{ fontSize: '15px !important', color: '#2563eb !important' }} />}
              label="AI Coaching Report" 
              sx={{ 
                fontWeight: 700, 
                px: 1, 
                bgcolor: '#eff6ff', 
                color: '#2563eb', 
                border: '1px solid #bfdbfe' 
              }} 
            />
          </Box>

          {/* Key Metrics Strip */}
          <Grid container spacing={2}>
            {detail.duration && (
              <Grid size={{ xs: 6, sm: 4 }}>
                <Box sx={{ p: 2, borderRadius: '10px', bgcolor: '#f8fafc', border: '1px solid #f1f5f9' }}>
                  <Typography variant="caption" sx={{ color: '#64748b', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <TimerOutlinedIcon sx={{ fontSize: 15 }} /> Duration
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 800, color: '#0f172a' }}>
                    {detail.duration} mins
                  </Typography>
                </Box>
              </Grid>
            )}

            {detail.caloriesBurned && (
              <Grid size={{ xs: 6, sm: 4 }}>
                <Box sx={{ p: 2, borderRadius: '10px', bgcolor: '#f8fafc', border: '1px solid #f1f5f9' }}>
                  <Typography variant="caption" sx={{ color: '#64748b', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <LocalFireDepartmentOutlinedIcon sx={{ fontSize: 15, color: '#ea580c' }} /> Energy Burned
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 800, color: '#0f172a' }}>
                    {detail.caloriesBurned} kcal
                  </Typography>
                </Box>
              </Grid>
            )}

            <Grid size={{ xs: 12, sm: 4 }}>
              <Box sx={{ p: 2, borderRadius: '10px', bgcolor: '#f8fafc', border: '1px solid #f1f5f9' }}>
                <Typography variant="caption" sx={{ color: '#64748b' }}>
                  Sport Discipline
                </Typography>
                <Typography variant="h6" sx={{ fontWeight: 800, color: '#2563eb' }}>
                  {config.label}
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* AI Recommendations Container */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" sx={{ fontWeight: 800, color: '#0f172a', mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
          <AutoAwesomeIcon sx={{ color: '#2563eb', fontSize: 20 }} /> Coach Insights & Recommendations
        </Typography>

        {/* Primary AI Recommendation Note */}
        {detail.recommendation && (
          <Card 
            sx={{ 
              mb: 3, 
              backgroundColor: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderLeft: '4px solid #2563eb'
            }}
          >
            <CardContent sx={{ p: 3 }}>
              <Typography variant="caption" sx={{ color: '#2563eb', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.04em', display: 'block', mb: 1 }}>
                Executive Assessment
              </Typography>
              <Typography variant="body1" sx={{ color: '#334155', lineHeight: 1.7, fontSize: '15.5px' }}>
                "{detail.recommendation}"
              </Typography>
            </CardContent>
          </Card>
        )}

        {/* Structured Feedback Sections */}
        <Grid container spacing={2.5}>
          {/* Areas for Improvement */}
          {detail.improvements && detail.improvements.length > 0 && (
            <Grid size={{ xs: 12, md: 6 }}>
              <Card sx={{ height: '100%', border: '1px solid #e9d5ff', bgcolor: '#faf5ff' }}>
                <CardContent sx={{ p: 2.5 }}>
                  <Typography variant="subtitle1" sx={{ color: '#7c3aed', fontWeight: 700, mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <TrendingUpIcon sx={{ fontSize: 20 }} /> Areas for Improvement
                  </Typography>
                  <Box component="ul" sx={{ pl: 2.5, m: 0, '& li': { mb: 1, color: '#475569', lineHeight: 1.5 } }}>
                    {detail.improvements.map((item, idx) => (
                      <li key={idx}>{item}</li>
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* Actionable Suggestions */}
          {detail.suggestions && detail.suggestions.length > 0 && (
            <Grid size={{ xs: 12, md: 6 }}>
              <Card sx={{ height: '100%', border: '1px solid #bae6fd', bgcolor: '#f0f9ff' }}>
                <CardContent sx={{ p: 2.5 }}>
                  <Typography variant="subtitle1" sx={{ color: '#0284c7', fontWeight: 700, mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <TipsAndUpdatesOutlinedIcon sx={{ fontSize: 20 }} /> Actionable Suggestions
                  </Typography>
                  <Box component="ul" sx={{ pl: 2.5, m: 0, '& li': { mb: 1, color: '#475569', lineHeight: 1.5 } }}>
                    {detail.suggestions.map((item, idx) => (
                      <li key={idx}>{item}</li>
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          )}

          {/* Safety Tips */}
          {detail.safety && detail.safety.length > 0 && (
            <Grid size={{ xs: 12 }}>
              <Card sx={{ border: '1px solid #fed7aa', bgcolor: '#fff7ed' }}>
                <CardContent sx={{ p: 2.5 }}>
                  <Typography variant="subtitle1" sx={{ color: '#ea580c', fontWeight: 700, mb: 1.5, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <HealthAndSafetyOutlinedIcon sx={{ fontSize: 20 }} /> Safety & Injury Prevention
                  </Typography>
                  <Box component="ul" sx={{ pl: 2.5, m: 0, '& li': { mb: 1, color: '#475569', lineHeight: 1.5 } }}>
                    {detail.safety.map((item, idx) => (
                      <li key={idx}>{item}</li>
                    ))}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          )}
        </Grid>
      </Box>

      {/* Footer Navigation */}
      <Box sx={{ display: 'flex', justifyContent: 'center', pt: 2 }}>
        <Button 
          variant="outlined" 
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/activities')}
        >
          Return to Dashboard
        </Button>
      </Box>
    </Container>
  );
};

export default ActivityDetail;