import React, { useEffect, useRef, useState } from 'react';
import { Html5Qrcode, Html5QrcodeSupportedFormats } from 'html5-qrcode';
import { RefreshCw, Zap, ZapOff, AlertCircle } from 'lucide-react';

interface CameraScannerProps {
  onScan: (decodedText: string) => void;
  isActive: boolean;
  onClose?: () => void;
}

export const CameraScanner: React.FC<CameraScannerProps> = ({ onScan, isActive }) => {
  const [isCameraRunning, setIsCameraRunning] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [hasTorch, setHasTorch] = useState(false);
  const [torchOn, setTorchOn] = useState(false);
  const [facingMode, setFacingMode] = useState<'environment' | 'user'>('environment');

  const scannerRef = useRef<Html5Qrcode | null>(null);
  const containerId = 'interactive-camera-scanner';
  const lastScanTimeRef = useRef<number>(0);

  useEffect(() => {
    if (!isActive) {
      stopCamera();
      return;
    }

    let isMounted = true;

    const startScanner = async () => {
      try {
        setErrorMessage(null);
        if (!scannerRef.current) {
          scannerRef.current = new Html5Qrcode(containerId, {
            formatsToSupport: [
              Html5QrcodeSupportedFormats.EAN_13,
              Html5QrcodeSupportedFormats.EAN_8,
              Html5QrcodeSupportedFormats.UPC_A,
              Html5QrcodeSupportedFormats.UPC_E,
              Html5QrcodeSupportedFormats.CODE_128,
              Html5QrcodeSupportedFormats.CODE_39,
            ],
            verbose: false,
          });
        }

        const config = {
          fps: 15,
          qrbox: { width: 280, height: 160 },
          aspectRatio: 1.3333,
        };

        await scannerRef.current.start(
          { facingMode: facingMode },
          config,
          (decodedText) => {
            const now = Date.now();
            // Debounce rapid repeats of same code (800ms)
            if (now - lastScanTimeRef.current > 800) {
              lastScanTimeRef.current = now;
              onScan(decodedText);
            }
          },
          () => {
            // Frame scan without barcode match, ignore
          }
        );

        if (isMounted) {
          setIsCameraRunning(true);
          // Check for torch capability
          try {
            const capabilities = scannerRef.current.getRunningTrackCapabilities() as MediaTrackCapabilities & { torch?: boolean };
            if (capabilities && 'torch' in capabilities) {
              setHasTorch(Boolean(capabilities.torch));
            }
          } catch {
            setHasTorch(false);
          }
        }
      } catch (err: unknown) {
        if (isMounted) {
          setIsCameraRunning(false);
          const msg = err instanceof Error ? err.message : String(err);
          if (msg.includes('NotAllowedError') || msg.includes('Permission')) {
            setErrorMessage('Camera access denied. Please grant camera permissions.');
          } else if (msg.includes('NotFoundError')) {
            setErrorMessage('No camera device detected on this workstation.');
          } else {
            setErrorMessage('Camera unavailable. Use USB Barcode Scanner or Manual EAN entry.');
          }
        }
      }
    };

    startScanner();

    return () => {
      isMounted = false;
      stopCamera();
    };
  }, [isActive, facingMode, onScan]);

  const stopCamera = async () => {
    if (scannerRef.current && scannerRef.current.isScanning) {
      try {
        await scannerRef.current.stop();
      } catch {
        // Ignore stop error
      }
    }
    setIsCameraRunning(false);
    setTorchOn(false);
  };

  const toggleTorch = async () => {
    if (!scannerRef.current || !hasTorch) return;
    try {
      const nextState = !torchOn;
      await scannerRef.current.applyVideoConstraints({
        advanced: [{ torch: nextState } as MediaTrackConstraintSet],
      });
      setTorchOn(nextState);
    } catch {
      // Ignore
    }
  };

  const switchCamera = () => {
    stopCamera().then(() => {
      setFacingMode((prev) => (prev === 'environment' ? 'user' : 'environment'));
    });
  };

  return (
    <div className="relative w-full max-w-lg mx-auto bg-slate-950 rounded-2xl overflow-hidden border border-slate-700/60 shadow-2xl">
      {/* Video Container */}
      <div id={containerId} className="w-full aspect-[4/3] bg-black relative" />

      {/* Targeting Overlay when scanning */}
      {isCameraRunning && (
        <div className="absolute inset-0 pointer-events-none flex flex-col items-center justify-center p-6">
          <div className="relative w-64 h-36 rounded-xl border-2 border-emerald-400/80 shadow-[0_0_20px_rgba(16,185,129,0.35)]">
            {/* Corner Reticle Markers */}
            <div className="absolute -top-1 -left-1 w-5 h-5 border-t-4 border-l-4 border-emerald-400 rounded-tl" />
            <div className="absolute -top-1 -right-1 w-5 h-5 border-t-4 border-r-4 border-emerald-400 rounded-tr" />
            <div className="absolute -bottom-1 -left-1 w-5 h-5 border-b-4 border-l-4 border-emerald-400 rounded-bl" />
            <div className="absolute -bottom-1 -right-1 w-5 h-5 border-b-4 border-r-4 border-emerald-400 rounded-br" />

            {/* Laser scanning beam line */}
            <div className="absolute left-2 right-2 h-0.5 bg-gradient-to-r from-emerald-400 via-cyan-300 to-emerald-400 shadow-[0_0_8px_#34d399] animate-scan-beam" />
          </div>
          <span className="mt-4 px-3 py-1 rounded-full bg-black/60 backdrop-blur-md text-emerald-400 text-xs font-mono font-bold tracking-wider uppercase border border-emerald-500/30">
            Align Barcode inside frame
          </span>
        </div>
      )}

      {/* Error state */}
      {errorMessage && (
        <div className="absolute inset-0 bg-slate-900/95 flex flex-col items-center justify-center p-6 text-center">
          <div className="w-12 h-12 rounded-full bg-red-500/20 text-red-400 flex items-center justify-center mb-3">
            <AlertCircle className="w-6 h-6" />
          </div>
          <p className="text-sm font-medium text-slate-200 mb-2">{errorMessage}</p>
          <p className="text-xs text-slate-400 mb-4 max-w-xs">
            You can still verify products by typing the EAN or using a USB/Bluetooth barcode scanner.
          </p>
        </div>
      )}

      {/* Bottom Controls Bar */}
      <div className="p-3 bg-slate-900/90 backdrop-blur-md border-t border-slate-800 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <span className={`w-2.5 h-2.5 rounded-full ${isCameraRunning ? 'bg-emerald-500 animate-pulse' : 'bg-slate-500'}`} />
          <span className="text-xs font-semibold text-slate-300">
            {isCameraRunning ? 'Camera Live' : 'Camera Standby'}
          </span>
        </div>

        <div className="flex items-center space-x-2">
          {hasTorch && isCameraRunning && (
            <button
              onClick={toggleTorch}
              className={`p-2 rounded-lg text-xs font-medium transition ${
                torchOn ? 'bg-amber-500 text-slate-950 font-bold' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
              }`}
              title="Toggle Flashlight"
            >
              {torchOn ? <Zap className="w-4 h-4" /> : <ZapOff className="w-4 h-4" />}
            </button>
          )}

          <button
            onClick={switchCamera}
            className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
            title="Switch Camera (Front/Rear)"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
