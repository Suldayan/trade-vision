import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import ConditionCard from '../../condition-card/ConditionCard';

interface ConditionListProps {
  activeTab: 'entry' | 'exit';
  activeConditions: any[];
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

const ConditionList: React.FC<ConditionListProps> = ({
  activeTab,
  activeConditions,
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
  const cardListVariants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const cardVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: {
        type: "spring",
        stiffness: 400,
        damping: 30
      }
    }
  };

  return (
    <motion.div 
      className="mb-6 space-y-4"
      variants={cardListVariants}
      initial="hidden"
      animate="visible"
    >
      <AnimatePresence>
        {activeConditions.length > 0 ? (
          activeConditions.map((condition, idx) => (
            <motion.div 
              key={`${activeTab}-condition-${idx}`}
              variants={cardVariants}
              layout
            >
              <ConditionCard
                condition={condition}
                index={idx}
                isEntry={activeTab === 'entry'}
                onRemove={onRemove}
                onTypeChange={onTypeChange}
                onParameterChange={onParameterChange}
                onAddNestedCondition={onAddNestedCondition}
                onUpdateNestedConditionType={onUpdateNestedConditionType}
                onUpdateNestedConditionParameter={onUpdateNestedConditionParameter}
                onRemoveNestedCondition={onRemoveNestedCondition}
                onUpdateNotCondition={onUpdateNotCondition}
                onUpdateNotChildParameter={onUpdateNotChildParameter}
              />
            </motion.div>
          ))
        ) : (
          <motion.div 
            className="p-6 text-center text-gray-500 border border-dashed border-gray-700 rounded-lg"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <svg className="w-12 h-12 mx-auto mb-3 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p className="italic">No conditions defined yet. Add your first condition below.</p>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default ConditionList;