import { useMemo } from "react";
import { LineChart, ResponsiveContainer, CartesianGrid, XAxis, YAxis, Tooltip, Legend, Line } from "recharts";
import type { BacktestResults } from "../../types/strategy";

interface EquityCurveProps {
  results: BacktestResults;
}

interface ChartDataPoint {
  period: number;
  equity: number;
}

export default function EquityCurve({ results }: EquityCurveProps) {
  const chartData = useMemo(() => {
    const equityCurve = results.equityCurve;
    const dataLength = equityCurve.length;
    
    const MAX_POINTS = 500; 
    
    if (dataLength <= MAX_POINTS) {
      return equityCurve.map((value, index) => ({
        period: index,
        equity: Math.round(value * 100) / 100
      }));
    }
    
    const step = Math.ceil(dataLength / MAX_POINTS);
    const downsampledData: ChartDataPoint[] = [];
    
    for (let i = 0; i < dataLength; i += step) {
      downsampledData.push({
        period: i,
        equity: Math.round(equityCurve[i] * 100) / 100
      });
    }
    
    if (downsampledData[downsampledData.length - 1].period !== dataLength - 1) {
      downsampledData.push({
        period: dataLength - 1,
        equity: Math.round(equityCurve[dataLength - 1] * 100) / 100
      });
    }
    
    return downsampledData;
  }, [results.equityCurve]);

  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  };

  return (
    <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 shadow-2xl">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-white">Equity Curve</h2>
        <span className="text-sm text-slate-400">
          {chartData.length} of {results.equityCurve.length} points
        </span>
      </div>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart 
            data={chartData}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
            <XAxis 
              dataKey="period" 
              stroke="rgba(255,255,255,0.7)"
              fontSize={12}
              tick={{ fill: 'rgba(255,255,255,0.7)' }}
            />
            <YAxis 
              stroke="rgba(255,255,255,0.7)"
              fontSize={12}
              tick={{ fill: 'rgba(255,255,255,0.7)' }}
              tickFormatter={formatCurrency}
            />
            <Tooltip 
              contentStyle={{ 
                backgroundColor: 'rgba(0,0,0,0.9)', 
                border: '1px solid rgba(255,255,255,0.2)',
                borderRadius: '8px',
                color: 'white'
              }}
              formatter={(value: number) => [formatCurrency(value), 'Equity']}
              labelFormatter={(label: string) => `Period: ${label}`}
            />
            <Legend wrapperStyle={{ color: 'rgba(255,255,255,0.7)' }} />
            <Line 
              type="monotone" 
              dataKey="equity" 
              stroke="#8B5CF6" 
              strokeWidth={2}
              dot={false}
              name="Portfolio Value"
              connectNulls={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}