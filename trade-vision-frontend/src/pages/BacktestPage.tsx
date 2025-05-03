import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import BacktestForm from '../components/BacktestForm';
import BacktestResults from '../components/BacktestResults';
import { BackTestRequest, BackTestResult } from '../types/backtest';
import { runBacktest } from '../services/api';

const BacktestPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<BackTestResult | null>(null);

  const handleSubmit = async (file: File, request: BackTestRequest) => {
    setIsLoading(true);
    setError(null);
    
    try {
      const result = await runBacktest(file, request);
      setResult(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An unknown error occurred');
      setResult(null);
    } finally {
      setIsLoading(false);
    }
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: { 
        when: "beforeChildren",
        staggerChildren: 0.15
      }
    }
  };

  const itemVariants = {
    hidden: { y: 20, opacity: 0 },
    visible: { 
      y: 0, 
      opacity: 1,
      transition: { type: "spring", stiffness: 100 }
    }
  };

  return (
    <motion.div 
      className="min-h-screen bg-gray-900 text-gray-100 font-mono"
      initial="hidden"
      animate="visible"
      variants={containerVariants}
    >
      <div className="container mx-auto p-6">
        <motion.header variants={itemVariants} className="mb-10 flex flex-col">
          <div className="flex items-center mb-1">
            <div className="w-3 h-3 bg-green-400 rounded-full mr-2"></div>
            <h1 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-500">TradeVision</h1>
          </div>
          <h2 className="text-4xl font-extrabold tracking-tight ml-5 text-white">
            Backtester<span className="text-blue-400">.</span>
          </h2>
          <p className="text-gray-400 ml-5 mt-1 text-sm tracking-wider">TEST YOUR STRATEGIES WITH PRECISION</p>
        </motion.header>

        <AnimatePresence>
          {error && (
            <motion.div 
              className="bg-gray-800 border-l-4 border-red-500 text-gray-100 p-4 mb-6 rounded-md backdrop-blur-sm bg-opacity-80" 
              role="alert"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, height: 0, marginBottom: 0 }}
              transition={{ duration: 0.3 }}
            >
              <div className="flex items-center">
                <svg className="h-5 w-5 text-red-500 mr-2" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <p className="font-medium text-sm tracking-wide">{error}</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          <motion.div variants={itemVariants} className="lg:col-span-5 bg-gray-800 rounded-xl shadow-xl overflow-hidden border border-gray-700">
            <div className="p-6">
              <div className="flex items-center mb-6">
                <div className="h-6 w-1 bg-blue-500 mr-3"></div>
                <h2 className="text-lg font-bold tracking-wider uppercase text-blue-400">Strategy Parameters</h2>
              </div>
              <BacktestForm onSubmit={handleSubmit} isLoading={isLoading} />
            </div>
          </motion.div>
          
          <motion.div variants={itemVariants} className="lg:col-span-7">
            <AnimatePresence mode="wait">
              {isLoading ? (
                <motion.div 
                  key="loading"
                  className="bg-gray-800 rounded-xl p-8 flex flex-col items-center justify-center h-full border border-gray-700 min-h-64"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                >
                  <svg className="animate-spin h-12 w-12 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <p className="mt-4 text-blue-400 font-medium tracking-wide text-sm">ANALYZING DATA</p>
                </motion.div>
              ) : result ? (
                <motion.div 
                  key="results"
                  className="bg-gray-800 rounded-xl shadow-xl overflow-hidden border border-gray-700"
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -20 }}
                  transition={{ type: "spring", stiffness: 100 }}
                >
                  <div className="p-6">
                    <div className="flex items-center mb-6">
                      <div className="h-6 w-1 bg-green-500 mr-3"></div>
                      <h2 className="text-lg font-bold tracking-wider uppercase text-green-400">Results</h2>
                    </div>
                    <BacktestResults result={result} />
                  </div>
                </motion.div>
              ) : (
                <motion.div 
                  key="empty"
                  className="bg-gray-800 rounded-xl p-8 flex flex-col items-center justify-center h-full border border-gray-700 border-dashed min-h-64"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                >
                  <div className="relative">
                    <svg className="h-16 w-16 text-gray-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                    <motion.div 
                      className="absolute w-full h-full top-0 left-0 border-2 border-blue-400 rounded-full opacity-75"
                      animate={{ 
                        scale: [1, 1.1, 1],
                        opacity: [0.5, 0.8, 0.5]
                      }}
                      transition={{ 
                        duration: 2,
                        repeat: Infinity,
                        ease: "easeInOut"
                      }}
                    />
                  </div>
                  <p className="mt-6 text-gray-400 text-sm tracking-wider uppercase">Configure parameters to view results</p>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
        </div>
        
        <motion.footer variants={itemVariants} className="mt-12 text-center text-xs text-gray-600">
          <div className="inline-flex items-center">
            <span className="h-1 w-1 bg-blue-500 rounded-full mr-2"></span>
            <span className="h-1 w-1 bg-purple-500 rounded-full mr-2"></span>
            <span className="tracking-widest">TRADEVISION 2.0</span>
          </div>
        </motion.footer>
      </div>
    </motion.div>
  );
};

export default BacktestPage;