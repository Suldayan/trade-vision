import React from 'react';
import { shouldHideParam } from '../utils/parameter-utils';
import ParameterField from './ParameterField';
import { ConditionConfig } from '../../../types/backtest';

interface StandardConditionProps {
  condition: ConditionConfig;
  index: number;
  isEntry: boolean;
  onParameterChange: (index: number, paramName: string, value: any, isEntry: boolean) => void;
}

const StandardCondition: React.FC<StandardConditionProps> = ({
  condition,
  index,
  isEntry,
  onParameterChange
}) => {
  const { type, parameters } = condition;
  
  return (
    <div className="bg-gray-800 bg-opacity-50 p-3 rounded-md">
      {Object.entries(parameters).map(([paramName, paramValue], pIndex) => {
        // Skip hidden parameters
        if (shouldHideParam(paramName)) return null;
        
        return (
          <div key={paramName}>
            <ParameterField
              conditionType={type}
              paramName={paramName}
              paramValue={paramValue}
              onChange={(value) => onParameterChange(index, paramName, value, isEntry)}
              paramIndex={pIndex}
            />
          </div>
        );
      })}
    </div>
  );
};

export default StandardCondition;