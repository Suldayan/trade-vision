import React from 'react';
import { motion } from 'framer-motion';
import { CONDITION_TYPES, getConditionColor } from '../../constants/condition-constants';
import { nestedContainerVariants } from '../utils/animation-variants';
import { shouldHideParam } from '../utils/parameter-utils';
import ParameterField from './ParameterField';
import { ConditionConfig } from '../../../types/backtest';

interface NotConditionProps {
  condition: ConditionConfig;
  index: number;
  isEntry: boolean;
  onUpdateNotCondition: (parentIndex: number, conditionType: string, isEntry: boolean) => void;
  onUpdateNotChildParameter: (parentIndex: number, paramName: string, value: any, isEntry: boolean) => void;
}

const NotCondition: React.FC<NotConditionProps> = ({
  condition,
  index,
  isEntry,
  onUpdateNotCondition,
  onUpdateNotChildParameter
}) => {
  const childCondition = condition.parameters.condition;
  
  if (!childCondition) {
    return (
      <motion.div 
        className="ml-4 border-l-2 pl-4 border-red-600 py-2"
        variants={nestedContainerVariants}
        initial="hidden"
        animate="visible"
      >
        <div className="flex justify-between items-center mb-3">
          <span className="font-medium text-red-400">Child Condition (Required)</span>
          <select
            onChange={(e) => onUpdateNotCondition(index, e.target.value, isEntry)}
            className="p-2 text-sm bg-gray-800 border border-gray-700 text-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
            value=""
          >
            <option value="" disabled>Select type</option>
            {CONDITION_TYPES.filter(t => t !== 'NOT').map(type => (
              <option key={type} value={type}>{type.replace('_', ' ')}</option>
            ))}
          </select>
        </div>
      </motion.div>
    );
  }
  
  return (
    <motion.div 
      className="ml-4 border-l-2 pl-4 border-red-600 py-2"
      variants={nestedContainerVariants}
      initial="hidden"
      animate="visible"
    >
      <div className="flex justify-between items-center mb-3">
        <div className="flex items-center">
          <span className="text-red-400 font-bold mr-2">NOT</span>
          <motion.div 
            className={`h-4 w-4 rounded-full bg-gradient-to-br ${getConditionColor(childCondition.type)}`}
            animate={{ scale: [1, 1.1, 1] }}
            transition={{ duration: 1, repeat: Infinity, repeatType: "reverse" }}
          />
        </div>
        <select
          value={childCondition.type}
          onChange={(e) => onUpdateNotCondition(index, e.target.value, isEntry)}
          className={`p-2 text-sm bg-gray-800 border border-gray-700 text-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500`}
        >
          {CONDITION_TYPES.filter(t => t !== 'NOT').map(type => (
            <option key={type} value={type}>{type.replace('_', ' ')}</option>
          ))}
        </select>
      </div>
      
      <div className="bg-gray-800 bg-opacity-50 p-3 rounded-md">
        {Object.entries(childCondition.parameters).map(([paramName, paramValue], pIndex) => {
          // Skip hidden parameters
          if (shouldHideParam(paramName)) return null;
          
          return (
            <div key={paramName}>
              <ParameterField
                conditionType={childCondition.type}
                paramName={paramName}
                paramValue={paramValue}
                onChange={(value) => onUpdateNotChildParameter(index, paramName, value, isEntry)}
                paramIndex={pIndex}
              />
            </div>
          );
        })}
      </div>
    </motion.div>
  );
};

export default NotCondition;