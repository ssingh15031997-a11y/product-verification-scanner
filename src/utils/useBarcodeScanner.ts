import { useEffect, useRef } from 'react';

interface UseBarcodeScannerOptions {
  onScan: (ean: string) => void;
  minChars?: number;
  maxKeyInterval?: number;
  enabled?: boolean;
}

/**
 * Listens for hardware barcode scanner bursts (USB / Bluetooth scanners acting as keyboard wedges).
 */
export function useBarcodeScanner({
  onScan,
  minChars = 4,
  maxKeyInterval = 60,
  enabled = true,
}: UseBarcodeScannerOptions): void {
  const bufferRef = useRef<string>('');
  const lastKeyTimeRef = useRef<number>(0);

  useEffect(() => {
    if (!enabled) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      const target = e.target as HTMLElement | null;
      const isInput = target?.tagName === 'INPUT' || target?.tagName === 'TEXTAREA';

      const now = Date.now();
      const timeSinceLastKey = now - lastKeyTimeRef.current;
      lastKeyTimeRef.current = now;

      if (e.key === 'Enter' || e.key === 'Tab') {
        const buffered = bufferRef.current.trim();
        bufferRef.current = '';

        if (buffered.length >= minChars && /^\d+$/.test(buffered)) {
          if (isInput) {
            e.preventDefault();
          }
          onScan(buffered);
        }
        return;
      }

      if (e.key.length === 1) {
        if (timeSinceLastKey > maxKeyInterval && bufferRef.current.length > 0) {
          bufferRef.current = '';
        }

        bufferRef.current += e.key;

        setTimeout(() => {
          if (Date.now() - lastKeyTimeRef.current >= 1500) {
            bufferRef.current = '';
          }
        }, 1600);
      }
    };

    window.addEventListener('keydown', handleKeyDown, true);
    return () => {
      window.removeEventListener('keydown', handleKeyDown, true);
    };
  }, [onScan, minChars, maxKeyInterval, enabled]);
}
