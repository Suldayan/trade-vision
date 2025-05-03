# Trade-Vision

A comprehensive backtesting application for financial strategies, built with Java Spring Boot + Modulith for the backend and React + TypeScript + Tailwind CSS for the frontend.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Usage](#usage)
- [Creating Custom Strategies](#creating-custom-strategies)
  - [Understanding the Architecture](#understanding-the-architecture)
  - [Step 1: Create an Indicator](#step-1-create-an-indicator)
  - [Step 2: Implement a Condition](#step-2-implement-a-condition)
  - [Step 3: Register Your Strategy](#step-3-register-your-strategy)
  - [Example: Creating a Simple Moving Average Strategy](#example-creating-a-simple-moving-average-strategy)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Overview

Trade-Vision is a backtesting application that allows users to test trading strategies against historical market data to evaluate their performance before applying them to live markets. The modular architecture provides flexibility to implement various strategies and analyze their results through an intuitive user interface.

## Features

- Historical market data import and management
- Strategy definition and configuration
- Comprehensive backtesting engine
- Performance metrics and analytics
- Interactive visualization of results
- Export functionality for reports and data
- Customizable trading strategies

## Tech Stack

### Backend
- Java 22 (but 17+ should work just fine)
- Spring Boot 3.x
- Spring Modulith (for modular architecture)
- JUnit 5 & Mockito for testing
- Docker (for containerization)

### Frontend
- React 18.x
- TypeScript
- Tailwind CSS
- React Query for API data fetching
- Recharts/Lucide-React for data visualization
- Framer-Motion for animation

## Installation

### Prerequisites

- JDK 17 or higher
- Maven or Gradle (for backend build)
- Node.js and npm/yarn (for frontend build)
- Docker (optional, for containerization)

### Backend Setup

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/trade-vision.git
   cd trade-vision
   ```

2. Build the backend
   ```bash
   cd trade-vision-backend
   ./mvnw clean install
   ```

3. Run the backend
   ```bash
   ./mvnw spring-boot:run
   ```

The backend server will start on `http://localhost:8080`.

### Frontend Setup

1. Navigate to the frontend directory
   ```bash
   cd trade-vision-frontend
   ```

2. Install dependencies
   ```bash
   npm install
   ```

3. Start the development server
   ```bash
   npm run dev
   ```

The frontend application will be available at `http://localhost:5173`.

## Usage

1. After installation, navigate to `http://localhost:5173` in your browser.
2. Upload or select historical market data (must be a CSV file).
3. Define your trading strategy using the strategy builder.
4. Configure backtest parameters (time period, initial capital, etc.).
5. Run the backtest and view results in the dashboard.
6. Analyze performance metrics and visualizations.

## Creating Custom Strategies

### Understanding the Architecture

Trade-Vision uses a modular approach to building trading strategies:

- **Indicators**: Mathematical calculations derived from market data (e.g., SMA, RSI, MACD)
- **Conditions**: Rules that evaluate indicators to determine buy/sell signals
- **Strategies**: Collections of entry and exit conditions

### Step 1: Create an Indicator

Indicators are the foundation of any strategy. They perform calculations on market data to generate signals.

1. Navigate to the `indicators` module and locate the `IndicatorUtils.java` file
2. Create a new static method that implements your indicator logic
3. Your method should accept market data and parameters, then return the calculated values

Example:
```java
public static double[] calculateCustomIndicator(List<MarketDataPoint> data, int period) {
    double[] result = new double[data.size()];
    // Your calculation logic here
    return result;
}
```

### Step 2: Implement a Condition

Conditions use indicators to generate buy/sell signals based on specific criteria.

1. Navigate to `strategies.internal.conditions` package
2. Create a new class that implements the `Condition` interface
3. Implement the `evaluate` method to determine if your condition is met

Example structure:
```java
@RequiredArgsConstructor // Lombok Annotation
public class CustomCondition implements Condition {
    private final int period;
    private final double threshold;
    
    @Override
    public boolean evaluate(@Nonnull MarketData marketData, int index) {
        // Use your indicator and apply logic to determine if the condition is met
        // Return true if condition is met, false otherwise
        return result > threshold;
    }
}
```

### Step 3: Register Your Strategy

To make your strategy available in the backend application:

1. Open `StrategyServiceImpl.java` in the internal package of the strategy module
2. Add your condition type to the `createConditionFromConfig` switch statement
3. Create a helper method to instantiate your condition with the correct parameters

Example:
```java
// In the switch statement
case "YOUR_STRATEGY_NAME" -> createYourStrategy(config);

// Helper method
@Nonnull
private CustomCondition createYourStrategy(@Nonnull ConditionConfig config) {
    int period = getIntParam(config, "period");
    double threshold = getDoubleParam(config, "threshold");
    
    log.debug("Creating custom strategy with period={}, threshold={}", 
              period, threshold);
    
    return new CustomCondition(period, threshold);
}
```

### Example: Creating a Simple Moving Average Strategy

Here's a complete example of implementing a Simple Moving Average (SMA) strategy:

1. **Indicator (already in IndicatorUtils)**
```java
public static double[] sma(double[] prices, int window) {
        validateInputs(prices, window);

        double[] result = new double[prices.length];
        Arrays.fill(result, Double.NaN);

        for (int i = window - 1; i < prices.length; i++) {
            boolean hasNaN = false;
            double sum = 0;

            for (int j = 0; j < window; j++) {
                if (Double.isNaN(prices[i - j])) {
                    hasNaN = true;
                    break;
                }
                sum += prices[i - j];
            }

            if (!hasNaN) {
                result[i] = sum / window;
            }
        }

        return result;
    }
```

2. **Condition**
```java
@RequiredArgsConstructor
public class SMACrossoverCondition implements Condition {
    private final int fastPeriod;
    private final int slowPeriod;
    private final boolean crossAbove;

    @Override
    public boolean evaluate(@Nonnull MarketData data, int currentIndex) {
        if (currentIndex < 1) return false;

        double[] fastSMA = IndicatorUtils.sma(data.close(), fastPeriod);
        double[] slowSMA = IndicatorUtils.sma(data.close(), slowPeriod);

        // Check for valid data points
        if (Double.isNaN(fastSMA[currentIndex]) || Double.isNaN(slowSMA[currentIndex]) ||
                Double.isNaN(fastSMA[currentIndex-1]) || Double.isNaN(slowSMA[currentIndex-1])) {
            return false;
        }

        if (crossAbove) {
            return fastSMA[currentIndex-1] <= slowSMA[currentIndex-1] &&
                    fastSMA[currentIndex] > slowSMA[currentIndex];
        } else {
            return fastSMA[currentIndex-1] >= slowSMA[currentIndex-1] &&
                    fastSMA[currentIndex] < slowSMA[currentIndex];
        }
    }
}
```

3. **Register in StrategyServiceImpl**
```java
// In the switch statement
case "SMA_CROSSOVER" -> createSmaCrossover(config);

@Nonnull
private SMACrossoverCondition createSmaCrossover(@Nonnull ConditionConfig config) {
    int fastPeriod = getIntParam(config, "fastPeriod");
    int slowPeriod = getIntParam(config, "slowPeriod");
    boolean crossAbove = getBooleanParam(config, "crossAbove");

    log.debug("Creating SMA crossover condition with fastPeriod={}, slowPeriod={}, crossAbove={}",
            fastPeriod, slowPeriod, crossAbove);

    return new SMACrossoverCondition(fastPeriod, slowPeriod, crossAbove);
}
```
### Testing Your Strategy
Testing your strategy is crucial to ensure it works as expected. Here's how to create unit tests for your custom strategy:

***Testing Indicators***

Navigate to `src.test.indicators.IndicatorUtilsUnitTest` and create a test class:
```java
@Test
@DisplayName("Test custom indicator should be successful")
public void testCustomIndicator() {
    // Prepare test data
    double[] high =  {15, 16, 15, 16, 16, 15, 15, 16, 16, 15};
    double[] low =   {10, 10, 11, 11, 10, 10, 11, 10, 10, 11};
    double[] close = {12, 13, 12, 14, 13, 12, 13, 15, 14, 13};
    
    // Call your indicator
    double[] result = IndicatorUtils.calculateCustomIndicator(high, low, close);
    
    // Assert expected results
    assertEquals(Double.NaN, result[0], 0.0001);  // First few points should be NaN
    assertEquals(15.5, result[2], 0.0001);        // Check calculated values
}
```

***Testing Conditions***

Navigate to `src.test.strategies.StrategyServiceUnitTest` and create a test class:
```java
@Test
@DisplayName("Test custom indicator should be successful")
void createConditionFromConfig_shouldCreateCustomCondition() {
    Map<String, Object> customConditionParams = new HashMap<>();
    customConditionParams.put("period", 14);
    customConditionParams.put("upperThreshold", 30.0);
    customConditionParams.put("lowerThreshold", 15.0);
    customConditionParams.put("checkOverbought", true);

    ConditionConfig config = ConditionConfig.builder()
            .type("CUSTOM_CONDITION_CROSSOVER")
            .parameters(customConditionParams)
            .build();

    Condition condition = strategyService.createConditionFromConfig(config);

    assertNotNull(condition);
    assertInstanceOf(CustomCondition.class, condition);
}
```
## Project Structure

```
trade-vision/
├── trade-vision-backend/                # Spring Boot application
│   ├── src/main/java/      
│   │   └── com/yourusername/trade-vision-backend/
│   │       ├── backtester/     # Backtesting engine
│   │       ├── config/         # Configurations
│   │       ├── domain/         # Shared domain models
│   │       ├── indicators/     # Technical indicators
│   │       ├── market/         # Market data handling
│   │       ├── strategies/     # Strategy definitions
│   │       └── ...
│   └── src/test/              # Backend tests
├── trade-vision-frontend/     # React application
│   ├── public/                # Static files
│   ├── src/                
│   │   ├── components/        # React components
│   │   ├── pages/             # Page components
│   │   ├── services/          # API services
│   │   └── ...
│   └── package.json           # Frontend dependencies
├── docker/                    # Docker configuration
├── docs/                      # Documentation
└── README.md                  # This file
```

## API Documentation

API documentation is available at `http://localhost:8080/swagger-ui.html` when the backend is running.

## Testing

### Backend Testing

```bash
cd trade-vision-backend
./mvnw test
```

## Contributing

This is my first open-source project and really project in general, and while I'm happy with its current state, I welcome contributions! If you have ideas for improvements or new features, feel free to:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the [MIT License](LICENSE).
