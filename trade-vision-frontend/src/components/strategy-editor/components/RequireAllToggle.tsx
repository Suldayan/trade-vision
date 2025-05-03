import React from 'react';
import { motion } from 'framer-motion';
import { BackTestRequest } from '../../../types/backtest';

interface RequireAllToggleProps {
  activeTab: 'entry' | 'exit';
  request: BackTestRequest;
  setRequest: React.Dispatch<React.SetStateAction<BackTestRequest>>;
}

const RequireAllToggle: React.FC<RequireAllToggleProps> = ({ 
  activeTab, 
  request, 
  setRequest 
}) => {
  const isChecked = activeTab === 'entry' 
    ? request.requireAllEntryConditions 
    : request.requireAllExitConditions;

  const handleToggle = (e: React.ChangeEvent<HTMLInputElement>) => {
    setRequest(prev => ({
      ...prev,
      [activeTab === 'entry' ? 'requireAllEntryConditions' : 'requireAllExitConditions']: e.target.checked
    }));
  };

  return (
    <motion.div 
      className="mb-6 bg-gray-800/50 p-4 rounded-lg border border-gray-700"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ delay: 0.3 }}
    >
      <label className="flex items-center cursor-pointer">
        <span className="relative inline-block w-10 h-5 mr-3">
          <input
            type="checkbox"
            checked={isChecked}
            onChange={handleToggle}
            className="sr-only"
          />
          <span className="absolute inset-0 bg-gray-700 rounded-full transition-colors duration-200 ease-in-out">
            <motion.span 
              className="absolute inset-0.5 bg-blue-500 rounded-full"
              animate={{ 
                left: isChecked ? '50%' : '0%',
                backgroundColor: isChecked 
                  ? (activeTab === 'entry' ? '#34D399' : '#F87171') 
                  : '#4B5563'
              }}
              transition={{ type: "spring", stiffness: 500, damping: 30 }}
              style={{ width: '50%' }}
            />
          </span>
        </span>
        <span className="text-sm text-gray-300">
          {activeTab === 'entry' 
            ? 'Require all entry conditions to be met (AND logic)' 
            : 'Require all exit conditions to be met (AND logic)'}
        </span>
      </label>
    </motion.div>
  );
};

export default RequireAllToggle;