export interface ExpandedSections {
  entry: boolean;
  exit: boolean;
  [key: string]: boolean; 
}

export interface FormErrors {
  file?: string;
  config?: string;
  general?: string;
}