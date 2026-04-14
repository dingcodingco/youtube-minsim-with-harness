import { useState, useEffect, useCallback } from 'react';
import type { KeywordResponse } from '../types/pulse';
import { pulseApi } from '../api/pulseApi';

export function useKeywords(videoId: string | null, limit?: number) {
  const [data, setData] = useState<KeywordResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetch = useCallback(async () => {
    if (!videoId) {
      setData([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const result = await pulseApi.getKeywords(videoId, limit);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '키워드 데이터를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, [videoId, limit]);

  useEffect(() => {
    fetch();
  }, [fetch]);

  return { data, loading, error, refetch: fetch };
}
