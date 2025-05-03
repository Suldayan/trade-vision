import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { BackTestRequest } from '../types/backtest';
import StrategyEditor from './strategy-editor/StrategyEditor';
import { validateStrategy, getStrategyDisplayText, DEFAULT_BACKTEST_REQUEST} from '../utils/backtestUtils'; 

interface BacktestFormProps {
  onSubmit: (file: File, request: BackTestRequest) => void;
  isLoading: boolean;
}

const BacktestForm: React.FC<BacktestFormProps> = ({ onSubmit, isLoading }) => {
  const [file, setFile] = useState<File | null>(null);
  const [request, setRequest] = useState<BackTestRequest>(DEFAULT_BACKTEST_REQUEST);
  
  const [showStrategyEditor, setShowStrategyEditor] = useState(false);
  const [, setActiveTab] = useState<'entry' | 'exit'>('entry');

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files && event.target.files.length > 0) {
      setFile(event.target.files[0]);
    }
  };

  const handleNumberChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    field: keyof BackTestRequest
  ) => {
    const value = parseFloat(e.target.value);
    setRequest((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleCheckboxChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    field: keyof BackTestRequest
  ) => {
    setRequest((prev) => ({
      ...prev,
      [field]: e.target.checked,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (file) {
      // Validate the strategy before submission
      try {
        validateStrategy(request);
        onSubmit(file, request);
      } catch (error) {
        alert(`Strategy validation error: ${error instanceof Error ? error.message : 'Unknown error'}`);
      }
    }
  };

  // Open strategy editor with specified tab
  const openStrategyEditor = (tab: 'entry' | 'exit') => {
    setActiveTab(tab);
    setShowStrategyEditor(true);
  };

  // Handle closing strategy editor
  const handleCloseStrategyEditor = () => {
    setShowStrategyEditor(false);
  };

  // Handle saving strategy changes
  const handleSaveStrategyChanges = () => {
    try {
      validateStrategy(request);
      // The request state is already updated via the setRequest function passed to StrategyEditor
      // So we just need to close the editor and possibly do any additional validation
      setShowStrategyEditor(false);
    } catch (error) {
      alert(`Strategy validation error: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  };

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
      className="max-w-4xl mx-auto p-4 text-gray-200 bg-gray-900 rounded-lg shadow-2xl border border-gray-800"
    >
      <motion.h1 
        initial={{ y: -20 }}
        animate={{ y: 0 }}
        transition={{ duration: 0.5, type: "spring" }}
        className="text-3xl font-bold mb-8 text-center text-white border-b border-gray-800 pb-4"
      >
        Backtest Trading Strategy
      </motion.h1>
      
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* File Upload */}
        <motion.div 
          whileHover={{ scale: 1.01 }}
          transition={{ type: "spring", stiffness: 300 }}
          className="border border-gray-700 p-5 rounded-lg bg-gray-800/50 backdrop-blur-sm shadow-lg"
        >
          <h2 className="text-lg font-semibold mb-3 text-cyan-400 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
            </svg>
            Data Source
          </h2>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Upload Historical Data (CSV)
            </label>
            <div className="relative">
              <input
                type="file"
                accept=".csv"
                onChange={handleFileChange}
                className="hidden"
                id="file-upload"
              />
              <label 
                htmlFor="file-upload" 
                className="cursor-pointer flex items-center justify-center w-full py-3 px-4 rounded-md text-sm
                           bg-gray-900 border border-gray-700 hover:border-cyan-400 transition-colors duration-300
                           text-gray-300 hover:text-cyan-400"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                </svg>
                {file ? file.name : 'Choose OHLCV Data File'}
              </label>
            </div>
            <p className="mt-2 text-xs text-gray-400">
              File should contain OHLCV data (Open, High, Low, Close, Volume).
            </p>
          </div>
        </motion.div>
        
        {/* Capital and Risk Parameters */}
        <motion.div 
          whileHover={{ scale: 1.01 }}
          transition={{ type: "spring", stiffness: 300 }}
          className="border border-gray-700 p-5 rounded-lg bg-gray-800/50 backdrop-blur-sm shadow-lg"
        >
          <h2 className="text-lg font-semibold mb-3 text-cyan-400 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            Capital & Risk Parameters
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Initial Capital ($)
              </label>
              <input
                type="number"
                value={request.initialCapital}
                onChange={(e) => handleNumberChange(e, 'initialCapital')}
                min="100"
                step="100"
                className="w-full p-3 bg-gray-900 border border-gray-700 rounded-md text-gray-200 focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300"
                required
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Risk Per Trade (0-1)
              </label>
              <input
                type="number"
                value={request.riskPerTrade}
                onChange={(e) => handleNumberChange(e, 'riskPerTrade')}
                min="0.01"
                max="1"
                step="0.01"
                className="w-full p-3 bg-gray-900 border border-gray-700 rounded-md text-gray-200 focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300"
                required
              />
              <p className="mt-2 text-xs text-gray-400">
                Fraction of capital risked on each trade (e.g., 0.02 = 2%).
              </p>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Commission Rate (0-1)
              </label>
              <input
                type="number"
                value={request.commissionRate}
                onChange={(e) => handleNumberChange(e, 'commissionRate')}
                min="0"
                max="0.1"
                step="0.0001"
                className="w-full p-3 bg-gray-900 border border-gray-700 rounded-md text-gray-200 focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300"
                required
              />
              <p className="mt-2 text-xs text-gray-400">
                Transaction fee as fraction of trade value (e.g., 0.001 = 0.1%).
              </p>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">
                Slippage (%)
              </label>
              <input
                type="number"
                value={request.slippagePercent}
                onChange={(e) => handleNumberChange(e, 'slippagePercent')}
                min="0"
                max="5"
                step="0.1"
                className="w-full p-3 bg-gray-900 border border-gray-700 rounded-md text-gray-200 focus:ring-2 focus:ring-cyan-500 focus:border-transparent transition-all duration-300"
                required
              />
              <p className="mt-2 text-xs text-gray-400">
                Estimated price slippage percentage.
              </p>
            </div>
          </div>
          
          <div className="mt-5">
            <label className="flex items-center space-x-3 text-gray-300">
              <input
                type="checkbox"
                checked={request.allowShort}
                onChange={(e) => handleCheckboxChange(e, 'allowShort')}
                className="h-5 w-5 rounded border-gray-600 text-cyan-500 focus:ring-cyan-500 focus:ring-offset-gray-900"
              />
              <span className="text-sm font-medium flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
                Allow Short Selling
              </span>
            </label>
          </div>
        </motion.div>
        
        {/* Strategy Configuration */}
        <motion.div 
          whileHover={{ scale: 1.01 }}
          transition={{ type: "spring", stiffness: 300 }}
          className="border border-gray-700 p-5 rounded-lg bg-gray-800/50 backdrop-blur-sm shadow-lg"
        >
          <h2 className="text-lg font-semibold mb-3 text-cyan-400 flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
            Strategy Configuration
          </h2>
          
          {/* Entry Conditions */}
          <div className="mb-6">
            <div className="flex justify-between items-center mb-3">
              <h3 className="text-md font-medium text-green-400 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
                </svg>
                Entry Conditions
              </h3>
              <motion.button
                type="button"
                onClick={() => openStrategyEditor('entry')}
                className="text-cyan-400 hover:text-cyan-300 text-sm flex items-center transition-colors duration-300"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit Conditions
              </motion.button>
            </div>
            <div className="bg-gray-900 p-4 border border-gray-700 rounded-md">
              <p className="text-sm text-gray-300">
                {getStrategyDisplayText(request.entryConditions, request.requireAllEntryConditions)}
              </p>
            </div>
            <div className="flex items-center mt-3">
              <input
                type="checkbox"
                checked={request.requireAllEntryConditions}
                onChange={(e) => handleCheckboxChange(e, 'requireAllEntryConditions')}
                className="h-4 w-4 rounded border-gray-600 text-cyan-500 focus:ring-cyan-500 focus:ring-offset-gray-900"
              />
              <label className="ml-2 text-sm text-gray-300">
                Require all entry conditions to be met
              </label>
            </div>
          </div>
          
          {/* Exit Conditions */}
          <div>
            <div className="flex justify-between items-center mb-3">
              <h3 className="text-md font-medium text-red-400 flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                </svg>
                Exit Conditions
              </h3>
              <motion.button
                type="button"
                onClick={() => openStrategyEditor('exit')}
                className="text-cyan-400 hover:text-cyan-300 text-sm flex items-center transition-colors duration-300"
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.95 }}
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                Edit Conditions
              </motion.button>
            </div>
            <div className="bg-gray-900 p-4 border border-gray-700 rounded-md">
              <p className="text-sm text-gray-300">
                {getStrategyDisplayText(request.exitConditions, request.requireAllExitConditions)}
              </p>
            </div>
            <div className="flex items-center mt-3">
              <input
                type="checkbox"
                checked={request.requireAllExitConditions}
                onChange={(e) => handleCheckboxChange(e, 'requireAllExitConditions')}
                className="h-4 w-4 rounded border-gray-600 text-cyan-500 focus:ring-cyan-500 focus:ring-offset-gray-900"
              />
              <label className="ml-2 text-sm text-gray-300">
                Require all exit conditions to be met
              </label>
            </div>
          </div>
        </motion.div>
        
        {/* Submit Button */}
        <div className="flex justify-end pt-2">
          <motion.button
            type="submit"
            disabled={!file || isLoading}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className={`py-3 px-8 rounded-md font-medium text-md flex items-center ${
              !file || isLoading
                ? 'bg-gray-700 text-gray-400 cursor-not-allowed'
                : 'bg-gradient-to-r from-cyan-500 to-blue-600 text-white shadow-lg hover:shadow-cyan-500/20 transition-all duration-300'
            }`}
          >
            {isLoading ? (
              <>
                <svg className="animate-spin -ml-1 mr-2 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Running Backtest...
              </>
            ) : (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Run Backtest
              </>
            )}
          </motion.button>
        </div>
      </form>
      
      {/* Strategy Editor Modal */}
      <AnimatePresence>
        {showStrategyEditor && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-sm"
          >
            <StrategyEditor
              isOpen={showStrategyEditor}
              onClose={handleCloseStrategyEditor}
              onSave={handleSaveStrategyChanges}
              request={request}
              setRequest={setRequest}
            />
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export default BacktestForm;