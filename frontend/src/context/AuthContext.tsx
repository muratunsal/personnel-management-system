import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import axios from 'axios';

interface User {
  email: string;
  role: string;
  departmentId?: number;
  departmentName?: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: boolean;
  getUserDetails: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const setupAxiosInterceptor = useCallback((authToken: string) => {
    axios.interceptors.request.use(
      (config) => {
        if (authToken) {
          config.headers.Authorization = `Bearer ${authToken}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );
  }, []);

  const removeAxiosInterceptor = useCallback(() => {
    axios.interceptors.request.clear();
  }, []);

  const validateToken = useCallback(async () => {
    try {
      const response = await fetch('http://localhost:8082/auth/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `token=${token}`,
      });

      if (response.ok) {
        const data = await response.json();
        setUser({ email: data.email, role: data.role });
        if (token) {
          getUserDetails();
        }
      } else {
        logout();
      }
    } catch (error) {
      logout();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const getUserDetails = useCallback(async () => {
    if (!token || !user?.email) return;
    
    try {
      const response = await fetch(`http://localhost:8081/api/people?size=1&email=${encodeURIComponent(user.email)}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      if (response.ok) {
        const data = await response.json();
        const userContent = Array.isArray(data) ? data : (data?.content ?? []);
        
        if (userContent.length > 0) {
          const userPerson = userContent[0];
          setUser(prev => ({
            ...prev!,
            departmentId: userPerson.department?.id,
            departmentName: userPerson.department?.name
          }));
        }
      }
    } catch (error) {
      console.error('Error loading user details:', error);
    }
  }, [token, user?.email]);

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      const response = await fetch('http://localhost:8082/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        setToken(data.token);
        setUser({ email: data.email, role: data.role });
        localStorage.setItem('token', data.token);
        return true;
      }
      return false;
    } catch (error) {
      return false;
    }
  };

  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('token');
    removeAxiosInterceptor();
  }, [removeAxiosInterceptor]);

  const isAuthenticated = !!user && !!token;

  useEffect(() => {
    if (token) {
      validateToken();
      setupAxiosInterceptor(token);
    } else {
      removeAxiosInterceptor();
    }
  }, [token, validateToken, setupAxiosInterceptor, removeAxiosInterceptor]);

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated, getUserDetails }}>
      {children}
    </AuthContext.Provider>
  );
};
