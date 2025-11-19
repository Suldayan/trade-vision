import React from 'react';
import { TrendingUp } from 'lucide-react';

export default function LoadingScreen() {
  return (
    <div className="bg-white/10 backdrop-blur-md rounded-xl border border-white/20 shadow-2xl p-8">
      <div className="flex flex-col items-center justify-center h-32">
        <div className="relative mb-4">
          <div className="animate-spin rounded-full h-8 w-8 border-2 border-slate-600">
            <div className="absolute inset-0 rounded-full border-2 border-transparent border-t-purple-400 border-r-blue-400 animate-spin"></div>
          </div>
          <div className="absolute inset-0 flex items-center justify-center">
            <TrendingUp className="h-4 w-4 text-purple-400 animate-pulse" />
          </div>
        </div>
        <p className="text-slate-300 text-sm">Loading...</p>
      </div>
    </div>
  );
};
