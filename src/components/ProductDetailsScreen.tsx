import React, { useMemo } from 'react';
import { Product } from '../types';
import {
  CheckCircle2,
  ScanLine,
  Layers,
  Palette,
  Hash,
  Barcode,
  IndianRupee,
  Printer,
  Sparkles,
  Cpu,
  Radio,
  Copy,
  Check
} from 'lucide-react';

interface ProductDetailsScreenProps {
  product: Product;
  onScanNext: () => void;
  operatorName?: string;
  terminalId?: string;
}

export const ProductDetailsScreen: React.FC<ProductDetailsScreenProps> = ({
  product,
  onScanNext,
  operatorName = 'Sanjay Sharma',
  terminalId = 'WS-DESK-01',
}) => {
  const [copied, setCopied] = React.useState(false);
  const [boxVerified, setBoxVerified] = React.useState(true);
  const [barcodeVerified, setBarcodeVerified] = React.useState(true);
  const [sealVerified, setSealVerified] = React.useState(true);

  // Format price cleanly
  const formattedPrice = useMemo(() => {
    if (!product.price) return 'N/A';
    const num = typeof product.price === 'number' ? product.price : parseFloat(String(product.price).replace(/[^0-9.]/g, ''));
    if (isNaN(num)) return String(product.price);
    return `₹${num.toLocaleString('en-IN')}`;
  }, [product.price]);

  // Dynamically parse SAR values directly from product.sarValue with zero fallbacks
  const sarBreakdown = useMemo(() => {
    const raw = (product.sarValue || '').trim();
    if (!raw) {
      return {
        body: 'Not Available',
        head: 'Not Available',
        rawDisplay: 'Not Available',
        isAvailable: false,
      };
    }

    const bodyRegex = /(?:Body\s*[:\-–]\s*|Body\s+)([^,;]+)/i;
    const headRegex = /(?:Head\s*[:\-–]\s*|Head\s+)([^,;]+)/i;

    const bodyMatch = raw.match(bodyRegex);
    const headMatch = raw.match(headRegex);

    if (bodyMatch || headMatch) {
      const bodyVal = bodyMatch ? bodyMatch[1].trim() : 'Not Available';
      const headVal = headMatch ? headMatch[1].trim() : 'Not Available';
      return {
        body: bodyVal,
        head: headVal,
        rawDisplay: raw,
        isAvailable: true,
      };
    }

    // Single un-segmented SAR measurement string
    return {
      body: raw,
      head: raw,
      rawDisplay: raw,
      isAvailable: true,
    };
  }, [product.sarValue]);

  const copyEan = () => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(product.ean);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between p-4 md:p-6 lg:p-8">
      <div className="max-w-4xl w-full mx-auto space-y-6">
        {/* Verification Success Header Banner */}
        <div className="bg-gradient-to-r from-emerald-950/80 via-emerald-900/40 to-slate-900 border border-emerald-500/40 rounded-3xl p-5 md:p-6 shadow-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-14 h-14 rounded-2xl bg-emerald-500/20 border border-emerald-400/40 flex items-center justify-center text-emerald-400 shrink-0 shadow-lg shadow-emerald-500/20">
              <CheckCircle2 className="w-8 h-8" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 font-mono font-bold text-xs uppercase tracking-wider border border-emerald-500/30">
                  PASSED QC CHECK
                </span>
                <span className="text-xs text-slate-400 font-mono">
                  {new Date().toLocaleTimeString()}
                </span>
              </div>
              <h2 className="text-xl md:text-2xl font-black text-white mt-1">
                EAN Verified Successfully
              </h2>
              <p className="text-xs text-slate-400">
                Verified by <span className="text-slate-200 font-semibold">{operatorName}</span> on <span className="font-mono text-slate-200">{terminalId}</span>
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2 w-full sm:w-auto">
            <button
              onClick={handlePrint}
              className="flex-1 sm:flex-initial px-4 py-2.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition flex items-center justify-center space-x-1.5"
              title="Print QC Verification Slip"
            >
              <Printer className="w-4 h-4" />
              <span>Print Slip</span>
            </button>
            <button
              onClick={onScanNext}
              className="flex-1 sm:flex-initial px-5 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold transition shadow-lg shadow-emerald-500/20 flex items-center justify-center space-x-1.5"
            >
              <ScanLine className="w-4 h-4" />
              <span>Scan Next</span>
            </button>
          </div>
        </div>

        {/* Product Identity Hero Card */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 md:p-8 shadow-xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-blue-600/5 rounded-full blur-3xl pointer-events-none" />

          {/* Top metadata tags */}
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div className="flex items-center space-x-2">
              <span className="px-3 py-1 rounded-xl bg-blue-500/10 text-blue-400 text-xs font-bold uppercase tracking-wider border border-blue-500/20 flex items-center space-x-1.5">
                <Cpu className="w-3.5 h-3.5" />
                <span>Genuine Product</span>
              </span>
              <span className="px-3 py-1 rounded-xl bg-slate-800 text-slate-300 text-xs font-mono font-medium border border-slate-700">
                DB ID: #{product.id || '1'}
              </span>
            </div>

            {/* Price Tag */}
            <div className="flex items-center space-x-1.5 px-4 py-1.5 rounded-2xl bg-blue-600/20 border border-blue-500/30 text-blue-300">
              <IndianRupee className="w-4 h-4" />
              <span className="text-lg md:text-xl font-extrabold tracking-tight text-white">
                {formattedPrice}
              </span>
            </div>
          </div>

          {/* Model Headline */}
          <h1 className="text-3xl md:text-4xl lg:text-5xl font-black text-white tracking-tight leading-none mb-6">
            {product.model || 'Unknown Model'}
          </h1>

          {/* Spec Badges Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 md:gap-4 mb-6">
            {/* Color */}
            <div className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400 font-semibold uppercase mb-1">
                <Palette className="w-3.5 h-3.5 text-indigo-400" />
                <span>Color</span>
              </div>
              <p className="text-base md:text-lg font-bold text-white truncate">
                {product.color || 'N/A'}
              </p>
            </div>

            {/* Memory / Storage */}
            <div className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400 font-semibold uppercase mb-1">
                <Layers className="w-3.5 h-3.5 text-blue-400" />
                <span>RAM + Storage</span>
              </div>
              <p className="text-base md:text-lg font-bold text-white font-mono truncate">
                {product.memory || 'N/A'}
              </p>
            </div>

            {/* SKU */}
            <div className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center space-x-1.5 text-xs text-slate-400 font-semibold uppercase mb-1">
                <Hash className="w-3.5 h-3.5 text-cyan-400" />
                <span>SKU Code</span>
              </div>
              <p className="text-sm md:text-base font-bold text-white font-mono truncate">
                {product.sku || 'N/A'}
              </p>
            </div>

            {/* EAN Barcode */}
            <div className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80">
              <div className="flex items-center justify-between text-xs text-slate-400 font-semibold uppercase mb-1">
                <span className="flex items-center space-x-1.5">
                  <Barcode className="w-3.5 h-3.5 text-emerald-400" />
                  <span>EAN Barcode</span>
                </span>
                <button
                  onClick={copyEan}
                  className="text-slate-400 hover:text-white transition"
                  title="Copy EAN"
                >
                  {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                </button>
              </div>
              <p className="text-sm md:text-base font-bold text-emerald-300 font-mono tracking-wider truncate">
                {product.ean || 'N/A'}
              </p>
            </div>
          </div>

          {/* SAR Radiation Value Section */}
          <div className="p-5 rounded-2xl bg-gradient-to-br from-slate-950 to-slate-900 border border-slate-700/60 relative overflow-hidden">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center space-x-2">
                <div className="w-7 h-7 rounded-lg bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
                  <Radio className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-slate-200 flex items-center space-x-1.5">
                    <span>SAR Value Compliance</span>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                      Standard: ≤ 1.6 W/kg
                    </span>
                  </h3>
                </div>
              </div>

              <span
                className={`text-xs font-mono font-bold px-2.5 py-0.5 rounded-full border ${
                  sarBreakdown.isAvailable
                    ? 'bg-indigo-500/10 text-indigo-300 border-indigo-500/30'
                    : 'bg-amber-500/10 text-amber-300 border-amber-500/30'
                }`}
              >
                {sarBreakdown.isAvailable ? 'Certified' : 'Not Available'}
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {/* Body SAR */}
              <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800">
                <span className="text-xs text-slate-400 font-semibold block mb-1">
                  Body SAR (10mm / 15mm)
                </span>
                <p className="text-base font-bold text-white font-mono" data-testid="sar-body">
                  {sarBreakdown.body}
                </p>
              </div>

              {/* Head SAR */}
              <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800">
                <span className="text-xs text-slate-400 font-semibold block mb-1">
                  Head SAR (Ear Level)
                </span>
                <p className="text-base font-bold text-white font-mono" data-testid="sar-head">
                  {sarBreakdown.head}
                </p>
              </div>
            </div>

            {/* Exact raw string directly from API response */}
            <div className="mt-3 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400 font-mono">
              <span>API Payload Value:</span>
              <span className="text-slate-300 font-semibold" data-testid="sar-raw">
                {product.sarValue ? product.sarValue : 'Not Available'}
              </span>
            </div>
          </div>
        </div>

        {/* Physical Quality Checklist */}
        <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 md:p-6 shadow-lg">
          <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider mb-4 flex items-center space-x-2">
            <Sparkles className="w-4 h-4 text-blue-400" />
            <span>Operator Physical QC Checklist</span>
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {/* Box condition */}
            <label className="flex items-center space-x-3 p-3 rounded-2xl bg-slate-950/60 border border-slate-800 cursor-pointer hover:border-slate-700 transition">
              <input
                type="checkbox"
                checked={boxVerified}
                onChange={(e) => setBoxVerified(e.target.checked)}
                className="w-5 h-5 rounded text-emerald-500 bg-slate-900 border-slate-700 focus:ring-emerald-500 cursor-pointer"
              />
              <span className="text-xs font-semibold text-slate-300">Package Undamaged</span>
            </label>

            {/* Barcode label */}
            <label className="flex items-center space-x-3 p-3 rounded-2xl bg-slate-950/60 border border-slate-800 cursor-pointer hover:border-slate-700 transition">
              <input
                type="checkbox"
                checked={barcodeVerified}
                onChange={(e) => setBarcodeVerified(e.target.checked)}
                className="w-5 h-5 rounded text-emerald-500 bg-slate-900 border-slate-700 focus:ring-emerald-500 cursor-pointer"
              />
              <span className="text-xs font-semibold text-slate-300">EAN Barcode Scannable</span>
            </label>

            {/* Seal status */}
            <label className="flex items-center space-x-3 p-3 rounded-2xl bg-slate-950/60 border border-slate-800 cursor-pointer hover:border-slate-700 transition">
              <input
                type="checkbox"
                checked={sealVerified}
                onChange={(e) => setSealVerified(e.target.checked)}
                className="w-5 h-5 rounded text-emerald-500 bg-slate-900 border-slate-700 focus:ring-emerald-500 cursor-pointer"
              />
              <span className="text-xs font-semibold text-slate-300">Security Hologram Intact</span>
            </label>
          </div>
        </div>

        {/* Big Bottom Action Button */}
        <div className="pt-2">
          <button
            onClick={onScanNext}
            className="w-full py-4 px-6 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 active:scale-[0.99] text-slate-950 font-extrabold text-base md:text-lg rounded-2xl shadow-xl shadow-emerald-500/25 flex items-center justify-center space-x-2 transition"
          >
            <ScanLine className="w-5 h-5" />
            <span>Verify Next Product (Spacebar / Scan)</span>
          </button>
        </div>
      </div>
    </div>
  );
};
