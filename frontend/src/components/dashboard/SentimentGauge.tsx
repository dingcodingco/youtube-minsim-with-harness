interface SentimentGaugeProps {
  score: number;
  totalComments: number;
}

function getGaugeColor(score: number): string {
  if (score >= 60) return '#22c55e';
  if (score >= 30) return '#eab308';
  return '#ef4444';
}

export default function SentimentGauge({ score, totalComments }: SentimentGaugeProps) {
  const clampedScore = Math.max(0, Math.min(100, score));
  // Semicircle gauge: arc from 180deg (left) to 0deg (right)
  // Angle in radians: pi (left) to 0 (right)
  const sweepAngle = (clampedScore / 100) * Math.PI;
  const cx = 120;
  const cy = 110;
  const r = 90;

  // Background arc (full semicircle)
  const bgStartX = cx - r;
  const bgStartY = cy;
  const bgEndX = cx + r;
  const bgEndY = cy;

  // Score arc
  const endX = cx - r * Math.cos(sweepAngle);
  const endY = cy - r * Math.sin(sweepAngle);
  const largeArc = clampedScore > 50 ? 1 : 0;

  const color = getGaugeColor(clampedScore);

  return (
    <div className="flex flex-col items-center">
      <svg viewBox="0 0 240 140" className="w-full max-w-[240px]">
        {/* Background track */}
        <path
          d={`M ${bgStartX} ${bgStartY} A ${r} ${r} 0 0 1 ${bgEndX} ${bgEndY}`}
          fill="none"
          stroke="#334155"
          strokeWidth={16}
          strokeLinecap="round"
        />
        {/* Score arc */}
        {clampedScore > 0 && (
          <path
            d={`M ${bgStartX} ${bgStartY} A ${r} ${r} 0 ${largeArc} 1 ${endX} ${endY}`}
            fill="none"
            stroke={color}
            strokeWidth={16}
            strokeLinecap="round"
          />
        )}
        {/* Score text */}
        <text
          x={cx}
          y={cy - 10}
          textAnchor="middle"
          className="fill-pulse-text text-4xl font-bold"
          style={{ fontSize: '36px', fontWeight: 700 }}
        >
          {clampedScore.toFixed(1)}
        </text>
        {/* Label */}
        <text
          x={cx}
          y={cy + 16}
          textAnchor="middle"
          className="fill-slate-400"
          style={{ fontSize: '12px' }}
        >
          감정 점수
        </text>
      </svg>
      <p className="mt-1 text-sm text-slate-400">
        총 {totalComments.toLocaleString()}개 댓글 기준
      </p>
    </div>
  );
}
