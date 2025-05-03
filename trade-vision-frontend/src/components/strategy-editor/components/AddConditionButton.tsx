import React from 'react';
import { motion } from 'framer-motion';

interface AddConditionButtonProps {
  activeTab: 'entry' | 'exit';
  onAddCondition: (conditionType: string, isEntry: boolean) => void;
}

const AddConditionButton: React.FC<AddConditionButtonProps> = ({ activeTab, onAddCondition }) => {
  const isEntry = activeTab === 'entry';
  
  return (
    <motion.button
      type="button"
      className={`flex items-center ${isEntry ? 'bg-green-600' : 'bg-red-600'} hover:${isEntry ? 'bg-green-700' : 'bg-red-700'} text-white py-2 px-4 rounded-lg transition-colors duration-200`}
      whileHover={{ scale: 1.03 }}
      whileTap={{ scale: 0.97 }}
      onClick={() => onAddCondition('SMA_CROSSOVER', isEntry)}
    >
      <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
      </svg>
      Add {isEntry ? 'Entry' : 'Exit'} Condition
    </motion.button>
  );
};

export default AddConditionButton;