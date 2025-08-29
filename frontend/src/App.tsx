import './styles/layout.css';
import { TabsProvider, useTabs } from './context/TabsContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar } from './components/Sidebar';
import { Topbar } from './components/Topbar';
import Dashboard from './pages/Dashboard';
import People from './pages/People';
import Organization from './pages/Organization';
import { DataProvider } from './context/DataContext';
import Login from './pages/Login';

function Content() {
  const { activeTab } = useTabs();
  if (activeTab === 'Dashboard') return <Dashboard />;
  if (activeTab === 'People') return <People />;
  if (activeTab === 'Organization') return <Organization />;
  return null;
}

function AppContent() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Login />;
  }

  return (
    <TabsProvider>
      <DataProvider>
        <div className="app-shell">
          <Sidebar />
          <Topbar />
          <main className="content">
            <div className="content-card">
              <Content />
            </div>
          </main>
        </div>
      </DataProvider>
    </TabsProvider>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
