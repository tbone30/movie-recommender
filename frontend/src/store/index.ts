import { configureStore } from '@reduxjs/toolkit';
import { persistStore, persistReducer } from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import authReducer from './authSlice';
import movieReducer from './movieSlice';
import recommendationReducer from './recommendationSlice';

// Persist configuration for auth state
const authPersistConfig = {
  key: 'auth',
  storage,
  whitelist: ['user', 'token'],
};

// Persist configuration for movies (cache frequently accessed data)
const moviePersistConfig = {
  key: 'movies',
  storage,
  whitelist: ['genres', 'popularMovies'],
};

const store = configureStore({
  reducer: {
    auth: persistReducer(authPersistConfig, authReducer),
    movies: persistReducer(moviePersistConfig, movieReducer),
    recommendations: recommendationReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    }),
});

export const persistor = persistStore(store);
export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export default store;