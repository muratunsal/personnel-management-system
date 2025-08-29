import type { Task, Meeting } from '../../types/models';
import DashTaskList from './DashTaskList';
import DashMeetingList from './DashMeetingList';

interface DashMiddleSectionProps {
  tasks: Task[];
  meetings: Meeting[];
  onTaskClick: (task: Task) => void;
  onMeetingClick: (meeting: Meeting) => void;
  taskTitle?: string;
  meetingTitle?: string;
  onViewAllTasks?: (tasks: Task[], title: string) => void;
  onViewAllMeetings?: (meetings: Meeting[], title: string) => void;
  getTaskColor?: (task: Task) => string;
  getMeetingColor?: (meeting: Meeting) => string;
}

export default function DashMiddleSection({ 
  tasks, 
  meetings, 
  onTaskClick, 
  onMeetingClick, 
  taskTitle = 'My Tasks', 
  meetingTitle = 'My Meetings',
  onViewAllTasks,
  onViewAllMeetings,
  getTaskColor,
  getMeetingColor
}: DashMiddleSectionProps) {
  return (
    <div className="dash-middle-section">
            <DashTaskList 
        tasks={tasks}
        title={taskTitle}
        onTaskClick={onTaskClick}
        onViewAll={onViewAllTasks}
        getTaskColor={getTaskColor}
      />
      <DashMeetingList 
        meetings={meetings}
        title={meetingTitle}
        onMeetingClick={onMeetingClick}
        onViewAll={onViewAllMeetings}
        getMeetingColor={getMeetingColor}
      />
    </div>
  );
}
