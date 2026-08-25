import React from 'react';
import { AlertOctagon, ScanLine, Keyboard, ShieldAlert } from 'lucide-react';

interface ProductNotFoundScreenProps {
  ean: string;
  onScanNext: () => void;
  onManualEntry: () => void;
}

export const ProductNotFoundScreen: React.FC<ProductNotFoundScreenProps> = ({
  ean,
  onScanNext,
  onManualEntry,
}) => {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between p-4 md:p-6 lg:p-8">
      <div className="max-w-xl w-full mx-auto my-auto space-y-6">
        {/* Warning Card */}
        <div className="bg-slate-900/90 border-2 border-red-500/50 rounded-3xl p-6 md:p-8 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-48 h-48 bg-red-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="flex items-center space-x-4 mb-6">
            <div className="w-16 h-16 rounded-2xl bg-red-500/20 border border-red-500/40 flex items-center justify-center text-red-400 shrink-0 shadow-lg shadow-red-500/20">
              <AlertOctagon className="w-10 h-10" />
            </div>
            <div>
              <span className="px-3 py-0.5 rounded-full bg-red-500/20 text-red-300 font-mono font-bold text-xs uppercase tracking-wider border border-red-500/30">
                QC FLAG: UNREGISTERED EAN
              </span>
              <h1 className="text-2xl md:text-3xl font-black text-white mt-1">
                Product Not Found
              </h1>
            </div>
          </div>

          {/* Scanned Barcode Container */}
          <div className="p-4 rounded-2xl bg-slate-950/80 border border-red-500/30 mb-6">
            <span className="text-xs text-slate-400 font-semibold uppercase block mb-1">
              Scanned Barcode
            </span>
            <p className="text-xl md:text-2xl font-black text-red-400 font-mono tracking-widest">
              {ean || 'Unknown EAN'}
            </p>
          </div>

          <div className="space-y-2 text-sm text-slate-300 mb-8">
            <p className="flex items-start space-x-2">
              <ShieldAlert className="w-4 h-4 text-red-400 mt-0.5 shrink-0" />
              <span>This EAN barcode is not listed in the Google Apps Script Master Database.</span>
            </p>
            <p className="text-xs text-slate-400 pl-6">
              Do not dispatch this unit. Verify barcode label print quality, re-scan, or escalate to the inventory supervisor.
            </p>
          </div>

          {/* Action buttons */}
          <div className="space-y-3">
            <button
              onClick={onScanNext}
              className="w-full py-4 px-6 bg-red-600 hover:bg-red-500 active:scale-[0.99] text-white font-extrabold text-base rounded-2xl shadow-xl shadow-red-600/30 flex items-center justify-center space-x-2 transition"
            >
              <ScanLine className="w-5 h-5" />
              <span>Scan Another Product</span>
            </button>

            <button
              onClick={onManualEntry}
              className="w-full py-3.5 px-6 bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-sm rounded-2xl border border-slate-700 flex items-center justify-center space-x-2 transition"
            >
              <Keyboard className="w-4 h-4" />
              <span>Enter EAN Manually</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
