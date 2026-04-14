import type { VideoSummaryResponse } from '../../types/pulse';

interface VideoSelectorProps {
  videos: VideoSummaryResponse[];
  selectedVideoId: string | null;
  onSelect: (videoId: string | null) => void;
}

function getSentimentColor(score: number): string {
  if (score >= 60) return 'bg-pulse-positive';
  if (score >= 30) return 'bg-yellow-500';
  return 'bg-pulse-negative';
}

export default function VideoSelector({
  videos,
  selectedVideoId,
  onSelect,
}: VideoSelectorProps) {
  return (
    <div className="relative">
      <select
        value={selectedVideoId ?? ''}
        onChange={(e) => onSelect(e.target.value || null)}
        className="appearance-none rounded-lg border border-slate-600 bg-slate-800 py-2 pl-4 pr-10 text-sm text-slate-200 transition-colors focus:border-pulse-request focus:outline-none focus:ring-1 focus:ring-pulse-request"
      >
        <option value="">전체 채널</option>
        {videos.map((video) => (
          <option key={video.videoId} value={video.videoId}>
            {video.title} ({video.sentimentScore.toFixed(1)}점)
          </option>
        ))}
      </select>
      <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
        <svg
          className="h-4 w-4 text-slate-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </div>
      {selectedVideoId && (
        <span
          className={`absolute -right-2 -top-2 h-3 w-3 rounded-full ${getSentimentColor(
            videos.find((v) => v.videoId === selectedVideoId)?.sentimentScore ?? 0
          )}`}
        />
      )}
    </div>
  );
}
