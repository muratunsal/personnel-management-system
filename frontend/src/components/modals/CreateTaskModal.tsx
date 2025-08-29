import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../../context/AuthContext';
import type { Department, Person } from '../../types/models';

interface CreateTaskFormData {
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  departmentId: string;
  assigneeId: string;
}

interface CreateTaskModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (formData: CreateTaskFormData) => Promise<void>;
  departments: Department[];
  token: string;
}

export default function CreateTaskModal({ 
  isOpen, 
  onClose, 
  onSubmit, 
  departments, 
  token 
}: CreateTaskModalProps) {
  const { user } = useAuth();
  const [formData, setFormData] = useState<CreateTaskFormData>({
    title: '',
    description: '',
    priority: 'MEDIUM',
    departmentId: '',
    assigneeId: ''
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
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

  useEffect(() => {
    if (formData.departmentId && formData.departmentId !== '') {
      loadDepartmentPeople(formData.departmentId);
    } else {
      setDepartmentPeople([]);
      setFormData(prev => ({ ...prev, assigneeId: '' }));
    }
  }, [formData.departmentId, token, loadDepartmentPeople]);

  const handleDepartmentChange = (departmentId: string) => {
    if (user?.role === 'HEAD') return;
    setFormData(prev => ({ 
      ...prev, 
      departmentId, 
      assigneeId: '' 
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await onSubmit(formData);
      setFormData({
        title: '',
        description: '',
        priority: 'MEDIUM',
        departmentId: '',
        assigneeId: ''
      });
      onClose();
    } catch (err: any) {
      setError(err?.message ?? 'Failed to create task');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      title: '',
      description: '',
      priority: 'MEDIUM',
      departmentId: '',
      assigneeId: ''
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
          <h2 className="modal-title">Create New Task</h2>
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
                placeholder="Task title" 
              />
            </div>
            <div className="form-field">
              <label className="form-label">Priority*</label>
              <select 
                className="form-select" 
                value={formData.priority} 
                onChange={(e) => setFormData({ ...formData, priority: e.target.value as any })}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
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
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Department*</label>
              {isHeadUser ? (
                <div className="form-input form-input--locked" title="Department is locked for Head users">
                  {userDepartment?.name || 'Your Department'}
                </div>
              ) : (
                <select 
                  className="form-select" 
                  value={formData.departmentId} 
                  onChange={(e) => handleDepartmentChange(e.target.value)} 
                  required
                >
                  <option value="">Select Department</option>
                  {departments.map(d => (<option key={d.id} value={d.id}>{d.name}</option>))}
                </select>
              )}
            </div>
            <div className="form-field">
              <label className="form-label">Assignee*</label>
              <select 
                className="form-select" 
                value={formData.assigneeId} 
                onChange={(e) => setFormData({ ...formData, assigneeId: e.target.value })} 
                required 
                disabled={!formData.departmentId || loadingPeople}
              >
                <option value="">{formData.departmentId ? 'Select Assignee' : 'Select department first'}</option>
                {loadingPeople ? (
                  <option value="">Loading assignees...</option>
                ) : (
                  departmentPeople.map(p => (<option key={p.id} value={p.id}>{p.firstName} {p.lastName}</option>))
                )}
              </select>
            </div>
          </div>
          {error && (<div className="error-message">{error}</div>)}
          {success && (<div className="success-message">{success}</div>)}
          <div className="modal-actions">
            <button type="button" onClick={handleClose} className="cancel-button">Cancel</button>
            <button type="submit" disabled={loading} className="submit-button">
              {loading ? 'Saving...' : 'Create Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
