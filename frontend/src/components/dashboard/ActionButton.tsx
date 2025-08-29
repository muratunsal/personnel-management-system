interface ActionButtonProps {
  label: string;
  onClick: () => void;
  icon?: React.ReactNode;
  disabled?: boolean;
}

export default function ActionButton({ 
  label, 
  onClick, 
  icon, 
  disabled = false
}: ActionButtonProps) {
  return (
    <button 
      className="action-button"
      onClick={onClick}
      disabled={disabled}
    >
      <div className="action-button-content">
        {icon && (
          <div className="action-button-icon">
            {icon}
          </div>
        )}
        <span className="action-button-label">{label}</span>
      </div>
      <div className="action-button-ripple"></div>
    </button>
  );
}
