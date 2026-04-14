import { useState, useEffect, useCallback } from 'react';
import type { TrendPointResponse } from '../types/pulse';
import { pulseApi } from '../api/pulseApi';

export function useTrend(params?: {
  videoId?: string;
  from?: string;
  to?: string;
}) {
  const [data, setData] = useState<TrendPointResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const videoId = params?.videoId;
  const from = params?.from;
  const to = params?.to;

  const fetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await pulseApi.getTrend({ videoId, from, to });
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '트렌드 데이터를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, [videoId, from, to]);

  useEffect(() => {
    fetch();
  }, [fetch]);

  return { data, loading, error, refetch: fetch };
}
