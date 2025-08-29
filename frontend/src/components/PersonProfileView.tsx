import { useState, useEffect, useCallback } from 'react';
import '../styles/people.css';
import { ReactComponent as ChevronDownIcon } from '../icons/chevron-down.svg';
import { ReactComponent as UserAvatarIcon } from '../icons/user-avatar.svg';
import { ReactComponent as EditIcon } from '../icons/edit.svg';
import { ReactComponent as SaveIcon } from '../icons/save.svg';
import { ReactComponent as TrashIcon } from '../icons/trash.svg';
import { ReactComponent as XIcon } from '../icons/x.svg';
import type { Person, Department, Title } from '../types/models';
import { useAuth } from '../context/AuthContext';
import * as permissions from '../utils/permissions';

export default function PersonProfileView({ person, onBack, onPersonUpdate }: {
  person: Person;
  onBack: () => void;
  onPersonUpdate?: (updatedPerson: Person) => void;
}) {
  const { user, token } = useAuth();
  const [isEdit, setIsEdit] = useState(false);
  const [showUrlInput, setShowUrlInput] = useState(false);
  const [urlInput, setUrlInput] = useState('');
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [titles, setTitles] = useState<Title[]>([]);
  const [userDepartment, setUserDepartment] = useState<number | null>(null);
  const [initialPerson, setInitialPerson] = useState<Person | null>(null);
  const [draftPerson, setDraftPerson] = useState<Person>(person);

  useEffect(() => {
    if (!isEdit) {
      setDraftPerson(person);
    }
  }, [person, isEdit]);

  const userPermissions = {
    canViewSensitive: permissions.canViewSensitiveInfo(user?.role || '', user?.email || '', person.email, userDepartment, person.department?.id || null),
    canEdit: permissions.canEdit(user?.role || ''),
    canDelete: permissions.canDelete(user?.role || '')
  };

  const getOrderedTitlesForDepartment = useCallback((departmentId: number | null) => {
    if (!departmentId) return [];
    
    const dep = departments.find(d => d.id === departmentId);
    if (!dep) return [];
    
    const headName = `Head of ${dep.name}`;
    const hasHead = !!dep.headOfDepartment;
    const list = titles.filter(t => t.department?.id === departmentId);
    const filtered = hasHead ? list.filter(t => t.name !== headName) : list;
    return filtered.sort((a, b) => a.name.localeCompare(b.name));
  }, [departments, titles]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [deptRes, titleRes] = await Promise.all([
          fetch('http://localhost:8081/api/departments', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/titles', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);
        const deptData = await deptRes.json();
        const titleData = await titleRes.json();
        setDepartments(deptData);
        setTitles(titleData);
      } catch (error) {
        console.error('Failed to fetch data:', error);
      }
    };
    fetchData();
  }, [token, person?.id]);

  useEffect(() => {
    if (user?.role === 'HEAD' && user?.email && token) {
      const fetchUserDept = async () => {
        try {
          const response = await fetch(`http://localhost:8081/api/people?size=1&email=${encodeURIComponent(user.email)}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          if (response.ok) {
            const data = await response.json();
            const content = Array.isArray(data) ? data : (data?.content ?? []);
            setUserDepartment(content[0]?.department?.id || null);
          }
        } catch (error) {
          console.error('Error fetching user department:', error);
        }
      };
      fetchUserDept();
    }
  }, [user?.role, user?.email, token]);

  const handleSave = async () => {
    try {
      if (!draftPerson.department?.id) {
        alert('Please select a department');
        return;
      }
      if (!draftPerson.title?.id) {
        alert('Please select a title');
        return;
      }
      let updatedImageUrl = draftPerson.profilePictureUrl;
      
      if (previewUrl) {
        updatedImageUrl = previewUrl;
      }

      const payload = {
        firstName: draftPerson.firstName,
        lastName: draftPerson.lastName,
        email: draftPerson.email,
        phoneNumber: draftPerson.phoneNumber || null,
        departmentId: draftPerson.department?.id || null,
        titleId: draftPerson.title?.id || null,
        contractStartDate: draftPerson.contractStartDate || null,
        birthDate: draftPerson.birthDate || null,
        gender: draftPerson.gender || null,
        address: draftPerson.address || null,
        profilePictureUrl: updatedImageUrl,
        salary: draftPerson.salary || null,
        nationalId: draftPerson.nationalId || null,
        bankAccount: draftPerson.bankAccount || null,
        insuranceNumber: draftPerson.insuranceNumber || null,
        contractType: draftPerson.contractType || null,
        contractEndDate: draftPerson.contractEndDate || null,
        
      };

      const response = await fetch(`http://localhost:8081/api/people/${person.id}`, {
        method: "PUT",
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        const savedPerson = await response.json();
        onPersonUpdate?.(savedPerson);
        setPreviewUrl(null);
        setUrlInput('');
        setShowUrlInput(false);
        setIsEdit(false);
        try {
          const wasHeadDeptId = initialPerson?.department?.id || null;
          const wasHeadDeptName = initialPerson?.department?.name || '';
          const wasHeadTitleName = wasHeadDeptName ? `Head of ${wasHeadDeptName}` : '';
          const wasHead = initialPerson?.title?.name === wasHeadTitleName;
          const stillSameDept = savedPerson.department?.id && wasHeadDeptId && savedPerson.department.id === wasHeadDeptId;
          const stillHeadSameDept = stillSameDept && savedPerson.title?.name === wasHeadTitleName;
          if (wasHead && !stillHeadSameDept && wasHeadDeptId && token) {
            await fetch(`http://localhost:8081/api/departments/${wasHeadDeptId}/clear-head`, {
              method: 'POST',
              headers: { Authorization: `Bearer ${token}` }
            });
          }
        } catch (e) {
          console.error('Failed to clear previous head assignment:', e);
        }
        setInitialPerson(null);
        try {
          const [deptRes, titleRes] = await Promise.all([
            fetch('http://localhost:8081/api/departments', {
              headers: { Authorization: `Bearer ${token}` }
            }),
            fetch('http://localhost:8081/api/titles', {
              headers: { Authorization: `Bearer ${token}` }
            })
          ]);
          const deptData = await deptRes.json();
          const titleData = await titleRes.json();
          setDepartments(deptData);
          setTitles(titleData);
        } catch (e) {
          console.error('Failed to refresh metadata:', e);
        }
      } else {
        const errorText = await response.text();
        alert(`Save failed: ${response.status} - ${errorText}`);
      }
    } catch (error) {
      console.error('Save failed:', error);
      alert(`Save error: ${error}`);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete ${person.firstName} ${person.lastName}?`)) return;
    
    try {
      const response = await fetch(`http://localhost:8081/api/people/${person.id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` }
      });

      if (response.ok) {
        alert('Person deleted successfully');
        onBack();
      } else {
        const errorText = await response.text();
        alert(`Delete failed: ${response.status} - ${errorText}`);
      }
    } catch (error) {
      console.error('Delete failed:', error);
      alert(`Delete error: ${error}`);
    }
  };

  const updatePerson = (field: keyof Person, value: any) => {
    if (isEdit) {
      setDraftPerson(prev => ({ ...prev, [field]: value } as Person));
    } else if (onPersonUpdate) {
      onPersonUpdate({ ...person, [field]: value });
    }
  };

  const onUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setUrlInput(e.target.value);
  };

  const onApproveUrl = async () => {
    try {
      const response = await fetch(urlInput);
      if (response.ok) {
        setPreviewUrl(urlInput);
        setShowUrlInput(false);
        setUrlInput('');
      } else {
        alert('Failed to load image from URL');
      }
    } catch (error) {
      alert('Invalid URL or failed to load image');
    }
  };

  const onDenyUrl = () => {
    setShowUrlInput(false);
    setUrlInput('');
  };

  const onEditToggle = () => {
    if (!isEdit) {
      setInitialPerson(person ? { ...person } : null);
      setDraftPerson(person);
      setPreviewUrl(null);
      setUrlInput('');
      setShowUrlInput(false);
    }
    setIsEdit(!isEdit);
  };

  const hasValidImage = person.profilePictureUrl && !previewUrl;

  return (
    <div className={`profile-view${isEdit ? ' is-edit' : ''}`}>
      <div className="profile-navigation">
        <button className="profile-back-button base-button" onClick={onBack}>
          <ChevronDownIcon width={16} height={16} />
        </button>
        <div className="profile-nav-preview">
          <div className="profile-nav-photo base-photo">
            {person.profilePictureUrl ? (
              <img src={person.profilePictureUrl} alt={`${person.firstName} ${person.lastName}`} />
            ) : (
              <UserAvatarIcon width={16} height={16} />
            )}
          </div>
          <span className="profile-nav-name">{`${person.firstName} ${person.lastName}`}</span>
        </div>
        {userPermissions.canEdit && (
          <div className="profile-actions">
            {!isEdit ? (
              <>
                <button className="button-edit base-button" onClick={onEditToggle}>
                  <EditIcon width={16} height={16} />
                  Edit
                </button>
                {userPermissions.canDelete && (
                  <button className="button-delete base-button" onClick={handleDelete}>
                    <TrashIcon width={16} height={16} />
                    Delete
                  </button>
                )}
              </>
            ) : (
              <>
                <button className="button-save base-button" onClick={handleSave}>
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
        <div className="profile-image-section">
          <div className="profile-image-label">PROFILE IMAGE</div>
          <div className="profile-image-container">
            <div className="profile-large-photo base-photo">
              {hasValidImage || previewUrl ? (
                <img 
                  src={isEdit ? (previewUrl || person.profilePictureUrl || '') : person.profilePictureUrl!} 
                  alt={`${person.firstName} ${person.lastName}`} 
                />
              ) : (
                <UserAvatarIcon width={80} height={80} />
              )}
            </div>
            {isEdit && !showUrlInput && (
              <button 
                className="url-toggle-button base-button" 
                onClick={() => setShowUrlInput(true)}
              >
                Change Profile Image
              </button>
            )}
            {isEdit && showUrlInput && (
              <div className="url-input-section">
                <div className="url-input-container">
                  <input 
                    type="text" 
                    className="url-input base-input" 
                    value={urlInput} 
                    onChange={onUrlChange} 
                    placeholder="Enter image URL" 
                  />
                  <div className="url-actions">
                    <button 
                      className="approve-button base-button" 
                      onClick={onApproveUrl}
                      title="Approve URL"
                    >
                      ✓
                    </button>
                    <button 
                      className="deny-button base-button" 
                      onClick={onDenyUrl}
                      title="Cancel"
                    >
                      ✗
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="employee-details-section">
          <div className="section-header">BASIC INFORMATION</div>

          <div className="form-field">
            <label className="field-label">First Name</label>
            <input 
              type="text" 
              className="field-input base-input" 
              value={(isEdit ? draftPerson.firstName : person.firstName) || ''} 
              onChange={(e) => updatePerson('firstName', e.target.value)} 
              readOnly={!isEdit} 
            />
          </div>

          <div className="form-field">
            <label className="field-label">Last Name</label>
            <input 
              type="text" 
              className="field-input base-input" 
              value={(isEdit ? draftPerson.lastName : person.lastName) || ''} 
              onChange={(e) => updatePerson('lastName', e.target.value)} 
              readOnly={!isEdit} 
            />
          </div>

          <div className="form-field">
            <label className="field-label">Email Address</label>
            <input 
              type="email" 
              className="field-input base-input" 
              value={(isEdit ? draftPerson.email : person.email) || ''} 
              onChange={(e) => updatePerson('email', e.target.value)} 
              readOnly={!isEdit} 
            />
          </div>

          

          <div className="form-field">
            <label className="field-label">Phone Number</label>
            <input 
              type="tel" 
              className="field-input base-input" 
              value={(isEdit ? draftPerson.phoneNumber : person.phoneNumber) || ''} 
              onChange={(e) => updatePerson('phoneNumber', e.target.value)} 
              placeholder="Not provided" 
              readOnly={!isEdit} 
            />
          </div>

          <div className="form-field">
            <label className="field-label">Department</label>
            {isEdit ? (
              <div className="custom-dropdown-wrapper">
                <select 
                  className="field-input base-input custom-dropdown" 
                  value={draftPerson.department?.id?.toString() || ''} 
                  onChange={(e) => {
                    const selected = departments.find(d => d.id === Number(e.target.value)) || null;
                    setDraftPerson(prev => ({ ...prev, department: selected, title: null } as Person));
                  }}
                >
                  <option value="">Select Department</option>
                  {departments.map(dept => (
                    <option key={dept.id} value={dept.id}>{dept.name}</option>
                  ))}
                </select>
                <ChevronDownIcon className="dropdown-icon" width={16} height={16} />
              </div>
            ) : (
              <input type="text" className="field-input base-input" value={person.department?.name || ''} placeholder="Not assigned" readOnly />
            )}
          </div>

          <div className="form-field">
            <label className="field-label">Title</label>
            {isEdit ? (
              <div className="custom-dropdown-wrapper">
                <select 
                  className="field-input base-input custom-dropdown" 
                  value={draftPerson.title?.id?.toString() || ''} 
                  onChange={(e) => {
                    const list = getOrderedTitlesForDepartment(draftPerson.department?.id || null);
                    const found = list.find(t => t.id === Number(e.target.value)) || null;
                    setDraftPerson(prev => ({ ...prev, title: found } as Person));
                  }}
                >
                  <option value="">Select Title</option>
                  {(() => {
                    const base = getOrderedTitlesForDepartment(draftPerson.department?.id || null);
                    const current = draftPerson.title;
                    const hasCurrent = current ? base.some(t => t.id === current.id) : true;
                    const finalList = !hasCurrent && current ? [current, ...base] : base;
                    return finalList.map(title => (
                      <option key={title.id} value={title.id}>{title.name}</option>
                    ));
                  })()}
                </select>
                <ChevronDownIcon className="dropdown-icon" width={16} height={16} />
              </div>
            ) : (
              <input type="text" className="field-input base-input" value={person.title?.name || ''} placeholder="Not assigned" readOnly />
            )}
          </div>

          <div className="form-field">
            <label className="field-label">Gender</label>
            {isEdit ? (
              <div className="custom-dropdown-wrapper">
                <select 
                  className="field-input base-input custom-dropdown" 
                  value={draftPerson.gender || ''} 
                  onChange={(e) => updatePerson('gender', e.target.value)}
                >
                  <option value="">Select Gender</option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </select>
                <ChevronDownIcon className="dropdown-icon" width={16} height={16} />
              </div>
            ) : (
              <input 
                type="text" 
                className="field-input base-input" 
                value={person.gender || 'Not specified'} 
                readOnly 
              />
            )}
          </div>

          {userPermissions.canViewSensitive && (
            <>
              <div className="section-header">SENSITIVE INFORMATION</div>

              <div className="form-field">
                <label className="field-label">Birth Date</label>
                <input 
                  type="date" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.birthDate : person.birthDate) || ''} 
                  onChange={(e) => updatePerson('birthDate', e.target.value)} 
                  readOnly={!isEdit} 
                />
              </div>

              <div className="form-field">
                <label className="field-label">Address</label>
                <input 
                  type="text" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.address : person.address) || ''} 
                  onChange={(e) => updatePerson('address', e.target.value)} 
                  placeholder="Not provided" 
                  readOnly={!isEdit} 
                />
              </div>

              <div className="form-field">
                <label className="field-label">Salary</label>
                {isEdit ? (
                  <input 
                    type="number" 
                    className="field-input base-input" 
                    value={draftPerson.salary || ''} 
                    onChange={(e) => updatePerson('salary', Number(e.target.value))} 
                    placeholder="Enter salary amount"
                    step="1"
                    min="0"
                  />
                ) : (
                  <input 
                    type="text" 
                    className="field-input base-input" 
                    value={person.salary ? `$${person.salary.toLocaleString()}` : 'Not provided'} 
                    readOnly 
                  />
                )}
              </div>

              <div className="form-field">
                <label className="field-label">Contract Type</label>
                {isEdit ? (
                  <div className="custom-dropdown-wrapper">
                    <select 
                      className="field-input base-input custom-dropdown" 
                      value={draftPerson.contractType || ''} 
                      onChange={(e) => updatePerson('contractType', e.target.value)}
                    >
                      <option value="">Select Contract Type</option>
                      <option value="Full-time">Full-time</option>
                      <option value="Part-time">Part-time</option>
                      <option value="Intern">Intern</option>
                    </select>
                    <ChevronDownIcon className="dropdown-icon" width={16} height={16} />
                  </div>
                ) : (
                  <input type="text" className="field-input base-input" value={person.contractType || 'Not provided'} readOnly />
                )}
              </div>

              <div className="form-field">
                <label className="field-label">Contract Start Date</label>
                <input 
                  type="date" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.contractStartDate : person.contractStartDate) || ''} 
                  onChange={(e) => updatePerson('contractStartDate', e.target.value)} 
                  readOnly={!isEdit} 
                />
              </div>

              <div className="form-field">
                <label className="field-label">Contract End Date</label>
                <input 
                  type="date" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.contractEndDate : person.contractEndDate) || ''} 
                  onChange={(e) => updatePerson('contractEndDate', e.target.value)} 
                  readOnly={!isEdit} 
                />
              </div>

              <div className="form-field">
                <label className="field-label">Bank Account</label>
                <input 
                  type="text" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.bankAccount : person.bankAccount) || ''} 
                  onChange={(e) => updatePerson('bankAccount', e.target.value)} 
                  readOnly={!isEdit} 
                />
              </div>

              <div className="form-field">
                <label className="field-label">National ID</label>
                <input 
                  type="text" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.nationalId : person.nationalId) || ''} 
                  onChange={(e) => updatePerson('nationalId', e.target.value)} 
                  readOnly={!isEdit} 
                />
              </div>

              <div className="form-field">
                <label className="field-label">Insurance Number</label>
                <input 
                  type="text" 
                  className="field-input base-input" 
                  value={(isEdit ? draftPerson.insuranceNumber : person.insuranceNumber) || ''} 
                  onChange={(e) => updatePerson('insuranceNumber', e.target.value)} 
                  readOnly={!isEdit} 
                />
              </div>

              
            </>
          )}
        </div>
      </div>
    </div>
  );
}
