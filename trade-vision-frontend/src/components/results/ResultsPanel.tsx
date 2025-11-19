import React, { useState, Suspense } from 'react';
import { TrendingUp, BarChart3, Activity, Users, Eye, EyeOff } from 'lucide-react';
import type { BacktestResults } from '../../types/strategy';
import LoadingScreen from '../LoadingScreen';
import PerformanceMetrics from './PerformanceMetrics';

const EquityCurve = React.lazy(() => import('./EquityCurve'))
const TradeHistory = React.lazy(() => import('./TradeHistory'))

interface BacktestConfig {
  name?: string;
}

interface ComparisonTableProps {
  results: BacktestResults[];
  configs: BacktestConfig[];
  visibleStrategies: number[];
  onToggleVisibility: (index: number) => void;
}

const ComparisonTable: React.FC<ComparisonTableProps> = ({ 
  results, 
  configs, 
  visibleStrategies, 
  onToggleVisibility 
}) => {
  const metrics = [
    { 
      key: 'totalReturn', 
      label: 'Total Return', 
      format: (val: number) => `${val?.toFixed(2) || '0.00'}%`,
      getValue: (result: BacktestResults) => result.totalReturn || 0
    },
    { 
      key: 'finalCapital', 
      label: 'Final Capital', 
      format: (val: number) => `$${val?.toLocaleString() || '0'}`,
      getValue: (result: BacktestResults) => result.finalCapital || 0
    },
    { 
      key: 'winRatio', 
      label: 'Win Ratio', 
      format: (val: number) => `${((val || 0) * 100).toFixed(1)}%`,
      getValue: (result: BacktestResults) => result.winRatio || 0
    },
    { 
      key: 'tradeCount', 
      label: 'Total Trades', 
      format: (val: number) => val?.toString() || '0',
      getValue: (result: BacktestResults) => result.tradeCount || 0
    },
    { 
      key: 'avgTrade', 
      label: 'Avg P&L per Trade', 
      format: (val: number) => `$${val?.toFixed(2) || '0.00'}`,
      getValue: (result: BacktestResults) => {
        if (!result.trades || result.trades.length === 0) return 0;
        const totalPnL = result.trades.reduce((sum, trade) => sum + (trade.pnl || 0), 0);
        return totalPnL / result.trades.length;
      }
    },
    { 
      key: 'maxDrawdown', 
      label: 'Max Drawdown', 
      format: (val: number) => `${val?.toFixed(2) || '0.00'}%`,
      getValue: (result: BacktestResults) => {
        // Calculate max drawdown from equity curve
        if (!result.equityCurve || result.equityCurve.length === 0) return 0;
        let maxDrawdown = 0;
        let peak = result.equityCurve[0];
        
        for (const value of result.equityCurve) {
          if (value > peak) peak = value;
          const drawdown = ((peak - value) / peak) * 100;
          if (drawdown > maxDrawdown) maxDrawdown = drawdown;
        }
        return maxDrawdown;
      }
    }
  ];

  return (
    <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 shadow-2xl">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-semibold text-white">Strategy Comparison</h3>
        <div className="flex items-center gap-3">
          <span className="text-sm text-slate-400">Toggle visibility:</span>
          <div className="flex gap-1">
            {configs.map((config, index) => (
              <button
                key={index}
                onClick={() => onToggleVisibility(index)}
                className={`p-2 rounded-lg transition-all ${
                  visibleStrategies.includes(index) 
                    ? 'bg-purple-500/20 text-purple-400 border border-purple-500/30' 
                    : 'bg-white/5 text-slate-500 border border-white/10 hover:bg-white/10'
                }`}
                title={`Toggle ${config?.name || `Strategy ${index + 1}`}`}
              >
                {visibleStrategies.includes(index) ? 
                  <Eye className="h-4 w-4" /> : 
                  <EyeOff className="h-4 w-4" />
                }
              </button>
            ))}
          </div>
        </div>
      </div>
      
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/20">
              <th className="text-left py-3 px-4 text-sm font-medium text-slate-300">Metric</th>
              {configs.map((config, index) => (
                visibleStrategies.includes(index) && (
                  <th key={index} className="text-center py-3 px-4 text-sm font-medium text-slate-300 min-w-[120px]">
                    {config?.name || `Strategy ${index + 1}`}
                  </th>
                )
              ))}
            </tr>
          </thead>
          <tbody>
            {metrics.map((metric) => (
              <tr key={metric.key} className="border-b border-white/5 hover:bg-white/5">
                <td className="py-3 px-4 text-sm font-medium text-slate-300">{metric.label}</td>
                {results.map((result, index) => (
                  visibleStrategies.includes(index) && (
                    <td key={index} className="text-center py-3 px-4 text-sm text-white">
                      {metric.format(metric.getValue(result))}
                    </td>
                  )
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

interface ResultsPanelProps {
  results: BacktestResults[] | null;
  configs: BacktestConfig[];
}

export default function ResultsPanel({ 
  results, 
  configs 
}: ResultsPanelProps) {
  const [activeTab, setActiveTab] = useState<'overview' | 'compare' | 'details'>('overview');
  const [selectedStrategy, setSelectedStrategy] = useState(0);
  const [visibleStrategies, setVisibleStrategies] = useState<number[]>([]);

  const toggleStrategyVisibility = (index: number) => {
    setVisibleStrategies(prev => 
      prev.includes(index) 
        ? prev.filter(i => i !== index)
        : [...prev, index]
    );
  };

  const tabs = [
    { id: 'overview' as const, label: 'Overview', icon: BarChart3 },
    { id: 'compare' as const, label: 'Compare', icon: Users },
    { id: 'details' as const, label: 'Details', icon: Activity }
  ];

  // Update visible strategies when results change
  React.useEffect(() => {
    if (results && results.length > 0) {
      setVisibleStrategies(results.map((_, index) => index));
    } else {
      setVisibleStrategies([]);
    }
  }, [results]);

  if (!results || results.length === 0) {
    return (
      <div className="bg-white/10 backdrop-blur-md rounded-xl p-12 border border-white/20 shadow-2xl text-center">
        <TrendingUp className="h-16 w-16 text-purple-400 mx-auto mb-4 opacity-50" />
        <h3 className="text-xl font-semibold text-white mb-2">Ready to Test Your Strategies</h3>
        <p className="text-slate-300">
          Upload your market data CSV file and configure your strategy parameters to begin backtesting.
          You can test up to 5 strategies simultaneously.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Tab Navigation */}
      <div className="bg-white/5 backdrop-blur-md rounded-xl border border-white/10 p-1">
        <div className="flex space-x-1">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium rounded-lg transition-all ${
                  activeTab === tab.id
                    ? 'bg-purple-500/20 text-purple-300 border border-purple-500/30 shadow-lg'
                    : 'text-slate-400 hover:text-slate-300 hover:bg-white/5'
                }`}
              >
                <Icon className="h-4 w-4" />
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tab Content */}
      <div className="space-y-6">
        {activeTab === 'overview' && (
          <div className="space-y-6">
            {/* Performance Cards Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
              {results.map((result, index) => (
                <div
                  key={index}
                  className="bg-white/5 backdrop-blur-md rounded-xl p-6 border border-white/10 hover:border-purple-500/30 transition-all hover:bg-white/10"
                >
                  <h4 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                    <BarChart3 className="h-5 w-5 text-purple-400" />
                    {configs[index]?.name || `Strategy ${index + 1}`}
                  </h4>
                  
                  <div className="grid grid-cols-2 gap-3">
                    <div className="bg-gradient-to-br from-green-500/20 to-emerald-600/20 p-3 rounded-lg border border-green-500/30">
                      <div className="text-xs font-medium text-green-300 mb-1">Total Return</div>
                      <div className="text-sm font-bold text-white">{result.totalReturn?.toFixed(2) || '0.00'}%</div>
                    </div>
                    <div className="bg-gradient-to-br from-blue-500/20 to-cyan-600/20 p-3 rounded-lg border border-blue-500/30">
                      <div className="text-xs font-medium text-blue-300 mb-1">Win Ratio</div>
                      <div className="text-sm font-bold text-white">{((result.winRatio || 0) * 100).toFixed(1)}%</div>
                    </div>
                    <div className="bg-gradient-to-br from-purple-500/20 to-violet-600/20 p-3 rounded-lg border border-purple-500/30">
                      <div className="text-xs font-medium text-purple-300 mb-1">Trades</div>
                      <div className="text-sm font-bold text-white">{result.tradeCount || 0}</div>
                    </div>
                    <div className="bg-gradient-to-br from-orange-500/20 to-red-600/20 p-3 rounded-lg border border-orange-500/30">
                      <div className="text-xs font-medium text-orange-300 mb-1">Final Capital</div>
                      <div className="text-sm font-bold text-white">${(result.finalCapital || 0).toLocaleString()}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'compare' && (
          <ComparisonTable 
            results={results} 
            configs={configs}
            visibleStrategies={visibleStrategies}
            onToggleVisibility={toggleStrategyVisibility}
          />
        )}

        {activeTab === 'details' && (
          <div className="space-y-6">
            {/* Strategy Selector */}
            <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 shadow-2xl">
              <h3 className="text-xl font-semibold text-white mb-4">Select Strategy for Detailed Analysis</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {configs.map((config, index) => (
                  <button
                    key={index}
                    onClick={() => setSelectedStrategy(index)}
                    className={`p-4 rounded-lg text-left transition-all ${
                      selectedStrategy === index
                        ? 'bg-purple-500/20 border border-purple-500/30 text-purple-300 shadow-lg'
                        : 'bg-white/5 border border-white/10 text-slate-300 hover:bg-white/10 hover:border-white/20'
                    }`}
                  >
                    <div className="font-medium mb-1">{config?.name || `Strategy ${index + 1}`}</div>
                    <div className="text-sm text-slate-400">
                      {results[index]?.trades?.length || 0} trades • {results[index]?.totalReturn?.toFixed(2) || '0.00'}% return
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Detailed View */}
            {results[selectedStrategy] && (
              <div className="space-y-6">
                <div className="border-b border-white/20 pb-4">
                  <h3 className="text-2xl font-semibold text-white">
                    {configs[selectedStrategy]?.name || `Strategy ${selectedStrategy + 1}`} - Detailed Analysis
                  </h3>
                  <p className="text-slate-400 mt-1">
                    Comprehensive performance analysis and trade history
                  </p>
                </div>
                
                <PerformanceMetrics results={results[selectedStrategy]} />
                
                <Suspense fallback={<LoadingScreen />}>
                  <EquityCurve results={results[selectedStrategy]} />
                </Suspense>
                
                {results[selectedStrategy]?.trades && results[selectedStrategy].trades.length > 0 && (
                  <TradeHistory results={results[selectedStrategy]} />
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}