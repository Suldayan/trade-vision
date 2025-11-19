import React, { useState, useEffect } from "react";
import { useCallback } from "react";

interface SettingInputProps {
  label: string;
  value: number;
  onChange: (value: number) => void;
  step?: number;
  min?: number;
  max?: number;
  suffix?: string;
  multiplier?: number; 
}

export default function SettingInput({ 
  label,
  value,
  onChange,
  min,
  max,
  suffix,
  multiplier = 1
}: SettingInputProps) {
  const [inputValue, setInputValue] = useState<string>('');
  const [error, setError] = useState<string>('');

  useEffect(() => {
    setInputValue((value * multiplier).toString());
  }, [value, multiplier]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setInputValue(newValue);
    setError('');

    if (newValue === '') {
      return;
    }

    const numericValue = parseFloat(newValue);
    
    if (isNaN(numericValue)) {
      setError('Please enter a valid number');
      return;
    }

    const actualValue = numericValue / multiplier;
    
    if (min !== undefined && actualValue < min) {
      setError(`Value must be at least ${min}`);
      return;
    }
    if (max !== undefined && actualValue > max) {
      setError(`Value cannot exceed ${max}`);
      return;
    }
    
    onChange(actualValue);
  }, [onChange, multiplier, min, max]);

  const handleBlur = useCallback(() => {
    if (inputValue === '') {
      setError('This field is required');
    }
  }, [inputValue]);

  return (
    <div>
      <label className="block text-sm font-medium text-slate-300 mb-1">
        {label} {suffix && `(${suffix})`}
      </label>
      <input
        type="text"
        value={inputValue}
        onChange={handleChange}
        onBlur={handleBlur}
        placeholder={`Enter ${label.toLowerCase()}`}
        className={`w-full px-3 py-2 bg-white/10 border rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 transition-colors ${
          error 
            ? 'border-red-400 focus:ring-red-500' 
            : 'border-white/20 focus:ring-purple-500'
        }`}
      />
      {error && (
        <p className="mt-1 text-sm text-red-400">{error}</p>
      )}
    </div>
  );
}