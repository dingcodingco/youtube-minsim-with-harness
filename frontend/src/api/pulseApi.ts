import type {
  ChannelOverviewResponse,
  VideoSummaryResponse,
  SentimentDetailResponse,
  KeywordResponse,
  ContentRequestResponse,
  TrendPointResponse,
  CollectionResultResponse,
} from '../types/pulse';

const BASE_URL = '/api';

class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);

  if (!response.ok) {
    let code = 'UNKNOWN_ERROR';
    let message = `HTTP ${response.status}`;
    try {
      const errorBody = await response.json();
      code = errorBody.code ?? code;
      message = errorBody.message ?? message;
    } catch {
      // response body is not JSON — use default message
    }
    throw new ApiError(response.status, code, message);
  }

  return response.json() as Promise<T>;
}

export const pulseApi = {
  getOverview: () =>
    fetchJson<ChannelOverviewResponse>(`${BASE_URL}/analytics/overview`),

  getVideos: (sort?: string) =>
    fetchJson<VideoSummaryResponse[]>(
      `${BASE_URL}/videos${sort ? `?sort=${sort}` : ''}`
    ),

  getSentiment: (videoId: string) =>
    fetchJson<SentimentDetailResponse>(
      `${BASE_URL}/videos/${videoId}/sentiment`
    ),

  getKeywords: (videoId: string, limit?: number) =>
    fetchJson<KeywordResponse[]>(
      `${BASE_URL}/videos/${videoId}/keywords${limit ? `?limit=${limit}` : ''}`
    ),

  getRequests: (videoId: string) =>
    fetchJson<ContentRequestResponse[]>(
      `${BASE_URL}/videos/${videoId}/requests`
    ),

  getTrend: (params?: { videoId?: string; from?: string; to?: string }) => {
    const searchParams = new URLSearchParams();
    if (params?.videoId) searchParams.set('videoId', params.videoId);
    if (params?.from) searchParams.set('from', params.from);
    if (params?.to) searchParams.set('to', params.to);
    const query = searchParams.toString();
    return fetchJson<TrendPointResponse[]>(
      `${BASE_URL}/analytics/trend${query ? `?${query}` : ''}`
    );
  },

  collectComments: async (videoId: string): Promise<CollectionResultResponse> => {
    const response = await fetch(`${BASE_URL}/collect/${videoId}`, {
      method: 'POST',
    });

    if (!response.ok) {
      let code = 'UNKNOWN_ERROR';
      let message = `HTTP ${response.status}`;
      try {
        const errorBody = await response.json();
        code = errorBody.code ?? code;
        message = errorBody.message ?? message;
      } catch {
        // response body is not JSON
      }
      throw new ApiError(response.status, code, message);
    }

    return response.json() as Promise<CollectionResultResponse>;
  },
};

export { ApiError };
