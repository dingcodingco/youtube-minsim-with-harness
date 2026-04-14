// ===== Shared Sub-Types =====

export interface VideoInfo {
  videoId: string;
  title: string;
  sentimentScore: number;
  commentCount: number;
}

export interface SampleComment {
  author: string;
  content: string;
  likeCount: number;
}

export interface PendingRequest {
  topic: string;
  count: number;
  suggestedTitle: string;
}

// ===== API Response Types =====

/** GET /api/analytics/overview */
export interface ChannelOverviewResponse {
  totalComments: number;
  avgSentiment: number;
  topPositiveVideo: VideoInfo | null;
  topNegativeVideo: VideoInfo | null;
  hottestKeywords: string[];
  pendingRequests: PendingRequest[];
}

/** GET /api/videos */
export interface VideoSummaryResponse {
  videoId: string;
  title: string;
  publishedAt: string;
  commentCount: number;
  sentimentScore: number;
  topKeywords: string[];
}

/** GET /api/videos/{videoId}/sentiment */
export interface SentimentDetailResponse {
  videoId: string;
  title: string;
  totalAnalyzed: number;
  distribution: {
    positive: number;
    negative: number;
    neutral: number;
    request: number;
    question: number;
  };
  sampleComments: {
    positive?: SampleComment[];
    negative?: SampleComment[];
    neutral?: SampleComment[];
    request?: SampleComment[];
    question?: SampleComment[];
  };
}

/** GET /api/videos/{videoId}/keywords */
export interface KeywordResponse {
  word: string;
  count: number;
  sentiment: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL' | 'REQUEST' | 'QUESTION';
}

/** GET /api/videos/{videoId}/requests */
export interface ContentRequestResponse {
  topic: string;
  count: number;
  sampleComments: SampleComment[];
  suggestedTitle: string;
}

/** GET /api/analytics/trend */
export interface TrendPointResponse {
  date: string;
  sentimentScore: number;
  commentCount: number;
}

/** POST /api/collect/{videoId} */
export interface CollectionResultResponse {
  videoId: string;
  newComments: number;
  totalComments: number;
  status: 'COMPLETED' | 'FAILED';
}

/** Error response format */
export interface ErrorResponse {
  code: string;
  message: string;
}
