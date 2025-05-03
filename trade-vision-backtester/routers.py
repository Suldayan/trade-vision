from fastapi import FastAPI, APIRouter, UploadFile, File
import data_handler as DataHandler
from strategy import MovingAverageCrossover, RSIStrategy, MACDStrategy, BollingerBandStrategy, CompositeStrategy
from pydantic import BaseModel, Field
import portfolio as Portfolio

app = FastAPI()
router = APIRouter()
_data_handler = DataHandler()
_portfolio = Portfolio()

@router.post("/data")
async def upload_historical_data(file: UploadFile = File(...)):
    result = _data_handler.process_and_save_csv(file)

    return {
        "message": "Data uploaded successfully!",
        "data": result
    }

@router.post("/strategies")
async def configure_strategy(strategy_type, ):
    _portfolio.execute_trade()
    return 