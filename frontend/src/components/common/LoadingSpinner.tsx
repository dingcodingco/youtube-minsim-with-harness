interface LoadingSpinnerProps {
  rows?: number;
  className?: string;
}

export default function LoadingSpinner({ rows = 3, className = '' }: LoadingSpinnerProps) {
  return (
    <div className={`animate-pulse space-y-4 ${className}`}>
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="space-y-2">
          <div
            className="h-4 rounded bg-slate-700"
            style={{ width: `${85 - i * 15}%` }}
          />
          <div
            className="h-3 rounded bg-slate-700/60"
            style={{ width: `${70 - i * 10}%` }}
          />
        </div>
      ))}
    </div>
  );
}
