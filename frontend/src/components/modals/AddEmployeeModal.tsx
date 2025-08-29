import { useState } from 'react';
import type { Department, Title } from '../../types/models';

interface AddEmployeeFormData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  departmentId: string;
  titleId: string;
  startDate: string;
  birthDate: string;
  gender: string;
  address: string;
  profilePictureUrl: string;
  salary: string;
  nationalId: string;
  bankAccount: string;
  insuranceNumber: string;
  contractType: string;
  contractEndDate: string;
}

interface AddEmployeeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (formData: AddEmployeeFormData) => Promise<void>;
  departments: Department[];
  titles: Title[];
  getOrderedTitlesForDepartment: (departmentId: number) => Title[];
}

export default function AddEmployeeModal({ 
  isOpen, 
  onClose, 
  onSubmit, 
  departments, 
  titles, 
  getOrderedTitlesForDepartment 
}: AddEmployeeModalProps) {
  const [formData, setFormData] = useState<AddEmployeeFormData>({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    departmentId: '',
    titleId: '',
    startDate: '',
    birthDate: '',
    gender: '',
    address: '',
    profilePictureUrl: '',
    salary: '',
    nationalId: '',
    bankAccount: '',
    insuranceNumber: '',
    contractType: '',
    contractEndDate: ''
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    
    if (name === 'departmentId') {
      setFormData(prev => ({ 
        ...prev, 
        [name]: value, 
        titleId: '' 
      }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await onSubmit(formData);
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        departmentId: '',
        titleId: '',
        startDate: '',
        birthDate: '',
        gender: '',
        address: '',
        profilePictureUrl: '',
        salary: '',
        nationalId: '',
        bankAccount: '',
        insuranceNumber: '',
        contractType: '',
        contractEndDate: ''
      });
      onClose();
    } catch (err: any) {
      setError(err?.message ?? 'Failed to add employee');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      departmentId: '',
      titleId: '',
      startDate: '',
      birthDate: '',
      gender: '',
      address: '',
      profilePictureUrl: '',
      salary: '',
      nationalId: '',
      bankAccount: '',
      insuranceNumber: '',
      contractType: '',
      contractEndDate: ''
    });
    setError('');
    setSuccess('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2 className="modal-title">Add New Employee</h2>
        </div>
        
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                First Name*
              </label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                required
                className="form-input"
                placeholder="Enter first name"
              />
            </div>
            
            <div className="form-field">
              <label className="form-label">
                Last Name*
              </label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                required
                className="form-input"
                placeholder="Enter last name"
              />
            </div>
          </div>

          <div className="form-row single">
            <div className="form-field">
              <label className="form-label">
                Email*
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                className="form-input"
                placeholder="Enter email address"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                Phone Number
              </label>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter phone number"
              />
            </div>
            
            <div className="form-field">
              <label className="form-label">
                National ID
              </label>
              <input
                type="text"
                name="nationalId"
                value={formData.nationalId}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter national ID"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                Department
              </label>
              <select
                name="departmentId"
                value={formData.departmentId}
                onChange={handleInputChange}
                required
                className="form-select"
              >
                <option value="">Select Department</option>
                {departments.map((dept) => (
                  <option key={dept.id} value={dept.id}>{dept.name}</option>
                ))}
              </select>
            </div>
            
            <div className="form-field">
              <label className="form-label">
                Title
              </label>
              <select
                name="titleId"
                value={formData.titleId}
                onChange={handleInputChange}
                required
                className="form-select"
                disabled={!formData.departmentId}
              >
                <option value="">
                  {formData.departmentId ? "Select Title" : "Select Department First"}
                </option>
                {formData.departmentId && getOrderedTitlesForDepartment(parseInt(formData.departmentId)).map(title => (
                  <option key={title.id} value={title.id}>{title.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                Contract Type
              </label>
              <select
                name="contractType"
                value={formData.contractType}
                onChange={handleInputChange}
                className="form-select"
              >
                <option value="">Select Contract Type</option>
                <option value="Full-time">Full-time</option>
                <option value="Part-time">Part-time</option>
                <option value="Internship">Internship</option>
              </select>
            </div>
            
            <div className="form-field">
              <label className="form-label">
                Salary
              </label>
              <input
                type="number"
                name="salary"
                value={formData.salary}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter salary"
                min="0"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                Contract Start Date
              </label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            
            <div className="form-field">
              <label className="form-label">
                Contract End Date
              </label>
              <input
                type="date"
                name="contractEndDate"
                value={formData.contractEndDate}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                Birth Date
              </label>
              <input
                type="date"
                name="birthDate"
                value={formData.birthDate}
                onChange={handleInputChange}
                className="form-input"
              />
            </div>
            
            <div className="form-field">
              <label className="form-label">
                Gender
              </label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleInputChange}
                className="form-select"
              >
                <option value="">Select Gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
          </div>

          <div className="form-row single">
            <div className="form-field">
              <label className="form-label">
                Address
              </label>
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter address"
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-field">
              <label className="form-label">
                Bank Account
              </label>
              <input
                type="text"
                name="bankAccount"
                value={formData.bankAccount}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter bank account number"
              />
            </div>
            
            <div className="form-field">
              <label className="form-label">
                Insurance Number
              </label>
              <input
                type="text"
                name="insuranceNumber"
                value={formData.insuranceNumber}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter insurance number"
              />
            </div>
          </div>

          <div className="form-row single">
            <div className="form-field">
              <label className="form-label">
                Profile Picture URL
              </label>
              <input
                type="url"
                name="profilePictureUrl"
                value={formData.profilePictureUrl}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Enter profile picture URL"
              />
            </div>
          </div>

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          {success && (
            <div className="success-message">
              {success}
            </div>
          )}

          <div className="modal-actions">
            <button
              type="button"
              onClick={handleClose}
              className="cancel-button"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="submit-button"
            >
              {loading ? 'Adding...' : 'Add Employee'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
