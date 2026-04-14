import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import { useKeywords } from '../../hooks/useKeywords';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import EmptyState from '../common/EmptyState';

interface KeywordRankingProps {
  videoId: string;
}

const SENTIMENT_COLORS: Record<string, string> = {
  POSITIVE: '#22c55e',
  NEGATIVE: '#ef4444',
  NEUTRAL: '#6b7280',
  REQUEST: '#3b82f6',
  QUESTION: '#a855f7',
};

export default function KeywordRanking({ videoId }: KeywordRankingProps) {
  const { data, loading, error, refetch } = useKeywords(videoId, 10);

  if (loading) {
    return <LoadingSpinner rows={5} />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={refetch} />;
  }

  if (data.length === 0) {
    return <EmptyState message="키워드 데이터가 없습니다" />;
  }

  const chartData = [...data]
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
    .reverse();

  return (
    <div>
      <h3 className="mb-4 text-sm font-medium text-slate-300">
        키워드 랭킹 Top 10
      </h3>
      <div className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={chartData} layout="vertical" margin={{ left: 10 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" horizontal={false} />
            <XAxis
              type="number"
              tick={{ fill: '#94a3b8', fontSize: 11 }}
              axisLine={{ stroke: '#475569' }}
              tickLine={false}
            />
            <YAxis
              type="category"
              dataKey="word"
              tick={{ fill: '#e2e8f0', fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              width={80}
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#1e293b',
                border: '1px solid #334155',
                borderRadius: '8px',
                color: '#f1f5f9',
              }}
              formatter={(value: number, _name: string, props) => {
                const sentiment = (props?.payload as Record<string, unknown>)?.sentiment as string | undefined;
                return [`${value}회`, sentiment ? `감정: ${sentiment}` : ''];
              }}
            />
            <Bar dataKey="count" radius={[0, 4, 4, 0]}>
              {chartData.map((entry, index) => (
                <Cell
                  key={`cell-${index}`}
                  fill={SENTIMENT_COLORS[entry.sentiment] ?? '#6b7280'}
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
