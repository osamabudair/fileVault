/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect } from 'react';
import { INITIAL_USER } from '../api/mockData';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return localStorage.getItem('filevault_auth') === 'true';
  });

  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('filevault_user');
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    if (isAuthenticated && user) {
      localStorage.setItem('filevault_auth', 'true');
      localStorage.setItem('filevault_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('filevault_auth');
      localStorage.removeItem('filevault_user');
    }
  }, [isAuthenticated, user]);

  const login = (email, password) => {
    if (!email || !password) {
      throw new Error('Please enter both email and password.');
    }
    const loggedUser = {
      ...INITIAL_USER,
      email: email.trim(),
      name: email.split('@')[0].replace('.', ' ').replace(/^./, str => str.toUpperCase()) || INITIAL_USER.name,
      avatarInitials: (email[0] || 'U').toUpperCase()
    };
    setUser(loggedUser);
    setIsAuthenticated(true);
    return loggedUser;
  };

  const register = ({ name, email, password }) => {
    if (!name || !email || !password) {
      throw new Error('Please fill out all required fields.');
    }
    const newUser = {
      id: `usr_${Date.now().toString(36)}`,
      name: name.trim(),
      email: email.trim(),
      avatarInitials: name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() || 'U',
      storageUsed: 0,
      storageTotal: 53687091200
    };
    setUser(newUser);
    setIsAuthenticated(true);
    return newUser;
  };

  const logout = () => {
    setIsAuthenticated(false);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
