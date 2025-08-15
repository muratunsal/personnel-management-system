import '../styles/people.css';
import { ReactComponent as ChevronDownIcon } from '../icons/chevron-down.svg';
import type { Department, Person as PersonModel } from '../types/models';

export default function DepartmentProfileView({ department, onBack }: { department: Department; onBack: () => void }) {
  const head = department.headOfDepartment ? department.headOfDepartment : null;
  const employees: PersonModel[] = department.employees ? department.employees : [];

  return (
    <div className="profile-view">
      <div className="profile-navigation">
        <button className="profile-back-button" onClick={onBack} title="Back to organization">
          <ChevronDownIcon width={16} height={16} />
        </button>
        <div className="profile-nav-preview">
          <div className="profile-nav-photo dept-color-dot" style={{ backgroundColor: department.color || '#64748b' }} />
          <span className="profile-nav-name">{department.name}</span>
        </div>
      </div>

      <div className="profile-content">
        <div className="employee-details-section" style={{ width: '100%' }}>
          <div className="section-header">DEPARTMENT DETAILS</div>

          <div className="form-field">
            <label className="field-label">Department Name</label>
            <div className="field-input-container">
              <input type="text" className="field-input" value={department.name} readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Color</label>
            <div className="field-input-container">
              <input type="text" className="field-input" value={department.color || ''} placeholder="Not set" readOnly />
            </div>
          </div>

          <div className="section-header" style={{ marginTop: 24 }}>HEAD OF DEPARTMENT</div>
          <div className="form-field">
            <label className="field-label">Head</label>
            <div className="field-input-container">
              <input type="text" className="field-input" value={head ? `${head.firstName} ${head.lastName}` : ''} placeholder="Not assigned" readOnly />
            </div>
          </div>

          <div className="section-header" style={{ marginTop: 24 }}>EMPLOYEES</div>
          <div className="form-field">
            <label className="field-label">Count</label>
            <div className="field-input-container">
              <input type="text" className="field-input" value={employees.length.toString()} readOnly />
            </div>
          </div>


        </div>
      </div>
    </div>
  );
}


