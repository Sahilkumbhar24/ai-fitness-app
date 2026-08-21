import React from 'react';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import LocalFireDepartmentOutlinedIcon from '@mui/icons-material/LocalFireDepartmentOutlined';
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined';

const StatsOverview = ({ activities = [] }) => {
  const totalWorkouts = activities.length;
  const totalMinutes = activities.reduce((acc, cur) => acc + (Number(cur.duration) || 0), 0);
  const totalCalories = activities.reduce((acc, cur) => acc + (Number(cur.caloriesBurned) || 0), 0);

  const typeCounts = activities.reduce((acc, cur) => {
    acc[cur.type] = (acc[cur.type] || 0) + 1;
    return acc;
  }, {});

  const topActivity = Object.entries(typeCounts).sort((a, b) => b[1] - a[1])[0]?.[0] || 'None';

  const stats = [
    {
      label: 'Total Sessions',
      value: totalWorkouts,
      icon: <FitnessCenterIcon sx={{ fontSize: 20, color: '#2563eb' }} />,
      bg: '#eff6ff',
      borderColor: '#dbeafe'
    },
    {
      label: 'Active Time',
      value: `${totalMinutes} mins`,
      icon: <TimerOutlinedIcon sx={{ fontSize: 20, color: '#7c3aed' }} />,
      bg: '#f5f3ff',
      borderColor: '#ede9fe'
    },
    {
      label: 'Calories Burned',
      value: `${totalCalories.toLocaleString()} kcal`,
      icon: <LocalFireDepartmentOutlinedIcon sx={{ fontSize: 20, color: '#ea580c' }} />,
      bg: '#fff7ed',
      borderColor: '#ffedd5'
    },
    {
      label: 'Top Sport',
      value: topActivity,
      icon: <EmojiEventsOutlinedIcon sx={{ fontSize: 20, color: '#059669' }} />,
      bg: '#ecfdf5',
      borderColor: '#d1fae5'
    }
  ];

  return (
    <Grid container spacing={2.5} sx={{ mb: 4 }}>
      {stats.map((stat, idx) => (
        <Grid key={idx} size={{ xs: 6, sm: 6, md: 3 }}>
          <Card 
            sx={{ 
              p: 0.5,
              border: '1px solid #e2e8f0',
              boxShadow: '0 1px 2px rgba(0, 0, 0, 0.04)',
              transition: 'all 0.2s ease-in-out',
              '&:hover': {
                borderColor: '#cbd5e1',
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.06)'
              }
            }}
          >
            <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
                <Typography variant="caption" sx={{ color: '#64748b', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.04em', fontSize: '11px' }}>
                  {stat.label}
                </Typography>
                <Box 
                  sx={{ 
                    width: 34, 
                    height: 34, 
                    borderRadius: '8px', 
                    bgcolor: stat.bg,
                    border: `1px solid ${stat.borderColor}`,
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center'
                  }}
                >
                  {stat.icon}
                </Box>
              </Box>
              <Typography variant="h5" sx={{ fontWeight: 800, color: '#0f172a' }}>
                {stat.value}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default StatsOverview;
