import { useState } from 'react';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
} from 'recharts';
import type { SentimentDetailResponse, SampleComment } from '../../types/pulse';

interface SentimentPieChartProps {
  data: SentimentDetailResponse;
}

const SENTIMENT_CONFIG: Record<
  string,
  { label: string; color: string; key: keyof SentimentDetailResponse['sampleComments'] | null }
> = {
  positive: { label: '긍정', color: '#22c55e', key: 'positive' },
  negative: { label: '부정', color: '#ef4444', key: 'negative' },
  neutral: { label: '중립', color: '#6b7280', key: 'neutral' },
  request: { label: '요청', color: '#3b82f6', key: 'request' },
  question: { label: '질문', color: '#a855f7', key: 'question' },
};

const RADIAN = Math.PI / 180;

function renderCustomLabel({
  cx,
  cy,
  midAngle,
  innerRadius,
  outerRadius,
  percent,
}: {
  cx: number;
  cy: number;
  midAngle: number;
  innerRadius: number;
  outerRadius: number;
  percent: number;
}) {
  if (percent < 0.03) return null;
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);

  return (
    <text
      x={x}
      y={y}
      fill="#f1f5f9"
      textAnchor="middle"
      dominantBaseline="central"
      fontSize={12}
      fontWeight={600}
    >
      {`${(percent * 100).toFixed(1)}%`}
    </text>
  );
}

export default function SentimentPieChart({ data }: SentimentPieChartProps) {
  const [selectedSentiment, setSelectedSentiment] = useState<string | null>(null);

  const chartData = Object.entries(data.distribution)
    .filter(([, value]) => value > 0)
    .map(([key, value]) => ({
      name: SENTIMENT_CONFIG[key]?.label ?? key,
      value,
      sentimentKey: key,
      color: SENTIMENT_CONFIG[key]?.color ?? '#6b7280',
    }));

  const handleClick = (_: unknown, index: number) => {
    const item = chartData[index];
    if (!item) return;
    setSelectedSentiment(
      selectedSentiment === item.sentimentKey ? null : item.sentimentKey
    );
  };

  const sampleCommentKey = selectedSentiment
    ? SENTIMENT_CONFIG[selectedSentiment]?.key
    : null;
  const sampleComments: SampleComment[] = sampleCommentKey
    ? (data.sampleComments[sampleCommentKey] ?? []).slice(0, 3)
    : [];

  return (
    <div className="flex flex-col">
      <div className="h-64">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              innerRadius={55}
              outerRadius={90}
              dataKey="value"
              onClick={handleClick}
              label={renderCustomLabel}
              labelLine={false}
              className="cursor-pointer"
            >
              {chartData.map((entry, index) => (
                <Cell
                  key={`cell-${index}`}
                  fill={entry.color}
                  opacity={
                    selectedSentiment && selectedSentiment !== entry.sentimentKey
                      ? 0.4
                      : 1
                  }
                  stroke="transparent"
                />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                backgroundColor: '#1e293b',
                border: '1px solid #334155',
                borderRadius: '8px',
                color: '#f1f5f9',
              }}
              formatter={(value: number) => [`${value.toFixed(1)}%`, '']}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>

      {/* Legend */}
      <div className="mt-2 flex flex-wrap justify-center gap-3">
        {chartData.map((entry) => (
          <button
            key={entry.sentimentKey}
            onClick={() =>
              setSelectedSentiment(
                selectedSentiment === entry.sentimentKey ? null : entry.sentimentKey
              )
            }
            className={`flex items-center gap-1.5 rounded-md px-2 py-1 text-xs transition-opacity ${
              selectedSentiment && selectedSentiment !== entry.sentimentKey
                ? 'opacity-50'
                : 'opacity-100'
            }`}
          >
            <span
              className="inline-block h-2.5 w-2.5 rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="text-slate-300">{entry.name}</span>
          </button>
        ))}
      </div>

      {/* Sample comments */}
      {sampleComments.length > 0 && (
        <div className="mt-4 space-y-2">
          <p className="text-xs font-medium text-slate-400">
            {SENTIMENT_CONFIG[selectedSentiment!]?.label} 댓글 샘플
          </p>
          {sampleComments.map((comment, i) => (
            <div
              key={i}
              className="rounded-lg bg-slate-900/50 px-3 py-2 text-sm"
            >
              <p className="text-slate-300">{comment.content}</p>
              <p className="mt-1 text-xs text-slate-500">
                {comment.author} · 좋아요 {comment.likeCount}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
