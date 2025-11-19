import { Trash2 } from 'lucide-react';
import ConditionParameters from './ConditionParameters';
import type { IndicatorDefinition } from '../../types/strategy';
import type { AnyCondition } from '../../types/strategy';

interface SingleConditionProps {
  condition: AnyCondition;
  index: number;
  indicators: Record<string, IndicatorDefinition>;
  onConditionChange: (newType: string) => void;
  onParameterChange: (paramKey: string, value: number | boolean | string) => void;
  onRemove: () => void;
}

export default function SingleCondition({ 
  condition, 
  indicators,
  onConditionChange,
  onParameterChange,
  onRemove
}: SingleConditionProps) {
  return (
  <div className="bg-white/5 border border-white/20 rounded-lg p-3 mb-3">
    <div className="flex items-center justify-between mb-2">
      <select
        value={condition.type}
        onChange={(e) => onConditionChange(e.target.value)}
        className="flex-1 px-2 py-1 text-xs bg-white/10 border border-white/20 rounded text-white focus:outline-none focus:ring-1 focus:ring-purple-500 mr-2"
        style={{
          colorScheme: 'dark'
        }}
      >
        {Object.entries(indicators).map(([key, def]) => (
          <option 
            key={key} 
            value={key}
            className="bg-gray-800 text-white"
            style={{
              backgroundColor: '#1f2937',
              color: '#ffffff'
            }}
          >
            {def.name}
          </option>
        ))}
      </select>
      <button
        onClick={onRemove}
        className="text-red-400 hover:text-red-300 p-1"
      >
        <Trash2 className="h-4 w-4" />
      </button>
    </div>
    <ConditionParameters
      condition={condition}
      indicators={indicators}
      onParameterChange={onParameterChange}
    />
  </div>
)};
