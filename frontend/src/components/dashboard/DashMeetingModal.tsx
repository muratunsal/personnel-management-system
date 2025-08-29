import type { Meeting } from '../../types/models';

interface DashMeetingModalProps {
  meeting: Meeting | null;
  onClose: () => void;
}

export default function DashMeetingModal({ meeting, onClose }: DashMeetingModalProps) {
  if (!meeting) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content dash-meeting-popup" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{meeting.title}</h2>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        
        <div className="dash-popup-content">
          <div className="dash-popup-section">
            <h3>Meeting Details</h3>
            <div className="dash-popup-field">
              <span className="field-label">Description:</span>
              <span className="field-value">{meeting.description || 'No description'}</span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Organizer:</span>
              <span className="field-value">
                {meeting.organizer ? `${meeting.organizer.firstName} ${meeting.organizer.lastName}` : 'Admin'}
              </span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Department:</span>
              <span className="field-value">{meeting.department?.name || 'Company-wide'}</span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Date & Time:</span>
              <span className="field-value">
                {meeting.day} from {meeting.startTime} to {meeting.endTime}
              </span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Status:</span>
              <span className="field-value">{meeting.finalized ? 'Finalized' : 'Draft'}</span>
            </div>
            <div className="dash-popup-field">
              <span className="field-label">Created:</span>
              <span className="field-value">{new Date(meeting.createdAt).toLocaleString()}</span>
            </div>
          </div>

          <div className="dash-popup-section">
            <h3>Participants ({meeting.participants?.length || 0})</h3>
            <div className="dash-participants-list">
              {meeting.participants?.map(participant => (
                <div key={participant.id} className="dash-participant">
                  {participant.firstName} {participant.lastName}
                  <span className="participant-title">{participant.title?.name}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
