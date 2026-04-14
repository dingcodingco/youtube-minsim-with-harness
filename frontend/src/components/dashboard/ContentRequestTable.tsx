import { useState } from 'react';
import { useRequests } from '../../hooks/useRequests';
import LoadingSpinner from '../common/LoadingSpinner';
import ErrorMessage from '../common/ErrorMessage';
import EmptyState from '../common/EmptyState';

interface ContentRequestTableProps {
  videoId: string;
}

export default function ContentRequestTable({ videoId }: ContentRequestTableProps) {
  const { data, loading, error, refetch } = useRequests(videoId);
  const [expandedIndex, setExpandedIndex] = useState<number | null>(null);

  if (loading) {
    return <LoadingSpinner rows={4} />;
  }

  if (error) {
    return <ErrorMessage message={error} onRetry={refetch} />;
  }

  if (data.length === 0) {
    return <EmptyState message="시청자 요청이 없습니다" />;
  }

  const sorted = [...data].sort((a, b) => b.count - a.count);

  return (
    <div>
      <h3 className="mb-4 text-sm font-medium text-slate-300">
        시청자 요청 콘텐츠
      </h3>
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-slate-700 text-xs text-slate-400">
              <th className="pb-2 pr-2 font-medium">순위</th>
              <th className="pb-2 pr-2 font-medium">주제</th>
              <th className="pb-2 pr-2 font-medium text-right">요청 수</th>
              <th className="pb-2 font-medium">추천 제목</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map((item, index) => (
              <>
                <tr
                  key={`row-${index}`}
                  onClick={() =>
                    setExpandedIndex(expandedIndex === index ? null : index)
                  }
                  className="cursor-pointer border-b border-slate-700/50 transition-colors hover:bg-slate-700/30"
                >
                  <td className="py-2.5 pr-2 text-slate-400">{index + 1}</td>
                  <td className="py-2.5 pr-2 font-medium text-slate-200">
                    {item.topic}
                  </td>
                  <td className="py-2.5 pr-2 text-right">
                    <span className="inline-flex items-center rounded-full bg-pulse-request/20 px-2 py-0.5 text-xs font-medium text-pulse-request">
                      {item.count}
                    </span>
                  </td>
                  <td className="py-2.5 text-slate-300">{item.suggestedTitle}</td>
                </tr>
                {expandedIndex === index && item.sampleComments.length > 0 && (
                  <tr key={`expanded-${index}`}>
                    <td colSpan={4} className="pb-3 pt-1">
                      <div className="ml-4 space-y-2 rounded-lg bg-slate-900/50 p-3">
                        <p className="text-xs font-medium text-slate-400">
                          관련 댓글
                        </p>
                        {item.sampleComments.map((comment, ci) => (
                          <div key={ci} className="text-sm">
                            <p className="text-slate-300">{comment.content}</p>
                            <p className="mt-0.5 text-xs text-slate-500">
                              {comment.author} · 좋아요 {comment.likeCount}
                            </p>
                          </div>
                        ))}
                      </div>
                    </td>
                  </tr>
                )}
              </>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
