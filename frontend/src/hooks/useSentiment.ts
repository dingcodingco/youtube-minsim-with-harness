import { useState, useEffect, useCallback } from 'react';
import type { SentimentDetailResponse } from '../types/pulse';
import { pulseApi } from '../api/pulseApi';

export function useSentiment(videoId: string | null) {
  const [data, setData] = useState<SentimentDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetch = useCallback(async () => {
    if (!videoId) {
      setData(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const result = await pulseApi.getSentiment(videoId);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '감정 분석 데이터를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, [videoId]);

  useEffect(() => {
    fetch();
  }, [fetch]);

  return { data, loading, error, refetch: fetch };
}
