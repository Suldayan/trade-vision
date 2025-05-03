import React from 'react';
import { motion } from 'framer-motion';

interface ActionButtonsProps {
  onCancel: () => void;
  onSave: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({ onCancel, onSave }) => {
  return (
    <div className="flex space-x-3">
      <motion.button
        type="button"
        className="bg-gray-800 hover:bg-gray-700 text-gray-300 border border-gray-700 py-2 px-4 rounded-lg transition-colors duration-200"
        whileHover={{ scale: 1.03 }}
        whileTap={{ scale: 0.97 }}
        onClick={onCancel}
      >
        Cancel
      </motion.button>
      <motion.button
        type="button"
        className="bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-lg transition-colors duration-200 flex items-center"
        whileHover={{ scale: 1.03 }}
        whileTap={{ scale: 0.97 }}
        onClick={onSave}
      >
        <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
        </svg>
        Save Changes
      </motion.button>
    </div>
  );
};

export default ActionButtons;