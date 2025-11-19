import React, { useState } from "react";
import type { BacktestResults, Trade } from "../../types/strategy";

interface TradeHistoryProps {
  results: BacktestResults;
}

export default function TradeHistory({ results }: TradeHistoryProps) {
  const [showAllTrades, setShowAllTrades] = useState(false);
  
  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  };
  
  const displayedTrades = showAllTrades 
    ? results.trades 
    : results.trades.slice(0, 10);
  
  return (
    <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 shadow-2xl">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-white">
          {showAllTrades ? `All ${results.trades.length} Trades` : 'First 10 Trades'}
        </h2>
        
        <select
          value={showAllTrades ? 'all' : 'first10'}
          onChange={(e) => setShowAllTrades(e.target.value === 'all')}
          className="bg-white/10 border border-white/20 rounded-lg px-3 py-1 text-white text-sm backdrop-blur-md focus:outline-none focus:ring-2 focus:ring-blue-400"
        >
          <option value="first10" className="bg-slate-800">First 10 Trades</option>
          <option value="all" className="bg-slate-800">All Trades ({results.trades.length})</option>
        </select>
      </div>
      
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/20">
              <th className="text-left py-2 text-slate-300 font-medium">Date</th>
              <th className="text-right py-2 text-slate-300 font-medium">Entry</th>
              <th className="text-right py-2 text-slate-300 font-medium">Exit</th>
              <th className="text-right py-2 text-slate-300 font-medium">Size</th>
              <th className="text-right py-2 text-slate-300 font-medium">P&L</th>
            </tr>
          </thead>
          <tbody>
            {displayedTrades.map((trade: Trade, index: number) => (
              <tr key={index} className="border-b border-white/10">
                <td className="py-2 text-slate-300">
                  {new Date(trade.date).toLocaleDateString()}
                </td>
                <td className="text-right py-2 text-white">
                  {formatCurrency(trade.entryPrice)}
                </td>
                <td className="text-right py-2 text-white">
                  {formatCurrency(trade.exitPrice)}
                </td>
                <td className="text-right py-2 text-slate-300">
                  {trade.positionSize.toFixed(2)}
                </td>
                <td className={`text-right py-2 font-medium ${
                  trade.pnl >= 0 ? 'text-green-400' : 'text-red-400'
                }`}>
                  {formatCurrency(trade.pnl)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-4 text-sm text-slate-400 text-center">
        Showing {displayedTrades.length} of {results.trades.length} trades
      </div>
    </div>
  );
}