import { useState, useEffect, useCallback } from 'react';
import type { VideoSummaryResponse } from '../types/pulse';
import { pulseApi } from '../api/pulseApi';

export function useVideos(sort?: string) {
  const [data, setData] = useState<VideoSummaryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await pulseApi.getVideos(sort);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '영상 목록을 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, [sort]);

  useEffect(() => {
    fetch();
  }, [fetch]);

  return { data, loading, error, refetch: fetch };
}
