import { useState } from 'react';

interface AddDepartmentFormData {
  name: string;
  color: string;
}

interface AddDepartmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (formData: AddDepartmentFormData) => Promise<void>;
}

export default function AddDepartmentModal({ isOpen, onClose, onSubmit }: AddDepartmentModalProps) {
  const [formData, setFormData] = useState<AddDepartmentFormData>({ name: '', color: '' });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [warning, setWarning] = useState('');
  const [error, setError] = useState('');

  const normalizeHex = (input: string) => {
    if (!input) return { value: '#999999', valid: false };
    let s = input.trim();
    if (!s.startsWith('#')) s = '#' + s;
    if (/^#[0-9a-fA-F]{6}$/.test(s)) return { value: s.toUpperCase(), valid: true };
    if (/^#[0-9a-fA-F]{3}$/.test(s)) {
      const r = s[1];
      const g = s[2];
      const b = s[3];
      return { value: (`#${r}${r}${g}${g}${b}${b}`).toUpperCase(), valid: true };
    }
    return { value: '#999999', valid: false };
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setSuccess('');
    setWarning('');
    setError('');
    
    const norm = normalizeHex(formData.color);
    if (!norm.valid) setWarning('Invalid hex color. Using fallback.');
    
    try {
      await onSubmit({ name: formData.name, color: norm.value });
      setFormData({ name: '', color: '' });
      onClose();
    } catch (err: any) {
      if (err?.status === 409) {
        setError('Department name already exists');
      } else {
        setError(err?.message ?? 'Failed to create department');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({ name: '', color: '' });
    setError('');
    setSuccess('');
    setWarning('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">Add New Department</h2>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Name*</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
                className="form-input"
                placeholder="Department name"
              />
            </div>
            <div className="form-field">
              <label className="form-label">Color (Hex)*</label>
              <input
                type="text"
                value={formData.color}
                onChange={(e) => setFormData({ ...formData, color: e.target.value })}
                required
                className="form-input"
                placeholder="#3366FF or 36F"
              />
            </div>
          </div>

          {warning && (
            <div className="error-message">{warning}</div>
          )}
          {error && (
            <div className="error-message">{error}</div>
          )}
          {success && (
            <div className="success-message">{success}</div>
          )}

          <div className="modal-actions">
            <button type="button" onClick={handleClose} className="cancel-button">Cancel</button>
            <button type="submit" disabled={loading} className="submit-button">
              {loading ? 'Saving...' : 'Add Department'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
