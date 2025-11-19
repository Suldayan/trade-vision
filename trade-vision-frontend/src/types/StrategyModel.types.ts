export interface ParameterOption {
  value: string;
  label: string;
}

export interface ParameterDefinition {
  type: string;
  defaultValue: any;
  label: string;
  step?: number;
  options?: ParameterOption[];
}

export interface StrategyModel {
  key: string;
  name: string;
  description: string;
  parameters: Record<string, ParameterDefinition>;
}

// Optional: More specific type definitions for common parameter types
export type ParameterType = 
  | 'number' 
  | 'string' 
  | 'boolean' 
  | 'select' 
  | 'range';

export interface TypedParameterDefinition<T = any> extends Omit<ParameterDefinition, 'type' | 'defaultValue'> {
  type: ParameterType;
  defaultValue: T;
}

// Helper type for creating strongly typed parameters
export interface NumberParameterDefinition extends TypedParameterDefinition<number> {
  type: 'number' | 'range';
  step?: number;
}

export interface StringParameterDefinition extends TypedParameterDefinition<string> {
  type: 'string';
}

export interface BooleanParameterDefinition extends TypedParameterDefinition<boolean> {
  type: 'boolean';
}

export interface SelectParameterDefinition extends TypedParameterDefinition<string> {
  type: 'select';
  options: ParameterOption[];
}

// Example usage and factory functions
export const createParameterDefinition = {
  number: (defaultValue: number, label: string, step?: number): NumberParameterDefinition => ({
    type: 'number',
    defaultValue,
    label,
    step
  }),

  string: (defaultValue: string, label: string): StringParameterDefinition => ({
    type: 'string',
    defaultValue,
    label
  }),

  boolean: (defaultValue: boolean, label: string): BooleanParameterDefinition => ({
    type: 'boolean',
    defaultValue,
    label
  }),

  select: (defaultValue: string, label: string, options: ParameterOption[]): SelectParameterDefinition => ({
    type: 'select',
    defaultValue,
    label,
    options
  })
};

// Example usage:
/*
const exampleStrategy: StrategyModel = {
  key: 'rsi_strategy',
  name: 'RSI Strategy',
  description: 'A trading strategy based on RSI indicator',
  parameters: {
    period: createParameterDefinition.number(14, 'RSI Period', 1),
    upperThreshold: createParameterDefinition.number(70, 'Upper Threshold', 0.1),
    lowerThreshold: createParameterDefinition.number(30, 'Lower Threshold', 0.1),
    enabled: createParameterDefinition.boolean(true, 'Enable Strategy'),
    signal: createParameterDefinition.select('buy', 'Signal Type', [
      { value: 'buy', label: 'Buy Signal' },
      { value: 'sell', label: 'Sell Signal' }
    ])
  }
};
*/