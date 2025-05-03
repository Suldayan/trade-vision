import React, { useState } from 'react';
import { BackTestResult } from '../types/backtest';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, ReferenceLine } from 'recharts';
import { TrendingUp, TrendingDown, ArrowUp, ArrowDown, BarChart2, DollarSign, Target, Activity, Calendar, Clock, Sliders, ChevronDown, ChevronUp } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface BacktestResultsProps {
  result: BackTestResult | null;
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    value: number;
    payload: {
      index: number;
    };
  }>;
}

const BacktestResults: React.FC<BacktestResultsProps> = ({ result }) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'trades'>('overview');
  const [showAllTrades, setShowAllTrades] = useState(false);

  if (!result) {
    return (
      <motion.div 
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex items-center justify-center h-96 bg-gray-900 rounded-xl border border-gray-800"
      >
        <p className="text-gray-400">No backtest results available</p>
      </motion.div>
    );
  }

  const chartData = result.equityCurve.map((value, index) => ({
    index,
    equity: value,
  }));

  // Calculate baseline for starting capital
  const initialCapital = chartData[0]?.equity || 0;

  // Format currency
  const formatCurrency = (value: number | bigint) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  };

  // Format percentage
  const formatPercentage = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
  };

  // Custom tooltip for the chart with proper TypeScript types
  const CustomTooltip = ({ active, payload }: CustomTooltipProps) => {
    if (active && payload && payload.length) {
      const tradeNumber = payload[0].payload.index;
      const equityValue = payload[0].value;
      
      return (
        <motion.div 
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-gray-800 p-4 shadow-lg border border-gray-700 rounded-lg backdrop-blur-sm"
        >
          <p className="text-gray-400 mb-1">Trade #{tradeNumber + 1}</p>
          <p className="font-medium text-white">{formatCurrency(equityValue)}</p>
          <p className="text-sm text-gray-400">
            {formatPercentage(((equityValue - initialCapital) / initialCapital) * 100)} from start
          </p>
        </motion.div>
      );
    }
    return null;
  };

  // Determine colors based on performance
  const getPerformanceColor = (value: number) => {
    return value >= 0 ? 'text-green-400' : 'text-red-400';
  };

  const getPerformanceBgColor = (value: number) => {
    return value >= 0 ? 'bg-green-400/10' : 'bg-red-400/10';
  };

  const getBorderColor = (value: number) => {
    return value >= 0 ? 'border-green-400/30' : 'border-red-400/30';
  };

  // Calculate more metrics
  const annualizedReturn = result.totalReturn / Math.max(1, result.tradeCount / 365) * 100;
  const avgTradeReturn = result.trades.length > 0 
    ? result.trades.reduce((sum, trade) => sum + trade.pnl, 0) / result.trades.length
    : 0;

  // Animation variants
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: { 
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: { 
      y: 0, 
      opacity: 1,
      transition: { type: 'spring', stiffness: 300, damping: 24 }
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="bg-gray-900 rounded-xl shadow-xl border border-gray-800 text-gray-200"
    >
      {/* Header */}
      <div className="p-6 border-b border-gray-800">
        <motion.h2 
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2 }}
          className="text-2xl font-bold text-white flex items-center"
        >
          <Activity className="mr-2 text-blue-400" size={24} />
          Backtest Results
        </motion.h2>
        <motion.p 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3 }}
          className="text-gray-400 mt-1"
        >
          Strategy analysis based on historical data
        </motion.p>
      </div>
      
      {/* Key Metrics */}
      <motion.div 
        variants={containerVariants}
        initial="hidden"
        animate="visible"
        className="grid grid-cols-2 lg:grid-cols-4 gap-4 p-6"
      >
        <motion.div 
          variants={itemVariants}
          className={`bg-gray-800 p-4 rounded-lg border ${getBorderColor(result.totalReturn)}`}
        >
          <div className="flex items-center mb-2">
            <div className={`p-2 rounded-full mr-2 ${getPerformanceBgColor(result.totalReturn)}`}>
              {result.totalReturn >= 0 ? <TrendingUp size={20} className="text-green-400" /> : <TrendingDown size={20} className="text-red-400" />}
            </div>
            <p className="text-gray-300 font-medium">Total Return</p>
          </div>
          <p className={`text-2xl font-bold ${getPerformanceColor(result.totalReturn)}`}>
            {formatPercentage(result.totalReturn)}
          </p>
        </motion.div>
        
        <motion.div 
          variants={itemVariants}
          className="bg-gray-800 p-4 rounded-lg border border-gray-700"
        >
          <div className="flex items-center mb-2">
            <div className="p-2 rounded-full mr-2 bg-blue-400/10 text-blue-400">
              <DollarSign size={20} />
            </div>
            <p className="text-gray-300 font-medium">Final Capital</p>
          </div>
          <p className="text-2xl font-bold text-white">{formatCurrency(result.finalCapital)}</p>
        </motion.div>
        
        <motion.div 
          variants={itemVariants}
          className="bg-gray-800 p-4 rounded-lg border border-gray-700"
        >
          <div className="flex items-center mb-2">
            <div className="p-2 rounded-full mr-2 bg-purple-400/10 text-purple-400">
              <Target size={20} />
            </div>
            <p className="text-gray-300 font-medium">Win Rate</p>
          </div>
          <p className="text-2xl font-bold text-white">{result.winRatio.toFixed(2)}%</p>
        </motion.div>
        
        <motion.div 
          variants={itemVariants}
          className="bg-gray-800 p-4 rounded-lg border border-red-900/30"
        >
          <div className="flex items-center mb-2">
            <div className="p-2 rounded-full mr-2 bg-red-400/10 text-red-400">
              <ArrowDown size={20} />
            </div>
            <p className="text-gray-300 font-medium">Max Drawdown</p>
          </div>
          <p className="text-2xl font-bold text-red-400">-{Math.abs(result.maxDrawdown).toFixed(2)}%</p>
        </motion.div>
      </motion.div>
      
      {/* Tab Navigation */}
      <div className="px-6 border-t border-gray-800">
        <div className="flex border-b border-gray-800">
          <motion.button 
            whileHover={{ y: -2 }}
            whileTap={{ y: 0 }}
            onClick={() => setActiveTab('overview')}
            className={`py-4 px-6 font-medium relative ${activeTab === 'overview' ? 'text-blue-400' : 'text-gray-400'}`}
          >
            Overview
            {activeTab === 'overview' && (
              <motion.div 
                layoutId="tab-indicator"
                className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-400"
              />
            )}
          </motion.button>
          <motion.button 
            whileHover={{ y: -2 }}
            whileTap={{ y: 0 }}
            onClick={() => setActiveTab('trades')}
            className={`py-4 px-6 font-medium relative ${activeTab === 'trades' ? 'text-blue-400' : 'text-gray-400'}`}
          >
            Trades
            {activeTab === 'trades' && (
              <motion.div 
                layoutId="tab-indicator"
                className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-400"
              />
            )}
          </motion.button>
        </div>
      </div>
      
      <AnimatePresence mode="wait">
        {activeTab === 'overview' ? (
          <motion.div
            key="overview"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.3 }}
          >
            {/* Additional Metrics */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 p-6">
              <div className="bg-gray-800 p-4 rounded-lg border border-gray-700">
                <p className="text-gray-400 text-sm mb-1">Annualized Return</p>
                <p className={`text-lg font-bold ${getPerformanceColor(annualizedReturn)}`}>
                  {formatPercentage(annualizedReturn)}
                </p>
              </div>
              
              <div className="bg-gray-800 p-4 rounded-lg border border-gray-700">
                <p className="text-gray-400 text-sm mb-1">Avg. Trade Profit</p>
                <p className={`text-lg font-bold ${getPerformanceColor(avgTradeReturn)}`}>
                  {formatCurrency(avgTradeReturn)}
                </p>
              </div>
              
              <div className="bg-gray-800 p-4 rounded-lg border border-gray-700">
                <p className="text-gray-400 text-sm mb-1">Total Trades</p>
                <p className="text-lg font-bold text-white">{result.tradeCount}</p>
              </div>
              
              <div className="bg-gray-800 p-4 rounded-lg border border-gray-700">
                <p className="text-gray-400 text-sm mb-1">Profit Factor</p>
                <p className="text-lg font-bold text-white">
                  {(result.winRatio / (100 - result.winRatio)).toFixed(2)}
                </p>
              </div>
            </div>
            
            {/* Equity Curve */}
            <div className="p-6">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold text-white flex items-center">
                  <BarChart2 size={20} className="mr-2 text-blue-400" />
                  Equity Curve
                </h3>
                <div className="flex items-center text-sm text-gray-400 bg-gray-800 px-3 py-1 rounded-full">
                  <Calendar size={14} className="mr-2" />
                  <span>{result.tradeCount} trades</span>
                </div>
              </div>
              
              <motion.div 
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.2, duration: 0.5 }}
                className="h-80 relative"
              >
                {chartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                      data={chartData}
                      margin={{ top: 10, right: 10, left: 10, bottom: 20 }}
                    >
                      <defs>
                        <linearGradient id="equityGradient" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.5} />
                          <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke="#2d3748" />
                      <XAxis 
                        dataKey="index" 
                        axisLine={false}
                        tickLine={false}
                        tickFormatter={(value) => `#${value + 1}`}
                        tick={{ fontSize: 12, fill: '#a0aec0' }}
                      />
                      <YAxis 
                        axisLine={false}
                        tickLine={false}
                        tick={{ fontSize: 12, fill: '#a0aec0' }}
                        tickFormatter={(value) => formatCurrency(value).replace('$', '')}
                        width={60}
                      />
                      <Tooltip content={<CustomTooltip />} />
                      <ReferenceLine y={initialCapital} stroke="#4a5568" strokeDasharray="3 3" />
                      <Legend wrapperStyle={{ color: '#a0aec0' }} />
                      <Line
                        type="monotone"
                        dataKey="equity"
                        name="Equity"
                        stroke="#3b82f6"
                        strokeWidth={2}
                        dot={false}
                        activeDot={{ r: 6, stroke: '#1e293b', strokeWidth: 2 }}
                        fill="url(#equityGradient)"
                      />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex items-center justify-center h-full bg-gray-800 rounded-lg">
                    <p className="text-gray-400">No equity curve data available</p>
                  </div>
                )}
              </motion.div>
            </div>
            
            {/* Performance Summary */}
            <div className="p-6 border-t border-gray-800">
              <h3 className="text-lg font-bold text-white mb-4 flex items-center">
                <Sliders size={20} className="mr-2 text-blue-400" />
                Performance Metrics
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Left column */}
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Initial Capital</span>
                    <span className="text-white font-medium">{formatCurrency(initialCapital)}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Final Capital</span>
                    <span className="text-white font-medium">{formatCurrency(result.finalCapital)}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Absolute Profit</span>
                    <span className={`font-medium ${getPerformanceColor(result.finalCapital - initialCapital)}`}>
                      {formatCurrency(result.finalCapital - initialCapital)}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Total Return</span>
                    <span className={`font-medium ${getPerformanceColor(result.totalReturn)}`}>
                      {formatPercentage(result.totalReturn)}
                    </span>
                  </div>
                </div>
                
                {/* Right column */}
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Win Rate</span>
                    <span className="text-white font-medium">{result.winRatio.toFixed(2)}%</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Max Drawdown</span>
                    <span className="text-red-400 font-medium">-{Math.abs(result.maxDrawdown).toFixed(2)}%</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Avg. Winning Trade</span>
                    <span className="text-green-400 font-medium">
                      {formatCurrency(
                        result.trades.filter(t => t.pnl > 0).reduce((sum, t) => sum + t.pnl, 0) / 
                        Math.max(1, result.trades.filter(t => t.pnl > 0).length)
                      )}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Avg. Losing Trade</span>
                    <span className="text-red-400 font-medium">
                      {formatCurrency(
                        Math.abs(result.trades.filter(t => t.pnl < 0).reduce((sum, t) => sum + t.pnl, 0)) / 
                        Math.max(1, result.trades.filter(t => t.pnl < 0).length)
                      )}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        ) : (
          <motion.div
            key="trades"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.3 }}
            className="p-6"
          >
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-bold text-white flex items-center">
                <Activity size={20} className="mr-2 text-blue-400" />
                Trading Activity
              </h3>
              <motion.button
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
                onClick={() => setShowAllTrades(!showAllTrades)}
                className="flex items-center text-sm text-blue-400 bg-blue-900/20 hover:bg-blue-900/30 px-3 py-1 rounded-full transition-colors"
              >
                {showAllTrades ? (
                  <>
                    <ChevronUp size={14} className="mr-1" />
                    Show Less
                  </>
                ) : (
                  <>
                    <ChevronDown size={14} className="mr-1" />
                    Show All ({result.tradeCount})
                  </>
                )}
              </motion.button>
            </div>
            
            <div className="overflow-x-auto rounded-lg border border-gray-800">
              <table className="min-w-full divide-y divide-gray-800">
                <thead>
                  <tr className="bg-gray-800/50">
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">#</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Direction</th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Entry Price</th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Exit Price</th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Position Size</th>
                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Profit/Loss</th>
                  </tr>
                </thead>
                <tbody className="bg-gray-900 divide-y divide-gray-800">
                  {result.trades.slice(0, showAllTrades ? undefined : 6).map((trade, i) => (
                    <motion.tr 
                      key={i}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: i * 0.05 }}
                      className="hover:bg-gray-800/50 transition-colors"
                    >
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-300">{i + 1}</td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm">
                        <span className={`inline-flex items-center px-2 py-1 rounded ${trade.pnl >= 0 ? 'bg-green-400/10 text-green-400' : 'bg-red-400/10 text-red-400'}`}>
                          {trade.pnl >= 0 ? 'LONG' : 'SHORT'}
                        </span>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-right text-gray-300">
                        {formatCurrency(trade.entryPrice)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-right text-gray-300">
                        {formatCurrency(trade.exitPrice)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-right text-gray-300">
                        {formatCurrency(trade.positionSize)}
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-right font-medium">
                        <div className={`inline-flex items-center px-2 py-1 rounded ${trade.pnl >= 0 ? 'bg-green-400/10 text-green-400' : 'bg-red-400/10 text-red-400'}`}>
                          {trade.pnl >= 0 ? 
                            <ArrowUp size={14} className="mr-1" /> : 
                            <ArrowDown size={14} className="mr-1" />
                          }
                          {formatCurrency(Math.abs(trade.pnl))}
                        </div>
                      </td>
                    </motion.tr>
                  ))}
                  {!showAllTrades && result.trades.length > 6 && (
                    <tr>
                      <td colSpan={6} className="px-4 py-3 text-center text-sm text-gray-400 bg-gray-800/50">
                        Showing 6 of {result.trades.length} trades
                      </td>
                    </tr>
                  )}
                  {result.trades.length === 0 && (
                    <tr>
                      <td colSpan={6} className="px-4 py-6 text-center text-gray-400">
                        No trades available
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
            
            {/* Trade Statistics */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
              <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
                <h4 className="text-white font-medium mb-4 flex items-center">
                  <Clock size={18} className="mr-2 text-blue-400" />
                  Trade Frequency
                </h4>
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Total Trades</span>
                    <span className="text-white">{result.tradeCount}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Winning Trades</span>
                    <span className="text-green-400">
                      {Math.round((result.winRatio / 100) * result.tradeCount)}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Losing Trades</span>
                    <span className="text-red-400">
                      {result.tradeCount - Math.round((result.winRatio / 100) * result.tradeCount)}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Win/Loss Ratio</span>
                    <span className="text-white">
                      {(result.winRatio / (100 - result.winRatio)).toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
                <h4 className="text-white font-medium mb-4 flex items-center">
                  <DollarSign size={18} className="mr-2 text-blue-400" />
                  Profit Analysis
                </h4>
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Total Profit</span>
                    <span className={`${getPerformanceColor(result.finalCapital - initialCapital)}`}>
                      {formatCurrency(result.finalCapital - initialCapital)}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Largest Win</span>
                    <span className="text-green-400">
                      {formatCurrency(Math.max(...result.trades.map(t => t.pnl), 0))}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Largest Loss</span>
                    <span className="text-red-400">
                      {formatCurrency(Math.abs(Math.min(...result.trades.map(t => t.pnl), 0)))}
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Risk/Reward Ratio</span>
                    <span className="text-white">
                      {(
                        Math.abs(Math.min(...result.trades.map(t => t.pnl), 0)) / 
                        Math.max(0.01, Math.max(...result.trades.map(t => t.pnl), 0))
                      ).toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default BacktestResults;