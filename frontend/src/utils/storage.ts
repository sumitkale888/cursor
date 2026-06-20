import AsyncStorage from '@react-native-async-storage/async-storage';
import {AuthResponse, User} from '../types';

const TOKEN_KEY = '@blinkit_token';
const USER_KEY = '@blinkit_user';

export async function saveAuth(auth: AuthResponse): Promise<void> {
  const user: User = {
    userId: auth.userId,
    email: auth.email,
    firstName: auth.firstName,
    lastName: auth.lastName,
  };
  await AsyncStorage.setItem(TOKEN_KEY, auth.token);
  await AsyncStorage.setItem(USER_KEY, JSON.stringify(user));
}

export async function getToken(): Promise<string | null> {
  return AsyncStorage.getItem(TOKEN_KEY);
}

export async function getUser(): Promise<User | null> {
  const raw = await AsyncStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  return JSON.parse(raw) as User;
}

export async function clearAuth(): Promise<void> {
  await AsyncStorage.removeItem(TOKEN_KEY);
  await AsyncStorage.removeItem(USER_KEY);
}
