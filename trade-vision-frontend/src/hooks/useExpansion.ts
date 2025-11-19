import { useState, useCallback } from 'react';

interface ExpandedSections {
  entry: boolean;
  exit: boolean;
}

export const useExpansion = () => {
  const [expandedSections, setExpandedSections] = useState<ExpandedSections>({
    entry: true,
    exit: true
  });

  const toggleSection = useCallback((section: keyof ExpandedSections): void => {
    setExpandedSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  }, []);

  return {
    expandedSections,
    toggleSection
  };
};