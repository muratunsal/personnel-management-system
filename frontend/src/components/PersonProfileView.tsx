import { useState } from 'react';
import '../styles/people.css';
import { ReactComponent as ChevronDownIcon } from '../icons/chevron-down.svg';
import { ReactComponent as UserAvatarIcon } from '../icons/user-avatar.svg';
import type { Person } from '../types/models';

export default function PersonProfileView({ person, onBack }: { person: Person; onBack: () => void }) {
  const [imageError, setImageError] = useState(false);
  const hasValidImage = person.profilePictureUrl && !imageError;

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="profile-view">
      <div className="profile-navigation">
        <button className="profile-back-button base-button" onClick={onBack} title="Back to list">
          <ChevronDownIcon width={16} height={16} />
        </button>
        <div className="profile-nav-preview">
          <div className="profile-nav-photo base-photo">
            {hasValidImage ? (
              <img
                src={person.profilePictureUrl!}
                alt={`${person.firstName} ${person.lastName}`}
                onError={() => setImageError(true)}
              />
            ) : (
              <UserAvatarIcon width={16} height={16} />
            )}
          </div>
          <span className="profile-nav-name">{person.firstName} {person.lastName}</span>
        </div>
      </div>

      <div className="profile-content">
        <div className="profile-image-section">
          <div className="profile-image-label">PROFILE IMAGE</div>
          <div className="profile-image-container">
            <div className="profile-large-photo base-photo">
              {hasValidImage ? (
                <img
                  src={person.profilePictureUrl!}
                  alt={`${person.firstName} ${person.lastName}`}
                  onError={() => setImageError(true)}
                />
              ) : (
                <UserAvatarIcon width={80} height={80} />
              )}
            </div>
            <button className="change-image-button base-button">
              Change Profile Image
            </button>
          </div>
        </div>

        <div className="employee-details-section">
          <div className="section-header">EMPLOYEE DETAILS</div>

          <div className="form-field">
            <label className="field-label">First Name</label>
            <div className="field-input-container">
              <input type="text" className="field-input base-input" value={person.firstName} readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Last Name</label>
            <div className="field-input-container">
              <input type="text" className="field-input base-input" value={person.lastName} readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Email Address</label>
            <div className="field-input-container">
              <input type="email" className="field-input base-input" value={person.email} readOnly />
              <button className="copy-button base-button" onClick={() => copyToClipboard(person.email)} title="Copy email">Copy</button>
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Phone Number</label>
            <div className="field-input-container">
              <input type="tel" className="field-input base-input" value={person.phoneNumber || ''} placeholder="Not provided" readOnly />
              {person.phoneNumber && (
                <button className="copy-button base-button" onClick={() => copyToClipboard(person.phoneNumber!)} title="Copy phone number">Copy</button>
              )}
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Position</label>
            <div className="field-input-container">
              <input type="text" className="field-input base-input" value={person.title?.name || ''} placeholder="Not assigned" readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Department</label>
            <div className="field-input-container">
              <input type="text" className="field-input base-input" value={person.department?.name || ''} placeholder="Not assigned" readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Start Date</label>
            <div className="field-input-container">
              <input type="date" className="field-input base-input" value={person.startDate || ''} readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Birth Date</label>
            <div className="field-input-container">
              <input type="date" className="field-input base-input" value={person.birthDate || ''} readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Gender</label>
            <div className="field-input-container">
              <input type="text" className="field-input base-input" value={person.gender || ''} placeholder="Not specified" readOnly />
            </div>
          </div>

          <div className="form-field">
            <label className="field-label">Address</label>
            <div className="field-input-container">
              <input type="text" className="field-input base-input" value={person.address || ''} placeholder="Not provided" readOnly />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}


