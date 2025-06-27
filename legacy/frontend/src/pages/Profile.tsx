import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardContent,
  TextField,
  Button,
  Alert,
  CircularProgress,
  Chip,
  LinearProgress,
  Divider,
  List,
  ListItem,
  ListItemText,
  Avatar,
  Paper,
} from '@mui/material';
import { 
  Person, 
  Movie, 
  Sync as SyncIcon,
  CheckCircle,
  Error,
  Schedule 
} from '@mui/icons-material';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import { userApi } from '../services/userApi';
import { SyncStatus, UserStats } from '../types';

const Profile: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { user } = useSelector((state: RootState) => state.auth);
  
  const [letterboxdUsername, setLetterboxdUsername] = useState('');
  const [syncStatus, setSyncStatus] = useState<SyncStatus | null>(null);
  const [lastSyncDate, setLastSyncDate] = useState<string | null>(null);
  const [userStats, setUserStats] = useState<UserStats | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isSyncing, setIsSyncing] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    loadUserData();
  }, []);

  const loadUserData = async () => {
    setIsLoading(true);
    try {
      // Load user stats
      const stats = await userApi.getStats();
      setUserStats(stats);

      // Load Letterboxd status
      try {
        const status = await userApi.getLetterboxdStatus();
        setSyncStatus(status.sync_status);
        setLastSyncDate(status.last_sync_date);
        setLetterboxdUsername(status.letterboxd_username || '');
      } catch (error) {
        // User hasn't connected Letterboxd yet
        setSyncStatus(null);
      }
    } catch (error) {
      console.error('Error loading user data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLetterboxdSync = async () => {
    if (!letterboxdUsername.trim()) {
      setMessage({ type: 'error', text: 'Please enter your Letterboxd username' });
      return;
    }

    setIsSyncing(true);
    setMessage(null);

    try {
      const response = await userApi.syncLetterboxd(letterboxdUsername);
      setMessage({ type: 'success', text: response.message });
      setSyncStatus(SyncStatus.IN_PROGRESS);
      
      // Poll for sync status updates
      pollSyncStatus();
    } catch (error: any) {
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || 'Failed to start Letterboxd sync' 
      });
    } finally {
      setIsSyncing(false);
    }
  };

  const pollSyncStatus = () => {
    const interval = setInterval(async () => {
      try {
        const status = await userApi.getLetterboxdStatus();
        setSyncStatus(status.sync_status);
        setLastSyncDate(status.last_sync_date);

        if (status.sync_status === SyncStatus.COMPLETED || status.sync_status === SyncStatus.FAILED) {
          clearInterval(interval);
          if (status.sync_status === SyncStatus.COMPLETED) {
            setMessage({ type: 'success', text: 'Letterboxd sync completed successfully!' });
            loadUserData(); // Refresh user stats
          } else {
            setMessage({ type: 'error', text: 'Letterboxd sync failed. Please try again.' });
          }
        }
      } catch (error) {
        clearInterval(interval);
      }
    }, 3000);

    // Clear interval after 5 minutes
    setTimeout(() => clearInterval(interval), 300000);
  };

  const getSyncStatusIcon = (status: SyncStatus) => {
    switch (status) {
      case SyncStatus.COMPLETED:
        return <CheckCircle color="success" />;
      case SyncStatus.FAILED:
        return <Error color="error" />;
      case SyncStatus.IN_PROGRESS:
        return <Schedule color="info" />;
      default:
        return <Schedule color="disabled" />;
    }
  };

  const getSyncStatusColor = (status: SyncStatus) => {
    switch (status) {
      case SyncStatus.COMPLETED:
        return 'success';
      case SyncStatus.FAILED:
        return 'error';
      case SyncStatus.IN_PROGRESS:
        return 'info';
      default:
        return 'default';
    }
  };

  if (isLoading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Box display="flex" justifyContent="center" py={8}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h3" component="h1" gutterBottom>
        Profile
      </Typography>

      <Grid container spacing={4}>
        {/* User Information */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={2}>
                <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>
                  <Person />
                </Avatar>
                <Box>
                  <Typography variant="h6">{user?.username}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {user?.email}
                  </Typography>
                </Box>
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="subtitle2" gutterBottom>
                Account Information
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Member since: {new Date(user?.createdAt || '').toLocaleDateString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Letterboxd Integration */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Letterboxd Integration
              </Typography>
              
              {message && (
                <Alert severity={message.type} sx={{ mb: 2 }}>
                  {message.text}
                </Alert>
              )}

              <Box mb={3}>
                <TextField
                  fullWidth
                  label="Letterboxd Username"
                  value={letterboxdUsername}
                  onChange={(e) => setLetterboxdUsername(e.target.value)}
                  placeholder="Enter your Letterboxd username"
                  helperText="We'll import your movie ratings and watchlist to generate personalized recommendations"
                />
              </Box>

              <Button
                variant="contained"
                startIcon={<SyncIcon />}
                onClick={handleLetterboxdSync}
                disabled={isSyncing || syncStatus === SyncStatus.IN_PROGRESS}
                sx={{ mb: 2 }}
              >
                {isSyncing ? 'Starting Sync...' : 
                 syncStatus === SyncStatus.IN_PROGRESS ? 'Syncing...' : 
                 'Sync Letterboxd Data'}
              </Button>

              {syncStatus && (
                <Box>
                  <Box display="flex" alignItems="center" mb={1}>
                    {getSyncStatusIcon(syncStatus)}
                    <Chip
                      label={syncStatus.replace('_', ' ')}
                      color={getSyncStatusColor(syncStatus) as any}
                      size="small"
                      sx={{ ml: 1 }}
                    />
                  </Box>
                  
                  {syncStatus === SyncStatus.IN_PROGRESS && (
                    <LinearProgress sx={{ mb: 2 }} />
                  )}
                  
                  {lastSyncDate && (
                    <Typography variant="body2" color="text.secondary">
                      Last sync: {new Date(lastSyncDate).toLocaleString()}
                    </Typography>
                  )}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* User Statistics */}
        {userStats && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Your Movie Statistics
                </Typography>
                
                <Grid container spacing={3}>
                  <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="primary">
                        {userStats.totalRatings}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Total Ratings
                      </Typography>
                    </Paper>
                  </Grid>
                  
                  <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="primary">
                        {userStats.averageRating.toFixed(1)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Average Rating
                      </Typography>
                    </Paper>
                  </Grid>
                  
                  <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="primary">
                        {userStats.recommendationCount}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Recommendations
                      </Typography>
                    </Paper>
                  </Grid>
                  
                  <Grid item xs={6} sm={3}>
                    <Paper sx={{ p: 2, textAlign: 'center' }}>
                      <Typography variant="h4" color="primary">
                        {userStats.favoriteGenres.length}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Favorite Genres
                      </Typography>
                    </Paper>
                  </Grid>
                </Grid>

                {/* Favorite Genres */}
                {userStats.favoriteGenres.length > 0 && (
                  <Box mt={3}>
                    <Typography variant="subtitle1" gutterBottom>
                      Your Favorite Genres
                    </Typography>
                    <Box>
                      {userStats.favoriteGenres.slice(0, 5).map((genre, index) => (
                        <Chip
                          key={genre}
                          label={`${index + 1}. ${genre}`}
                          sx={{ mr: 1, mb: 1 }}
                          color={index < 3 ? 'primary' : 'default'}
                        />
                      ))}
                    </Box>
                  </Box>
                )}

                {/* Recent Activity */}
                {userStats.recentActivity.length > 0 && (
                  <Box mt={3}>
                    <Typography variant="subtitle1" gutterBottom>
                      Recent Activity
                    </Typography>
                    <List dense>
                      {userStats.recentActivity.slice(0, 5).map((rating) => (
                        <ListItem key={rating.id}>
                          <ListItemText
                            primary={rating.movie?.title}
                            secondary={
                              <Box component="span">
                                <Chip
                                  label={`⭐ ${rating.rating}/10`}
                                  size="small"
                                  sx={{ mr: 1 }}
                                />
                                {new Date(rating.createdAt).toLocaleDateString()}
                              </Box>
                            }
                          />
                        </ListItem>
                      ))}
                    </List>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Container>
  );
};

export default Profile;