import React from 'react';
import { motion } from 'framer-motion';
import { CONDITION_TYPES, getConditionColor } from '../constants/condition-constants';
import { cardVariants, glowAnimation, pillAnimation } from './utils/animation-variants';
import LogicalCondition from './components/LogicalCondition';
import NotCondition from './components/NotCondition';
import StandardCondition from './components/StandardCondition';
import { ConditionConfig } from '../../types/backtest';

interface ConditionCardProps {
  condition: ConditionConfig;
  index: number;
  isEntry: boolean;
  onRemove: (index: number, isEntry: boolean) => void;
  onTypeChange: (index: number, newType: string, isEntry: boolean) => void;
  onParameterChange: (index: number, paramName: string, value: any, isEntry: boolean) => void;
  onAddNestedCondition: (parentIndex: number, conditionType: string, isEntry: boolean) => void;
  onUpdateNestedConditionType: (parentIndex: number, childIndex: number, newType: string, isEntry: boolean) => void;
  onUpdateNestedConditionParameter: (parentIndex: number, childIndex: number, paramName: string, value: any, isEntry: boolean) => void;
  onRemoveNestedCondition: (parentIndex: number, childIndex: number, isEntry: boolean) => void;
  onUpdateNotCondition: (parentIndex: number, conditionType: string, isEntry: boolean) => void;
  onUpdateNotChildParameter: (parentIndex: number, paramName: string, value: any, isEntry: boolean) => void;
}

const ConditionCard: React.FC<ConditionCardProps> = ({
  condition,
  index,
  isEntry,
  onRemove,
  onTypeChange,
  onParameterChange,
  onAddNestedCondition,
  onUpdateNestedConditionType,
  onUpdateNestedConditionParameter,
  onRemoveNestedCondition,
  onUpdateNotCondition,
  onUpdateNotChildParameter
}) => {
  const renderConditionContent = () => {
    const { type } = condition;
    
    if (type === 'NOT') {
      return (
        <NotCondition
          condition={condition}
          index={index}
          isEntry={isEntry}
          onUpdateNotCondition={onUpdateNotCondition}
          onUpdateNotChildParameter={onUpdateNotChildParameter}
        />
      );
    }
    
    if (type === 'AND' || type === 'OR') {
      return (
        <LogicalCondition
          condition={condition}
          index={index}
          isEntry={isEntry}
          onAddNestedCondition={onAddNestedCondition}
          onUpdateNestedConditionType={onUpdateNestedConditionType}
          onUpdateNestedConditionParameter={onUpdateNestedConditionParameter}
          onRemoveNestedCondition={onRemoveNestedCondition}
        />
      );
    }
    
    return (
      <StandardCondition
        condition={condition}
        index={index}
        isEntry={isEntry}
        onParameterChange={onParameterChange}
      />
    );
  };
  
  return (
    <motion.div 
      className="border border-gray-700 rounded-md p-4 mb-4 bg-gray-900 overflow-hidden backdrop-blur-sm relative"
      layout
      variants={cardVariants}
      initial="hidden"
      animate="visible"
      exit="exit"
    >
      {/* Background glow effect */}
      <div className={`absolute inset-0 bg-gradient-to-br ${getConditionColor(condition.type)} opacity-5 z-0`}></div>
      
      {/* Header with pulsing indicator */}
      <div className="flex justify-between items-center mb-4 relative z-10">
        <div className="flex items-center">
          <motion.div 
            className={`h-3 w-3 rounded-full bg-gradient-to-br ${getConditionColor(condition.type)} mr-3`}
            animate={glowAnimation(getConditionColor(condition.type).split(' ')[0].replace('from-', ''))}
          />
          <select
            value={condition.type}
            onChange={(e) => onTypeChange(index, e.target.value, isEntry)}
            className="p-2 bg-gray-800 border border-gray-700 text-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {CONDITION_TYPES.map(type => (
              <option key={type} value={type}>
                {type.replace('_', ' ')}
              </option>
            ))}
          </select>
        </div>
        
        {/* Remove button with animation */}
        <motion.button
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
          type="button"
          onClick={() => onRemove(index, isEntry)}
          className="text-red-500 hover:text-red-400 bg-gray-800 p-2 rounded-md"
        >
          <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </motion.button>
      </div>
      
      {/* Content area */}
      <div className="relative z-10">
        {renderConditionContent()}
      </div>
      
      {/* Bottom "pill" indicator showing the condition type */}
      <motion.div
        className={`absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r ${getConditionColor(condition.type)}`}
        animate={pillAnimation}
      />
    </motion.div>
  );
};

export default ConditionCard;