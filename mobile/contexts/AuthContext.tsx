import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { apiRequest, ApiError } from '../services/api';
import { getAccessToken, getRefreshToken, saveTokens, clearTokens } from '../services/auth';
import { AuthResponse, Profile } from '../types/api';

type AuthState = {
  isLoading: boolean;
  isAuthenticated: boolean;
  userId: number | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string, profile: Profile) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isLoading, setIsLoading] = useState(true);
  const [token, setToken] = useState<string | null>(null);
  const [userId, setUserId] = useState<number | null>(null);

  useEffect(() => {
    loadStoredAuth();
  }, []);

  async function loadStoredAuth() {
    try {
      const stored = await getAccessToken();
      if (stored) {
        setToken(stored);
        // Try to refresh to validate the token is still good
        const refreshToken = await getRefreshToken();
        if (refreshToken) {
          try {
            const res = await apiRequest<AuthResponse>('/api/auth/refresh', {
              method: 'POST',
              body: { refreshToken },
            });
            setToken(res.accessToken);
            setUserId(res.userId);
            if (res.refreshToken) {
              await saveTokens(res.accessToken, res.refreshToken);
            }
          } catch {
            // Refresh failed — clear and go to login
            await clearTokens();
            setToken(null);
          }
        }
      }
    } finally {
      setIsLoading(false);
    }
  }

  async function login(email: string, password: string) {
    const res = await apiRequest<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: { email, password },
    });
    setToken(res.accessToken);
    setUserId(res.userId);
    await saveTokens(res.accessToken, res.refreshToken!);
  }

  async function register(email: string, password: string, displayName: string, profile: Profile) {
    const res = await apiRequest<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: { email, password, displayName, profile },
    });
    setToken(res.accessToken);
    setUserId(res.userId);
    await saveTokens(res.accessToken, res.refreshToken!);
  }

  async function logout() {
    await clearTokens();
    setToken(null);
    setUserId(null);
  }

  return (
    <AuthContext.Provider
      value={{
        isLoading,
        isAuthenticated: token !== null,
        userId,
        token,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
