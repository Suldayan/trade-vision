import { BackTestRequest, BackTestResult } from '../types/backtest';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const runBacktest = async (
  file: File, 
  request: BackTestRequest
): Promise<BackTestResult> => {
  const formData = new FormData();
  formData.append('file', file);
  
  const requestBlob = new Blob([JSON.stringify(request)], {
    type: 'application/json',
  });
  
  formData.append('request', requestBlob);
  
  try {
    const response = await fetch(`${API_URL}`, {
      method: 'POST',
      body: formData,
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }
    
    // Check if the response has content
    const contentType = response.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      throw new Error(`Expected JSON response but got ${contentType}`);
    }
    
    try {
      const result = await response.json();
      return result;
    } catch (parseError) {
      console.error('JSON parsing error:', parseError);
      throw new Error('Failed to parse response as JSON');
    }
  } catch (error) {
    console.error('Backtest API error:', error);
    throw error;
  }
};