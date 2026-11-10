import { ThemeProvider } from './context/ThemeContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { FileProvider } from './context/FileContext';
import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';

function AppContent() {
  const { isAuthenticated } = useAuth();

  return isAuthenticated ? <DashboardPage /> : <AuthPage />;
}

export function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <FileProvider>
          <AppContent />
        </FileProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
