import type { IndicatorParameter } from '../../types/strategy';

interface ConditionParameterProps {
  paramKey: string;
  paramDef: IndicatorParameter;
  value: number | boolean | string;
  onChange: (value: number | boolean | string) => void;
}

export default function ConditionParameter({ 
  paramDef, 
  value, 
  onChange 
}: ConditionParameterProps) {
  const getNumberValue = (): string => {
    if (typeof value === 'number') {
      return value.toString();
    }
    if (typeof value === 'string') {
      return value;
    }
    return paramDef.default?.toString() || '';
  };

  const getSelectValue = (): string => {
    return value?.toString() || paramDef.default?.toString() || '';
  };

  return (
    <div>
      <label className="block text-xs font-medium text-slate-400 mb-1">
        {paramDef.label}
      </label>
      
      {paramDef.type === 'boolean' ? (
        <label className="flex items-center">
          <input
            type="checkbox"
            checked={Boolean(value)}
            onChange={(e) => onChange(e.target.checked)}
            className="mr-2 rounded"
          />
          <span className="text-xs text-slate-300">{paramDef.label}</span>
        </label>
      ) : paramDef.options ? (
        <select
          value={getSelectValue()}
          onChange={(e) => onChange(e.target.value)}
        >
          {paramDef.options?.map((option) => (
            <option 
              key={typeof option === 'object' ? option.value : option} 
              value={typeof option === 'object' ? option.value : option}
            >
              {typeof option === 'object' ? option.label : option}
            </option>
          ))}
        </select>
      ) : (
        <input
          type="number"
          step={paramDef.step || 1}
          min={paramDef.min}
          max={paramDef.max}
          value={getNumberValue()}
          onChange={(e) => onChange(
            paramDef.type === 'number' ? parseFloat(e.target.value) || 0 : e.target.value
          )}
          className="w-full px-2 py-1 text-xs bg-white/5 border border-white/10 rounded text-white placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-purple-500"
        />
      )}
    </div>
  );
};