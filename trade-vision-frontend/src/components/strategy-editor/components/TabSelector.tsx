import React from 'react';
import { motion } from 'framer-motion';

interface TabSelectorProps {
  activeTab: 'entry' | 'exit';
  setActiveTab: (tab: 'entry' | 'exit') => void;
}

const TabSelector: React.FC<TabSelectorProps> = ({ activeTab, setActiveTab }) => {
  const tabIndicatorVariants = {
    entry: { x: 0 },
    exit: { x: "100%" }
  };

  return (
    <div className="relative mb-6 border-b border-gray-800">
      <div className="flex">
        <button
          className={`py-3 px-4 relative z-10 ${activeTab === 'entry' ? 'text-green-400 font-medium' : 'text-gray-400'}`}
          onClick={() => setActiveTab('entry')}
        >
          <div className="flex items-center">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 9l3 3m0 0l-3 3m3-3H8m13 0a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Entry Conditions
          </div>
        </button>
        <button
          className={`py-3 px-4 relative z-10 ${activeTab === 'exit' ? 'text-red-400 font-medium' : 'text-gray-400'}`}
          onClick={() => setActiveTab('exit')}
        >
          <div className="flex items-center">
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Exit Conditions
          </div>
        </button>
      </div>
      <motion.div 
        className={`absolute bottom-0 h-0.5 w-1/2 ${activeTab === 'entry' ? 'bg-green-400' : 'bg-red-400'}`}
        variants={tabIndicatorVariants}
        animate={activeTab}
        transition={{ type: "spring", stiffness: 300, damping: 30 }}
      />
    </div>
  );
};

export default TabSelector;