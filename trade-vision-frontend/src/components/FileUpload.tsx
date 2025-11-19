import React, { useCallback, useState, useRef } from 'react';
import { Upload, X, FileText, AlertCircle } from 'lucide-react';

interface FileUploadProps {
  onFileSelect?: (file: File | null) => void;
  acceptedTypes?: string[];
  acceptedExtensions?: string[];
  maxFileSize?: number;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  showFileSize?: boolean;
  allowClear?: boolean;
}

export default function FileUpload({
  onFileSelect,
  acceptedTypes = ['text/csv'],
  acceptedExtensions = ['.csv'],
  maxFileSize = 100 * 1024 * 1024,
  label = 'Upload File',
  placeholder = 'Choose file',
  disabled = false,
  className = '',
  showFileSize = true,
  allowClear = true,
}: FileUploadProps) {
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string>('');
  const [isDragOver, setIsDragOver] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const validateFile = (selectedFile: File): string | null => {
    const isValidType = acceptedTypes.some(type => selectedFile.type === type);
    const isValidExtension = acceptedExtensions.some(ext => 
      selectedFile.name.toLowerCase().endsWith(ext.toLowerCase())
    );

    if (!isValidType && !isValidExtension) {
      return `Please select a valid file. Accepted types: ${acceptedExtensions.join(', ')}`;
    }

    if (selectedFile.size > maxFileSize) {
      return `File size must be less than ${formatFileSize(maxFileSize)}`;
    }

    return null;
  };

  const handleFileChange = useCallback((selectedFile: File | null) => {
    if (!selectedFile) {
      setFile(null);
      setError('');
      onFileSelect?.(null);
      return;
    }

    const validationError = validateFile(selectedFile);
    if (validationError) {
      setError(validationError);
      setFile(null);
      onFileSelect?.(null);
      return;
    }

    setFile(selectedFile);
    setError('');
    onFileSelect?.(selectedFile);
  }, [acceptedTypes, acceptedExtensions, maxFileSize, onFileSelect]);

  const handleInputChange = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0] || null;
    handleFileChange(selectedFile);
  }, [handleFileChange]);

  const handleDragOver = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    if (!disabled) {
      setIsDragOver(true);
    }
  }, [disabled]);

  const handleDragLeave = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragOver(false);
  }, []);

  const handleDrop = useCallback((event: React.DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragOver(false);
    
    if (disabled) return;

    const droppedFile = event.dataTransfer.files?.[0] || null;
    handleFileChange(droppedFile);
  }, [disabled, handleFileChange]);

  const clearFile = useCallback(() => {
    setFile(null);
    setError('');
    onFileSelect?.(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }, [onFileSelect]);

  const handleLabelClick = useCallback(() => {
    if (!disabled && fileInputRef.current) {
      fileInputRef.current.click();
    }
  }, [disabled]);

  return (
    <div className={`mb-6 ${className}`}>
      <label className="block text-sm font-medium text-slate-300 mb-3">
        {label}
      </label>
      
      <div className="relative">
        <input
          ref={fileInputRef}
          type="file"
          accept={acceptedExtensions.join(',')}
          onChange={handleInputChange}
          className="hidden"
          disabled={disabled}
        />
        
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={handleLabelClick}
          className={`
            flex items-center justify-center w-full px-4 py-4 border-2 border-dashed rounded-lg cursor-pointer transition-all duration-200
            ${isDragOver && !disabled
              ? 'border-purple-300 bg-purple-500/30'
              : 'border-purple-400 bg-purple-500/10 hover:border-purple-300 hover:bg-purple-500/20'
            }
            ${disabled 
              ? 'opacity-50 cursor-not-allowed border-gray-500 bg-gray-500/10' 
              : ''
            }
            ${error 
              ? 'border-red-400 bg-red-500/10' 
              : ''
            }
          `}
        >
          {file ? (
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center">
                <FileText className="h-5 w-5 text-purple-400 mr-2" />
                <div className="flex flex-col">
                  <span className="text-purple-300 text-sm font-medium">
                    {file.name}
                  </span>
                  {showFileSize && (
                    <span className="text-slate-400 text-xs">
                      {formatFileSize(file.size)}
                    </span>
                  )}
                </div>
              </div>
              
              {allowClear && !disabled && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    clearFile();
                  }}
                  className="ml-2 p-1 rounded-full hover:bg-purple-500/20 transition-colors"
                  title="Clear file"
                >
                  <X className="h-4 w-4 text-purple-400" />
                </button>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center">
              <Upload className="h-5 w-5 text-purple-400 mb-2" />
              <span className="text-purple-300 text-sm font-medium">
                {isDragOver ? 'Drop file here' : placeholder}
              </span>
              <span className="text-slate-400 text-xs mt-1">
                {acceptedExtensions.join(', ')} • Max {formatFileSize(maxFileSize)}
              </span>
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="mt-3 p-3 bg-red-500/20 border border-red-500/50 rounded-lg flex items-center">
          <AlertCircle className="h-4 w-4 text-red-400 mr-2 flex-shrink-0" />
          <span className="text-red-300 text-sm">{error}</span>
        </div>
      )}
    </div>
  );
}