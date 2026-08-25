import React, { useState } from 'react';
import { CameraScanner } from './CameraScanner';
import { UserSession, ScanLog } from '../types';
import {
  Barcode,
  Camera,
  Keyboard,
  History,
  Settings,
  LogOut,
  AlertOctagon,
  Sparkles,
  ArrowRight
} from 'lucide-react';

interface ScannerScreenProps {
  session: UserSession;
  onSearchEan: (ean: string) => void;
  isSearching: boolean;
  searchError: string | null;
  scanHistory: ScanLog[];
  onOpenHistory: () => void;
  onOpenSettings: () => void;
  onLogout: () => void;
}

export const ScannerScreen: React.FC<ScannerScreenProps> = ({
  session,
  onSearchEan,
  isSearching,
  searchError,
  scanHistory,
  onOpenHistory,
  onOpenSettings,
  onLogout,
}) => {
  const [manualEan, setManualEan] = useState('');
  const [cameraActive, setCameraActive] = useState(false);
  const [validationMsg, setValidationMsg] = useState<string | null>(null);

  const handleManualSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setValidationMsg(null);

    const clean = manualEan.trim().replace(/[\s-]/g, '');
    if (!clean) {
      setValidationMsg('Please enter an EAN barcode number');
      return;
    }
    if (!/^\d+$/.test(clean)) {
      setValidationMsg('EAN barcode must contain numbers only');
      return;
    }

    onSearchEan(clean);
  };

  const handleQuickTestEan = (ean: string) => {
    setManualEan(ean);
    onSearchEan(ean);
  };

  // Stats calculation
  const totalScans = scanHistory.length;
  const verifiedScans = scanHistory.filter((s) => s.status === 'FOUND').length;
  const missingScans = scanHistory.filter((s) => s.status === 'NOT_FOUND' || s.status === 'ERROR').length;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between">
      {/* Top Navigation Bar */}
      <header className="bg-slate-900/90 backdrop-blur-md border-b border-slate-800 sticky top-0 z-20 px-4 py-3">
        <div className="max-w-6xl w-full mx-auto flex items-center justify-between">
          {/* Brand Identity */}
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center shadow-md shadow-blue-500/20">
              <Barcode className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="text-sm font-bold text-white tracking-tight flex items-center space-x-1.5">
                <span>Product Verification Scanner</span>
                <span className="hidden sm:inline-block px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-300 text-[10px] font-mono font-bold uppercase">
                  Live API
                </span>
              </h1>
              <p className="text-[11px] text-slate-400">
                Operator: <span className="text-slate-200 font-semibold">{session.name}</span> • {session.department}
              </p>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center space-x-2">
            <button
              onClick={onOpenHistory}
              className="p-2 sm:px-3 sm:py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition flex items-center space-x-1.5"
              title="View Scan Logs"
            >
              <History className="w-4 h-4 text-blue-400" />
              <span className="hidden sm:inline">Logs ({totalScans})</span>
            </button>

            <button
              onClick={onOpenSettings}
              className="p-2 sm:px-3 sm:py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition flex items-center space-x-1.5"
              title="API Settings"
            >
              <Settings className="w-4 h-4 text-slate-400" />
              <span className="hidden sm:inline">Config</span>
            </button>

            <button
              onClick={onLogout}
              className="p-2 rounded-xl bg-slate-800 hover:bg-red-500/20 hover:text-red-400 text-slate-400 border border-slate-700 transition"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="max-w-4xl w-full mx-auto px-4 py-6 flex-1 flex flex-col justify-center">
        {/* Hardware Scanner Status & Live Banner */}
        <div className="bg-gradient-to-r from-blue-950/70 via-slate-900 to-indigo-950/70 border border-blue-500/30 rounded-3xl p-5 md:p-6 mb-6 shadow-xl relative overflow-hidden">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center space-x-3.5">
              <div className="relative">
                <div className="w-3.5 h-3.5 rounded-full bg-emerald-500 animate-ping absolute inset-0" />
                <div className="w-3.5 h-3.5 rounded-full bg-emerald-500 relative shadow-lg shadow-emerald-500/50" />
              </div>
              <div>
                <h2 className="text-base md:text-lg font-black text-white flex items-center space-x-2">
                  <span>Hardware Scanner Ready</span>
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-mono font-bold border border-emerald-500/30">
                    USB / Bluetooth
                  </span>
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Point handheld barcode scanner at product box. Scans are detected automatically.
                </p>
              </div>
            </div>

            {/* Camera Scanner Toggle */}
            <button
              onClick={() => setCameraActive(!cameraActive)}
              className={`px-4 py-2.5 rounded-2xl text-xs font-bold transition flex items-center justify-center space-x-2 border shadow-lg ${
                cameraActive
                  ? 'bg-blue-600 text-white border-blue-400 shadow-blue-600/30'
                  : 'bg-slate-800 text-slate-200 border-slate-700 hover:bg-slate-700'
              }`}
            >
              <Camera className="w-4 h-4" />
              <span>{cameraActive ? 'Close Camera' : 'Use Device Camera'}</span>
            </button>
          </div>
        </div>

        {/* Camera Viewfinder (if active) */}
        {cameraActive && (
          <div className="mb-6">
            <CameraScanner
              isActive={cameraActive}
              onScan={(scanned) => {
                onSearchEan(scanned);
              }}
            />
          </div>
        )}

        {/* Manual EAN Entry Box */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 md:p-8 shadow-2xl mb-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2 text-slate-300 font-bold text-sm uppercase tracking-wider">
              <Keyboard className="w-4 h-4 text-blue-400" />
              <span>Enter Barcode (EAN-13 / UPC / SKU)</span>
            </div>
            <span className="text-xs text-slate-400 font-mono">
              Press [Enter] or click Verify
            </span>
          </div>

          <form onSubmit={handleManualSubmit} className="space-y-4">
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-slate-400">
                <Barcode className="w-6 h-6 text-blue-400" />
              </div>
              <input
                type="text"
                value={manualEan}
                onChange={(e) => {
                  setManualEan(e.target.value);
                  setValidationMsg(null);
                }}
                placeholder="Scan or type 13-digit EAN (e.g. 8906202671265)"
                className="w-full pl-14 pr-32 py-4 bg-slate-950/90 border-2 border-slate-700 hover:border-slate-600 focus:border-blue-500 rounded-2xl text-white placeholder-slate-500 font-mono text-base md:text-lg tracking-wider focus:outline-none focus:ring-4 focus:ring-blue-500/20 transition shadow-inner"
                autoFocus
                disabled={isSearching}
              />
              <button
                type="submit"
                disabled={isSearching || !manualEan.trim()}
                className="absolute right-2 top-2 bottom-2 px-5 bg-blue-600 hover:bg-blue-500 active:scale-95 text-white font-extrabold text-xs md:text-sm rounded-xl transition shadow-md shadow-blue-600/30 flex items-center space-x-1.5 disabled:opacity-40 disabled:cursor-not-allowed"
              >
                {isSearching ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    <span className="hidden sm:inline">Checking...</span>
                  </>
                ) : (
                  <>
                    <span>Verify</span>
                    <ArrowRight className="w-4 h-4" />
                  </>
                )}
              </button>
            </div>

            {/* Error or validation message */}
            {(validationMsg || searchError) && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs font-medium flex items-center space-x-2 animate-pulse">
                <AlertOctagon className="w-4 h-4 shrink-0" />
                <span>{validationMsg || searchError}</span>
              </div>
            )}
          </form>

          {/* Quick Demo Test Barcodes */}
          <div className="mt-6 pt-5 border-t border-slate-800">
            <div className="flex items-center space-x-2 text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">
              <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
              <span>Quick Test Verification Barcodes</span>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <button
                type="button"
                onClick={() => handleQuickTestEan('8906202671265')}
                disabled={isSearching}
                className="p-3 rounded-xl bg-slate-950/80 hover:bg-blue-600/10 border border-slate-800 hover:border-blue-500/40 text-left transition flex items-center justify-between group"
              >
                <div>
                  <div className="text-xs font-bold text-slate-200 group-hover:text-blue-300 transition">
                    Nova2Ultra (Black, 6+128)
                  </div>
                  <div className="text-[11px] font-mono text-slate-400">
                    EAN: 8906202671265 • ₹29,999
                  </div>
                </div>
                <span className="text-[10px] font-mono font-bold px-2 py-1 rounded bg-slate-900 group-hover:bg-blue-500 text-slate-300 group-hover:text-white transition">
                  Test Scan
                </span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickTestEan('8906202671500')}
                disabled={isSearching}
                className="p-3 rounded-xl bg-slate-950/80 hover:bg-blue-600/10 border border-slate-800 hover:border-blue-500/40 text-left transition flex items-center justify-between group"
              >
                <div>
                  <div className="text-xs font-bold text-slate-200 group-hover:text-blue-300 transition">
                    Evo (4G) (Blue, 4+64)
                  </div>
                  <div className="text-[11px] font-mono text-slate-400">
                    EAN: 8906202671500 • ₹14,999
                  </div>
                </div>
                <span className="text-[10px] font-mono font-bold px-2 py-1 rounded bg-slate-900 group-hover:bg-blue-500 text-slate-300 group-hover:text-white transition">
                  Test Scan
                </span>
              </button>
            </div>
          </div>
        </div>

        {/* Quick Shift Stats Cards */}
        <div className="grid grid-cols-3 gap-3">
          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 text-center">
            <span className="text-xs text-slate-400 font-semibold block mb-1">Total Scans</span>
            <span className="text-xl md:text-2xl font-black text-white font-mono">{totalScans}</span>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 text-center">
            <span className="text-xs text-emerald-400 font-semibold block mb-1">Verified OK</span>
            <span className="text-xl md:text-2xl font-black text-emerald-400 font-mono">{verifiedScans}</span>
          </div>

          <div className="p-4 rounded-2xl bg-slate-900/80 border border-slate-800 text-center">
            <span className="text-xs text-red-400 font-semibold block mb-1">QC Flags</span>
            <span className="text-xl md:text-2xl font-black text-red-400 font-mono">{missingScans}</span>
          </div>
        </div>
      </main>

      {/* Footer info */}
      <footer className="py-3 px-4 text-center text-xs text-slate-500 border-t border-slate-800/80 bg-slate-950">
        <p>Google Apps Script Web App API Gateway • Single Source of Truth • Universal Web Deployment</p>
      </footer>
    </div>
  );
};
