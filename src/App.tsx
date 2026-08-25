import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Product, UserSession, ScanLog } from './types';
import { getProductByEAN, DEFAULT_API_ENDPOINT } from './services/productService';
import { soundManager } from './utils/audio';
import { useBarcodeScanner } from './utils/useBarcodeScanner';
import { LoginScreen } from './components/LoginScreen';
import { ScannerScreen } from './components/ScannerScreen';
import { ProductDetailsScreen } from './components/ProductDetailsScreen';
import { ProductNotFoundScreen } from './components/ProductNotFoundScreen';
import { DuplicateEanScreen } from './components/DuplicateEanScreen';
import { HistoryModal } from './components/HistoryModal';
import { SettingsModal } from './components/SettingsModal';

type AppView = 'login' | 'scanner' | 'product_details' | 'product_not_found' | 'duplicate_warning';

export const App: React.FC = () => {
  // Session State
  const [session, setSession] = useState<UserSession | null>(() => {
    try {
      const saved = localStorage.getItem('qc_operator_session');
      if (saved) return JSON.parse(saved);
    } catch {
      // Ignore
    }
    return null;
  });

  // Current View
  const [currentView, setCurrentView] = useState<AppView>(() => (session ? 'scanner' : 'login'));

  // Active Product State (Always fresh object, cleared prior to new scan)
  const [currentProduct, setCurrentProduct] = useState<Product | null>(null);
  const [currentEan, setCurrentEan] = useState<string>('');
  const [duplicateProducts, setDuplicateProducts] = useState<Product[]>([]);

  // Search execution status
  const [isSearching, setIsSearching] = useState<boolean>(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  // Settings & Scan History
  const [apiEndpoint, setApiEndpoint] = useState<string>(() => {
    try {
      return localStorage.getItem('qc_api_endpoint') || DEFAULT_API_ENDPOINT;
    } catch {
      return DEFAULT_API_ENDPOINT;
    }
  });

  const [scanHistory, setScanHistory] = useState<ScanLog[]>(() => {
    try {
      const saved = localStorage.getItem('qc_scan_logs');
      if (saved) return JSON.parse(saved);
    } catch {
      // Ignore
    }
    return [];
  });

  const [historyOpen, setHistoryOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);

  // In-flight query cancellation & race-condition prevention
  const abortControllerRef = useRef<AbortController | null>(null);
  const currentRequestIdRef = useRef<number>(0);

  // Save session changes
  const handleLoginSuccess = (newSession: UserSession) => {
    setSession(newSession);
    try {
      localStorage.setItem('qc_operator_session', JSON.stringify(newSession));
    } catch {
      // Ignore
    }
    setCurrentView('scanner');
  };

  const handleLogout = () => {
    setSession(null);
    try {
      localStorage.removeItem('qc_operator_session');
    } catch {
      // Ignore
    }
    setCurrentView('login');
  };

  // Perform Real-Time EAN Verification
  const performSearch = useCallback(
    async (rawEan: string) => {
      const trimmedEan = (rawEan || '').trim();
      if (!trimmedEan) return;

      // 1. Cancel previous in-flight request
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      const controller = new AbortController();
      abortControllerRef.current = controller;

      // 2. Increment request tracker
      const requestId = ++currentRequestIdRef.current;

      // 3. Clear previous product state immediately to prevent stale SAR or specs
      setCurrentProduct(null);
      setDuplicateProducts([]);
      setCurrentEan(trimmedEan);
      setSearchError(null);
      setIsSearching(true);
      setCurrentView('scanner');

      try {
        const result = await getProductByEAN(trimmedEan, apiEndpoint, controller.signal);

        // Discard if a newer search has already started
        if (requestId !== currentRequestIdRef.current) {
          return;
        }

        setIsSearching(false);

        if (result.error) {
          soundManager.playError();
          setSearchError(result.error);
          return;
        }

        if (result.found && !result.duplicate && result.products.length > 0) {
          // Single match: brand new product object
          const freshProduct = result.products[0];
          setCurrentProduct(freshProduct);
          soundManager.playSuccess();

          // Log verification
          const newLog: ScanLog = {
            id: `log-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
            timestamp: Date.now(),
            ean: freshProduct.ean,
            status: 'FOUND',
            model: freshProduct.model,
            sku: freshProduct.sku,
            color: freshProduct.color,
            memory: freshProduct.memory,
            price: freshProduct.price,
            sarValue: freshProduct.sarValue,
          };
          setScanHistory((prev) => {
            const updated = [newLog, ...prev.slice(0, 99)];
            try {
              localStorage.setItem('qc_scan_logs', JSON.stringify(updated));
            } catch {
              // Ignore
            }
            return updated;
          });

          setCurrentView('product_details');
        } else if (result.found && result.duplicate && result.products.length > 0) {
          // Duplicate barcode conflict
          setDuplicateProducts(result.products);
          soundManager.playWarning();

          const newLog: ScanLog = {
            id: `log-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
            timestamp: Date.now(),
            ean: trimmedEan,
            status: 'DUPLICATE',
          };
          setScanHistory((prev) => {
            const updated = [newLog, ...prev.slice(0, 99)];
            try {
              localStorage.setItem('qc_scan_logs', JSON.stringify(updated));
            } catch {
              // Ignore
            }
            return updated;
          });

          setCurrentView('duplicate_warning');
        } else {
          // Not found in database
          soundManager.playWarning();

          const newLog: ScanLog = {
            id: `log-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`,
            timestamp: Date.now(),
            ean: trimmedEan,
            status: 'NOT_FOUND',
          };
          setScanHistory((prev) => {
            const updated = [newLog, ...prev.slice(0, 99)];
            try {
              localStorage.setItem('qc_scan_logs', JSON.stringify(updated));
            } catch {
              // Ignore
            }
            return updated;
          });

          setCurrentView('product_not_found');
        }
      } catch (err: unknown) {
        if (err instanceof DOMException && err.name === 'AbortError') {
          // Intentionally superseded by newer scan
          return;
        }
        if (requestId === currentRequestIdRef.current) {
          setIsSearching(false);
          soundManager.playError();
          setSearchError('Network error connecting to Apps Script API. Please retry.');
        }
      }
    },
    [apiEndpoint]
  );

  // Listen for USB & Bluetooth barcode scanner input across all screens
  useBarcodeScanner({
    onScan: performSearch,
    enabled: Boolean(session),
  });

  // Spacebar / Escape hotkeys for fast warehouse workflow
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // If pressing Space outside of an input on Details or Not Found screen, trigger Scan Next
      const target = e.target as HTMLElement | null;
      const isInput = target?.tagName === 'INPUT' || target?.tagName === 'TEXTAREA';

      if (!isInput && e.code === 'Space') {
        if (currentView === 'product_details' || currentView === 'product_not_found' || currentView === 'duplicate_warning') {
          e.preventDefault();
          setCurrentProduct(null);
          setCurrentView('scanner');
        }
      } else if (e.key === 'Escape') {
        if (historyOpen) setHistoryOpen(false);
        if (settingsOpen) setSettingsOpen(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [currentView, historyOpen, settingsOpen]);

  const handleScanNext = () => {
    setCurrentProduct(null);
    setSearchError(null);
    setCurrentView('scanner');
  };

  const handleSelectDuplicateVariant = (selected: Product) => {
    setCurrentProduct(selected);
    setCurrentView('product_details');
  };

  const handleClearLogs = () => {
    setScanHistory([]);
    try {
      localStorage.removeItem('qc_scan_logs');
    } catch {
      // Ignore
    }
  };

  const handleSaveEndpoint = (newUrl: string) => {
    setApiEndpoint(newUrl);
    try {
      localStorage.setItem('qc_api_endpoint', newUrl);
    } catch {
      // Ignore
    }
  };

  // Render view
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans">
      {!session || currentView === 'login' ? (
        <LoginScreen onLoginSuccess={handleLoginSuccess} />
      ) : currentView === 'product_details' && currentProduct ? (
        <ProductDetailsScreen
          product={currentProduct}
          onScanNext={handleScanNext}
          operatorName={session.name}
          terminalId={session.terminalId}
        />
      ) : currentView === 'product_not_found' ? (
        <ProductNotFoundScreen
          ean={currentEan}
          onScanNext={handleScanNext}
          onManualEntry={handleScanNext}
        />
      ) : currentView === 'duplicate_warning' ? (
        <DuplicateEanScreen
          ean={currentEan}
          products={duplicateProducts}
          onSelectProduct={handleSelectDuplicateVariant}
          onScanNext={handleScanNext}
        />
      ) : (
        <ScannerScreen
          session={session}
          onSearchEan={performSearch}
          isSearching={isSearching}
          searchError={searchError}
          scanHistory={scanHistory}
          onOpenHistory={() => setHistoryOpen(true)}
          onOpenSettings={() => setSettingsOpen(true)}
          onLogout={handleLogout}
        />
      )}

      {/* History Modal */}
      <HistoryModal
        isOpen={historyOpen}
        onClose={() => setHistoryOpen(false)}
        logs={scanHistory}
        onClearLogs={handleClearLogs}
      />

      {/* Settings Modal */}
      <SettingsModal
        isOpen={settingsOpen}
        onClose={() => setSettingsOpen(false)}
        apiEndpoint={apiEndpoint}
        onSaveEndpoint={handleSaveEndpoint}
      />
    </div>
  );
};

export default App;
