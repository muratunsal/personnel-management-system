interface DashCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  color: string;
  valueClassName?: string;
}

export default function DashCard({ title, value, subtitle, icon, color, valueClassName }: DashCardProps) {
  return (
    <div 
      className="dash-card dash-card-dynamic"
      style={{
        '--card-color': color,
        '--card-color-10': `${color}10`,
        '--card-color-05': `${color}05`,
        '--card-color-15': `${color}15`,
        '--card-color-08': `${color}08`
      } as React.CSSProperties}
    >
      <div className="dash-card-header">
        <div className="dash-card-icon dash-card-icon-dynamic">
          {icon}
        </div>
      </div>
      <div className={`dash-card-value ${valueClassName ?? ''}`}>{value}</div>
      
      <div className="dash-card-content">
        <div className="dash-card-title">{title}</div>
        {subtitle && <div className="dash-card-subtitle">{subtitle}</div>}
      </div>
      
      <div className="dash-card-glow dash-card-glow-dynamic"></div>
    </div>
  );
}
