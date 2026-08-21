import React, { useState } from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import InputAdornment from '@mui/material/InputAdornment';
import Alert from '@mui/material/Alert';
import DirectionsRunIcon from '@mui/icons-material/DirectionsRun';
import DirectionsWalkIcon from '@mui/icons-material/DirectionsWalk';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import PoolIcon from '@mui/icons-material/Pool';
import HikingIcon from '@mui/icons-material/Hiking';
import SelfImprovementIcon from '@mui/icons-material/SelfImprovement';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import AddIcon from '@mui/icons-material/Add';
import { addActivity } from '../services/api';

const SPORT_OPTIONS = [
  { value: 'RUNNING', label: 'Running', icon: <DirectionsRunIcon sx={{ fontSize: 20, color: '#2563eb' }} /> },
  { value: 'WALKING', label: 'Walking', icon: <DirectionsWalkIcon sx={{ fontSize: 20, color: '#059669' }} /> },
  { value: 'CYCLING', label: 'Cycling', icon: <DirectionsBikeIcon sx={{ fontSize: 20, color: '#7c3aed' }} /> },
  { value: 'SWIMMING', label: 'Swimming', icon: <PoolIcon sx={{ fontSize: 20, color: '#0284c7' }} /> },
  { value: 'HIKING', label: 'Hiking', icon: <HikingIcon sx={{ fontSize: 20, color: '#d97706' }} /> },
  { value: 'YOGA', label: 'Yoga', icon: <SelfImprovementIcon sx={{ fontSize: 20, color: '#db2777' }} /> },
  { value: 'OTHER', label: 'Other', icon: <FitnessCenterIcon sx={{ fontSize: 20, color: '#475569' }} /> },
];

const ActivityForm = ({ onActivityAdded }) => {
  const [activity, setActivity] = useState({
    type: 'RUNNING',
    duration: '',
    caloriesBurned: '',
    additionalMetrics: {}
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!activity.duration || !activity.caloriesBurned) {
      setError('Please provide both duration and calories burned.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await addActivity({
        ...activity,
        duration: Number(activity.duration),
        caloriesBurned: Number(activity.caloriesBurned)
      });
      
      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);

      setActivity({
        type: 'RUNNING',
        duration: '',
        caloriesBurned: '',
        additionalMetrics: {}
      });

      if (onActivityAdded) {
        onActivityAdded();
      }
    } catch (err) {
      console.error('Error adding activity:', err);
      setError(err.response?.data?.message || 'Failed to record workout. Please ensure services are active.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card 
      sx={{ 
        position: 'sticky', 
        top: 90, 
        border: '1px solid #e2e8f0',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.05)',
        p: 1
      }}
    >
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2.5 }}>
          <Box 
            sx={{ 
              width: 36, 
              height: 36, 
              borderRadius: '8px', 
              bgcolor: '#eff6ff',
              border: '1px solid #dbeafe',
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center'
            }}
          >
            <AddIcon sx={{ color: '#2563eb', fontSize: 20 }} />
          </Box>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 700, lineHeight: 1.2, color: '#0f172a' }}>
              Log Workout
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Record metrics to generate AI insights
            </Typography>
          </Box>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2.5, borderRadius: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" sx={{ mb: 2.5, borderRadius: 2 }}>
            Workout recorded successfully!
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit} noValidate>
          {/* Sport Selector */}
          <FormControl fullWidth sx={{ mb: 2.5 }}>
            <InputLabel id="activity-type-label">Sport / Activity</InputLabel>
            <Select
              labelId="activity-type-label"
              value={activity.type}
              label="Sport / Activity"
              onChange={(e) => setActivity({ ...activity, type: e.target.value })}
            >
              {SPORT_OPTIONS.map((opt) => (
                <MenuItem key={opt.value} value={opt.value} sx={{ py: 1.2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    {opt.icon}
                    <span style={{ fontWeight: 500 }}>{opt.label}</span>
                  </Box>
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {/* Duration Input */}
          <TextField
            fullWidth
            label="Duration"
            type="number"
            placeholder="e.g. 45"
            slotProps={{
              input: {
                endAdornment: <InputAdornment position="end">mins</InputAdornment>,
              },
              htmlInput: { min: 1 }
            }}
            sx={{ mb: 2.5 }}
            value={activity.duration}
            onChange={(e) => setActivity({ ...activity, duration: e.target.value })}
          />

          {/* Calories Input */}
          <TextField
            fullWidth
            label="Calories Burned"
            type="number"
            placeholder="e.g. 350"
            slotProps={{
              input: {
                endAdornment: <InputAdornment position="end">kcal</InputAdornment>,
              },
              htmlInput: { min: 1 }
            }}
            sx={{ mb: 3 }}
            value={activity.caloriesBurned}
            onChange={(e) => setActivity({ ...activity, caloriesBurned: e.target.value })}
          />

          {/* Submit Button */}
          <Button
            type="submit"
            variant="contained"
            fullWidth
            disabled={loading}
            size="large"
            sx={{
              py: 1.4,
              fontSize: '15px',
              fontWeight: 700,
              backgroundColor: '#2563eb',
              boxShadow: '0 2px 6px rgba(37, 99, 235, 0.25)',
              '&:hover': {
                backgroundColor: '#1d4ed8'
              }
            }}
          >
            {loading ? <CircularProgress size={22} color="inherit" /> : 'Record Activity'}
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

export default ActivityForm;