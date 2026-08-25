export interface Product {
  id: string | number;
  model: string;
  color: string;
  memory: string;
  ean: string;
  sku: string;
  price: string | number;
  sarValue: string;
}

export interface ApiResponse {
  success: boolean;
  found: boolean;
  duplicate: boolean;
  count: number;
  message?: string;
  products: Product[];
}

export type ScanStatus = 'FOUND' | 'NOT_FOUND' | 'DUPLICATE' | 'ERROR';

export interface ScanLog {
  id: string;
  timestamp: number;
  ean: string;
  status: ScanStatus;
  model?: string;
  sku?: string;
  color?: string;
  memory?: string;
  price?: string | number;
  sarValue?: string;
}

export interface UserSession {
  isLoggedIn: boolean;
  userId: string;
  name: string;
  role: string;
  department: string;
  terminalId: string;
  loginTime: number;
}

export interface SheetConfig {
  apiEndpoint: string;
  lastSyncTime: number;
  lastSyncStatus: string;
  totalProductsVerified: number;
}
