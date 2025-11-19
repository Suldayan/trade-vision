import { Trash2, Plus } from 'lucide-react';
import type { AnyCondition as LogicalGroupType, IndicatorDefinition } from '../../types/strategy';
import ConditionRenderer from './ConditionRenderer';

const LOGICAL_OPERATORS: Array<'AND' | 'OR' | 'NOT'> = ['AND', 'OR', 'NOT'];

interface LogicalGroupConditionProps {
  condition: LogicalGroupType;
  index: number;
  indicators: Record<string, IndicatorDefinition>;
  onConditionChange: (newType: string, subIndex?: number) => void;
  onParameterChange: (paramKey: string, value: number | boolean | string, subIndex?: number) => void;
  onRemove: () => void;
  onAddSubCondition: () => void;
  onRemoveSubCondition: (subIndex: number) => void;
  onLogicalOperatorChange?: (newOperator: 'AND' | 'OR' | 'NOT') => void;
}

export default function LogicalGroupCondition({ 
  condition, 
  indicators,
  onConditionChange,
  onParameterChange,
  onRemove,
  onAddSubCondition,
  onRemoveSubCondition,
  onLogicalOperatorChange
}: LogicalGroupConditionProps) {
  const handleOperatorChange = (newOperator: 'AND' | 'OR' | 'NOT') => {
    if (onLogicalOperatorChange) {
      onLogicalOperatorChange(newOperator);
    }
  };

  // Type guard to check if parameters has conditions property (AND/OR groups)
  const hasConditions = (params: any): params is { conditions: any[] } => {
    return params && Array.isArray(params.conditions);
  };

  // Type guard to check if parameters has a single condition property (NOT groups)
  const hasCondition = (params: any): params is { condition: any } => {
    return params && params.condition && !Array.isArray(params.condition);
  };

  const isNotGroup = condition.type === 'NOT';

  return (
    <div className="bg-white/5 border border-purple-400/30 rounded-lg p-4 mb-3">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className="px-2 py-1 bg-purple-500/20 text-purple-300 text-xs font-medium rounded">
            {condition.type}
          </div>
          <select
            value={condition.type}
            onChange={(e) => handleOperatorChange(e.target.value as 'AND' | 'OR' | 'NOT')}
            className="px-2 py-1 text-xs bg-white/10 border border-white/20 rounded text-white focus:outline-none focus:ring-1 focus:ring-purple-500"
          >
            {LOGICAL_OPERATORS.map(op => (
              <option key={op} value={op} className="text-white bg-gray-800">
                {op}
              </option>
            ))}
          </select>
        </div>
        <button
          onClick={onRemove}
          className="text-red-400 hover:text-red-300 p-1"
        >
          <Trash2 className="h-4 w-4" />
        </button>
      </div>
      
      {/* Handle NOT groups - render single condition */}
      {isNotGroup && hasCondition(condition.parameters) && (
        <ConditionRenderer
          condition={condition.parameters.condition}
          index={0}
          indicators={indicators}
          onConditionChange={(newType) => onConditionChange(newType, undefined)} // No subIndex for NOT groups
          onParameterChange={(paramKey, value) => onParameterChange(paramKey, value, undefined)} // No subIndex for NOT groups
          onRemove={() => {}} // NOT groups can't remove their single condition
          onAddSubCondition={onAddSubCondition}
          onRemoveSubCondition={onRemoveSubCondition}
          onLogicalOperatorChange={onLogicalOperatorChange}
          isInGroup={true}
        />
      )}
      
      {/* Handle AND/OR groups - render conditions array */}
      {!isNotGroup && hasConditions(condition.parameters) && condition.parameters.conditions.map((subCondition, subIndex) => (
        <ConditionRenderer
          key={subIndex}
          condition={subCondition}
          index={subIndex}
          indicators={indicators}
          onConditionChange={(newType) => onConditionChange(newType, subIndex)}
          onParameterChange={(paramKey, value) => onParameterChange(paramKey, value, subIndex)}
          onRemove={() => onRemoveSubCondition(subIndex)}
          onAddSubCondition={onAddSubCondition}
          onRemoveSubCondition={onRemoveSubCondition}
          onLogicalOperatorChange={onLogicalOperatorChange}
          isInGroup={true}
        />
      ))}
      
      {/* Only show "Add Sub-Condition" button for AND/OR groups, not for NOT groups */}
      {!isNotGroup && (
        <button
          onClick={onAddSubCondition}
          className="mt-2 text-xs text-purple-400 hover:text-purple-300 flex items-center gap-1"
        >
          <Plus className="h-3 w-3" />
          Add Sub-Condition
        </button>
      )}
    </div>
  );
};