import React from 'react';
import { motion } from 'framer-motion';
import { paramVariants } from '../utils/animation-variants';
import { 
  formatParamLabel, 
  getParamMinValue, 
  getParamStepValue, 
  getParamMaxValue 
} from '../utils/parameter-utils';
import { getParamOptions } from '../../constants/condition-constants';

interface ParameterFieldProps {
  conditionType: string;
  paramName: string;
  paramValue: any;
  onChange: (value: any) => void;
  paramIndex: number;
}

const ParameterField: React.FC<ParameterFieldProps> = ({
  conditionType,
  paramName,
  paramValue,
  onChange,
  paramIndex
}) => {
  if (typeof paramValue === 'boolean') {
    return (
      <motion.div 
        className="flex items-center mb-3" 
        variants={paramVariants}
        initial="hidden"
        animate="visible"
        custom={paramIndex}
      >
        <div className="relative inline-block w-10 mr-2 align-middle select-none">
          <input 
            type="checkbox" 
            id={`toggle-${paramName}-${paramIndex}`}
            checked={paramValue}
            onChange={(e) => onChange(e.target.checked)}
            className="opacity-0 absolute h-0 w-0"
          />
          <label 
            htmlFor={`toggle-${paramName}-${paramIndex}`}
            className={`block overflow-hidden h-6 rounded-full bg-gray-700 cursor-pointer ${paramValue ? 'bg-opacity-100' : 'bg-opacity-40'}`}
          >
            <span 
              className={`block h-6 w-6 rounded-full transform transition-transform duration-200 ease-in ${paramValue ? 'translate-x-4 bg-emerald-400' : 'translate-x-0 bg-gray-400'}`} 
            />
          </label>
        </div>
        <label className="text-sm text-gray-300 font-medium">
          {formatParamLabel(paramName)}
        </label>
      </motion.div>
    );
  }

  if (typeof paramValue === 'number') {
    const min = getParamMinValue(paramName);
    const step = getParamStepValue(paramName);
    const max = getParamMaxValue(paramName);
    
    return (
      <motion.div 
        className="mb-3"
        variants={paramVariants}
        initial="hidden"
        animate="visible"
        custom={paramIndex}
      >
        <div className="flex justify-between items-center mb-1">
          <label className="block text-sm text-gray-300 font-medium">
            {formatParamLabel(paramName)}
          </label>
          <span className="text-sm font-mono bg-gray-800 px-2 py-0.5 rounded text-gray-300">
            {paramValue}
          </span>
        </div>
        <input
          type="range"
          value={paramValue}
          onChange={(e) => onChange(parseFloat(e.target.value))}
          min={min}
          max={max}
          step={step}
          className="w-full appearance-none h-2 bg-gray-700 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <div className="flex justify-between text-xs text-gray-500 mt-1">
          <span>{min}</span>
          <span>{max}</span>
        </div>
      </motion.div>
    );
  }

  if (typeof paramValue === 'string') {
    // Get options based on parameter name and condition type
    const options = getParamOptions(conditionType, paramName);
    
    // If we have predefined options, render a dropdown
    if (options && options.length > 0) {
      return (
        <motion.div 
          className="mb-3"
          variants={paramVariants}
          initial="hidden"
          animate="visible"
          custom={paramIndex}
        >
          <label className="block text-sm text-gray-300 font-medium mb-1">
            {formatParamLabel(paramName)}
          </label>
          <div className="relative">
            <select
              value={paramValue}
              onChange={(e) => onChange(e.target.value)}
              className="block w-full p-2 text-sm bg-gray-800 border border-gray-700 text-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none"
            >
              {options.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-400">
              <svg className="h-4 w-4 fill-current" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
              </svg>
            </div>
          </div>
        </motion.div>
      );
    }
  }
  
  // Default text input
  return (
    <motion.div 
      className="mb-3"
      variants={paramVariants}
      initial="hidden"
      animate="visible"
      custom={paramIndex}
    >
      <label className="block text-sm text-gray-300 font-medium mb-1">
        {formatParamLabel(paramName)}
      </label>
      <input
        type="text"
        value={paramValue}
        onChange={(e) => onChange(e.target.value)}
        className="w-full p-2 text-sm bg-gray-800 border border-gray-700 text-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </motion.div>
  );
};

export default ParameterField;