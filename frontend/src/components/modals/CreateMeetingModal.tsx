import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import type { Department, Person } from '../../types/models';

interface CreateMeetingFormData {
  title: string;
  description: string;
  departmentId: string;
  day: string;
  startTime: string;
  endTime: string;
  participantIds: number[];
}

interface CreateMeetingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (formData: CreateMeetingFormData) => Promise<void>;
  departments: Department[];
  token: string;
}

export default function CreateMeetingModal({ 
  isOpen, 
  onClose, 
  onSubmit, 
  departments, 
  token 
}: CreateMeetingModalProps) {
  const { user } = useAuth();
  const [formData, setFormData] = useState<CreateMeetingFormData>({
    title: '',
    description: '',
    departmentId: '',
    day: '',
    startTime: '',
    endTime: '',
    participantIds: []
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [departmentPeople, setDepartmentPeople] = useState<Person[]>([]);
  const [loadingPeople, setLoadingPeople] = useState(false);

  useEffect(() => {
    if (isOpen && user?.role === 'HEAD' && user?.departmentId) {
      setFormData(prev => ({ ...prev, departmentId: String(user.departmentId) }));
    }
  }, [isOpen, user]);

  const loadDepartmentPeople = useCallback(async (departmentId: string) => {
    setLoadingPeople(true);
    try {
      const params = new URLSearchParams();
      params.set('page', '0');
      params.set('size', '1000');
      params.set('departmentId', departmentId);
      
      const res = await fetch(`http://localhost:8081/api/people?${params.toString()}`, { 
        headers: { Authorization: `Bearer ${token}` } 
      });
      
      if (res.ok) {
        const data = await res.json();
        const people: Person[] = Array.isArray(data) ? data : (data?.content || []);
        setDepartmentPeople(people);
      } else {
        setDepartmentPeople([]);
      }
    } catch (err) {
      console.error('Error loading department people:', err);
      setDepartmentPeople([]);
    } finally {
      setLoadingPeople(false);
    }
  }, [token]);

  const loadAllPeople = useCallback(async () => {
    setLoadingPeople(true);
    try {
      const params = new URLSearchParams();
      params.set('page', '0');
      params.set('size', '1000');
      
      const res = await fetch(`http://localhost:8081/api/people?${params.toString()}`, { 
        headers: { Authorization: `Bearer ${token}` } 
      });
      
      if (res.ok) {
        const data = await res.json();
        const people: Person[] = Array.isArray(data) ? data : (data?.content || []);
        setDepartmentPeople(people);
      } else {
        setDepartmentPeople([]);
      }
    } catch (err) {
      console.error('Error loading all people:', err);
      setDepartmentPeople([]);
    } finally {
      setLoadingPeople(false);
    }
  }, [token]);

  useEffect(() => {
    if (formData.departmentId && formData.departmentId !== '') {
      loadDepartmentPeople(formData.departmentId);
    } else if (formData.departmentId === '' && token) {
      loadAllPeople();
    } else {
      setDepartmentPeople([]);
      setFormData(prev => ({ ...prev, participantIds: [] }));
    }
  }, [formData.departmentId, token, loadAllPeople, loadDepartmentPeople]);

  const handleDepartmentChange = (departmentId: string) => {
    if (user?.role === 'HEAD') return;
    setFormData(prev => ({ 
      ...prev, 
      departmentId, 
      participantIds: [] 
    }));
  };

  const handleParticipantToggle = (personId: number) => {
    setFormData(prev => ({
      ...prev,
      participantIds: prev.participantIds.includes(personId)
        ? prev.participantIds.filter(id => id !== personId)
        : [...prev.participantIds, personId]
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const start = formData.day && formData.startTime ? new Date(`${formData.day}T${formData.startTime}`) : null;
    const end = formData.day && formData.endTime ? new Date(`${formData.day}T${formData.endTime}`) : null;
    const now = new Date();
    
    if (!start || !end) return;
    if (!(start.getTime() > now.getTime())) {
      setError('Start must be in the future');
      return;
    }
    if (!(end.getTime() > start.getTime())) {
      setError('End must be after start');
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await onSubmit(formData);
      setFormData({
        title: '',
        description: '',
        departmentId: '',
        day: '',
        startTime: '',
        endTime: '',
        participantIds: []
      });
      onClose();
    } catch (err: any) {
      setError(err?.message ?? 'Failed to create meeting');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      title: '',
      description: '',
      departmentId: '',
      day: '',
      startTime: '',
      endTime: '',
      participantIds: []
    });
    setError('');
    setSuccess('');
    onClose();
  };

  if (!isOpen) return null;

  const isHeadUser = user?.role === 'HEAD';
  const userDepartment = departments.find(d => d.id === user?.departmentId);

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">Create New Meeting</h2>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Title*</label>
              <input 
                className="form-input" 
                required 
                value={formData.title} 
                onChange={(e) => setFormData({ ...formData, title: e.target.value })} 
                placeholder="Meeting title" 
              />
            </div>
            <div className="form-field">
              <label className="form-label">Department</label>
              {isHeadUser ? (
                <div className="form-input form-input--locked" title="Department is locked for Head users">
                  {userDepartment?.name || 'Your Department'}
                </div>
              ) : (
                <select 
                  className="form-select" 
                  value={formData.departmentId} 
                  onChange={(e) => handleDepartmentChange(e.target.value)} 
                >
                  <option value="">All Departments</option>
                  {departments.map(d => (<option key={d.id} value={d.id}>{d.name}</option>))}
                </select>
              )}
            </div>
          </div>
          <div className="form-row single">
            <div className="form-field">
              <label className="form-label">Description</label>
              <input 
                className="form-input" 
                value={formData.description} 
                onChange={(e) => setFormData({ ...formData, description: e.target.value })} 
                placeholder="Details" 
              />
            </div>
          </div>
          <div className="form-row single">
            <div className="form-field">
              <label className="form-label">Day*</label>
              <input 
                type="date" 
                className="form-input" 
                required 
                value={formData.day} 
                onChange={(e) => setFormData({ ...formData, day: e.target.value })} 
              />
            </div>
          </div>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Start Time*</label>
              <input 
                type="time" 
                className="form-input" 
                required 
                value={formData.startTime} 
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })} 
              />
            </div>
            <div className="form-field">
              <label className="form-label">End Time*</label>
              <input 
                type="time" 
                className="form-input" 
                required 
                value={formData.endTime} 
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })} 
              />
            </div>
          </div>
          <div className="form-row single">
            <div className="form-field">
              <label className="form-label">Participants</label>
              <div className="participants-grid">
                {loadingPeople ? (
                  <div className="participants-loading">Loading participants...</div>
                ) : departmentPeople.length === 0 ? (
                  <div className="participants-empty">No people found in this department.</div>
                ) : (
                  departmentPeople.map(p => (
                    <label key={p.id} className={`participant-checkbox ${formData.participantIds.includes(p.id) ? 'selected' : ''}`}>
                      <input
                        type="checkbox"
                        checked={formData.participantIds.includes(p.id)}
                        onChange={() => handleParticipantToggle(p.id)}
                      />
                      <div className="participant-info">
                        <div className="participant-name">{p.firstName} {p.lastName}</div>
                        <div className="participant-title">{p.title?.name || 'No Title'}</div>
                      </div>
                    </label>
                  ))
                )}
              </div>
            </div>
          </div>
          {error && (<div className="error-message">{error}</div>)}
          {success && (<div className="success-message">{success}</div>)}
          <div className="modal-actions">
            <button type="button" onClick={handleClose} className="cancel-button">Cancel</button>
            <button type="submit" disabled={loading} className="submit-button">
              {loading ? 'Saving...' : 'Create Meeting'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
