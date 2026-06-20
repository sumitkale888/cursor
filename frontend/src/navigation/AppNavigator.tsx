import React from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {useAuth} from '../context/AuthContext';
import {AuthNavigator} from './AuthNavigator';
import {MainNavigator} from './MainNavigator';

export function AppNavigator() {
  const {isAuthenticated, isLoading} = useAuth();

  if (isLoading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <NavigationContainer>
      {isAuthenticated ? <MainNavigator /> : <AuthNavigator />}
    </NavigationContainer>
  );
}
