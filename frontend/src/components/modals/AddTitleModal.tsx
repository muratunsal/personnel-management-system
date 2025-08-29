import { useState } from 'react';
import type { Department } from '../../types/models';

interface AddTitleFormData {
  name: string;
  departmentId: string;
}

interface AddTitleModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (formData: AddTitleFormData) => Promise<void>;
  departments: Department[];
}

export default function AddTitleModal({ isOpen, onClose, onSubmit, departments }: AddTitleModalProps) {
  const [formData, setFormData] = useState<AddTitleFormData>({ name: '', departmentId: '' });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setSuccess('');
    setError('');
    
    if (!formData.departmentId) {
      setError('Select department');
      setLoading(false);
      return;
    }
    
    try {
      await onSubmit(formData);
      setFormData({ name: '', departmentId: '' });
      onClose();
    } catch (err: any) {
      if (err?.status === 409) {
        setError('Title name already exists');
      } else {
        setError(err?.message ?? 'Failed to create title');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({ name: '', departmentId: '' });
    setError('');
    setSuccess('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">Add New Title</h2>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Department*</label>
              <select
                className="form-select"
                value={formData.departmentId}
                onChange={(e) => setFormData({ ...formData, departmentId: e.target.value })}
                required
              >
                <option value="">Select Department</option>
                {departments.map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </div>
            <div className="form-field">
              <label className="form-label">Title Name*</label>
              <input
                type="text"
                className="form-input"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
                placeholder="e.g., Senior Engineer"
              />
            </div>
          </div>

          {error && (<div className="error-message">{error}</div>)}
          {success && (<div className="success-message">{success}</div>)}

          <div className="modal-actions">
            <button type="button" onClick={handleClose} className="cancel-button">Cancel</button>
            <button type="submit" disabled={loading} className="submit-button">
              {loading ? 'Saving...' : 'Add Title'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
