import './styles/layout.css';
import { TabsProvider, useTabs } from './context/TabsContext';
import { Sidebar } from './components/Sidebar';
import { Topbar } from './components/Topbar';
import Dashboard from './pages/Dashboard';
import People from './pages/People';
import Organization from './pages/Organization';
import Messages from './pages/Messages';
import Calendar from './pages/Calendar';
import Settings from './pages/Settings';

function Content() {
  const { activeTab } = useTabs();
  if (activeTab === 'Dashboard') return <Dashboard />;
  if (activeTab === 'People') return <People />;
  if (activeTab === 'Organization') return <Organization />;
  if (activeTab === 'Messages') return <Messages />;
  if (activeTab === 'Calendar') return <Calendar />;
  if (activeTab === 'Settings') return <Settings />;
  return null;
}

export default function App() {
  return (
    <TabsProvider>
      <div className="app-shell">
        <Sidebar />
        <Topbar />
        <main className="content">
          <div className="content-card">
            <Content />
          </div>
        </main>
      </div>
    </TabsProvider>
  );
}
