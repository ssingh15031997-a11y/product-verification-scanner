import React from 'react';
import { Product } from '../types';
import { AlertTriangle, ScanLine, ArrowRight } from 'lucide-react';

interface DuplicateEanScreenProps {
  ean: string;
  products: Product[];
  onSelectProduct: (product: Product) => void;
  onScanNext: () => void;
}

export const DuplicateEanScreen: React.FC<DuplicateEanScreenProps> = ({
  ean,
  products,
  onSelectProduct,
  onScanNext,
}) => {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between p-4 md:p-6 lg:p-8">
      <div className="max-w-3xl w-full mx-auto space-y-6">
        {/* Warning Banner */}
        <div className="bg-slate-900/90 border-2 border-amber-500/50 rounded-3xl p-6 md:p-8 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-48 h-48 bg-amber-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="flex items-center space-x-4 mb-4">
            <div className="w-14 h-14 rounded-2xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400 shrink-0 shadow-lg shadow-amber-500/20">
              <AlertTriangle className="w-8 h-8" />
            </div>
            <div>
              <span className="px-3 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-mono font-bold text-xs uppercase tracking-wider border border-amber-500/30">
                QC WARNING: MULTIPLE MATCHES ({products.length})
              </span>
              <h1 className="text-2xl md:text-3xl font-black text-white mt-1">
                Duplicate EAN Detected
              </h1>
            </div>
          </div>

          <p className="text-sm text-slate-300 mb-2">
            The scanned barcode <span className="font-mono font-bold text-amber-400">{ean}</span> is assigned to multiple product variants in the database.
          </p>
          <p className="text-xs text-slate-400 mb-6">
            Please cross-verify the box SKU and model name below to select the verified physical unit.
          </p>

          {/* Product list variants */}
          <div className="space-y-3 mb-6">
            {products.map((p, idx) => (
              <div
                key={p.id || idx}
                onClick={() => onSelectProduct(p)}
                className="p-4 md:p-5 rounded-2xl bg-slate-950/80 border border-slate-800 hover:border-amber-500/60 transition cursor-pointer flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 group"
              >
                <div className="space-y-1">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-mono font-semibold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                      Option #{idx + 1}
                    </span>
                    <span className="text-xs font-mono text-cyan-400">SKU: {p.sku || 'N/A'}</span>
                  </div>
                  <h3 className="text-lg font-bold text-white group-hover:text-amber-300 transition">
                    {p.model}
                  </h3>
                  <div className="flex items-center space-x-3 text-xs text-slate-400">
                    <span>Color: <strong className="text-slate-200">{p.color}</strong></span>
                    <span>•</span>
                    <span>Memory: <strong className="text-slate-200">{p.memory}</strong></span>
                    {p.price && (
                      <>
                        <span>•</span>
                        <span>Price: <strong className="text-slate-200">₹{typeof p.price === 'number' ? p.price.toLocaleString('en-IN') : p.price}</strong></span>
                      </>
                    )}
                    {p.cartonMrp && (
                      <>
                        <span>•</span>
                        <span>Carton MRP: <strong className="text-slate-200">₹{typeof p.cartonMrp === 'number' ? p.cartonMrp.toLocaleString('en-IN') : p.cartonMrp}</strong></span>
                      </>
                    )}
                  </div>
                </div>

                <button
                  type="button"
                  className="px-4 py-2 rounded-xl bg-amber-500 group-hover:bg-amber-400 text-slate-950 font-bold text-xs flex items-center space-x-1.5 transition shrink-0"
                >
                  <span>Select Variant</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>

          <div className="pt-2">
            <button
              onClick={onScanNext}
              className="w-full py-3.5 px-6 bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-sm rounded-2xl border border-slate-700 flex items-center justify-center space-x-2 transition"
            >
              <ScanLine className="w-4 h-4" />
              <span>Discard & Scan Next</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
