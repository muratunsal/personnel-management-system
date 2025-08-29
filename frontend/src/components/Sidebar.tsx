import { useTabs } from '../context/TabsContext';
import { useAuth } from '../context/AuthContext';
import { ReactComponent as DashboardIcon } from '../icons/dashboard.svg';
import { ReactComponent as UsersIcon } from '../icons/users.svg';
import { ReactComponent as OrganizationIcon } from '../icons/organization.svg';
import { ReactComponent as LogoIcon } from '../icons/logo.svg';
import { ReactComponent as LogoutIcon } from '../icons/logout.svg';

export function Sidebar() {
  const { activeTab, setActiveTab } = useTabs();
  const { logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-logo" aria-label="EMS Logo">
        <LogoIcon height={60} width={60} />
      </div>

      <nav className="sidebar-tabs">
        <button
          className={`tab-button ${activeTab === 'Dashboard' ? 'active' : ''}`}
          onClick={() => setActiveTab('Dashboard')}
          type="button"
        >
          <DashboardIcon/>
          <span>Dashboard</span>
        </button>

        <button
          className={`tab-button ${activeTab === 'People' ? 'active' : ''}`}
          onClick={() => setActiveTab('People')}
          type="button"
        >
          <UsersIcon/>
          <span>People</span>
        </button>

        <button
          className={`tab-button ${activeTab === 'Organization' ? 'active' : ''}`}
          onClick={() => setActiveTab('Organization')}
          type="button"
        >
          <OrganizationIcon/>
          <span>Organization</span>
        </button>

      </nav>

      <div className="sidebar-footer">
        <button
          className="logout-button"
          onClick={logout}
          type="button"
        >
          <LogoutIcon/>
          <span>Log out</span>
        </button>
      </div>
    </aside>
  );
} 