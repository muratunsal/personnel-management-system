
import type { Meeting } from '../../types/models';
import { ReactComponent as ChevronRightIcon } from '../../icons/chevron-right.svg';
import MeetingItem from './MeetingItem';

interface DashMeetingListProps {
  meetings: Meeting[];
  title: string;
  onMeetingClick: (meeting: Meeting) => void;
  onViewAll?: (meetings: Meeting[], title: string) => void;
  getMeetingColor?: (meeting: Meeting) => string;
}

export default function DashMeetingList({ meetings, title, onMeetingClick, onViewAll, getMeetingColor }: DashMeetingListProps) {



  return (
    <div className="dash-section-card">
      <div className="dash-section-header" onClick={() => {
        if (onViewAll) {
          onViewAll(meetings, title);
        }
      }}>
        <h3 className="dash-section-title">{title}</h3>
        <button className="dash-section-header-button" onClick={(e) => { 
          e.stopPropagation(); 
          if (onViewAll) {
            onViewAll(meetings, title);
          }
        }}>
          View All
          <ChevronRightIcon width={16} height={16} />
        </button>
      </div>
      <div className="dash-meetings-list">
        {meetings.slice(0,5).length > 0 ? (
          meetings.slice(0,5).map((meeting, index) => (
            <MeetingItem 
              key={meeting.id} 
              meeting={meeting} 
              onClick={onMeetingClick} 
              stripColor={getMeetingColor ? getMeetingColor(meeting) : ['#3b82f6', '#f59e0b', '#10b981', '#8b5cf6', '#ef4444'][index % 5]} 
            />
          ))
        ) : (
          <div className="empty-state">No meetings scheduled</div>
        )}
      </div>
    </div>
  );
}
