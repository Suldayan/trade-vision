import { useCallback, useState } from 'react'
import SettingInput from './SettingInput'
import type { BacktestConfig } from '../types';

export default function BasicSettingsPanel() {
    const [config, setConfig] = useState<BacktestConfig>({
        initialCapital: 10000,
        riskPerTrade: 0.02,
        commissionRate: 0.001,
        slippagePercent: 0.1,
        allowShort: false,
        requireAllEntryConditions: true,
        requireAllExitConditions: false,
        entryConditions: [{
            type: 'RSI_THRESHOLD',
            parameters: {
            period: 14,
            upperThreshold: 70,
            lowerThreshold: 30,
            checkOverbought: false
            }
        }],
        exitConditions: [{
            type: 'RSI_THRESHOLD',
            parameters: {
            period: 14,
            upperThreshold: 70,
            lowerThreshold: 30,
            checkOverbought: true
            }
        }]
    });

    const updateConfig = useCallback((path: string, value: number | boolean): void => {
        setConfig(prev => {
        const keys = path.split('.');
        const newConfig = JSON.parse(JSON.stringify(prev));
        
        let current = newConfig;
        for (let i = 0; i < keys.length - 1; i++) {
            const key = keys[i];
            if (key.includes('[') && key.includes(']')) {
            const arrayKey = key.split('[')[0];
            const index = parseInt(key.split('[')[1].split(']')[0]);
            current = current[arrayKey][index];
            } else {
            current = current[key];
            }
        }
        
        const lastKey = keys[keys.length - 1];
        if (lastKey.includes('[') && lastKey.includes(']')) {
            const arrayKey = lastKey.split('[')[0];
            const index = parseInt(lastKey.split('[')[1].split(']')[0]);
            current[arrayKey][index] = value;
        } else {
            current[lastKey] = value;
        }
        
        return newConfig;
        });
    }, []);
  return (
    <div className="space-y-4 mb-6">
      <SettingInput
        label="Initial Capital"
        value={config.initialCapital}
        onChange={(value) => updateConfig('initialCapital', value)}
        suffix="$"
        step={100}
        min={0}
      />

      <SettingInput
        label="Risk Per Trade"
        value={config.riskPerTrade}
        onChange={(value) => updateConfig('riskPerTrade', value)}
        suffix="%"
        step={0.01}
        multiplier={100} 
        min={0}
        max={100}
      />

      <SettingInput
        label="Commission Rate"
        value={config.commissionRate}
        onChange={(value) => updateConfig('commissionRate', value)}
        suffix="%"
        step={0.001}
        multiplier={100} 
        min={0}
        max={10}
      />

      <div className="flex items-center">
        <input
          type="checkbox"
          id="allowShort"
          checked={config.allowShort}
          onChange={(e) => updateConfig('allowShort', e.target.checked)}
          className="mr-2 rounded"
        />
        <label htmlFor="allowShort" className="text-sm text-slate-300">
          Allow Short Selling
        </label>
      </div>
    </div>
  )
}
