import type { Task } from '../../types/models';
import { ReactComponent as UserAvatarIcon } from '../../icons/user-avatar.svg';

interface TaskItemProps {
  task: Task;
  onClick?: (task: Task) => void;
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

function formatDay(dateString: string): string {
  const dt = new Date(dateString);
  const dd = String(dt.getDate()).padStart(2, '0');
  const mm = String(dt.getMonth() + 1).padStart(2, '0');
  const yy = String(dt.getFullYear()).slice(-2);
  return `${dd}/${mm}/${yy}`;
}

export default function TaskItem({ task, onClick, stripColor = '#0ea5e9' }: TaskItemProps) {
  const cardBg = toRgba(stripColor, 0.075);

  return (
    <div 
      className="dash-item task-card task-card-dynamic"
      onClick={() => onClick?.(task)}
      style={{ '--task-bg-color': cardBg, '--strip-color': stripColor, '--fallback-color': stripColor } as React.CSSProperties}
    >
      <div className="meeting-strip meeting-strip-dynamic" />
      <div className="meeting-inner">
        <div className="meeting-left task-left">
          <div className="meeting-title">{task.title}</div>
          <div className="item-info-row">
            <span className="meeting-datetime">{formatDay(task.createdAt)}</span>
            <span className="separator">•</span>
            <span className={`dash-tag dash-tag--status-${task.status.toLowerCase()}`}>{task.status.replace('_', ' ')}</span>
          </div>
        </div>
        <div className="assignee-block">
          <div className="assignee-pill">
            {task.assignee?.profilePictureUrl ? (
              <img className="avatar" src={task.assignee.profilePictureUrl} alt={`${task.assignee.firstName} ${task.assignee.lastName}`} />
            ) : (
              <div className="avatar avatar-fallback avatar-fallback-dynamic">
                <UserAvatarIcon width={14} height={14} />
              </div>
            )}
            <span className="assignee-name">{task.assignee ? `${task.assignee.firstName} ${task.assignee.lastName}` : 'Admin'}</span>
          </div>
        </div>
      </div>
    </div>
  );
}


