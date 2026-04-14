import { useState } from 'react';
import Header from './components/layout/Header';
import VideoSelector from './components/dashboard/VideoSelector';
import SentimentGauge from './components/dashboard/SentimentGauge';
import SentimentPieChart from './components/dashboard/SentimentPieChart';
import TrendLineChart from './components/dashboard/TrendLineChart';
import KeywordRanking from './components/dashboard/KeywordRanking';
import ContentRequestTable from './components/dashboard/ContentRequestTable';
import LoadingSpinner from './components/common/LoadingSpinner';
import ErrorMessage from './components/common/ErrorMessage';
import EmptyState from './components/common/EmptyState';
import { useOverview } from './hooks/useOverview';
import { useVideos } from './hooks/useVideos';
import { useSentiment } from './hooks/useSentiment';

export default function App() {
  const [selectedVideoId, setSelectedVideoId] = useState<string | null>(null);

  const { data: overview, loading: overviewLoading, error: overviewError, refetch: refetchOverview } = useOverview();
  const { data: videos, loading: videosLoading } = useVideos();
  const { data: sentiment, loading: sentimentLoading, error: sentimentError, refetch: refetchSentiment } = useSentiment(selectedVideoId);

  // Determine gauge score and total comments
  const gaugeScore = selectedVideoId
    ? (sentiment?.totalAnalyzed
        ? computeScoreFromDistribution(sentiment.distribution)
        : 0)
    : (overview?.avgSentiment ?? 0);

  const totalComments = selectedVideoId
    ? (sentiment?.totalAnalyzed ?? 0)
    : (overview?.totalComments ?? 0);

  const isOverviewMode = selectedVideoId === null;
  const showLoading = isOverviewMode ? overviewLoading : sentimentLoading;
  const showError = isOverviewMode ? overviewError : sentimentError;

  return (
    <div className="flex min-h-screen flex-col bg-pulse-bg font-sans">
      <Header>
        <VideoSelector
          videos={videos}
          selectedVideoId={selectedVideoId}
          onSelect={setSelectedVideoId}
        />
      </Header>

      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-6 sm:px-6">
        {showLoading && videosLoading ? (
          <div className="rounded-xl bg-pulse-card p-8">
            <LoadingSpinner rows={6} />
          </div>
        ) : showError ? (
          <div className="rounded-xl bg-pulse-card p-8">
            <ErrorMessage
              message={showError}
              onRetry={isOverviewMode ? refetchOverview : refetchSentiment}
            />
          </div>
        ) : !overview && isOverviewMode ? (
          <div className="rounded-xl bg-pulse-card p-8">
            <EmptyState />
          </div>
        ) : (
          <div className="space-y-6">
            {/* Row 1: Gauge + Pie Chart */}
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              {/* Sentiment Gauge */}
              <section className="rounded-xl bg-pulse-card p-6 shadow">
                <h2 className="mb-4 text-sm font-medium text-slate-400">
                  감정 온도계
                </h2>
                {showLoading ? (
                  <LoadingSpinner rows={3} />
                ) : (
                  <SentimentGauge
                    score={gaugeScore}
                    totalComments={totalComments}
                  />
                )}
              </section>

              {/* Sentiment Distribution */}
              <section className="rounded-xl bg-pulse-card p-6 shadow">
                <h2 className="mb-4 text-sm font-medium text-slate-400">
                  감정 분포
                </h2>
                {!selectedVideoId ? (
                  <EmptyState message="영상을 선택하면 감정 분포를 확인할 수 있습니다" />
                ) : sentimentLoading ? (
                  <LoadingSpinner rows={3} />
                ) : sentimentError ? (
                  <ErrorMessage message={sentimentError} onRetry={refetchSentiment} />
                ) : sentiment ? (
                  <SentimentPieChart data={sentiment} />
                ) : (
                  <EmptyState />
                )}
              </section>
            </div>

            {/* Row 2: Trend Chart (full width) */}
            <section className="rounded-xl bg-pulse-card p-6 shadow">
              <TrendLineChart videoId={selectedVideoId ?? undefined} />
            </section>

            {/* Row 3: Keywords + Content Requests */}
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              {/* Keyword Ranking */}
              <section className="rounded-xl bg-pulse-card p-6 shadow">
                {!selectedVideoId ? (
                  <>
                    <h3 className="mb-4 text-sm font-medium text-slate-300">
                      키워드 랭킹 Top 10
                    </h3>
                    <EmptyState message="영상을 선택하면 키워드 랭킹을 확인할 수 있습니다" />
                  </>
                ) : (
                  <KeywordRanking videoId={selectedVideoId} />
                )}
              </section>

              {/* Content Request Table */}
              <section className="rounded-xl bg-pulse-card p-6 shadow">
                {!selectedVideoId ? (
                  <>
                    <h3 className="mb-4 text-sm font-medium text-slate-300">
                      시청자 요청 콘텐츠
                    </h3>
                    {overview && overview.pendingRequests.length > 0 ? (
                      <OverviewRequestsList
                        requests={overview.pendingRequests}
                      />
                    ) : (
                      <EmptyState message="영상을 선택하면 시청자 요청을 확인할 수 있습니다" />
                    )}
                  </>
                ) : (
                  <ContentRequestTable videoId={selectedVideoId} />
                )}
              </section>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

/** Compute an approximate sentiment score from distribution percentages */
function computeScoreFromDistribution(distribution: {
  positive: number;
  negative: number;
  neutral: number;
  request: number;
  question: number;
}): number {
  // Weighted formula: positive contributes fully, neutral at 50%, others less
  const score =
    distribution.positive * 1.0 +
    distribution.neutral * 0.5 +
    distribution.question * 0.5 +
    distribution.request * 0.3 +
    distribution.negative * 0.0;
  return Math.min(100, Math.max(0, score));
}

/** Simple list of pending requests shown in overview mode */
function OverviewRequestsList({
  requests,
}: {
  requests: { topic: string; count: number; suggestedTitle: string }[];
}) {
  return (
    <div className="space-y-3">
      {requests.map((req, i) => (
        <div key={i} className="rounded-lg bg-slate-900/50 p-3">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-slate-200">
              {req.topic}
            </span>
            <span className="inline-flex items-center rounded-full bg-pulse-request/20 px-2 py-0.5 text-xs font-medium text-pulse-request">
              {req.count}건
            </span>
          </div>
          <p className="mt-1 text-xs text-slate-400">{req.suggestedTitle}</p>
        </div>
      ))}
    </div>
  );
}
