import { ReactComponent as BellIcon } from '../icons/bell.svg';
import { ReactComponent as SearchIcon } from '../icons/search.svg';
import { useTabs } from '../context/TabsContext';

export function Topbar() {
  const { activeTab } = useTabs();
  return (
    <div className="topbar">
      <div className="topbar-section left">
        <div className="topbar-title">{activeTab}</div>
      </div>
      <div className="topbar-section right">
        <div className="search-wrapper">
          <span className="search-icon"><SearchIcon width={18} height={18} /></span>
          <input className="search-input" placeholder="Search..." />
        </div>
        <button className="icon-button" aria-label="Notifications"><BellIcon width={20} height={20} /></button>
        <div className="profile-button">
          <div className="avatar" />
          <span className="profile-name">John Doe</span>
        </div>
      </div>
    </div>
  );
} 