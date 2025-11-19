import ConditionParameter from './ConditionParameter';
import type { IndicatorDefinition } from '../../types/strategy';
import type { AnyCondition, Condition } from '../../types/strategy';

interface ConditionParametersProps {
  condition: AnyCondition;
  indicators: Record<string, IndicatorDefinition>;
  onParameterChange: (paramKey: string, value: number | boolean | string) => void;
}

export default function ConditionParameters({ 
  condition, 
  indicators, 
  onParameterChange 
}: ConditionParametersProps) {
  const indicatorDef = indicators[condition.type];
  if (!indicatorDef) return null;

  const isRegularCondition = (cond: AnyCondition): cond is Condition => {
    return 'parameters' in cond && !('conditions' in cond.parameters);
  };

  if (!isRegularCondition(condition)) {
    return null; 
  }

  return (
    <div className="grid grid-cols-2 gap-3 mt-3">
      {Object.entries(indicatorDef.parameters).map(([paramKey, paramDef]) => {
        const paramValue = (condition.parameters as Record<string, any>)[paramKey];
        
        if (paramDef.options && Array.isArray(paramDef.options)) {
          return (
            <div key={paramKey} className="flex flex-col">
              <label className="block text-xs font-medium text-slate-400 mb-1">
                {paramDef.label}
              </label>
              <select
                value={paramValue || paramDef.default}
                onChange={(e) => onParameterChange(paramKey, e.target.value)}
                className="w-full px-2 py-1 text-xs bg-white/5 border border-white/10 rounded text-white focus:outline-none focus:ring-1 focus:ring-purple-500"
                style={{
                  colorScheme: 'dark'
                }}
              >
                {paramDef.options.map((option: any) => (
                  <option 
                    key={option.value} 
                    value={option.value}
                    className="bg-gray-800 text-white"
                    style={{
                      backgroundColor: '#1f2937',
                      color: '#ffffff'
                    }}
                  >
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          );
        }

        return (
          <ConditionParameter
            key={paramKey}
            paramKey={paramKey}
            paramDef={paramDef}
            value={paramValue}
            onChange={(value) => onParameterChange(paramKey, value)}
          />
        );
      })}
    </div>
  );
};