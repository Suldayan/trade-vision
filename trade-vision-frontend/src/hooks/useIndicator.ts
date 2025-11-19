import { useState, useCallback, useEffect } from 'react';
import { type StrategyModel } from '../types/StrategyModel.types';

interface UseIndicatorsReturn {
  indicators: StrategyModel[] | null;
  loading: boolean;
  error: string | null;
  fetchIndicators: () => Promise<void>;
  refetch: () => Promise<void>;
}

export const useIndicators = (): UseIndicatorsReturn => {
  const [indicators, setIndicators] = useState<StrategyModel[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const API_STRATEGY_URL = import.meta.env.VITE_API_URL;

  const fetchIndicators = useCallback(async (): Promise<void> => {
    console.log("[useIndicators] Fetching indicators from backend...");
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(API_STRATEGY_URL, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      console.log('[useIndicators] Response received:', {
        status: response.status,
        statusText: response.statusText,
        ok: response.ok
      });

      if (!response.ok) {
        const errorText = await response.text().catch(() => 'Unknown error');
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }

      const result: StrategyModel[] = await response.json();
      console.log('[useIndicators] Indicators fetched successfully:', result);
      setIndicators(result);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      const fullError = `Failed to fetch indicators: ${errorMessage}`;
      setError(fullError);
      console.error('[useIndicators] Error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchIndicators();
  }, [fetchIndicators]);

  return {
    indicators,
    loading,
    error,
    fetchIndicators,
    refetch: fetchIndicators 
  };
};

export const useIndicator = (key: string) => {
  const { indicators, loading, error } = useIndicators();
  
  const indicator = indicators?.find(ind => ind.key === key) || null;
  
  return {
    indicator,
    loading,
    error,
    exists: indicator !== null
  };
};

export const convertIndicatorsToStrategyModels = (indicators: any): StrategyModel[] => {
  return Object.entries(indicators).map(([key, indicator]: [string, any]) => ({
    key,
    name: indicator.name,
    description: indicator.description,
    parameters: Object.entries(indicator.parameters).reduce((acc, [paramKey, param]: [string, any]) => {
      acc[paramKey] = {
        type: param.type,
        defaultValue: param.default,
        label: param.label,
        step: param.step,
        options: param.options
      };
      return acc;
    }, {} as Record<string, any>)
  }));
};