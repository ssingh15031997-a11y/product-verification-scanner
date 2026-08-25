import React from 'react';
import { ScanLog } from '../types';
import { X, Trash2, CheckCircle2, AlertOctagon, AlertTriangle, Clock, Barcode } from 'lucide-react';

interface HistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  logs: ScanLog[];
  onClearLogs: () => void;
}

export const HistoryModal: React.FC<HistoryModalProps> = ({
  isOpen,
  onClose,
  logs,
  onClearLogs,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-3xl max-w-2xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="p-5 md:p-6 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-blue-600/20 text-blue-400 flex items-center justify-center">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white">Workstation Scan Log History</h2>
              <p className="text-xs text-slate-400">Recorded verification events for this session</p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            {logs.length > 0 && (
              <button
                onClick={onClearLogs}
                className="p-2 rounded-xl bg-slate-800 hover:bg-red-500/20 hover:text-red-400 text-slate-400 border border-slate-700 transition"
                title="Clear Logs"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            )}
            <button
              onClick={onClose}
              className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Modal Body */}
        <div className="p-4 md:p-6 overflow-y-auto space-y-3 flex-1">
          {logs.length === 0 ? (
            <div className="py-12 text-center text-slate-500">
              <Barcode className="w-12 h-12 mx-auto mb-3 opacity-40" />
              <p className="text-sm font-medium">No barcodes scanned yet in this session.</p>
              <p className="text-xs text-slate-600 mt-1">Scanned EAN records will appear here.</p>
            </div>
          ) : (
            logs.map((log) => {
              const isFound = log.status === 'FOUND';
              const isDup = log.status === 'DUPLICATE';
              const timeStr = new Date(log.timestamp).toLocaleTimeString();

              return (
                <div
                  key={log.id}
                  className="p-3.5 rounded-2xl bg-slate-950/80 border border-slate-800/90 flex items-center justify-between gap-3"
                >
                  <div className="flex items-center space-x-3">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${
                        isFound
                          ? 'bg-emerald-500/20 text-emerald-400'
                          : isDup
                          ? 'bg-amber-500/20 text-amber-400'
                          : 'bg-red-500/20 text-red-400'
                      }`}
                    >
                      {isFound ? (
                        <CheckCircle2 className="w-5 h-5" />
                      ) : isDup ? (
                        <AlertTriangle className="w-5 h-5" />
                      ) : (
                        <AlertOctagon className="w-5 h-5" />
                      )}
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="font-mono font-bold text-sm text-white">{log.ean}</span>
                        <span
                          className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded-full ${
                            isFound
                              ? 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/30'
                              : isDup
                              ? 'bg-amber-500/10 text-amber-300 border border-amber-500/30'
                              : 'bg-red-500/10 text-red-300 border border-red-500/30'
                          }`}
                        >
                          {log.status}
                        </span>
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">
                        {log.model ? (
                          <>
                            <strong className="text-slate-200">{log.model}</strong>
                            {log.color && ` (${log.color})`}
                            {log.sku && ` • SKU: ${log.sku}`}
                          </>
                        ) : (
                          'Not registered in database'
                        )}
                      </p>
                    </div>
                  </div>

                  <span className="text-xs text-slate-500 font-mono shrink-0">{timeStr}</span>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};
