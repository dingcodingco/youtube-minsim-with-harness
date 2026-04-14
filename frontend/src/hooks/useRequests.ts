import { useState, useEffect, useCallback } from 'react';
import type { ContentRequestResponse } from '../types/pulse';
import { pulseApi } from '../api/pulseApi';

export function useRequests(videoId: string | null) {
  const [data, setData] = useState<ContentRequestResponse[]>([]);
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
      const result = await pulseApi.getRequests(videoId);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '콘텐츠 요청 데이터를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, [videoId]);

  useEffect(() => {
    fetch();
  }, [fetch]);

  return { data, loading, error, refetch: fetch };
}
