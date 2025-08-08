import { useTabs } from '../context/TabsContext';
import { ReactComponent as DashboardIcon } from '../icons/dashboard.svg';
import { ReactComponent as UsersIcon } from '../icons/users.svg';
import { ReactComponent as OrganizationIcon } from '../icons/organization.svg';
import { ReactComponent as MessagesIcon } from '../icons/messages.svg';
import { ReactComponent as CalendarIcon } from '../icons/calendar.svg';
import { ReactComponent as SettingsIcon } from '../icons/settings.svg';
import { ReactComponent as LogoIcon } from '../icons/logo.svg';
import { ReactComponent as LogoutIcon } from '../icons/logout.svg';

export function Sidebar() {
  const { activeTab, setActiveTab } = useTabs();

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

        <button
          className={`tab-button ${activeTab === 'Messages' ? 'active' : ''}`}
          onClick={() => setActiveTab('Messages')}
          type="button"
        >
          <MessagesIcon/>
          <span>Messages</span>
        </button>

        <button
          className={`tab-button ${activeTab === 'Calendar' ? 'active' : ''}`}
          onClick={() => setActiveTab('Calendar')}
          type="button"
        >
          <CalendarIcon/>
          <span>Calendar</span>
        </button>
      </nav>

      <div className="sidebar-footer">
        <button
          className={`tab-button ${activeTab === 'Settings' ? 'active' : ''}`}
          onClick={() => setActiveTab('Settings')}
          type="button"
        >
          <SettingsIcon/>
          <span>Settings</span>
        </button>

        <button
          className="logout-button"
          onClick={() => {}}
          type="button"
        >
          <LogoutIcon/>
          <span>Log out</span>
        </button>
      </div>
    </aside>
  );
} 