import { useCallback } from 'react'
import { Plus, Copy, Trash2 } from 'lucide-react'

interface BacktestConfig {
  id: string;
  name: string;
  initialCapital: number;
  riskPerTrade: number;
  commissionRate: number;
  slippagePercent: number;
  allowShort: boolean;
  requireAllEntryConditions: boolean;
  requireAllExitConditions: boolean;
  entryConditions: any[];
  exitConditions: any[];
}

interface StrategyTabProps {
  configs: BacktestConfig[];
  activeConfigIndex: number;
  onConfigsChange: (configs: BacktestConfig[]) => void;
  onActiveConfigIndexChange: (index: number) => void;
  onError: (error: string) => void;
  createDefaultConfig: (id: string, name: string) => BacktestConfig;
}

export default function StrategyTab({
  configs,
  activeConfigIndex,
  onConfigsChange,
  onActiveConfigIndexChange,
  onError,
  createDefaultConfig
}: StrategyTabProps) {
  
  const addBacktest = useCallback((): void => {
    if (configs.length >= 5) {
      onError('Maximum 5 concurrent backtests allowed');
      return;
    }
    
    const newId = (configs.length + 1).toString();
    const newConfig = createDefaultConfig(newId, `Strategy ${newId}`);
    onConfigsChange([...configs, newConfig]);
    onActiveConfigIndexChange(configs.length);
    onError('');
  }, [configs, onConfigsChange, onActiveConfigIndexChange, onError, createDefaultConfig]);

  const removeBacktest = useCallback((index: number): void => {
    if (configs.length <= 1) {
      onError('At least one strategy configuration is required');
      return;
    }
    
    const newConfigs = configs.filter((_, i) => i !== index);
    onConfigsChange(newConfigs);
    
    // Adjust active index if necessary
    if (activeConfigIndex >= index && activeConfigIndex > 0) {
      onActiveConfigIndexChange(activeConfigIndex - 1);
    } else if (activeConfigIndex >= configs.length - 1) {
      onActiveConfigIndexChange(configs.length - 2);
    }
    onError('');
  }, [configs, activeConfigIndex, onConfigsChange, onActiveConfigIndexChange, onError]);

  const duplicateBacktest = useCallback((index: number): void => {
    if (configs.length >= 5) {
      onError('Maximum 5 concurrent backtests allowed');
      return;
    }
    
    const configToDuplicate = configs[index];
    const newId = (configs.length + 1).toString();
    const duplicatedConfig = {
      ...JSON.parse(JSON.stringify(configToDuplicate)),
      id: newId,
      name: `${configToDuplicate.name} (Copy)`
    };
    
    onConfigsChange([...configs, duplicatedConfig]);
    onActiveConfigIndexChange(configs.length);
    onError('');
  }, [configs, onConfigsChange, onActiveConfigIndexChange, onError]);

  return (
    <div className="mb-6">
      <div className="flex items-center justify-between mb-3">
        <label className="block text-sm font-medium text-slate-300">
          Strategy Configurations ({configs.length}/5)
        </label>
        <button
          onClick={addBacktest}
          disabled={configs.length >= 5}
          className="p-1 bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white rounded transition-colors"
          title="Add Strategy"
        >
          <Plus className="h-4 w-4" />
        </button>
      </div>
      
      <div className="space-y-2">
        {configs.map((config, index) => (
          <div
            key={config.id}
            className={`flex items-center gap-2 p-2 rounded-lg border transition-colors ${
              activeConfigIndex === index
                ? 'bg-purple-600/20 border-purple-400'
                : 'bg-white/5 border-white/10 hover:border-white/20'
            }`}
          >
            <button
              onClick={() => onActiveConfigIndexChange(index)}
              className="flex-1 text-left text-sm text-white truncate"
            >
              {config.name}
            </button>
            <button
              onClick={() => duplicateBacktest(index)}
              disabled={configs.length >= 5}
              className="p-1 text-slate-400 hover:text-white disabled:text-slate-600 disabled:cursor-not-allowed transition-colors"
              title="Duplicate Strategy"
            >
              <Copy className="h-3 w-3" />
            </button>
            <button
              onClick={() => removeBacktest(index)}
              disabled={configs.length <= 1}
              className="p-1 text-slate-400 hover:text-red-400 disabled:text-slate-600 disabled:cursor-not-allowed transition-colors"
              title="Remove Strategy"
            >
              <Trash2 className="h-3 w-3" />
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}