import { useState, useCallback } from 'react';
import { createDefaultCondition } from '../utils/configHelpers';

interface Condition {
  type: string;
  parameters: Record<string, any>; 
}

interface AndOrGroup {
  type: 'AND' | 'OR';
  parameters: {
    conditions: Condition[];
  };
}

interface NotGroup {
  type: 'NOT';
  parameters: {
    condition: Condition;
  };
}

type LogicalGroup = AndOrGroup | NotGroup;
type ConditionOrGroup = Condition | LogicalGroup;
type ConditionsPath = 'entryConditions' | 'exitConditions';

export interface BacktestConfig {
  id: string;
  name: string;
  initialCapital: number;
  riskPerTrade: number;
  commissionRate: number;
  slippagePercent: number;
  allowShort: boolean;
  requireAllEntryConditions: boolean;
  requireAllExitConditions: boolean;
  entryConditions: ConditionOrGroup[];
  exitConditions: ConditionOrGroup[];
}

const createDefaultConfig = (id: string, name: string): BacktestConfig => ({
  id,
  name,
  initialCapital: 10000,
  riskPerTrade: 0.02,
  commissionRate: 0.001,
  slippagePercent: 0.1,
  allowShort: false,
  requireAllEntryConditions: true,
  requireAllExitConditions: false,
  entryConditions: [{
    type: 'RSI_THRESHOLD',
    parameters: {
      period: 14,
      upperThreshold: 70,
      lowerThreshold: 30,
      checkOverbought: false
    }
  }],
  exitConditions: [{
    type: 'RSI_THRESHOLD',
    parameters: {
      period: 14,
      upperThreshold: 70,
      lowerThreshold: 30,
      checkOverbought: true
    }
  }]
});

// Type guards
const isNotGroup = (group: LogicalGroup): group is NotGroup => group.type === 'NOT';
const isAndOrGroup = (group: LogicalGroup): group is AndOrGroup => group.type === 'AND' || group.type === 'OR';
const isLogicalGroup = (condition: ConditionOrGroup): condition is LogicalGroup => 
  condition.type === 'AND' || condition.type === 'OR' || condition.type === 'NOT';

export const useConfig = () => {
  const [configs, setConfigs] = useState<BacktestConfig[]>([
    createDefaultConfig('1', 'Strategy 1')
  ]);
  const [activeConfigIndex, setActiveConfigIndex] = useState<number>(0);

  const activeConfig = configs[activeConfigIndex];

  const updateConfig = useCallback((path: string, value: number | boolean | string | any): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      
      const keys = path.split('.');
      let current: any = config; 
      
      for (let i = 0; i < keys.length - 1; i++) {
        const key = keys[i];
        if (key.includes('[') && key.includes(']')) {
          const arrayKey = key.split('[')[0];
          const index = parseInt(key.split('[')[1].split(']')[0]);
          
          if (current[arrayKey] && Array.isArray(current[arrayKey])) {
            current[arrayKey] = [...current[arrayKey]];
          }
          
          if (arrayKey === 'entryConditions' || arrayKey === 'exitConditions') {
            const conditionOrGroup = current[arrayKey][index];
            if (isLogicalGroup(conditionOrGroup)) {
              current[arrayKey][index] = {
                ...conditionOrGroup,
                parameters: { ...conditionOrGroup.parameters }
              };
              
              if (isAndOrGroup(conditionOrGroup)) {
                current[arrayKey][index].parameters.conditions = [...conditionOrGroup.parameters.conditions];
              } else if (isNotGroup(conditionOrGroup)) {
                current[arrayKey][index].parameters.condition = { ...conditionOrGroup.parameters.condition };
              }
            } else {
              current[arrayKey][index] = {
                ...conditionOrGroup,
                parameters: { ...conditionOrGroup.parameters }
              };
            }
          }
          
          current = current[arrayKey][index];
        } else {
          if (current[key] && typeof current[key] === 'object' && !Array.isArray(current[key])) {
            current[key] = { ...current[key] };
          }
          current = current[key];
        }
      }
      
      const lastKey = keys[keys.length - 1];
      if (lastKey.includes('[') && lastKey.includes(']')) {
        const arrayKey = lastKey.split('[')[0];
        const index = parseInt(lastKey.split('[')[1].split(']')[0]);
        
        if (!current[arrayKey]) {
          current[arrayKey] = [];
        }
        current[arrayKey][index] = value;
      } else {
        current[lastKey] = value;
      }
      
      newConfigs[activeConfigIndex] = config;
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const updateConfigName = useCallback((name: string): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      newConfigs[activeConfigIndex] = { ...newConfigs[activeConfigIndex], name };
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const addCondition = useCallback((conditionsPath: ConditionsPath, indicators: any): void => {
    const newCondition = createDefaultCondition('RSI_THRESHOLD', indicators);
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      config[conditionsPath] = [...config[conditionsPath], newCondition];
      newConfigs[activeConfigIndex] = config;
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const removeCondition = useCallback((conditionsPath: ConditionsPath, index: number): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      config[conditionsPath] = config[conditionsPath].filter((_, i) => i !== index);
      newConfigs[activeConfigIndex] = config;
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const addLogicalGroup = useCallback((conditionsPath: ConditionsPath, operator: 'AND' | 'OR' | 'NOT' = 'AND', indicators: any): void => {
    let newGroup: LogicalGroup;
    
    if (operator === 'NOT') {
      newGroup = {
        type: 'NOT',
        parameters: {
          condition: createDefaultCondition('RSI_THRESHOLD', indicators)
        }
      };
    } else {
      newGroup = {
        type: operator,
        parameters: {
          conditions: [createDefaultCondition('RSI_THRESHOLD', indicators)]
        }
      };
    }
    
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      config[conditionsPath] = [...config[conditionsPath], newGroup];
      newConfigs[activeConfigIndex] = config;
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const addConditionToGroup = useCallback((conditionsPath: ConditionsPath, groupIndex: number, indicators: any): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      const conditions = [...config[conditionsPath]];
      const group = conditions[groupIndex] as LogicalGroup;
      
      if (isAndOrGroup(group)) {
        const newCondition = createDefaultCondition('RSI_THRESHOLD', indicators);
        const updatedGroup: AndOrGroup = {
          ...group,
          parameters: {
            ...group.parameters,
            conditions: [...group.parameters.conditions, newCondition]
          }
        };
        conditions[groupIndex] = updatedGroup;
        config[conditionsPath] = conditions;
        newConfigs[activeConfigIndex] = config;
      }
      
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const removeConditionFromGroup = useCallback((conditionsPath: ConditionsPath, groupIndex: number, conditionIndex: number): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      const conditions = [...config[conditionsPath]];
      const group = conditions[groupIndex] as LogicalGroup;
      
      if (isAndOrGroup(group)) {
        const updatedGroup: AndOrGroup = {
          ...group,
          parameters: {
            ...group.parameters,
            conditions: group.parameters.conditions.filter((_, i) => i !== conditionIndex)
          }
        };
        conditions[groupIndex] = updatedGroup;
        config[conditionsPath] = conditions;
        newConfigs[activeConfigIndex] = config;
      }
      
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const handleConditionChange = useCallback((conditionsPath: ConditionsPath, index: number, newCondition: ConditionOrGroup): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      const conditions = [...config[conditionsPath]];
      conditions[index] = newCondition;
      config[conditionsPath] = conditions;
      newConfigs[activeConfigIndex] = config;
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const handleNotGroupConditionChange = useCallback((conditionsPath: ConditionsPath, groupIndex: number, newCondition: Condition): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      const conditions = [...config[conditionsPath]];
      const group = conditions[groupIndex] as LogicalGroup;
      
      if (isNotGroup(group)) {
        const updatedGroup: NotGroup = {
          ...group,
          parameters: {
            condition: newCondition
          }
        };
        conditions[groupIndex] = updatedGroup;
        config[conditionsPath] = conditions;
        newConfigs[activeConfigIndex] = config;
      }
      
      return newConfigs;
    });
  }, [activeConfigIndex]);

  const handleLogicalOperatorChange = useCallback((
    conditionsPath: ConditionsPath, 
    index: number, 
    newOperator: 'AND' | 'OR' | 'NOT',
    indicators: any
  ): void => {
    setConfigs(prev => {
      const newConfigs = [...prev];
      const config = { ...newConfigs[activeConfigIndex] };
      const conditions = [...config[conditionsPath]];
      const currentGroup = conditions[index] as LogicalGroup;
      
      let newGroup: LogicalGroup;
      
      if (newOperator === 'NOT') {
        let condition: Condition;
        if (isAndOrGroup(currentGroup) && currentGroup.parameters.conditions.length > 0) {
          condition = currentGroup.parameters.conditions[0];
        } else if (isNotGroup(currentGroup)) {
          condition = currentGroup.parameters.condition;
        } else {
          condition = createDefaultCondition('RSI_THRESHOLD', indicators);
        }
        
        newGroup = {
          type: 'NOT',
          parameters: {
            condition: condition
          }
        };
      } else {
        let conditionsArray: Condition[];
        if (isAndOrGroup(currentGroup)) {
          conditionsArray = currentGroup.parameters.conditions;
        } else if (isNotGroup(currentGroup)) {
          conditionsArray = [currentGroup.parameters.condition];
        } else {
          conditionsArray = [createDefaultCondition('RSI_THRESHOLD', indicators)];
        }
        
        newGroup = {
          type: newOperator,
          parameters: {
            conditions: conditionsArray
          }
        };
      }
      
      conditions[index] = newGroup;
      config[conditionsPath] = conditions;
      newConfigs[activeConfigIndex] = config;
      return newConfigs;
    });
  }, [activeConfigIndex]);

  return {
    configs,
    setConfigs,
    activeConfigIndex,
    setActiveConfigIndex,
    activeConfig,
    updateConfig,
    updateConfigName,
    addCondition,
    removeCondition,
    addLogicalGroup,
    addConditionToGroup,
    removeConditionFromGroup,
    handleConditionChange,
    handleNotGroupConditionChange,
    handleLogicalOperatorChange,
    createDefaultConfig
  };
};