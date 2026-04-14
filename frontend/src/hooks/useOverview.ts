import { useState, useEffect, useCallback } from 'react';
import type { ChannelOverviewResponse } from '../types/pulse';
import { pulseApi } from '../api/pulseApi';

export function useOverview() {
  const [data, setData] = useState<ChannelOverviewResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await pulseApi.getOverview();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '채널 개요를 불러오지 못했습니다');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetch();
  }, [fetch]);

  return { data, loading, error, refetch: fetch };
}
