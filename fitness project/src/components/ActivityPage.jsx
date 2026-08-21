import React, { useState, useEffect, useCallback } from 'react';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import StatsOverview from './StatsOverview';
import ActivityForm from './ActivityForm';
import ActivityList from './ActivityList';
import { getActivities } from '../services/api';

const ActivityPage = () => {
  const [activities, setActivities] = useState([]);
  const [refreshKey, setRefreshKey] = useState(0);

  const fetchActivities = useCallback(async () => {
    try {
      const response = await getActivities();
      setActivities(response.data || []);
    } catch (error) {
      console.error('Error fetching activities in ActivityPage:', error);
    }
  }, []);

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities, refreshKey]);

  const handleActivityAdded = () => {
    setRefreshKey((prev) => prev + 1);
  };

  return (
    <Container maxWidth="xl" sx={{ py: 4, px: { xs: 2, sm: 3, md: 4 } }}>
      {/* Dashboard Welcome & Header */}
      <Box sx={{ mb: 3.5 }}>
        <Typography 
          variant="h4" 
          component="h1" 
          sx={{ 
            fontWeight: 800, 
            letterSpacing: '-0.02em',
            color: '#0f172a',
            mb: 0.5
          }}
        >
          Activity Dashboard
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Track your daily sessions, monitor metabolic volume, and review personalized coaching insights.
        </Typography>
      </Box>

      {/* Summary Metrics Banner */}
      <StatsOverview activities={activities} />

      {/* Two Column Layout: Log Form & Activity List */}
      <Grid container spacing={3.5} alignItems="flex-start">
        <Grid size={{ xs: 12, lg: 4.5 }}>
          <ActivityForm onActivityAdded={handleActivityAdded} />
        </Grid>
        <Grid size={{ xs: 12, lg: 7.5 }}>
          <ActivityList key={refreshKey} />
        </Grid>
      </Grid>
    </Container>
  );
};

export default ActivityPage;
