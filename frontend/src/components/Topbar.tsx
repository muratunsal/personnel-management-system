import { ReactComponent as UserAvatarIcon } from '../icons/user-avatar.svg';
import { useTabs } from '../context/TabsContext';
import { useAuth } from '../context/AuthContext';
import { useEffect, useState } from 'react';
import type { Person } from '../types/models';

export function Topbar() {
  const { activeTab } = useTabs();
  const { user, token } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [me, setMe] = useState<Person | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!isAdmin && user?.email) {
        try {
          const res = await fetch(`http://localhost:8081/api/people?size=1&email=${encodeURIComponent(user.email)}`, {
            headers: { Authorization: token ? `Bearer ${token}` : '' }
          });
          if (res.ok) {
            const data = await res.json();
            const p = data?.content?.[0] as Person | undefined;
            if (p) setMe(p);
          }
        } catch {}
      } else {
        setMe(null);
      }
    };
    load();
  }, [user, isAdmin, token]);
  return (
    <div className="topbar">
      <div className="topbar-section left">
        <div className="topbar-title">{activeTab}</div>
      </div>
      <div className="topbar-section right">
        <div className="profile-button">
          <div className="avatar">
            {!isAdmin && me?.profilePictureUrl ? (
              <img src={me.profilePictureUrl} alt="avatar" />
            ) : (
              <UserAvatarIcon width={20} height={20} />
            )}
          </div>
          {isAdmin ? (
            <div className="profile-text center">
              <span className="profile-name-top">Admin</span>
            </div>
          ) : (
            <div className="profile-text">
              <span className="profile-name-top">{me ? `${me.firstName} ${me.lastName}` : ''}</span>
              <span className="profile-title-top">{me?.title?.name || ''}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
} 