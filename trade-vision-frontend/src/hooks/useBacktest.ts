import { useState, useCallback, useEffect } from 'react';
import { type BacktestConfig } from './useConfig';
import { type BacktestResults } from '../types/strategy';

export const useBacktest = () => {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [results, setResults] = useState<BacktestResults[] | null>(null);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    console.log('[useBacktest] File state changed:', {
      file: file ? { name: file.name, size: file.size, type: file.type } : null
    });
  }, [file]);

  useEffect(() => {
    console.log('[useBacktest] Loading state changed:', loading);
  }, [loading]);

  useEffect(() => {
    console.log('[useBacktest] Error state changed:', error);
  }, [error]);

  const handleFileUpload = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    console.log('[useBacktest] handleFileUpload called');
    
    const selectedFile = event.target.files?.[0];
    console.log('[useBacktest] Selected file:', selectedFile ? {
      name: selectedFile.name,
      size: selectedFile.size,
      type: selectedFile.type
    } : null);

    if (selectedFile) {
      const isCSV = selectedFile.type === 'text/csv' || 
                   selectedFile.type === 'application/vnd.ms-excel' ||
                   selectedFile.name.toLowerCase().endsWith('.csv');
      
      console.log('[useBacktest] File type check:', {
        type: selectedFile.type,
        name: selectedFile.name,
        isCSV
      });

      if (isCSV) {
        setFile(selectedFile);
        setError('');
        console.log('[useBacktest] File accepted and set');
      } else {
        setError('Please select a valid CSV file');
        setFile(null);
        console.log('[useBacktest] File rejected - invalid type');
      }
    } else {
      setError('No file selected');
      setFile(null);
      console.log('[useBacktest] No file selected');
    }

    // Clear the input value to allow re-uploading the same file
    event.target.value = '';
  }, []);

  const handleFileSelect = useCallback((selectedFile: File | null) => {
    console.log('[useBacktest] handleFileSelect called with:', selectedFile);
    
    if (selectedFile) {
      const isCSV = selectedFile.type === 'text/csv' || 
                   selectedFile.type === 'application/vnd.ms-excel' ||
                   selectedFile.name.toLowerCase().endsWith('.csv');
      
      if (isCSV) {
        setFile(selectedFile);
        setError('');
        console.log('[useBacktest] File accepted via direct selection');
      } else {
        setError('Please select a valid CSV file');
        setFile(null);
        console.log('[useBacktest] File rejected via direct selection - invalid type');
      }
    } else {
      setFile(null);
      setError('');
      console.log('[useBacktest] File cleared via direct selection');
    }
  }, []);

  const runBacktest = useCallback(async (configs: BacktestConfig[]): Promise<void> => {
    console.log('[useBacktest] runBacktest called with:', {
      file: file ? { name: file.name, size: file.size } : null,
      configsLength: configs.length,
      loading
    });

    if (!file) {
      const errorMsg = 'Please upload a CSV file first';
      setError(errorMsg);
      console.log('[useBacktest] Backtest blocked - no file');
      return;
    }

    if (loading) {
      console.log('[useBacktest] Backtest blocked - already loading');
      return;
    }

    setLoading(true);
    setError('');
    console.log('[useBacktest] Starting backtest...');

    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('request', new Blob([JSON.stringify(configs)], {
        type: 'application/json'
      }));

      console.log('[useBacktest] Sending request to backend...');

      const response = await fetch('http://localhost:8080/api/backtest/execute', {
        method: 'POST',
        body: formData,
      });

      console.log('[useBacktest] Response received:', {
        status: response.status,
        statusText: response.statusText,
        ok: response.ok
      });

      if (!response.ok) {
        const errorText = await response.text().catch(() => 'Unknown error');
        throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
      }

      const result: BacktestResults[] = await response.json();
      console.log('[useBacktest] Backtest completed successfully:', result);
      setResults(result);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      const fullError = `Failed to run backtest: ${errorMessage}`;
      setError(fullError);
      console.error('[useBacktest] Backtest error:', err);
    } finally {
      setLoading(false);
      console.log('[useBacktest] Backtest finished (loading set to false)');
    }
  }, [file, loading]);

  const canRunBacktest = useCallback(() => {
    const canRun = !loading && file !== null;
    console.log('[useBacktest] canRunBacktest:', {
      loading,
      hasFile: !!file,
      canRun
    });
    return canRun;
  }, [loading, file]);

  return {
    file,
    loading,
    results,
    error,
    handleFileUpload,
    handleFileSelect, 
    runBacktest,
    canRunBacktest, 
  };
};