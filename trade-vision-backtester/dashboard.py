import pandas as pd

def generate_dashboard_data(portfolio, metrics):
    equity_data = [
        {
            'date': entry['date'].strftime('%Y-%m-%d'),
            'value': entry['portfolio_value']
        }
        for entry in portfolio.equity_curve
    ]
    
    trade_list = []
    for i, trade in enumerate(portfolio.history):
        if trade['action'] == 'SELL':
            buy_trade = next(
                (t for t in portfolio.history[:i] if t['action'] == 'BUY' and t['symbol'] == trade['symbol']),
                None
            )
            
            if buy_trade:
                buy_date = buy_trade['date']
                sell_date = trade['date']
                duration = (sell_date - buy_date).days
                
                return_pct = trade['profit_loss'] / (buy_trade['qty'] * buy_trade['price']) * 100
                
                trade_list.append({
                    'buy_date': buy_date.strftime('%Y-%m-%d'),
                    'sell_date': sell_date.strftime('%Y-%m-%d'),
                    'buy_price': buy_trade['price'],
                    'sell_price': trade['price'],
                    'shares': trade['qty'],
                    'profit_loss': trade['profit_loss'],
                    'return_pct': return_pct,
                    'duration': duration
                })
    
    equity_df = pd.DataFrame([
        {'date': entry['date'], 'value': entry['portfolio_value']}
        for entry in portfolio.equity_curve
    ])
    equity_df['date'] = pd.to_datetime(equity_df['date'])
    equity_df = equity_df.set_index('date')
    
    equity_df['daily_return'] = equity_df['value'].pct_change()
    
    monthly_returns = equity_df['daily_return'].resample('M').apply(
        lambda x: ((1 + x).prod() - 1) * 100
    ).to_frame('monthly_return')
    
    monthly_returns_data = {}
    for date, value in monthly_returns.iterrows():
        year = date.year
        month = date.month
        
        if year not in monthly_returns_data:
            monthly_returns_data[year] = {}
            
        monthly_returns_data[year][month] = value['monthly_return']
    
    return {
        'equity_curve': equity_data,
        'metrics': metrics,
        'trade_list': trade_list,
        'monthly_returns': monthly_returns_data
    }