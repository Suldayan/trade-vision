import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';

interface ErrorDisplayProps {
  error: string | null;
}

const ErrorDisplay: React.FC<ErrorDisplayProps> = ({ error }) => {
  const errorVariants = {
    hidden: { opacity: 0, y: -10 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { 
        type: "spring", 
        stiffness: 500, 
        damping: 20 
      } 
    },
    exit: { 
      opacity: 0,
      transition: { duration: 0.2 } 
    }
  };

  return (
    <AnimatePresence>
      {error && (
        <motion.div 
          className="mb-6 p-4 bg-red-900/30 border border-red-700 text-red-300 rounded-lg flex items-start"
          variants={errorVariants}
          initial="hidden"
          animate="visible"
          exit="exit"
        >
          <svg className="w-5 h-5 mr-3 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <div>
            <strong className="font-medium">Error:</strong> {error}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default ErrorDisplay;