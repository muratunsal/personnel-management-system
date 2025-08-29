import type { Meeting, Person } from '../../types/models';
import { ReactComponent as UserAvatarIcon } from '../../icons/user-avatar.svg';

interface MeetingItemProps {
  meeting: Meeting;
  onClick?: (meeting: Meeting) => void;
  stripColor?: string;
}

function toRgba(hex: string, alpha: number): string {
  if (hex.startsWith('rgb')) return hex;
  let h = hex.replace('#', '');
  if (h.length === 3) h = h.split('').map(c => c + c).join('');
  const r = parseInt(h.substring(0, 2), 16);
  const g = parseInt(h.substring(2, 4), 16);
  const b = parseInt(h.substring(4, 6), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

export default function MeetingItem({ meeting, onClick, stripColor = '#3b82f6' }: MeetingItemProps) {
  const avatars: Person[] = (meeting.participants || []).slice(0, 3);
  const remaining = Math.max(0, (meeting.participants || []).length - avatars.length);
  const cardBg = toRgba(stripColor, 0.075);

  const formatTime = (t: string): string => {
    if (!t) return '';
    const parts = t.split(':');
    if (parts.length >= 2) return `${parts[0]}:${parts[1]}`;
    return t;
  };
  
  const formatDay = (d: string): string => {
    const dt = new Date(d);
    const dd = String(dt.getDate()).padStart(2, '0');
    const mm = String(dt.getMonth() + 1).padStart(2, '0');
    const yy = String(dt.getFullYear()).slice(-2);
    return `${dd}/${mm}/${yy}`;
  };

  return (
    <div 
      className="dash-item meeting-card meeting-card-dynamic"
      onClick={() => onClick?.(meeting)}
      style={{ '--meeting-bg-color': cardBg, '--strip-color': stripColor, '--fallback-color': stripColor } as React.CSSProperties}
    >
      <div className="meeting-strip meeting-strip-dynamic" />
      <div className="meeting-inner">
        <div className="meeting-left">
          <div className="meeting-title">{meeting.title}</div>
          <div className="meeting-datetime">{formatDay(meeting.day)} • {formatTime(meeting.startTime)} - {formatTime(meeting.endTime)}</div>
        </div>
        <div className="meeting-avatars">
          <div className="avatar-stack">
            {avatars.map((p) => (
              p.profilePictureUrl ? (
                <img key={p.id} className="avatar" src={p.profilePictureUrl} alt={`${p.firstName} ${p.lastName}`} />
              ) : (
                <div key={p.id} className="avatar avatar-fallback avatar-fallback-dynamic">
                  <UserAvatarIcon width={14} height={14} />
                </div>
              )
            ))}
          </div>
          {remaining > 0 && <span className="more-count">+{remaining}</span>}
        </div>
      </div>
    </div>
  );
}


