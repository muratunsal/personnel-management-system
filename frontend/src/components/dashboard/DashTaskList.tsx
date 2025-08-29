
import type { Task } from '../../types/models';
import { ReactComponent as ChevronRightIcon } from '../../icons/chevron-right.svg';
import TaskItem from './TaskItem';

interface DashTaskListProps {
  tasks: Task[];
  title: string;
  onTaskClick: (task: Task) => void;
  onViewAll?: (tasks: Task[], title: string) => void;
  getTaskColor?: (task: Task) => string;
}

export default function DashTaskList({ tasks, title, onTaskClick, onViewAll, getTaskColor }: DashTaskListProps) {



  return (
    <div className="dash-section-card">
            <div className="dash-section-header" onClick={() => {
        if (onViewAll) {
          onViewAll(tasks, title);
        }
      }}>
        <h3 className="dash-section-title">{title}</h3>
        <button className="dash-section-header-button" onClick={(e) => { 
          e.stopPropagation(); 
          if (onViewAll) {
            onViewAll(tasks, title);
          }
        }}>
          View All
          <ChevronRightIcon width={16} height={16} />
        </button>
      </div>
      <div className="dash-tasks-list">
        {tasks.slice(0,5).length > 0 ? (
          tasks.slice(0,5).map((task, index) => (
            <TaskItem 
              key={task.id} 
              task={task} 
              onClick={onTaskClick} 
              stripColor={getTaskColor ? getTaskColor(task) : ['#0ea5e9', '#f97316', '#22c55e', '#a855f7', '#e11d48'][index % 5]} 
            />
          ))
        ) : (
          <div className="empty-state">No tasks assigned to you</div>
        )}
      </div>
    </div>
  );
}
