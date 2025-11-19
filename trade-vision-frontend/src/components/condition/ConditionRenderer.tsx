import SingleCondition from './SingleCondition';
import LogicalGroupCondition from './LogicalGroupCondition';
import type { IndicatorDefinition } from '../../types/strategy';
import type { AnyCondition } from '../../types/strategy';
import { isLogicalGroup } from '../../utils/configHelpers';

interface ConditionRendererProps {
  condition: AnyCondition;
  index: number;
  indicators: Record<string, IndicatorDefinition>;
  onConditionChange: (newType: string, subIndex?: number) => void;
  onParameterChange: (paramKey: string, value: number | boolean | string, subIndex?: number) => void;
  onRemove: () => void;
  onAddSubCondition: () => void;  
  onRemoveSubCondition: (subIndex: number) => void;  
  onLogicalOperatorChange?: (newOperator: 'AND' | 'OR' | 'NOT') => void;
  isInGroup?: boolean;
}

export default function ConditionRenderer({
  condition,
  index,
  indicators,
  onConditionChange,
  onParameterChange,
  onRemove,
  onAddSubCondition,
  onRemoveSubCondition,
  onLogicalOperatorChange
}: ConditionRendererProps) {
  if (isLogicalGroup(condition)) {
    return (
      <LogicalGroupCondition
        condition={condition}
        index={index}
        indicators={indicators}
        onConditionChange={onConditionChange}
        onParameterChange={onParameterChange}
        onRemove={onRemove}
        onAddSubCondition={onAddSubCondition}
        onRemoveSubCondition={onRemoveSubCondition}
        onLogicalOperatorChange={onLogicalOperatorChange}
      />
    );
  }

  return (
    <SingleCondition
      condition={condition}
      index={index}
      indicators={indicators}
      onConditionChange={(newType) => onConditionChange(newType)}
      onParameterChange={onParameterChange}
      onRemove={onRemove}
    />
  );
};