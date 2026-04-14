import { useState, useMemo } from 'react';
import {
  ComposedChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { useTrend } from '../../hooks/useTrend';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import EmptyState from '../common/EmptyState';

interface TrendLineChartProps {
  videoId?: string;
}

type Period = 7 | 14 | 30;

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}/${d.getDate()}`;
}

function getDateRange(days: number): { from: string; to: string } {
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - days);
  return {
    from: from.toISOString().split('T')[0],
    to: to.toISOString().split('T')[0],
  };
}

export default function TrendLineChart({ videoId }: TrendLineChartProps) {
  const [period, setPeriod] = useState<Period>(30);
  const dateRange = useMemo(() => getDateRange(period), [period]);

  const { data, loading, error, refetch } = useTrend({
    videoId,
    from: dateRange.from,
    to: dateRange.to,
  });

  const periods: { label: string; value: Period }[] = [
    { label: '7일', value: 7 },
    { label: '14일', value: 14 },
    { label: '30일', value: 30 },
  ];

  if (loading) {
    return <LoadingSpinner rows={4} />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={refetch} />;
  }

  if (data.length === 0) {
    return <EmptyState message="트렌드 데이터가 없습니다" />;
  }

  const chartData = data.map((point) => ({
    ...point,
    dateLabel: formatDate(point.date),
  }));

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-sm font-medium text-slate-300">감정 트렌드</h3>
        <div className="flex gap-1">
          {periods.map((p) => (
            <button
              key={p.value}
              onClick={() => setPeriod(p.value)}
              className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                period === p.value
                  ? 'bg-pulse-request text-white'
                  : 'bg-slate-700 text-slate-400 hover:text-slate-200'
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <XAxis
              dataKey="dateLabel"
              tick={{ fill: '#94a3b8', fontSize: 11 }}
              axisLine={{ stroke: '#475569' }}
              tickLine={false}
            />
            <YAxis
              yAxisId="score"
              domain={[0, 100]}
              tick={{ fill: '#94a3b8', fontSize: 11 }}
              axisLine={{ stroke: '#475569' }}
              tickLine={false}
              label={{
                value: '감정 점수',
                angle: -90,
                position: 'insideLeft',
                fill: '#64748b',
                fontSize: 11,
              }}
            />
            <YAxis
              yAxisId="count"
              orientation="right"
              tick={{ fill: '#94a3b8', fontSize: 11 }}
              axisLine={{ stroke: '#475569' }}
              tickLine={false}
              label={{
                value: '댓글 수',
                angle: 90,
                position: 'insideRight',
                fill: '#64748b',
                fontSize: 11,
              }}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#1e293b',
                border: '1px solid #334155',
                borderRadius: '8px',
                color: '#f1f5f9',
              }}
              labelFormatter={(label) => `날짜: ${label}`}
              formatter={(value: number, name: string) => [
                name === 'sentimentScore'
                  ? `${value.toFixed(1)}점`
                  : `${value}개`,
                name === 'sentimentScore' ? '감정 점수' : '댓글 수',
              ]}
            />
            <Bar
              yAxisId="count"
              dataKey="commentCount"
              fill="#3b82f6"
              opacity={0.3}
              radius={[2, 2, 0, 0]}
            />
            <Line
              yAxisId="score"
              type="monotone"
              dataKey="sentimentScore"
              stroke="#22c55e"
              strokeWidth={2}
              dot={{ fill: '#22c55e', r: 3 }}
              activeDot={{ r: 5 }}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
