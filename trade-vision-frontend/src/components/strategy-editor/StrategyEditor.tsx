import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { BackTestRequest } from '../../types/backtest';
import { createCondition, validateStrategy } from '../../utils/backtestUtils';
import { ModalHeader } from './components/ModalHeader';
import ErrorDisplay from './components/ErrorDisplay';
import TabSelector from './components/TabSelector';
import RequireAllToggle from './components/RequireAllToggle';
import ConditionList from './components/ConditionList';
import AddConditionButton from './components/AddConditionButton';
import ActionButtons from './components/ActionButtons';

interface StrategyEditorProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: () => void;
  request: BackTestRequest;
  setRequest: React.Dispatch<React.SetStateAction<BackTestRequest>>;
}

const StrategyEditor: React.FC<StrategyEditorProps> = ({ isOpen, onClose, onSave, request, setRequest }) => {
  const [activeTab, setActiveTab] = useState<'entry' | 'exit'>('entry');
  const [editorError, setEditorError] = useState<string | null>(null);

  if (!isOpen) return null;

  const activeConditions = activeTab === 'entry' 
    ? request.entryConditions 
    : request.exitConditions;

  // Close strategy editor and validate before saving
  const handleSaveStrategyChanges = () => {
    try {
      validateStrategy(request);
      setEditorError(null);
      onSave();
    } catch (error) {
      // Display error in the editor
      setEditorError(error instanceof Error ? error.message : 'Unknown error');
    }
  };

  // Add a new condition to entry or exit conditions
  const addCondition = (conditionType: string, isEntry: boolean) => {
    const newCondition = createCondition(conditionType);
    
    setRequest(prev => ({
      ...prev,
      [isEntry ? 'entryConditions' : 'exitConditions']: [
        ...prev[isEntry ? 'entryConditions' : 'exitConditions'],
        newCondition
      ]
    }));
    
    // Clear any previous error
    setEditorError(null);
  };

  // Remove a condition from entry or exit conditions
  const removeCondition = (index: number, isEntry: boolean) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      conditions.splice(index, 1);
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Update a condition's type
  const updateConditionType = (index: number, newType: string, isEntry: boolean) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      conditions[index] = createCondition(newType);
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Update a condition's parameter
  const updateConditionParameter = (
    index: number, 
    paramName: string, 
    value: any, 
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const condition = { ...conditions[index] };
      
      // Update the specific parameter
      condition.parameters = {
        ...condition.parameters,
        [paramName]: value
      };
      
      conditions[index] = condition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Add a nested condition to a logical group (AND/OR)
  const addNestedCondition = (
    parentIndex: number, 
    conditionType: string, 
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const parentCondition = { ...conditions[parentIndex] };
      
      // Create new nested condition
      const newCondition = createCondition(conditionType);
      
      // Add to parent's conditions array
      const nestedConditions = [...(parentCondition.parameters.conditions || []), newCondition];
      parentCondition.parameters = {
        ...parentCondition.parameters,
        conditions: nestedConditions
      };
      
      conditions[parentIndex] = parentCondition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Update a nested condition's type
  const updateNestedConditionType = (
    parentIndex: number, 
    childIndex: number, 
    newType: string, 
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const parentCondition = { ...conditions[parentIndex] };
      const nestedConditions = [...(parentCondition.parameters.conditions || [])];
      
      // Replace with new condition of specified type
      nestedConditions[childIndex] = createCondition(newType);
      
      parentCondition.parameters = {
        ...parentCondition.parameters,
        conditions: nestedConditions
      };
      
      conditions[parentIndex] = parentCondition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Update a nested condition's parameter
  const updateNestedConditionParameter = (
    parentIndex: number,
    childIndex: number,
    paramName: string,
    value: any,
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const parentCondition = { ...conditions[parentIndex] };
      const nestedConditions = [...(parentCondition.parameters.conditions || [])];
      const childCondition = { ...nestedConditions[childIndex] };
      
      // Update the specific parameter
      childCondition.parameters = {
        ...childCondition.parameters,
        [paramName]: value
      };
      
      nestedConditions[childIndex] = childCondition;
      parentCondition.parameters = {
        ...parentCondition.parameters,
        conditions: nestedConditions
      };
      
      conditions[parentIndex] = parentCondition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Remove a nested condition from a logical group
  const removeNestedCondition = (
    parentIndex: number, 
    childIndex: number, 
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const parentCondition = { ...conditions[parentIndex] };
      
      // Remove from parent's conditions array
      const nestedConditions = [...(parentCondition.parameters.conditions || [])];
      nestedConditions.splice(childIndex, 1);
      
      parentCondition.parameters = {
        ...parentCondition.parameters,
        conditions: nestedConditions
      };
      
      conditions[parentIndex] = parentCondition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Handle setting child condition for NOT operator
  const updateNotCondition = (
    parentIndex: number,
    conditionType: string,
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const parentCondition = { ...conditions[parentIndex] };
      
      // Create new child condition
      const childCondition = createCondition(conditionType);
      
      parentCondition.parameters = {
        ...parentCondition.parameters,
        condition: childCondition
      };
      
      conditions[parentIndex] = parentCondition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Update NOT child condition parameter
  const updateNotChildParameter = (
    parentIndex: number,
    paramName: string,
    value: any,
    isEntry: boolean
  ) => {
    setRequest(prev => {
      const conditions = [...prev[isEntry ? 'entryConditions' : 'exitConditions']];
      const parentCondition = { ...conditions[parentIndex] };
      const childCondition = { ...parentCondition.parameters.condition };
      
      // Update the specific parameter
      childCondition.parameters = {
        ...childCondition.parameters,
        [paramName]: value
      };
      
      parentCondition.parameters = {
        ...parentCondition.parameters,
        condition: childCondition
      };
      
      conditions[parentIndex] = parentCondition;
      return {
        ...prev,
        [isEntry ? 'entryConditions' : 'exitConditions']: conditions
      };
    });
    
    // Clear any previous error
    setEditorError(null);
  };

  // Animation variants
  const overlayVariants = {
    hidden: { opacity: 0 },
    visible: { opacity: 1, transition: { duration: 0.2 } }
  };

  const modalVariants = {
    hidden: { y: 50, opacity: 0 },
    visible: { 
      y: 0, 
      opacity: 1, 
      transition: { 
        type: "spring", 
        stiffness: 300, 
        damping: 30,
        delay: 0.1 
      } 
    },
    exit: { 
      y: 50, 
      opacity: 0,
      transition: { duration: 0.2 } 
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div 
          className="fixed inset-0 bg-black bg-opacity-70 backdrop-blur-sm flex items-center justify-center z-50"
          initial="hidden"
          animate="visible"
          exit="hidden"
          variants={overlayVariants}
        >
          <motion.div 
            className="bg-gray-900 text-gray-100 rounded-xl p-6 max-w-3xl w-full max-h-screen overflow-auto border border-gray-800 shadow-2xl"
            variants={modalVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
          >
            {/* Use the ModalHeader component */}
            <ModalHeader 
              title="Advanced Strategy Editor" 
              onClose={onClose}
            />
            
            {/* Use ErrorDisplay component */}
            <ErrorDisplay error={editorError} />
            
            {/* Use TabSelector component */}
            <TabSelector 
              activeTab={activeTab} 
              setActiveTab={setActiveTab} 
            />
            
            {/* Use RequireAllToggle component */}
            <RequireAllToggle 
              activeTab={activeTab} 
              request={request} 
              setRequest={setRequest} 
            />
            
            {/* Use ConditionList component */}
            <ConditionList 
              activeTab={activeTab}
              activeConditions={activeConditions}
              onRemove={removeCondition}
              onTypeChange={updateConditionType}
              onParameterChange={updateConditionParameter}
              onAddNestedCondition={addNestedCondition}
              onUpdateNestedConditionType={updateNestedConditionType}
              onUpdateNestedConditionParameter={updateNestedConditionParameter}
              onRemoveNestedCondition={removeNestedCondition}
              onUpdateNotCondition={updateNotCondition}
              onUpdateNotChildParameter={updateNotChildParameter}
            />
            
            <div className="flex justify-between">
              {/* Use AddConditionButton component */}
              <AddConditionButton 
                activeTab={activeTab} 
                onAddCondition={addCondition} 
              />
              
              {/* Use ActionButtons component */}
              <ActionButtons 
                onCancel={onClose} 
                onSave={handleSaveStrategyChanges} 
              />
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export default StrategyEditor;