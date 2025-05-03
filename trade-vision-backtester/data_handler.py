import pandas as pd
import os
from fastapi import HTTPException

HISTORICAL_DATA_PATH = "data/historical_data.csv"

class DataHandler:
    def __init__(self, upload_dir: str = "historical_data"):
        self.upload_dir = upload_dir
        os.makedirs(upload_dir, exist_ok=True)

    def process_and_save_csv(self, file) -> dict:
        try:
            df = pd.read_csv(file.file)
            file_path = os.path.join(self.upload_dir, f"historical_{file.filename}")
            df.to_csv(file_path, index=False)
            
            return {
                "saved_path": file_path,
                "filename": file.filename
            }
        except pd.errors.EmptyDataError:
            raise HTTPException(status_code=400, detail="Empty CSV file")
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Failed to process file: {str(e)}")
        finally:
            file.file.close()