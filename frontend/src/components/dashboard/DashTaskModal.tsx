import type { Task } from '../../types/models';

interface DashTaskModalProps {
  task: Task | null;
  onClose: () => void;
  onUpdateStatus: (taskId: number, status: string) => void;
  onCloseTask?: (taskId: number) => void;
  showCloseButton?: boolean;
}

export default function DashTaskModal({ task, onClose, onUpdateStatus, onCloseTask, showCloseButton = false }: DashTaskModalProps) {
  if (!task) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content dash-task-popup" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{task.title}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        
        <div className="dash-popup-content">
          <div className="dash-popup-section">
            <h3>Details</h3>
            <div className="dash-popup-field">
              <span className="field-label">Description:</span>
              <span className="field-value">{task.description || 'No description'}</span>
            </div>
            {task.assignee && (
              <div className="dash-popup-field">
                <span className="field-label">Assigned to:</span>
                <span className="field-value">
                  {task.assignee.firstName} {task.assignee.lastName}
                </span>
              </div>
            )}
            <div className="dash-popup-field">
              <span className="field-label">Created by:</span>
              <span className="field-value">
                {task.createdBy ? `${task.createdBy.firstName} ${task.createdBy.lastName}` : 'Admin'}
              </span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Department:</span>
              <span className="field-value">{task.department?.name || 'No department'}</span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Created:</span>
              <span className="field-value">{new Date(task.createdAt).toLocaleString()}</span>
            </div>
          </div>

          <div className="dash-popup-section">
            <h3>Status & Priority</h3>
            <div className="dash-popup-tags">
              <span className={`dash-tag dash-tag--priority-${task.priority.toLowerCase()}`}>
                {task.priority}
              </span>
              <span className={`dash-tag dash-tag--status-${task.status.toLowerCase()}`}>
                {task.status.replace('_', ' ')}
              </span>
            </div>
          </div>

          <div className="dash-popup-section">
            <h3>Actions</h3>
            <div className="dash-popup-actions">
              {task.status === 'ASSIGNED' && (
                <button 
                  className="dash-action-btn dash-action-btn--primary"
                  onClick={() => onUpdateStatus(task.id, 'IN_PROGRESS')}
                >
                  Mark as In Progress
                </button>
              )}
              {task.status === 'IN_PROGRESS' && (
                <button 
                  className="dash-action-btn dash-action-btn--success"
                  onClick={() => onUpdateStatus(task.id, 'COMPLETED')}
                >
                  Mark as Completed
                </button>
              )}
              {showCloseButton && task.status === 'COMPLETED' && onCloseTask && (
                <button 
                  className="dash-action-btn dash-action-btn--secondary"
                  onClick={() => onCloseTask(task.id)}
                >
                  Close Task
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
