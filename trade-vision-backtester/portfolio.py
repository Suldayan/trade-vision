class Portfolio:
    def __init__(self, initial_capital=100000):
        self.initial_capital = initial_capital
        self.cash = initial_capital
        self.positions = {}  # Dictionary to track current positions
        self.history = []    # List to store trade history
        self.equity_curve = []  # Track portfolio value over time
    
    def execute_trade(self, symbol, signal, price, date, qty=None, percent_risk=0.02):
        """Execute a trade based on the signal.
        
        Args:
            symbol: The asset symbol
            signal: 1 for buy, -1 for sell, 0 for no action
            price: Current price of the asset
            date: Date of the trade
            qty: Optional specific quantity to trade
            percent_risk: Percentage of portfolio to risk per trade
        """
        if signal == 0:
            # No trade
            return
        
        if signal == 1:  # Buy signal
            if symbol in self.positions:
                # Already have a position, do nothing
                return
            
            # Calculate position size based on percent risk
            if qty is None:
                position_size = (self.cash * percent_risk) / price
                # Round down to nearest whole share
                position_size = int(position_size)
            else:
                position_size = qty
            
            # Check if we have enough cash
            cost = position_size * price
            if cost > self.cash:
                position_size = int(self.cash / price)  # Adjust for available cash
                cost = position_size * price
            
            if position_size <= 0:
                return  # Not enough cash for even 1 share
            
            # Execute buy
            self.positions[symbol] = {'qty': position_size, 'price': price}
            self.cash -= cost
            
            # Record trade
            self.history.append({
                'date': date,
                'symbol': symbol,
                'action': 'BUY',
                'price': price,
                'qty': position_size,
                'cost': cost,
                'cash_remaining': self.cash
            })
            
        elif signal == -1:  # Sell signal
            if symbol not in self.positions:
                # No position to sell
                return
            
            # Get position details
            position = self.positions[symbol]
            qty_to_sell = position['qty']
            revenue = qty_to_sell * price
            profit_loss = revenue - (qty_to_sell * position['price'])
            
            # Execute sell
            self.cash += revenue
            del self.positions[symbol]
            
            # Record trade
            self.history.append({
                'date': date,
                'symbol': symbol,
                'action': 'SELL',
                'price': price,
                'qty': qty_to_sell,
                'revenue': revenue,
                'profit_loss': profit_loss,
                'cash_remaining': self.cash
            })
    
    def update_portfolio_value(self, current_prices, date):
        """Update the portfolio value based on current prices."""
        portfolio_value = self.cash
        
        # Add value of all open positions
        for symbol, position in self.positions.items():
            if symbol in current_prices:
                portfolio_value += position['qty'] * current_prices[symbol]
        
        # Record in equity curve
        self.equity_curve.append({
            'date': date,
            'portfolio_value': portfolio_value
        })
        
        return portfolio_value