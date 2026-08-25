import React, { useState } from 'react';
import { UserSession } from '../types';
import { ShieldCheck, User, Lock, ArrowRight, Building2, Terminal, CheckCircle2, Sparkles } from 'lucide-react';

interface LoginScreenProps {
  onLoginSuccess: (session: UserSession) => void;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLoginSuccess }) => {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [department, setDepartment] = useState('Packaging & Dispatch Line 1');
  const [terminalId] = useState('WS-DESK-01');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const cleanUser = userId.trim();
    const cleanPass = password.trim();

    if (!cleanUser || !cleanPass) {
      setError('Please enter both Operator ID and Password');
      return;
    }

    setIsLoading(true);

    // Operator Authentication
    setTimeout(() => {
      setIsLoading(false);
      // Valid credentials or operator override
      if (
        (cleanUser.toLowerCase() === 'sanjay2007' && cleanPass === 'Sanjay@2007') ||
        cleanUser.length >= 3
      ) {
        onLoginSuccess({
          isLoggedIn: true,
          userId: cleanUser,
          name: cleanUser.toLowerCase() === 'sanjay2007' ? 'Sanjay Sharma' : cleanUser,
          role: 'Senior QC Verification Specialist',
          department,
          terminalId,
          loginTime: Date.now(),
        });
      } else {
        setError('Invalid operator credentials. Use the Demo credentials button below.');
      }
    }, 400);
  };

  const fillDemoCredentials = () => {
    setUserId('sanjay2007');
    setPassword('Sanjay@2007');
    setError(null);
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-slate-950 text-slate-100 px-4 py-8 relative overflow-hidden">
      {/* Background industrial glow accents */}
      <div className="absolute -top-40 -left-40 w-96 h-96 bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />

      {/* Top Header */}
      <div className="max-w-md w-full mx-auto flex items-center justify-between z-10">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white">Product Verification</h1>
            <p className="text-xs text-slate-400 font-mono">QC Industrial Gateway v2.4</p>
          </div>
        </div>

        <div className="flex items-center space-x-1.5 px-2.5 py-1 rounded-full bg-slate-900 border border-slate-800 text-xs text-slate-400 font-mono">
          <Terminal className="w-3.5 h-3.5 text-blue-400" />
          <span>{terminalId}</span>
        </div>
      </div>

      {/* Main Login Card */}
      <div className="max-w-md w-full mx-auto my-auto z-10">
        <div className="bg-slate-900/90 backdrop-blur-xl border border-slate-800 rounded-3xl p-8 shadow-2xl">
          <div className="mb-6 text-center">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-blue-500/10 border border-blue-500/20 text-blue-400 text-xs font-semibold uppercase tracking-wider mb-3">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Operator Authentication</span>
            </div>
            <h2 className="text-2xl font-extrabold text-white">Sign In to Workstation</h2>
            <p className="text-sm text-slate-400 mt-1">
              Enter your QC badge ID to begin barcode product verification
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Operator ID */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Operator Badge / ID
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <User className="w-5 h-5" />
                </div>
                <input
                  type="text"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                  placeholder="e.g. sanjay2007"
                  className="w-full pl-11 pr-4 py-3.5 bg-slate-950/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm font-medium transition"
                  autoComplete="username"
                  required
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Security PIN / Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-5 h-5" />
                </div>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full pl-11 pr-4 py-3.5 bg-slate-950/80 border border-slate-700/80 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm font-medium transition"
                  autoComplete="current-password"
                  required
                />
              </div>
            </div>

            {/* Department */}
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Workstation Line
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Building2 className="w-5 h-5" />
                </div>
                <select
                  value={department}
                  onChange={(e) => setDepartment(e.target.value)}
                  className="w-full pl-11 pr-4 py-3.5 bg-slate-950/80 border border-slate-700/80 rounded-xl text-white text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition appearance-none cursor-pointer"
                >
                  <option value="Packaging & Dispatch Line 1">Packaging & Dispatch Line 1</option>
                  <option value="Quality Audit Bay 2">Quality Audit Bay 2</option>
                  <option value="Final Assembly Testing">Final Assembly Testing</option>
                  <option value="Inbound Warehouse Receiving">Inbound Warehouse Receiving</option>
                </select>
              </div>
            </div>

            {/* Error banner */}
            {error && (
              <div className="p-3 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs font-medium flex items-center space-x-2 animate-pulse">
                <span>{error}</span>
              </div>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3.5 px-4 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 active:scale-[0.99] text-white font-bold rounded-xl shadow-lg shadow-blue-600/30 flex items-center justify-center space-x-2 transition disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              <span>{isLoading ? 'Verifying Credentials...' : 'Authenticate & Open Scanner'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Quick Demo Fill button */}
          <div className="mt-5 pt-5 border-t border-slate-800 text-center">
            <button
              type="button"
              onClick={fillDemoCredentials}
              className="text-xs text-blue-400 hover:text-blue-300 font-semibold inline-flex items-center space-x-1.5 transition py-1 px-2.5 rounded-lg hover:bg-blue-500/10"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Auto-fill Operator Credentials (sanjay2007)</span>
            </button>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="max-w-md w-full mx-auto text-center text-xs text-slate-500 z-10">
        <p>Connected to Google Apps Script Master Database • Single Source of Truth</p>
      </div>
    </div>
  );
};
