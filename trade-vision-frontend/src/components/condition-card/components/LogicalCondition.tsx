import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { CONDITION_TYPES, getConditionColor } from '../../constants/condition-constants';
import { cardVariants, nestedContainerVariants, glowAnimation } from '../utils/animation-variants';
import { shouldHideParam } from '../utils/parameter-utils';
import ParameterField from './ParameterField';
import { ConditionConfig } from '../../../types/backtest';

interface LogicalConditionProps {
  condition: ConditionConfig;
  index: number;
  isEntry: boolean;
  onAddNestedCondition: (parentIndex: number, conditionType: string, isEntry: boolean) => void;
  onUpdateNestedConditionType: (parentIndex: number, childIndex: number, newType: string, isEntry: boolean) => void;
  onUpdateNestedConditionParameter: (parentIndex: number, childIndex: number, paramName: string, value: any, isEntry: boolean) => void;
  onRemoveNestedCondition: (parentIndex: number, childIndex: number, isEntry: boolean) => void;
}

const LogicalCondition: React.FC<LogicalConditionProps> = ({
  condition,
  index,
  isEntry,
  onAddNestedCondition,
  onUpdateNestedConditionType,
  onUpdateNestedConditionParameter,
  onRemoveNestedCondition
}) => {
  const { type, parameters } = condition;
  const bgColor = type === 'AND' ? 'border-blue-600' : 'border-indigo-600';
  const btnColor = type === 'AND' ? 'bg-blue-800 hover:bg-blue-700' : 'bg-indigo-800 hover:bg-indigo-700';
  
  return (
    <motion.div 
      className={`ml-4 border-l-2 pl-4 ${bgColor} py-2`}
      variants={nestedContainerVariants}
      initial="hidden"
      animate="visible"
    >
      <div className="flex justify-between items-center mb-3">
        <div className="flex items-center">
          <span className={`font-medium ${type === 'AND' ? 'text-blue-400' : 'text-indigo-400'}`}>
            {type === 'AND' ? 'All Conditions Must Match' : 'Any Condition Can Match'}
          </span>
        </div>
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          type="button"
          className={`text-sm ${btnColor} text-gray-200 py-1.5 px-3 rounded-md flex items-center`}
          onClick={() => onAddNestedCondition(index, 'SMA_CROSSOVER', isEntry)}
        >
          <svg className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Add Condition
        </motion.button>
      </div>
      
      <AnimatePresence>
        {parameters.conditions && parameters.conditions.length > 0 ? (
          <div className="space-y-3">
            {parameters.conditions.map((nestedCondition: ConditionConfig, nestedIndex: number) => (
              <motion.div 
                key={`nested-${nestedIndex}`}
                className={`border border-gray-700 rounded-md p-3 bg-gray-800 overflow-hidden`}
                variants={cardVariants}
                initial="hidden"
                animate="visible"
                exit="exit"
                custom={nestedIndex}
                layout
              >
                <div className="flex justify-between items-center mb-3">
                  <div className="flex items-center">
                    <motion.div 
                      className={`h-3 w-3 rounded-full bg-gradient-to-br ${getConditionColor(nestedCondition.type)} mr-2`}
                      animate={glowAnimation(getConditionColor(nestedCondition.type).split(' ')[0].replace('from-', ''))}
                    />
                    <select
                      value={nestedCondition.type}
                      onChange={(e) => onUpdateNestedConditionType(index, nestedIndex, e.target.value, isEntry)}
                      className="p-2 text-sm bg-gray-800 border border-gray-700 text-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      {CONDITION_TYPES.filter(t => t !== 'AND' && t !== 'OR').map(type => (
                        <option key={type} value={type}>{type.replace('_', ' ')}</option>
                      ))}
                    </select>
                  </div>
                  <motion.button
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.9 }}
                    type="button"
                    className="text-sm text-red-400 hover:text-red-300 bg-gray-900 py-1 px-2 rounded-md"
                    onClick={() => onRemoveNestedCondition(index, nestedIndex, isEntry)}
                  >
                    <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </motion.button>
                </div>
                
                <div className="bg-gray-900 bg-opacity-50 p-3 rounded-md">
                  {Object.entries(nestedCondition.parameters).map(([paramName, paramValue], pIndex) => {
                    // Skip hidden parameters
                    if (shouldHideParam(paramName)) return null;
                    
                    return (
                      <div key={paramName}>
                        <ParameterField
                          conditionType={nestedCondition.type}
                          paramName={paramName}
                          paramValue={paramValue}
                          onChange={(value) => onUpdateNestedConditionParameter(index, nestedIndex, paramName, value, isEntry)}
                          paramIndex={pIndex}
                        />
                      </div>
                    );
                  })}
                </div>
              </motion.div>
            ))}
          </div>
        ) : (
          <motion.p 
            className="text-sm text-gray-500 italic bg-gray-800 bg-opacity-50 p-3 rounded-md"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2 }}
          >
            No nested conditions. Add one to continue.
          </motion.p>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default LogicalCondition;