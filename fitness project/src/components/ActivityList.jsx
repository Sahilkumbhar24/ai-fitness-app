import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Stack from '@mui/material/Stack';
import DirectionsRunIcon from '@mui/icons-material/DirectionsRun';
import DirectionsWalkIcon from '@mui/icons-material/DirectionsWalk';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import PoolIcon from '@mui/icons-material/Pool';
import HikingIcon from '@mui/icons-material/Hiking';
import SelfImprovementIcon from '@mui/icons-material/SelfImprovement';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import LocalFireDepartmentOutlinedIcon from '@mui/icons-material/LocalFireDepartmentOutlined';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import { getActivities } from '../services/api';

const SPORT_CONFIG = {
  RUNNING: { label: 'Running', icon: <DirectionsRunIcon sx={{ fontSize: 22 }} />, color: '#2563eb', bg: '#eff6ff' },
  WALKING: { label: 'Walking', icon: <DirectionsWalkIcon sx={{ fontSize: 22 }} />, color: '#059669', bg: '#ecfdf5' },
  CYCLING: { label: 'Cycling', icon: <DirectionsBikeIcon sx={{ fontSize: 22 }} />, color: '#7c3aed', bg: '#f5f3ff' },
  SWIMMING: { label: 'Swimming', icon: <PoolIcon sx={{ fontSize: 22 }} />, color: '#0284c7', bg: '#f0f9ff' },
  HIKING: { label: 'Hiking', icon: <HikingIcon sx={{ fontSize: 22 }} />, color: '#d97706', bg: '#fffbeb' },
  YOGA: { label: 'Yoga', icon: <SelfImprovementIcon sx={{ fontSize: 22 }} />, color: '#db2777', bg: '#fdf2f8' },
  OTHER: { label: 'Other', icon: <FitnessCenterIcon sx={{ fontSize: 22 }} />, color: '#475569', bg: '#f8fafc' },
};

const ActivityList = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedFilter, setSelectedFilter] = useState('ALL');
  const navigate = useNavigate();

  const fetchActivities = async () => {
    try {
      setLoading(true);
      const response = await getActivities();
      setActivities(response.data || []);
    } catch (error) {
      console.error('Error fetching activities:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchActivities();
  }, []);

  const filteredActivities = useMemo(() => {
    if (selectedFilter === 'ALL') return activities;
    return activities.filter((act) => act.type === selectedFilter);
  }, [activities, selectedFilter]);

  const uniqueTypes = useMemo(() => {
    const types = new Set(activities.map((a) => a.type));
    return ['ALL', ...Array.from(types)];
  }, [activities]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 8 }}>
        <CircularProgress color="primary" size={36} />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header & Filter Chips */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 1.5 }}>
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#0f172a' }}>
            Logged Activities ({filteredActivities.length})
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Select a session to view detailed coaching analysis
          </Typography>
        </Box>

        {activities.length > 0 && (
          <Stack direction="row" spacing={1} sx={{ overflowX: 'auto', pb: 0.5, maxWidth: '100%' }}>
            {uniqueTypes.map((type) => {
              const isAll = type === 'ALL';
              const label = isAll ? 'All' : (SPORT_CONFIG[type]?.label || type);

              return (
                <Chip
                  key={type}
                  label={label}
                  onClick={() => setSelectedFilter(type)}
                  variant={selectedFilter === type ? 'filled' : 'outlined'}
                  color={selectedFilter === type ? 'primary' : 'default'}
                  size="small"
                  sx={{
                    fontWeight: 600,
                    cursor: 'pointer',
                    borderColor: '#cbd5e1',
                    color: selectedFilter === type ? '#ffffff' : '#475569'
                  }}
                />
              );
            })}
          </Stack>
        )}
      </Box>

      {/* Empty State */}
      {filteredActivities.length === 0 ? (
        <Card 
          sx={{ 
            p: 5, 
            textAlign: 'center', 
            borderStyle: 'dashed',
            borderColor: '#cbd5e1',
            backgroundColor: '#ffffff'
          }}
        >
          <FitnessCenterIcon sx={{ fontSize: 40, color: '#94a3b8', mb: 1 }} />
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, color: '#0f172a' }}>
            No activities recorded
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 360, mx: 'auto' }}>
            {activities.length === 0 
              ? 'Log your first workout session using the form on the left to start receiving AI coaching insights.'
              : 'No activities match the selected filter. Try selecting "All".'}
          </Typography>
        </Card>
      ) : (
        /* Activity Cards Grid */
        <Grid container spacing={2.5}>
          {filteredActivities.map((activity, index) => {
            const config = SPORT_CONFIG[activity.type] || SPORT_CONFIG.OTHER;

            return (
              <Grid key={activity.id || index} size={{ xs: 12, sm: 6 }}>
                <Card
                  sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease-in-out',
                    border: '1px solid #e2e8f0',
                    '&:hover': {
                      transform: 'translateY(-2px)',
                      borderColor: '#94a3b8',
                      boxShadow: '0 8px 20px -4px rgba(0, 0, 0, 0.08)'
                    }
                  }}
                  onClick={() => activity.id && navigate(`/activities/${activity.id}`)}
                >
                  <CardContent sx={{ p: 2.5 }}>
                    {/* Header */}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Box
                          sx={{
                            width: 40,
                            height: 40,
                            borderRadius: '10px',
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
                          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#0f172a', lineHeight: 1.2 }}>
                            {config.label}
                          </Typography>
                          {activity.createdAt && (
                            <Typography variant="caption" color="text.secondary">
                              {new Date(activity.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                            </Typography>
                          )}
                        </Box>
                      </Box>

                      <Chip
                        icon={<AutoAwesomeIcon sx={{ fontSize: '14px !important', color: '#2563eb !important' }} />}
                        label="AI Report"
                        size="small"
                        sx={{
                          bgcolor: '#eff6ff',
                          color: '#2563eb',
                          fontWeight: 700,
                          fontSize: '11px',
                          border: '1px solid #bfdbfe'
                        }}
                      />
                    </Box>

                    {/* Metrics */}
                    <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5, mb: 2 }}>
                      <Box 
                        sx={{ 
                          p: 1.5, 
                          borderRadius: '8px', 
                          bgcolor: '#f8fafc', 
                          border: '1px solid #f1f5f9' 
                        }}
                      >
                        <Typography variant="caption" sx={{ color: '#64748b', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <TimerOutlinedIcon sx={{ fontSize: 14 }} /> Duration
                        </Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: '#0f172a' }}>
                          {activity.duration} <span style={{ fontSize: '13px', fontWeight: 500, color: '#64748b' }}>min</span>
                        </Typography>
                      </Box>

                      <Box 
                        sx={{ 
                          p: 1.5, 
                          borderRadius: '8px', 
                          bgcolor: '#f8fafc', 
                          border: '1px solid #f1f5f9' 
                        }}
                      >
                        <Typography variant="caption" sx={{ color: '#64748b', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          <LocalFireDepartmentOutlinedIcon sx={{ fontSize: 14, color: '#ea580c' }} /> Energy
                        </Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: '#0f172a' }}>
                          {activity.caloriesBurned} <span style={{ fontSize: '13px', fontWeight: 500, color: '#64748b' }}>kcal</span>
                        </Typography>
                      </Box>
                    </Box>

                    {/* Action Prompt */}
                    <Button
                      fullWidth
                      variant="text"
                      size="small"
                      endIcon={<ArrowForwardIcon sx={{ fontSize: 16 }} />}
                      sx={{
                        color: '#2563eb',
                        fontWeight: 600,
                        justifyContent: 'space-between',
                        px: 1,
                        '&:hover': {
                          bgcolor: '#eff6ff'
                        }
                      }}
                    >
                      <span>View AI Recommendations</span>
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      )}
    </Box>
  );
};

export default ActivityList;