import { ChevronDown, ChevronRight, Plus } from 'lucide-react';
import ConditionRenderer from './ConditionRenderer';
import type { IndicatorDefinition } from '../../types/strategy';
import type { StrategyConfig, AnyCondition } from '../../types/strategy';
import { createDefaultCondition } from '../../utils/configHelpers';

const LOGICAL_OPERATORS: Array<'AND' | 'OR' | 'NOT'> = ['AND', 'OR', 'NOT'];

interface ConditionsSectionProps {
  title: string;
  conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>;
  conditions: AnyCondition[];
  indicators: Record<string, IndicatorDefinition>;
  isExpanded: boolean;
  onToggleExpanded: () => void;
  onAddCondition: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>) => void;
  onAddLogicalGroup: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, operator: 'AND' | 'OR' | 'NOT') => void;
  onConditionChange: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, index: number, newCondition: AnyCondition) => void;
  onParameterChange: (path: string, value: any) => void;
  onRemoveCondition: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, index: number) => void;
  onAddConditionToGroup: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, groupIndex: number) => void;
  onRemoveConditionFromGroup: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, groupIndex: number, conditionIndex: number) => void;
  onNotGroupConditionChange: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, groupIndex: number, newCondition: any) => void;
  onLogicalOperatorChange: (conditionsPath: keyof Pick<StrategyConfig, 'entryConditions' | 'exitConditions'>, index: number, newOperator: 'AND' | 'OR' | 'NOT') => void;
}

export default function ConditionsSection({ 
  title,
  conditionsPath,
  conditions,
  indicators,
  isExpanded,
  onToggleExpanded,
  onAddCondition,
  onAddLogicalGroup,
  onConditionChange,
  onParameterChange,
  onRemoveCondition,
  onAddConditionToGroup,
  onRemoveConditionFromGroup,
  onNotGroupConditionChange,
  onLogicalOperatorChange
}: ConditionsSectionProps) {
  const handleConditionChange = (index: number, newType: string, subIndex?: number) => {
    const condition = conditions[index];
    
    // Handle NOT group condition change (unary)
    if (condition.type === 'NOT' && subIndex === undefined) {
      const newCondition = createDefaultCondition(newType, indicators);
      onNotGroupConditionChange(conditionsPath, index, newCondition);
      return;
    }
    
    // Handle regular condition change or AND/OR group sub-condition change
    if (subIndex !== undefined) {
      const newCondition = createDefaultCondition(newType, indicators);
      onParameterChange(`${conditionsPath}[${index}].parameters.conditions[${subIndex}]`, newCondition);
    } else {
      const newCondition = createDefaultCondition(newType, indicators);
      onConditionChange(conditionsPath, index, newCondition);
    }
  };

  const handleParameterChange = (index: number, paramKey: string, value: number | boolean | string, subIndex?: number) => {
    const condition = conditions[index];
    
    // Handle NOT group parameter change (unary)
    if (condition.type === 'NOT' && subIndex === undefined) {
      onParameterChange(`${conditionsPath}[${index}].parameters.condition.parameters.${paramKey}`, value);
      return;
    }
    
    // Handle regular parameter change or AND/OR group sub-condition parameter change
    if (subIndex !== undefined) {
      onParameterChange(`${conditionsPath}[${index}].parameters.conditions[${subIndex}].parameters.${paramKey}`, value);
    } else {
      onParameterChange(`${conditionsPath}[${index}].parameters.${paramKey}`, value);
    }
  };

  const handleLogicalOperatorChange = (index: number, newOperator: 'AND' | 'OR' | 'NOT') => {
    onLogicalOperatorChange(conditionsPath, index, newOperator);
  };

  return (
    <div className="mb-6">
      <div 
        className="flex items-center justify-between cursor-pointer mb-3"
        onClick={onToggleExpanded}
      >
        <h3 className="text-lg font-semibold text-white flex items-center gap-2">
          {isExpanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          {title}
        </h3>
      </div>
      
      {isExpanded && (
        <div>
          {conditions.map((condition, index) => (
            <ConditionRenderer
              key={index}
              condition={condition}
              index={index}
              indicators={indicators}
              onConditionChange={(newType, subIndex) => handleConditionChange(index, newType, subIndex)}
              onParameterChange={(paramKey, value, subIndex) => handleParameterChange(index, paramKey, value, subIndex)}
              onRemove={() => onRemoveCondition(conditionsPath, index)}
              onAddSubCondition={() => onAddConditionToGroup(conditionsPath, index)}
              onRemoveSubCondition={(subIndex) => onRemoveConditionFromGroup(conditionsPath, index, subIndex)}
              onLogicalOperatorChange={(newOperator) => handleLogicalOperatorChange(index, newOperator)}
            />
          ))}
          
          <div className="flex gap-2 mt-3">
            <button
              onClick={() => onAddCondition(conditionsPath)}
              className="px-3 py-2 text-xs bg-green-600 hover:bg-green-700 text-white rounded flex items-center gap-1"
            >
              <Plus className="h-3 w-3" />
              Add Condition
            </button>
            
            <select
              onChange={(e) => {
                const value = e.target.value as 'AND' | 'OR' | 'NOT';
                if (value) {
                  onAddLogicalGroup(conditionsPath, value);
                  e.target.value = '';
                }
              }}
              className="px-3 py-2 text-xs bg-blue-600 hover:bg-blue-700 text-white rounded focus:outline-none"
              defaultValue=""
            >
              <option value="" disabled>Add Logic Group</option>
              {LOGICAL_OPERATORS.map(op => (
                <option key={op} value={op}>{op} Group</option>
              ))}
            </select>
          </div>
        </div>
      )}
    </div>
  );
};