import React, { useMemo } from "react";
import { BarChart3, TrendingUp, DollarSign, Target, FileText } from "lucide-react";
import type { BacktestResults } from "../../types/strategy";

interface PerformanceMetricsProps {
  results: BacktestResults;
}

interface MetricCardData {
  label: string;
  value: string;
  icon: React.ComponentType<{ className?: string }>;
  colorClasses: {
    background: string;
    border: string;
    icon: string;
    text: string;
  };
}

export default function PerformanceMetrics({ results }: PerformanceMetricsProps) {
  const formatCurrency = (value: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(value);
  };

  const formatPercentage = (value: number): string => {
    return `${(value * 100).toFixed(2)}%`;
  };

  const metrics: MetricCardData[] = useMemo(() => [
    {
      label: "Total Return",
      value: `${results.totalReturn.toFixed(2)}%`,
      icon: TrendingUp,
      colorClasses: {
        background: "bg-gradient-to-br from-green-500/20 to-emerald-600/20",
        border: "border-green-500/30",
        icon: "text-green-400",
        text: "text-green-300"
      }
    },
    {
      label: "Final Capital",
      value: formatCurrency(results.finalCapital),
      icon: DollarSign,
      colorClasses: {
        background: "bg-gradient-to-br from-blue-500/20 to-cyan-600/20",
        border: "border-blue-500/30",
        icon: "text-blue-400",
        text: "text-blue-300"
      }
    },
    {
      label: "Win Ratio",
      value: formatPercentage(results.winRatio),
      icon: Target,
      colorClasses: {
        background: "bg-gradient-to-br from-purple-500/20 to-violet-600/20",
        border: "border-purple-500/30",
        icon: "text-purple-400",
        text: "text-purple-300"
      }
    },
    {
      label: "Trades",
      value: results.tradeCount.toString(),
      icon: FileText,
      colorClasses: {
        background: "bg-gradient-to-br from-orange-500/20 to-red-600/20",
        border: "border-orange-500/30",
        icon: "text-orange-400",
        text: "text-orange-300"
      }
    }
  ], [results]);

  return (
    <div className="bg-white/10 backdrop-blur-md rounded-xl p-6 border border-white/20 shadow-2xl">
      <h2 className="text-xl font-semibold text-white mb-4 flex items-center gap-2">
        <BarChart3 className="h-5 w-5" />
        Performance Metrics
      </h2>
      
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {metrics.map((metric) => {
          const IconComponent = metric.icon;
          return (
            <div 
              key={metric.label}
              className={`${metric.colorClasses.background} p-4 rounded-lg border ${metric.colorClasses.border}`}
              role="region"
              aria-label={`${metric.label}: ${metric.value}`}
            >
              <div className="flex items-center gap-2 mb-1">
                <IconComponent className={`h-4 w-4 ${metric.colorClasses.icon}`} />
                <span className={`text-xs font-medium ${metric.colorClasses.text}`}>
                  {metric.label}
                </span>
              </div>
              <div className="text-lg font-bold text-white">
                {metric.value}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}