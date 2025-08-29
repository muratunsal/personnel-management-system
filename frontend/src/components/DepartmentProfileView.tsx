import { useState } from 'react';
import '../styles/people.css';
import { ReactComponent as ChevronDownIcon } from '../icons/chevron-down.svg';
import { ReactComponent as EditIcon } from '../icons/edit.svg';
import { ReactComponent as SaveIcon } from '../icons/save.svg';
import { ReactComponent as TrashIcon } from '../icons/trash.svg';
import { ReactComponent as XIcon } from '../icons/x.svg';
import { ReactComponent as UserAvatarIcon } from '../icons/user-avatar.svg';
import { ReactComponent as UsersIcon } from '../icons/users.svg';
import type { Department, Person as PersonModel } from '../types/models';
import { useAuth } from '../context/AuthContext';

export default function DepartmentProfileView({ department, onBack, onDepartmentUpdate, onPersonClick }: { 
  department: Department; 
  onBack: () => void; 
  onDepartmentUpdate?: (updatedDepartment: Department) => void;
  onPersonClick?: (person: PersonModel) => void; 
}) {
  const { user, token } = useAuth();
  const head = department.headOfDepartment ? department.headOfDepartment : null;
  const employees: PersonModel[] = department.employees ? department.employees : [];
  const [isEdit, setIsEdit] = useState(false);
  const [form, setForm] = useState({
    name: department.name || '',
    color: department.color || ''
  });

  const onChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const onEditToggle = () => {
    if (!isEdit) {
      setForm({ 
        name: department.name || '', 
        color: department.color || ''
      });
    }
    setIsEdit(!isEdit);
  };

  const onSave = async () => {
    try {
      const payload = { name: form.name, color: form.color };
      const response = await fetch(`http://localhost:8081/api/departments/${department.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token ? `Bearer ${token}` : ''
        },
        body: JSON.stringify(payload)
      });
      
      if (response.ok) {
        const updatedDepartment = await response.json();
        onDepartmentUpdate?.(updatedDepartment);
      }
      setIsEdit(false);
    } catch (error) {
      console.error('Save failed:', error);
    }
  };

  const onDelete = async () => {
    const confirmMessage = `Are you sure you want to delete the "${department.name}" department? This action cannot be undone.`;
    
    if (window.confirm(confirmMessage)) {
      try {
        const response = await fetch(`http://localhost:8081/api/departments/${department.id}`, {
          method: 'DELETE',
          headers: {
            Authorization: token ? `Bearer ${token}` : ''
          }
        });

        if (response.ok) {
          alert('Department deleted successfully');
          onBack();
        } else {
          const errorText = await response.text();
          alert(`Delete failed: ${response.status} - ${errorText}`);
        }
      } catch (error) {
        console.error('Delete failed:', error);
        alert(`Delete error: ${error}`);
      }
    }
  };

  return (
    <div className={`profile-view${isEdit ? ' is-edit' : ''}`}>
      <div className="profile-navigation">
        <button className="profile-back-button base-button" onClick={onBack} title="Back to organization">
          <ChevronDownIcon width={16} height={16} />
        </button>
        <div className="profile-nav-preview">
          <div 
            className={`profile-nav-photo dept-color-dot ${isEdit ? 'edit-mode' : ''}`} 
            style={{ '--dept-color': isEdit ? form.color || '#64748b' : department.color || '#64748b' } as React.CSSProperties}
          />
          <span className="profile-nav-name">{isEdit ? form.name : department.name}</span>
        </div>
        {user?.role === 'ADMIN' && (
          <div className="profile-actions">
            {!isEdit ? (
              <>
                <button className="button-edit base-button" onClick={onEditToggle}>
                  <EditIcon width={16} height={16} />
                  Edit
                </button>
                <button className="button-delete base-button" onClick={onDelete}>
                  <TrashIcon width={16} height={16} />
                  Delete
                </button>
              </>
            ) : (
              <>
                <button className="button-save base-button" onClick={onSave}>
                  <SaveIcon width={16} height={16} />
                  Save
                </button>
                <button className="button-cancel base-button" onClick={onEditToggle}>
                  <XIcon width={16} height={16} />
                  Cancel
                </button>
              </>
            )}
          </div>
        )}
      </div>

      <div className="profile-content">
        <div className="dept-overview-section">
          <div className="dept-header-card">
            <div 
              className="dept-color-indicator" 
              style={{ '--dept-color': department.color || '#64748b' } as React.CSSProperties}
            />
            <div className="dept-summary">
              <h2 className="dept-title">{department.name}</h2>
              <div className="dept-stats">
                <div className="stat-item">
                  <UsersIcon width={16} height={16} />
                  <span>{employees.length} employees</span>
                </div>
                <div className="stat-item">
                  <span className="stat-label">Head:</span>
                  <span>{head ? `${head.firstName} ${head.lastName}` : 'Not assigned'}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="profile-sections-container">
          <div className="employee-details-section">
            <div className="section-header">DEPARTMENT DETAILS</div>

            <div className="form-field">
              <label className="field-label">Department Name</label>
              <input 
                type="text" 
                className="field-input base-input" 
                name="name" 
                value={isEdit ? form.name : department.name} 
                onChange={onChange} 
                readOnly={!isEdit} 
              />
            </div>

            <div className="form-field">
              <label className="field-label">Color</label>
              <div className="color-input-container">
                <input 
                  type="text" 
                  className="field-input base-input" 
                  name="color" 
                  value={isEdit ? form.color : department.color || ''} 
                  placeholder="#64748b" 
                  onChange={onChange} 
                  readOnly={!isEdit} 
                />
                {(isEdit ? form.color : department.color) && (
                  <div 
                    className="color-preview" 
                    style={{ '--preview-color': isEdit ? form.color : department.color } as React.CSSProperties}
                  />
                )}
              </div>
            </div>
          </div>

          <div className="employee-details-section">
            <div className="section-header">LEADERSHIP</div>

            <div className="form-field">
              <label className="field-label">Head of Department</label>
              <div className="head-display">
                {head ? (
                  <div 
                    className="person-info clickable-person" 
                    onClick={() => onPersonClick?.(head)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        onPersonClick?.(head);
                      }
                    }}
                  >
                    <div className="person-avatar">
                      <div className="person-photo base-photo">
                        {head.profilePictureUrl ? (
                          <img src={head.profilePictureUrl} alt={`${head.firstName} ${head.lastName}`} />
                        ) : (
                          <UserAvatarIcon width={20} height={20} />
                        )}
                      </div>
                      <div className="person-details">
                        <div className="person-name">{head.firstName} {head.lastName}</div>
                        <div className="person-role">{head.title?.name}</div>
                        <div className="person-email">{head.email}</div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="no-head-assigned">
                    <UserAvatarIcon width={20} height={20} />
                    <span>No head assigned</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="employee-details-section">
            <div className="section-header">TEAM OVERVIEW</div>

            <div className="form-field">
              <label className="field-label">Total Employees</label>
              <input 
                type="text" 
                className="field-input base-input" 
                value={employees.length.toString()} 
                readOnly 
              />
            </div>

            <div className="employees-list">
              {employees.length > 0 ? (
                <div className="employees-grid">
                  {employees.slice(0, 8).map(employee => (
                    <div 
                      key={employee.id} 
                      className="employee-card clickable-person"
                      onClick={() => onPersonClick?.(employee)}
                      role="button"
                      tabIndex={0}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          onPersonClick?.(employee);
                        }
                      }}
                    >
                      <div className="employee-avatar">
                        <div className="person-photo base-photo">
                          {employee.profilePictureUrl ? (
                            <img src={employee.profilePictureUrl} alt={`${employee.firstName} ${employee.lastName}`} />
                          ) : (
                            <UserAvatarIcon width={16} height={16} />
                          )}
                        </div>
                      </div>
                      <div className="employee-info">
                        <div className="employee-name">{employee.firstName} {employee.lastName}</div>
                        <div className="employee-title">{employee.title?.name || 'No title'}</div>
                      </div>
                    </div>
                  ))}
                  {employees.length > 8 && (
                    <div className="employee-card more-employees">
                      <div className="more-count">+{employees.length - 8}</div>
                      <div className="more-text">more employees</div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="no-employees">
                  <UsersIcon width={24} height={24} />
                  <span>No employees assigned</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


