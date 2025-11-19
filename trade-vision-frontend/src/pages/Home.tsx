import React, { useMemo } from 'react';
import { TrendingUp, Settings, Play } from 'lucide-react';
import { indicators as fallbackIndicators } from '../constants/indicators'; 
import ConditionsSection from '../components/condition/ConditionsSection';
import BasicSettingsPanel from '../components/BasicSettingsPanel';
import StrategyTab from '../components/StrategyTab';
import { useBacktest } from '../hooks/useBacktest';
import { useConfig } from '../hooks/useConfig';
import { useExpansion } from '../hooks/useExpansion';
import { useIndicators } from '../hooks/useIndicator';
import FileUpload from '../components/FileUpload';
import { type StrategyModel } from '../types/StrategyModel.types';

const ResultsPanel = React.lazy(() => import('../components/results/ResultsPanel'));

export default function Home() {
  const { file, loading, results, error, handleFileSelect, runBacktest } = useBacktest();
  const { 
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
  } = useConfig();
  const { expandedSections, toggleSection } = useExpansion();

  const { indicators: backendIndicators, loading: indicatorsLoading, error: indicatorsError } = useIndicators();

  const indicators = useMemo(() => {
    if (backendIndicators && backendIndicators.length > 0) {
      return backendIndicators.reduce((acc: { [x: string]: { name: any; description: any; parameters: any; }; }, indicator: StrategyModel) => {
        acc[indicator.key] = {
          name: indicator.name,
          description: indicator.description,
          parameters: Object.entries(indicator.parameters).reduce((paramAcc, [key, param]) => {
            if (param && typeof param === 'object' && 'type' in param) {
              paramAcc[key] = {
                type: param.type,
                default: param.defaultValue,
                label: param.label,
                step: param.step,
                options: param.options
              };
            }
            return paramAcc;
          }, {} as any)
        };
        return acc;
      }, {} as any);
    }
    return fallbackIndicators;
  }, [backendIndicators]);

  const isBacktestDisabled = useMemo(() => loading || !file, [loading, file]);

  const BasicSettings = useMemo(() => (
    <BasicSettingsPanel />
  ), []);

  if (indicatorsLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-400 mx-auto mb-4"></div>
          <p className="text-white text-lg">Loading indicators...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-white mb-4 flex items-center justify-center gap-3">
            <TrendingUp className="h-10 w-10 text-purple-400" />
            TradeVision - Advanced Trading Strategy Backtester
          </h1>
          <p className="text-slate-300 text-lg max-w-3xl mx-auto">
            Build complex trading strategies with logical operators and comprehensive technical indicators
          </p>
        </div>

        <div className="grid lg:grid-cols-12 gap-8">
          {/* Configuration Panel */}
          <div className="lg:col-span-4 xl:col-span-3">
            <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 shadow-2xl sticky top-8 max-h-[calc(100vh-4rem)] overflow-y-auto">
              <h2 className="text-xl font-semibold text-white mb-6 flex items-center gap-2">
                <Settings className="h-5 w-5" />
                Strategy Configuration
              </h2>
              
              {/* Show indicators error if any */}
              {indicatorsError && (
                <div className="mb-4 p-3 bg-yellow-500/20 border border-yellow-500/50 rounded-lg text-yellow-300 text-sm">
                  <p className="font-medium">Warning: Using fallback indicators</p>
                  <p className="text-xs mt-1">{indicatorsError}</p>
                </div>
              )}
              
              {/* File Upload */}
              <FileUpload
                onFileSelect={handleFileSelect} 
                acceptedTypes={['text/csv']}
                acceptedExtensions={['.csv']}
                maxFileSize={100 * 1024 * 1024} 
                label="Market Data (CSV)"
                placeholder="Choose CSV file"
                showFileSize={true}
                allowClear={true}
              />

              {/* Strategy Tabs */}
              <div className="mb-6">
                <StrategyTab
                  configs={configs}
                  activeConfigIndex={activeConfigIndex}
                  onConfigsChange={setConfigs}
                  onActiveConfigIndexChange={setActiveConfigIndex}
                  onError={console.error}
                  createDefaultConfig={createDefaultConfig}
                />
              </div>

              {/* Strategy Name Input */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-slate-300 mb-3">
                  Strategy Name
                </label>
                <input
                  type="text"
                  value={activeConfig.name}
                  onChange={(e) => updateConfigName(e.target.value)}
                  className="w-full px-4 py-3 bg-white/10 border border-white/20 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                  placeholder="Strategy name..."
                />
              </div>

              {/* Basic Settings */}
              <div className="mb-6">
                {BasicSettings}
              </div>

              {/* Conditions Sections */}
              <div className="space-y-6 mb-8">
                <ConditionsSection
                  title="Entry Conditions"
                  conditionsPath="entryConditions"
                  conditions={activeConfig.entryConditions}
                  indicators={indicators}
                  isExpanded={expandedSections.entry}
                  onToggleExpanded={() => toggleSection('entry')}
                  onAddCondition={(path) => addCondition(path, indicators)}
                  onAddLogicalGroup={(path, operator) => addLogicalGroup(path, operator, indicators)}
                  onConditionChange={handleConditionChange}
                  onParameterChange={updateConfig}
                  onRemoveCondition={removeCondition}
                  onAddConditionToGroup={(path, groupIndex) => addConditionToGroup(path, groupIndex, indicators)}
                  onRemoveConditionFromGroup={removeConditionFromGroup}
                  onNotGroupConditionChange={handleNotGroupConditionChange}
                  onLogicalOperatorChange={(path, index, operator) => handleLogicalOperatorChange(path, index, operator, indicators)}
                />

                <ConditionsSection
                  title="Exit Conditions"
                  conditionsPath="exitConditions"
                  conditions={activeConfig.exitConditions}
                  indicators={indicators}
                  isExpanded={expandedSections.exit}
                  onToggleExpanded={() => toggleSection('exit')}
                  onAddCondition={(path) => addCondition(path, indicators)}
                  onAddLogicalGroup={(path, operator) => addLogicalGroup(path, operator, indicators)}
                  onConditionChange={handleConditionChange}
                  onParameterChange={updateConfig}
                  onRemoveCondition={removeCondition}
                  onAddConditionToGroup={(path, groupIndex) => addConditionToGroup(path, groupIndex, indicators)}
                  onRemoveConditionFromGroup={removeConditionFromGroup}
                  onNotGroupConditionChange={handleNotGroupConditionChange}
                  onLogicalOperatorChange={(path, index, operator) => handleLogicalOperatorChange(path, index, operator, indicators)}
                />
              </div>

              {/* Run Backtest Button */}
              <div className="sticky bottom-0 bg-white/10 backdrop-blur-md -mx-6 -mb-6 p-6 border-t border-white/20">
                <button
                  onClick={() => runBacktest(configs)}
                  disabled={isBacktestDisabled}
                  className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 disabled:from-gray-600 disabled:to-gray-600 text-white font-semibold py-4 px-6 rounded-lg transition-all duration-200 flex items-center justify-center gap-3 disabled:cursor-not-allowed shadow-lg"
                >
                  {loading ? (
                    <>
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                      <span>Running {configs.length} Backtest{configs.length > 1 ? 's' : ''}...</span>
                    </>
                  ) : (
                    <>
                      <Play className="h-5 w-5" />
                      <span>Run {configs.length} Backtest{configs.length > 1 ? 's' : ''}</span>
                    </>
                  )}
                </button>

                {error && (
                  <div className="mt-4 p-4 bg-red-500/20 border border-red-500/50 rounded-lg text-red-300 text-sm">
                    {error}
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Results Panel */}
          <div className="lg:col-span-8 xl:col-span-9">
            <ResultsPanel results={results} configs={configs} />
          </div>
        </div>
      </div>
    </div>
  );
};